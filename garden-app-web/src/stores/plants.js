import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getPlants } from '@/api/plants'

const CACHE_TTL = 60_000

export const usePlantsStore = defineStore('plants', () => {
  const plants = ref([])
  const lastFetchedAt = ref(null)

  async function fetchIfStale() {
    const isStale = !lastFetchedAt.value || Date.now() - lastFetchedAt.value > CACHE_TTL
    if (isStale) {
      const res = await getPlants()
      plants.value = [...res.data].sort((a, b) => b.createdAt.localeCompare(a.createdAt))
      lastFetchedAt.value = Date.now()
    }
  }

  function invalidate() {
    lastFetchedAt.value = null
  }

  return { plants, fetchIfStale, invalidate }
})
