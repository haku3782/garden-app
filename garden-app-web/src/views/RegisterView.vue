<template>
  <div class="auth-container">
    <h1>新規登録</h1>
    <form @submit.prevent="handleRegister">
      <input v-model="username" type="text" placeholder="ユーザー名" required />
      <input v-model="password" type="password" placeholder="パスワード" required />
      <button type="submit">登録</button>
      <p>アカウントがある方は<router-link to="/login">ログイン</router-link></p>
    </form>
    <p v-if="error" class="error">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { register } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const error = ref('')

async function handleRegister() {
  try {
    const res = await register(username.value, password.value)
    authStore.login(res.data.token, username.value)
    router.push('/plants')
  } catch {
    error.value = '登録に失敗しました'
  }
}
</script>

<style scoped>
.auth-container {
  max-width: 400px;
  margin: 100px auto;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1rem;
}
button {
  width: 100%;
  padding: 0.75rem;
  background: #4a9d5f;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
}
.error { color: red; }

@media (max-width: 600px) {
  .auth-container { margin: 40px auto; padding: 1.5rem; }
}
</style>
