import React, { createContext, useContext, useEffect, useMemo, useState } from 'react'
import createAxiosInstance from '../services/api'
import * as authApi from '../services/auth'

type AuthContextType = {
  accessToken?: string | null
  refreshToken?: string | null
  login: (username: string, password: string) => Promise<void>
  register: (fullName: string, email: string, mobile: string | undefined, password: string) => Promise<void>
  logout: () => Promise<void>
  axiosInstance: typeof createAxiosInstance
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [accessToken, setAccessToken] = useState<string | null>(() => localStorage.getItem('accessToken'))
  const [refreshToken, setRefreshToken] = useState<string | null>(() => localStorage.getItem('refreshToken'))

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
            return inst(originalRequest)
          } catch (e) {
            // refresh failed; proceed to logout
            setAccessToken(null)
            setRefreshToken(null)
            localStorage.removeItem('accessToken')
            localStorage.removeItem('refreshToken')
            return Promise.reject(e)
          }
        }
        return Promise.reject(error)
      }
    )

    return inst
  }, [accessToken, refreshToken])

  useEffect(() => {
    // ensure axios default auth header updated
    if (accessToken) {
      // no-op, axiosInstance created with header
    }
  }, [accessToken])

  async function login(username: string, password: string) {
    const data = await authApi.login(username, password)
    setAccessToken(data.accessToken)
    setRefreshToken(data.refreshToken)
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
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
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  return (
    <AuthContext.Provider value={{ accessToken, refreshToken, login, register, logout, axiosInstance: createAxiosInstance }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
