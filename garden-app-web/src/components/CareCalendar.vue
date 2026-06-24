<template>
  <div class="calendar-container">
    <div class="calendar-header">
      <button class="planted-btn" :disabled="!earliestDate" @click="goToFirstDate">初日</button>
      <div class="calendar-nav">
        <button class="nav-btn" @click="prevMonth">◀</button>
        <h3>{{ viewYear }}年{{ viewMonth + 1 }}月</h3>
        <button class="nav-btn" @click="nextMonth">▶</button>
      </div>
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
        <div v-if="cell.dots.length" class="day-dots">
          <span v-for="(color, i) in cell.dots" :key="i" class="day-dot" :style="{ background: color }"></span>
          <span v-if="cell.extraCount > 0" class="day-extra">+{{ cell.extraCount }}</span>
        </div>
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

const careTypeColor = { water: '#4a90d9', fertilize: '#4a9d5f', harvest: '#d9a92a', other: '#9b9b9b' }
const careTypeLabel = { water: '水やり', fertilize: '肥料', harvest: '収穫', other: 'その他' }
const DOT_LIMIT = 4

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

// ケア記録の中で最も古い日付（データ登録のある最初の日付）
const earliestDate = computed(() => {
  const dates = props.careLogs.map(log => log.caredAt ? log.caredAt.split('T')[0] : null).filter(Boolean)
  return dates.length ? dates.sort()[0] : null
})

function goToFirstDate() {
  if (!earliestDate.value) return
  const [y, m] = earliestDate.value.split('-').map(Number)
  viewYear.value = y
  viewMonth.value = m - 1
  emit('select-date', earliestDate.value)
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
    result.push({ day: null, date: null, dots: [], extraCount: 0, isToday: false, isFuture: false, tooltip: '' })
  }
  for (let day = 1; day <= daysInMonth; day++) {
    const dateStr = toDateStr(year, month, day)
    const types = careTypesByDate.value.get(dateStr)
    const typeList = types ? [...types] : []
    const dots = typeList.slice(0, DOT_LIMIT).map(t => careTypeColor[t] || careTypeColor.other)
    const extraCount = Math.max(0, typeList.length - DOT_LIMIT)
    const isFuture = dateStr > todayStr
    const tooltip = typeList.length
      ? `${dateStr}: ${typeList.map(t => careTypeLabel[t] || t).join('・')}`
      : dateStr
    result.push({ day, date: dateStr, dots, extraCount, isToday: dateStr === todayStr, isFuture, tooltip })
  }
  return result
})
</script>

<style scoped>
.calendar-container { margin: 2rem 0; }
.calendar-header { display: grid; grid-template-columns: 1fr auto 1fr; align-items: center; margin-bottom: 0.75rem; }
.calendar-nav { grid-column: 2; display: flex; align-items: center; gap: 1rem; justify-content: center; }
.calendar-header h3 { margin: 0; min-width: 8em; text-align: center; }
.nav-btn { background: #f0f0f0; color: #333; border: none; border-radius: 6px; padding: 0.4rem 0.8rem; cursor: pointer; }
.today-btn { grid-column: 3; justify-self: end; background: #4a7a9d; color: #fff; border: none; border-radius: 6px; padding: 0.4rem 0.8rem; font-size: 0.85rem; cursor: pointer; }
.planted-btn { grid-column: 1; justify-self: start; background: #4a7a9d; color: #fff; border: none; border-radius: 6px; padding: 0.4rem 0.8rem; font-size: 0.85rem; cursor: pointer; }
.planted-btn:disabled { background: #e3e3e3; color: #999; cursor: not-allowed; }
.weekdays { display: grid; grid-template-columns: repeat(7, 1fr); text-align: center; font-size: 12px; color: #999; margin-bottom: 4px; }
.grid { display: grid; grid-template-columns: repeat(7, minmax(0, 1fr)); gap: 4px; }
.cell { aspect-ratio: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; border-radius: 6px; background: #f9f9f9; gap: 2px; min-width: 0; }
.cell.is-empty { background: transparent; }
.cell.is-today { background: #e3f3e6; border: 2px solid #4a9d5f; }
.cell.is-clickable { cursor: pointer; }
.cell.is-future { opacity: 0.4; }
.cell.is-selected { background: #cce8d4; box-shadow: inset 0 0 0 2px #2d6a3f; }
.day-num { font-size: 12px; color: #666; }
.day-dots { display: flex; gap: 2px; align-items: center; max-width: 100%; }
.day-dot { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }
.day-extra { font-size: 9px; color: #767676; line-height: 1; }

@media (max-width: 600px) {
  .calendar-header { gap: 0.5rem; }
  .calendar-header h3 { min-width: 6em; font-size: 1rem; }
  .nav-btn, .today-btn, .planted-btn { padding: 0.3rem 0.6rem; font-size: 0.8rem; }
}
</style>
