// frontend/src/features/instructor/instructorService.js
// Handles all API calls for instructor use cases UC-30 through UC-34

import axios from 'axios'

const BASE_URL = '/api/instructors'

// UC-30: Register instructor account using invite token
export async function registerInstructor(token, formData) {
  const response = await axios.post(`${BASE_URL}/register?token=${token}`, formData)
  return response.data
}

// UC-31: Get peer eval report for entire section for a given week
export async function getSectionPeerEvalReport(sectionId, activeWeek) {
  const response = await axios.get(
    `${BASE_URL}/reports/peer-evaluation/section/${sectionId}`,
    { params: { activeWeek } }
  )
  return response.data
}

// UC-32: Get WAR report for a team for a given week
export async function getTeamWarReport(teamId, activeWeek) {
  const response = await axios.get(
    `${BASE_URL}/reports/war/team/${teamId}`,
    { params: { activeWeek } }
  )
  return response.data
}

// UC-33: Get peer eval report for a student over a period
export async function getStudentPeerEvalReport(studentId, startWeek, endWeek) {
  const response = await axios.get(
    `${BASE_URL}/reports/peer-evaluation/student/${studentId}`,
    { params: { startWeek, endWeek } }
  )
  return response.data
}

// UC-34: Get WAR report for a student over a period
export async function getStudentWarReport(studentId, startWeek, endWeek) {
  const response = await axios.get(
    `${BASE_URL}/reports/war/student/${studentId}`,
    { params: { startWeek, endWeek } }
  )
  return response.data
}