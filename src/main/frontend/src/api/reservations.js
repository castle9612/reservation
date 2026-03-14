import { api, bootstrapCsrf } from './client'

export async function createMemberReservation(payload) {
  await bootstrapCsrf()
  const { data } = await api.post('/reservations/member', payload)
  return data
}

export async function createGuestReservation(payload) {
  await bootstrapCsrf()
  const { data } = await api.post('/reservations/guest', payload)
  return data
}

export async function fetchMyReservations() {
  const { data } = await api.get('/reservations/me')
  if (!data.success) {
    throw new Error(data.message || '예약 목록을 불러오지 못했습니다.')
  }
  return data.data
}

export async function searchGuestReservations(phoneNumber) {
  const { data } = await api.get('/reservations/search', {
    params: { phoneNumber },
  })
  if (!data.success) {
    throw new Error(data.message || '예약 조회에 실패했습니다.')
  }
  return data.data
}