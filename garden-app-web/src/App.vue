<template>
  <LanguageSelector class="global-lang-selector" />
  <RouterView />
</template>

<script setup>
import LanguageSelector from '@/components/LanguageSelector.vue'
</script>

<style scoped>
.global-lang-selector { position: fixed; top: 0.75rem; right: 0.75rem; z-index: 100; }
</style>
