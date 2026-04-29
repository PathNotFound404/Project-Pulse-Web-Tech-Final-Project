<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../services/api.js'
import { useAuthStore } from '../../stores/auth.js'

const router = useRouter()
const auth = useAuthStore()

const form = ref({ email: '', password: '' })
const errorMessage = ref('')
const loading = ref(false)

const ROLE_HOME = {
  student: '/home',
  instructor: '/instructor/home',
  admin: '/admin',
}

async function handleLogin() {
  errorMessage.value = ''
  loading.value = true
  try {
    const { data } = await api.post('/api/auth/login', form.value)
    if (data.flag) {
      auth.login({
        id: data.data.id,
        role: data.data.role,
        firstName: data.data.firstName,
        lastName: data.data.lastName,
      })
      router.push(ROLE_HOME[data.data.role?.toLowerCase()] ?? '/home')
    } else {
      errorMessage.value = data.message || 'Invalid email or password'
    }
  } catch (err) {
    errorMessage.value = err.response?.data?.message ?? 'Invalid email or password'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <h1>Project Pulse</h1>
    <h2>Log In</h2>

    <form @submit.prevent="handleLogin">
      <div class="field">
        <label for="email">Email</label>
        <input id="email" v-model="form.email" type="text" autocomplete="email" />
      </div>

      <div class="field">
        <label for="password">Password</label>
        <input id="password" v-model="form.password" type="password" autocomplete="current-password" />
      </div>

      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

      <button type="submit" :disabled="loading">
        {{ loading ? 'Logging in…' : 'Log In' }}
      </button>
    </form>

    <p class="register-link">
      New student? <RouterLink to="/register">Create an account</RouterLink>
    </p>
  </div>
</template>

<style scoped>
.login-page {
  max-width: 400px;
  margin: 80px auto;
  padding: 32px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: white;
}

h1 {
  font-size: 20px;
  color: #2c3e50;
  margin: 0 0 4px;
}

h2 {
  font-size: 15px;
  font-weight: 400;
  color: #666;
  margin: 0 0 24px;
}

.field {
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
}

label {
  margin-bottom: 4px;
  font-weight: 500;
  font-size: 14px;
}

input {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
}

.error {
  color: #c0392b;
  font-size: 13px;
  margin-bottom: 12px;
}

button {
  width: 100%;
  padding: 10px;
  background-color: #2c3e50;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.register-link {
  margin-top: 16px;
  font-size: 14px;
  text-align: center;
}

.register-link a {
  color: #2c3e50;
}
</style>
