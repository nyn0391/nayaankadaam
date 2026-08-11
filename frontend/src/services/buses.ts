import createAxiosInstance from './api'

export type Bus = {
  id?: string
  model?: string
  registrationNumber?: string
  seatLayoutId?: string
  totalSeats?: number
}

export async function listBuses(accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.get('/buses')
  return res.data as Bus[]
}

export async function createBus(payload: Bus, accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.post('/buses', payload)
  return res.data as Bus
}
