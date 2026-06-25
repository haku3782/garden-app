<template>
  <span class="plant-placeholder-icon" :style="{ fontSize: size * 0.5 + 'px' }">♡</span>
</template>

<script setup>
defineProps({
  size: {
    type: Number,
    default: 64
  }
})
</script>

<style scoped>
.plant-placeholder-icon { display: block; color: #b5b9b5; line-height: 1; }
</style>
