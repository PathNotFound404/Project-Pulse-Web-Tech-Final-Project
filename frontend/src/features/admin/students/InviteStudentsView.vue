<script setup>
import { ref } from 'vue'
import { inviteStudents } from './studentAdminService.js'
import ErrorMessage from '../../../components/ErrorMessage.vue'

const emailsText = ref('')
const links = ref([])
const loading = ref(false)
const error = ref('')

async function handleInvite() {
  error.value = ''
  links.value = []
  const emails = emailsText.value.split(/[\n,]+/).map(e => e.trim()).filter(Boolean)
  if (!emails.length) { error.value = 'Enter at least one email address.'; return }
  loading.value = true
  try {
    links.value = await inviteStudents(emails)
  } catch (e) {
    error.value = e.response?.data?.message ?? 'Failed to generate invite links.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div>
    <RouterLink to="/admin/students" class="back-link">← Students</RouterLink>
    <h1>Invite Students</h1>
    <p class="hint">Enter one email address per line (or comma-separated). Each will receive a unique registration link.</p>

    <ErrorMessage :message="error" />

    <textarea v-model="emailsText" rows="6" placeholder="student1@tcu.edu&#10;student2@tcu.edu" class="email-input" />

    <button :disabled="loading" class="btn-primary" @click="handleInvite">
      {{ loading ? 'Generating…' : 'Generate Invite Links' }}
    </button>

    <div v-if="links.length" class="results">
      <h2>Generated Links</h2>
      <p class="hint">Share these links with the corresponding students.</p>
      <table class="data-table">
        <thead><tr><th>Email</th><th>Registration Link</th></tr></thead>
        <tbody>
          <tr v-for="l in links" :key="l.email">
            <td>{{ l.email }}</td>
            <td class="link-cell"><a :href="l.link" target="_blank">{{ l.link }}</a></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.back-link { color: #2c3e50; font-size: 13px; text-decoration: none; display: inline-block; margin-bottom: 20px; }
.back-link:hover { text-decoration: underline; }
h1 { font-size: 22px; margin: 0 0 6px; color: #2c3e50; }
.hint { font-size: 13px; color: #666; margin: 0 0 16px; }
.email-input { width: 100%; max-width: 480px; padding: 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 13px; font-family: inherit; display: block; margin-bottom: 14px; resize: vertical; }
.btn-primary { padding: 9px 20px; background: #2c3e50; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.results { margin-top: 28px; }
.results h2 { font-size: 16px; margin: 0 0 6px; }
.data-table { width: 100%; border-collapse: collapse; font-size: 13px; margin-top: 10px; }
.data-table th { text-align: left; padding: 8px 12px; border-bottom: 2px solid #ddd; }
.data-table td { padding: 8px 12px; border-bottom: 1px solid #eee; word-break: break-all; }
.link-cell a { color: #2c3e50; }
</style>
