import { api } from './client'
import { mockCourses } from '../data/mock'

function arrayFrom(payload) {
  if (Array.isArray(payload)) return payload
  if (Array.isArray(payload?.data)) return payload.data
  if (Array.isArray(payload?.content)) return payload.content
  if (Array.isArray(payload?.data?.content)) return payload.data.content
  return null
}

export async function fetchCourses() {
  try {
    const { data } = await api.get('/courses')
    return arrayFrom(data) ?? mockCourses
  } catch {
    return mockCourses
  }
}

export async function fetchCourseDetail(courseId) {
  try {
    const { data } = await api.get(`/courses/${courseId}`)
    return data?.data ?? data ?? mockCourses.find((item) => String(item.id) === String(courseId))
  } catch {
    return mockCourses.find((item) => String(item.id) === String(courseId)) ?? null
  }
}
