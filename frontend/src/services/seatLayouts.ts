import createAxiosInstance from './api'

export type SeatLayout = {
  id?: string
  name: string
  description?: string
  format: 'json' | 'svg'
  content: string
  meta?: any
}

export async function listSeatLayouts(accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.get('/seat-layouts')
  return res.data as SeatLayout[]
}

export async function createSeatLayout(payload: SeatLayout, accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.post('/seat-layouts', payload)
  return res.data as SeatLayout
}
