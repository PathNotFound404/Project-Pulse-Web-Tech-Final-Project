import api from '../../services/api.js'

export function getWar(week) {
  return api.get('/api/wars', { params: { week } })
}

export function createActivity(warId, data) {
  return api.post(`/api/wars/${warId}/activities`, data)
}

export function updateActivity(warId, activityId, data) {
  return api.put(`/api/wars/${warId}/activities/${activityId}`, data)
}

export function deleteActivity(warId, activityId) {
  return api.delete(`/api/wars/${warId}/activities/${activityId}`)
}
