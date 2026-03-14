import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Search } from 'lucide-react'
import { fetchCourses } from '../api/courses'
import SectionTitle from '../components/common/SectionTitle'
import CourseCard from '../components/common/CourseCard'
import EmptyState from '../components/common/EmptyState'

export default function CoursesPage() {
  const [keyword, setKeyword] = useState('')

  const {
    data: courses = [],
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ['courses'],
    queryFn: fetchCourses,
  })

  const filtered = useMemo(() => {
    const lower = keyword.trim().toLowerCase()
    if (!lower) return courses
    return courses.filter((course) => course.name.toLowerCase().includes(lower))
  }, [courses, keyword])

  return (
    <div>
      <SectionTitle
        eyebrow="Course menu"
        title="코스와 가격을 차분하게 비교해봐"
        description="필요한 정보만 먼저 보여주게 만들었어."
      />

      <div className="mt-8 flex items-center gap-3 rounded-[24px] border border-stone-200 bg-white px-4 py-3 shadow-sm">
        <Search className="h-4 w-4 text-stone-400" />
        <input
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="코스명 검색"
          className="w-full bg-transparent text-sm outline-none placeholder:text-stone-400"
        />
      </div>

      {isLoading ? (
        <div className="mt-8">
          <EmptyState title="코스를 불러오는 중..." description="잠깐만 기다려." />
        </div>
      ) : isError ? (
        <div className="mt-8">
          <EmptyState title="코스를 불러오지 못했어." description={error.message} />
        </div>
      ) : filtered.length === 0 ? (
        <div className="mt-8">
          <EmptyState title="조건에 맞는 코스가 없어." description="검색어를 바꾸거나 등록된 코스를 확인해봐." />
        </div>
      ) : (
        <div className="mt-8 grid gap-6 md:grid-cols-2 xl:grid-cols-3">
          {filtered.map((course) => (
            <CourseCard key={course.id} course={course} />
          ))}
        </div>
      )}
    </div>
  )
}