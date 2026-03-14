import { useQuery } from '@tanstack/react-query'
import SurfaceCard from '../components/common/SurfaceCard'
import SectionHeading from '../components/common/SectionHeading'
import StatusMessage from '../components/common/StatusMessage'
import { fetchMyReservations } from '../api/reservations'
import { useAuth } from '../hooks/useAuth'
import { dateTime } from '../utils/format'

export default function MyReservationsPage() {
  const { data: auth } = useAuth()
  const { data, error } = useQuery({
    queryKey: ['myReservations'],
    queryFn: fetchMyReservations,
    enabled: Boolean(auth?.authenticated),
  })

  const reservations = Array.isArray(data) ? data : Array.isArray(data?.content) ? data.content : []

  return (
    <div>
      <SectionHeading eyebrow="My Reservations" title="내 예약 조회" />
      {!auth?.authenticated ? <StatusMessage type="error">로그인이 필요합니다.</StatusMessage> : null}
      {error ? <div className="mb-4"><StatusMessage type="error">{error.message}</StatusMessage></div> : null}
      <div className="grid gap-4">
        {reservations.map((reservation, index) => (
          <SurfaceCard key={reservation.id ?? index}>
            <div className="grid gap-3 md:grid-cols-4">
              <div>
                <div className="text-xs text-slate-500">예약일시</div>
                <div className="mt-1 font-semibold text-slate-900">{dateTime(reservation.reservationDateTime ?? reservation.dateTime)}</div>
              </div>
              <div>
                <div className="text-xs text-slate-500">상태</div>
                <div className="mt-1 font-semibold text-slate-900">{reservation.status ?? '대기중'}</div>
              </div>
              <div>
                <div className="text-xs text-slate-500">코스</div>
                <div className="mt-1 font-semibold text-slate-900">{reservation.courseName ?? reservation.course?.name ?? '-'}</div>
              </div>
              <div>
                <div className="text-xs text-slate-500">담당자</div>
                <div className="mt-1 font-semibold text-slate-900">{reservation.staffName ?? '-'}</div>
              </div>
            </div>
          </SurfaceCard>
        ))}
        {auth?.authenticated && reservations.length === 0 ? <SurfaceCard>예약 내역이 없습니다.</SurfaceCard> : null}
      </div>
    </div>
  )
}
