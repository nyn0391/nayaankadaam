import createAxiosInstance from './api'

export type HoldResponse = {
  holdToken?: string
  conflicts?: string[]
  expiresIn?: number
}

export async function holdSeats(tripId: string, seatCodes: string[], accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.post('/bookings/hold', { tripId, seatCodes })
  return res.data as HoldResponse
}

export async function confirmHold(holdToken: string, paymentReference: string, accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.post('/bookings/confirm', { holdToken, paymentReference })
  return res.data
}

export async function cancelHold(holdToken: string, accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.post('/bookings/cancel', { holdToken })
  return res.data
}
