<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { searchInstructors } from './instructorAdminService.js'
import LoadingSpinner from '../../../components/LoadingSpinner.vue'
import ErrorMessage from '../../../components/ErrorMessage.vue'

const router = useRouter()
const filters = ref({ firstName: '', lastName: '', teamName: '', status: '' })
const instructors = ref([])
const loading = ref(false)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const params = Object.fromEntries(Object.entries(filters.value).filter(([, v]) => v))
    instructors.value = await searchInstructors(params)
  } catch (e) {
    error.value = e.response?.data?.message ?? 'Failed to load instructors.'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h1>Instructors</h1>
      <RouterLink to="/admin/instructors/invite" class="btn-primary">Invite Instructors</RouterLink>
    </div>

    <form class="filter-form" @submit.prevent="load">
      <input v-model="filters.firstName" placeholder="First name" />
      <input v-model="filters.lastName" placeholder="Last name" />
      <input v-model="filters.teamName" placeholder="Team" />
      <select v-model="filters.status">
        <option value="">All statuses</option>
        <option value="ACTIVE">Active</option>
        <option value="DEACTIVATED">Deactivated</option>
      </select>
      <button type="submit">Search</button>
    </form>

    <ErrorMessage :message="error" />
    <LoadingSpinner v-if="loading" />

    <table v-else-if="instructors.length" class="data-table">
      <thead>
        <tr><th>Name</th><th>Teams</th><th>Status</th><th></th></tr>
      </thead>
      <tbody>
        <tr
          v-for="i in instructors"
          :key="i.id"
          class="clickable-row"
          @click="router.push(`/admin/instructors/${i.id}`)"
        >
          <td>{{ i.firstName }} {{ i.lastName }}</td>
          <td>{{ i.teamNames?.join(', ') || '—' }}</td>
          <td>
            <span :class="['badge', i.status === 'ACTIVE' ? 'badge-green' : 'badge-red']">
              {{ i.status }}
            </span>
          </td>
          <td class="action-cell">View →</td>
        </tr>
      </tbody>
    </table>

    <p v-else-if="!loading" class="empty">No instructors found.</p>
  </div>
</template>

<style scoped>
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
h1 { font-size: 22px; margin: 0; color: #2c3e50; }
.filter-form { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 20px; }
.filter-form input, .filter-form select { padding: 7px 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 13px; min-width: 130px; }
.filter-form button { padding: 7px 18px; background: #2c3e50; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.data-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.data-table th { text-align: left; padding: 10px 12px; border-bottom: 2px solid #ddd; color: #555; font-weight: 600; }
.data-table td { padding: 10px 12px; border-bottom: 1px solid #eee; }
.clickable-row { cursor: pointer; }
.clickable-row:hover td { background: #f7f9fb; }
.action-cell { color: #2c3e50; font-size: 13px; }
.badge { display: inline-block; padding: 2px 8px; border-radius: 12px; font-size: 12px; font-weight: 600; }
.badge-green { background: #e6f4ea; color: #1e7e34; }
.badge-red { background: #fce8e6; color: #c0392b; }
.empty { color: #888; margin-top: 20px; }
.btn-primary { padding: 8px 18px; background: #2c3e50; color: white; border-radius: 4px; text-decoration: none; font-size: 13px; }
</style>
