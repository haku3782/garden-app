<template>
  <div class="streak-container">
    <div class="streak-card">
      <span class="streak-icon">🔥</span>
      <div class="streak-info">
        <span class="streak-num">{{ currentStreak }}</span>
        <span class="streak-label">日連続ケア</span>
      </div>
    </div>
    <div class="streak-card">
      <span class="streak-icon">⭐</span>
      <div class="streak-info">
        <span class="streak-num">{{ maxStreak }}</span>
        <span class="streak-label">最長連続記録</span>
      </div>
    </div>
    <div class="streak-card">
      <span class="streak-icon">📝</span>
      <div class="streak-info">
        <span class="streak-num">{{ totalCount }}</span>
        <span class="streak-label">総ケア回数</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  careLogs: {
    type: Array,
    default: () => []
  }
})

const totalCount = computed(() => props.careLogs.length)

// caredAt は "2026-06-11T09:30:00" 形式なので日付部分だけ取り出す
function toDateStr(caredAt) {
  return caredAt ? caredAt.split('T')[0] : null
}

const currentStreak = computed(() => {
  if (props.careLogs.length === 0) return 0

  const dates = [...new Set(props.careLogs.map(l => toDateStr(l.caredAt)).filter(Boolean))]
    .sort()
    .reverse()
  const today = new Date().toISOString().split('T')[0]
  const yesterday = new Date(Date.now() - 86400000).toISOString().split('T')[0]

  if (dates[0] !== today && dates[0] !== yesterday) return 0

  let streak = 1
  for (let i = 1; i < dates.length; i++) {
    const prev = new Date(dates[i - 1])
    const curr = new Date(dates[i])
    const diff = (prev - curr) / 86400000
    if (diff === 1) streak++
    else break
  }
  return streak
})

const maxStreak = computed(() => {
  if (props.careLogs.length === 0) return 0

  const dates = [...new Set(props.careLogs.map(l => toDateStr(l.caredAt)).filter(Boolean))].sort()
  let max = 1
  let current = 1

  for (let i = 1; i < dates.length; i++) {
    const prev = new Date(dates[i - 1])
    const curr = new Date(dates[i])
    const diff = (curr - prev) / 86400000
    if (diff === 1) {
      current++
      max = Math.max(max, current)
    } else {
      current = 1
    }
  }
  return max
})
</script>

<style scoped>
.streak-container { display: flex; gap: 1rem; margin: 1.5rem 0; flex-wrap: wrap; }
.streak-card { display: flex; align-items: center; gap: 0.75rem; padding: 1rem 1.5rem; background: #f9f9f9; border-radius: 12px; border: 1px solid #eee; }
.streak-icon { font-size: 2rem; }
.streak-info { display: flex; flex-direction: column; }
.streak-num { font-size: 2rem; font-weight: 700; color: #2d6a3f; line-height: 1; }
.streak-label { font-size: 12px; color: #999; }
</style>
