import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../features/auth/LoginView.vue'
import RegisterView from '../features/auth/RegisterView.vue'
import AccountView from '../features/account/AccountView.vue'
import WarView from '../features/war/WarView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', component: LoginView },
    { path: '/register', component: RegisterView },
    { path: '/account', component: AccountView },
    { path: '/war', component: WarView },
  ],
})

export default router
