<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSection, createSection, updateSection } from './sectionAdminService.js'
import { getRubrics } from '../rubrics/rubricAdminService.js'
import ErrorMessage from '../../../components/ErrorMessage.vue'
import LoadingSpinner from '../../../components/LoadingSpinner.vue'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const form = ref({ name: '', startDate: '', endDate: '', rubricId: '' })
const rubrics = ref([])
const loading = ref(false)
const pageLoading = ref(false)
const error = ref('')

onMounted(async () => {
  pageLoading.value = true
  try {
    rubrics.value = await getRubrics()
    if (isEdit.value) {
      const s = await getSection(route.params.id)
      form.value = { name: s.name, startDate: s.startDate ?? '', endDate: s.endDate ?? '', rubricId: '' }
    }
  } catch (e) {
    error.value = 'Failed to load data.'
  } finally {
    pageLoading.value = false
  }
})

async function handleSubmit() {
  error.value = ''
  loading.value = true
  try {
    const payload = { ...form.value, rubricId: form.value.rubricId ? Number(form.value.rubricId) : undefined }
    if (isEdit.value) {
      await updateSection(route.params.id, payload)
      router.push(`/admin/sections/${route.params.id}`)
    } else {
      const s = await createSection(payload)
      router.push(`/admin/sections/${s.id}`)
    }
  } catch (e) {
    error.value = e.response?.data?.message ?? 'Failed to save section.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div>
    <RouterLink to="/admin/sections" class="back-link">← Sections</RouterLink>
    <h1>{{ isEdit ? 'Edit Section' : 'Create Section' }}</h1>

    <LoadingSpinner v-if="pageLoading" />

    <form v-else class="form" @submit.prevent="handleSubmit">
      <ErrorMessage :message="error" />

      <div class="field">
        <label>Section Name *</label>
        <input v-model="form.name" required />
      </div>
      <div class="field">
        <label>Start Date</label>
        <input v-model="form.startDate" type="date" />
      </div>
      <div class="field">
        <label>End Date</label>
        <input v-model="form.endDate" type="date" />
      </div>
      <div class="field">
        <label>Rubric</label>
        <select v-model="form.rubricId">
          <option value="">— None —</option>
          <option v-for="r in rubrics" :key="r.id" :value="r.id">{{ r.name }}</option>
        </select>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? 'Saving…' : isEdit ? 'Save Changes' : 'Create Section' }}
        </button>
        <RouterLink :to="isEdit ? `/admin/sections/${route.params.id}` : '/admin/sections'" class="btn-cancel">Cancel</RouterLink>
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
input, select { padding: 8px 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; }
.form-actions { display: flex; align-items: center; gap: 14px; margin-top: 8px; }
.btn-primary { padding: 9px 22px; background: #2c3e50; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-cancel { font-size: 13px; color: #666; text-decoration: none; }
.btn-cancel:hover { text-decoration: underline; }
</style>
