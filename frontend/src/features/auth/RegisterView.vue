<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '../../services/api.js'

const router = useRouter()
const route = useRoute()

const form = ref({ firstName: '', lastName: '', email: '', password: '' })
const errors = ref({})
const apiError = ref('')
const successMessage = ref('')

function validate() {
  const e = {}
  if (!form.value.firstName.trim()) e.firstName = 'First name is required'
  if (!form.value.lastName.trim()) e.lastName = 'Last name is required'
  if (!form.value.email.trim()) {
    e.email = 'Email is required'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
    e.email = 'Invalid email format'
  }
  if (!form.value.password) {
    e.password = 'Password is required'
  } else if (form.value.password.length < 8) {
    e.password = 'Password must be at least 8 characters'
  }
  errors.value = e
  return Object.keys(e).length === 0
}

async function handleSubmit() {
  apiError.value = ''
  if (!validate()) return

  try {
    const { data } = await api.post(`/api/auth/register/student?token=${encodeURIComponent(route.query.token)}`, {
      firstName: form.value.firstName,
      lastName: form.value.lastName,
      email: form.value.email,
      password: form.value.password,
    })

    if (data.flag) {
      successMessage.value = 'Account created!'
      setTimeout(() => router.push('/login'), 1500)
    } else {
      apiError.value = data.message
    }
  } catch (err) {
    apiError.value = err.response?.data?.message ?? 'Something went wrong. Please try again.'
  }
}

function handleCancel() {
  router.push('/login')
}
</script>

<template>
  <div class="register-page">
    <h1>Create Your Account</h1>

    <form @submit.prevent="handleSubmit">
      <div class="field">
        <label for="firstName">First Name</label>
        <input id="firstName" v-model="form.firstName" type="text" />
        <span v-if="errors.firstName" class="error">{{ errors.firstName }}</span>
      </div>

      <div class="field">
        <label for="lastName">Last Name</label>
        <input id="lastName" v-model="form.lastName" type="text" />
        <span v-if="errors.lastName" class="error">{{ errors.lastName }}</span>
      </div>

      <div class="field">
        <label for="email">Email</label>
        <input id="email" v-model="form.email" type="text" />
        <span v-if="errors.email" class="error">{{ errors.email }}</span>
      </div>

      <div class="field">
        <label for="password">Password</label>
        <input id="password" v-model="form.password" type="password" />
        <span v-if="errors.password" class="error">{{ errors.password }}</span>
      </div>

      <p v-if="apiError" class="error api-error">{{ apiError }}</p>
      <p v-if="successMessage" class="success">{{ successMessage }}</p>

      <div class="actions">
        <button type="submit">Create Account</button>
        <button type="button" @click="handleCancel">Cancel</button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.register-page {
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
  margin-top: 4px;
}

.api-error {
  margin-bottom: 12px;
}

.success {
  color: #27ae60;
  margin-bottom: 12px;
}

.actions {
  display: flex;
  gap: 12px;
}

button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

button[type='submit'] {
  background-color: #2c3e50;
  color: white;
}

button[type='button'] {
  background-color: #ecf0f1;
  color: #333;
}
</style>