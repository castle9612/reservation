import { NavLink } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { logout } from '../../api/auth'
import { useAuth } from '../../hooks/useAuth'

function navClassName({ isActive }) {
  return isActive
    ? 'rounded-md bg-slate-900 px-3 py-2 text-sm font-medium text-white'
    : 'rounded-md px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-200'
}

export default function Header() {
  const queryClient = useQueryClient()
  const { data, isLoading } = useAuth()

  const logoutMutation = useMutation({
    mutationFn: logout,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['me'] })
    },
  })

  const authenticated = Boolean(data?.authenticated)
  const userId = data?.userId
  const role = data?.role

  const handleLogout = () => {
    if (logoutMutation.isPending) return
    logoutMutation.mutate()
  }

  return (
    <header className="sticky top-0 z-50 border-b border-slate-200 bg-white/90 backdrop-blur">
      <div className="mx-auto flex max-w-6xl flex-col gap-3 px-4 py-4 md:flex-row md:items-center md:justify-between">
        <NavLink to="/" className="text-xl font-bold text-slate-900">
          Reservation
        </NavLink>

        <nav className="flex flex-wrap items-center gap-2">
          <NavLink to="/" className={navClassName}>
            홈
          </NavLink>
          <NavLink to="/courses" className={navClassName}>
            코스
          </NavLink>
          <NavLink to="/reservations/member" className={navClassName}>
            회원 예약
          </NavLink>
          <NavLink to="/reservations/guest" className={navClassName}>
            비회원 예약
          </NavLink>
          <NavLink to="/reservations/search" className={navClassName}>
            비회원 조회
          </NavLink>

          {!isLoading && !authenticated && (
            <>
              <NavLink to="/login" className={navClassName}>
                로그인
              </NavLink>
              <NavLink to="/signup" className={navClassName}>
                회원가입
              </NavLink>
            </>
          )}

          {!isLoading && authenticated && (
            <>
              <NavLink to="/reservations/me" className={navClassName}>
                내 예약
              </NavLink>
              <span className="ml-2 text-sm text-slate-600">
                {userId}
                {role ? ` (${role})` : ''}
              </span>
              <button
                type="button"
                onClick={handleLogout}
                className="ml-2 rounded-md bg-rose-600 px-3 py-2 text-sm font-medium text-white hover:bg-rose-700"
              >
                {logoutMutation.isPending ? '로그아웃 중...' : '로그아웃'}
              </button>
            </>
          )}
        </nav>
      </div>
    </header>
  )
}
