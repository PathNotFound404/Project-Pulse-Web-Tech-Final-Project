<script setup>
import { ref, watch, onMounted } from 'vue'
import { getSectionPeerEvalReport } from './InstructorService.js'
import { searchSections, getActiveWeeks } from '../admin/sections/sectionAdminService.js'
import LoadingSpinner from '../../components/LoadingSpinner.vue'
import ErrorMessage from '../../components/ErrorMessage.vue'

const sections = ref([])
const activeWeeks = ref([])
const selectedSectionId = ref('')
const selectedWeek = ref('')
const rows = ref([])
const loading = ref(false)
const weeksLoading = ref(false)
const error = ref('')
const noData = ref(false)

function weekValue(week) {
  const [y, m, d] = week.startDate.split('-')
  return `${m}-${d}-${y}`
}

function formatWeekLabel(week) {
  return `${week.startDate} → ${week.endDate}`
}

async function loadWeeks(id) {
  weeksLoading.value = true
  activeWeeks.value = []
  try {
    activeWeeks.value = await getActiveWeeks(id)
  } catch {
    error.value = 'Failed to load active weeks for this section.'
  } finally {
    weeksLoading.value = false
  }
}

async function loadReport() {
  if (!selectedSectionId.value || !selectedWeek.value) return
  loading.value = true
  error.value = ''
  noData.value = false
  rows.value = []
  try {
    const result = await getSectionPeerEvalReport(selectedSectionId.value, selectedWeek.value)
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
    sections.value = await searchSections({})
    if (sections.value.length) {
      selectedSectionId.value = sections.value[0].id
    }
  } catch {
    error.value = 'Failed to load sections.'
  }
})

watch(selectedSectionId, async (id) => {
  selectedWeek.value = ''
  rows.value = []
  noData.value = false
  if (!id) return
  await loadWeeks(id)
  if (activeWeeks.value.length) {
    selectedWeek.value = weekValue(activeWeeks.value[0])
  }
})

watch(selectedWeek, async (week) => {
  if (!week || !selectedSectionId.value) {
    rows.value = []
    noData.value = false
    return
  }
  await loadReport()
})
</script>

<template>
  <div>
    <h1>Peer Evaluation Report — Section</h1>

    <div class="filters">
      <div class="filter-field">
        <label>Section</label>
        <select v-model="selectedSectionId">
          <option value="">— Select a section —</option>
          <option v-for="s in sections" :key="s.id" :value="s.id">{{ s.name }}</option>
        </select>
      </div>

      <div class="filter-field">
        <label>Active Week</label>
        <select v-model="selectedWeek" :disabled="!selectedSectionId || weeksLoading">
          <option value="">
            {{ weeksLoading ? 'Loading weeks…' : activeWeeks.length ? '— Select a week —' : '— No active weeks —' }}
          </option>
          <option v-for="w in activeWeeks" :key="w.id" :value="weekValue(w)">
            {{ formatWeekLabel(w) }}
          </option>
        </select>
      </div>
    </div>

    <ErrorMessage :message="error" />
    <LoadingSpinner v-if="loading" />

    <p v-if="noData" class="no-data">No peer evaluation data available for this week.</p>

    <table v-if="rows.length" class="report-table">
      <thead>
        <tr>
          <th>Student</th>
          <th>Average Grade</th>
          <th>Max Grade</th>
          <th>Public Comments</th>
          <th>Submitted</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in rows" :key="row.studentName" :class="{ 'not-submitted': !row.submittedEval }">
          <td>{{ row.studentName }}</td>
          <td>{{ row.averageGrade }}</td>
          <td>{{ row.maxGrade }}</td>
          <td>
            <ul v-if="row.publicComments?.length">
              <li v-for="(c, i) in row.publicComments" :key="i">{{ c }}</li>
            </ul>
            <span v-else class="muted">—</span>
          </td>
          <td>
            <span :class="row.submittedEval ? 'badge-yes' : 'badge-no'">
              {{ row.submittedEval ? 'Yes' : 'No' }}
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
.filter-field select { padding: 8px 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; min-width: 220px; }
.filter-field select:disabled { background: #f5f5f5; color: #999; }
.report-table { width: 100%; border-collapse: collapse; font-size: 14px; margin-top: 8px; }
.report-table th { text-align: left; padding: 10px 12px; border-bottom: 2px solid #ddd; background: #f8f8f8; font-weight: 600; color: #555; }
.report-table td { padding: 10px 12px; border-bottom: 1px solid #eee; vertical-align: top; }
.not-submitted td { background: #fff9f9; }
ul { margin: 0; padding-left: 16px; }
li { margin-bottom: 2px; }
.muted { color: #aaa; }
.badge-yes { display: inline-block; padding: 2px 8px; background: #e6f4ea; color: #1e7e34; border-radius: 12px; font-size: 12px; font-weight: 600; }
.badge-no { display: inline-block; padding: 2px 8px; background: #fce8e6; color: #c0392b; border-radius: 12px; font-size: 12px; font-weight: 600; }
.no-data { color: #888; font-style: italic; }
</style>
