<template>
  <select class="lang-selector" :value="lang" @change="changeLang($event.target.value)">
    <option v-for="opt in langList" :key="opt.code" :value="opt.code">{{ isMobile ? opt.short : `${opt.flag} ${opt.name}` }}</option>
  </select>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useLanguage } from '@/composables/useLanguage'

const { lang, changeLang, langList } = useLanguage()

// 768px未満ではプルダウンの表示をJP/USのようなイニシャルだけにする（タイトル改行を防ぐため）
const isMobile = ref(window.innerWidth < 768)
function handleResize() {
  isMobile.value = window.innerWidth < 768
}
onMounted(() => window.addEventListener('resize', handleResize))
onUnmounted(() => window.removeEventListener('resize', handleResize))
</script>

<style scoped>
.lang-selector {
  background: #fff;
  color: #2d3436;
  border: 1px solid #e3e3e3;
  border-radius: 8px;
  padding: 0.35rem 0.5rem;
  font-size: 0.8rem;
  cursor: pointer;
}
</style>
