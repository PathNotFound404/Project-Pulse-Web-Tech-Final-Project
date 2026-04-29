<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMyReport } from './peerEvaluationService.js'

const CRITERIA = [
  { key: 'avgQualityOfWork',           label: 'Quality of Work' },
  { key: 'avgProductivity',            label: 'Productivity' },
  { key: 'avgProactiveness',           label: 'Proactiveness' },
  { key: 'avgTreatsOthersWithRespect', label: 'Treats Others with Respect' },
  { key: 'avgHandlesCriticism',        label: 'Handles Criticism' },
  { key: 'avgPerformanceInMeetings',   label: 'Performance in Meetings' },
]

function getMondayOf(date) {
  const d = new Date(date)
  const day = d.getDay()
  const diff = day === 0 ? -6 : 1 - day
  d.setDate(d.getDate() + diff)
  d.setHours(0, 0, 0, 0)
  return d
}

function toISODate(date) {
  return date.toISOString().split('T')[0]
}

function formatDate(isoString) {
  return new Date(isoString + 'T00:00:00').toLocaleDateString('en-US', {
    month: 'short', day: 'numeric', year: 'numeric',
  })
}

function fmt(value) {
  return value != null ? Number(value).toFixed(1) : '—'
}

const thisWeekMonday = getMondayOf(new Date())
const previousWeekMonday = new Date(thisWeekMonday)
previousWeekMonday.setDate(previousWeekMonday.getDate() - 7)

const currentWeekStart = ref(new Date(previousWeekMonday))

const report = ref(null)
const apiError = ref('')

const weekLabel = computed(() => {
  const end = new Date(currentWeekStart.value)
  end.setDate(end.getDate() + 6)
  return `${formatDate(toISODate(currentWeekStart.value))} – ${formatDate(toISODate(end))}`
})

const isNextWeekDisabled = computed(() => {
  const next = new Date(currentWeekStart.value)
  next.setDate(next.getDate() + 7)
  return next >= thisWeekMonday
})

const hasData = computed(() => report.value && report.value.grade != null)

async function loadReport() {
  apiError.value = ''
  report.value = null
  try {
    const result = await getMyReport(toISODate(currentWeekStart.value))
    if (result.flag) {
      report.value = result.data
    } else {
      apiError.value = result.message
    }
  } catch (err) {
    apiError.value = err.response?.data?.message ?? 'Failed to load report.'
  }
}

function prevWeek() {
  const d = new Date(currentWeekStart.value)
  d.setDate(d.getDate() - 7)
  currentWeekStart.value = d
  loadReport()
}

function nextWeek() {
  if (isNextWeekDisabled.value) return
  const d = new Date(currentWeekStart.value)
  d.setDate(d.getDate() + 7)
  currentWeekStart.value = d
  loadReport()
}

onMounted(loadReport)
</script>

<template>
  <div class="report-page">
    <h1>My Peer Evaluation Report</h1>

    <!-- Week navigation -->
    <div class="week-nav">
      <button @click="prevWeek">&#8249; Prev</button>
      <span class="week-label">{{ weekLabel }}</span>
      <button @click="nextWeek" :disabled="isNextWeekDisabled">Next &#8250;</button>
    </div>

    <p v-if="apiError" class="msg error">{{ apiError }}</p>

    <template v-if="report">
      <p class="student-name" v-if="report.firstName">
        {{ report.firstName }} {{ report.lastName }}
      </p>

      <!-- No data for this week -->
      <p v-if="!hasData" class="msg info">
        No peer evaluations have been submitted for this week yet.
      </p>

      <!-- Report table -->
      <template v-if="hasData">
        <table class="report-table">
          <thead>
            <tr>
              <th v-for="c in CRITERIA" :key="c.key">{{ c.label }}</th>
              <th>Grade</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td v-for="c in CRITERIA" :key="c.key" class="score-cell">
                {{ fmt(report[c.key]) }}
              </td>
              <td class="grade-cell">{{ fmt(report.grade) }} / 60</td>
            </tr>
          </tbody>
        </table>

        <!-- Public comments -->
        <div class="comments-section" v-if="report.publicComments && report.publicComments.length > 0">
          <h2>Comments from Teammates</h2>
          <ul class="comment-list">
            <li v-for="(comment, i) in report.publicComments" :key="i">{{ comment }}</li>
          </ul>
        </div>
        <p v-else class="hint">No public comments for this week.</p>
      </template>
    </template>

    <p v-if="!report && !apiError" class="hint">Loading…</p>
  </div>
</template>

<style scoped>
.report-page {
  max-width: 960px;
  margin: 40px auto;
  padding: 0 24px;
  font-family: sans-serif;
}

h1 { margin-bottom: 20px; }

.week-nav {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.week-nav button {
  padding: 6px 14px;
  border: 1px solid #ccc;
  border-radius: 4px;
  cursor: pointer;
  background: #f5f5f5;
}

.week-nav button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.week-label { font-weight: 600; font-size: 15px; }

.student-name {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 20px;
}

.msg {
  padding: 10px 14px;
  border-radius: 4px;
  margin-bottom: 14px;
}
.error { background: #fdecea; color: #c0392b; }
.info  { background: #eaf4fb; color: #2980b9; }

.hint { color: #888; font-size: 14px; margin-top: 8px; }

.report-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 28px;
  font-size: 14px;
}

.report-table th,
.report-table td {
  padding: 10px 12px;
  border: 1px solid #e0e0e0;
  text-align: center;
}

.report-table th {
  background: #f5f5f5;
  font-weight: 600;
  font-size: 13px;
}

.score-cell { color: #2c3e50; }

.grade-cell {
  font-weight: 700;
  font-size: 15px;
  color: #27ae60;
}

.comments-section { margin-top: 4px; }

.comments-section h2 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #2c3e50;
}

.comment-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.comment-list li {
  padding: 10px 14px;
  background: #fafafa;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
}
</style>
