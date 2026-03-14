import { useMemo, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useLocation, useNavigate } from 'react-router-dom'
import Container from '../components/ui/Container'
import SectionHeading from '../components/ui/SectionHeading'
import InputField from '../components/ui/InputField'
import SelectField from '../components/ui/SelectField'
import { createMemberReservation } from '../api/reservations'
import { useCourses } from '../hooks/useCourses'
import { useAuth } from '../hooks/useAuth'

function toLocalDateTimeValue(value) {
  if (!value) return ''
  const now = new Date()
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset())
  return now.toISOString().slice(0, 16)
}

export default function MemberBookingPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const queryClient = useQueryClient()
  const { data: auth } = useAuth()
  const { data: courses = [] } = useCourses()

  const searchParams = useMemo(() => new URLSearchParams(location.search), [location.search])
  const defaultCourseId = searchParams.get('courseId') || ''

  const [form, setForm] = useState({
    courseId: defaultCourseId,
    reservationDateTime: toLocalDateTimeValue(),
  })
  const [message, setMessage] = useState('')

  const mutation = useMutation({
    mutationFn: createMemberReservation,
    onSuccess: async (data) => {
      setMessage(data?.message || '예약이 등록되었습니다.')
      await queryClient.invalidateQueries({ queryKey: ['myReservations'] })
      setTimeout(() => navigate('/reservations/me'), 700)
    },
    onError: (error) => {
      setMessage(error.message || '예약에 실패했습니다.')
    },
  })

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    mutation.mutate({
      courseId: Number(form.courseId),
      reservationDateTime: form.reservationDateTime,
    })
  }

  if (!auth?.authenticated) {
    return (
      <section className="pb-20 pt-10">
        <Container className="max-w-3xl">
          <div className="glass-card soft-shadow rounded-[36px] border border-white/70 p-10">
            <h1 className="text-3xl font-semibold text-slate-900">회원 로그인 후 이용 가능</h1>
            <p className="mt-4 text-base leading-7 text-slate-600">
              회원 예약은 로그인 상태에서만 등록할 수 있다.
            </p>
          </div>
        </Container>
      </section>
    )
  }

  return (
    <section className="pb-20 pt-10">
      <Container className="max-w-3xl">
        <div className="glass-card soft-shadow rounded-[36px] border border-white/70 p-8 sm:p-10">
          <SectionHeading
            eyebrow="Member Booking"
            title="회원 예약 등록"
            description="원하는 코스와 시간을 선택하면 예약이 접수된다."
          />

          <form className="mt-8 grid gap-5 md:grid-cols-2" onSubmit={handleSubmit}>
            <SelectField
              label="코스"
              name="courseId"
              value={form.courseId}
              onChange={handleChange}
              required
              options={[
                { value: '', label: '코스를 선택하세요' },
                ...courses.map((course) => ({
                  value: String(course.id),
                  label: `${course.name} · ${course.durationMinutes}분`,
                })),
              ]}
            />
            <InputField
              label="예약 희망 일시"
              name="reservationDateTime"
              type="datetime-local"
              value={form.reservationDateTime}
              onChange={handleChange}
              required
              min={toLocalDateTimeValue()}
            />

            {message && (
              <div className="md:col-span-2 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700">
                {message}
              </div>
            )}

            <div className="md:col-span-2">
              <button
                type="submit"
                disabled={mutation.isPending}
                className="w-full rounded-full bg-slate-900 px-5 py-3 text-sm font-semibold text-white"
              >
                {mutation.isPending ? '예약 등록 중...' : '회원 예약 등록'}
              </button>
            </div>
          </form>
        </div>
      </Container>
    </section>
  )
}
