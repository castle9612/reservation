import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import Container from '../components/ui/Container'
import SectionHeading from '../components/ui/SectionHeading'
import InputField from '../components/ui/InputField'
import ReservationCard from '../components/ui/ReservationCard'
import EmptyState from '../components/ui/EmptyState'
import { searchGuestReservations } from '../api/reservations'

export default function GuestLookupPage() {
  const [phoneNumber, setPhoneNumber] = useState('')
  const [results, setResults] = useState([])
  const [searched, setSearched] = useState(false)

  const mutation = useMutation({
    mutationFn: searchGuestReservations,
    onSuccess: (data) => {
      setResults(data)
      setSearched(true)
    },
    onError: () => {
      setResults([])
      setSearched(true)
    },
  })

  const handleSubmit = (event) => {
    event.preventDefault()
    mutation.mutate(phoneNumber)
  }

  return (
    <section className="pb-20 pt-10">
      <Container className="space-y-8">
        <SectionHeading
          eyebrow="Guest Lookup"
          title="비회원 예약 조회"
          description="예약 당시 입력한 연락처로 예약 내역을 확인할 수 있다."
        />

        <div className="glass-card soft-shadow rounded-[36px] border border-white/70 p-8">
          <form className="flex flex-col gap-4 sm:flex-row" onSubmit={handleSubmit}>
            <div className="flex-1">
              <InputField
                label="전화번호"
                name="phoneNumber"
                value={phoneNumber}
                onChange={(event) => setPhoneNumber(event.target.value)}
                placeholder="01012345678"
                required
              />
            </div>
            <div className="sm:mt-8">
              <button
                type="submit"
                disabled={mutation.isPending}
                className="w-full rounded-full bg-slate-900 px-6 py-3 text-sm font-semibold text-white sm:w-auto"
              >
                {mutation.isPending ? '조회 중...' : '예약 조회'}
              </button>
            </div>
          </form>
        </div>

        {searched && (
          results.length > 0 ? (
            <div className="grid gap-5 lg:grid-cols-2">
              {results.map((reservation, index) => (
                <ReservationCard
                  key={reservation.id ?? `${reservation.courseId}-${index}`}
                  reservation={reservation}
                />
              ))}
            </div>
          ) : (
            <EmptyState
              title="조회된 예약이 없습니다."
              description="입력한 연락처를 다시 확인해 주세요."
            />
          )
        )}
      </Container>
    </section>
  )
}
