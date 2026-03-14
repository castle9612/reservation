import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import SurfaceCard from '../components/common/SurfaceCard'
import StatusMessage from '../components/common/StatusMessage'
import { signup } from '../api/auth'

const initialForm = {
  userId: '',
  password: '',
  userName: '',
  userEmail: '',
  phoneNumber: '',
  gender: '',
  maritalStatus: '',
  birthdate: '',
  privacyConsent: true,
}

export default function SignupPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const mutation = useMutation({
    mutationFn: signup,
    onSuccess: () => {
      setError('')
      setSuccess('회원가입이 완료되었습니다.')
      setTimeout(() => navigate('/login'), 900)
    },
    onError: (err) => {
      setSuccess('')
      setError(err.message || '회원가입에 실패했습니다.')
    },
  })

  const onChange = (event) => {
    const { name, value, type, checked } = event.target
    setForm((prev) => ({ ...prev, [name]: type === 'checkbox' ? checked : value }))
  }

  return (
    <div className="mx-auto max-w-3xl">
      <SurfaceCard>
        <h1 className="text-3xl font-bold text-slate-900">회원가입</h1>
        <form
          className="mt-8 grid gap-4 md:grid-cols-2"
          onSubmit={(event) => {
            event.preventDefault()
            mutation.mutate(form)
          }}
        >
          <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">아이디</label>
            <input name="userId" value={form.userId} onChange={onChange} className="w-full rounded-2xl border border-slate-200 px-4 py-3" required />
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">비밀번호</label>
            <input type="password" name="password" value={form.password} onChange={onChange} className="w-full rounded-2xl border border-slate-200 px-4 py-3" required />
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">이름</label>
            <input name="userName" value={form.userName} onChange={onChange} className="w-full rounded-2xl border border-slate-200 px-4 py-3" required />
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">이메일</label>
            <input type="email" name="userEmail" value={form.userEmail} onChange={onChange} className="w-full rounded-2xl border border-slate-200 px-4 py-3" />
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">전화번호</label>
            <input name="phoneNumber" value={form.phoneNumber} onChange={onChange} className="w-full rounded-2xl border border-slate-200 px-4 py-3" required />
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">생년월일</label>
            <input type="date" name="birthdate" value={form.birthdate} onChange={onChange} className="w-full rounded-2xl border border-slate-200 px-4 py-3" />
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">성별</label>
            <select name="gender" value={form.gender} onChange={onChange} className="w-full rounded-2xl border border-slate-200 px-4 py-3">
              <option value="">선택</option>
              <option value="FEMALE">여성</option>
              <option value="MALE">남성</option>
            </select>
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">결혼여부</label>
            <select name="maritalStatus" value={form.maritalStatus} onChange={onChange} className="w-full rounded-2xl border border-slate-200 px-4 py-3">
              <option value="">선택</option>
              <option value="SINGLE">미혼</option>
              <option value="MARRIED">기혼</option>
            </select>
          </div>
          <label className="md:col-span-2 flex items-center gap-3 rounded-2xl bg-stone-100 px-4 py-4 text-sm text-slate-700">
            <input type="checkbox" name="privacyConsent" checked={form.privacyConsent} onChange={onChange} />
            개인정보 수집에 동의합니다.
          </label>
          {error ? <div className="md:col-span-2"><StatusMessage type="error">{error}</StatusMessage></div> : null}
          {success ? <div className="md:col-span-2"><StatusMessage type="success">{success}</StatusMessage></div> : null}
          <div className="md:col-span-2">
            <button type="submit" className="w-full rounded-full bg-slate-900 px-5 py-3 text-sm font-semibold text-white" disabled={mutation.isPending}>
              회원가입
            </button>
          </div>
        </form>
      </SurfaceCard>
    </div>
  )
}
