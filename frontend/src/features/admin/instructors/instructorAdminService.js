import api from '../../../services/api.js'

const BASE = '/api/instructors'

export function searchInstructors(params) {
  return api.get(BASE, { params }).then(r => r.data.data)
}

export function getInstructor(id) {
  return api.get(`${BASE}/${id}`).then(r => r.data.data)
}

export function inviteInstructors(emails) {
  return api.post(`${BASE}/invite`, { emails }).then(r => r.data.data)
}

export function deactivateInstructor(id) {
  return api.put(`${BASE}/${id}/deactivate`).then(r => r.data.data)
}

export function reactivateInstructor(id) {
  return api.put(`${BASE}/${id}/reactivate`).then(r => r.data.data)
}
