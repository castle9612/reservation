import { Link } from 'react-router-dom'

export default function HomePage() {
  return (
    <div className="space-y-8">
      <section className="rounded-2xl bg-white p-8 shadow-sm ring-1 ring-slate-200">
        <p className="mb-3 text-sm font-semibold uppercase tracking-widest text-slate-500">
          Reservation Service
        </p>
        <h1 className="mb-4 text-4xl font-bold tracking-tight text-slate-900">
          예약 웹 프로젝트 프론트 기본 화면
        </h1>
        <p className="max-w-2xl text-base leading-7 text-slate-600">
          지금 필요한 건 일단 뜨는 프론트다. 없는 컴포넌트끼리 서로 import 하면서 같이 무너지는 구조를 걷어내고,
          홈, 코스, 로그인, 회원가입, 예약 진입 화면까지 최소 동작 상태로 다시 잡았다.
        </p>

        <div className="mt-6 flex flex-wrap gap-3">
          <Link
            to="/courses"
            className="rounded-lg bg-slate-900 px-4 py-3 text-sm font-semibold text-white hover:bg-slate-800"
          >
            코스 보러가기
          </Link>
          <Link
            to="/reservations/member"
            className="rounded-lg border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-100"
          >
            회원 예약
          </Link>
          <Link
            to="/reservations/guest"
            className="rounded-lg border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-100"
          >
            비회원 예약
          </Link>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-3">
        <div className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
          <h2 className="mb-2 text-lg font-semibold">1. 프론트 안정화</h2>
          <p className="text-sm leading-6 text-slate-600">
            깨진 import, 누락된 레이아웃, 없는 공통 컴포넌트 의존성 제거.
          </p>
        </div>

        <div className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
          <h2 className="mb-2 text-lg font-semibold">2. 인증 연결</h2>
          <p className="text-sm leading-6 text-slate-600">
            로그인, 로그아웃, 현재 로그인 상태 확인을 API와 연결.
          </p>
        </div>

        <div className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
          <h2 className="mb-2 text-lg font-semibold">3. 예약 기능 확장</h2>
          <p className="text-sm leading-6 text-slate-600">
            이후 회원 예약, 비회원 예약, 조회 화면에 실제 폼과 API를 붙이면 된다.
          </p>
        </div>
      </section>
    </div>
  )
}
