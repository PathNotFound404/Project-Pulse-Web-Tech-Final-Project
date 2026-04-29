import api from '../../../services/api.js'

const BASE = '/api/sections'

export function searchSections(params) {
  return api.get(BASE, { params }).then(r => r.data.data)
}

export function getSection(id) {
  return api.get(`${BASE}/${id}`).then(r => r.data.data)
}

export function createSection(data) {
  return api.post(BASE, data).then(r => r.data.data)
}

export function updateSection(id, data) {
  return api.put(`${BASE}/${id}`, data).then(r => r.data.data)
}

export function getActiveWeeks(id) {
  return api.get(`${BASE}/${id}/active-weeks`).then(r => r.data.data)
}

export function setupActiveWeeks(id, inactiveWeekStarts) {
  return api.post(`${BASE}/${id}/active-weeks`, { inactiveWeekStarts }).then(r => r.data.data)
}
