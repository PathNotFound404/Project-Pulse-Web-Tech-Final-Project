import api from '../../../services/api.js'

const BASE = '/api/students'

export function searchStudents(params) {
  return api.get(BASE, { params }).then(r => r.data.data)
}

export function getStudent(id) {
  return api.get(`${BASE}/${id}`).then(r => r.data.data)
}

export function deleteStudent(id) {
  return api.delete(`${BASE}/${id}`).then(r => r.data)
}

export function inviteStudents(emails) {
  return api.post(`${BASE}/invite`, { emails }).then(r => r.data.data)
}
