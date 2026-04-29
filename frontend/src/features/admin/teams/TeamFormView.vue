<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTeam, createTeam, updateTeam } from './teamAdminService.js'
import { searchSections } from '../sections/sectionAdminService.js'
import ErrorMessage from '../../../components/ErrorMessage.vue'
import LoadingSpinner from '../../../components/LoadingSpinner.vue'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const form = ref({ name: '', description: '', websiteUrl: '', sectionId: '' })
const sections = ref([])
const loading = ref(false)
const pageLoading = ref(false)
const error = ref('')

onMounted(async () => {
  try {
    sections.value = await searchSections({})
  } catch {
    error.value = 'Failed to load sections.'
  }

  // Pre-select section when coming from a section detail page (?sectionId=X)
  if (!isEdit.value && route.query.sectionId) {
    form.value.sectionId = Number(route.query.sectionId)
    return
  }

  if (!isEdit.value) return
  pageLoading.value = true
  try {
    const team = await getTeam(route.params.id)
    const matchedSection = sections.value.find(s => s.name === team.sectionName)
    form.value = {
      name: team.name,
      description: team.description ?? '',
      websiteUrl: team.websiteUrl ?? '',
      sectionId: matchedSection ? matchedSection.id : '',
    }
  } catch {
    error.value = 'Failed to load team.'
  } finally {
    pageLoading.value = false
  }
})

async function handleSubmit() {
  error.value = ''
  loading.value = true
  try {
    const payload = {
      name: form.value.name,
      description: form.value.description,
      websiteUrl: form.value.websiteUrl,
      sectionId: form.value.sectionId || null,
    }
    if (isEdit.value) {
      await updateTeam(route.params.id, payload)
      router.push(`/admin/teams/${route.params.id}`)
    } else {
      const team = await createTeam(payload)
      router.push(`/admin/teams/${team.id}`)
    }
  } catch (e) {
    error.value = e.response?.data?.message ?? 'Failed to save team.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div>
    <RouterLink to="/admin/teams" class="back-link">← Teams</RouterLink>
    <h1>{{ isEdit ? 'Edit Team' : 'Create Team' }}</h1>

    <LoadingSpinner v-if="pageLoading" />

    <form v-else class="form" @submit.prevent="handleSubmit">
      <ErrorMessage :message="error" />

      <div class="field">
        <label>Team Name *</label>
        <input v-model="form.name" required />
      </div>
      <div class="field">
        <label>Description</label>
        <textarea v-model="form.description" rows="3" />
      </div>
      <div class="field">
        <label>Website URL</label>
        <input v-model="form.websiteUrl" type="url" placeholder="https://…" />
      </div>
      <div class="field">
        <label>Section</label>
        <select v-model="form.sectionId">
          <option value="">— None —</option>
          <option v-for="s in sections" :key="s.id" :value="s.id">{{ s.name }}</option>
        </select>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? 'Saving…' : isEdit ? 'Save Changes' : 'Create Team' }}
        </button>
        <RouterLink :to="isEdit ? `/admin/teams/${route.params.id}` : '/admin/teams'" class="btn-cancel">Cancel</RouterLink>
      </div>
    </form>
  </div>
</template>

<style scoped>
.back-link { color: #2c3e50; font-size: 13px; text-decoration: none; display: inline-block; margin-bottom: 20px; }
.back-link:hover { text-decoration: underline; }
h1 { font-size: 22px; margin: 0 0 24px; color: #2c3e50; }
.form { max-width: 480px; }
.field { display: flex; flex-direction: column; margin-bottom: 18px; }
label { font-size: 13px; font-weight: 600; color: #555; margin-bottom: 6px; }
input, textarea, select { padding: 8px 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; font-family: inherit; }
textarea { resize: vertical; }
.form-actions { display: flex; align-items: center; gap: 14px; margin-top: 8px; }
.btn-primary { padding: 9px 22px; background: #2c3e50; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-cancel { font-size: 13px; color: #666; text-decoration: none; }
.btn-cancel:hover { text-decoration: underline; }
</style>
