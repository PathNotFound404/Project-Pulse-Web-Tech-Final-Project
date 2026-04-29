<script setup>
import { ref, computed, onMounted } from 'vue'
import { getSheet, submitEvaluations } from './peerEvaluationService.js'

const CRITERIA = [
  { key: 'qualityOfWork',           label: 'Quality of Work',           description: 'How do you rate the quality of this teammate\'s work? (1–10)' },
  { key: 'productivity',            label: 'Productivity',              description: 'How productive is this teammate? (1–10)' },
  { key: 'proactiveness',           label: 'Proactiveness',             description: 'How proactive is this teammate? (1–10)' },
  { key: 'treatsOthersWithRespect', label: 'Treats Others with Respect', description: 'Does this teammate treat others with respect? (1–10)' },
  { key: 'handlesCriticism',        label: 'Handles Criticism',         description: 'How well does this teammate handle criticism of their work? (1–10)' },
  { key: 'performanceInMeetings',   label: 'Performance in Meetings',   description: 'How is this teammate\'s performance during meetings? (1–10)' },
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

const thisWeekMonday = getMondayOf(new Date())
const previousWeekMonday = new Date(thisWeekMonday)
previousWeekMonday.setDate(previousWeekMonday.getDate() - 7)

const currentWeekStart = ref(new Date(previousWeekMonday))

const sheet = ref(null)
const apiError = ref('')
const successMessage = ref('')
const mode = ref('edit') // 'edit' | 'review'

// form state: map of evaluateeId → scores + comments
const formEntries = ref({})
const formError = ref('')

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

async function loadSheet() {
  apiError.value = ''
  successMessage.value = ''
  mode.value = 'edit'
  formError.value = ''
  try {
    const result = await getSheet(toISODate(currentWeekStart.value))
    if (result.flag) {
      sheet.value = result.data
      initFormEntries(result.data.entries)
    } else {
      apiError.value = result.message
    }
  } catch (err) {
    apiError.value = err.response?.data?.message ?? 'Failed to load evaluation sheet.'
  }
}

function initFormEntries(entries) {
  const map = {}
  for (const entry of entries) {
    map[entry.evaluateeId] = {
      qualityOfWork:           entry.qualityOfWork ?? '',
      productivity:            entry.productivity ?? '',
      proactiveness:           entry.proactiveness ?? '',
      treatsOthersWithRespect: entry.treatsOthersWithRespect ?? '',
      handlesCriticism:        entry.handlesCriticism ?? '',
      performanceInMeetings:   entry.performanceInMeetings ?? '',
      publicComment:           entry.publicComment ?? '',
      privateComment:          '',
    }
  }
  formEntries.value = map
}

function prevWeek() {
  const d = new Date(currentWeekStart.value)
  d.setDate(d.getDate() - 7)
  currentWeekStart.value = d
  loadSheet()
}

function nextWeek() {
  if (isNextWeekDisabled.value) return
  const d = new Date(currentWeekStart.value)
  d.setDate(d.getDate() + 7)
  currentWeekStart.value = d
  loadSheet()
}

function validateForm() {
  if (!sheet.value) return false
  for (const entry of sheet.value.entries) {
    const f = formEntries.value[entry.evaluateeId]
    for (const c of CRITERIA) {
      const val = Number(f[c.key])
      if (!f[c.key] && f[c.key] !== 0) {
        formError.value = `Please fill in all scores for ${entry.firstName} ${entry.lastName}.`
        return false
      }
      if (!Number.isInteger(val) || val < 1 || val > 10) {
        formError.value = `${c.label} score for ${entry.firstName} ${entry.lastName} must be an integer between 1 and 10.`
        return false
      }
    }
  }
  return true
}

function goToReview() {
  formError.value = ''
  if (validateForm()) {
    mode.value = 'review'
  }
}

function backToEdit() {
  mode.value = 'edit'
  formError.value = ''
}

function cancel() {
  mode.value = 'edit'
  formError.value = ''
  successMessage.value = ''
  if (sheet.value) {
    initFormEntries(sheet.value.entries)
  }
}

async function confirmSubmit() {
  formError.value = ''
  const entries = sheet.value.entries.map(entry => {
    const f = formEntries.value[entry.evaluateeId]
    return {
      evaluateeId:              entry.evaluateeId,
      qualityOfWork:            Number(f.qualityOfWork),
      productivity:             Number(f.productivity),
      proactiveness:            Number(f.proactiveness),
      treatsOthersWithRespect:  Number(f.treatsOthersWithRespect),
      handlesCriticism:         Number(f.handlesCriticism),
      performanceInMeetings:    Number(f.performanceInMeetings),
      publicComment:            f.publicComment,
      privateComment:           f.privateComment,
    }
  })

  try {
    const result = await submitEvaluations({
      week: toISODate(currentWeekStart.value),
      entries,
    })
    if (result.flag) {
      successMessage.value = 'Peer evaluation submitted successfully.'
      sheet.value = result.data
      initFormEntries(result.data.entries)
      mode.value = 'edit'
    } else {
      formError.value = result.message
    }
  } catch (err) {
    formError.value = err.response?.data?.message ?? 'Submission failed.'
  }
}

onMounted(loadSheet)
</script>

<template>
  <div class="pe-page">
    <h1>Peer Evaluation</h1>

    <!-- Week navigation -->
    <div class="week-nav">
      <button @click="prevWeek">&#8249; Prev</button>
      <span class="week-label">{{ weekLabel }}</span>
      <button @click="nextWeek" :disabled="isNextWeekDisabled">Next &#8250;</button>
    </div>

    <p v-if="apiError" class="msg error">{{ apiError }}</p>
    <p v-if="successMessage" class="msg success">{{ successMessage }}</p>

    <!-- Edit mode -->
    <template v-if="sheet && mode === 'edit'">
      <p class="hint">Rate each teammate (including yourself) on all 6 criteria.</p>

      <div v-for="entry in sheet.entries" :key="entry.evaluateeId" class="teammate-card">
        <h2 class="teammate-name">{{ entry.firstName }} {{ entry.lastName }}</h2>

        <div class="criteria-grid">
          <div v-for="c in CRITERIA" :key="c.key" class="criterion">
            <label :title="c.description">{{ c.label }}</label>
            <input
              type="number"
              v-model="formEntries[entry.evaluateeId][c.key]"
              min="1"
              max="10"
              step="1"
              placeholder="1–10"
            />
          </div>
        </div>

        <div class="comment-row">
          <div class="field">
            <label>Comments for teammate</label>
            <textarea
              v-model="formEntries[entry.evaluateeId].publicComment"
              rows="2"
              placeholder="Optional public comments…"
            />
          </div>
          <div class="field">
            <label>Private comments for instructor only</label>
            <textarea
              v-model="formEntries[entry.evaluateeId].privateComment"
              rows="2"
              placeholder="Optional private comments…"
            />
          </div>
        </div>
      </div>

      <p v-if="formError" class="msg error">{{ formError }}</p>

      <div class="form-actions">
        <button class="btn-primary" @click="goToReview">Review & Confirm</button>
        <button class="btn-cancel" @click="cancel">Cancel</button>
      </div>
    </template>

    <!-- Review mode -->
    <template v-if="sheet && mode === 'review'">
      <p class="hint">Review your evaluations before submitting.</p>

      <table class="review-table">
        <thead>
          <tr>
            <th>Teammate</th>
            <th v-for="c in CRITERIA" :key="c.key">{{ c.label }}</th>
            <th>Public Comment</th>
            <th>Private Comment</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="entry in sheet.entries" :key="entry.evaluateeId">
            <td class="name-cell">{{ entry.firstName }} {{ entry.lastName }}</td>
            <td v-for="c in CRITERIA" :key="c.key" class="score-cell">
              {{ formEntries[entry.evaluateeId][c.key] }}
            </td>
            <td class="comment-cell">{{ formEntries[entry.evaluateeId].publicComment || '—' }}</td>
            <td class="comment-cell private-tag">{{ formEntries[entry.evaluateeId].privateComment || '—' }}</td>
          </tr>
        </tbody>
      </table>

      <p v-if="formError" class="msg error">{{ formError }}</p>

      <div class="form-actions">
        <button class="btn-primary" @click="confirmSubmit">Submit</button>
        <button class="btn-cancel" @click="backToEdit">Back to Edit</button>
      </div>
    </template>

    <p v-if="!sheet && !apiError" class="empty-msg">Loading…</p>
  </div>
</template>

<style scoped>
.pe-page {
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

.hint { color: #555; margin-bottom: 20px; font-size: 14px; }

.msg {
  padding: 10px 14px;
  border-radius: 4px;
  margin-bottom: 14px;
}
.error  { background: #fdecea; color: #c0392b; }
.success { background: #eafaf1; color: #27ae60; }

.teammate-card {
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 16px 20px;
  margin-bottom: 20px;
  background: #fafafa;
}

.teammate-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 14px;
  color: #2c3e50;
}

.criteria-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px 20px;
  margin-bottom: 14px;
}

.criterion {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.criterion label {
  font-size: 12px;
  font-weight: 500;
  color: #444;
  cursor: help;
}

.criterion input {
  padding: 6px 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
  width: 70px;
}

.comment-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.field label {
  font-size: 12px;
  font-weight: 500;
  color: #444;
}

textarea {
  padding: 6px 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 13px;
  font-family: inherit;
  resize: vertical;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 8px;
}

.btn-primary {
  padding: 9px 22px;
  background: #2c3e50;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-cancel {
  padding: 9px 22px;
  background: #ecf0f1;
  color: #333;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

/* Review table */
.review-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
  font-size: 13px;
}

.review-table th,
.review-table td {
  padding: 8px 10px;
  border: 1px solid #e0e0e0;
  text-align: left;
  vertical-align: top;
}

.review-table th {
  background: #f5f5f5;
  font-weight: 600;
  font-size: 12px;
}

.name-cell  { white-space: nowrap; font-weight: 500; }
.score-cell { text-align: center; }
.comment-cell { max-width: 160px; word-break: break-word; }
.private-tag  { color: #888; font-style: italic; }

.empty-msg { color: #888; }
</style>
