import createAxiosInstance from './api'

export type Seat = {
  id?: string
  seatCode?: string
  isBooked?: boolean
  bookingId?: string | null
}

export async function getSeats(instanceId: string, accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.get(`/trips/${instanceId}/seats`)
  return res.data as Seat[]
}

export async function createHold(instanceId: string, seats: string[], holdSeconds?: number, accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const body: any = { seats }
  if (holdSeconds) body.holdSeconds = holdSeconds
  const res = await inst.post(`/trips/${instanceId}/holds`, body)
  return res.data
}

export async function releaseHold(instanceId: string, holdToken: string, accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.delete(`/trips/${instanceId}/holds/${holdToken}`)
  return res.data
}

export async function extendHold(instanceId: string, holdToken: string, extraSeconds?: number, accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const body: any = {}
  if (extraSeconds) body.extraSeconds = extraSeconds
  const res = await inst.post(`/trips/${instanceId}/holds/${holdToken}/extend`, body)
  return res.data
}

export async function confirmHold(holdToken: string, paymentInfo: any = {}, accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.post(`/trips/holds/${holdToken}/confirm`, paymentInfo)
  return res.data
}
