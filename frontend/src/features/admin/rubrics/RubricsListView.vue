<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getRubrics, deleteRubric } from './rubricAdminService.js'
import LoadingSpinner from '../../../components/LoadingSpinner.vue'
import ErrorMessage from '../../../components/ErrorMessage.vue'

const router = useRouter()
const rubrics = ref([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    rubrics.value = await getRubrics()
  } catch (e) {
    error.value = 'Failed to load rubrics.'
  } finally {
    loading.value = false
  }
})

async function handleDelete(id, name) {
  if (!confirm(`Delete rubric "${name}"? This cannot be undone.`)) return
  try {
    await deleteRubric(id)
    rubrics.value = rubrics.value.filter(r => r.id !== id)
  } catch (e) {
    error.value = e.response?.data?.message ?? 'Failed to delete rubric.'
  }
}
</script>

<template>
  <div>
    <div class="page-header">
      <h1>Rubrics</h1>
      <RouterLink to="/admin/rubrics/new" class="btn-primary">Create Rubric</RouterLink>
    </div>

    <ErrorMessage :message="error" />
    <LoadingSpinner v-if="loading" />

    <table v-else-if="rubrics.length" class="data-table">
      <thead>
        <tr><th>Name</th><th>Criteria</th><th></th></tr>
      </thead>
      <tbody>
        <tr v-for="r in rubrics" :key="r.id">
          <td class="clickable" @click="router.push(`/admin/rubrics/${r.id}`)">{{ r.name }}</td>
          <td>{{ r.criteria?.length ?? 0 }}</td>
          <td class="action-cell">
            <RouterLink :to="`/admin/rubrics/${r.id}`" class="link-edit">Edit</RouterLink>
            <button class="btn-delete" @click="handleDelete(r.id, r.name)">Delete</button>
          </td>
        </tr>
      </tbody>
    </table>

    <p v-else-if="!loading" class="empty">No rubrics found.</p>
  </div>
</template>

<style scoped>
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
h1 { font-size: 22px; margin: 0; color: #2c3e50; }
.data-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.data-table th { text-align: left; padding: 10px 12px; border-bottom: 2px solid #ddd; color: #555; font-weight: 600; }
.data-table td { padding: 10px 12px; border-bottom: 1px solid #eee; }
.clickable { cursor: pointer; color: #2c3e50; }
.clickable:hover { text-decoration: underline; }
.action-cell { display: flex; gap: 14px; align-items: center; }
.link-edit { color: #2c3e50; text-decoration: none; font-size: 13px; }
.link-edit:hover { text-decoration: underline; }
.btn-delete { background: none; border: none; cursor: pointer; color: #c0392b; font-size: 13px; }
.empty { color: #888; margin-top: 20px; }
.btn-primary { padding: 8px 18px; background: #2c3e50; color: white; border-radius: 4px; text-decoration: none; font-size: 13px; }
</style>
