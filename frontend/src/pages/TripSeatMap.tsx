import React, { useEffect, useState, useRef, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Box, Button, Grid, Paper, Typography, CircularProgress } from '@mui/material'
import * as holdsApi from '../services/holds'
import useSeatSocket from '../hooks/useSeatSocket'

export default function TripSeatMap() {
  const { instanceId } = useParams()
  const navigate = useNavigate()
  const [seats, setSeats] = useState<any[]>([])
  const [layout, setLayout] = useState<any>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [myHold, setMyHold] = useState<{ holdToken: string, expiresAt: string, seats: string[] } | null>(null)
  const [countdown, setCountdown] = useState<number | null>(null)
  const [selectedSeats, setSelectedSeats] = useState<string[]>([])
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

  const handleSeatEvent = useCallback((ev: any) => {
    if (!ev || !ev.seatCode) return
    setSeats(prev => prev.map(s => {
      if (s.seatCode !== ev.seatCode) return s
      if (ev.eventType === 'HELD') {
        // if this is our hold token, mark as heldByMe
        const heldByMe = myHold && ev.holdToken && myHold.holdToken === ev.holdToken
        if (heldByMe) {
          // update myHold expiry if necessary
          setMyHold(mh => mh ? { ...mh, expiresAt: ev.expiresAt } : mh)
        }
        return { ...s, held: true, heldByMe: heldByMe, expiresAt: ev.expiresAt }
      }
      if (ev.eventType === 'RELEASED') {
        // clear hold
        return { ...s, held: false, heldByMe: false, expiresAt: null }
      }
      if (ev.eventType === 'BOOKED') {
        // mark booked and remove hold-related flags
        // if this seat was in our hold, clear our hold
        if (myHold && myHold.seats.includes(ev.seatCode)) setMyHold(null)
        return { ...s, held: false, heldByMe: false, expiresAt: null, isBooked: true, bookingId: ev.bookingId }
      }
      return s
    }))
  }, [myHold])

  // connect to websocket for live seat events (no token passed here; add token if your app stores JWT)
  useSeatSocket(instanceId, null, handleSeatEvent)

  async function loadSeats() {
    if (!instanceId) return
    setLoading(true)
    setError('')
    try {
      const data = await holdsApi.getSeats(instanceId)
      setLayout(data.layout)
      setSeats(data.seats)
      // clear selection if seats changed
      setSelectedSeats([])
    } catch (e:any) {
      setError(e?.response?.data?.error || 'Failed to load seats')
    } finally { setLoading(false) }
  }

  function isSelectable(s:any) {
    return !s.isBooked && !(s.held && !s.heldByMe)
  }

  function toggleSelect(seatCode: string) {
    setSelectedSeats(prev => {
      if (prev.includes(seatCode)) return prev.filter(x => x !== seatCode)
      return [...prev, seatCode]
    })
  }

  async function handleSeatClick(seatCode: string) {
    if (!instanceId) return
    setError('')
    const seat = seats.find(s => s.seatCode === seatCode)
    if (!seat) return
    if (!isSelectable(seat)) return setError('Seat not selectable')
    // toggle selection locally
    toggleSelect(seatCode)
  }

  async function handleCreateHold() {
    if (!instanceId) return
    if (selectedSeats.length === 0) return setError('Select seats first')
    setLoading(true)
    setError('')
    try {
      const res = await holdsApi.createHold(instanceId, selectedSeats)
      setMyHold({ holdToken: res.holdToken, expiresAt: res.expiresAt, seats: res.seats })
      // mark selected seats as held by me
      setSeats(prev => prev.map(s => selectedSeats.includes(s.seatCode) ? { ...s, held: true, heldByMe: true } : s))
      setSelectedSeats([])
    } catch (e:any) {
      setError(e?.response?.data?.error || 'Failed to create hold')
      await loadSeats()
    } finally { setLoading(false) }
  }

  async function handleConfirm() {
    if (!myHold) return
    setLoading(true)
    setError('')
    try {
      const res = await holdsApi.confirmHold(myHold.holdToken, {})
      navigate(`/bookings/${res.bookingId}`)
    } catch (e:any) {
      setError(e?.response?.data?.error || 'Failed to confirm booking')
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

  // compute grid layout
  function renderGrid() {
    // if layout contains seats with row/col use that
    let positioned = false
    let maxRow = 0
    let maxCol = 0
    const mapping:any = {}
    if (layout && layout.seats && Array.isArray(layout.seats)) {
      layout.seats.forEach((s:any) => {
        if (s.row != null && s.col != null) {
          positioned = true
          maxRow = Math.max(maxRow, s.row)
          maxCol = Math.max(maxCol, s.col)
          mapping[s.seatCode] = { row: s.row, col: s.col }
        }
      })
    }

    if (positioned) {
      const gridItems:any[] = []
      for (let r = 1; r <= maxRow; r++) {
        for (let c = 1; c <= maxCol; c++) {
          const seatAt = Object.keys(mapping).find(k => mapping[k].row === r && mapping[k].col === c)
          if (seatAt) {
            const s = seats.find(x => x.seatCode === seatAt)
            gridItems.push(renderSeatButton(s))
          } else {
            gridItems.push(<div key={`${r}-${c}`} style={{ width: 72, height: 48 }} />)
          }
        }
      }
      return (<div style={{ display: 'grid', gridTemplateColumns: `repeat(${maxCol}, 80px)`, gap: 8 }}>{gridItems}</div>)
    }

    // fallback: simple grid with 6 columns
    const cols = 6
    return (<div style={{ display: 'grid', gridTemplateColumns: `repeat(${cols}, 80px)`, gap: 8 }}>{seats.map(s => renderSeatButton(s))}</div>)
  }

  function renderSeatButton(s:any) {
    const selected = selectedSeats.includes(s.seatCode)
    return (
      <Button key={s.seatCode}
        variant={s.isBooked ? 'contained' : (selected ? 'outlined' : 'text')}
        color={s.isBooked ? 'error' : (selected ? 'primary' : 'inherit')}
        onClick={() => handleSeatClick(s.seatCode)}
        disabled={s.isBooked || (s.held && !s.heldByMe)}
        sx={{ minWidth: 72, minHeight: 48 }}
      >
        <div>{s.seatCode}</div>
        {s.isBooked && <div style={{ fontSize: 10 }}>BOOKED</div>}
        {!s.isBooked && s.held && !s.heldByMe && <div style={{ fontSize: 10 }}>HELD</div>}
        {!s.isBooked && s.held && s.heldByMe && <div style={{ fontSize: 10 }}>HELD (you)</div>}
      </Button>
    )
  }

  return (
    <Box>
      <Typography variant="h6">Seat map</Typography>
      {loading && <CircularProgress />}
      {error && <Typography color="error">{error}</Typography>}

      <Paper sx={{ p: 2, mt: 2 }}>
        {renderGrid()}

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
            <>
              <Typography>Select seats to hold them for checkout.</Typography>
              <Button variant="contained" onClick={handleCreateHold} disabled={selectedSeats.length===0} sx={{ mt:1 }}>Hold selected seats ({selectedSeats.length})</Button>
            </>
          )}
        </Box>
      </Paper>
    </Box>
  )
}
