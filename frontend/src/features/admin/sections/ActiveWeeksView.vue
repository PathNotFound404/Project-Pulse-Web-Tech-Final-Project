<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getSection, setupActiveWeeks } from './sectionAdminService.js'
import LoadingSpinner from '../../../components/LoadingSpinner.vue'
import ErrorMessage from '../../../components/ErrorMessage.vue'

const route = useRoute()
const section = ref(null)
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const success = ref('')
const inactiveWeeks = ref([])
const newWeek = ref('')

onMounted(async () => {
  try {
    section.value = await getSection(route.params.id)
  } catch (e) {
    error.value = 'Failed to load section.'
  } finally {
    loading.value = false
  }
})

function addInactiveWeek() {
  const w = newWeek.value.trim()
  if (!w || inactiveWeeks.value.includes(w)) return
  inactiveWeeks.value.push(w)
  newWeek.value = ''
}

function removeWeek(w) {
  inactiveWeeks.value = inactiveWeeks.value.filter(x => x !== w)
}

async function handleSave() {
  saving.value = true
  error.value = ''
  success.value = ''
  try {
    await setupActiveWeeks(route.params.id, inactiveWeeks.value)
    success.value = 'Active weeks saved.'
  } catch (e) {
    error.value = e.response?.data?.message ?? 'Failed to save active weeks.'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div>
    <RouterLink :to="`/admin/sections/${route.params.id}`" class="back-link">← Section</RouterLink>

    <LoadingSpinner v-if="loading" />
    <ErrorMessage :message="error" />

    <template v-if="section">
      <h1>Active Weeks — {{ section.name }}</h1>
      <p class="hint">
        Add the start dates (YYYY-MM-DD) of weeks that should be <strong>inactive</strong> (no peer evals or WARs accepted).
        All other weeks within the section's date range will be active.
      </p>

      <div class="add-row">
        <input v-model="newWeek" type="date" placeholder="Week start date" />
        <button class="btn-add" @click="addInactiveWeek">Add Inactive Week</button>
      </div>

      <ul v-if="inactiveWeeks.length" class="week-list">
        <li v-for="w in inactiveWeeks" :key="w">
          {{ w }}
          <button class="btn-remove" @click="removeWeek(w)">✕</button>
        </li>
      </ul>
      <p v-else class="empty">No inactive weeks configured. All weeks are active.</p>

      <p v-if="success" class="success">{{ success }}</p>

      <button class="btn-primary" :disabled="saving" @click="handleSave">
        {{ saving ? 'Saving…' : 'Save Active Weeks' }}
      </button>
    </template>
  </div>
</template>

<style scoped>
.back-link { color: #2c3e50; font-size: 13px; text-decoration: none; display: inline-block; margin-bottom: 20px; }
.back-link:hover { text-decoration: underline; }
h1 { font-size: 22px; margin: 0 0 8px; color: #2c3e50; }
.hint { font-size: 13px; color: #555; max-width: 560px; line-height: 1.6; margin-bottom: 20px; }
.add-row { display: flex; gap: 10px; margin-bottom: 16px; }
.add-row input { padding: 7px 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 13px; }
.btn-add { padding: 7px 16px; background: #eee; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.week-list { list-style: none; padding: 0; margin: 0 0 20px; }
.week-list li { display: flex; justify-content: space-between; align-items: center; padding: 7px 0; border-bottom: 1px solid #eee; font-size: 14px; max-width: 320px; }
.btn-remove { background: none; border: none; cursor: pointer; color: #c0392b; font-size: 14px; }
.empty { color: #888; font-size: 13px; margin-bottom: 20px; }
.success { color: #1e7e34; font-size: 13px; margin-bottom: 12px; }
.btn-primary { padding: 9px 22px; background: #2c3e50; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
