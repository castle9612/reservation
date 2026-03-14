import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { signup } from '../api/auth'

export default function SignupPage() {
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const [form, setForm] = useState({
    userId: '',
    password: '',
    userName: '',
    userEmail: '',
    phoneNumber: '',
    gender: '',
    maritalStatus: false,
    birthdate: '',
    privacyConsent: true,
    packageCount: 0,
    memo: '',
  })

  const mutation = useMutation({
    mutationFn: signup,
    onSuccess: () => {
      navigate('/login')
    },
    onError: (e) => {
      setError(e.message)
    },
  })

  return (
    <div className="mx-auto max-w-2xl rounded-[32px] border border-stone-200 bg-white p-8 shadow-[0_18px_50px_rgba(20,29,26,0.07)]">
      <p className="text-xs font-semibold uppercase tracking-[0.28em] text-emerald-700">Member signup</p>
      <h1 className="mt-3 text-3xl font-semibold text-stone-900">회원가입</h1>

      <form
        className="mt-8 grid gap-4 md:grid-cols-2"
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
            className="w-full rounded-2xl border border-stone-300 px-4 py-3 outline-none focus:border-emerald-700"
          />
        </div>

        <div>
          <label className="mb-2 block text-sm font-medium text-stone-700">비밀번호</label>
          <input
            type="password"
            value={form.password}
            onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))}
            className="w-full rounded-2xl border border-stone-300 px-4 py-3 outline-none focus:border-emerald-700"
          />
        </div>

        <div>
          <label className="mb-2 block text-sm font-medium text-stone-700">이름</label>
          <input
            value={form.userName}
            onChange={(e) => setForm((prev) => ({ ...prev, userName: e.target.value }))}
            className="w-full rounded-2xl border border-stone-300 px-4 py-3 outline-none focus:border-emerald-700"
          />
        </div>

        <div>
          <label className="mb-2 block text-sm font-medium text-stone-700">이메일</label>
          <input
            value={form.userEmail}
            onChange={(e) => setForm((prev) => ({ ...prev, userEmail: e.target.value }))}
            className="w-full rounded-2xl border border-stone-300 px-4 py-3 outline-none focus:border-emerald-700"
          />
        </div>

        <div>
          <label className="mb-2 block text-sm font-medium text-stone-700">전화번호</label>
          <input
            value={form.phoneNumber}
            onChange={(e) => setForm((prev) => ({ ...prev, phoneNumber: e.target.value }))}
            placeholder="01012345678"
            className="w-full rounded-2xl border border-stone-300 px-4 py-3 outline-none focus:border-emerald-700"
          />
        </div>

        <div>
          <label className="mb-2 block text-sm font-medium text-stone-700">생년월일</label>
          <input
            value={form.birthdate}
            onChange={(e) => setForm((prev) => ({ ...prev, birthdate: e.target.value }))}
            placeholder="1999-01-01"
            className="w-full rounded-2xl border border-stone-300 px-4 py-3 outline-none focus:border-emerald-700"
          />
        </div>

        <div className="md:col-span-2">
          <label className="flex items-center gap-2 text-sm text-stone-700">
            <input
              type="checkbox"
              checked={form.privacyConsent}
              onChange={(e) => setForm((prev) => ({ ...prev, privacyConsent: e.target.checked }))}
            />
            개인정보 수집 및 이용에 동의
          </label>
        </div>

        {error && (
          <div className="md:col-span-2">
            <p className="rounded-2xl bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</p>
          </div>
        )}

        <div className="md:col-span-2">
          <button
            type="submit"
            className="w-full rounded-full bg-emerald-900 px-4 py-3 text-sm font-semibold text-white"
          >
            회원가입
          </button>
        </div>
      </form>

      <p className="mt-6 text-center text-sm text-stone-600">
        이미 계정이 있으면{' '}
        <Link to="/login" className="font-semibold text-emerald-900">
          로그인
        </Link>
      </p>
    </div>
  )
}