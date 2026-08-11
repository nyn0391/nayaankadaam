import createAxiosInstance from './api'

export type Route = {
  id?: string
  code?: string
  origin?: string
  destination?: string
  stops?: any
  distanceKm?: number
  durationMinutes?: number
}

export async function listRoutes(accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.get('/routes')
  return res.data as Route[]
}

export async function createRoute(payload: Route, accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.post('/routes', payload)
  return res.data as Route
}
