import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const ROLE_HOME = {
  student: '/home',
  instructor: '/instructor/home',
  admin: '/admin',
}

export const useAuthStore = defineStore('auth', () => {
  const userId = ref(null)
  const role = ref(null)
  const firstName = ref(null)
  const lastName = ref(null)

  const isLoggedIn = computed(() => userId.value !== null)
  const fullName = computed(() => [firstName.value, lastName.value].filter(Boolean).join(' '))
  const homeRoute = computed(() => (role.value ? ROLE_HOME[role.value] : '/login'))

  function init() {
    const stored = localStorage.getItem('auth')
    if (!stored) return
    try {
      const parsed = JSON.parse(stored)
      userId.value = parsed.userId
      role.value = parsed.role
      firstName.value = parsed.firstName
      lastName.value = parsed.lastName
    } catch {
      localStorage.removeItem('auth')
    }
  }

  function login({ id, role: userRole, firstName: fn, lastName: ln }) {
    const normalizedRole = userRole.toLowerCase()
    userId.value = id
    role.value = normalizedRole
    firstName.value = fn
    lastName.value = ln
    localStorage.setItem('auth', JSON.stringify({
      userId: id,
      role: normalizedRole,
      firstName: fn,
      lastName: ln,
    }))
  }

  function logout(router) {
    userId.value = null
    role.value = null
    firstName.value = null
    lastName.value = null
    localStorage.removeItem('auth')
    router.push('/login')
  }

  return { userId, role, firstName, lastName, isLoggedIn, fullName, homeRoute, init, login, logout }
})
