<template>
  <div class="container">
    <div class="top-bar">
      <button @click="router.back()" class="back-btn">{{ t('backBtn') }}</button>
      <button @click="router.push('/plants')" class="back-btn">{{ t('topBtn') }}</button>
    </div>

    <p v-if="isLoading" class="loading-message">{{ t('loadingText') }}</p>

    <div v-if="plant" class="plant-card">
      <div v-if="editingPlant" class="plant-edit-form" @click.stop>
        <p class="edit-note">{{ t('thumbnailNote') }}</p>
        <input v-model="plantEditForm.name" type="text" :placeholder="t('plantNamePlaceholder')" />
        <select v-model="plantEditForm.type">
          <option value="vegetable">{{ t('typeVegetable') }}</option>
          <option value="fruit">{{ t('typeFruit') }}</option>
          <option value="herb">{{ t('typeHerb') }}</option>
          <option value="flower">{{ t('typeFlower') }}</option>
          <option value="tree">{{ t('typeTree') }}</option>
          <option value="other">{{ t('other') }}</option>
        </select>
        <input v-model="plantEditForm.memo" type="text" :placeholder="t('commentPlaceholder')" />
        <div class="edit-actions">
          <button type="button" class="delete-btn" @click="handleDeletePlant">{{ t('deleteBtn') }}</button>
          <div class="edit-actions-right">
            <button :disabled="!plantEditForm.name" @click="savePlantEdit">{{ t('saveBtn') }}</button>
            <button type="button" class="cancel-btn" @click="cancelPlantEdit">{{ t('cancelBtn') }}</button>
          </div>
        </div>
      </div>
      <div v-else class="plant-card-body" @click="startEditPlant">
        <img v-if="plant.latestPhotoUrl" :src="plant.latestPhotoUrl" alt="" class="plant-detail-thumbnail" />
        <div v-else class="plant-detail-thumbnail plant-thumbnail-placeholder">
          <PlantPlaceholderIcon :size="80" />
        </div>
        <div class="plant-info-col">
          <h1>{{ plant.name }}</h1>
          <span class="plant-type-badge">{{ typeLabel(plant.type) }}</span>
          <p v-if="plant.memo" class="plant-memo">{{ plant.memo }}</p>
        </div>
      </div>
    </div>

    <template v-if="!isLoading">
      <div class="care-heading-row">
        <h2 class="care-heading">{{ t('recordsTitle') }}<span v-if="selectedDate" class="selected-date-label">（{{ selectedDate }}）</span></h2>
        <button @click="router.push(`/plants/${route.params.id}/care/new`)" class="add-care-btn">{{ t('addRecordBtn') }}</button>
      </div>

      <!-- カレンダー -->
      <CareCalendar :care-logs="careLogs" :selected-date="selectedDate" @select-date="selectedDate = $event" />

      <div class="care-section">
        <!-- 編集時の写真選択用（v-forの外に置き、編集中のログにのみ使う） -->
        <input ref="editCameraInput" type="file" accept="image/jpeg,image/png,image/webp" capture="environment" @change="handleEditPhotoSelect" class="hidden-file-input" />
        <input ref="editGalleryInput" type="file" accept="image/jpeg,image/png,image/webp" @change="handleEditPhotoSelect" class="hidden-file-input" />

        <p v-if="filteredCareLogs.length === 0" class="empty-message">{{ t('noCareRecordsText') }}</p>
        <div v-for="log in filteredCareLogs" :key="log.id" class="care-card" @click="handleCardClick(log)">
          <div v-if="editingIds.includes(log.id)" class="edit-care-form" @click.stop>
            <div class="edit-photo-row">
              <div v-if="log.photoUrl" class="edit-photo-preview-wrap">
                <img :src="log.photoUrl" :alt="t('careLogPhotoAlt')" class="edit-photo-preview" />
                <button type="button" class="delete-photo-btn" :title="t('deletePhotoTitle')" @click="handleDeletePhoto(log)"></button>
              </div>
              <template v-else>
                <div class="edit-photo-preview edit-photo-placeholder"></div>
                <button type="button" class="camera-btn" @click="openEditCamera(log.id)">◎ {{ t('takePhotoBtn') }}</button>
                <button type="button" @click="openEditGallery(log.id)">▭ <span class="select-text-mobile">{{ t('selectPhotoBtn') }}</span><span class="select-text-desktop">{{ t('selectPhotoBtnFull') }}</span></button>
              </template>
            </div>
            <div class="care-type-select">
              <button
                v-for="option in careTypeOptions"
                :key="option.value"
                type="button"
                class="care-type-btn"
                :class="{ active: editForms[log.id].careTypes.includes(option.value) }"
                :style="editForms[log.id].careTypes.includes(option.value) ? { background: careTypeColor(option.value), borderColor: careTypeColor(option.value) } : {}"
                @click="toggleEditType(log.id, option.value)"
              >{{ t(option.labelKey) }}</button>
            </div>
            <input v-model="editForms[log.id].caredAt" type="datetime-local" />
            <input v-model="editForms[log.id].memo" type="text" :placeholder="t('commentPlaceholder')" />
            <div class="edit-actions">
              <button type="button" class="delete-btn" @click="handleDeleteCareLog(log.id)">{{ t('deleteBtn') }}</button>
              <div class="edit-actions-right">
                <button @click="saveEdit(log)">{{ t('saveBtn') }}</button>
                <button type="button" class="cancel-btn" @click="cancelEdit(log.id)">{{ t('cancelBtn') }}</button>
              </div>
            </div>
          </div>
          <template v-else>
            <img v-if="log.photoUrl" :src="log.photoUrl" :alt="t('careLogPhotoAlt')" class="care-photo" />
            <div class="care-info">
              <div class="care-info-top">
                <span class="care-date">{{ formatDateTime(log.caredAt) }}</span>
                <div class="care-type-row">
                  <span
                    v-for="type in parseCareTypes(log.careType)"
                    :key="type"
                    class="care-type-badge"
                    :style="{ background: careTypeColor(type) }"
                  >{{ careTypeLabel(type) }}</span>
                </div>
              </div>
              <p v-if="log.memo" class="care-memo">{{ log.memo }}</p>
            </div>
          </template>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPlants, updatePlant, deletePlant } from '@/api/plants'
import { getCareLogs, updateCareLog, deleteCareLog, uploadCareLogPhoto, deleteCareLogPhoto } from '@/api/careLogs'
import CareCalendar from '@/components/CareCalendar.vue'
import PlantPlaceholderIcon from '@/components/PlantPlaceholderIcon.vue'
import { useLanguage } from '@/composables/useLanguage'

const route = useRoute()
const router = useRouter()
const { t } = useLanguage()

const plant = ref(null)
const careLogs = ref([])
const isLoading = ref(true)
const selectedDate = ref(null)
const editingIds = ref([])
const editForms = ref({})
const editCameraInput = ref(null)
const editGalleryInput = ref(null)
const activePhotoTargetId = ref(null)
const editingPlant = ref(false)
const plantEditForm = ref({ name: '', type: '', memo: '' })

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
  if (!confirm(t('deletePlantConfirm'))) return
  await deletePlant(route.params.id)
  router.push('/plants')
}

function startEditPlant() {
  editingPlant.value = true
  plantEditForm.value = { name: plant.value.name, type: plant.value.type, memo: plant.value.memo || '' }
}

function cancelPlantEdit() {
  editingPlant.value = false
}

async function savePlantEdit() {
  if (!plantEditForm.value.name) return
  const payload = { ...plantEditForm.value, plantedAt: plant.value.plantedAt }
  await updatePlant(route.params.id, payload)
  plant.value = { ...plant.value, ...plantEditForm.value }
  editingPlant.value = false
}

const typeKeyMap = { vegetable: 'typeVegetable', fruit: 'typeFruit', herb: 'typeHerb', flower: 'typeFlower', tree: 'typeTree', other: 'other' }
const typeLabel = (type) => t(typeKeyMap[type] || 'other')

const careTypeKeyMap = { water: 'careWater', fertilize: 'careFertilize', harvest: 'careHarvest', other: 'other' }
const careTypeLabel = (type) => t(careTypeKeyMap[type] || 'other')

// calendarのドット表示と同じ配色
const careTypeColor = (type) => ({
  water: '#4a90d9', fertilize: '#4a9d5f', harvest: '#d9a92a', other: '#9b9b9b'
}[type] || '#9b9b9b')

// careType（カンマ区切りの複数種別）を配列に分解する
const parseCareTypes = (careType) => careType ? careType.split(',') : []

const careTypeOptions = [
  { value: 'water', labelKey: 'careWater' },
  { value: 'fertilize', labelKey: 'careFertilize' },
  { value: 'harvest', labelKey: 'careHarvest' },
  { value: 'other', labelKey: 'other' }
]

// plant.latestPhotoUrl はサーバー側で「写真付きの最新ケア記録」から算出されるため、
// ケア記録側の写真を変更・削除した際は植物情報も再取得しないと古い写真が表示され続ける
async function refreshPlant() {
  const res = await getPlants()
  plant.value = res.data.find(p => p.id === route.params.id)
}

onMounted(async () => {
  const plantId = route.params.id
  const plantsRes = await getPlants()
  plant.value = plantsRes.data.find(p => p.id === plantId)
  const logsRes = await getCareLogs(plantId)
  careLogs.value = logsRes.data
  // ギャラリーからの遷移時はその日付を選択、それ以外は最新日付を選択
  if (route.query.date) {
    selectedDate.value = route.query.date
  } else {
    selectLatestDate()
  }
  isLoading.value = false
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
  editingIds.value.push(log.id)
  editForms.value[log.id] = { careTypes: parseCareTypes(log.careType), caredAt: log.caredAt.slice(0, 16), memo: log.memo || '' }
}

// 編集中のカードをクリックしてもeditFormがリセットされないようにする
function handleCardClick(log) {
  if (editingIds.value.includes(log.id)) return
  startEdit(log)
}

function cancelEdit(id) {
  editingIds.value = editingIds.value.filter(i => i !== id)
  delete editForms.value[id]
}

function toggleEditType(id, value) {
  const careTypes = editForms.value[id].careTypes
  const index = careTypes.indexOf(value)
  if (index === -1) {
    careTypes.push(value)
  } else {
    careTypes.splice(index, 1)
  }
}

async function saveEdit(log) {
  const form = editForms.value[log.id]
  const payload = {
    careType: form.careTypes.join(','),
    caredAt: form.caredAt,
    memo: form.memo
  }
  await updateCareLog(route.params.id, log.id, payload)
  const res = await getCareLogs(route.params.id)
  careLogs.value = res.data
  await refreshPlant()
  cancelEdit(log.id)
}

function openEditCamera(id) {
  activePhotoTargetId.value = id
  editCameraInput.value.click()
}

function openEditGallery(id) {
  activePhotoTargetId.value = id
  editGalleryInput.value.click()
}

async function handleEditPhotoSelect(event) {
  const file = event.target.files[0]
  if (!file || !activePhotoTargetId.value) return
  const compressed = await compressImage(file)
  await uploadCareLogPhoto(route.params.id, activePhotoTargetId.value, compressed)
  const res = await getCareLogs(route.params.id)
  careLogs.value = res.data
  await refreshPlant()
  event.target.value = ''
}

async function handleDeletePhoto(log) {
  await deleteCareLogPhoto(route.params.id, log.id)
  const res = await getCareLogs(route.params.id)
  careLogs.value = res.data
  await refreshPlant()
}

async function handleDeleteCareLog(id) {
  await deleteCareLog(route.params.id, id)
  careLogs.value = careLogs.value.filter(l => l.id !== id)
  await refreshPlant()
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

.top-bar { display: flex; align-items: center; gap: 0.5rem; margin-bottom: 1.5rem; }

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

.add-care-btn { padding: 0.45rem 0.8rem; font-size: 0.85rem; }

.plant-card { position: relative; padding: 1rem; border: 1px solid var(--color-border); border-radius: var(--radius); background: #fff; box-shadow: var(--shadow-sm); }
.plant-card-body { display: flex; align-items: center; gap: 1rem; cursor: pointer; }

.plant-edit-form { display: flex; flex-direction: column; gap: 0.5rem; }
.edit-note { margin: 0; color: var(--color-muted); font-size: 0.8rem; }
.plant-edit-form select {
  padding: 0.45rem 0.8rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  font-size: 0.85rem;
  background: #fff;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.plant-edit-form select:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(74, 157, 95, 0.15);
}
.plant-detail-thumbnail { width: 80px; height: 80px; object-fit: cover; border-radius: var(--radius); flex-shrink: 0; }
.plant-thumbnail-placeholder { background: var(--color-bg-soft); display: flex; align-items: center; justify-content: center; }
.plant-info-col { display: flex; flex-direction: column; gap: 0.4rem; min-width: 0; }
.plant-info-col h1 { font-size: 0.95rem; margin: 0; }
.plant-type-badge { align-self: flex-start; background: var(--color-bg-soft); color: #2d3436; border: 1px solid var(--color-border); padding: 0.2rem 0.7rem; font-size: 0.8rem; border-radius: 999px; }
.plant-memo { margin: 0; color: var(--color-muted); font-size: 0.9rem; }

.care-heading-row { display: flex; align-items: center; justify-content: space-between; gap: 0.75rem; margin-top: 1.5rem; }
.care-heading { font-size: 1.05rem; margin: 0; }
.selected-date-label { font-size: 0.9rem; color: var(--color-muted); font-weight: normal; }

.care-section { margin-top: 1rem; }

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
  padding: 0.2rem 0.7rem;
  font-size: 0.8rem;
  border-radius: 999px;
}
.care-type-btn:hover { background: #fff; color: #2d3436; }
.care-type-btn.active { color: #fff; }
.care-type-btn.active:hover { filter: brightness(0.9); }

.hidden-file-input { display: none; }

.empty-message { color: var(--color-muted); min-height: 120px; display: flex; align-items: center; }
.loading-message { color: var(--color-muted); }

.care-card {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 0.85rem 1rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: #fff;
  box-shadow: var(--shadow-sm);
  margin-bottom: 0.6rem;
  cursor: pointer;
  transition: box-shadow 0.15s;
}
.care-card:hover { box-shadow: var(--shadow-md); }
.care-photo { width: 100%; max-height: 320px; object-fit: cover; border-radius: var(--radius); }
.care-info { display: flex; flex-direction: column; gap: 0.3rem; min-width: 0; }
.care-info-top { display: flex; flex-direction: column; gap: 0.3rem; }
.care-type-row { display: flex; align-items: center; gap: 0.6rem; flex-wrap: wrap; }
.care-type-badge { color: #fff; padding: 0.2rem 0.7rem; font-size: 0.8rem; border-radius: 999px; }
.care-date { color: var(--color-muted); font-size: 0.85rem; }
.care-memo { margin: 0; color: #555; font-size: 0.9rem; }

.edit-care-form { display: flex; flex-direction: column; gap: 0.5rem; width: 100%; cursor: default; }
.edit-actions { display: flex; align-items: center; justify-content: space-between; gap: 0.5rem; }
.edit-actions-right { display: flex; gap: 0.5rem; }
.edit-actions button { padding: 0.45rem 0.8rem; font-size: 0.85rem; }
.cancel-btn {
  background: transparent;
  color: var(--color-muted);
  border: 1px solid var(--color-border);
}
.cancel-btn:hover { background: var(--color-bg-soft); color: #2d3436; }
.edit-actions .delete-btn {
  background: transparent;
  color: var(--color-danger);
  border: 1px solid var(--color-border);
}
.edit-actions .delete-btn:hover { background: var(--color-bg-soft); }

.edit-photo-row { display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap; }
.edit-photo-row button {
  background: transparent;
  color: var(--color-muted);
  border: 1px solid var(--color-border);
  padding: 0.45rem 0.8rem;
  font-size: 0.85rem;
}
.edit-photo-row button:hover { background: var(--color-bg-soft); color: #2d3436; }
.edit-photo-preview-wrap { position: relative; display: inline-block; }
.edit-photo-preview { width: 56px; height: 56px; object-fit: cover; border-radius: var(--radius); flex-shrink: 0; display: block; }
.edit-photo-placeholder { background: var(--color-bg-soft); }
.edit-photo-row .delete-photo-btn {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 22px;
  height: 22px;
  box-sizing: border-box;
  padding: 0;
  background: #fff;
  border: 2px solid var(--color-danger);
  border-radius: 50%;
}
.delete-photo-btn::before,
.delete-photo-btn::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 10px;
  height: 2px;
  background: var(--color-danger);
}
.delete-photo-btn::before { transform: translate(-50%, -50%) rotate(45deg); }
.delete-photo-btn::after { transform: translate(-50%, -50%) rotate(-45deg); }
.edit-photo-row .delete-photo-btn:hover { background: var(--color-bg-soft); }
.select-text-desktop { display: none; }

@media (min-width: 768px) {
  .container { max-width: 900px; padding: 2rem; }
  .camera-btn { display: none; }
  .select-text-mobile { display: none; }
  .select-text-desktop { display: inline; }
}

@media (min-width: 1024px) {
  .container { max-width: 1000px; }
  .plant-info-col h1 { font-size: 1.05rem; }
}
</style>
