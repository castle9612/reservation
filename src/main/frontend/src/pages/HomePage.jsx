import { Link } from 'react-router-dom'
import { CalendarDays, HeartHandshake, SearchCheck } from 'lucide-react'
import HeroSection from '../components/sections/HeroSection'
import Container from '../components/ui/Container'
import SectionHeading from '../components/ui/SectionHeading'
import CourseCard from '../components/ui/CourseCard'
import { useCourses } from '../hooks/useCourses'

const features = [
  {
    icon: CalendarDays,
    title: '간단한 예약',
    description: '회원과 비회원 흐름을 분리해 입력 부담을 줄였다.',
  },
  {
    icon: HeartHandshake,
    title: '부드러운 안내',
    description: '복잡한 관리 화면보다 고객 경험 중심으로 구성했다.',
  },
  {
    icon: SearchCheck,
    title: '빠른 조회',
    description: '회원은 내 예약, 비회원은 연락처로 예약 상태를 확인할 수 있다.',
  },
]

export default function HomePage() {
  const { data: courses = [] } = useCourses()

  return (
    <>
      <HeroSection />

      <section className="pb-18">
        <Container className="space-y-10">
          <SectionHeading
            eyebrow="Why choose us"
            title="조용하고 매끈한 예약 흐름"
            description="복잡하게 힘을 주기보다, 필요한 정보가 자연스럽게 이어지는 구조로 설계했다."
            align="center"
          />

          <div className="grid gap-5 md:grid-cols-3">
            {features.map((feature) => {
              const Icon = feature.icon
              return (
                <article
                  key={feature.title}
                  className="glass-card soft-shadow rounded-[28px] border border-white/70 p-6"
                >
                  <div className="inline-flex rounded-2xl bg-emerald-50 p-3 text-emerald-700">
                    <Icon className="h-6 w-6" />
                  </div>
                  <h3 className="mt-5 text-xl font-semibold text-slate-900">
                    {feature.title}
                  </h3>
                  <p className="mt-3 text-sm leading-7 text-slate-600">
                    {feature.description}
                  </p>
                </article>
              )
            })}
          </div>
        </Container>
      </section>

      <section className="pb-20">
        <Container className="space-y-8">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
            <SectionHeading
              eyebrow="Programs"
              title="추천 테라피 코스"
              description="분위기와 흐름이 드러나도록 대표 코스를 먼저 보여준다."
            />
            <Link
              to="/courses"
              className="text-sm font-semibold text-slate-900 underline underline-offset-4"
            >
              전체 코스 보기
            </Link>
          </div>

          <div className="grid gap-5 lg:grid-cols-3">
            {courses.slice(0, 3).map((course) => (
              <CourseCard key={course.id} course={course} />
            ))}
          </div>
        </Container>
      </section>
    </>
  )
}
