<template>
  <div class="auth-container">
    <h1 class="site-title">{{ t('appTitle') }}</h1>
    <h2>{{ t('loginTitle') }}</h2>
    <form @submit.prevent="handleLogin">
      <input v-model="username" type="text" :placeholder="t('usernamePlaceholder')" required />
      <input v-model="password" type="password" :placeholder="t('passwordPlaceholder')" required />
      <button type="submit">{{ t('loginBtn') }}</button>
    </form>
    <p class="auth-switch">{{ t('noAccountText') }}<router-link to="/register">{{ t('registerLink') }}</router-link></p>
    <p v-if="error" class="error">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useLanguage } from '@/composables/useLanguage'
import { login } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()
const { t } = useLanguage()

const username = ref('')
const password = ref('')
const error = ref('')

async function handleLogin() {
  try {
    const res = await login(username.value, password.value)
    authStore.login(res.data.token, username.value)
    router.push('/plants')
  } catch {
    error.value = t('loginError')
  }
}
</script>

<style scoped>
.auth-container {
  --color-primary: #4a9d5f;
  --color-primary-dark: #3d8350;
  --color-muted: #767676;
  --color-border: #e3e3e3;
  --color-danger: #e74c3c;
  --radius: 10px;

  max-width: 400px;
  margin: 100px auto;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  color: #2d3436;
}

.site-title { font-size: 1.4rem; text-align: center; margin: 0; }
h2 { font-size: 1.05rem; text-align: center; margin: 0 0 0.5rem; color: var(--color-muted); font-weight: normal; }

form { display: flex; flex-direction: column; gap: 1rem; }

input {
  width: 100%;
  padding: 0.7rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  font-size: 1rem;
  background: #fff;
  transition: border-color 0.15s, box-shadow 0.15s;
}
input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(74, 157, 95, 0.15);
}

button {
  width: 100%;
  padding: 0.7rem;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius);
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.15s;
}
button:hover { background: var(--color-primary-dark); }

.auth-switch { margin: 0; text-align: center; color: var(--color-muted); font-size: 0.9rem; }
.auth-switch a { color: var(--color-primary); text-decoration: none; font-weight: 500; }
.auth-switch a:hover { text-decoration: underline; }

.error { margin: 0; color: var(--color-danger); text-align: center; font-size: 0.9rem; }

@media (max-width: 600px) {
  .auth-container { margin: 40px auto; padding: 1.5rem; }
}
</style>
