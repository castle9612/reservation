import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'
import SurfaceCard from '../components/common/SurfaceCard'
import { fetchCourseDetail } from '../api/courses'
import { currency } from '../utils/format'

export default function CourseDetailPage() {
  const { courseId } = useParams()
  const { data: course } = useQuery({
    queryKey: ['course', courseId],
    queryFn: () => fetchCourseDetail(courseId),
  })

  if (!course) {
    return <SurfaceCard>코스를 찾을 수 없습니다.</SurfaceCard>
  }

  return (
    <div className="grid gap-8 xl:grid-cols-[1fr_0.85fr]">
      <SurfaceCard className="overflow-hidden">
        <div className="mb-6 h-80 rounded-[28px] bg-gradient-to-br from-stone-100 via-amber-50 to-orange-100" />
        <div className="text-3xl font-bold text-slate-900">{course.name}</div>
        <div className="mt-4 text-base leading-7 text-slate-600">{course.description}</div>
        <div className="mt-8 grid gap-3 sm:grid-cols-3">
          <div className="rounded-2xl bg-stone-100 px-4 py-4">
            <div className="text-xs text-slate-500">소요시간</div>
            <div className="mt-1 font-semibold text-slate-900">{course.duration}분</div>
          </div>
          <div className="rounded-2xl bg-stone-100 px-4 py-4">
            <div className="text-xs text-slate-500">회원가</div>
            <div className="mt-1 font-semibold text-slate-900">{currency(course.memberPrice ?? course.price)}</div>
          </div>
          <div className="rounded-2xl bg-stone-100 px-4 py-4">
            <div className="text-xs text-slate-500">비회원가</div>
            <div className="mt-1 font-semibold text-slate-900">{currency(course.nonMemberPrice ?? course.guestPrice)}</div>
          </div>
        </div>
      </SurfaceCard>

      <SurfaceCard>
        <div className="text-xl font-semibold text-slate-900">예약 이동</div>
        <div className="mt-4 grid gap-3">
          <Link to="/booking/member" className="rounded-full bg-slate-900 px-4 py-3 text-center text-sm font-semibold text-white">
            회원 예약하기
          </Link>
          <Link to="/booking/guest" className="rounded-full border border-slate-300 bg-white px-4 py-3 text-center text-sm font-semibold text-slate-800">
            비회원 예약하기
          </Link>
        </div>
      </SurfaceCard>
    </div>
  )
}
