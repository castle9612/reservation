function formatDateTime(value) {
  if (!value) return '-'
  try {
    return new Intl.DateTimeFormat('ko-KR', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value))
  } catch {
    return value
  }
}

function statusLabel(status) {
  if (!status) return '대기'
  switch (status) {
    case 'APPROVED':
      return '확정'
    case 'REJECTED':
      return '거절'
    case 'PENDING':
      return '대기'
    default:
      return status
  }
}

export default function ReservationCard({ reservation }) {
  return (
    <article className="glass-card soft-shadow rounded-[28px] border border-white/70 p-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h3 className="text-xl font-semibold text-slate-900">
          {reservation.name || reservation.userId || '예약 정보'}
        </h3>
        <span className="rounded-full bg-slate-900 px-3 py-1 text-xs font-semibold text-white">
          {statusLabel(reservation.status)}
        </span>
      </div>

      <dl className="mt-5 grid gap-3 text-sm text-slate-600 sm:grid-cols-2">
        <div>
          <dt className="text-slate-400">예약 일시</dt>
          <dd className="mt-1 font-medium text-slate-900">
            {formatDateTime(reservation.reservationDateTime)}
          </dd>
        </div>
        <div>
          <dt className="text-slate-400">코스 ID</dt>
          <dd className="mt-1 font-medium text-slate-900">{reservation.courseId ?? '-'}</dd>
        </div>
        <div>
          <dt className="text-slate-400">연락처</dt>
          <dd className="mt-1 font-medium text-slate-900">{reservation.phoneNumber ?? '-'}</dd>
        </div>
        <div>
          <dt className="text-slate-400">회원 ID</dt>
          <dd className="mt-1 font-medium text-slate-900">{reservation.userId ?? '-'}</dd>
        </div>
      </dl>
    </article>
  )
}
