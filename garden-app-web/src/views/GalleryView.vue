<template>
  <div class="container">
    <div class="top-bar">
      <button @click="router.back()" class="back-btn">← 戻る</button>
      <button @click="router.push('/plants')" class="back-btn">TOPへ</button>
    </div>
    <h1>📷 写真ギャラリー</h1>

    <p v-if="photos.length === 0">写真付きのケア記録がまだありません</p>

    <div class="gallery-grid">
      <div v-for="photo in photos" :key="photo.id" class="gallery-card">
        <img :src="photo.photoUrl" :alt="photo.plantName" class="gallery-photo" />
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
import { getPhotoGallery } from '@/api/gallery'

const router = useRouter()
const photos = ref([])

const careTypeLabel = (type) => ({
  water: '💧水やり', fertilize: '🌿肥料',
  harvest: '🌾収穫', other: 'その他'
}[type] || type)

// careType（カンマ区切りの複数種別）をまとめて表示する
const careTypeLabels = (careType) => careType ? careType.split(',').map(careTypeLabel).join('・') : ''

const formatDate = (dateStr) => dateStr ? dateStr.replace('T', ' ') : ''

onMounted(async () => {
  const res = await getPhotoGallery()
  photos.value = res.data
})
</script>

<style scoped>
.container { max-width: 960px; margin: 0 auto; padding: 2rem; }
.top-bar { display: flex; gap: 0.5rem; margin-bottom: 1.5rem; }
.back-btn { background: #999; color: white; border: none; padding: 0.5rem 1rem; border-radius: 6px; cursor: pointer; }
.gallery-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 1rem; margin-top: 1.5rem; }
.gallery-card { border: 1px solid #eee; border-radius: 8px; overflow: hidden; }
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
