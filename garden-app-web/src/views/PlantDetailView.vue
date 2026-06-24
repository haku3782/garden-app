<template>
  <div class="container">
    <button @click="router.back()" class="back-btn">← 戻る</button>

    <div v-if="plant" class="plant-card">
      <button @click="handleDeletePlant" class="delete-plant-btn">削除</button>
      <div class="plant-card-body">
        <img v-if="plant.latestPhotoUrl" :src="plant.latestPhotoUrl" alt="" class="plant-detail-thumbnail" />
        <div v-else class="plant-detail-thumbnail plant-thumbnail-placeholder"></div>
        <div class="plant-info-col">
          <h1>{{ plant.name }}</h1>
          <span class="plant-type-badge">{{ typeLabel(plant.type) }}</span>
          <p v-if="plant.memo" class="plant-memo">{{ plant.memo }}</p>
        </div>
      </div>
    </div>

    <h2 class="care-heading">ケア記録<span v-if="selectedDate" class="selected-date-label">（{{ selectedDate }}）</span></h2>

    <!-- カレンダー -->
    <CareCalendar :care-logs="careLogs" :selected-date="selectedDate" @select-date="selectedDate = $event" />

    <div class="care-section">
      <button @click="router.push(`/plants/${route.params.id}/care/new`)" class="add-care-btn">＋ ケア記録を追加</button>

      <!-- 編集時の写真選択用（v-forの外に置き、編集中のログにのみ使う） -->
      <input ref="editCameraInput" type="file" accept="image/jpeg,image/png,image/webp" capture="environment" @change="handleEditPhotoSelect" class="hidden-file-input" />
      <input ref="editGalleryInput" type="file" accept="image/jpeg,image/png,image/webp" @change="handleEditPhotoSelect" class="hidden-file-input" />

      <p v-if="filteredCareLogs.length === 0" class="empty-message">ケア記録がありません</p>
      <div v-for="log in filteredCareLogs" :key="log.id" class="care-card">
        <div v-if="editingId === log.id" class="edit-care-form">
          <div class="care-type-select">
            <button
              v-for="option in careTypeOptions"
              :key="option.value"
              type="button"
              class="care-type-btn"
              :class="{ active: editForm.careType === option.value }"
              @click="editForm.careType = option.value"
            >{{ option.label }}</button>
          </div>
          <input v-model="editForm.caredAt" type="datetime-local" />
          <input v-model="editForm.memo" type="text" placeholder="メモ" />
          <div class="edit-photo-row">
            <template v-if="log.photoUrl">
              <img :src="log.photoUrl" alt="ケア記録の写真" class="care-photo" />
              <button type="button" class="delete-photo-btn" @click="handleDeletePhoto(log)">🗑 写真を削除</button>
            </template>
            <template v-else>
              <button type="button" @click="editCameraInput.click()">📷 撮影</button>
              <button type="button" @click="editGalleryInput.click()">🏞️ ギャラリーから選択</button>
            </template>
          </div>
          <div class="edit-actions">
            <button @click="saveEdit(log)">保存</button>
            <button type="button" class="cancel-btn" @click="cancelEdit">キャンセル</button>
          </div>
        </div>
        <template v-else>
          <img v-if="log.photoUrl" :src="log.photoUrl" alt="ケア記録の写真" class="care-photo" />
          <div v-else class="care-photo care-photo-placeholder"></div>
          <div class="care-info">
            <div class="care-info-top">
              <span class="care-type-badge" :style="{ background: careTypeColor(log.careType) }">{{ careTypeLabel(log.careType) }}</span>
              <span class="care-date">{{ formatDateTime(log.caredAt) }}</span>
            </div>
            <p v-if="log.memo" class="care-memo">{{ log.memo }}</p>
          </div>
          <div class="care-actions">
            <button class="edit-btn" title="編集" @click="startEdit(log)">✏️</button>
            <button class="delete-btn" title="削除" @click="handleDeleteCareLog(log.id)">🗑️</button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPlants, deletePlant } from '@/api/plants'
import { getCareLogs, updateCareLog, deleteCareLog, uploadCareLogPhoto, deleteCareLogPhoto } from '@/api/careLogs'
import CareCalendar from '@/components/CareCalendar.vue'

const route = useRoute()
const router = useRouter()

const plant = ref(null)
const careLogs = ref([])
const selectedDate = ref(null)
const editingId = ref(null)
const editForm = ref({ careType: '', caredAt: '', memo: '' })
const editCameraInput = ref(null)
const editGalleryInput = ref(null)

// caredAt（"2026-06-11T09:30:00"形式）から日付部分だけ取り出す
const toDateStr = (caredAt) => caredAt ? caredAt.split('T')[0] : null

// caredAtを "yyyy/mm/dd hh:mm" 表記に整形する
function formatDateTime(caredAt) {
  if (!caredAt) return ''
  const [datePart, timePart] = caredAt.split('T')
  const [y, m, d] = datePart.split('-')
  return `${y}/${m}/${d} ${timePart ? timePart.slice(0, 5) : ''}`
}

const filteredCareLogs = computed(() => {
  if (!selectedDate.value) return []
  return careLogs.value.filter(log => toDateStr(log.caredAt) === selectedDate.value)
})

// ケア記録の中で最新の日付を選択状態にする
function selectLatestDate() {
  const dates = careLogs.value.map(log => toDateStr(log.caredAt)).filter(Boolean)
  selectedDate.value = dates.length ? dates.sort().at(-1) : null
}

async function handleDeletePlant() {
  if (!confirm('この植物を削除しますか？')) return
  await deletePlant(route.params.id)
  router.push('/plants')
}

const typeLabel = (type) => ({
  vegetable: '野菜', fruit: '果物', herb: 'ハーブ',
  flower: '花', tree: '樹木', other: 'その他'
}[type] || type)

const careTypeLabel = (type) => ({
  water: '水やり', fertilize: '肥料',
  harvest: '収穫', other: 'その他'
}[type] || type)

// calendarのドット表示と同じ配色
const careTypeColor = (type) => ({
  water: '#4a90d9', fertilize: '#4a9d5f', harvest: '#d9a92a', other: '#9b9b9b'
}[type] || '#9b9b9b')

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

function startEdit(log) {
  editingId.value = log.id
  editForm.value = { careType: log.careType, caredAt: log.caredAt.slice(0, 16), memo: log.memo || '' }
}

function cancelEdit() {
  editingId.value = null
}

async function saveEdit(log) {
  await updateCareLog(route.params.id, log.id, editForm.value)
  const res = await getCareLogs(route.params.id)
  careLogs.value = res.data
  editingId.value = null
}

async function handleEditPhotoSelect(event) {
  const file = event.target.files[0]
  if (!file || !editingId.value) return
  const compressed = await compressImage(file)
  await uploadCareLogPhoto(route.params.id, editingId.value, compressed)
  const res = await getCareLogs(route.params.id)
  careLogs.value = res.data
  event.target.value = ''
}

async function handleDeletePhoto(log) {
  await deleteCareLogPhoto(route.params.id, log.id)
  const res = await getCareLogs(route.params.id)
  careLogs.value = res.data
}

async function handleDeleteCareLog(id) {
  await deleteCareLog(route.params.id, id)
  careLogs.value = careLogs.value.filter(l => l.id !== id)
}
</script>

<style scoped>
.container {
  --color-primary: #4a9d5f;
  --color-primary-dark: #3d8350;
  --color-accent: #4a7a9d;
  --color-danger: #e74c3c;
  --color-muted: #767676;
  --color-border: #e3e3e3;
  --color-bg-soft: #f7f8f7;
  --radius: 10px;
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.06);
  --shadow-md: 0 2px 10px rgba(0, 0, 0, 0.08);

  max-width: 800px;
  margin: 0 auto;
  padding: 1rem;
  color: #2d3436;
}

.back-btn {
  background: transparent;
  color: var(--color-muted);
  border: 1px solid var(--color-border);
  padding: 0.45rem 0.8rem;
  font-size: 0.85rem;
  border-radius: var(--radius);
  cursor: pointer;
  margin-bottom: 1.5rem;
}
.back-btn:hover { background: var(--color-bg-soft); color: #2d3436; }

.plant-card { position: relative; padding: 1rem; border: 1px solid var(--color-border); border-radius: var(--radius); background: #fff; box-shadow: var(--shadow-sm); }
.plant-card-body { display: flex; align-items: center; gap: 1rem; padding-right: 3.5rem; }
.plant-detail-thumbnail { width: 80px; height: 80px; object-fit: cover; border-radius: var(--radius); flex-shrink: 0; }
.plant-thumbnail-placeholder { background: var(--color-bg-soft); }
.plant-info-col { display: flex; flex-direction: column; gap: 0.4rem; min-width: 0; }
.plant-info-col h1 { font-size: 1.25rem; margin: 0; }
.plant-type-badge { align-self: flex-start; background: var(--color-bg-soft); color: #2d3436; border: 1px solid var(--color-border); padding: 0.2rem 0.7rem; font-size: 0.8rem; border-radius: 999px; }
.plant-memo { margin: 0; color: var(--color-muted); font-size: 0.9rem; }
.delete-plant-btn {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  background: transparent;
  color: var(--color-danger);
  border: 1px solid var(--color-border);
  padding: 0.35rem 0.7rem;
  font-size: 0.8rem;
}
.delete-plant-btn:hover { background: var(--color-bg-soft); }

.care-heading { font-size: 1.05rem; margin: 1.5rem 0 0; }
.selected-date-label { font-size: 0.9rem; color: var(--color-muted); font-weight: normal; }

.care-section { margin-top: 1rem; }
.add-care-btn { margin-bottom: 1.5rem; }

input {
  padding: 0.7rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  font-size: 1rem;
  background: #fff;
  transition: border-color 0.15s, box-shadow 0.15s;
}
input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(74, 157, 95, 0.15);
}

button {
  padding: 0.65rem 1.1rem;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius);
  font-size: 0.9rem;
  cursor: pointer;
  transition: background-color 0.15s, transform 0.05s;
}
button:hover { background: var(--color-primary-dark); }
button:active { transform: scale(0.97); }
button:disabled { background: var(--color-border); color: var(--color-muted); cursor: not-allowed; }

.care-type-select { display: flex; gap: 0.5rem; flex-wrap: wrap; }
.care-type-btn {
  background: #fff;
  color: #2d3436;
  border: 1px solid var(--color-border);
  padding: 0.45rem 0.9rem;
  font-size: 0.85rem;
  border-radius: 999px;
}
.care-type-btn.active { background: var(--color-primary); color: #fff; border-color: var(--color-primary); }

.hidden-file-input { display: none; }

.empty-message { color: var(--color-muted); min-height: 120px; display: flex; align-items: center; }

.care-card {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  align-items: center;
  padding: 0.85rem 1rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: #fff;
  box-shadow: var(--shadow-sm);
  margin-bottom: 0.6rem;
  transition: box-shadow 0.15s;
}
.care-card:hover { box-shadow: var(--shadow-md); }
.care-photo { width: 56px; height: 56px; object-fit: cover; border-radius: var(--radius); flex-shrink: 0; }
.care-photo-placeholder { background: var(--color-bg-soft); }
.care-info { display: flex; flex-direction: column; gap: 0.3rem; min-width: 0; flex: 1; }
.care-info-top { display: flex; align-items: center; gap: 0.6rem; flex-wrap: wrap; }
.care-type-badge { color: #fff; padding: 0.2rem 0.7rem; font-size: 0.8rem; border-radius: 999px; }
.care-date { color: var(--color-muted); font-size: 0.85rem; }
.care-memo { margin: 0; color: #555; font-size: 0.9rem; }

.care-actions { display: flex; gap: 0.1rem; flex-shrink: 0; margin-left: auto; }
.care-actions button {
  background: transparent;
  border: none;
  padding: 0.4rem;
  font-size: 1rem;
  line-height: 1;
}
.edit-btn:hover { background: var(--color-bg-soft); }
.delete-btn:hover { background: var(--color-bg-soft); }

.edit-care-form { display: flex; flex-direction: column; gap: 0.5rem; width: 100%; }
.edit-actions { display: flex; gap: 0.5rem; }
.cancel-btn {
  background: transparent;
  color: var(--color-muted);
  border: 1px solid var(--color-border);
}
.cancel-btn:hover { background: var(--color-bg-soft); color: #2d3436; }

.edit-photo-row { display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; }
.edit-photo-row button { background: var(--color-accent); padding: 0.6rem 1rem; font-size: 0.9rem; }
.edit-photo-row button:hover { background: #3d6584; }
.delete-photo-btn { background: var(--color-danger) !important; }

@media (min-width: 768px) {
  .container { max-width: 900px; padding: 2rem; }
}

@media (min-width: 1024px) {
  .container { max-width: 1000px; }
  .plant-info-col h1 { font-size: 1.6rem; }
}
</style>
