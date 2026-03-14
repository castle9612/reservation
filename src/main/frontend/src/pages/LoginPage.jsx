import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate, Link } from 'react-router-dom'
import SurfaceCard from '../components/common/SurfaceCard'
import StatusMessage from '../components/common/StatusMessage'
import { login } from '../api/auth'

export default function LoginPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [form, setForm] = useState({ userId: '', password: '' })
  const [message, setMessage] = useState('')

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: async () => {
      setMessage('')
      await queryClient.invalidateQueries({ queryKey: ['me'] })
      navigate('/')
    },
    onError: (error) => setMessage(error.message || '로그인에 실패했습니다.'),
  })

  const onChange = (event) => {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  return (
    <div className="mx-auto max-w-lg">
      <SurfaceCard>
        <h1 className="text-3xl font-bold text-slate-900">로그인</h1>
        <form
          className="mt-8 space-y-4"
          onSubmit={(event) => {
            event.preventDefault()
            mutation.mutate(form)
          }}
        >
          <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">아이디</label>
            <input
              name="userId"
              value={form.userId}
              onChange={onChange}
              className="w-full rounded-2xl border border-slate-200 px-4 py-3 outline-none focus:border-slate-400"
              required
            />
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">비밀번호</label>
            <input
              type="password"
              name="password"
              value={form.password}
              onChange={onChange}
              className="w-full rounded-2xl border border-slate-200 px-4 py-3 outline-none focus:border-slate-400"
              required
            />
          </div>
          {message ? <StatusMessage type="error">{message}</StatusMessage> : null}
          <button
            type="submit"
            className="w-full rounded-full bg-slate-900 px-5 py-3 text-sm font-semibold text-white"
            disabled={mutation.isPending}
          >
            로그인
          </button>
        </form>
        <div className="mt-6 text-sm text-slate-600">
          아직 계정이 없으면{' '}
          <Link to="/signup" className="font-semibold text-slate-900">
            회원가입
          </Link>
        </div>
      </SurfaceCard>
    </div>
  )
}
