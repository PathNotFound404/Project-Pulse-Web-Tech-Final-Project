<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getWar, createActivity, updateActivity, deleteActivity } from './warService.js'

const router = useRouter()

const CATEGORIES = [
  'DEVELOPMENT', 'TESTING', 'BUGFIX', 'COMMUNICATION', 'DOCUMENTATION',
  'DESIGN', 'PLANNING', 'LEARNING', 'DEPLOYMENT', 'SUPPORT', 'MISCELLANEOUS',
]
const STATUSES = ['IN_PROGRESS', 'UNDER_TESTING', 'DONE']

function formatLabel(s) {
  return s.split('_').map(w => w.charAt(0) + w.slice(1).toLowerCase()).join(' ')
}

function generateWeekOptions() {
  const options = []
  const today = new Date()
  const dow = today.getDay() === 0 ? 7 : today.getDay()
  const currentMonday = new Date(today)
  currentMonday.setDate(today.getDate() - (dow - 1))

  for (let i = 0; i < 12; i++) {
    const monday = new Date(currentMonday)
    monday.setDate(currentMonday.getDate() - i * 7)
    const sunday = new Date(monday)
    sunday.setDate(monday.getDate() + 6)
    const mondayStr = monday.toISOString().split('T')[0]
    const sundayStr = sunday.toISOString().split('T')[0]
    options.push({ value: mondayStr, label: `${mondayStr} – ${sundayStr}` })
  }
  return options
}

const weekOptions = generateWeekOptions()
const selectedWeek = ref(weekOptions[0].value)

const warId = ref(null)
const activities = ref([])
const loading = ref(false)
const apiError = ref('')
const successMessage = ref('')

const showForm = ref(false)
const editingActivity = ref(null)
const form = ref({ category: '', description: '', plannedHours: '', actualHours: '', status: '' })
const formErrors = ref({})

const deletingActivityId = ref(null)

onMounted(() => {
  fetchWar()
})

async function fetchWar() {
  loading.value = true
  apiError.value = ''
  successMessage.value = ''
  try {
    const { data } = await getWar(selectedWeek.value)
    if (data.flag) {
      warId.value = data.data.warId
      activities.value = data.data.activities
    } else {
      apiError.value = data.message
    }
  } catch (err) {
    if (err.response?.status === 401) router.push('/login')
    else apiError.value = err.response?.data?.message ?? 'Failed to load activities.'
  } finally {
    loading.value = false
  }
}

function handleWeekChange() {
  showForm.value = false
  deletingActivityId.value = null
  fetchWar()
}

function openAddForm() {
  editingActivity.value = null
  form.value = { category: '', description: '', plannedHours: '', actualHours: '', status: '' }
  formErrors.value = {}
  apiError.value = ''
  successMessage.value = ''
  showForm.value = true
}

function openEditForm(activity) {
  editingActivity.value = activity
  form.value = {
    category: activity.category,
    description: activity.description,
    plannedHours: String(activity.plannedHours),
    actualHours: String(activity.actualHours),
    status: activity.status,
  }
  formErrors.value = {}
  apiError.value = ''
  successMessage.value = ''
  showForm.value = true
}

function cancelForm() {
  showForm.value = false
  editingActivity.value = null
  formErrors.value = {}
}

function validateForm() {
  const e = {}
  if (!form.value.category) e.category = 'Category is required'
  if (!form.value.description.trim()) e.description = 'Description is required'
  const planned = Number(form.value.plannedHours)
  if (form.value.plannedHours === '' || isNaN(planned) || planned <= 0) e.plannedHours = 'Planned hours must be a positive number'
  const actual = Number(form.value.actualHours)
  if (form.value.actualHours === '' || isNaN(actual) || actual < 0) e.actualHours = 'Actual hours must be a non-negative number'
  if (!form.value.status) e.status = 'Status is required'
  formErrors.value = e
  return Object.keys(e).length === 0
}

async function handleSubmitForm() {
  if (!validateForm()) return
  apiError.value = ''
  successMessage.value = ''

  const payload = {
    category: form.value.category,
    description: form.value.description,
    plannedHours: Number(form.value.plannedHours),
    actualHours: Number(form.value.actualHours),
    status: form.value.status,
  }

  try {
    if (editingActivity.value) {
      const { data } = await updateActivity(warId.value, editingActivity.value.id, payload)
      if (data.flag) {
        successMessage.value = 'Activity updated successfully'
        showForm.value = false
        await fetchWar()
      } else {
        apiError.value = data.message
      }
    } else {
      const { data } = await createActivity(warId.value, payload)
      if (data.flag) {
        successMessage.value = 'Activity added successfully'
        showForm.value = false
        await fetchWar()
      } else {
        apiError.value = data.message
      }
    }
  } catch (err) {
    if (err.response?.status === 401) router.push('/login')
    else apiError.value = err.response?.data?.message ?? 'Something went wrong.'
  }
}

function confirmDelete(activityId) {
  deletingActivityId.value = activityId
  successMessage.value = ''
  apiError.value = ''
}

function cancelDelete() {
  deletingActivityId.value = null
}

async function handleDelete() {
  try {
    const { data } = await deleteActivity(warId.value, deletingActivityId.value)
    if (data.flag) {
      successMessage.value = 'Activity deleted successfully'
      deletingActivityId.value = null
      await fetchWar()
    } else {
      apiError.value = data.message
    }
  } catch (err) {
    if (err.response?.status === 401) router.push('/login')
    else apiError.value = err.response?.data?.message ?? 'Failed to delete activity.'
    deletingActivityId.value = null
  }
}
</script>

<template>
  <div class="war-page">
    <h1>Weekly Activity Report</h1>

    <div class="week-selector">
      <label for="week">Week</label>
      <select id="week" v-model="selectedWeek" @change="handleWeekChange">
        <option v-for="opt in weekOptions" :key="opt.value" :value="opt.value">
          {{ opt.label }}
        </option>
      </select>
    </div>

    <p v-if="loading" class="loading">Loading…</p>

    <template v-else>
      <p v-if="apiError" class="error api-error">{{ apiError }}</p>
      <p v-if="successMessage" class="success">{{ successMessage }}</p>

      <div v-if="deletingActivityId !== null" class="confirm-panel">
        <p>Are you sure you want to delete this activity?</p>
        <div class="actions">
          <button type="button" class="btn-danger" @click="handleDelete">Delete</button>
          <button type="button" class="btn-secondary" @click="cancelDelete">Cancel</button>
        </div>
      </div>

      <div v-if="showForm" class="form-panel">
        <h2>{{ editingActivity ? 'Edit Activity' : 'New Activity' }}</h2>

        <div class="field">
          <label for="category">Category</label>
          <select id="category" v-model="form.category">
            <option value="">Select category</option>
            <option v-for="c in CATEGORIES" :key="c" :value="c">{{ formatLabel(c) }}</option>
          </select>
          <span v-if="formErrors.category" class="error">{{ formErrors.category }}</span>
        </div>

        <div class="field">
          <label for="description">Description</label>
          <textarea id="description" v-model="form.description" rows="3" />
          <span v-if="formErrors.description" class="error">{{ formErrors.description }}</span>
        </div>

        <div class="field">
          <label for="plannedHours">Planned Hours</label>
          <input id="plannedHours" v-model="form.plannedHours" type="number" min="0.1" step="0.5" />
          <span v-if="formErrors.plannedHours" class="error">{{ formErrors.plannedHours }}</span>
        </div>

        <div class="field">
          <label for="actualHours">Actual Hours</label>
          <input id="actualHours" v-model="form.actualHours" type="number" min="0" step="0.5" />
          <span v-if="formErrors.actualHours" class="error">{{ formErrors.actualHours }}</span>
        </div>

        <div class="field">
          <label for="status">Status</label>
          <select id="status" v-model="form.status">
            <option value="">Select status</option>
            <option v-for="s in STATUSES" :key="s" :value="s">{{ formatLabel(s) }}</option>
          </select>
          <span v-if="formErrors.status" class="error">{{ formErrors.status }}</span>
        </div>

        <div class="actions">
          <button type="button" class="btn-primary" @click="handleSubmitForm">
            {{ editingActivity ? 'Save Changes' : 'Add Activity' }}
          </button>
          <button type="button" class="btn-secondary" @click="cancelForm">Cancel</button>
        </div>
      </div>

      <div v-else>
        <button type="button" class="btn-primary add-btn" @click="openAddForm">+ Add New Activity</button>

        <p v-if="activities.length === 0" class="empty">No activities for this week.</p>

        <table v-else class="activity-table">
          <thead>
            <tr>
              <th>Category</th>
              <th>Description</th>
              <th>Planned Hrs</th>
              <th>Actual Hrs</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="activity in activities" :key="activity.id">
              <td>{{ formatLabel(activity.category) }}</td>
              <td>{{ activity.description }}</td>
              <td>{{ activity.plannedHours }}</td>
              <td>{{ activity.actualHours }}</td>
              <td>{{ formatLabel(activity.status) }}</td>
              <td class="row-actions">
                <button type="button" class="btn-secondary btn-sm" @click="openEditForm(activity)">Edit</button>
                <button type="button" class="btn-danger btn-sm" @click="confirmDelete(activity.id)">Delete</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>

<style scoped>
.war-page {
  max-width: 900px;
  margin: 40px auto;
  padding: 24px;
}

.week-selector {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.week-selector label {
  font-weight: 500;
}

.week-selector select {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
}

.loading {
  color: #666;
}

.error {
  color: #c0392b;
  font-size: 13px;
  margin-top: 4px;
}

.api-error {
  margin-bottom: 12px;
}

.success {
  color: #27ae60;
  margin-bottom: 12px;
}

.confirm-panel {
  padding: 16px;
  background: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: 4px;
  margin-bottom: 20px;
}

.confirm-panel p {
  margin: 0 0 16px;
}

.form-panel {
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
}

.form-panel h2 {
  margin: 0 0 16px;
  font-size: 18px;
}

.field {
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
}

label {
  margin-bottom: 4px;
  font-weight: 500;
  font-size: 14px;
}

input, select, textarea {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
}

textarea {
  resize: vertical;
}

.add-btn {
  margin-bottom: 16px;
}

.empty {
  color: #666;
  font-style: italic;
}

.activity-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.activity-table th,
.activity-table td {
  padding: 10px 12px;
  border: 1px solid #dee2e6;
  text-align: left;
}

.activity-table th {
  background: #f8f9fa;
  font-weight: 600;
}

.activity-table tbody tr:hover {
  background: #f8f9fa;
}

.row-actions {
  display: flex;
  gap: 8px;
}

.actions {
  display: flex;
  gap: 12px;
}

button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-sm {
  padding: 4px 10px;
  font-size: 13px;
}

.btn-primary {
  background-color: #2c3e50;
  color: white;
}

.btn-secondary {
  background-color: #ecf0f1;
  color: #333;
}

.btn-danger {
  background-color: #e74c3c;
  color: white;
}
</style>
