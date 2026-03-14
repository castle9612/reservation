import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import Container from '../components/ui/Container'
import InputField from '../components/ui/InputField'
import SelectField from '../components/ui/SelectField'
import { signup } from '../api/auth'

const initialForm = {
  userId: '',
  password: '',
  userName: '',
  userEmail: '',
  phoneNumber: '',
  gender: '',
  maritalStatus: false,
  birthdate: '',
  privacyConsent: true,
}

export default function SignupPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState(initialForm)
  const [errorMessage, setErrorMessage] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const mutation = useMutation({
    mutationFn: signup,
    onSuccess: (data) => {
      setErrorMessage('')
      setSuccessMessage(data?.message || '회원가입이 완료되었습니다.')
      setTimeout(() => navigate('/login'), 700)
    },
    onError: (error) => {
      setSuccessMessage('')
      setErrorMessage(error.message || '회원가입에 실패했습니다.')
    },
  })

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target
    setForm((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : name === 'maritalStatus' ? value === 'true' : value,
    }))
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    mutation.mutate(form)
  }

  return (
    <section className="pb-20 pt-10">
      <Container className="max-w-3xl">
        <div className="glass-card soft-shadow rounded-[36px] border border-white/70 p-8 sm:p-10">
          <p className="text-sm font-semibold uppercase tracking-[0.28em] text-emerald-700">
            Join Membership
          </p>
          <h1 className="mt-3 text-4xl font-semibold tracking-tight text-slate-900">
            편안한 예약을 위한 회원 등록
          </h1>

          <form className="mt-8 grid gap-5 md:grid-cols-2" onSubmit={handleSubmit}>
            <InputField
              label="아이디"
              name="userId"
              value={form.userId}
              onChange={handleChange}
              required
            />
            <InputField
              label="비밀번호"
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              required
            />
            <InputField
              label="이름"
              name="userName"
              value={form.userName}
              onChange={handleChange}
            />
            <InputField
              label="이메일"
              name="userEmail"
              type="email"
              value={form.userEmail}
              onChange={handleChange}
            />
            <InputField
              label="전화번호"
              name="phoneNumber"
              value={form.phoneNumber}
              onChange={handleChange}
              placeholder="01012345678"
            />
            <InputField
              label="생년월일"
              name="birthdate"
              type="date"
              value={form.birthdate}
              onChange={handleChange}
            />

            <SelectField
              label="성별"
              name="gender"
              value={form.gender}
              onChange={handleChange}
              options={[
                { value: '', label: '선택' },
                { value: 'MALE', label: '남성' },
                { value: 'FEMALE', label: '여성' },
              ]}
            />

            <SelectField
              label="결혼 여부"
              name="maritalStatus"
              value={String(form.maritalStatus)}
              onChange={handleChange}
              options={[
                { value: 'false', label: '미혼' },
                { value: 'true', label: '기혼' },
              ]}
            />

            <label className="md:col-span-2 flex items-center gap-3 rounded-2xl bg-white px-4 py-4">
              <input
                type="checkbox"
                name="privacyConsent"
                checked={Boolean(form.privacyConsent)}
                onChange={handleChange}
                className="h-4 w-4"
              />
              <span className="text-sm text-slate-700">
                개인정보 수집 및 이용에 동의합니다.
              </span>
            </label>

            {successMessage && (
              <div className="md:col-span-2 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
                {successMessage}
              </div>
            )}

            {errorMessage && (
              <div className="md:col-span-2 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
                {errorMessage}
              </div>
            )}

            <div className="md:col-span-2">
              <button
                type="submit"
                disabled={mutation.isPending}
                className="w-full rounded-full bg-slate-900 px-5 py-3 text-sm font-semibold text-white"
              >
                {mutation.isPending ? '처리 중...' : '회원가입'}
              </button>
            </div>
          </form>
        </div>
      </Container>
    </section>
  )
}
