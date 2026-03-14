import { api } from './client'
import { mockNotices, mockStaff } from '../data/mock'

function arrayFrom(payload) {
  if (Array.isArray(payload)) return payload
  if (Array.isArray(payload?.data)) return payload.data
  if (Array.isArray(payload?.content)) return payload.content
  if (Array.isArray(payload?.data?.content)) return payload.data.content
  return null
}

export async function fetchNotices() {
  try {
    const { data } = await api.get('/announcements')
    return arrayFrom(data) ?? mockNotices
  } catch {
    return mockNotices
  }
}

export async function fetchNoticeDetail(id) {
  try {
    const { data } = await api.get(`/announcements/${id}`)
    return data?.data ?? data ?? mockNotices.find((item) => String(item.id) === String(id))
  } catch {
    return mockNotices.find((item) => String(item.id) === String(id)) ?? null
  }
}

export async function fetchStaff() {
  try {
    const { data } = await api.get('/staff')
    return arrayFrom(data) ?? mockStaff
  } catch {
    return mockStaff
  }
}
