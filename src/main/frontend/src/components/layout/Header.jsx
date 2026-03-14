import { Link, NavLink } from 'react-router-dom'
import { CalendarDays, Search, UserCircle2, UserPlus } from 'lucide-react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { logout } from '../../api/auth'
import { useAuth } from '../../hooks/useAuth'

export default function Header() {
  const queryClient = useQueryClient()
  const { data } = useAuth()

  const logoutMutation = useMutation({
    mutationFn: logout,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['me'] })
      await queryClient.invalidateQueries({ queryKey: ['my-reservations'] })
    },
  })

  return (
    <header className="sticky top-0 z-50 border-b border-stone-200/70 bg-white/90 backdrop-blur-xl">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6 lg:px-8">
        <Link to="/" className="flex items-center gap-3">
          <div className="grid h-11 w-11 place-items-center rounded-2xl bg-emerald-900 text-white shadow-lg shadow-emerald-900/20">
            <CalendarDays className="h-5 w-5" />
          </div>
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.24em] text-emerald-900">Therapy</p>
            <p className="text-xs text-stone-500">Healing Reservation Studio</p>
          </div>
        </Link>

        <nav className="hidden items-center gap-7 md:flex">
          <NavLink to="/courses" className="text-sm font-medium text-stone-700 hover:text-emerald-900">코스</NavLink>
          <NavLink to="/guest-lookup" className="text-sm font-medium text-stone-700 hover:text-emerald-900">비회원 조회</NavLink>
          {data?.authenticated && (
            <NavLink to="/my-reservations" className="text-sm font-medium text-stone-700 hover:text-emerald-900">내 예약</NavLink>
          )}
        </nav>

        <div className="flex items-center gap-3">
          <Link
            to="/guest-lookup"
            className="hidden rounded-full border border-stone-300 px-4 py-2 text-sm font-medium text-stone-700 transition hover:border-stone-400 hover:bg-stone-50 sm:inline-flex"
          >
            <Search className="mr-2 h-4 w-4" />
            예약 조회
          </Link>

          {data?.authenticated ? (
            <button
              onClick={() => logoutMutation.mutate()}
              className="rounded-full bg-emerald-900 px-4 py-2 text-sm font-medium text-white shadow-lg shadow-emerald-900/20 transition hover:-translate-y-0.5"
            >
              로그아웃
            </button>
          ) : (
            <>
              <Link
                to="/signup"
                className="inline-flex items-center rounded-full border border-emerald-900 px-4 py-2 text-sm font-medium text-emerald-900 transition hover:bg-emerald-50"
              >
                <UserPlus className="mr-2 h-4 w-4" />
                회원가입
              </Link>
              <Link
                to="/login"
                className="inline-flex items-center rounded-full bg-emerald-900 px-4 py-2 text-sm font-medium text-white shadow-lg shadow-emerald-900/20 transition hover:-translate-y-0.5"
              >
                <UserCircle2 className="mr-2 h-4 w-4" />
                회원 로그인
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  )
}