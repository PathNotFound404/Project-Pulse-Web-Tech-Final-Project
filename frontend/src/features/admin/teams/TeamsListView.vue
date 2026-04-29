<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { searchTeams } from './teamAdminService.js'
import LoadingSpinner from '../../../components/LoadingSpinner.vue'
import ErrorMessage from '../../../components/ErrorMessage.vue'

const router = useRouter()
const filters = ref({ name: '', instructor: '' })
const teams = ref([])
const loading = ref(false)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const params = Object.fromEntries(Object.entries(filters.value).filter(([, v]) => v))
    teams.value = await searchTeams(params)
  } catch (e) {
    error.value = e.response?.data?.message ?? 'Failed to load teams.'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h1>Teams</h1>
      <RouterLink to="/admin/teams/new" class="btn-primary">Create Team</RouterLink>
    </div>

    <form class="filter-form" @submit.prevent="load">
      <input v-model="filters.name" placeholder="Team name" />
      <input v-model="filters.instructor" placeholder="Instructor name" />
      <button type="submit">Search</button>
    </form>

    <ErrorMessage :message="error" />
    <LoadingSpinner v-if="loading" />

    <table v-else-if="teams.length" class="data-table">
      <thead>
        <tr><th>Name</th><th>Section</th><th>Students</th><th>Instructors</th><th></th></tr>
      </thead>
      <tbody>
        <tr
          v-for="t in teams"
          :key="t.id"
          class="clickable-row"
          @click="router.push(`/admin/teams/${t.id}`)"
        >
          <td>{{ t.name }}</td>
          <td>{{ t.sectionName ?? '—' }}</td>
          <td>{{ t.studentNames?.length ?? 0 }}</td>
          <td>{{ t.instructorNames?.join(', ') || '—' }}</td>
          <td class="action-cell">View →</td>
        </tr>
      </tbody>
    </table>

    <p v-else-if="!loading" class="empty">No teams found.</p>
  </div>
</template>

<style scoped>
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
h1 { font-size: 22px; margin: 0; color: #2c3e50; }
.filter-form { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 20px; }
.filter-form input { padding: 7px 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 13px; width: 160px; }
.filter-form button { padding: 7px 18px; background: #2c3e50; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.data-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.data-table th { text-align: left; padding: 10px 12px; border-bottom: 2px solid #ddd; color: #555; font-weight: 600; }
.data-table td { padding: 10px 12px; border-bottom: 1px solid #eee; }
.clickable-row { cursor: pointer; }
.clickable-row:hover td { background: #f7f9fb; }
.action-cell { color: #2c3e50; font-size: 13px; }
.empty { color: #888; margin-top: 20px; }
.btn-primary { padding: 8px 18px; background: #2c3e50; color: white; border-radius: 4px; text-decoration: none; font-size: 13px; }
</style>
