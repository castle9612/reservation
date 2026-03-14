import { Link } from 'react-router-dom'

const courses = [
  {
    id: 1,
    name: '기본 테라피 코스',
    duration: '60분',
    memberPrice: '50,000원',
    guestPrice: '60,000원',
    description: '가볍게 관리받고 싶은 경우에 적합한 기본 코스다.',
  },
  {
    id: 2,
    name: '프리미엄 테라피 코스',
    duration: '90분',
    memberPrice: '80,000원',
    guestPrice: '95,000원',
    description: '집중 관리가 필요한 경우를 위한 확장 코스다.',
  },
  {
    id: 3,
    name: '스페셜 케어 코스',
    duration: '120분',
    memberPrice: '110,000원',
    guestPrice: '130,000원',
    description: '시간을 충분히 확보해서 세심하게 관리받는 코스다.',
  },
]

export default function CoursesPage() {
  return (
    <div className="space-y-6">
      <div>
        <p className="text-sm font-semibold uppercase tracking-widest text-slate-500">
          Courses
        </p>
        <h1 className="mt-2 text-3xl font-bold tracking-tight text-slate-900">
          코스 목록
        </h1>
        <p className="mt-3 text-slate-600">
          우선은 화면 확인용 고정 데이터다. 백엔드 코스 API가 안정화되면 여기만 교체하면 된다.
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {courses.map((course) => (
          <article
            key={course.id}
            className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200"
          >
            <h2 className="text-xl font-semibold text-slate-900">{course.name}</h2>
            <p className="mt-3 text-sm leading-6 text-slate-600">{course.description}</p>
            <dl className="mt-4 space-y-2 text-sm text-slate-600">
              <div className="flex justify-between gap-3">
                <dt>소요 시간</dt>
                <dd className="font-medium text-slate-800">{course.duration}</dd>
              </div>
              <div className="flex justify-between gap-3">
                <dt>회원가</dt>
                <dd className="font-medium text-slate-800">{course.memberPrice}</dd>
              </div>
              <div className="flex justify-between gap-3">
                <dt>비회원가</dt>
                <dd className="font-medium text-slate-800">{course.guestPrice}</dd>
              </div>
            </dl>
            <Link
              to={`/courses/${course.id}`}
              className="mt-5 inline-flex rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800"
            >
              상세 보기
            </Link>
          </article>
        ))}
      </div>
    </div>
  )
}
