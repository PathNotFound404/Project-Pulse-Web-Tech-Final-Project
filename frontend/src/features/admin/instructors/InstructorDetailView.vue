<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getInstructor, deactivateInstructor, reactivateInstructor } from './instructorAdminService.js'
import LoadingSpinner from '../../../components/LoadingSpinner.vue'
import ErrorMessage from '../../../components/ErrorMessage.vue'

const route = useRoute()
const instructor = ref(null)
const loading = ref(true)
const error = ref('')
const actionLoading = ref(false)

onMounted(async () => {
  try {
    instructor.value = await getInstructor(route.params.id)
  } catch (e) {
    error.value = e.response?.data?.message ?? 'Failed to load instructor.'
  } finally {
    loading.value = false
  }
})

async function handleToggle() {
  actionLoading.value = true
  error.value = ''
  try {
    const isActive = instructor.value.status === 'ACTIVE'
    instructor.value = isActive
      ? await deactivateInstructor(route.params.id)
      : await reactivateInstructor(route.params.id)
  } catch (e) {
    error.value = e.response?.data?.message ?? 'Action failed.'
  } finally {
    actionLoading.value = false
  }
}
</script>

<template>
  <div>
    <RouterLink to="/admin/instructors" class="back-link">← Instructors</RouterLink>

    <LoadingSpinner v-if="loading" />
    <ErrorMessage :message="error" />

    <template v-if="instructor">
      <div class="page-header">
        <div>
          <h1>{{ instructor.firstName }} {{ instructor.lastName }}</h1>
          <span :class="['badge', instructor.status === 'ACTIVE' ? 'badge-green' : 'badge-red']">
            {{ instructor.status }}
          </span>
        </div>
        <button
          :class="instructor.status === 'ACTIVE' ? 'btn-danger' : 'btn-primary'"
          :disabled="actionLoading"
          @click="handleToggle"
        >
          {{ actionLoading ? '…' : instructor.status === 'ACTIVE' ? 'Deactivate' : 'Reactivate' }}
        </button>
      </div>

      <dl class="detail-grid">
        <dt>Email</dt><dd>{{ instructor.email }}</dd>
      </dl>

      <div v-if="instructor.supervisedTeamsBySection && Object.keys(instructor.supervisedTeamsBySection).length" class="section-teams">
        <h2>Supervised Teams</h2>
        <div v-for="(teams, section) in instructor.supervisedTeamsBySection" :key="section" class="section-block">
          <h3>{{ section }}</h3>
          <ul>
            <li v-for="team in teams" :key="team">{{ team }}</li>
          </ul>
        </div>
      </div>
      <p v-else class="empty">No teams assigned.</p>
    </template>
  </div>
</template>

<style scoped>
.back-link { color: #2c3e50; font-size: 13px; text-decoration: none; display: inline-block; margin-bottom: 20px; }
.back-link:hover { text-decoration: underline; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 24px; }
h1 { font-size: 22px; margin: 0 0 6px; color: #2c3e50; }
.badge { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
.badge-green { background: #e6f4ea; color: #1e7e34; }
.badge-red { background: #fce8e6; color: #c0392b; }
.detail-grid { display: grid; grid-template-columns: 140px 1fr; gap: 10px 20px; font-size: 14px; max-width: 500px; margin-bottom: 28px; }
dt { font-weight: 600; color: #555; }
dd { margin: 0; }
h2 { font-size: 16px; margin: 0 0 14px; }
h3 { font-size: 14px; color: #555; margin: 12px 0 6px; }
ul { margin: 0 0 8px; padding-left: 20px; font-size: 14px; }
li { margin-bottom: 4px; }
.btn-primary { padding: 8px 18px; background: #2c3e50; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-danger { padding: 8px 18px; background: #c0392b; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-primary:disabled, .btn-danger:disabled { opacity: 0.6; cursor: not-allowed; }
.empty { color: #888; }
</style>
