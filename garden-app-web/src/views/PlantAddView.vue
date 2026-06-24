<template>
  <div class="container">
    <button @click="router.back()" class="back-btn">← 戻る</button>
    <h1>植物を追加</h1>

    <div class="add-form">
      <input v-model="newPlant.name" type="text" placeholder="植物名" />
      <select v-model="newPlant.type">
        <option value="">種類を選択</option>
        <option value="vegetable">野菜</option>
        <option value="fruit">果物</option>
        <option value="herb">ハーブ</option>
        <option value="flower">花</option>
        <option value="tree">樹木</option>
        <option value="other">その他</option>
      </select>
      <input v-model="newPlant.memo" type="text" placeholder="メモ" />
      <div class="photo-select-row">
        <input ref="cameraInput" type="file" accept="image/jpeg,image/png,image/webp" capture="environment" @change="handlePhotoSelect" class="hidden-file-input" />
        <input ref="galleryInput" type="file" accept="image/jpeg,image/png,image/webp" @change="handlePhotoSelect" class="hidden-file-input" />
        <button type="button" class="photo-btn" @click="cameraInput.click()">📷 撮影</button>
        <button type="button" class="photo-btn" @click="galleryInput.click()">🏞️ アルバムから選択</button>
        <span v-if="selectedPhoto" class="selected-photo-name">{{ selectedPhoto.name }}</span>
      </div>
      <button @click="handleCreate">追加</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { createPlant } from '@/api/plants'
import { createCareLog, uploadCareLogPhoto } from '@/api/careLogs'

const router = useRouter()

const newPlant = ref({
  name: '',
  type: '',
  memo: ''
})

const cameraInput = ref(null)
const galleryInput = ref(null)
const selectedPhoto = ref(null)

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

async function handleCreate() {
  if (!newPlant.value.name || !newPlant.value.type) return
  // 植えた日は登録時点の日時を自動で使う
  const data = { ...newPlant.value, plantedAt: nowLocalDateTime() }
  const created = await createPlant(data)
  if (selectedPhoto.value) {
    // 植物自体には写真フィールドが無いため、ケア記録（その他）を1件作成して写真を添付する
    const careLog = await createCareLog(created.data.id, { careType: 'other', caredAt: nowLocalDateTime() })
    await uploadCareLogPhoto(created.data.id, careLog.data.id, selectedPhoto.value)
  }
  router.push('/plants')
}
</script>

<style scoped>
.container {
  --color-primary: #4a9d5f;
  --color-primary-dark: #3d8350;
  --color-border: #e3e3e3;
  --color-bg-soft: #f7f8f7;
  --radius: 10px;

  max-width: 500px;
  margin: 0 auto;
  padding: 1rem;
  color: #2d3436;
}

.back-btn {
  background: transparent;
  color: #767676;
  border: 1px solid var(--color-border);
  padding: 0.45rem 0.8rem;
  font-size: 0.85rem;
  border-radius: var(--radius);
  cursor: pointer;
  margin-bottom: 1.5rem;
}
.back-btn:hover { background: var(--color-bg-soft); color: #2d3436; }

h1 { font-size: 1.3rem; margin-bottom: 1.25rem; }

.add-form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 1.25rem;
  background: var(--color-bg-soft);
  border-radius: var(--radius);
}

input, select {
  padding: 0.7rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  font-size: 1rem;
  background: #fff;
  transition: border-color 0.15s, box-shadow 0.15s;
}
input:focus, select:focus {
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
  font-size: 0.95rem;
  cursor: pointer;
  transition: background-color 0.15s, transform 0.05s;
}
button:hover { background: var(--color-primary-dark); }
button:active { transform: scale(0.97); }

.photo-select-row { display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; }
.hidden-file-input { display: none; }
.photo-btn { background: #4a7a9d; padding: 0.6rem 1rem; font-size: 0.9rem; }
.photo-btn:hover { background: #3d6584; }
.selected-photo-name { color: #767676; font-size: 0.85rem; }

@media (min-width: 768px) {
  .container { max-width: 600px; padding: 2rem; }
}
</style>
