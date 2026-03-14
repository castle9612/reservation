import { Link } from 'react-router-dom'
import { ArrowRight, Clock3, UserRound } from 'lucide-react'

function formatPrice(value) {
  return new Intl.NumberFormat('ko-KR').format(value)
}

export default function CourseCard({ course }) {
  return (
    <article className="glass-card soft-shadow group overflow-hidden rounded-[28px] border border-white/70">
      <div className="h-44 bg-gradient-to-br from-emerald-100 via-teal-50 to-stone-100" />
      <div className="space-y-4 p-6">
        <div className="space-y-2">
          <p className="text-xs font-semibold uppercase tracking-[0.25em] text-emerald-700">
            Signature Program
          </p>
          <h3 className="text-2xl font-semibold tracking-tight text-slate-900">
            {course.name}
          </h3>
        </div>

        <div className="space-y-2 text-sm text-slate-600">
          <div className="flex items-center gap-2">
            <Clock3 className="h-4 w-4" />
            <span>{course.durationMinutes}분</span>
          </div>
          <div className="flex items-center gap-2">
            <UserRound className="h-4 w-4" />
            <span>{course.staff?.name ?? '전문 테라피스트 배정'}</span>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-3 rounded-2xl bg-slate-50 p-4">
          <div>
            <p className="text-xs text-slate-500">회원가</p>
            <p className="mt-1 text-lg font-semibold text-slate-900">
              {formatPrice(course.memberPrice)}원
            </p>
          </div>
          <div>
            <p className="text-xs text-slate-500">비회원가</p>
            <p className="mt-1 text-lg font-semibold text-slate-900">
              {formatPrice(course.nonMemberPrice)}원
            </p>
          </div>
        </div>

        <Link
          to={`/courses/${course.id}`}
          className="inline-flex items-center gap-2 text-sm font-semibold text-slate-900 transition group-hover:gap-3"
        >
          자세히 보기
          <ArrowRight className="h-4 w-4" />
        </Link>
      </div>
    </article>
  )
}
