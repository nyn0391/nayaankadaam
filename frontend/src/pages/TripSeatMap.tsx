import React, { useEffect, useState, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Box, Button, Grid, Paper, Typography, CircularProgress } from '@mui/material'
import * as holdsApi from '../services/holds'

export default function TripSeatMap() {
  const { instanceId } = useParams()
  const navigate = useNavigate()
  const [seats, setSeats] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [myHold, setMyHold] = useState<{ holdToken: string, expiresAt: string, seats: string[] } | null>(null)
  const [countdown, setCountdown] = useState<number | null>(null)
  const timerRef = useRef<any>(null)

  useEffect(() => { loadSeats(); return cleanupTimer }, [instanceId])

  useEffect(() => {
    if (myHold && myHold.expiresAt) startTimer(myHold.expiresAt)
    else cleanupTimer()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [myHold])

  function cleanupTimer() {
    if (timerRef.current) clearInterval(timerRef.current)
    timerRef.current = null
    setCountdown(null)
  }

  function startTimer(expiresAt: string) {
    cleanupTimer()
    const expiry = new Date(expiresAt).getTime()
    timerRef.current = setInterval(() => {
      const now = Date.now()
      const diff = Math.max(0, Math.floor((expiry - now) / 1000))
      setCountdown(diff)
      if (diff <= 0) {
        // hold expired
        setMyHold(null)
        cleanupTimer()
        loadSeats()
      }
    }, 1000)
  }

  async function loadSeats() {
    if (!instanceId) return
    setLoading(true)
    setError('')
    try {
      const data = await holdsApi.getSeats(instanceId)
      setSeats(data)
    } catch (e:any) {
      setError(e?.response?.data?.error || 'Failed to load seats')
    } finally { setLoading(false) }
  }

  async function handleSeatClick(seatCode: string) {
    if (!instanceId) return
    setError('')
    const seat = seats.find(s => s.seatCode === seatCode)
    if (!seat) return
    if (seat.isBooked) return setError('Seat already booked')
    // if we already hold this seat, release it
    if (myHold && myHold.seats.includes(seatCode)) {
      try {
        await holdsApi.releaseHold(instanceId, myHold.holdToken)
        setMyHold(null)
        loadSeats()
      } catch (e:any) { setError(e?.response?.data?.error || 'Failed to release hold') }
      return
    }

    // create a hold for this single seat
    try {
      const res = await holdsApi.createHold(instanceId, [seatCode])
      setMyHold({ holdToken: res.holdToken, expiresAt: res.expiresAt, seats: res.seats })
      // optimistic update: mark seat as held by you locally
      setSeats(prev => prev.map(s => s.seatCode === seatCode ? { ...s, isHeldByMe: true } : s))
    } catch (e:any) {
      setError(e?.response?.data?.error || 'Failed to create hold')
      await loadSeats()
    }
  }

  async function handleConfirm() {
    if (!myHold) return
    setLoading(true)
    setError('')
    try {
      // in a real app payment flow would go here; we call confirm with empty payment info
      const res = await holdsApi.confirmHold(myHold.holdToken, {})
      // on success, navigate to booking details page or show confirmation
      navigate(`/bookings/${res.bookingId}`)
    } catch (e:any) {
      setError(e?.response?.data?.error || 'Failed to confirm booking')
      // reload seats
      loadSeats()
    } finally { setLoading(false) }
  }

  async function handleRelease() {
    if (!myHold || !instanceId) return
    setLoading(true)
    setError('')
    try {
      await holdsApi.releaseHold(instanceId, myHold.holdToken)
      setMyHold(null)
      loadSeats()
    } catch (e:any) { setError(e?.response?.data?.error || 'Failed to release hold') }
    finally { setLoading(false) }
  }

  return (
    <Box>
      <Typography variant="h6">Seat map</Typography>
      {loading && <CircularProgress />}
      {error && <Typography color="error">{error}</Typography>}

      <Paper sx={{ p: 2, mt: 2 }}>
        <Grid container spacing={1}>
          {seats.map((s:any) => (
            <Grid item key={s.seatCode}>
              <Button
                variant={s.isBooked ? 'contained' : (myHold && myHold.seats.includes(s.seatCode) ? 'outlined' : 'text')}
                color={s.isBooked ? 'error' : (myHold && myHold.seats.includes(s.seatCode) ? 'primary' : 'inherit')}
                onClick={() => handleSeatClick(s.seatCode)}
                disabled={s.isBooked}
                sx={{ minWidth: 72, minHeight: 48 }}
              >
                <div>{s.seatCode}</div>
                {s.isBooked && <div style={{ fontSize: 10 }}>BOOKED</div>}
                {!s.isBooked && myHold && myHold.seats.includes(s.seatCode) && <div style={{ fontSize: 10 }}>HELD</div>}
              </Button>
            </Grid>
          ))}
        </Grid>

        <Box sx={{ mt: 2 }}>
          {myHold ? (
            <>
              <Typography>Hold token: {myHold.holdToken}</Typography>
              <Typography>Seats: {myHold.seats.join(', ')}</Typography>
              <Typography>Expires in: {countdown !== null ? `${countdown}s` : '-'}</Typography>
              <Button variant="contained" onClick={handleConfirm} sx={{ mr: 2 }}>Confirm booking</Button>
              <Button variant="outlined" onClick={handleRelease}>Release hold</Button>
            </>
          ) : (
            <Typography>Select a seat to hold it for checkout.</Typography>
          )}
        </Box>
      </Paper>
    </Box>
  )
}
