import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import SectionHeading from '../components/common/SectionHeading'
import SurfaceCard from '../components/common/SurfaceCard'
import { fetchCourses } from '../api/courses'
import { currency } from '../utils/format'

export default function CoursesPage() {
  const { data: courses = [] } = useQuery({
    queryKey: ['courses'],
    queryFn: fetchCourses,
  })

  return (
    <div>
      <SectionHeading eyebrow="Courses" title="코스 목록" />
      <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
        {courses.map((course) => (
          <SurfaceCard key={course.id}>
            <div className="mb-5 h-56 rounded-[26px] bg-gradient-to-br from-stone-100 via-amber-50 to-orange-100" />
            <div className="text-2xl font-semibold text-slate-900">{course.name}</div>
            <p className="mt-3 min-h-12 text-sm leading-6 text-slate-600">{course.description}</p>
            <div className="mt-5 grid gap-2 text-sm text-slate-600">
              <div className="flex items-center justify-between">
                <span>소요시간</span>
                <span className="font-medium text-slate-900">{course.duration}분</span>
              </div>
              <div className="flex items-center justify-between">
                <span>회원가</span>
                <span className="font-medium text-slate-900">{currency(course.memberPrice ?? course.price)}</span>
              </div>
              <div className="flex items-center justify-between">
                <span>비회원가</span>
                <span className="font-medium text-slate-900">{currency(course.nonMemberPrice ?? course.guestPrice)}</span>
              </div>
            </div>
            <Link to={`/courses/${course.id}`} className="mt-6 inline-flex rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white">
              상세 보기
            </Link>
          </SurfaceCard>
        ))}
      </div>
    </div>
  )
}
