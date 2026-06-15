<template>
  <div class="heatmap-container">
    <h3>ケア記録ヒートマップ</h3>
    <div class="heatmap">
      <div class="months">
        <span v-for="month in months" :key="month">{{ month }}</span>
      </div>
      <div class="grid">
        <div
          v-for="day in days"
          :key="day.date"
          class="cell"
          :class="getLevel(day.count)"
          :title="`${day.date}: ${day.count}回`"
        />
      </div>
    </div>
    <div class="legend">
      <span>少ない</span>
      <div class="cell level-0" />
      <div class="cell level-1" />
      <div class="cell level-2" />
      <div class="cell level-3" />
      <div class="cell level-4" />
      <span>多い</span>
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

const months = ['1月', '2月', '3月', '4月', '5月', '6月',
                 '7月', '8月', '9月', '10月', '11月', '12月']

const days = computed(() => {
  const result = []
  const today = new Date()
  const countMap = {}

  // caredAt は "2026-06-11T09:30:00" 形式なので日付部分だけ取り出す
  props.careLogs.forEach(log => {
    const date = log.caredAt ? log.caredAt.split('T')[0] : null
    if (date) countMap[date] = (countMap[date] || 0) + 1
  })

  for (let i = 364; i >= 0; i--) {
    const date = new Date(today)
    date.setDate(today.getDate() - i)
    const dateStr = date.toISOString().split('T')[0]
    result.push({
      date: dateStr,
      count: countMap[dateStr] || 0
    })
  }
  return result
})

function getLevel(count) {
  if (count === 0) return 'level-0'
  if (count === 1) return 'level-1'
  if (count === 2) return 'level-2'
  if (count === 3) return 'level-3'
  return 'level-4'
}
</script>

<style scoped>
.heatmap-container { margin: 2rem 0; }
.heatmap { overflow-x: auto; }
.months { display: flex; justify-content: space-between; margin-bottom: 4px; font-size: 11px; color: #999; }
.grid { display: grid; grid-template-columns: repeat(52, 14px); grid-template-rows: repeat(7, 14px); gap: 2px; }
.cell { width: 14px; height: 14px; border-radius: 2px; }
.level-0 { background: #ebedf0; }
.level-1 { background: #c6e48b; }
.level-2 { background: #7bc96f; }
.level-3 { background: #239a3b; }
.level-4 { background: #196127; }
.legend { display: flex; align-items: center; gap: 4px; margin-top: 8px; font-size: 12px; color: #999; }
</style>
