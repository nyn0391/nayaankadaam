import React, { createContext, useContext, useEffect, useMemo, useState } from 'react'
import createAxiosInstance from '../services/api'
import * as authApi from '../services/auth'
import { getCurrentUser } from '../services/users'
import type { User } from '../types/user'

type AuthContextType = {
  accessToken?: string | null
  refreshToken?: string | null
  user?: User | null
  login: (username: string, password: string) => Promise<void>
  register: (fullName: string, email: string, mobile: string | undefined, password: string) => Promise<void>
  logout: () => Promise<void>
  axiosInstance: typeof createAxiosInstance
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [accessToken, setAccessToken] = useState<string | null>(() => localStorage.getItem('accessToken'))
  const [refreshToken, setRefreshToken] = useState<string | null>(() => localStorage.getItem('refreshToken'))
  const [user, setUser] = useState<User | null>(() => {
    const raw = localStorage.getItem('user')
    return raw ? JSON.parse(raw) : null
  })

  // create axios instance bound to current access token
  const axiosInstance = useMemo(() => {
    const inst = createAxiosInstance(accessToken || undefined)

    // response interceptor to handle 401 by refreshing token
    inst.interceptors.response.use(
      (resp) => resp,
      async (error) => {
        const originalRequest = error.config
        if (error.response && error.response.status === 401 && refreshToken && !originalRequest._retry) {
          originalRequest._retry = true
          try {
            const data = await authApi.refreshToken(refreshToken)
            setAccessToken(data.accessToken)
            setRefreshToken(data.refreshToken)
            localStorage.setItem('accessToken', data.accessToken)
            localStorage.setItem('refreshToken', data.refreshToken)
            inst.defaults.headers.common['Authorization'] = `Bearer ${data.accessToken}`
            originalRequest.headers['Authorization'] = `Bearer ${data.accessToken}`
            // refresh local user as well
            try {
              const u = await getCurrentUser(data.accessToken)
              setUser(u)
              localStorage.setItem('user', JSON.stringify(u))
            } catch (e) { /* ignore */ }
            return inst(originalRequest)
          } catch (e) {
            // refresh failed; proceed to logout
            setAccessToken(null)
            setRefreshToken(null)
            setUser(null)
            localStorage.removeItem('accessToken')
            localStorage.removeItem('refreshToken')
            localStorage.removeItem('user')
            return Promise.reject(e)
          }
        }
        return Promise.reject(error)
      }
    )

    return inst
  }, [accessToken, refreshToken])

  useEffect(() => {
    // on initial load, if we have access token but no user, try to fetch
    const init = async () => {
      if (accessToken && !user) {
        try {
          const u = await getCurrentUser(accessToken)
          setUser(u)
          localStorage.setItem('user', JSON.stringify(u))
        } catch (e) {
          // ignore - tokens might be invalid
        }
      }
    }
    init()
  }, [])

  async function login(username: string, password: string) {
    const data = await authApi.login(username, password)
    setAccessToken(data.accessToken)
    setRefreshToken(data.refreshToken)
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    // fetch user
    try {
      const u = await getCurrentUser(data.accessToken)
      setUser(u)
      localStorage.setItem('user', JSON.stringify(u))
    } catch (e) { /* ignore */ }
  }

  async function register(fullName: string, email: string, mobile: string | undefined, password: string) {
    await authApi.register(fullName, email, mobile, password)
    // auto-login after register
    await login(email, password)
  }

  async function logout() {
    if (refreshToken) {
      try { await authApi.logoutServer(refreshToken) } catch (e) { /* ignore */ }
    }
    setAccessToken(null)
    setRefreshToken(null)
    setUser(null)
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }

  return (
    <AuthContext.Provider value={{ accessToken, refreshToken, user, login, register, logout, axiosInstance: createAxiosInstance }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
