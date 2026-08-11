import createAxiosInstance from './api'

export type HoldResponse = {
  holdToken?: string
  conflicts?: string[]
}

export async function holdSeats(tripId: string, seatCodes: string[], userId?: string) {
  const inst = createAxiosInstance()
  const res = await inst.post('/bookings/hold', { tripId, userId, seatCodes })
  return res.data as HoldResponse
}

export async function confirmHold(holdToken: string, paymentReference: string) {
  const inst = createAxiosInstance()
  const res = await inst.post('/bookings/confirm', { holdToken, paymentReference })
  return res.data
}

export async function cancelHold(holdToken: string) {
  const inst = createAxiosInstance()
  const res = await inst.post('/bookings/cancel', { holdToken })
  return res.data
}
