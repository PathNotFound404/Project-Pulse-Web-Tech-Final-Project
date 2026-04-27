const API_BASE = '/api'

export async function registerStudent(token, data) {
  const response = await fetch(`${API_BASE}/auth/register/student?token=${encodeURIComponent(token)}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  return response.json()
}