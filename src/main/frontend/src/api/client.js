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
  const csrfToken = getCookie('XSRF-TOKEN')
  if (csrfToken) {
    config.headers['X-XSRF-TOKEN'] = csrfToken
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error?.response?.data?.message ||
      error?.message ||
      '요청 처리 중 오류가 발생했습니다.'
    return Promise.reject(new Error(message))
  }
)