<template>
  <div class="calendar-container">
    <div class="calendar-header">
      <button class="nav-btn" @click="prevMonth">◀</button>
      <h3>{{ viewYear }}年{{ viewMonth + 1 }}月</h3>
      <button class="nav-btn" @click="nextMonth">▶</button>
      <button class="today-btn" @click="goToToday">今日</button>
    </div>

    <div class="weekdays">
      <span v-for="w in weekdays" :key="w">{{ w }}</span>
    </div>

    <div class="grid">
      <div
        v-for="(cell, index) in cells"
        :key="index"
        class="cell"
        :class="{ 'is-today': cell.isToday, 'is-empty': !cell.day, 'is-clickable': !!cell.day && !cell.isFuture, 'is-future': cell.isFuture, 'is-selected': cell.date === selectedDate }"
        :title="cell.tooltip"
        @click="cell.day && !cell.isFuture && $emit('select-date', cell.date)"
      >
        <span v-if="cell.day" class="day-num">{{ cell.day }}</span>
        <span v-if="cell.icons.length" class="day-icons">{{ cell.icons.join('') }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  careLogs: {
    type: Array,
    default: () => []
  },
  selectedDate: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['select-date'])

const weekdays = ['日', '月', '火', '水', '木', '金', '土']

const careTypeIcon = { water: '💧', fertilize: '🌿', harvest: '🌾', other: '📝' }
const careTypeLabel = { water: '水やり', fertilize: '肥料', harvest: '収穫', other: 'その他' }

const today = new Date()
const viewYear = ref(today.getFullYear())
const viewMonth = ref(today.getMonth())

function prevMonth() {
  if (viewMonth.value === 0) {
    viewMonth.value = 11
    viewYear.value -= 1
  } else {
    viewMonth.value -= 1
  }
}

function nextMonth() {
  if (viewMonth.value === 11) {
    viewMonth.value = 0
    viewYear.value += 1
  } else {
    viewMonth.value += 1
  }
}

function goToToday() {
  viewYear.value = today.getFullYear()
  viewMonth.value = today.getMonth()
  emit('select-date', toDateStr(today.getFullYear(), today.getMonth(), today.getDate()))
}

function toDateStr(year, month, day) {
  return `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`
}

// dateStr（YYYY-MM-DD）ごとに、その日に行ったケア種別の集合を作る（同じ種別の重複は1つに集約）
const careTypesByDate = computed(() => {
  const map = new Map()
  props.careLogs.forEach(log => {
    const dateStr = log.caredAt ? log.caredAt.split('T')[0] : null
    if (!dateStr) return
    if (!map.has(dateStr)) map.set(dateStr, new Set())
    map.get(dateStr).add(log.careType)
  })
  return map
})

const cells = computed(() => {
  const year = viewYear.value
  const month = viewMonth.value
  const firstWeekday = new Date(year, month, 1).getDay()
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const todayStr = toDateStr(today.getFullYear(), today.getMonth(), today.getDate())

  const result = []
  for (let i = 0; i < firstWeekday; i++) {
    result.push({ day: null, date: null, icons: [], isToday: false, isFuture: false, tooltip: '' })
  }
  for (let day = 1; day <= daysInMonth; day++) {
    const dateStr = toDateStr(year, month, day)
    const types = careTypesByDate.value.get(dateStr)
    const icons = types ? [...types].map(t => careTypeIcon[t] || '📝') : []
    const isFuture = dateStr > todayStr
    const tooltip = types
      ? `${dateStr}: ${[...types].map(t => careTypeLabel[t] || t).join('・')}`
      : dateStr
    result.push({ day, date: dateStr, icons, isToday: dateStr === todayStr, isFuture, tooltip })
  }
  return result
})
</script>

<style scoped>
.calendar-container { margin: 2rem 0; }
.calendar-header { display: flex; align-items: center; justify-content: center; gap: 1rem; margin-bottom: 0.75rem; }
.calendar-header h3 { margin: 0; min-width: 8em; text-align: center; }
.nav-btn { background: #f0f0f0; color: #333; border: none; border-radius: 6px; padding: 0.4rem 0.8rem; cursor: pointer; }
.today-btn { background: #4a7a9d; color: #fff; border: none; border-radius: 6px; padding: 0.4rem 0.8rem; font-size: 0.85rem; cursor: pointer; }
.weekdays { display: grid; grid-template-columns: repeat(7, 1fr); text-align: center; font-size: 12px; color: #999; margin-bottom: 4px; }
.grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 4px; }
.cell { aspect-ratio: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; border-radius: 6px; background: #f9f9f9; gap: 2px; }
.cell.is-empty { background: transparent; }
.cell.is-today { background: #e3f3e6; border: 2px solid #4a9d5f; }
.cell.is-clickable { cursor: pointer; }
.cell.is-future { opacity: 0.4; }
.cell.is-selected { background: #cce8d4; box-shadow: inset 0 0 0 2px #2d6a3f; }
.day-num { font-size: 12px; color: #666; }
.day-icons { font-size: 12px; line-height: 1; }
</style>
