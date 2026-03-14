import { Link } from 'react-router-dom'

export default function NotFoundPage() {
  return (
    <div className="mx-auto max-w-2xl rounded-2xl bg-white p-8 text-center shadow-sm ring-1 ring-slate-200">
      <p className="text-sm font-semibold uppercase tracking-widest text-slate-500">404</p>
      <h1 className="mt-2 text-3xl font-bold text-slate-900">페이지를 찾을 수 없다</h1>
      <p className="mt-4 leading-7 text-slate-600">
        없는 경로로 왔다. 웹도 사람도 길 잃는 건 익숙하지만, 일단 홈으로 돌아가면 된다.
      </p>
      <Link
        to="/"
        className="mt-6 inline-flex rounded-lg bg-slate-900 px-4 py-3 text-sm font-semibold text-white hover:bg-slate-800"
      >
        홈으로 이동
      </Link>
    </div>
  )
}
