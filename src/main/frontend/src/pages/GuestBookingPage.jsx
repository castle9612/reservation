import { useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import SurfaceCard from '../components/common/SurfaceCard'
import StatusMessage from '../components/common/StatusMessage'
import { fetchCourses } from '../api/courses'
import { createGuestReservation } from '../api/reservations'

export default function GuestBookingPage() {
  const { data: courses = [] } = useQuery({ queryKey: ['courses'], queryFn: fetchCourses })
  const [form, setForm] = useState({
    guestName: '',
    guestPhone: '',
    courseId: '',
    reservationDate: '',
    reservationTime: '',
    requestNote: '',
  })
  const [message, setMessage] = useState({ type: '', text: '' })

  const mutation = useMutation({
    mutationFn: createGuestReservation,
    onSuccess: () => {
      setMessage({ type: 'success', text: '비회원 예약 신청이 완료되었습니다.' })
      setForm({
        guestName: '',
        guestPhone: '',
        courseId: '',
        reservationDate: '',
        reservationTime: '',
        requestNote: '',
      })
    },
    onError: (error) => {
      setMessage({ type: 'error', text: error.message || '비회원 예약 신청에 실패했습니다.' })
    },
  })

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  return (
    <div className="grid gap-8 xl:grid-cols-[0.85fr_1.15fr]">
      <SurfaceCard>
        <h1 className="text-3xl font-bold text-slate-900">비회원 예약</h1>
        <div className="mt-5 space-y-3 text-sm text-slate-600">
          <div className="rounded-2xl bg-stone-100 px-4 py-3">이름과 전화번호로 예약 조회가 가능하다.</div>
          <div className="rounded-2xl bg-stone-100 px-4 py-3">예약 승인 여부는 운영 방식에 따라 달라질 수 있다.</div>
        </div>
      </SurfaceCard>

      <SurfaceCard>
        <form
          className="grid gap-4"
          onSubmit={(event) => {
            event.preventDefault()
            mutation.mutate(form)
          }}
        >
          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">이름</label>
              <input name="guestName" value={form.guestName} onChange={handleChange} className="w-full rounded-2xl border border-slate-200 px-4 py-3" required />
            </div>
            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">전화번호</label>
              <input name="guestPhone" value={form.guestPhone} onChange={handleChange} className="w-full rounded-2xl border border-slate-200 px-4 py-3" required />
            </div>
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">코스</label>
            <select name="courseId" value={form.courseId} onChange={handleChange} className="w-full rounded-2xl border border-slate-200 px-4 py-3" required>
              <option value="">선택</option>
              {courses.map((course) => (
                <option key={course.id} value={course.id}>
                  {course.name}
                </option>
              ))}
            </select>
          </div>
          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">예약일</label>
              <input type="date" name="reservationDate" value={form.reservationDate} onChange={handleChange} className="w-full rounded-2xl border border-slate-200 px-4 py-3" required />
            </div>
            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">예약시간</label>
              <input type="time" name="reservationTime" value={form.reservationTime} onChange={handleChange} className="w-full rounded-2xl border border-slate-200 px-4 py-3" required />
            </div>
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">요청사항</label>
            <textarea name="requestNote" value={form.requestNote} onChange={handleChange} rows="5" className="w-full rounded-2xl border border-slate-200 px-4 py-3" />
          </div>
          {message.text ? <StatusMessage type={message.type}>{message.text}</StatusMessage> : null}
          <button type="submit" className="rounded-full bg-slate-900 px-5 py-3 text-sm font-semibold text-white" disabled={mutation.isPending}>
            예약 신청
          </button>
        </form>
      </SurfaceCard>
    </div>
  )
}
