<template>
  <!-- UC-32: Instructor or Student generates WAR report for a team -->
  <div class="report-container">
    <h2>WAR Report – Team</h2>

    <div class="report-params">
      <label>Team ID</label>
      <input v-model="teamId" type="number" placeholder="Enter team ID" />

      <label>Active Week</label>
      <input v-model="activeWeek" type="text" placeholder="e.g. 02-12-2024 to 02-18-2024" />

      <button @click="generateReport" :disabled="loading">
        {{ loading ? 'Generating...' : 'Generate Report' }}
      </button>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    <p v-if="noData" class="no-data">No WAR data available for this week.</p>

    <table v-if="rows.length > 0" class="report-table">
      <thead>
        <tr>
          <th>Student</th>
          <th>Category</th>
          <th>Activity</th>
          <th>Description</th>
          <th>Planned Hrs</th>
          <th>Actual Hrs</th>
          <th>Status</th>
          <th>Submitted?</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(row, i) in rows" :key="i">
          <td>{{ row.studentName }}</td>
          <td>{{ row.activityCategory }}</td>
          <td>{{ row.plannedActivity }}</td>
          <td>{{ row.description }}</td>
          <td>{{ row.plannedHours }}</td>
          <td>{{ row.actualHours }}</td>
          <td>{{ row.status }}</td>
          <td>{{ row.submittedWar ? 'Yes' : 'No ⚠️' }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import { getTeamWarReport } from './instructorService'

export default {
  name: 'WarTeamReport',
  data() {
    return {
      teamId: '',
      activeWeek: '',
      rows: [],
      loading: false,
      errorMessage: '',
      noData: false
    }
  },
  methods: {
    async generateReport() {
      if (!this.teamId || !this.activeWeek) {
        this.errorMessage = 'Please enter both a team ID and active week.'
        return
      }
      this.errorMessage = ''
      this.noData = false
      this.rows = []
      this.loading = true
      try {
        const result = await getTeamWarReport(this.teamId, this.activeWeek)
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
.report-container { max-width: 1100px; margin: 40px auto; padding: 24px; }
.report-params { display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end; margin-bottom: 24px; }
.report-params label { font-weight: 500; }
.report-params input { padding: 8px; border: 1px solid #ccc; border-radius: 4px; }
.report-table { width: 100%; border-collapse: collapse; }
.report-table th, .report-table td { border: 1px solid #ddd; padding: 10px; text-align: left; }
.report-table th { background: #f5f5f5; }
.error { color: red; }
.no-data { color: #888; font-style: italic; }
</style>