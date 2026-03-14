import { Link, NavLink } from 'react-router-dom'
import { Menu, Sparkles } from 'lucide-react'
import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useAuth } from '../../hooks/useAuth'
import { logout } from '../../api/auth'

const navItems = [
  { to: '/', label: '홈' },
  { to: '/courses', label: '코스' },
  { to: '/staff', label: '스태프' },
  { to: '/notices', label: '공지사항' },
  { to: '/booking/member', label: '회원예약' },
  { to: '/booking/guest', label: '비회원예약' },
  { to: '/reservations/guest-search', label: '비회원조회' },
]

function itemClass({ isActive }) {
  return isActive
    ? 'rounded-full bg-amber-100 px-4 py-2 text-sm font-semibold text-amber-900'
    : 'rounded-full px-4 py-2 text-sm font-medium text-slate-700 transition hover:bg-white/80 hover:text-slate-900'
}

export default function Header() {
  const [open, setOpen] = useState(false)
  const queryClient = useQueryClient()
  const { data } = useAuth()

  const logoutMutation = useMutation({
    mutationFn: logout,
    onSettled: async () => {
      await queryClient.invalidateQueries({ queryKey: ['me'] })
    },
  })

  const authenticated = Boolean(data?.authenticated)

  return (
    <header className="sticky top-0 z-50 border-b border-white/50 bg-stone-50/80 backdrop-blur-xl">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6 lg:px-8">
        <Link to="/" className="flex items-center gap-3">
          <div className="flex h-11 w-11 items-center justify-center rounded-full bg-slate-900 text-white shadow-lg shadow-slate-900/10">
            <Sparkles size={18} />
          </div>
          <div>
            <div className="text-lg font-bold tracking-tight text-slate-900">ANU Therapy</div>
            <div className="text-xs text-slate-500">Reservation Studio</div>
          </div>
        </Link>

        <button
          type="button"
          className="rounded-full border border-slate-200 bg-white p-2 text-slate-700 lg:hidden"
          onClick={() => setOpen((prev) => !prev)}
        >
          <Menu size={18} />
        </button>

        <div className="hidden items-center gap-2 lg:flex">
          {navItems.map((item) => (
            <NavLink key={item.to} to={item.to} className={itemClass}>
              {item.label}
            </NavLink>
          ))}
        </div>

        <div className="hidden items-center gap-3 lg:flex">
          {authenticated ? (
            <>
              <NavLink to="/reservations/me" className={itemClass}>
                내 예약
              </NavLink>
              <button
                type="button"
                className="rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white"
                onClick={() => logoutMutation.mutate()}
              >
                로그아웃
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" className={itemClass}>
                로그인
              </NavLink>
              <NavLink
                to="/signup"
                className="rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white"
              >
                회원가입
              </NavLink>
            </>
          )}
        </div>
      </div>

      {open && (
        <div className="border-t border-slate-200 bg-white/95 lg:hidden">
          <div className="mx-auto flex max-w-7xl flex-col gap-2 px-4 py-4 sm:px-6 lg:px-8">
            {navItems.map((item) => (
              <NavLink key={item.to} to={item.to} className={itemClass} onClick={() => setOpen(false)}>
                {item.label}
              </NavLink>
            ))}
            {authenticated ? (
              <>
                <NavLink to="/reservations/me" className={itemClass} onClick={() => setOpen(false)}>
                  내 예약
                </NavLink>
                <button
                  type="button"
                  className="rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white"
                  onClick={() => {
                    setOpen(false)
                    logoutMutation.mutate()
                  }}
                >
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <NavLink to="/login" className={itemClass} onClick={() => setOpen(false)}>
                  로그인
                </NavLink>
                <NavLink
                  to="/signup"
                  className="rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white"
                  onClick={() => setOpen(false)}
                >
                  회원가입
                </NavLink>
              </>
            )}
          </div>
        </div>
      )}
    </header>
  )
}
