import { useQuery } from '@tanstack/react-query'
import { fetchCourses, fetchCourse } from '../api/courses'

export function useCourses() {
  return useQuery({
    queryKey: ['courses'],
    queryFn: fetchCourses,
  })
}

export function useCourse(courseId) {
  return useQuery({
    queryKey: ['course', courseId],
    queryFn: () => fetchCourse(courseId),
    enabled: Boolean(courseId),
  })
}
