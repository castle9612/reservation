import { api } from './client'

export async function fetchCourses() {
  const { data } = await api.get('/courses')
  if (!data.success) {
    throw new Error(data.message || '코스 목록을 불러오지 못했습니다.')
  }
  return data.data
}

export async function fetchCourse(courseId) {
  const { data } = await api.get(`/courses/${courseId}`)
  if (!data.success) {
    throw new Error(data.message || '코스 상세를 불러오지 못했습니다.')
  }
  return data.data
}