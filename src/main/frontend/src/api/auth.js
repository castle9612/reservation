import { api, bootstrapCsrf } from './client'

export async function fetchMe() {
  const { data } = await api.get('/auth/me')
  return data?.data ?? { authenticated: false, userId: null, role: null }
}

export async function login(payload) {
  await bootstrapCsrf()

  const formData = new URLSearchParams()
  formData.append('userId', payload.userId)
  formData.append('password', payload.password)

  const { data } = await api.post('/auth/login', formData, {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
  })

  if (!data?.success) {
    throw new Error(data?.message || '로그인에 실패했습니다.')
  }

  return fetchMe()
}

export async function logout() {
  await bootstrapCsrf()
  const { data } = await api.post('/auth/logout')
  if (!data?.success) {
    throw new Error(data?.message || '로그아웃에 실패했습니다.')
  }
  return data
}

export async function signup(payload) {
  await bootstrapCsrf()
  const { data } = await api.post('/auth/signup', payload)
  if (!data?.success) {
    throw new Error(data?.message || '회원가입에 실패했습니다.')
  }
  return data
}
