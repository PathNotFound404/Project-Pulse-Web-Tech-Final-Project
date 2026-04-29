<template>
  <!-- UC-33 & UC-34: Instructor views peer eval or WAR report for a specific student -->
  <div class="report-container">
    <h2>Student Reports</h2>

    <!-- Report type toggle -->
    <div class="toggle">
      <button :class="{ active: reportType === 'peereval' }" @click="reportType = 'peereval'">
        Peer Evaluation (UC-33)
      </button>
      <button :class="{ active: reportType === 'war' }" @click="reportType = 'war'">
        WAR Report (UC-34)
      </button>
    </div>

    <div class="report-params">
      <label>Student ID</label>
      <input v-model="studentId" type="number" placeholder="Enter student ID" />

      <label>Start Week</label>
      <input v-model="startWeek" type="text" placeholder="e.g. 02-12-2024" />

      <label>End Week</label>
      <input v-model="endWeek" type="text" placeholder="e.g. 04-28-2024" />

      <button @click="generateReport" :disabled="loading">
        {{ loading ? 'Generating...' : 'Generate Report' }}
      </button>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    <p v-if="noData" class="no-data">No data available for this student in the selected period.</p>

    <!-- UC-33: Peer eval table -->
    <table v-if="reportType === 'peereval' && rows.length > 0" class="report-table">
      <thead>
        <tr>
          <th>Week</th>
          <th>Grade</th>
          <th>Public Comments</th>
          <th>Submitted?</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(row, i) in rows" :key="i">
          <td>{{ row.week || row.studentName }}</td>
          <td>{{ row.averageGrade }} / {{ row.maxGrade }}</td>
          <td>
            <ul>
              <li v-for="(c, j) in row.publicComments" :key="j">{{ c }}</li>
            </ul>
          </td>
          <td>{{ row.submittedEval ? 'Yes' : 'No ⚠️' }}</td>
        </tr>
      </tbody>
    </table>

    <!-- UC-34: WAR table -->
    <table v-if="reportType === 'war' && rows.length > 0" class="report-table">
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
          <td>{{ row.weekStart && row.weekEnd ? row.weekStart + ' to ' + row.weekEnd : '-' }}</td>
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

<script>
import { getStudentPeerEvalReport, getStudentWarReport } from './instructorService'

export default {
  name: 'StudentReports',
  data() {
    return {
      reportType: 'peereval',
      studentId: '',
      startWeek: '',
      endWeek: '',
      rows: [],
      loading: false,
      errorMessage: '',
      noData: false
    }
  },
  methods: {
    async generateReport() {
      if (!this.studentId || !this.startWeek || !this.endWeek) {
        this.errorMessage = 'Please fill in all fields.'
        return
      }
      this.errorMessage = ''
      this.noData = false
      this.rows = []
      this.loading = true
      try {
        let result
        if (this.reportType === 'peereval') {
          // UC-33
          result = await getStudentPeerEvalReport(this.studentId, this.startWeek, this.endWeek)
        } else {
          // UC-34
          result = await getStudentWarReport(this.studentId, this.startWeek, this.endWeek)
        }
        if (!result.data || result.data.length === 0) {
          this.noData = true
        } else {
          this.rows = result.data
        }
      } catch (err) {
        this.errorMessage = err.response?.data?.message || 'Failed to generate report.'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.report-container { max-width: 1000px; margin: 40px auto; padding: 24px; }
.toggle { display: flex; gap: 8px; margin-bottom: 20px; }
.toggle button { padding: 8px 16px; border: 1px solid #ccc; border-radius: 4px; cursor: pointer; background: white; }
.toggle button.active { background: #4a90d9; color: white; border-color: #4a90d9; }
.report-params { display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end; margin-bottom: 24px; }
.report-params label { font-weight: 500; }
.report-params input { padding: 8px; border: 1px solid #ccc; border-radius: 4px; }
.report-table { width: 100%; border-collapse: collapse; }
.report-table th, .report-table td { border: 1px solid #ddd; padding: 10px; text-align: left; }
.report-table th { background: #f5f5f5; }
.error { color: red; }
.no-data { color: #888; font-style: italic; }
</style>