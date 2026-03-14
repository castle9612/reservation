import Container from '../components/ui/Container'
import SectionHeading from '../components/ui/SectionHeading'
import CourseCard from '../components/ui/CourseCard'
import { useCourses } from '../hooks/useCourses'
import EmptyState from '../components/ui/EmptyState'

export default function CoursesPage() {
  const { data: courses = [], isLoading } = useCourses()

  return (
    <section className="pb-20 pt-8">
      <Container className="space-y-8">
        <SectionHeading
          eyebrow="Course Menu"
          title="당신의 컨디션에 맞는 케어 코스"
          description="테라피 시간, 담당자, 회원가와 비회원가를 한눈에 비교할 수 있다."
        />

        {isLoading ? (
          <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
            {Array.from({ length: 6 }).map((_, index) => (
              <div
                key={index}
                className="h-[420px] animate-pulse rounded-[28px] bg-white/70"
              />
            ))}
          </div>
        ) : courses.length > 0 ? (
          <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
            {courses.map((course) => (
              <CourseCard key={course.id} course={course} />
            ))}
          </div>
        ) : (
          <EmptyState
            title="등록된 코스가 아직 없습니다."
            description="관리자에서 코스를 등록하면 이 화면에 자동으로 표시된다."
          />
        )}
      </Container>
    </section>
  )
}
