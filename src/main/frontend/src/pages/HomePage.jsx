import { useQuery } from '@tanstack/react-query'
import BookingHero from '../components/common/BookingHero'
import SectionTitle from '../components/common/SectionTitle'
import CourseCard from '../components/common/CourseCard'
import EmptyState from '../components/common/EmptyState'
import { fetchCourses } from '../api/courses'

export default function HomePage() {
  const {
    data: courses = [],
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ['courses'],
    queryFn: fetchCourses,
  })

  return (
    <div className="space-y-16">
      <BookingHero />

      <section>
        <SectionTitle
          eyebrow="Featured course"
          title="지금 예약 가능한 대표 코스"
          description="회원가와 비회원가를 한 번에 비교하고, 담당 테라피스트까지 확인할 수 있어."
        />

        {isLoading ? (
          <div className="mt-8">
            <EmptyState title="코스를 불러오는 중..." description="잠깐만 기다려." />
          </div>
        ) : isError ? (
          <div className="mt-8">
            <EmptyState title="코스를 불러오지 못했어." description={error.message} />
          </div>
        ) : courses.length === 0 ? (
          <div className="mt-8">
            <EmptyState title="등록된 코스가 아직 없어." description="관리자에서 코스를 추가하면 여기에 표시돼." />
          </div>
        ) : (
          <div className="mt-8 grid gap-6 lg:grid-cols-3">
            {courses.slice(0, 3).map((course) => (
              <CourseCard key={course.id} course={course} />
            ))}
          </div>
        )}
      </section>
    </div>
  )
}