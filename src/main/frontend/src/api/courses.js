import { api } from './client'

const fallbackCourses = [
  {
    id: 1,
    name: '아로마 밸런스 테라피',
    durationMinutes: 60,
    memberPrice: 69000,
    nonMemberPrice: 79000,
    staff: { name: '하린 테라피스트' },
  },
  {
    id: 2,
    name: '딥 릴리프 바디 케어',
    durationMinutes: 90,
    memberPrice: 98000,
    nonMemberPrice: 115000,
    staff: { name: '서윤 테라피스트' },
  },
  {
    id: 3,
    name: '시그니처 릴랙싱 프로그램',
    durationMinutes: 120,
    memberPrice: 132000,
    nonMemberPrice: 149000,
    staff: { name: '민재 테라피스트' },
  },
]

export async function fetchCourses() {
  try {
    const { data } = await api.get('/courses')
    return Array.isArray(data?.data) && data.data.length > 0 ? data.data : fallbackCourses
  } catch {
    return fallbackCourses
  }
}

export async function fetchCourse(courseId) {
  try {
    const { data } = await api.get(`/courses/${courseId}`)
    return data?.data
  } catch {
    return fallbackCourses.find((course) => String(course.id) === String(courseId)) ?? null
  }
}
