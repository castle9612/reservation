import { Link } from 'react-router-dom'
import Container from '../components/ui/Container'

export default function NotFoundPage() {
  return (
    <section className="pb-20 pt-16">
      <Container className="max-w-3xl">
        <div className="glass-card soft-shadow rounded-[36px] border border-white/70 p-10 text-center">
          <p className="text-sm font-semibold uppercase tracking-[0.28em] text-emerald-700">
            404
          </p>
          <h1 className="mt-4 text-4xl font-semibold text-slate-900">
            페이지를 찾을 수 없습니다.
          </h1>
          <p className="mt-4 text-base leading-7 text-slate-600">
            주소가 바뀌었거나 삭제된 페이지입니다.
          </p>
          <Link
            to="/"
            className="mt-8 inline-flex rounded-full bg-slate-900 px-6 py-3 text-sm font-semibold text-white"
          >
            홈으로 이동
          </Link>
        </div>
      </Container>
    </section>
  )
}
