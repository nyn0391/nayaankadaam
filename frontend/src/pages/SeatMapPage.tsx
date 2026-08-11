import React, { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Box, Button, CircularProgress, Grid, Paper, Typography, TextField, Alert, Dialog, DialogTitle, DialogContent, DialogActions, LinearProgress } from '@mui/material'
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
  const [initialExpiresIn, setInitialExpiresIn] = useState<number | null>(null)
  const [confirmOpen, setConfirmOpen] = useState(false)

  useEffect(() => { fetchSeats() }, [tripId])

  useEffect(() => {
    const id = setInterval(() => fetchSeats(), 10000)
    return () => clearInterval(id)
  }, [tripId])

  useEffect(() => {
    let timer: any = null
    if (expiresAt) {
      timer = setInterval(() => {
        if (Date.now() > expiresAt) {
          setMessage({ type: 'info', text: 'Hold expired' })
          setHoldToken(null)
          setSelected([])
          setExpiresAt(null)
          setInitialExpiresIn(null)
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
          setInitialExpiresIn(res.expiresIn)
          setExpiresAt(Date.now() + res.expiresIn * 1000)
        }
        setMessage({ type: 'success', text: 'Seats held. Proceed to confirm.' })
        // after hold, refresh seats which will now show held-by-you
        await fetchSeats()
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
      setInitialExpiresIn(null)
      await fetchSeats()
      setConfirmOpen(false)
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
      setInitialExpiresIn(null)
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

  const progressPercent = () => {
    if (!expiresAt || !initialExpiresIn) return 0
    const remaining = Math.max(0, (expiresAt - Date.now()) / 1000)
    return Math.max(0, Math.min(100, Math.floor((remaining / initialExpiresIn) * 100)))
  }

  return (
    <Box>
      <Typography variant="h5" gutterBottom>Seat selection for trip {tripId}</Typography>
      {message && <Alert severity={message.type} sx={{ mb: 2 }}>{message.text}</Alert>}
      {loading && <CircularProgress />}
      <Grid container spacing={1}>
        {seats.map(s => {
          const isHeldByOther = s.heldBy === 'other'
          const isHeldByYou = s.heldBy === 'you'
          const isBooked = s.isBooked
          const bg = isBooked ? '#ddd' : isHeldByOther ? '#ffd54f' : isHeldByYou ? '#90caf9' : (selected.includes(s.seatCode) ? '#64b5f6' : '#e8f5e9')
          return (
            <Grid item key={s.seatCode} xs={2} sm={1}>
              <Paper sx={{ p: 1, textAlign: 'center', cursor: (isBooked || isHeldByOther || !!holdToken) ? 'not-allowed' : 'pointer', backgroundColor: bg }} onClick={() => { if (!isBooked && !isHeldByOther && !holdToken) toggleSelect(s.seatCode) }}>
                <Typography variant="body2">{s.seatCode}</Typography>
                <Typography variant="caption">{s.price ? `₹${s.price}` : ''}</Typography>
                {isHeldByOther && <Typography variant="caption" color="textSecondary">Held</Typography>}
                {isHeldByYou && <Typography variant="caption" color="textSecondary">Held by you</Typography>}
              </Paper>
            </Grid>
          )
        })}
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
            {expiresAt && <Box sx={{ mt: 1, mb: 1 }}>
              <Typography variant="body2">Hold expires in: {formatRemaining()}</Typography>
              <LinearProgress variant="determinate" value={progressPercent()} sx={{ height: 8, borderRadius: 1 }} />
            </Box>}
            <TextField label="Payment reference" value={paymentRef} onChange={(e) => setPaymentRef(e.target.value)} sx={{ mt: 1, mb: 1 }} fullWidth />
            <Box sx={{ display: 'flex', gap: 2 }}>
              <Button variant="contained" onClick={() => setConfirmOpen(true)}>Confirm & Pay</Button>
              <Button variant="outlined" onClick={handleCancel}>Cancel Hold</Button>
            </Box>
          </Paper>
        )}
      </Box>

      <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)}>
        <DialogTitle>Confirm Booking</DialogTitle>
        <DialogContent>
          <Typography>Seats: {selected.join(', ')}</Typography>
          <Typography sx={{ mt: 1 }}>Payment reference: {paymentRef}</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmOpen(false)}>Back</Button>
          <Button variant="contained" onClick={handleConfirm}>Confirm & Pay</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
