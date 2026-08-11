// users API client
import { createAxiosInstance } from './api'

export type User = {
  id: string
  fullName?: string
  email?: string
  mobile?: string
  roles?: string[]
}

export async function getCurrentUser(accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.get('/users/me')
  return res.data as User
}
