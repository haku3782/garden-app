<template>
  <div class="container">
    <div class="top-bar">
      <button @click="router.back()" class="back-btn">{{ t('backBtn') }}</button>
      <button @click="router.push('/plants')" class="back-btn">{{ t('topBtn') }}</button>
    </div>
    <h1>{{ t('galleryViewTitle') }}</h1>

    <p v-if="isLoading" class="loading-message">{{ t('loadingText') }}</p>
    <p v-else-if="photos.length === 0">{{ t('noPhotosMessage') }}</p>

    <div class="gallery-grid">
      <div v-for="photo in photos" :key="photo.id" class="gallery-card" @click="goToPlant(photo)">
        <img :src="photo.photoUrl" :alt="photo.plantName" class="gallery-photo" loading="lazy" />
        <div class="gallery-info">
          <span class="gallery-plant-name">{{ photo.plantName }}</span>
          <span class="gallery-care-type">{{ careTypeLabels(photo.careType) }}</span>
          <span class="gallery-date">{{ formatDate(photo.caredAt) }}</span>
          <span v-if="photo.memo" class="gallery-memo">{{ photo.memo }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useLanguage } from '@/composables/useLanguage'
import { getPhotoGallery } from '@/api/gallery'

const router = useRouter()
const { t } = useLanguage()
const photos = ref([])
const isLoading = ref(true)

const careTypeEmoji = { water: '💧', fertilize: '🌿', harvest: '🌾', other: '' }
const careTypeKey = { water: 'careWater', fertilize: 'careFertilize', harvest: 'careHarvest', other: 'other' }
const careTypeLabel = (type) => `${careTypeEmoji[type] || ''}${t(careTypeKey[type] || 'other')}`

// careType（カンマ区切りの複数種別）をまとめて表示する
const careTypeLabels = (careType) => careType ? careType.split(',').map(careTypeLabel).join('・') : ''

const formatDate = (dateStr) => dateStr ? dateStr.replace('T', ' ') : ''

function goToPlant(photo) {
  const date = photo.caredAt ? photo.caredAt.split('T')[0] : null
  router.push({ path: `/plants/${photo.plantId}`, query: date ? { date } : {} })
}

onMounted(async () => {
  const res = await getPhotoGallery()
  photos.value = res.data
  isLoading.value = false
})
</script>

<style scoped>
.container {
  --color-border: #e3e3e3;
  --color-bg-soft: #f7f8f7;
  --radius: 10px;

  max-width: 960px;
  margin: 0 auto;
  padding: 2rem;
}
.top-bar { display: flex; gap: 0.5rem; margin-bottom: 1.5rem; }
.back-btn {
  background: transparent;
  color: #767676;
  border: 1px solid var(--color-border);
  padding: 0.45rem 0.8rem;
  font-size: 0.85rem;
  border-radius: var(--radius);
  cursor: pointer;
}
.back-btn:hover { background: var(--color-bg-soft); color: #2d3436; }
.loading-message { color: #767676; }
.gallery-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 1rem; margin-top: 1.5rem; }
.gallery-card { border: 1px solid #eee; border-radius: 8px; overflow: hidden; cursor: pointer; }
.gallery-photo { width: 100%; height: 160px; object-fit: cover; display: block; }
.gallery-info { display: flex; flex-direction: column; gap: 0.25rem; padding: 0.75rem; }
.gallery-plant-name { font-weight: 500; }
.gallery-care-type { color: #4a9d5f; font-size: 0.9rem; }
.gallery-date { color: #999; font-size: 0.85rem; }
.gallery-memo { color: #666; font-size: 0.9rem; }

@media (max-width: 600px) {
  .container { padding: 1rem; }
  .gallery-grid { grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 0.75rem; }
  .gallery-photo { height: 120px; }
}
</style>
