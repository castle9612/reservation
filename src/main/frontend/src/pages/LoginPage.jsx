import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { login } from '../api/auth'

export default function LoginPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [form, setForm] = useState({ userId: '', password: '' })
  const [error, setError] = useState('')

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['me'] })
      navigate('/courses')
    },
    onError: (e) => {
      setError(e.message)
    },
  })

  return (
    <div className="mx-auto max-w-md rounded-[32px] border border-stone-200 bg-white p-8 shadow-[0_18px_50px_rgba(20,29,26,0.07)]">
      <p className="text-xs font-semibold uppercase tracking-[0.28em] text-emerald-700">Member login</p>
      <h1 className="mt-3 text-3xl font-semibold text-stone-900">회원 로그인</h1>
      <p className="mt-3 text-sm leading-6 text-stone-600">예약 확인과 회원 전용 예약을 위해 로그인해.</p>

      <form
        className="mt-8 space-y-4"
        onSubmit={(e) => {
          e.preventDefault()
          setError('')
          mutation.mutate(form)
        }}
      >
        <div>
          <label className="mb-2 block text-sm font-medium text-stone-700">아이디</label>
          <input
            value={form.userId}
            onChange={(e) => setForm((prev) => ({ ...prev, userId: e.target.value }))}
            className="w-full rounded-2xl border border-stone-300 px-4 py-3 outline-none transition focus:border-emerald-700"
          />
        </div>

        <div>
          <label className="mb-2 block text-sm font-medium text-stone-700">비밀번호</label>
          <input
            type="password"
            value={form.password}
            onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))}
            className="w-full rounded-2xl border border-stone-300 px-4 py-3 outline-none transition focus:border-emerald-700"
          />
        </div>

        {error && <p className="rounded-2xl bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</p>}

        <button
          type="submit"
          className="w-full rounded-full bg-emerald-900 px-4 py-3 text-sm font-semibold text-white shadow-lg shadow-emerald-900/20"
        >
          로그인
        </button>
      </form>

      <p className="mt-6 text-center text-sm text-stone-600">
        아직 회원이 아니면{' '}
        <Link to="/signup" className="font-semibold text-emerald-900">
          회원가입
        </Link>
      </p>
    </div>
  )
}