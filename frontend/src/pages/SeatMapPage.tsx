import React, { useEffect, useState, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Box, Button, Chip, CircularProgress, Grid, Paper, Typography, TextField, Alert } from '@mui/material'
import { getTripSeats, TripSeat } from '../services/trips'
import * as bookingsApi from '../services/bookings'
import { useAuth } from '../context/AuthContext'

export default function SeatMapPage() {
  const { tripId } = useParams()
  const [seats, setSeats] = useState<TripSeat[]>([])
  const [loading, setLoading] = useState(false)
  const [selected, setSelected] = useState<string[]>([])
  const [holdToken, setHoldToken] = useState<string | null>(null)
  const [paymentRef, setPaymentRef] = useState('')
  const [message, setMessage] = useState<{ type: 'success'|'error'|'info', text: string } | null>(null)
  const auth = useAuth()
  const navigate = useNavigate()
  const [expiresAt, setExpiresAt] = useState<number | null>(null)
  const intervalRef = useRef<number | null>(null)

  useEffect(() => { fetchSeats() }, [tripId])

  useEffect(() => {
    // auto-refresh seats every 10s
    const id = setInterval(() => fetchSeats(), 10000)
    return () => clearInterval(id)
  }, [tripId])

  useEffect(() => {
    // countdown for hold expiry
    let timer: any = null
    if (expiresAt) {
      timer = setInterval(() => {
        if (Date.now() > expiresAt) {
          setMessage({ type: 'info', text: 'Hold expired' })
          setHoldToken(null)
          setSelected([])
          setExpiresAt(null)
          fetchSeats()
        }
      }, 1000)
    }
    return () => { if (timer) clearInterval(timer) }
  }, [expiresAt])

  async function fetchSeats() {
    if (!tripId) return
    setLoading(true)
    try {
      const data = await getTripSeats(tripId, auth.accessToken || undefined)
      setSeats(data)
    } catch (e: any) {
      setMessage({ type: 'error', text: 'Failed to load seats' })
    } finally { setLoading(false) }
  }

  const toggleSelect = (code: string) => {
    if (selected.includes(code)) setSelected(prev => prev.filter(s => s !== code))
    else setSelected(prev => [...prev, code])
  }

  const handleHold = async () => {
    if (!tripId) return
    if (selected.length === 0) return setMessage({ type: 'info', text: 'Select at least one seat' })
    setLoading(true)
    try {
      const res = await bookingsApi.holdSeats(tripId, selected, auth.accessToken || undefined)
      if (res.holdToken) {
        setHoldToken(res.holdToken)
        if (res.expiresIn) {
          setExpiresAt(Date.now() + res.expiresIn * 1000)
        }
        setMessage({ type: 'success', text: 'Seats held. Proceed to confirm.' })
      } else if (res.conflicts && res.conflicts.length > 0) {
        setMessage({ type: 'error', text: `Conflicts: ${res.conflicts.join(',')}` })
        await fetchSeats()
      } else {
        setMessage({ type: 'error', text: 'Hold failed' })
      }
    } catch (e: any) {
      setMessage({ type: 'error', text: e?.response?.data?.message || 'Hold failed' })
    } finally { setLoading(false) }
  }

  const handleConfirm = async () => {
    if (!holdToken) return
    if (!paymentRef) return setMessage({ type: 'info', text: 'Enter payment reference' })
    setLoading(true)
    try {
      const res = await bookingsApi.confirmHold(holdToken, paymentRef, auth.accessToken || undefined)
      setMessage({ type: 'success', text: `Booking confirmed: ${res.bookingId}` })
      setSelected([])
      setHoldToken(null)
      setPaymentRef('')
      setExpiresAt(null)
      await fetchSeats()
      navigate('/')
    } catch (e: any) {
      setMessage({ type: 'error', text: e?.response?.data?.message || 'Confirm failed' })
      await fetchSeats()
    } finally { setLoading(false) }
  }

  const handleCancel = async () => {
    if (!holdToken) return
    setLoading(true)
    try {
      await bookingsApi.cancelHold(holdToken, auth.accessToken || undefined)
      setMessage({ type: 'info', text: 'Hold released' })
      setHoldToken(null)
      setSelected([])
      setExpiresAt(null)
      await fetchSeats()
    } catch (e: any) {
      setMessage({ type: 'error', text: 'Cancel failed' })
    } finally { setLoading(false) }
  }

  const formatRemaining = () => {
    if (!expiresAt) return null
    const diff = Math.max(0, Math.floor((expiresAt - Date.now()) / 1000))
    const m = Math.floor(diff / 60)
    const s = diff % 60
    return `${m}:${s.toString().padStart(2,'0')}`
  }

  return (
    <Box>
      <Typography variant="h5" gutterBottom>Seat selection for trip {tripId}</Typography>
      {message && <Alert severity={message.type} sx={{ mb: 2 }}>{message.text}</Alert>}
      {loading && <CircularProgress />}
      <Grid container spacing={1}>
        {seats.map(s => (
          <Grid item key={s.seatCode} xs={2} sm={1}>
            <Paper sx={{ p: 1, textAlign: 'center', cursor: s.isBooked ? 'not-allowed' : 'pointer', backgroundColor: s.isBooked ? '#ddd' : (selected.includes(s.seatCode) ? '#90caf9' : '#e8f5e9') }} onClick={() => { if (!s.isBooked && !holdToken) toggleSelect(s.seatCode) }}>
              <Typography variant="body2">{s.seatCode}</Typography>
              <Typography variant="caption">{s.price ? `₹${s.price}` : ''}</Typography>
            </Paper>
          </Grid>
        ))}
      </Grid>

      <Box sx={{ mt: 3 }}>
        {!holdToken ? (
          <>
            <Button variant="contained" onClick={handleHold} disabled={selected.length === 0}>Hold seats</Button>
          </>
        ) : (
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6">Checkout</Typography>
            <Typography>Seats: {selected.join(', ')}</Typography>
            {expiresAt && <Typography>Hold expires in: {formatRemaining()}</Typography>}
            <TextField label="Payment reference" value={paymentRef} onChange={(e) => setPaymentRef(e.target.value)} sx={{ mt: 1, mb: 1 }} fullWidth />
            <Box sx={{ display: 'flex', gap: 2 }}>
              <Button variant="contained" onClick={handleConfirm}>Confirm & Pay</Button>
              <Button variant="outlined" onClick={handleCancel}>Cancel Hold</Button>
            </Box>
          </Paper>
        )}
      </Box>
    </Box>
  )
}
