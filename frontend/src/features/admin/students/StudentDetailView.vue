<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getStudent, deleteStudent } from './studentAdminService.js'
import LoadingSpinner from '../../../components/LoadingSpinner.vue'
import ErrorMessage from '../../../components/ErrorMessage.vue'

const route = useRoute()
const router = useRouter()
const student = ref(null)
const loading = ref(true)
const error = ref('')
const deleting = ref(false)
const confirmDelete = ref(false)

onMounted(async () => {
  try {
    student.value = await getStudent(route.params.id)
  } catch (e) {
    error.value = e.response?.data?.message ?? 'Failed to load student.'
  } finally {
    loading.value = false
  }
})

async function handleDelete() {
  deleting.value = true
  try {
    await deleteStudent(route.params.id)
    router.push('/admin/students')
  } catch (e) {
    error.value = e.response?.data?.message ?? 'Failed to delete student.'
    confirmDelete.value = false
  } finally {
    deleting.value = false
  }
}
</script>

<template>
  <div>
    <RouterLink to="/admin/students" class="back-link">← Students</RouterLink>

    <LoadingSpinner v-if="loading" />
    <ErrorMessage :message="error" />

    <template v-if="student">
      <div class="page-header">
        <h1>{{ student.firstName }} {{ student.lastName }}</h1>
        <button class="btn-danger" @click="confirmDelete = true">Delete Student</button>
      </div>

      <dl class="detail-grid">
        <dt>Email</dt><dd>{{ student.email }}</dd>
        <dt>Section</dt><dd>{{ student.sectionName ?? '—' }}</dd>
        <dt>Teams</dt><dd>{{ student.teamNames?.join(', ') || '—' }}</dd>
        <dt>WARs submitted</dt><dd>{{ student.warCount ?? 0 }}</dd>
        <dt>Peer evals submitted</dt><dd>{{ student.peerEvaluationCount ?? 0 }}</dd>
      </dl>
    </template>

    <div v-if="confirmDelete" class="modal-overlay">
      <div class="modal">
        <h2>Delete student?</h2>
        <p>This will permanently delete <strong>{{ student.firstName }} {{ student.lastName }}</strong> along with all their WARs and peer evaluations. This cannot be undone.</p>
        <div class="modal-actions">
          <button class="btn-danger" :disabled="deleting" @click="handleDelete">
            {{ deleting ? 'Deleting…' : 'Yes, Delete' }}
          </button>
          <button @click="confirmDelete = false">Cancel</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.back-link { color: #2c3e50; font-size: 13px; text-decoration: none; display: inline-block; margin-bottom: 20px; }
.back-link:hover { text-decoration: underline; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }
h1 { font-size: 22px; margin: 0; color: #2c3e50; }
.detail-grid { display: grid; grid-template-columns: 160px 1fr; gap: 10px 20px; font-size: 14px; max-width: 500px; }
dt { font-weight: 600; color: #555; }
dd { margin: 0; color: #222; }
.btn-danger { padding: 8px 16px; background: #c0392b; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-danger:disabled { opacity: 0.6; cursor: not-allowed; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.45); display: flex; align-items: center; justify-content: center; z-index: 200; }
.modal { background: white; border-radius: 8px; padding: 28px 32px; max-width: 440px; width: 90%; }
.modal h2 { margin: 0 0 12px; font-size: 18px; }
.modal p { color: #444; font-size: 14px; line-height: 1.6; margin: 0 0 20px; }
.modal-actions { display: flex; gap: 12px; }
.modal-actions button:last-child { padding: 8px 16px; background: #eee; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
</style>
