import api from '../../services/api.js'

export function getProfile() {
  return api.get('/api/users/me')
}

export function updateProfile(data) {
  return api.put('/api/users/me', data)
}