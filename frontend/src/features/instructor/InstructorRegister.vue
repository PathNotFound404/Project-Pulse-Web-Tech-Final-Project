<template>
  <!-- UC-30: Instructor sets up account via invite link -->
  <div class="register-container">
    <h2>Set Up Your Instructor Account</h2>

    <div v-if="alreadyRegistered" class="alert alert-info">
      You have already registered. Please
      <router-link to="/login">log in</router-link>.
    </div>

    <form v-else @submit.prevent="handleSubmit">
      <div class="form-group">
        <label>First Name *</label>
        <input v-model="form.firstName" type="text" required />
      </div>

      <div class="form-group">
        <label>Middle Initial</label>
        <input v-model="form.middleInitial" type="text" maxlength="1" />
      </div>

      <div class="form-group">
        <label>Last Name *</label>
        <input v-model="form.lastName" type="text" required />
      </div>

      <div class="form-group">
        <label>Password *</label>
        <input v-model="form.password" type="password" required />
      </div>

      <div class="form-group">
        <label>Re-enter Password *</label>
        <input v-model="form.reenterPassword" type="password" required />
      </div>

      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

      <div class="form-actions">
        <button type="button" @click="handleCancel">Cancel</button>
        <button type="submit" :disabled="loading">
          {{ loading ? 'Registering...' : 'Create Account' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script>
import { registerInstructor } from './instructorService'

export default {
  name: 'InstructorRegister',
  data() {
    return {
      form: {
        firstName: '',
        middleInitial: '',
        lastName: '',
        password: '',
        reenterPassword: ''
      },
      loading: false,
      errorMessage: '',
      alreadyRegistered: false
    }
  },
  computed: {
    // Pull token from URL query param: /register?token=abc123
    token() {
      return this.$route.query.token
    }
  },
  methods: {
    async handleSubmit() {
      this.errorMessage = ''

      if (this.form.password !== this.form.reenterPassword) {
        this.errorMessage = 'Passwords do not match.'
        return
      }

      this.loading = true
      try {
        await registerInstructor(this.token, this.form)
        // UC-30: Redirect to login after successful registration
        this.$router.push('/login')
      } catch (err) {
        const msg = err.response?.data?.message || 'Registration failed.'
        if (msg.includes('already')) {
          this.alreadyRegistered = true
        } else {
          this.errorMessage = msg
        }
      } finally {
        this.loading = false
      }
    },
    handleCancel() {
      // UC-30: Cancel at any time before submitting
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
.register-container {
  max-width: 480px;
  margin: 60px auto;
  padding: 32px;
  border: 1px solid #ddd;
  border-radius: 8px;
}
.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
}
.form-group label {
  margin-bottom: 4px;
  font-weight: 500;
}
.form-group input {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
}
.error {
  color: red;
  font-size: 0.9em;
}
.alert-info {
  background: #e8f4fd;
  padding: 12px;
  border-radius: 4px;
}
</style>