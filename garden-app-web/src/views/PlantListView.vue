<template>
  <div class="container">
    <header>
      <div class="title-row">
        <img src="/favicon.png" alt="" class="app-logo" />
        <h1>{{ t('appTitle') }}</h1>
      </div>
      <div class="header-actions">
        <LanguageSelector />
        <button @click="router.push('/gallery')" class="ghost-btn icon-btn" :title="t('galleryTitle')">▭</button>
        <button @click="handleLogout" class="ghost-btn icon-btn" :title="t('logoutTitle')">⏻</button>
      </div>
    </header>

    <div class="plant-list">
      <div class="list-heading-row">
        <h2>{{ t('plantListTitle') }}</h2>
        <button @click="router.push('/plants/new')" class="add-plant-btn">{{ t('addPlantBtn') }}</button>
      </div>

      <div class="type-tabs">
        <button
          v-for="option in typeFilterOptions"
          :key="option.value"
          type="button"
          class="type-tab"
          :class="{ active: selectedType === option.value }"
          @click="selectedType = option.value"
        >{{ t(option.labelKey) }}</button>
      </div>

      <p v-if="isLoading" class="loading-message">{{ t('loadingText') }}</p>
      <p v-else-if="filteredPlants.length === 0" class="empty-message">{{ t('noPlantsText') }}</p>
      <div v-else class="plant-grid">
        <div v-for="plant in filteredPlants" :key="plant.id" class="plant-card">
          <div class="plant-info" @click="goToDetail(plant.id)">
            <img v-if="plant.latestPhotoUrl" :src="plant.latestPhotoUrl" alt="" class="plant-thumbnail" loading="lazy" />
            <div v-else class="plant-thumbnail plant-thumbnail-placeholder">
              <PlantPlaceholderIcon :size="64" />
            </div>
            <span class="plant-name">{{ plant.name }}</span>
          </div>
        </div>
      </div>
    </div>

    <ConfirmModal
      v-if="showLogoutConfirm"
      :message="t('logoutConfirm')"
      :confirm-label="t('logoutTitle')"
      :cancel-label="t('cancelBtn')"
      @confirm="confirmLogout"
      @cancel="showLogoutConfirm = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useLanguage } from '@/composables/useLanguage'
import { getPlants } from '@/api/plants'
import PlantPlaceholderIcon from '@/components/PlantPlaceholderIcon.vue'
import LanguageSelector from '@/components/LanguageSelector.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'

const router = useRouter()
const authStore = useAuthStore()
const { t } = useLanguage()

const plants = ref([])
const isLoading = ref(true)
const showLogoutConfirm = ref(false)

const selectedType = ref('all')

const typeFilterOptions = [
  { value: 'all', labelKey: 'typeAll' },
  { value: 'vegetable', labelKey: 'typeVegetable' },
  { value: 'fruit', labelKey: 'typeFruit' },
  { value: 'herb', labelKey: 'typeHerb' },
  { value: 'flower', labelKey: 'typeFlower' },
  { value: 'tree', labelKey: 'typeTree' },
  { value: 'houseplant', labelKey: 'typeHouseplant' },
  { value: 'succulent', labelKey: 'typeSucculent' },
  { value: 'airplant', labelKey: 'typeAirPlant' },
  { value: 'other', labelKey: 'other' }
]

const filteredPlants = computed(() => {
  if (selectedType.value === 'all') return plants.value
  return plants.value.filter(p => p.type === selectedType.value)
})

onMounted(async () => {
  const res = await getPlants()
  // 登録日時（createdAt）の新しい順に並べる
  plants.value = [...res.data].sort((a, b) => b.createdAt.localeCompare(a.createdAt))
  isLoading.value = false
})

function goToDetail(id) {
  router.push(`/plants/${id}`)
}

function handleLogout() {
  showLogoutConfirm.value = true
}

function confirmLogout() {
  showLogoutConfirm.value = false
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.container {
  --color-primary: #4a9d5f;
  --color-primary-dark: #3d8350;
  --color-accent: #4a7a9d;
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

header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}
.title-row { display: flex; align-items: center; gap: 0.5rem; }
.app-logo { width: 1.5rem; height: 1.5rem; flex-shrink: 0; }
header h1 { font-size: 1.3rem; margin: 0; }
.header-actions { display: flex; gap: 0.5rem; flex-shrink: 0; width: 100%; justify-content: flex-end; }

h2 { font-size: 1.05rem; margin: 0; }

.list-heading-row { display: flex; align-items: center; justify-content: space-between; gap: 0.75rem; margin-bottom: 0.75rem; }

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

.ghost-btn {
  background: transparent;
  color: var(--color-muted);
  border: 1px solid var(--color-border);
  padding: 0.45rem 0.8rem;
  font-size: 0.85rem;
}
.ghost-btn:hover { background: var(--color-bg-soft); color: #2d3436; }

.icon-btn { padding: 0.45rem; width: 2.1rem; height: 2.1rem; display: flex; align-items: center; justify-content: center; font-size: 1.1rem; }

.type-tabs { display: flex; flex-wrap: wrap; gap: 0.5rem; margin-bottom: 1rem; }
.type-tab {
  background: #fff;
  color: #2d3436;
  border: 1px solid var(--color-border);
  padding: 0.45rem 0.9rem;
  font-size: 0.85rem;
  border-radius: 999px;
  transition: background-color 0.15s, border-color 0.15s;
}
.type-tab:hover { background: #fff; color: #2d3436; }
.type-tab.active { background: var(--color-primary); color: #fff; border-color: var(--color-primary); }
.type-tab.active:hover { background: var(--color-primary-dark); color: #fff; }

.empty-message { color: var(--color-muted); min-height: 160px; display: flex; align-items: center; }
.loading-message { color: var(--color-muted); min-height: 160px; display: flex; align-items: center; }

.plant-list { margin-bottom: 2rem; }

.plant-grid { display: flex; flex-direction: column; gap: 0.6rem; }

.plant-card {
  display: flex;
  align-items: center;
  padding: 0.85rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: #fff;
  box-shadow: var(--shadow-sm);
  transition: box-shadow 0.15s;
}
.plant-card:hover { box-shadow: var(--shadow-md); }

.plant-info { display: flex; gap: 0.85rem; align-items: center; cursor: pointer; flex: 1; min-width: 0; }
.plant-thumbnail { width: 64px; height: 64px; object-fit: cover; border-radius: var(--radius); flex-shrink: 0; }
.plant-thumbnail-placeholder { background: var(--color-bg-soft); display: flex; align-items: center; justify-content: center; }
.plant-name { font-weight: 500; font-size: 0.9rem; }

.add-plant-btn { padding: 0.45rem 0.8rem; font-size: 0.85rem; flex-shrink: 0; }

@media (min-width: 600px) {
  header { flex-direction: row; align-items: center; justify-content: space-between; }
  header h1 { font-size: 1.5rem; }
  .header-actions { width: auto; }
}

@media (min-width: 768px) {
  .container { max-width: 900px; padding: 2rem; }
  .plant-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 1rem; }
}

@media (min-width: 1024px) {
  .container { max-width: 1000px; }
  header h1 { font-size: 1.7rem; }
}
</style>
