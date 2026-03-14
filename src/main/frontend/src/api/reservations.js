import { api, bootstrapCsrf } from './client'

function unwrap(data) {
  return data?.data ?? data
}

export async function createMemberReservation(payload) {
  await bootstrapCsrf()
  const { data } = await api.post('/reservations/member', payload)
  return unwrap(data)
}

export async function createGuestReservation(payload) {
  await bootstrapCsrf()
  const { data } = await api.post('/reservations/guest', payload)
  return unwrap(data)
}

export async function fetchMyReservations() {
  const { data } = await api.get('/reservations/me')
  return unwrap(data)
}

export async function searchGuestReservations(params) {
  const { data } = await api.get('/reservations/search', { params })
  return unwrap(data)
}
