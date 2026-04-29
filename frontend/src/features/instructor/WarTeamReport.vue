<script setup>
import { ref, watch, onMounted } from 'vue'
import { getTeamWarReport } from './InstructorService.js'
import { searchTeams } from '../admin/teams/teamAdminService.js'
import LoadingSpinner from '../../components/LoadingSpinner.vue'
import ErrorMessage from '../../components/ErrorMessage.vue'

const teams = ref([])
const selectedTeamId = ref('')
const weekDate = ref('')
const rows = ref([])
const loading = ref(false)
const error = ref('')
const noData = ref(false)

function getCurrentMonday() {
  const today = new Date()
  const day = today.getDay()
  const diff = day === 0 ? -6 : 1 - day
  const monday = new Date(today)
  monday.setDate(today.getDate() + diff)
  return monday.toISOString().split('T')[0]
}

function toApiDate(isoDate) {
  const [y, m, d] = isoDate.split('-')
  return `${m}-${d}-${y}`
}

async function loadReport() {
  if (!selectedTeamId.value || !weekDate.value) return
  loading.value = true
  error.value = ''
  noData.value = false
  rows.value = []
  try {
    const result = await getTeamWarReport(selectedTeamId.value, toApiDate(weekDate.value))
    rows.value = result.data || []
    noData.value = rows.value.length === 0
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load report.'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    teams.value = await searchTeams({})
    weekDate.value = getCurrentMonday()
    if (teams.value.length) {
      selectedTeamId.value = teams.value[0].id
    }
    await loadReport()
  } catch {
    error.value = 'Failed to load teams.'
  }
})

watch([selectedTeamId, weekDate], loadReport)
</script>

<template>
  <div>
    <h1>WAR Report — Team</h1>

    <div class="filters">
      <div class="filter-field">
        <label>Team</label>
        <select v-model="selectedTeamId">
          <option value="">— Select a team —</option>
          <option v-for="t in teams" :key="t.id" :value="t.id">
            {{ t.name }}{{ t.sectionName ? ` (${t.sectionName})` : '' }}
          </option>
        </select>
      </div>

      <div class="filter-field">
        <label>Week Start Date</label>
        <input v-model="weekDate" type="date" />
      </div>
    </div>

    <ErrorMessage :message="error" />
    <LoadingSpinner v-if="loading" />

    <p v-if="noData" class="no-data">No WAR data available for this team and week.</p>

    <table v-if="rows.length" class="report-table">
      <thead>
        <tr>
          <th>Student</th>
          <th>Category</th>
          <th>Activity</th>
          <th>Description</th>
          <th>Planned Hrs</th>
          <th>Actual Hrs</th>
          <th>Status</th>
          <th>Submitted</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(row, i) in rows" :key="i" :class="{ 'not-submitted': !row.submittedWar }">
          <td>{{ row.studentName }}</td>
          <td>{{ row.activityCategory }}</td>
          <td>{{ row.plannedActivity }}</td>
          <td>{{ row.description }}</td>
          <td>{{ row.plannedHours }}</td>
          <td>{{ row.actualHours }}</td>
          <td>{{ row.status }}</td>
          <td>
            <span :class="row.submittedWar ? 'badge-yes' : 'badge-no'">
              {{ row.submittedWar ? 'Yes' : 'No' }}
            </span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
h1 { font-size: 22px; margin: 0 0 24px; color: #2c3e50; }
.filters { display: flex; flex-wrap: wrap; gap: 16px; align-items: flex-end; margin-bottom: 24px; }
.filter-field { display: flex; flex-direction: column; gap: 5px; min-width: 220px; }
.filter-field label { font-size: 13px; font-weight: 600; color: #555; }
.filter-field select, .filter-field input { padding: 8px 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; min-width: 220px; }
.report-table { width: 100%; border-collapse: collapse; font-size: 14px; margin-top: 8px; }
.report-table th { text-align: left; padding: 10px 12px; border-bottom: 2px solid #ddd; background: #f8f8f8; font-weight: 600; color: #555; }
.report-table td { padding: 10px 12px; border-bottom: 1px solid #eee; vertical-align: top; }
.not-submitted td { background: #fff9f9; }
.badge-yes { display: inline-block; padding: 2px 8px; background: #e6f4ea; color: #1e7e34; border-radius: 12px; font-size: 12px; font-weight: 600; }
.badge-no { display: inline-block; padding: 2px 8px; background: #fce8e6; color: #c0392b; border-radius: 12px; font-size: 12px; font-weight: 600; }
.no-data { color: #888; font-style: italic; }
</style>
