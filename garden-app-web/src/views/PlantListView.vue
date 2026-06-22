<template>
  <div class="container">
    <header>
      <h1>🌱 家庭菜園管理</h1>
      <div class="header-actions">
        <button @click="router.push('/gallery')" class="gallery-btn">📷 ギャラリー</button>
        <button @click="handleLogout" class="logout-btn">ログアウト</button>
      </div>
    </header>

    <div class="add-form">
      <h2>植物を追加</h2>
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
      <input v-model="newPlant.plantedAt" type="datetime-local" />
      <input v-model="newPlant.memo" type="text" placeholder="メモ" />
      <button @click="handleCreate">追加</button>
    </div>

    <div class="plant-list">
      <h2>登録済みの植物</h2>
      <p v-if="plants.length === 0">植物が登録されていません</p>
      <div v-for="plant in plants" :key="plant.id" class="plant-card">
        <div class="plant-info" @click="goToDetail(plant.id)">
          <span class="plant-name">{{ plant.name }}</span>
          <span class="plant-type">{{ typeLabel(plant.type) }}</span>
          <span class="plant-date">{{ plant.plantedAt }}</span>
        </div>
        <button @click="handleDelete(plant.id)" class="delete-btn">削除</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getPlants, createPlant, deletePlant } from '@/api/plants'

const router = useRouter()
const authStore = useAuthStore()

const plants = ref([])
const newPlant = ref({
  name: '',
  type: '',
  plantedAt: '',
  memo: ''
})

const typeLabel = (type) => ({
  vegetable: '野菜',
  fruit: '果物',
  herb: 'ハーブ',
  flower: '花',
  tree: '樹木',
  other: 'その他'
}[type] || type)

onMounted(async () => {
  const res = await getPlants()
  plants.value = res.data
})

async function handleCreate() {
  if (!newPlant.value.name || !newPlant.value.type) return
  const data = { ...newPlant.value }
  if (!data.plantedAt) data.plantedAt = null
  await createPlant(data)
  const res = await getPlants()
  plants.value = res.data
  newPlant.value = { name: '', type: '', plantedAt: '', memo: '' }
}

async function handleDelete(id) {
  await deletePlant(id)
  plants.value = plants.value.filter(p => p.id !== id)
}

function goToDetail(id) {
  router.push(`/plants/${id}`)
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.container { max-width: 800px; margin: 0 auto; padding: 2rem; }
header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
.header-actions { display: flex; gap: 0.5rem; }
.gallery-btn { background: #4a7a9d; }
.add-form { display: flex; flex-direction: column; gap: 0.75rem; margin-bottom: 2rem; padding: 1.5rem; background: #f9f9f9; border-radius: 8px; }
input, select { padding: 0.75rem; border: 1px solid #ccc; border-radius: 6px; font-size: 1rem; }
button { padding: 0.75rem 1.5rem; background: #4a9d5f; color: white; border: none; border-radius: 6px; cursor: pointer; }
.logout-btn { background: #999; }
.plant-card { display: flex; justify-content: space-between; align-items: center; padding: 1rem; border: 1px solid #eee; border-radius: 8px; margin-bottom: 0.5rem; }
.plant-info { display: flex; gap: 1rem; cursor: pointer; flex: 1; }
.plant-name { font-weight: 500; }
.plant-type { color: #4a9d5f; }
.plant-date { color: #999; font-size: 0.9rem; }
.delete-btn { background: #e74c3c; padding: 0.5rem 1rem; }
</style>
