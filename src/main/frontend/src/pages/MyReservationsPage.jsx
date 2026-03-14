import { useQuery } from '@tanstack/react-query'
import Container from '../components/ui/Container'
import SectionHeading from '../components/ui/SectionHeading'
import EmptyState from '../components/ui/EmptyState'
import ReservationCard from '../components/ui/ReservationCard'
import { fetchMyReservations } from '../api/reservations'
import { useAuth } from '../hooks/useAuth'

export default function MyReservationsPage() {
  const { data: auth } = useAuth()
  const {
    data: reservations = [],
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ['myReservations'],
    queryFn: fetchMyReservations,
    enabled: Boolean(auth?.authenticated),
  })

  if (!auth?.authenticated) {
    return (
      <section className="pb-20 pt-10">
        <Container className="max-w-4xl">
          <EmptyState
            title="로그인 후 내 예약을 확인할 수 있습니다."
            description="회원 전용 예약 조회 화면입니다."
          />
        </Container>
      </section>
    )
  }

  return (
    <section className="pb-20 pt-10">
      <Container className="space-y-8">
        <SectionHeading
          eyebrow="My Reservations"
          title="내 예약 확인"
          description="접수된 예약 상태와 예약 시간을 확인할 수 있다."
        />

        {isLoading ? (
          <div className="grid gap-5 lg:grid-cols-2">
            {Array.from({ length: 2 }).map((_, index) => (
              <div key={index} className="h-60 animate-pulse rounded-[28px] bg-white/70" />
            ))}
          </div>
        ) : isError ? (
          <EmptyState
            title="예약을 불러오지 못했습니다."
            description={error?.message || '잠시 후 다시 시도해 주세요.'}
          />
        ) : reservations.length > 0 ? (
          <div className="grid gap-5 lg:grid-cols-2">
            {reservations.map((reservation, index) => (
              <ReservationCard
                key={reservation.id ?? `${reservation.courseId}-${index}`}
                reservation={reservation}
              />
            ))}
          </div>
        ) : (
          <EmptyState
            title="등록된 예약이 없습니다."
            description="원하는 코스를 선택하고 회원 예약을 등록해 보세요."
          />
        )}
      </Container>
    </section>
  )
}
