import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/plants'
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue')
    },
    {
      path: '/plants',
      name: 'plants',
      component: () => import('@/views/PlantListView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/plants/:id',
      name: 'plant-detail',
      component: () => import('@/views/PlantDetailView.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

// 認証が必要なページへのガード
router.beforeEach((to) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.token) {
    return { name: 'login' }
  }
})

export default router
