<template>
  <div class="container">
    <div class="top-bar">
      <button @click="router.back()" class="back-btn">{{ t('backBtn') }}</button>
    </div>
    <h1>{{ t('addPlantTitle') }}</h1>

    <div class="add-form">
      <div class="type-tabs">
        <button
          v-for="option in typeOptions"
          :key="option.value"
          type="button"
          class="type-tab"
          :class="{ active: newPlant.type === option.value }"
          @click="newPlant.type = option.value"
        >{{ t(option.labelKey) }}</button>
      </div>
      <input v-model="newPlant.name" type="text" :placeholder="t('namePlaceholder')" />
      <input v-model="newPlant.memo" type="text" :placeholder="t('commentPlaceholder')" />
      <div class="add-actions">
        <button @click="handleCreate">{{ t('saveBtn') }}</button>
        <button type="button" class="cancel-btn" @click="router.back()">{{ t('cancelBtn') }}</button>
      </div>
      <p v-if="error" class="error">{{ error }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useLanguage } from '@/composables/useLanguage'
import { createPlant } from '@/api/plants'

const router = useRouter()
const { t } = useLanguage()

const newPlant = ref({
  name: '',
  type: '',
  memo: ''
})
const error = ref('')

const typeOptions = [
  { value: 'vegetable', labelKey: 'typeVegetable' },
  { value: 'fruit', labelKey: 'typeFruit' },
  { value: 'herb', labelKey: 'typeHerb' },
  { value: 'flower', labelKey: 'typeFlower' },
  { value: 'tree', labelKey: 'typeTree' },
  { value: 'other', labelKey: 'other' }
]

// ローカル時刻のまま "YYYY-MM-DDTHH:mm" を作る（toISOStringはUTC変換され日付がずれるため使わない）
function nowLocalDateTime() {
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`
}

async function handleCreate() {
  if (!newPlant.value.name || !newPlant.value.type) {
    error.value = t('addPlantError')
    return
  }
  error.value = ''
  // 植えた日は登録時点の日時を自動で使う
  const data = { ...newPlant.value, plantedAt: nowLocalDateTime() }
  await createPlant(data)
  router.back()
}
</script>

<style scoped>
.container {
  --color-primary: #4a9d5f;
  --color-primary-dark: #3d8350;
  --color-border: #e3e3e3;
  --color-bg-soft: #f7f8f7;
  --radius: 10px;

  max-width: 500px;
  margin: 0 auto;
  padding: 1rem;
  color: #2d3436;
}

.top-bar { display: flex; gap: 0.5rem; margin-bottom: 1.5rem; }
.back-btn {
  background: transparent;
  color: #767676;
  border: 1px solid var(--color-border);
  padding: 0.45rem 0.8rem;
  font-size: 0.85rem;
}
.back-btn:hover { background: var(--color-bg-soft); color: #2d3436; }

h1 { font-size: 1.05rem; margin-bottom: 1.25rem; }

.add-form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 1.25rem;
  background: var(--color-bg-soft);
  border-radius: var(--radius);
}

input {
  padding: 0.45rem 0.8rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  font-size: 0.85rem;
  background: #fff;
  transition: border-color 0.15s, box-shadow 0.15s;
}
input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(74, 157, 95, 0.15);
}

.type-tabs { display: flex; flex-wrap: wrap; gap: 0.5rem; }
.type-tab {
  background: #fff;
  color: #2d3436;
  border: 1px solid var(--color-border);
  padding: 0.45rem 0.9rem;
  font-size: 0.85rem;
  border-radius: 999px;
  transition: background-color 0.15s, border-color 0.15s;
}
.type-tab:hover { background: #fff; color: #2d3436; }
.type-tab.active { background: var(--color-primary); color: #fff; border-color: var(--color-primary); }
.type-tab.active:hover { background: var(--color-primary-dark); color: #fff; }

button {
  padding: 0.45rem 0.8rem;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius);
  font-size: 0.85rem;
  cursor: pointer;
  transition: background-color 0.15s, transform 0.05s;
}
button:hover { background: var(--color-primary-dark); }
button:active { transform: scale(0.97); }

.add-actions { display: flex; justify-content: flex-end; gap: 0.5rem; }
.cancel-btn {
  background: transparent;
  color: #767676;
  border: 1px solid var(--color-border);
}
.cancel-btn:hover { background: var(--color-bg-soft); color: #2d3436; }

.error { margin: 0; color: #2d3436; font-size: 0.85rem; }

@media (min-width: 768px) {
  .container { max-width: 600px; padding: 2rem; }
}
</style>
