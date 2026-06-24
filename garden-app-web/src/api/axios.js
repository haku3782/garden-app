import axios from 'axios'
import router from '@/router'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8081',
})

// リクエストのたびにトークンを自動で付ける
// useAuthStore() はコンポーネント外で呼べないため localStorage から直接取得する
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// トークンが無効・期限切れ（401/403）の場合は認証情報を破棄してログイン画面に戻す
// useAuthStore() はモジュール読み込み時に Pinia がまだ無いため使わず、直接 localStorage を操作する
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      if (router.currentRoute.value.name !== 'login') {
        router.push('/login')
      }
    }
    return Promise.reject(error)
  }
)

export default api
