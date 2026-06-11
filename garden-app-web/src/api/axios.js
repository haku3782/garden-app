import axios from 'axios'

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

export default api
