import api from '../../services/api.js'

export async function getSheet(week) {
  const { data } = await api.get(`/api/peer-evaluations/team?week=${week}`)
  return data
}

export async function submitEvaluations(payload) {
  const { data } = await api.post('/api/peer-evaluations', payload)
  return data
}

export async function getMyReport(week) {
  const { data } = await api.get(`/api/peer-evaluations/my-report?week=${week}`)
  return data
}
