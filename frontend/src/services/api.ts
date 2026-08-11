import axios from 'axios'

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'

export const createAxiosInstance = (accessToken?: string) => {
  const instance = axios.create({
    baseURL: API_BASE,
    headers: {
      'Content-Type': 'application/json'
    }
  })

  if (accessToken) {
    instance.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`
  }

  return instance
}

export default createAxiosInstance()
