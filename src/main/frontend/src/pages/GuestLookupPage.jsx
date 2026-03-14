import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import SurfaceCard from '../components/common/SurfaceCard'
import StatusMessage from '../components/common/StatusMessage'
import { searchGuestReservations } from '../api/reservations'
import { dateTime } from '../utils/format'

export default function GuestLookupPage() {
  const [form, setForm] = useState({ guestName: '', guestPhone: '' })
  const [items, setItems] = useState([])

  const mutation = useMutation({
    mutationFn: searchGuestReservations,
    onSuccess: (data) => {
      const list = Array.isArray(data) ? data : Array.isArray(data?.content) ? data.content : []
      setItems(list)
    },
  })

  return (
    <div className="grid gap-8 xl:grid-cols-[0.8fr_1.2fr]">
      <SurfaceCard>
        <h1 className="text-3xl font-bold text-slate-900">비회원 예약 조회</h1>
        <form
          className="mt-8 grid gap-4"
          onSubmit={(event) => {
            event.preventDefault()
            mutation.mutate(form)
          }}
        >
          <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">이름</label>
            <input
              name="guestName"
              value={form.guestName}
              onChange={(e) => setForm((prev) => ({ ...prev, guestName: e.target.value }))}
              className="w-full rounded-2xl border border-slate-200 px-4 py-3"
              required
            />
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">전화번호</label>
            <input
              name="guestPhone"
              value={form.guestPhone}
              onChange={(e) => setForm((prev) => ({ ...prev, guestPhone: e.target.value }))}
              className="w-full rounded-2xl border border-slate-200 px-4 py-3"
              required
            />
          </div>
          <button type="submit" className="rounded-full bg-slate-900 px-5 py-3 text-sm font-semibold text-white" disabled={mutation.isPending}>
            조회하기
          </button>
          {mutation.isError ? <StatusMessage type="error">{mutation.error.message}</StatusMessage> : null}
        </form>
      </SurfaceCard>

      <div className="grid gap-4">
        {items.map((item, index) => (
          <SurfaceCard key={item.id ?? index}>
            <div className="grid gap-3 md:grid-cols-4">
              <div>
                <div className="text-xs text-slate-500">예약자</div>
                <div className="mt-1 font-semibold text-slate-900">{item.guestName ?? item.name ?? '-'}</div>
              </div>
              <div>
                <div className="text-xs text-slate-500">예약일시</div>
                <div className="mt-1 font-semibold text-slate-900">{dateTime(item.reservationDateTime ?? item.dateTime)}</div>
              </div>
              <div>
                <div className="text-xs text-slate-500">코스</div>
                <div className="mt-1 font-semibold text-slate-900">{item.courseName ?? item.course?.name ?? '-'}</div>
              </div>
              <div>
                <div className="text-xs text-slate-500">상태</div>
                <div className="mt-1 font-semibold text-slate-900">{item.status ?? '대기중'}</div>
              </div>
            </div>
          </SurfaceCard>
        ))}
        {!mutation.isPending && items.length === 0 ? <SurfaceCard>조회 결과가 없습니다.</SurfaceCard> : null}
      </div>
    </div>
  )
}
