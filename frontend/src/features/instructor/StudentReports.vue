<script setup>
import { ref, watch, onMounted } from 'vue'
import { getStudentPeerEvalReport, getStudentWarReport } from './InstructorService.js'
import { searchStudents } from '../admin/students/studentAdminService.js'
import LoadingSpinner from '../../components/LoadingSpinner.vue'
import ErrorMessage from '../../components/ErrorMessage.vue'

const students = ref([])
const selectedStudentId = ref('')
const reportType = ref('peereval')
const startDate = ref('')
const endDate = ref('')
const rows = ref([])
const loading = ref(false)
const error = ref('')
const noData = ref(false)
const expandedRow = ref(null)

function toApiDate(isoDate) {
  const [y, m, d] = isoDate.split('-')
  return `${m}-${d}-${y}`
}

function isoToday() {
  return new Date().toISOString().split('T')[0]
}

function isoMonthsAgo(n) {
  const d = new Date()
  d.setMonth(d.getMonth() - n)
  return d.toISOString().split('T')[0]
}

function toggleRow(i) {
  expandedRow.value = expandedRow.value === i ? null : i
}

async function loadReport() {
  if (!selectedStudentId.value || !startDate.value || !endDate.value) return
  loading.value = true
  error.value = ''
  noData.value = false
  rows.value = []
  expandedRow.value = null
  try {
    const result = reportType.value === 'peereval'
      ? await getStudentPeerEvalReport(selectedStudentId.value, toApiDate(startDate.value), toApiDate(endDate.value))
      : await getStudentWarReport(selectedStudentId.value, toApiDate(startDate.value), toApiDate(endDate.value))
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
    students.value = await searchStudents({})
    startDate.value = isoMonthsAgo(4)
    endDate.value = isoToday()
    if (students.value.length) {
      selectedStudentId.value = students.value[0].id
    }
    await loadReport()
  } catch {
    error.value = 'Failed to load students.'
  }
})

watch([selectedStudentId, startDate, endDate, reportType], loadReport)
</script>

<template>
  <div>
    <h1>Student Reports</h1>

    <div class="toggle">
      <button :class="{ active: reportType === 'peereval' }" @click="reportType = 'peereval'">
        Peer Evaluation
      </button>
      <button :class="{ active: reportType === 'war' }" @click="reportType = 'war'">
        WAR Report
      </button>
    </div>

    <div class="filters">
      <div class="filter-field">
        <label>Student</label>
        <select v-model="selectedStudentId">
          <option value="">— Select a student —</option>
          <option v-for="s in students" :key="s.id" :value="s.id">
            {{ s.firstName }} {{ s.lastName }}
          </option>
        </select>
      </div>

      <div class="filter-field">
        <label>From</label>
        <input v-model="startDate" type="date" />
      </div>

      <div class="filter-field">
        <label>To</label>
        <input v-model="endDate" type="date" />
      </div>
    </div>

    <ErrorMessage :message="error" />
    <LoadingSpinner v-if="loading" />

    <p v-if="noData" class="no-data">No data available for this student in the selected period.</p>

    <!-- Peer eval table -->
    <template v-if="reportType === 'peereval' && rows.length">
      <p class="hint">Click a row to see individual evaluations and private comments.</p>
      <table class="report-table">
        <thead>
          <tr>
            <th></th>
            <th>Week</th>
            <th>Avg Score</th>
            <th>Max Score</th>
            <th>Public Comments</th>
            <th>Submitted</th>
          </tr>
        </thead>
        <tbody>
          <template v-for="(row, i) in rows" :key="i">
            <tr class="summary-row" @click="toggleRow(i)">
              <td class="chevron">{{ expandedRow === i ? '▾' : '▸' }}</td>
              <td>{{ row.week || '—' }}</td>
              <td>{{ row.averageGrade }}</td>
              <td>{{ row.maxGrade }}</td>
              <td>
                <ul v-if="row.publicComments?.length">
                  <li v-for="(c, j) in row.publicComments" :key="j">{{ c }}</li>
                </ul>
                <span v-else class="muted">—</span>
              </td>
              <td>
                <span :class="row.submittedEval ? 'badge-yes' : 'badge-no'">
                  {{ row.submittedEval ? 'Yes' : 'No' }}
                </span>
              </td>
            </tr>

            <!-- Expanded detail panel -->
            <tr v-if="expandedRow === i" class="detail-row">
              <td colspan="6">
                <div class="detail-panel">
                  <h4>Individual Evaluations</h4>
                  <table class="detail-table">
                    <thead>
                      <tr>
                        <th>Evaluator</th>
                        <th>Courtesy</th>
                        <th>Engagement</th>
                        <th>Initiative</th>
                        <th>Open-mindedness</th>
                        <th>Productivity</th>
                        <th>Quality</th>
                        <th>Total</th>
                        <th>Public Comment</th>
                        <th>Private Comment</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(ev, j) in row.evaluations" :key="j">
                        <td>{{ ev.evaluatorName }}</td>
                        <td>{{ ev.courtesy ?? '—' }}</td>
                        <td>{{ ev.engagementInMeetings ?? '—' }}</td>
                        <td>{{ ev.initiative ?? '—' }}</td>
                        <td>{{ ev.openMindedness ?? '—' }}</td>
                        <td>{{ ev.productivity ?? '—' }}</td>
                        <td>{{ ev.qualityOfWork ?? '—' }}</td>
                        <td><strong>{{ ev.totalScore }}</strong></td>
                        <td>{{ ev.publicComments || '—' }}</td>
                        <td class="private-comment">{{ ev.privateComments || '—' }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </template>

    <!-- WAR table -->
    <table v-if="reportType === 'war' && rows.length" class="report-table">
      <thead>
        <tr>
          <th>Week</th>
          <th>Category</th>
          <th>Activity</th>
          <th>Description</th>
          <th>Planned Hrs</th>
          <th>Actual Hrs</th>
          <th>Status</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(row, i) in rows" :key="i">
          <td>{{ row.weekStart && row.weekEnd ? `${row.weekStart} → ${row.weekEnd}` : '—' }}</td>
          <td>{{ row.activityCategory }}</td>
          <td>{{ row.plannedActivity }}</td>
          <td>{{ row.description }}</td>
          <td>{{ row.plannedHours }}</td>
          <td>{{ row.actualHours }}</td>
          <td>{{ row.status }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
h1 { font-size: 22px; margin: 0 0 20px; color: #2c3e50; }
.toggle { display: flex; gap: 8px; margin-bottom: 20px; }
.toggle button { padding: 8px 16px; border: 1px solid #ccc; border-radius: 4px; cursor: pointer; background: white; font-size: 13px; }
.toggle button.active { background: #2c3e50; color: white; border-color: #2c3e50; }
.filters { display: flex; flex-wrap: wrap; gap: 16px; align-items: flex-end; margin-bottom: 24px; }
.filter-field { display: flex; flex-direction: column; gap: 5px; min-width: 200px; }
.filter-field label { font-size: 13px; font-weight: 600; color: #555; }
.filter-field select, .filter-field input { padding: 8px 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; }
.hint { font-size: 12px; color: #888; margin-bottom: 8px; }
.report-table { width: 100%; border-collapse: collapse; font-size: 14px; margin-top: 4px; }
.report-table th { text-align: left; padding: 10px 12px; border-bottom: 2px solid #ddd; background: #f8f8f8; font-weight: 600; color: #555; }
.report-table td { padding: 10px 12px; border-bottom: 1px solid #eee; vertical-align: top; }
.summary-row { cursor: pointer; }
.summary-row:hover td { background: #f5f8fb; }
.chevron { color: #888; font-size: 12px; width: 20px; }
.detail-row td { padding: 0; background: #f9fbfd; border-bottom: 2px solid #ddd; }
.detail-panel { padding: 16px 20px; }
h4 { font-size: 13px; font-weight: 600; color: #2c3e50; margin: 0 0 10px; }
.detail-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.detail-table th { text-align: left; padding: 7px 10px; border-bottom: 1px solid #ddd; background: #eef2f7; font-weight: 600; color: #555; white-space: nowrap; }
.detail-table td { padding: 7px 10px; border-bottom: 1px solid #eee; vertical-align: top; }
.private-comment { color: #8b5e3c; font-style: italic; }
ul { margin: 0; padding-left: 16px; }
li { margin-bottom: 2px; }
.muted { color: #aaa; }
.badge-yes { display: inline-block; padding: 2px 8px; background: #e6f4ea; color: #1e7e34; border-radius: 12px; font-size: 12px; font-weight: 600; }
.badge-no { display: inline-block; padding: 2px 8px; background: #fce8e6; color: #c0392b; border-radius: 12px; font-size: 12px; font-weight: 600; }
.no-data { color: #888; font-style: italic; }
</style>
