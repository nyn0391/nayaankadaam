import { createAxiosInstance } from './api'

export type LoginResponse = {
  accessToken: string
  refreshToken: string
  tokenType?: string
}

export async function login(username: string, password: string) {
  const res = await createAxiosInstance().post('/auth/login', { username, password })
  return res.data as LoginResponse
}

export async function register(fullName: string, email: string, mobile: string | undefined, password: string) {
  const res = await createAxiosInstance().post('/auth/register', { fullName, email, mobile, password })
  return res.data
}

export async function refreshToken(refreshToken: string) {
  const res = await createAxiosInstance().post('/auth/refresh', { refreshToken })
  return res.data as LoginResponse
}

export async function logoutServer(refreshToken: string) {
  const res = await createAxiosInstance().post('/auth/logout', { refreshToken })
  return res.data
}
