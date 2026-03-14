import { api, bootstrapCsrf } from './client'

function unwrap(data) {
  return data?.data ?? data
}

export async function fetchMe() {
  const { data } = await api.get('/auth/me')
  const payload = unwrap(data)
  return {
    authenticated: Boolean(payload?.authenticated),
    userId: payload?.userId ?? null,
    role: payload?.role ?? null,
    name: payload?.name ?? null,
  }
}

export async function login(payload) {
  await bootstrapCsrf()
  const form = new URLSearchParams()
  form.append('userId', payload.userId)
  form.append('password', payload.password)

  const { data } = await api.post('/auth/login', form, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })

  const payloadData = unwrap(data)
  if (payloadData?.success === false) {
    throw new Error(payloadData?.message || '로그인에 실패했습니다.')
  }

  return fetchMe()
}

export async function signup(payload) {
  await bootstrapCsrf()
  const { data } = await api.post('/auth/signup', payload)
  return unwrap(data)
}

export async function logout() {
  await bootstrapCsrf()
  const { data } = await api.post('/auth/logout')
  return unwrap(data)
}
