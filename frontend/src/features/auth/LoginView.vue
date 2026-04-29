<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../services/api.js'

const router = useRouter()

const form = ref({ email: '', password: '' })
const errorMessage = ref('')
const loading = ref(false)

async function handleLogin() {
  errorMessage.value = ''
  loading.value = true

  try {
    const { data } = await api.post('/api/auth/login', {
      email: form.value.email,
      password: form.value.password,
    })

    if (data.flag) {
      localStorage.setItem('studentId', data.data.id)
      router.push('/home')
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
    <h1>Log In</h1>

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
      New user? <a href="/register">Create an account</a>
    </p>
  </div>
</template>

<style scoped>
.login-page {
  max-width: 400px;
  margin: 60px auto;
  padding: 24px;
}

.field {
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
}

label {
  margin-bottom: 4px;
  font-weight: 500;
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
</style>
