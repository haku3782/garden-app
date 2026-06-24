<template>
  <div class="container">
    <button @click="router.back()" class="back-btn">← 戻る</button>

    <div v-if="plant">
      <h1>{{ plant.name }}</h1>
      <p>種類：{{ typeLabel(plant.type) }}</p>
      <p>植えた日：{{ plant.plantedAt }}</p>
      <p v-if="plant.memo">メモ：{{ plant.memo }}</p>
    </div>

    <!-- ストリーク -->
    <StreakCounter :care-logs="careLogs" />

    <!-- ヒートマップ -->
    <CareHeatmap :care-logs="careLogs" />

    <div class="care-section">
      <h2>ケア記録</h2>

      <div class="add-care-form">
        <select v-model="newCareLog.careType">
          <option value="">種類を選択</option>
          <option value="water">💧水やり</option>
          <option value="fertilize">🌿肥料</option>
          <option value="harvest">🌾収穫</option>
          <option value="other">その他</option>
        </select>
        <input v-model="newCareLog.caredAt" type="datetime-local" />
        <input v-model="newCareLog.memo" type="text" placeholder="メモ" />
        <input ref="photoInput" type="file" accept="image/jpeg,image/png,image/webp" capture="environment" @change="handlePhotoSelect" />
        <button @click="handleCreateCareLog">記録追加</button>
      </div>

      <div v-if="careLogs.length === 0">
        <p>ケア記録がありません</p>
      </div>
      <div v-for="log in careLogs" :key="log.id" class="care-card">
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
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPlants } from '@/api/plants'
import { getCareLogs, createCareLog, deleteCareLog, uploadCareLogPhoto } from '@/api/careLogs'
import CareHeatmap from '@/components/CareHeatmap.vue'
import StreakCounter from '@/components/StreakCounter.vue'

const route = useRoute()
const router = useRouter()

const plant = ref(null)
const careLogs = ref([])
const newCareLog = ref({
  careType: '',
  caredAt: '',
  memo: ''
})
const photoInput = ref(null)
const selectedPhoto = ref(null)

const typeLabel = (type) => ({
  vegetable: '野菜', fruit: '果物', herb: 'ハーブ',
  flower: '花', tree: '樹木', other: 'その他'
}[type] || type)

const careTypeLabel = (type) => ({
  water: '💧水やり', fertilize: '🌿肥料',
  harvest: '🌾収穫', other: 'その他'
}[type] || type)

onMounted(async () => {
  const plantId = route.params.id
  const plantsRes = await getPlants()
  plant.value = plantsRes.data.find(p => p.id === plantId)
  const logsRes = await getCareLogs(plantId)
  careLogs.value = logsRes.data
})

function handlePhotoSelect(event) {
  selectedPhoto.value = event.target.files[0] || null
}

async function handleCreateCareLog() {
  if (!newCareLog.value.careType) return
  if (!newCareLog.value.caredAt) newCareLog.value.caredAt = new Date().toISOString().slice(0, 16)
  const created = await createCareLog(route.params.id, newCareLog.value)
  if (selectedPhoto.value) {
    await uploadCareLogPhoto(route.params.id, created.data.id, selectedPhoto.value)
  }
  const res = await getCareLogs(route.params.id)
  careLogs.value = res.data
  newCareLog.value = { careType: '', caredAt: '', memo: '' }
  selectedPhoto.value = null
  if (photoInput.value) photoInput.value.value = ''
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
.add-care-form { display: flex; flex-direction: column; gap: 0.75rem; margin-bottom: 1.5rem; padding: 1.5rem; background: #f9f9f9; border-radius: 8px; }
input, select { padding: 0.75rem; border: 1px solid #ccc; border-radius: 6px; font-size: 1rem; }
button { padding: 0.75rem 1.5rem; background: #4a9d5f; color: white; border: none; border-radius: 6px; cursor: pointer; }
.care-card { display: flex; gap: 1rem; align-items: center; padding: 0.75rem 1rem; border: 1px solid #eee; border-radius: 8px; margin-bottom: 0.5rem; }
.care-photo { width: 48px; height: 48px; object-fit: cover; border-radius: 6px; flex-shrink: 0; }
.care-type { font-weight: 500; min-width: 80px; }
.care-date { color: #999; }
.care-memo { color: #666; flex: 1; }
.delete-btn { background: #e74c3c; padding: 0.4rem 0.8rem; margin-left: auto; }
</style>
