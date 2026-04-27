import { createRouter, createWebHistory } from 'vue-router'
import RegisterView from '../features/auth/RegisterView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/register',
      component: RegisterView,
    },
  ],
})

export default router
