import { useQuery } from '@tanstack/react-query'
import { fetchMe } from '../api/auth'

export function useAuth() {
  return useQuery({
    queryKey: ['me'],
    queryFn: fetchMe,
    retry: false,
    staleTime: 60 * 1000,
  })
}
