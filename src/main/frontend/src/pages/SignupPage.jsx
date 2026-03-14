import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { signup } from '../api/auth'

const initialForm = {
  userId: '',
  password: '',
  userName: '',
  userEmail: '',
  phoneNumber: '',
  gender: 'NONE',
  maritalStatus: false,
  birthdate: '',
  privacyConsent: true,
}

export default function SignupPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState(initialForm)
  const [message, setMessage] = useState('')
  const [errorMessage, setErrorMessage] = useState('')

  const signupMutation = useMutation({
    mutationFn: signup,
    onSuccess: (data) => {
      setErrorMessage('')
      setMessage(data?.message || '회원가입이 완료되었습니다.')
      window.setTimeout(() => {
        navigate('/login')
      }, 800)
    },
    onError: (error) => {
      setMessage('')
      setErrorMessage(error.message || '회원가입에 실패했습니다.')
    },
  })

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target
    setForm((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }))
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    signupMutation.mutate(form)
  }

  return (
    <div className="mx-auto max-w-2xl rounded-2xl bg-white p-8 shadow-sm ring-1 ring-slate-200">
      <h1 className="text-2xl font-bold text-slate-900">회원가입</h1>
      <p className="mt-2 text-sm text-slate-600">
        백엔드 UserDTO 필드명에 맞춰 최소 가입 폼을 정리했다. 이름은 userName, 이메일은 userEmail, 전화번호는 phoneNumber다.
      </p>

      <form className="mt-6 grid gap-4 md:grid-cols-2" onSubmit={handleSubmit}>
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">아이디</label>
          <input
            type="text"
            name="userId"
            value={form.userId}
            onChange={handleChange}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:border-slate-500"
            required
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">비밀번호</label>
          <input
            type="password"
            name="password"
            value={form.password}
            onChange={handleChange}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:border-slate-500"
            required
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">이름</label>
          <input
            type="text"
            name="userName"
            value={form.userName}
            onChange={handleChange}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:border-slate-500"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">이메일</label>
          <input
            type="email"
            name="userEmail"
            value={form.userEmail}
            onChange={handleChange}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:border-slate-500"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">전화번호</label>
          <input
            type="text"
            name="phoneNumber"
            value={form.phoneNumber}
            onChange={handleChange}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:border-slate-500"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">생년월일</label>
          <input
            type="date"
            name="birthdate"
            value={form.birthdate}
            onChange={handleChange}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:border-slate-500"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">성별</label>
          <select
            name="gender"
            value={form.gender}
            onChange={handleChange}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 outline-none focus:border-slate-500"
          >
            <option value="NONE">선택 안 함</option>
            <option value="MALE">남성</option>
            <option value="FEMALE">여성</option>
          </select>
        </div>

        <div className="flex items-center gap-2 pt-7">
          <input
            id="maritalStatus"
            type="checkbox"
            name="maritalStatus"
            checked={form.maritalStatus}
            onChange={handleChange}
          />
          <label htmlFor="maritalStatus" className="text-sm text-slate-700">
            기혼 여부
          </label>
        </div>

        <div className="md:col-span-2 flex items-center gap-2">
          <input
            id="privacyConsent"
            type="checkbox"
            name="privacyConsent"
            checked={form.privacyConsent}
            onChange={handleChange}
          />
          <label htmlFor="privacyConsent" className="text-sm text-slate-700">
            개인정보 수집에 동의합니다.
          </label>
        </div>

        {message && (
          <div className="md:col-span-2 rounded-lg bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
            {message}
          </div>
        )}

        {errorMessage && (
          <div className="md:col-span-2 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">
            {errorMessage}
          </div>
        )}

        <div className="md:col-span-2">
          <button
            type="submit"
            disabled={signupMutation.isPending}
            className="w-full rounded-lg bg-slate-900 px-4 py-3 text-sm font-semibold text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {signupMutation.isPending ? '가입 처리 중...' : '회원가입'}
          </button>
        </div>
      </form>
    </div>
  )
}
