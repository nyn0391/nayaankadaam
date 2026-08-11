import { render, screen, fireEvent } from '@testing-library/react'
import React from 'react'
import SeatMapPage from '../pages/SeatMapPage'
import * as trips from '../services/trips'
import * as bookings from '../services/bookings'
import { BrowserRouter } from 'react-router-dom'

jest.mock('../services/trips')
jest.mock('../services/bookings')

test('seat map hold and confirm flow (smoke)', async () => {
  const tripId = '00000000-0000-0000-0000-000000000000'
  (trips.getTripSeats as jest.Mock).mockResolvedValue([
    { seatCode: 'A1', isBooked: false, price: 100 },
    { seatCode: 'A2', isBooked: false, price: 100 }
  ])
  (bookings.holdSeats as jest.Mock).mockResolvedValue({ holdToken: 'hold-123', expiresIn: 300 })
  (bookings.confirmHold as jest.Mock).mockResolvedValue({ bookingId: 'booking-1' })

  render(<BrowserRouter><SeatMapPage /></BrowserRouter>)

  // note: this is a smoke test; more detailed interaction tests require mocking hooks and params
  expect(await screen.findByText(/Seat selection/)).toBeInTheDocument()
})
