<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTeam, deleteTeam, removeStudent, removeInstructor, assignStudent, assignInstructors } from './teamAdminService.js'
import { searchStudents } from '../students/studentAdminService.js'
import { searchInstructors } from '../instructors/instructorAdminService.js'
import LoadingSpinner from '../../../components/LoadingSpinner.vue'
import ErrorMessage from '../../../components/ErrorMessage.vue'

const route = useRoute()
const router = useRouter()

const team = ref(null)
const loading = ref(true)
const error = ref('')
const confirmDelete = ref(false)
const deleting = ref(false)

const studentQuery = ref('')
const studentResults = ref([])
const instructorQuery = ref('')
const instructorResults = ref([])

onMounted(load)

async function load() {
  loading.value = true
  try {
    team.value = await getTeam(route.params.id)
  } catch (e) {
    error.value = e.response?.data?.message ?? 'Failed to load team.'
  } finally {
    loading.value = false
  }
}

async function handleDeleteTeam() {
  deleting.value = true
  try {
    await deleteTeam(route.params.id)
    router.push('/admin/teams')
  } catch (e) {
    error.value = e.response?.data?.message ?? 'Failed to delete team.'
    confirmDelete.value = false
  } finally {
    deleting.value = false
  }
}

async function handleRemoveStudent(studentName) {
  const all = await searchStudents({ teamName: team.value.name })
  const s = all.find(x => `${x.firstName} ${x.lastName}` === studentName)
  if (!s) return
  error.value = ''
  try {
    team.value = await removeStudent(route.params.id, s.id)
  } catch (e) {
    error.value = e.response?.data?.message ?? 'Failed to remove student.'
  }
}

async function searchForStudents() {
  studentResults.value = await searchStudents({ firstName: studentQuery.value, lastName: studentQuery.value })
}

async function handleAssignStudent(studentId) {
  error.value = ''
  try {
    team.value = await assignStudent(route.params.id, studentId)
    studentResults.value = []
    studentQuery.value = ''
  } catch (e) {
    error.value = e.response?.data?.message ?? 'Failed to assign student.'
  }
}

async function searchForInstructors() {
  instructorResults.value = await searchInstructors({ firstName: instructorQuery.value, lastName: instructorQuery.value })
}

async function handleRemoveInstructor(instructorName) {
  const all = await searchInstructors({ firstName: instructorName.split(' ')[0] })
  const inst = all.find(x => `${x.firstName} ${x.lastName}` === instructorName)
  if (!inst) return
  error.value = ''
  try {
    team.value = await removeInstructor(route.params.id, inst.id)
  } catch (e) {
    error.value = e.response?.data?.message ?? 'Failed to remove instructor.'
  }
}

async function handleAssignInstructor(instructorId) {
  error.value = ''
  try {
    team.value = await assignInstructors(route.params.id, [instructorId])
    instructorResults.value = []
    instructorQuery.value = ''
  } catch (e) {
    error.value = e.response?.data?.message ?? 'Failed to assign instructor.'
  }
}
</script>

<template>
  <div>
    <RouterLink to="/admin/teams" class="back-link">← Teams</RouterLink>

    <LoadingSpinner v-if="loading" />
    <ErrorMessage :message="error" />

    <template v-if="team">
      <div class="page-header">
        <h1>{{ team.name }}</h1>
        <div class="header-actions">
          <RouterLink :to="`/admin/teams/${team.id}/edit`" class="btn-secondary">Edit</RouterLink>
          <button class="btn-danger" @click="confirmDelete = true">Delete</button>
        </div>
      </div>

      <dl class="detail-grid">
        <dt>Section</dt><dd>{{ team.sectionName ?? '—' }}</dd>
      </dl>

      <div class="members-section">
        <div class="members-block">
          <h2>Students ({{ team.studentNames?.length ?? 0 }})</h2>
          <ul>
            <li v-for="name in team.studentNames" :key="name" class="member-row">
              {{ name }}
              <button class="btn-remove" @click="handleRemoveStudent(name)">Remove</button>
            </li>
            <li v-if="!team.studentNames?.length" class="empty">No students assigned.</li>
          </ul>
          <div class="assign-row">
            <input v-model="studentQuery" placeholder="Search students…" @input="searchForStudents" />
            <ul v-if="studentResults.length" class="search-results">
              <li v-for="s in studentResults" :key="s.id" @click="handleAssignStudent(s.id)">
                {{ s.firstName }} {{ s.lastName }}
              </li>
            </ul>
          </div>
        </div>

        <div class="members-block">
          <h2>Instructors ({{ team.instructorNames?.length ?? 0 }})</h2>
          <ul>
            <li v-for="name in team.instructorNames" :key="name" class="member-row">
              {{ name }}
              <button class="btn-remove" @click="handleRemoveInstructor(name)">Remove</button>
            </li>
            <li v-if="!team.instructorNames?.length" class="empty">No instructors assigned.</li>
          </ul>
          <div class="assign-row">
            <input v-model="instructorQuery" placeholder="Search instructors…" @input="searchForInstructors" />
            <ul v-if="instructorResults.length" class="search-results">
              <li v-for="i in instructorResults" :key="i.id" @click="handleAssignInstructor(i.id)">
                {{ i.firstName }} {{ i.lastName }}
              </li>
            </ul>
          </div>
        </div>
      </div>
    </template>

    <div v-if="confirmDelete" class="modal-overlay">
      <div class="modal">
        <h2>Delete team?</h2>
        <p>This will permanently delete <strong>{{ team?.name }}</strong> and remove all associated members. This cannot be undone.</p>
        <div class="modal-actions">
          <button class="btn-danger" :disabled="deleting" @click="handleDeleteTeam">
            {{ deleting ? 'Deleting…' : 'Yes, Delete' }}
          </button>
          <button @click="confirmDelete = false">Cancel</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.back-link { color: #2c3e50; font-size: 13px; text-decoration: none; display: inline-block; margin-bottom: 20px; }
.back-link:hover { text-decoration: underline; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; }
h1 { font-size: 22px; margin: 0; color: #2c3e50; }
.header-actions { display: flex; gap: 10px; }
.detail-grid { display: grid; grid-template-columns: 140px 1fr; gap: 10px 20px; font-size: 14px; max-width: 500px; margin-bottom: 28px; }
dt { font-weight: 600; color: #555; } dd { margin: 0; }
.members-section { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.members-block h2 { font-size: 16px; margin: 0 0 12px; }
ul { list-style: none; padding: 0; margin: 0 0 12px; }
.member-row { display: flex; justify-content: space-between; align-items: center; padding: 6px 0; border-bottom: 1px solid #eee; font-size: 14px; }
.empty { color: #888; font-size: 13px; }
.btn-remove { font-size: 12px; color: #c0392b; background: none; border: none; cursor: pointer; }
.assign-row { position: relative; }
.assign-row input { width: 100%; padding: 7px 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 13px; box-sizing: border-box; }
.search-results { position: absolute; top: 100%; left: 0; right: 0; background: white; border: 1px solid #ccc; border-top: none; border-radius: 0 0 4px 4px; z-index: 10; max-height: 160px; overflow-y: auto; }
.search-results li { padding: 8px 12px; cursor: pointer; font-size: 13px; }
.search-results li:hover { background: #f0f0f0; }
.btn-secondary { padding: 7px 16px; background: #eee; color: #333; border-radius: 4px; text-decoration: none; font-size: 13px; border: none; cursor: pointer; }
.btn-danger { padding: 7px 16px; background: #c0392b; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-danger:disabled { opacity: 0.6; cursor: not-allowed; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.45); display: flex; align-items: center; justify-content: center; z-index: 200; }
.modal { background: white; border-radius: 8px; padding: 28px 32px; max-width: 440px; width: 90%; }
.modal h2 { margin: 0 0 12px; font-size: 18px; }
.modal p { color: #444; font-size: 14px; line-height: 1.6; margin: 0 0 20px; }
.modal-actions { display: flex; gap: 12px; }
.modal-actions button:last-child { padding: 8px 16px; background: #eee; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
</style>
