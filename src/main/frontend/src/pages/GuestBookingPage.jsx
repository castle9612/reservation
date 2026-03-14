import { useMemo, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import Container from '../components/ui/Container'
import SectionHeading from '../components/ui/SectionHeading'
import InputField from '../components/ui/InputField'
import SelectField from '../components/ui/SelectField'
import { createGuestReservation } from '../api/reservations'
import { useCourses } from '../hooks/useCourses'

function toLocalDateTimeValue() {
  const now = new Date()
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset())
  return now.toISOString().slice(0, 16)
}

export default function GuestBookingPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const { data: courses = [] } = useCourses()

  const searchParams = useMemo(() => new URLSearchParams(location.search), [location.search])

  const [form, setForm] = useState({
    name: '',
    phoneNumber: '',
    courseId: searchParams.get('courseId') || '',
    reservationDateTime: toLocalDateTimeValue(),
  })
  const [message, setMessage] = useState('')

  const mutation = useMutation({
    mutationFn: createGuestReservation,
    onSuccess: (data) => {
      setMessage(data?.message || '비회원 예약이 접수되었습니다.')
      setTimeout(() => navigate('/reservations/lookup'), 700)
    },
    onError: (error) => {
      setMessage(error.message || '비회원 예약에 실패했습니다.')
    },
  })

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    mutation.mutate({
      name: form.name,
      phoneNumber: form.phoneNumber,
      courseId: Number(form.courseId),
      reservationDateTime: form.reservationDateTime,
    })
  }

  return (
    <section className="pb-20 pt-10">
      <Container className="max-w-3xl">
        <div className="glass-card soft-shadow rounded-[36px] border border-white/70 p-8 sm:p-10">
          <SectionHeading
            eyebrow="Guest Booking"
            title="비회원 예약"
            description="이름과 연락처만으로 간단하게 예약을 남길 수 있다."
          />

          <form className="mt-8 grid gap-5 md:grid-cols-2" onSubmit={handleSubmit}>
            <InputField
              label="이름"
              name="name"
              value={form.name}
              onChange={handleChange}
              required
            />
            <InputField
              label="전화번호"
              name="phoneNumber"
              value={form.phoneNumber}
              onChange={handleChange}
              placeholder="01012345678"
              required
            />
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
                {mutation.isPending ? '예약 등록 중...' : '비회원 예약 등록'}
              </button>
            </div>
          </form>
        </div>
      </Container>
    </section>
  )
}
