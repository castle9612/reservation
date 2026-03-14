import { Link, NavLink } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Menu, X } from 'lucide-react'
import { useState } from 'react'
import { logout } from '../../api/auth'
import { useAuth } from '../../hooks/useAuth'
import Container from '../ui/Container'

function navClassName({ isActive }) {
  return isActive
    ? 'text-slate-900 font-semibold'
    : 'text-slate-600 transition hover:text-slate-900'
}

const links = [
  { to: '/', label: '홈' },
  { to: '/courses', label: '코스' },
  { to: '/booking/member', label: '회원 예약' },
  { to: '/booking/guest', label: '비회원 예약' },
  { to: '/reservations/lookup', label: '예약 조회' },
]

export default function SiteHeader() {
  const [open, setOpen] = useState(false)
  const queryClient = useQueryClient()
  const { data } = useAuth()

  const authenticated = Boolean(data?.authenticated)

  const logoutMutation = useMutation({
    mutationFn: logout,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['me'] })
      setOpen(false)
    },
  })

  return (
    <header className="sticky top-0 z-50 border-b border-white/60 bg-white/75 backdrop-blur-xl">
      <Container className="flex items-center justify-between py-4">
        <Link to="/" className="text-xl font-semibold tracking-tight text-slate-900">
          Calm Reserve
        </Link>

        <nav className="hidden items-center gap-6 lg:flex">
          {links.map((link) => (
            <NavLink key={link.to} to={link.to} className={navClassName}>
              {link.label}
            </NavLink>
          ))}
        </nav>

        <div className="hidden items-center gap-3 lg:flex">
          {!authenticated ? (
            <>
              <Link
                to="/login"
                className="rounded-full px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100"
              >
                로그인
              </Link>
              <Link
                to="/signup"
                className="rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white"
              >
                회원가입
              </Link>
            </>
          ) : (
            <>
              <Link
                to="/reservations/me"
                className="rounded-full px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100"
              >
                내 예약
              </Link>
              <button
                type="button"
                onClick={() => logoutMutation.mutate()}
                className="rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white"
              >
                로그아웃
              </button>
            </>
          )}
        </div>

        <button
          type="button"
          className="inline-flex rounded-full border border-slate-200 p-2 lg:hidden"
          onClick={() => setOpen((prev) => !prev)}
        >
          {open ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
        </button>
      </Container>

      {open && (
        <div className="border-t border-slate-100 bg-white lg:hidden">
          <Container className="flex flex-col gap-4 py-4">
            {links.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                onClick={() => setOpen(false)}
                className={({ isActive }) =>
                  isActive ? 'font-semibold text-slate-900' : 'text-slate-600'
                }
              >
                {link.label}
              </NavLink>
            ))}

            <div className="flex gap-3 pt-2">
              {!authenticated ? (
                <>
                  <Link
                    to="/login"
                    onClick={() => setOpen(false)}
                    className="rounded-full border border-slate-200 px-4 py-2 text-sm font-medium text-slate-700"
                  >
                    로그인
                  </Link>
                  <Link
                    to="/signup"
                    onClick={() => setOpen(false)}
                    className="rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white"
                  >
                    회원가입
                  </Link>
                </>
              ) : (
                <>
                  <Link
                    to="/reservations/me"
                    onClick={() => setOpen(false)}
                    className="rounded-full border border-slate-200 px-4 py-2 text-sm font-medium text-slate-700"
                  >
                    내 예약
                  </Link>
                  <button
                    type="button"
                    onClick={() => logoutMutation.mutate()}
                    className="rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white"
                  >
                    로그아웃
                  </button>
                </>
              )}
            </div>
          </Container>
        </div>
      )}
    </header>
  )
}
