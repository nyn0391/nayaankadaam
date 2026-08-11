import createAxiosInstance from './api'

export type TripSeat = {
  seatCode: string
  isBooked: boolean
  price?: number
}

export async function getTripSeats(tripId: string, accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.get(`/trips/${tripId}/seats`)
  return res.data as TripSeat[]
}
