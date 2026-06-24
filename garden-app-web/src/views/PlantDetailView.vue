<template>
  <div class="container">
    <button @click="router.back()" class="back-btn">← 戻る</button>

    <div v-if="plant">
      <h1>{{ plant.name }}</h1>
      <p>種類：{{ typeLabel(plant.type) }}</p>
      <p>植えた日：{{ plant.plantedAt }}</p>
      <p v-if="plant.memo">メモ：{{ plant.memo }}</p>
    </div>

    <!-- カレンダー -->
    <CareCalendar :care-logs="careLogs" :selected-date="selectedDate" @select-date="selectedDate = $event" />

    <div class="care-section">
      <h2>ケア記録<span v-if="selectedDate" class="selected-date-label">（{{ selectedDate }}）</span></h2>

      <div class="add-care-form">
        <div class="care-type-select">
          <button
            v-for="option in careTypeOptions"
            :key="option.value"
            type="button"
            class="care-type-btn"
            :class="{ active: newCareLog.careType === option.value }"
            @click="newCareLog.careType = option.value"
          >{{ option.label }}</button>
        </div>
        <p class="care-date-display">登録日：{{ selectedDate || '（カレンダーで日付を選択してください）' }}</p>
        <input v-model="newCareLog.memo" type="text" placeholder="メモ" />
        <div class="photo-select-row">
          <input ref="cameraInput" type="file" accept="image/jpeg,image/png,image/webp" capture="environment" @change="handlePhotoSelect" class="hidden-file-input" />
          <input ref="galleryInput" type="file" accept="image/jpeg,image/png,image/webp" @change="handlePhotoSelect" class="hidden-file-input" />
          <button type="button" @click="cameraInput.click()">📷 撮影</button>
          <button type="button" @click="galleryInput.click()">🖼 ギャラリーから選択</button>
          <span v-if="selectedPhoto" class="selected-photo-name">{{ selectedPhoto.name }}</span>
        </div>
        <button :disabled="!selectedDate" @click="handleCreateCareLog">記録追加</button>
      </div>

      <div v-if="filteredCareLogs.length === 0">
        <p>ケア記録がありません</p>
      </div>
      <div v-for="log in filteredCareLogs" :key="log.id" class="care-card">
        <img v-if="log.photoUrl" :src="log.photoUrl" alt="ケア記録の写真" class="care-photo" />
        <span class="care-type">{{ careTypeLabel(log.careType) }}</span>
        <span class="care-date">{{ log.caredAt }}</span>
        <span v-if="log.memo" class="care-memo">{{ log.memo }}</span>
        <button @click="handleDeleteCareLog(log.id)" class="delete-btn">削除</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPlants } from '@/api/plants'
import { getCareLogs, createCareLog, deleteCareLog, uploadCareLogPhoto } from '@/api/careLogs'
import CareCalendar from '@/components/CareCalendar.vue'

const route = useRoute()
const router = useRouter()

const plant = ref(null)
const careLogs = ref([])
const newCareLog = ref({
  careType: '',
  memo: ''
})
const cameraInput = ref(null)
const galleryInput = ref(null)
const selectedPhoto = ref(null)
const selectedDate = ref(null)

// caredAt（"2026-06-11T09:30:00"形式）から日付部分だけ取り出す
const toDateStr = (caredAt) => caredAt ? caredAt.split('T')[0] : null

const filteredCareLogs = computed(() => {
  if (!selectedDate.value) return []
  return careLogs.value.filter(log => toDateStr(log.caredAt) === selectedDate.value)
})

// ケア記録の中で最新の日付を選択状態にする
function selectLatestDate() {
  const dates = careLogs.value.map(log => toDateStr(log.caredAt)).filter(Boolean)
  selectedDate.value = dates.length ? dates.sort().at(-1) : null
}

const typeLabel = (type) => ({
  vegetable: '野菜', fruit: '果物', herb: 'ハーブ',
  flower: '花', tree: '樹木', other: 'その他'
}[type] || type)

const careTypeLabel = (type) => ({
  water: '💧水やり', fertilize: '🌿肥料',
  harvest: '🌾収穫', other: 'その他'
}[type] || type)

const careTypeOptions = [
  { value: 'water', label: '💧水やり' },
  { value: 'fertilize', label: '🌿肥料' },
  { value: 'harvest', label: '🌾収穫' },
  { value: 'other', label: 'その他' }
]

onMounted(async () => {
  const plantId = route.params.id
  const plantsRes = await getPlants()
  plant.value = plantsRes.data.find(p => p.id === plantId)
  const logsRes = await getCareLogs(plantId)
  careLogs.value = logsRes.data
  selectLatestDate()
})

const PHOTO_MAX_SIZE = 1600
const PHOTO_QUALITY = 0.8

// スマホのカメラ画像は数MB～十数MBになることがあり、そのまま処理すると
// 低スペック端末でブラウザがメモリ不足になることがあるため、
// 縮小・再エンコードしてから保持する
async function compressImage(file) {
  const bitmap = await createImageBitmap(file)
  let { width, height } = bitmap
  if (width > PHOTO_MAX_SIZE || height > PHOTO_MAX_SIZE) {
    const ratio = Math.min(PHOTO_MAX_SIZE / width, PHOTO_MAX_SIZE / height)
    width = Math.round(width * ratio)
    height = Math.round(height * ratio)
  }
  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height
  canvas.getContext('2d').drawImage(bitmap, 0, 0, width, height)
  bitmap.close()
  const blob = await new Promise((resolve) => canvas.toBlob(resolve, 'image/jpeg', PHOTO_QUALITY))
  return new File([blob], file.name.replace(/\.\w+$/, '.jpg'), { type: 'image/jpeg' })
}

async function handlePhotoSelect(event) {
  const file = event.target.files[0]
  selectedPhoto.value = file ? await compressImage(file) : null
}

async function handleCreateCareLog() {
  if (!newCareLog.value.careType || !selectedDate.value) return
  // 登録日はカレンダーで選択中の日付を使い、時刻だけ現在時刻を使う
  const nowTime = new Date().toTimeString().slice(0, 5)
  const payload = { ...newCareLog.value, caredAt: `${selectedDate.value}T${nowTime}` }
  const created = await createCareLog(route.params.id, payload)
  if (selectedPhoto.value) {
    await uploadCareLogPhoto(route.params.id, created.data.id, selectedPhoto.value)
  }
  const res = await getCareLogs(route.params.id)
  careLogs.value = res.data
  newCareLog.value = { careType: '', memo: '' }
  selectedPhoto.value = null
  if (cameraInput.value) cameraInput.value.value = ''
  if (galleryInput.value) galleryInput.value.value = ''
}

async function handleDeleteCareLog(id) {
  await deleteCareLog(route.params.id, id)
  careLogs.value = careLogs.value.filter(l => l.id !== id)
}
</script>

<style scoped>
.container { max-width: 800px; margin: 0 auto; padding: 2rem; }
.back-btn { background: #999; color: white; border: none; padding: 0.5rem 1rem; border-radius: 6px; cursor: pointer; margin-bottom: 1.5rem; }
.care-section { margin-top: 2rem; }
.selected-date-label { font-size: 0.9rem; color: #999; font-weight: normal; }
.add-care-form { display: flex; flex-direction: column; gap: 0.75rem; margin-bottom: 1.5rem; padding: 1.5rem; background: #f9f9f9; border-radius: 8px; }
input, select { padding: 0.75rem; border: 1px solid #ccc; border-radius: 6px; font-size: 1rem; }
button { padding: 0.75rem 1.5rem; background: #4a9d5f; color: white; border: none; border-radius: 6px; cursor: pointer; }
button:disabled { background: #ccc; cursor: not-allowed; }
.care-date-display { margin: 0; color: #666; font-size: 0.95rem; }
.care-type-select { display: flex; gap: 0.5rem; flex-wrap: wrap; }
.care-type-btn { background: #fff; color: #333; border: 1px solid #ccc; padding: 0.6rem 1rem; font-size: 0.9rem; }
.care-type-btn.active { background: #4a9d5f; color: #fff; border-color: #4a9d5f; }
.photo-select-row { display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; }
.hidden-file-input { display: none; }
.photo-select-row button { background: #4a7a9d; padding: 0.6rem 1rem; font-size: 0.9rem; }
.selected-photo-name { color: #666; font-size: 0.85rem; }
.care-card { display: flex; gap: 1rem; align-items: center; padding: 0.75rem 1rem; border: 1px solid #eee; border-radius: 8px; margin-bottom: 0.5rem; }
.care-photo { width: 48px; height: 48px; object-fit: cover; border-radius: 6px; flex-shrink: 0; }
.care-type { font-weight: 500; min-width: 80px; }
.care-date { color: #999; }
.care-memo { color: #666; flex: 1; }
.delete-btn { background: #e74c3c; padding: 0.4rem 0.8rem; margin-left: auto; }
</style>
