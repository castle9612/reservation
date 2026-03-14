import { Link } from 'react-router-dom'

export default function Footer() {
  return (
    <footer className="border-t border-white/60 bg-white/60">
      <div className="mx-auto flex max-w-7xl flex-col gap-4 px-4 py-8 text-sm text-slate-600 sm:px-6 lg:flex-row lg:items-center lg:justify-between lg:px-8">
        <div>
          <div className="font-semibold text-slate-900">ANU Therapy</div>
          <div>Seoul Relaxation & Reservation Studio</div>
        </div>
        <div className="flex flex-wrap gap-4">
          <Link to="/notices" className="hover:text-slate-900">공지사항</Link>
          <Link to="/staff" className="hover:text-slate-900">스태프</Link>
          <Link to="/courses" className="hover:text-slate-900">코스</Link>
          <Link to="/reservations/guest-search" className="hover:text-slate-900">비회원조회</Link>
        </div>
      </div>
    </footer>
  )
}
