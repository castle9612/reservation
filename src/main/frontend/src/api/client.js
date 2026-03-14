import axios from 'axios'

function getCookie(name) {
  const value = `; ${document.cookie}`
  const parts = value.split(`; ${name}=`)
  if (parts.length === 2) return parts.pop().split(';').shift()
  return null
}

export const api = axios.create({
  baseURL: '/api',
  withCredentials: true,
})

export async function bootstrapCsrf() {
  await api.get('/auth/csrf')
}

api.interceptors.request.use((config) => {
  const token = getCookie('XSRF-TOKEN')
  if (token) {
    config.headers['X-XSRF-TOKEN'] = token
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    const message =
      error?.response?.data?.message ||
      (status === 401
        ? '로그인이 필요합니다.'
        : status === 403
          ? '접근 권한이 없습니다.'
          : error?.code === 'ERR_NETWORK'
            ? '백엔드 서버에 연결할 수 없습니다.'
            : '요청 처리 중 오류가 발생했습니다.')
    return Promise.reject(new Error(message))
  },
)
