import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { CalendarCheck2, HeartHandshake, UserRound } from 'lucide-react'
import PageHero from '../components/common/PageHero'
import SectionHeading from '../components/common/SectionHeading'
import SurfaceCard from '../components/common/SurfaceCard'
import { fetchCourses } from '../api/courses'
import { fetchNotices, fetchStaff } from '../api/content'
import { currency, date } from '../utils/format'

export default function HomePage() {
  const coursesQuery = useQuery({ queryKey: ['courses'], queryFn: fetchCourses })
  const noticesQuery = useQuery({ queryKey: ['notices'], queryFn: fetchNotices })
  const staffQuery = useQuery({ queryKey: ['staff'], queryFn: fetchStaff })

  const courses = (coursesQuery.data ?? []).slice(0, 3)
  const notices = (noticesQuery.data ?? []).slice(0, 3)
  const staff = (staffQuery.data ?? []).slice(0, 3)

  return (
    <div className="space-y-14">
      <PageHero
        eyebrow="Reservation Therapy"
        title="편안한 예약, 차분한 케어"
        description="회원과 비회원 모두 간단하게 예약하고, 코스와 스태프 정보를 한 번에 확인할 수 있도록 정리한 메인 화면."
        actions={
          <>
            <Link to="/booking/member" className="rounded-full bg-slate-900 px-5 py-3 text-sm font-semibold text-white">
              회원 예약
            </Link>
            <Link to="/booking/guest" className="rounded-full border border-slate-300 bg-white px-5 py-3 text-sm font-semibold text-slate-800">
              비회원 예약
            </Link>
          </>
        }
        image={{
          title: 'Quiet Ritual for Everyday Recovery',
          description: '차분한 색감과 매끈한 동선으로 실제 예약 사이트처럼 보이게 다시 구성했다.',
        }}
      />

      <section className="grid gap-4 md:grid-cols-3">
        {[
          { icon: <CalendarCheck2 size={20} />, title: '간편 예약', text: '회원 예약과 비회원 예약을 분리해 흐름을 단순하게 구성.' },
          { icon: <UserRound size={20} />, title: '스태프 정보', text: '담당 스태프와 전문 영역을 빠르게 확인 가능.' },
          { icon: <HeartHandshake size={20} />, title: '예약 조회', text: '회원과 비회원 모두 자신의 예약을 다시 찾아볼 수 있음.' },
        ].map((item) => (
          <SurfaceCard key={item.title} className="flex items-start gap-4">
            <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-amber-100 text-amber-900">
              {item.icon}
            </div>
            <div>
              <div className="font-semibold text-slate-900">{item.title}</div>
              <div className="mt-2 text-sm leading-6 text-slate-600">{item.text}</div>
            </div>
          </SurfaceCard>
        ))}
      </section>

      <section>
        <SectionHeading
          eyebrow="Courses"
          title="대표 코스"
          description="현재 등록된 코스를 바로 확인하고 상세 화면으로 이동할 수 있다."
          action={
            <Link to="/courses" className="rounded-full border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-800">
              전체 보기
            </Link>
          }
        />
        <div className="grid gap-4 lg:grid-cols-3">
          {courses.map((course) => (
            <SurfaceCard key={course.id}>
              <div className="mb-4 h-48 rounded-[24px] bg-gradient-to-br from-stone-100 via-amber-50 to-orange-100" />
              <div className="text-xl font-semibold text-slate-900">{course.name}</div>
              <div className="mt-3 line-clamp-2 text-sm leading-6 text-slate-600">{course.description}</div>
              <div className="mt-5 flex items-center justify-between text-sm">
                <span className="rounded-full bg-stone-100 px-3 py-1 text-slate-700">{course.duration}분</span>
                <span className="font-semibold text-slate-900">{currency(course.memberPrice ?? course.price)}</span>
              </div>
              <Link
                to={`/courses/${course.id}`}
                className="mt-6 inline-flex rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white"
              >
                자세히 보기
              </Link>
            </SurfaceCard>
          ))}
        </div>
      </section>

      <section className="grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
        <div>
          <SectionHeading
            eyebrow="Notices"
            title="공지사항"
            action={
              <Link to="/notices" className="rounded-full border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-800">
                더 보기
              </Link>
            }
          />
          <div className="space-y-4">
            {notices.map((notice) => (
              <Link key={notice.id} to={`/notices/${notice.id}`}>
                <SurfaceCard className="transition hover:-translate-y-0.5">
                  <div className="text-sm text-slate-500">{date(notice.createdAt)}</div>
                  <div className="mt-2 text-lg font-semibold text-slate-900">{notice.title}</div>
                  <div className="mt-2 line-clamp-2 text-sm text-slate-600">{notice.content}</div>
                </SurfaceCard>
              </Link>
            ))}
          </div>
        </div>

        <div>
          <SectionHeading
            eyebrow="Staff"
            title="스태프"
            action={
              <Link to="/staff" className="rounded-full border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-800">
                전체 보기
              </Link>
            }
          />
          <div className="grid gap-4">
            {staff.map((member) => (
              <SurfaceCard key={member.id} className="flex items-center gap-5">
                <div className="flex h-20 w-20 items-center justify-center rounded-[24px] bg-gradient-to-br from-amber-100 to-orange-100 text-2xl font-bold text-slate-900">
                  {member.name?.slice(0, 1)}
                </div>
                <div>
                  <div className="text-lg font-semibold text-slate-900">{member.name}</div>
                  <div className="text-sm text-amber-700">{member.role}</div>
                  <div className="mt-2 text-sm text-slate-600">{member.specialty}</div>
                </div>
              </SurfaceCard>
            ))}
          </div>
        </div>
      </section>
    </div>
  )
}
