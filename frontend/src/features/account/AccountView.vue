<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProfile, updateProfile } from './accountService.js'

const router = useRouter()

const form = ref({ firstName: '', lastName: '', email: '' })
const original = ref({ firstName: '', lastName: '', email: '' })
const errors = ref({})
const apiError = ref('')
const successMessage = ref('')
const confirming = ref(false)
const loading = ref(true)

onMounted(async () => {
  try {
    const { data } = await getProfile()
    if (data.flag) {
      form.value = { ...data.data }
      original.value = { ...data.data }
    } else {
      apiError.value = data.message
    }
  } catch (err) {
    if (err.response?.status === 401) {
      router.push('/login')
    } else {
      apiError.value = err.response?.data?.message ?? 'Failed to load profile.'
    }
  } finally {
    loading.value = false
  }
})

function validate() {
  const e = {}
  if (!form.value.firstName.trim()) e.firstName = 'First name is required'
  if (!form.value.lastName.trim()) e.lastName = 'Last name is required'
  if (!form.value.email.trim()) {
    e.email = 'Email is required'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
    e.email = 'Invalid email format'
  }
  errors.value = e
  return Object.keys(e).length === 0
}

function handleSaveClick() {
  apiError.value = ''
  successMessage.value = ''
  if (!validate()) return
  confirming.value = true
}

async function handleConfirm() {
  try {
    const { data } = await updateProfile({
      firstName: form.value.firstName,
      lastName: form.value.lastName,
      email: form.value.email,
    })
    if (data.flag) {
      original.value = { ...form.value }
      successMessage.value = 'Account updated successfully'
      confirming.value = false
    } else {
      apiError.value = data.message
      confirming.value = false
    }
  } catch (err) {
    if (err.response?.status === 401) {
      router.push('/login')
    } else {
      apiError.value = err.response?.data?.message ?? 'Something went wrong. Please try again.'
      confirming.value = false
    }
  }
}

function handleBack() {
  confirming.value = false
}

function handleCancel() {
  form.value = { ...original.value }
  errors.value = {}
  apiError.value = ''
  successMessage.value = ''
  confirming.value = false
}
</script>

<template>
  <div class="account-page">
    <h1>Edit Account</h1>

    <p v-if="loading" class="loading">Loading…</p>

    <template v-else>
      <div v-if="confirming" class="confirm-panel">
        <p>Are you sure you want to save these changes?</p>
        <div class="actions">
          <button type="button" class="btn-primary" @click="handleConfirm">Confirm</button>
          <button type="button" class="btn-secondary" @click="handleBack">Back</button>
        </div>
      </div>

      <form v-else @submit.prevent="handleSaveClick">
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

        <p v-if="apiError" class="error api-error">{{ apiError }}</p>
        <p v-if="successMessage" class="success">{{ successMessage }}</p>

        <div class="actions">
          <button type="submit" class="btn-primary">Save Changes</button>
          <button type="button" class="btn-secondary" @click="handleCancel">Cancel</button>
        </div>
      </form>
    </template>
  </div>
</template>

<style scoped>
.account-page {
  max-width: 400px;
  margin: 60px auto;
  padding: 24px;
}

.loading {
  color: #666;
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

.confirm-panel {
  padding: 16px;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  margin-bottom: 16px;
}

.confirm-panel p {
  margin: 0 0 16px;
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

.btn-primary {
  background-color: #2c3e50;
  color: white;
}

.btn-secondary {
  background-color: #ecf0f1;
  color: #333;
}
</style>
