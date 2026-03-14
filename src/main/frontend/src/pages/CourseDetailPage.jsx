import { Link, useParams } from 'react-router-dom'

export default function CourseDetailPage() {
  const { courseId } = useParams()

  return (
    <div className="mx-auto max-w-3xl rounded-2xl bg-white p-8 shadow-sm ring-1 ring-slate-200">
      <p className="text-sm font-semibold uppercase tracking-widest text-slate-500">
        Course Detail
      </p>
      <h1 className="mt-2 text-3xl font-bold text-slate-900">코스 상세 #{courseId}</h1>
      <p className="mt-4 leading-7 text-slate-600">
        현재는 최소 실행용 placeholder다. 백엔드 코스 상세 API가 준비되면 이 페이지에서 조회해서 보여주면 된다.
      </p>
      <div className="mt-6 flex gap-3">
        <Link
          to="/courses"
          className="rounded-lg border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-100"
        >
          코스 목록으로
        </Link>
        <Link
          to="/reservations/member"
          className="rounded-lg bg-slate-900 px-4 py-3 text-sm font-semibold text-white hover:bg-slate-800"
        >
          예약하러 가기
        </Link>
      </div>
    </div>
  )
}
