<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getRubric, createRubric, updateRubric, deleteRubric } from './rubricAdminService.js'
import ErrorMessage from '../../../components/ErrorMessage.vue'
import LoadingSpinner from '../../../components/LoadingSpinner.vue'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => route.params.id !== 'new' && !!route.params.id)
const name = ref('')
const criteria = ref([])
const loading = ref(false)
const pageLoading = ref(false)
const error = ref('')

function blankCriterion() {
  return { name: '', description: '', maxScore: 10 }
}

onMounted(async () => {
  if (!isEdit.value) {
    criteria.value = [blankCriterion()]
    return
  }
  pageLoading.value = true
  try {
    const r = await getRubric(route.params.id)
    name.value = r.name
    criteria.value = r.criteria.map(c => ({ name: c.name, description: c.description, maxScore: c.maxScore }))
  } catch (e) {
    error.value = 'Failed to load rubric.'
  } finally {
    pageLoading.value = false
  }
})

function addCriterion() {
  criteria.value.push(blankCriterion())
}

function removeCriterion(index) {
  criteria.value.splice(index, 1)
}

async function handleSubmit() {
  error.value = ''
  if (!name.value.trim()) { error.value = 'Rubric name is required.'; return }
  if (!criteria.value.length) { error.value = 'At least one criterion is required.'; return }
  loading.value = true
  try {
    const payload = { name: name.value, criteria: criteria.value }
    if (isEdit.value) {
      await updateRubric(route.params.id, payload)
    } else {
      await createRubric(payload)
    }
    router.push('/admin/rubrics')
  } catch (e) {
    error.value = e.response?.data?.message ?? 'Failed to save rubric.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div>
    <RouterLink to="/admin/rubrics" class="back-link">← Rubrics</RouterLink>
    <h1>{{ isEdit ? 'Edit Rubric' : 'Create Rubric' }}</h1>

    <LoadingSpinner v-if="pageLoading" />

    <form v-else class="form" @submit.prevent="handleSubmit">
      <ErrorMessage :message="error" />

      <div class="field">
        <label>Rubric Name *</label>
        <input v-model="name" required placeholder="e.g. Senior Design Peer Eval" />
      </div>

      <h2>Criteria</h2>
      <div v-for="(c, i) in criteria" :key="i" class="criterion-row">
        <div class="criterion-fields">
          <input v-model="c.name" placeholder="Criterion name *" required />
          <input v-model="c.description" placeholder="Description" />
          <input v-model.number="c.maxScore" type="number" min="1" step="0.5" placeholder="Max score" style="width:100px" />
        </div>
        <button type="button" class="btn-remove" @click="removeCriterion(i)" :disabled="criteria.length === 1">✕</button>
      </div>

      <button type="button" class="btn-add" @click="addCriterion">+ Add Criterion</button>

      <div class="form-actions">
        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? 'Saving…' : isEdit ? 'Save Changes' : 'Create Rubric' }}
        </button>
        <RouterLink to="/admin/rubrics" class="btn-cancel">Cancel</RouterLink>
      </div>
    </form>
  </div>
</template>

<style scoped>
.back-link { color: #2c3e50; font-size: 13px; text-decoration: none; display: inline-block; margin-bottom: 20px; }
.back-link:hover { text-decoration: underline; }
h1 { font-size: 22px; margin: 0 0 24px; color: #2c3e50; }
h2 { font-size: 16px; margin: 24px 0 12px; }
.form { max-width: 680px; }
.field { display: flex; flex-direction: column; margin-bottom: 18px; }
label { font-size: 13px; font-weight: 600; color: #555; margin-bottom: 6px; }
input { padding: 8px 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; }
.criterion-row { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.criterion-fields { display: flex; gap: 10px; flex: 1; flex-wrap: wrap; }
.criterion-fields input { flex: 1; min-width: 120px; }
.btn-remove { background: none; border: none; cursor: pointer; color: #c0392b; font-size: 16px; padding: 4px; }
.btn-remove:disabled { opacity: 0.3; cursor: not-allowed; }
.btn-add { padding: 7px 16px; background: #eee; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; margin-bottom: 24px; }
.form-actions { display: flex; align-items: center; gap: 14px; }
.btn-primary { padding: 9px 22px; background: #2c3e50; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-cancel { font-size: 13px; color: #666; text-decoration: none; }
.btn-cancel:hover { text-decoration: underline; }
</style>
