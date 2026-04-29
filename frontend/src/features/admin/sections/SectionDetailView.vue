<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getSection } from './sectionAdminService.js'
import { searchTeams, updateTeam } from '../teams/teamAdminService.js'
import LoadingSpinner from '../../../components/LoadingSpinner.vue'
import ErrorMessage from '../../../components/ErrorMessage.vue'

const route = useRoute()
const section = ref(null)
const allTeams = ref([])
const loading = ref(true)
const error = ref('')

const addTeamId = ref('')
const addingTeam = ref(false)
const addTeamError = ref('')

// Teams not already assigned to this section
const availableTeams = computed(() => {
  if (!section.value) return []
  return allTeams.value.filter(t => t.sectionName !== section.value.name)
})

async function reload() {
  const [sec, teams] = await Promise.all([
    getSection(route.params.id),
    searchTeams({}),
  ])
  section.value = sec
  allTeams.value = teams
}

onMounted(async () => {
  try {
    await reload()
  } catch (e) {
    error.value = e.response?.data?.message ?? 'Failed to load section.'
  } finally {
    loading.value = false
  }
})

async function assignTeam() {
  if (!addTeamId.value) return
  addingTeam.value = true
  addTeamError.value = ''
  try {
    await updateTeam(addTeamId.value, { sectionId: section.value.id })
    await reload()
    addTeamId.value = ''
  } catch (e) {
    addTeamError.value = e.response?.data?.message ?? 'Failed to assign team.'
  } finally {
    addingTeam.value = false
  }
}
</script>

<template>
  <div>
    <RouterLink to="/admin/sections" class="back-link">← Sections</RouterLink>

    <LoadingSpinner v-if="loading" />
    <ErrorMessage :message="error" />

    <template v-if="section">
      <div class="page-header">
        <h1>{{ section.name }}</h1>
        <div class="header-actions">
          <RouterLink :to="`/admin/sections/${section.id}/edit`" class="btn-secondary">Edit</RouterLink>
          <RouterLink :to="`/admin/sections/${section.id}/active-weeks`" class="btn-secondary">Active Weeks</RouterLink>
        </div>
      </div>

      <dl class="detail-grid">
        <dt>Start Date</dt><dd>{{ section.startDate ?? '—' }}</dd>
        <dt>End Date</dt><dd>{{ section.endDate ?? '—' }}</dd>
        <dt>Rubric</dt><dd>{{ section.rubricName ?? '—' }}</dd>
      </dl>

      <div class="section-header">
        <h2>Teams ({{ section.teamNames?.length ?? 0 }})</h2>
        <RouterLink :to="`/admin/teams/new?sectionId=${section.id}`" class="btn-primary">+ Create Team</RouterLink>
      </div>

      <ul v-if="section.teamNames?.length" class="team-list">
        <li v-for="name in section.teamNames" :key="name">{{ name }}</li>
      </ul>
      <p v-else class="empty">No teams assigned to this section yet.</p>

      <!-- Assign existing team -->
      <div class="assign-box">
        <h3>Assign Existing Team</h3>
        <ErrorMessage :message="addTeamError" />
        <div class="assign-row">
          <select v-model="addTeamId" :disabled="addingTeam">
            <option value="">
              {{ availableTeams.length ? '— Select a team —' : '— All teams are already in this section —' }}
            </option>
            <option v-for="t in availableTeams" :key="t.id" :value="t.id">
              {{ t.name }}{{ t.sectionName ? ` (currently in ${t.sectionName})` : ' (unassigned)' }}
            </option>
          </select>
          <button class="btn-primary" :disabled="!addTeamId || addingTeam" @click="assignTeam">
            {{ addingTeam ? 'Assigning…' : 'Assign' }}
          </button>
        </div>
      </div>
    </template>
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
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
h2 { font-size: 16px; margin: 0; }
.team-list { padding-left: 20px; font-size: 14px; margin-bottom: 28px; }
.team-list li { margin-bottom: 6px; }
.empty { color: #888; font-size: 14px; margin-bottom: 28px; }
.assign-box { border: 1px solid #e0e0e0; border-radius: 6px; padding: 18px 20px; max-width: 540px; margin-top: 8px; }
h3 { font-size: 14px; font-weight: 600; color: #555; margin: 0 0 12px; }
.assign-row { display: flex; gap: 10px; align-items: center; }
.assign-row select { flex: 1; padding: 8px 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; }
.btn-secondary { padding: 7px 16px; background: #eee; color: #333; border-radius: 4px; text-decoration: none; font-size: 13px; }
.btn-primary { padding: 7px 16px; background: #2c3e50; color: white; border-radius: 4px; text-decoration: none; border: none; cursor: pointer; font-size: 13px; white-space: nowrap; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
