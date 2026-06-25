<template>
  <div class="container">
    <div class="top-bar">
      <button @click="router.back()" class="back-btn">← 戻る</button>
      <button @click="router.push('/plants')" class="back-btn">TOPへ</button>
    </div>
    <h1>記録を追加</h1>

    <div class="add-care-form">
      <div class="photo-select-row">
        <input ref="cameraInput" type="file" accept="image/jpeg,image/png,image/webp" capture="environment" @change="handlePhotoSelect" class="hidden-file-input" />
        <input ref="galleryInput" type="file" accept="image/jpeg,image/png,image/webp" @change="handlePhotoSelect" class="hidden-file-input" />
        <button type="button" class="camera-btn" @click="cameraInput.click()">◎ 撮影</button>
        <button type="button" @click="galleryInput.click()">▭ <span class="select-text-mobile">選択</span><span class="select-text-desktop">写真を選択</span></button>
        <span v-if="selectedPhoto" class="selected-photo-name">{{ selectedPhoto.name }}</span>
      </div>
      <div class="care-type-select">
        <button
          v-for="option in careTypeOptions"
          :key="option.value"
          type="button"
          class="care-type-btn"
          :class="{ active: selectedTypes.includes(option.value) }"
          :style="selectedTypes.includes(option.value) ? { background: careTypeColor(option.value), borderColor: careTypeColor(option.value) } : {}"
          @click="toggleType(option.value)"
        >{{ option.label }}</button>
      </div>
      <input v-model="newCareLog.caredAt" type="datetime-local" />
      <input v-model="newCareLog.memo" type="text" placeholder="コメント" />
      <div class="add-care-actions">
        <button @click="handleCreateCareLog">保存</button>
        <button type="button" class="cancel-btn" @click="router.back()">キャンセル</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createCareLog, uploadCareLogPhoto } from '@/api/careLogs'

const route = useRoute()
const router = useRouter()

const newCareLog = ref({
  memo: '',
  caredAt: nowLocalDateTime()
})
const selectedTypes = ref([])
const cameraInput = ref(null)
const galleryInput = ref(null)
const selectedPhoto = ref(null)

const careTypeOptions = [
  { value: 'water', label: '水やり' },
  { value: 'fertilize', label: '肥料' },
  { value: 'harvest', label: '収穫' },
  { value: 'other', label: 'その他' }
]

// calendar・履歴一覧と同じ配色
const careTypeColor = (type) => ({
  water: '#4a90d9', fertilize: '#4a9d5f', harvest: '#d9a92a', other: '#9b9b9b'
}[type] || '#9b9b9b')

function toggleType(value) {
  const index = selectedTypes.value.indexOf(value)
  if (index === -1) {
    selectedTypes.value.push(value)
  } else {
    selectedTypes.value.splice(index, 1)
  }
}

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

// ローカル時刻のまま "YYYY-MM-DDTHH:mm" を作る（toISOStringはUTC変換され日付がずれるため使わない）
function nowLocalDateTime() {
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`
}

async function handleCreateCareLog() {
  const payload = { ...newCareLog.value, careType: selectedTypes.value.join(',') }
  const created = await createCareLog(route.params.id, payload)
  if (selectedPhoto.value) {
    await uploadCareLogPhoto(route.params.id, created.data.id, selectedPhoto.value)
  }
  router.back()
}
</script>

<style scoped>
.container {
  --color-primary: #4a9d5f;
  --color-primary-dark: #3d8350;
  --color-muted: #767676;
  --color-border: #e3e3e3;
  --color-bg-soft: #f7f8f7;
  --radius: 10px;

  max-width: 500px;
  margin: 0 auto;
  padding: 1rem;
  color: #2d3436;
}

.top-bar { display: flex; gap: 0.5rem; margin-bottom: 1.5rem; }
.back-btn {
  background: transparent;
  color: var(--color-muted);
  border: 1px solid var(--color-border);
  padding: 0.45rem 0.8rem;
  font-size: 0.85rem;
  border-radius: var(--radius);
  cursor: pointer;
}
.back-btn:hover { background: var(--color-bg-soft); color: #2d3436; }

h1 { font-size: 1.3rem; margin-bottom: 1.25rem; }

.add-care-form { display: flex; flex-direction: column; gap: 0.5rem; padding: 1.25rem; background: var(--color-bg-soft); border-radius: var(--radius); }

.care-type-select { display: flex; gap: 0.5rem; flex-wrap: wrap; }
.care-type-btn {
  background: #fff;
  color: #2d3436;
  border: 1px solid var(--color-border);
  padding: 0.2rem 0.7rem;
  font-size: 0.8rem;
  border-radius: 999px;
}
.care-type-btn:hover { background: #fff; color: #2d3436; }
.care-type-btn.active { color: #fff; }
.care-type-btn.active:hover { filter: brightness(0.9); }


input {
  padding: 0.45rem 0.8rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  font-size: 0.85rem;
  background: #fff;
  transition: border-color 0.15s, box-shadow 0.15s;
}
input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(74, 157, 95, 0.15);
}

.photo-select-row { display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; }
.hidden-file-input { display: none; }
.photo-select-row button {
  background: transparent;
  color: var(--color-muted);
  border: 1px solid var(--color-border);
  padding: 0.45rem 0.8rem;
  font-size: 0.85rem;
}
.photo-select-row button:hover { background: var(--color-bg-soft); color: #2d3436; }
.selected-photo-name { color: var(--color-muted); font-size: 0.85rem; }
.select-text-desktop { display: none; }

button {
  padding: 0.65rem 1.1rem;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius);
  font-size: 0.95rem;
  cursor: pointer;
  transition: background-color 0.15s, transform 0.05s;
}
button:hover { background: var(--color-primary-dark); }
button:active { transform: scale(0.97); }
button:disabled { background: var(--color-border); color: var(--color-muted); cursor: not-allowed; }

.add-care-actions { display: flex; justify-content: flex-end; gap: 0.5rem; }
.add-care-actions button { padding: 0.45rem 0.8rem; font-size: 0.85rem; }
.cancel-btn {
  background: transparent;
  color: var(--color-muted);
  border: 1px solid var(--color-border);
}
.cancel-btn:hover { background: var(--color-bg-soft); color: #2d3436; }

@media (min-width: 768px) {
  .container { max-width: 600px; padding: 2rem; }
  .camera-btn { display: none; }
  .select-text-mobile { display: none; }
  .select-text-desktop { display: inline; }
}
</style>
