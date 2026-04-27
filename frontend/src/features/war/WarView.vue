<script setup>
import { ref, computed, onMounted } from 'vue'
import { getWar, addActivity, updateActivity, deleteActivity } from './warService.js'

const CATEGORIES = [
  'DEVELOPMENT', 'TESTING', 'BUGFIX', 'COMMUNICATION', 'DOCUMENTATION',
  'DESIGN', 'PLANNING', 'LEARNING', 'DEPLOYMENT', 'SUPPORT', 'MISCELLANEOUS',
]
const STATUSES = ['IN_PROGRESS', 'UNDER_TESTING', 'DONE']
const STATUS_LABELS = { IN_PROGRESS: 'In Progress', UNDER_TESTING: 'Under Testing', DONE: 'Done' }

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
const currentWeekStart = ref(getMondayOf(new Date()))

const war = ref(null)
const apiError = ref('')
const successMessage = ref('')

const showForm = ref(false)
const editingId = ref(null)
const confirmDeleteId = ref(null)

const emptyForm = () => ({
  category: CATEGORIES[0],
  description: '',
  plannedHours: '',
  actualHours: '',
  status: STATUSES[0],
})
const form = ref(emptyForm())
const formError = ref('')

const weekLabel = computed(() => {
  const end = new Date(currentWeekStart.value)
  end.setDate(end.getDate() + 6)
  return `${formatDate(toISODate(currentWeekStart.value))} – ${formatDate(toISODate(end))}`
})

const isNextWeekDisabled = computed(() => {
  const next = new Date(currentWeekStart.value)
  next.setDate(next.getDate() + 7)
  return next > thisWeekMonday
})

async function loadWar() {
  apiError.value = ''
  successMessage.value = ''
  try {
    const result = await getWar(toISODate(currentWeekStart.value))
    if (result.flag) {
      war.value = result.data
    } else {
      apiError.value = result.message
    }
  } catch (err) {
    apiError.value = err.response?.data?.message ?? 'Failed to load activities.'
  }
}

function prevWeek() {
  const d = new Date(currentWeekStart.value)
  d.setDate(d.getDate() - 7)
  currentWeekStart.value = d
  loadWar()
}

function nextWeek() {
  if (isNextWeekDisabled.value) return
  const d = new Date(currentWeekStart.value)
  d.setDate(d.getDate() + 7)
  currentWeekStart.value = d
  loadWar()
}

function openAddForm() {
  editingId.value = null
  form.value = emptyForm()
  formError.value = ''
  showForm.value = true
}

function openEditForm(activity) {
  editingId.value = activity.id
  form.value = {
    category: activity.category,
    description: activity.description,
    plannedHours: activity.plannedHours,
    actualHours: activity.actualHours,
    status: activity.status,
  }
  formError.value = ''
  showForm.value = true
}

function cancelForm() {
  showForm.value = false
  editingId.value = null
  form.value = emptyForm()
  formError.value = ''
}

async function submitForm() {
  formError.value = ''
  const payload = {
    category: form.value.category,
    description: form.value.description,
    plannedHours: parseFloat(form.value.plannedHours),
    actualHours: parseFloat(form.value.actualHours),
    status: form.value.status,
  }

  try {
    let result
    if (editingId.value) {
      result = await updateActivity(war.value.id, editingId.value, payload)
    } else {
      result = await addActivity(war.value.id, payload)
    }

    if (result.flag) {
      successMessage.value = editingId.value ? 'Activity updated.' : 'Activity added.'
      cancelForm()
      await loadWar()
    } else {
      formError.value = result.message
    }
  } catch (err) {
    formError.value = err.response?.data?.message ?? 'Something went wrong.'
  }
}

function requestDelete(activityId) {
  console.log('[DEBUG] requestDelete called, activityId:', activityId)
  confirmDeleteId.value = activityId
  console.log('[DEBUG] confirmDeleteId.value is now:', confirmDeleteId.value)
}

function cancelDelete() {
  confirmDeleteId.value = null
}

async function confirmDelete() {
  const activityId = confirmDeleteId.value
  console.log('[DEBUG] confirmDelete called, warId:', war.value?.id, 'activityId:', activityId)
  confirmDeleteId.value = null
  try {
    const result = await deleteActivity(war.value.id, activityId)
    console.log('[DEBUG] deleteActivity result:', result)
    if (result.flag) {
      successMessage.value = 'Activity deleted.'
      await loadWar()
    } else {
      apiError.value = result.message
    }
  } catch (err) {
    apiError.value = err.response?.data?.message ?? 'Failed to delete activity.'
  }
}

onMounted(loadWar)
</script>

<template>
  <div class="war-page">
    <h1>Weekly Activity Report</h1>

    <!-- Week navigation -->
    <div class="week-nav">
      <button @click="prevWeek">&#8249; Prev</button>
      <span class="week-label">{{ weekLabel }}</span>
      <button @click="nextWeek" :disabled="isNextWeekDisabled">Next &#8250;</button>
    </div>

    <p v-if="apiError" class="msg error">{{ apiError }}</p>
    <p v-if="successMessage" class="msg success">{{ successMessage }}</p>

    <!-- Activity table -->
    <template v-if="war">
      <table v-if="war.activities.length > 0">
        <thead>
          <tr>
            <th>Category</th>
            <th>Description</th>
            <th>Planned h</th>
            <th>Actual h</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="activity in war.activities" :key="activity.id">
            <td>{{ activity.category }}</td>
            <td class="desc-cell">{{ activity.description }}</td>
            <td>{{ activity.plannedHours }}</td>
            <td>{{ activity.actualHours }}</td>
            <td>{{ STATUS_LABELS[activity.status] }}</td>
            <td class="actions-cell">
              <button class="btn-edit" @click="openEditForm(activity)">Edit</button>
              <button class="btn-delete" @click="requestDelete(activity.id)">Delete</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="empty-msg">No activities logged for this week.</p>

      <button v-if="!showForm" class="btn-add" @click="openAddForm">+ Add New Activity</button>
    </template>

    <!-- Add / Edit form -->
    <div v-if="showForm" class="activity-form">
      <h2>{{ editingId ? 'Edit Activity' : 'New Activity' }}</h2>

      <div class="field">
        <label>Category</label>
        <select v-model="form.category">
          <option v-for="cat in CATEGORIES" :key="cat" :value="cat">{{ cat }}</option>
        </select>
      </div>

      <div class="field">
        <label>Description</label>
        <textarea v-model="form.description" rows="3" placeholder="Describe the activity…" />
      </div>

      <div class="field">
        <label>Planned Hours</label>
        <input type="number" v-model="form.plannedHours" min="0.1" step="0.5" />
      </div>

      <div class="field">
        <label>Actual Hours</label>
        <input type="number" v-model="form.actualHours" min="0" step="0.5" />
      </div>

      <div class="field">
        <label>Status</label>
        <select v-model="form.status">
          <option v-for="s in STATUSES" :key="s" :value="s">{{ STATUS_LABELS[s] }}</option>
        </select>
      </div>

      <p v-if="formError" class="msg error">{{ formError }}</p>

      <div class="form-actions">
        <button class="btn-submit" @click="submitForm">
          {{ editingId ? 'Save Changes' : 'Add Activity' }}
        </button>
        <button class="btn-cancel" @click="cancelForm">Cancel</button>
      </div>
    </div>

    <!-- Delete confirmation dialog -->
    <div v-if="confirmDeleteId !== null" class="dialog-overlay">
      <div class="dialog">
        <p>Delete this activity? This cannot be undone.</p>
        <div class="dialog-actions">
          <button class="btn-delete" @click="confirmDelete">Delete</button>
          <button class="btn-cancel" @click="cancelDelete">Cancel</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.war-page {
  max-width: 900px;
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

.week-label {
  font-weight: 600;
  font-size: 15px;
}

.msg {
  padding: 10px 14px;
  border-radius: 4px;
  margin-bottom: 14px;
}
.error { background: #fdecea; color: #c0392b; }
.success { background: #eafaf1; color: #27ae60; }

table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 16px;
}

th, td {
  text-align: left;
  padding: 10px 12px;
  border-bottom: 1px solid #e0e0e0;
  font-size: 14px;
}

th { background: #f5f5f5; font-weight: 600; }

.desc-cell { max-width: 280px; word-break: break-word; }

.actions-cell { white-space: nowrap; }

.empty-msg { color: #888; margin-bottom: 16px; }

.btn-add {
  padding: 8px 18px;
  background: #2c3e50;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  margin-bottom: 24px;
}

.btn-edit {
  padding: 5px 10px;
  background: #3498db;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  margin-right: 6px;
}

.btn-delete {
  padding: 5px 10px;
  background: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}

.activity-form {
  background: #fafafa;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 20px 24px;
  max-width: 520px;
  margin-top: 8px;
}

.activity-form h2 { margin-bottom: 16px; font-size: 17px; }

.field {
  display: flex;
  flex-direction: column;
  margin-bottom: 14px;
}

label { font-size: 13px; font-weight: 500; margin-bottom: 4px; }

input, select, textarea {
  padding: 7px 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
}

textarea { resize: vertical; }

.form-actions { display: flex; gap: 10px; margin-top: 6px; }

.btn-submit {
  padding: 8px 18px;
  background: #2c3e50;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-cancel {
  padding: 8px 18px;
  background: #ecf0f1;
  color: #333;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.dialog {
  background: #fff;
  border-radius: 6px;
  padding: 28px 32px;
  max-width: 360px;
  text-align: center;
}

.dialog p { margin-bottom: 20px; font-size: 15px; }

.dialog-actions { display: flex; justify-content: center; gap: 12px; }
</style>
