import api from '../../../services/api.js'

const BASE = '/api/rubrics'

export function getRubrics() {
  return api.get(BASE).then(r => r.data.data)
}

export function getRubric(id) {
  return api.get(`${BASE}/${id}`).then(r => r.data.data)
}

export function createRubric(data) {
  return api.post(BASE, data).then(r => r.data.data)
}

export function updateRubric(id, data) {
  return api.put(`${BASE}/${id}`, data).then(r => r.data.data)
}

export function duplicateRubric(id, data) {
  return api.post(`${BASE}/${id}/duplicate`, data).then(r => r.data.data)
}

export function deleteRubric(id) {
  return api.delete(`${BASE}/${id}`).then(r => r.data)
}
