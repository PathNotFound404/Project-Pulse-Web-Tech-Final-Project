import api from '../../services/api.js'

export async function getWar(week) {
  const { data } = await api.get(`/api/wars?week=${week}`)
  return data
}

export async function addActivity(warId, payload) {
  const { data } = await api.post(`/api/wars/${warId}/activities`, payload)
  return data
}

export async function updateActivity(warId, activityId, payload) {
  const { data } = await api.put(`/api/wars/${warId}/activities/${activityId}`, payload)
  return data
}

export async function deleteActivity(warId, activityId) {
  const { data } = await api.delete(`/api/wars/${warId}/activities/${activityId}`)
  return data
}
