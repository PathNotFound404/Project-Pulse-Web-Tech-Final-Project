import api from '../../../services/api.js'

const BASE = '/api/teams'

export function searchTeams(params) {
  return api.get(BASE, { params }).then(r => r.data.data)
}

export function getTeam(id) {
  return api.get(`${BASE}/${id}`).then(r => r.data.data)
}

export function createTeam(data) {
  return api.post(BASE, data).then(r => r.data.data)
}

export function updateTeam(id, data) {
  return api.put(`${BASE}/${id}`, data).then(r => r.data.data)
}

export function deleteTeam(id) {
  return api.delete(`${BASE}/${id}`).then(r => r.data)
}

export function assignStudent(teamId, studentId) {
  return api.post(`${BASE}/${teamId}/students/${studentId}`).then(r => r.data.data)
}

export function removeStudent(teamId, studentId) {
  return api.delete(`${BASE}/${teamId}/students/${studentId}`).then(r => r.data.data)
}

export function assignInstructors(teamId, instructorIds) {
  return api.put(`${BASE}/${teamId}/instructors`, { instructorIds }).then(r => r.data.data)
}

export function removeInstructor(teamId, instructorId) {
  return api.delete(`${BASE}/${teamId}/instructors/${instructorId}`).then(r => r.data.data)
}
