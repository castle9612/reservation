export default function MyReservationsPage() {
  return (
    <div className="mx-auto max-w-3xl rounded-2xl bg-white p-8 shadow-sm ring-1 ring-slate-200">
      <p className="text-sm font-semibold uppercase tracking-widest text-slate-500">
        My Reservations
      </p>
      <h1 className="mt-2 text-3xl font-bold text-slate-900">내 예약 조회</h1>
      <p className="mt-4 leading-7 text-slate-600">
        로그인 사용자 예약 조회용 자리다. `/api/reservations/me` 응답 구조가 정리되면 리스트와 수정/취소 버튼을 붙이면 된다.
      </p>
    </div>
  )
}
