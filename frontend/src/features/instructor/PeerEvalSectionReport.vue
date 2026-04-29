<template>
  <!-- UC-31: Instructor generates peer eval report for entire section -->
  <div class="report-container">
    <h2>Peer Evaluation Report – Entire Section</h2>

    <div class="report-params">
      <label>Section ID</label>
      <input v-model="sectionId" type="number" placeholder="Enter section ID" />

      <label>Active Week</label>
      <input v-model="activeWeek" type="text" placeholder="e.g. 02-12-2024 to 02-18-2024" />

      <button @click="generateReport" :disabled="loading">
        {{ loading ? 'Generating...' : 'Generate Report' }}
      </button>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <!-- UC-31: No data returned -->
    <p v-if="noData" class="no-data">No peer evaluation data available for this week.</p>

    <!-- Report table -->
    <table v-if="rows.length > 0" class="report-table">
      <thead>
        <tr>
          <th>Student</th>
          <th>Grade</th>
          <th>Public Comments</th>
          <th>Submitted?</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in rows" :key="row.studentName">
          <td>{{ row.studentName }}</td>
          <td>{{ row.averageGrade }} / {{ row.maxGrade }}</td>
          <td>
            <ul>
              <li v-for="(comment, i) in row.publicComments" :key="i">{{ comment }}</li>
            </ul>
          </td>
          <td>{{ row.submittedEval ? 'Yes' : 'No ⚠️' }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import { getSectionPeerEvalReport } from './instructorService'

export default {
  name: 'PeerEvalSectionReport',
  data() {
    return {
      sectionId: '',
      activeWeek: '',
      rows: [],
      loading: false,
      errorMessage: '',
      noData: false
    }
  },
  methods: {
    async generateReport() {
      if (!this.sectionId || !this.activeWeek) {
        this.errorMessage = 'Please enter both a section ID and active week.'
        return
      }
      this.errorMessage = ''
      this.noData = false
      this.rows = []
      this.loading = true
      try {
        const result = await getSectionPeerEvalReport(this.sectionId, this.activeWeek)
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
.report-container { max-width: 900px; margin: 40px auto; padding: 24px; }
.report-params { display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end; margin-bottom: 24px; }
.report-params label { font-weight: 500; }
.report-params input { padding: 8px; border: 1px solid #ccc; border-radius: 4px; }
.report-table { width: 100%; border-collapse: collapse; }
.report-table th, .report-table td { border: 1px solid #ddd; padding: 10px; text-align: left; }
.report-table th { background: #f5f5f5; }
.error { color: red; }
.no-data { color: #888; font-style: italic; }
</style>