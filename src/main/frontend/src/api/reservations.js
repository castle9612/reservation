import { api, bootstrapCsrf } from './client'

export async function createMemberReservation(payload) {
  await bootstrapCsrf()
  const { data } = await api.post('/reservations/member', payload)
  if (!data?.success) {
    throw new Error(data?.message || '회원 예약에 실패했습니다.')
  }
  return data
}

export async function createGuestReservation(payload) {
  await bootstrapCsrf()
  const { data } = await api.post('/reservations/guest', payload)
  if (!data?.success) {
    throw new Error(data?.message || '비회원 예약에 실패했습니다.')
  }
  return data
}

export async function fetchMyReservations() {
  const { data } = await api.get('/reservations/me')
  return data?.data ?? []
}

export async function searchGuestReservations(phoneNumber) {
  const { data } = await api.get('/reservations/search', {
    params: { phoneNumber },
  })
  return data?.data ?? []
}
