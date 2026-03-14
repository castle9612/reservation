import { useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import SurfaceCard from '../components/common/SurfaceCard'
import StatusMessage from '../components/common/StatusMessage'
import { fetchCourses } from '../api/courses'
import { createMemberReservation } from '../api/reservations'
import { useAuth } from '../hooks/useAuth'

export default function MemberBookingPage() {
  const { data: auth } = useAuth()
  const { data: courses = [] } = useQuery({ queryKey: ['courses'], queryFn: fetchCourses })
  const [message, setMessage] = useState({ type: '', text: '' })
  const [form, setForm] = useState({
    courseId: '',
    reservationDate: '',
    reservationTime: '',
    requestNote: '',
  })

  const mutation = useMutation({
    mutationFn: createMemberReservation,
    onSuccess: () => {
      setMessage({ type: 'success', text: '예약 신청이 완료되었습니다.' })
      setForm({ courseId: '', reservationDate: '', reservationTime: '', requestNote: '' })
    },
    onError: (error) => setMessage({ type: 'error', text: error.message || '예약 신청에 실패했습니다.' }),
  })

  return (
    <div className="grid gap-8 xl:grid-cols-[0.85fr_1.15fr]">
      <SurfaceCard>
        <h1 className="text-3xl font-bold text-slate-900">회원 예약</h1>
        <div className="mt-5 space-y-3 text-sm text-slate-600">
          <div className="rounded-2xl bg-stone-100 px-4 py-3">로그인 계정으로 예약이 연결된다.</div>
          <div className="rounded-2xl bg-stone-100 px-4 py-3">관리자 승인 흐름이 있으면 상태는 대기중으로 저장될 수 있다.</div>
        </div>
        {auth?.authenticated ? (
          <div className="mt-6 rounded-2xl bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
            로그인 사용자: {auth.userId}
          </div>
        ) : (
          <div className="mt-6 rounded-2xl bg-rose-50 px-4 py-3 text-sm text-rose-700">
            로그인 후 이용 가능하다.
          </div>
        )}
      </SurfaceCard>

      <SurfaceCard>
        <form
          className="grid gap-4"
          onSubmit={(event) => {
            event.preventDefault()
            mutation.mutate(form)
          }}
        >
          <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">코스</label>
            <select
              name="courseId"
              value={form.courseId}
              onChange={(e) => setForm((prev) => ({ ...prev, courseId: e.target.value }))}
              className="w-full rounded-2xl border border-slate-200 px-4 py-3"
              required
            >
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
              <input type="date" value={form.reservationDate} onChange={(e) => setForm((prev) => ({ ...prev, reservationDate: e.target.value }))} className="w-full rounded-2xl border border-slate-200 px-4 py-3" required />
            </div>
            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">예약시간</label>
              <input type="time" value={form.reservationTime} onChange={(e) => setForm((prev) => ({ ...prev, reservationTime: e.target.value }))} className="w-full rounded-2xl border border-slate-200 px-4 py-3" required />
            </div>
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">요청사항</label>
            <textarea value={form.requestNote} onChange={(e) => setForm((prev) => ({ ...prev, requestNote: e.target.value }))} rows="5" className="w-full rounded-2xl border border-slate-200 px-4 py-3" />
          </div>
          {message.text ? <StatusMessage type={message.type}>{message.text}</StatusMessage> : null}
          <button type="submit" className="rounded-full bg-slate-900 px-5 py-3 text-sm font-semibold text-white" disabled={!auth?.authenticated || mutation.isPending}>
            예약 신청
          </button>
        </form>
      </SurfaceCard>
    </div>
  )
}
