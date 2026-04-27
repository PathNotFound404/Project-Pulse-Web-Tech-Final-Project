import { createRouter, createWebHistory } from 'vue-router'
import RegisterView from '../features/auth/RegisterView.vue'
import AccountView from '../features/account/AccountView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/register',
      component: RegisterView,
    },
    {
      path: '/account',
      component: AccountView,
    },
  ],
})

export default router
