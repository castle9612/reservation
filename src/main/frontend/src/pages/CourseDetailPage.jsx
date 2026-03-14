import { Link, useParams } from 'react-router-dom'
import { Clock3, UserRound, ChevronRight } from 'lucide-react'
import Container from '../components/ui/Container'
import { useCourse } from '../hooks/useCourses'

function formatPrice(value) {
  return new Intl.NumberFormat('ko-KR').format(value ?? 0)
}

export default function CourseDetailPage() {
  const { courseId } = useParams()
  const { data: course, isLoading } = useCourse(courseId)

  if (isLoading) {
    return (
      <section className="pb-20 pt-8">
        <Container>
          <div className="h-[520px] animate-pulse rounded-[36px] bg-white/70" />
        </Container>
      </section>
    )
  }

  if (!course) {
    return (
      <section className="pb-20 pt-8">
        <Container>
          <div className="glass-card soft-shadow rounded-[32px] border border-white/70 p-10">
            <h1 className="text-3xl font-semibold text-slate-900">코스를 찾을 수 없습니다.</h1>
            <Link
              to="/courses"
              className="mt-6 inline-flex rounded-full bg-slate-900 px-5 py-3 text-sm font-semibold text-white"
            >
              코스 목록으로
            </Link>
          </div>
        </Container>
      </section>
    )
  }

  return (
    <section className="pb-20 pt-8">
      <Container className="space-y-8">
        <nav className="flex items-center gap-2 text-sm text-slate-500">
          <Link to="/">홈</Link>
          <ChevronRight className="h-4 w-4" />
          <Link to="/courses">코스</Link>
          <ChevronRight className="h-4 w-4" />
          <span className="text-slate-900">{course.name}</span>
        </nav>

        <div className="grid gap-8 lg:grid-cols-[1.15fr_0.85fr]">
          <div className="glass-card soft-shadow overflow-hidden rounded-[36px] border border-white/70">
            <div className="h-80 bg-gradient-to-br from-emerald-100 via-teal-50 to-stone-100" />
            <div className="space-y-5 p-8">
              <p className="text-sm font-semibold uppercase tracking-[0.28em] text-emerald-700">
                Signature Course
              </p>
              <h1 className="text-4xl font-semibold tracking-tight text-slate-900">
                {course.name}
              </h1>
              <p className="text-base leading-8 text-slate-600">
                긴장을 천천히 내려놓고 컨디션을 가다듬는 흐름으로 구성된 프로그램.
                예약 전 회원 여부에 맞는 가격을 확인하고 원하는 일정으로 선택할 수 있다.
              </p>

              <div className="grid gap-4 sm:grid-cols-2">
                <div className="rounded-[28px] bg-slate-50 p-5">
                  <div className="flex items-center gap-2 text-sm text-slate-500">
                    <Clock3 className="h-4 w-4" />
                    소요 시간
                  </div>
                  <p className="mt-2 text-2xl font-semibold text-slate-900">
                    {course.durationMinutes}분
                  </p>
                </div>
                <div className="rounded-[28px] bg-slate-50 p-5">
                  <div className="flex items-center gap-2 text-sm text-slate-500">
                    <UserRound className="h-4 w-4" />
                    담당자
                  </div>
                  <p className="mt-2 text-2xl font-semibold text-slate-900">
                    {course.staff?.name ?? '전문 테라피스트'}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <aside className="glass-card soft-shadow rounded-[36px] border border-white/70 p-8">
            <p className="text-sm font-semibold uppercase tracking-[0.24em] text-slate-500">
              Price & Booking
            </p>

            <div className="mt-6 grid gap-4">
              <div className="rounded-[28px] bg-emerald-50 p-5">
                <p className="text-sm text-emerald-700">회원가</p>
                <p className="mt-2 text-3xl font-semibold text-slate-900">
                  {formatPrice(course.memberPrice)}원
                </p>
              </div>
              <div className="rounded-[28px] bg-slate-50 p-5">
                <p className="text-sm text-slate-500">비회원가</p>
                <p className="mt-2 text-3xl font-semibold text-slate-900">
                  {formatPrice(course.nonMemberPrice)}원
                </p>
              </div>
            </div>

            <div className="mt-8 grid gap-3">
              <Link
                to={`/booking/member?courseId=${course.id}`}
                className="inline-flex justify-center rounded-full bg-slate-900 px-5 py-3 text-sm font-semibold text-white"
              >
                회원 예약
              </Link>
              <Link
                to={`/booking/guest?courseId=${course.id}`}
                className="inline-flex justify-center rounded-full border border-slate-200 bg-white px-5 py-3 text-sm font-semibold text-slate-800"
              >
                비회원 예약
              </Link>
            </div>
          </aside>
        </div>
      </Container>
    </section>
  )
}
