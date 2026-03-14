import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import Container from '../components/ui/Container'
import InputField from '../components/ui/InputField'
import { login } from '../api/auth'

export default function LoginPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [form, setForm] = useState({ userId: '', password: '' })
  const [errorMessage, setErrorMessage] = useState('')

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: async () => {
      setErrorMessage('')
      await queryClient.invalidateQueries({ queryKey: ['me'] })
      navigate('/')
    },
    onError: (error) => {
      setErrorMessage(error.message || '로그인에 실패했습니다.')
    },
  })

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    mutation.mutate(form)
  }

  return (
    <section className="pb-20 pt-10">
      <Container className="max-w-xl">
        <div className="glass-card soft-shadow rounded-[36px] border border-white/70 p-8 sm:p-10">
          <p className="text-sm font-semibold uppercase tracking-[0.28em] text-emerald-700">
            Member Login
          </p>
          <h1 className="mt-3 text-4xl font-semibold tracking-tight text-slate-900">
            다시 차분한 리듬으로
          </h1>
          <p className="mt-4 text-base leading-7 text-slate-600">
            회원 전용 예약과 내 예약 확인을 위해 로그인해 주세요.
          </p>

          <form className="mt-8 space-y-5" onSubmit={handleSubmit}>
            <InputField
              label="아이디"
              name="userId"
              value={form.userId}
              onChange={handleChange}
              placeholder="아이디 입력"
              required
            />
            <InputField
              label="비밀번호"
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              placeholder="비밀번호 입력"
              required
            />

            {errorMessage && (
              <div className="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
                {errorMessage}
              </div>
            )}

            <button
              type="submit"
              disabled={mutation.isPending}
              className="w-full rounded-full bg-slate-900 px-5 py-3 text-sm font-semibold text-white"
            >
              {mutation.isPending ? '로그인 중...' : '로그인'}
            </button>
          </form>

          <p className="mt-6 text-sm text-slate-600">
            아직 회원이 아니라면{' '}
            <Link to="/signup" className="font-semibold text-slate-900 underline underline-offset-4">
              회원가입
            </Link>
          </p>
        </div>
      </Container>
    </section>
  )
}
