import React, { useEffect, useState } from 'react'
import { Box, Button, TextField, Typography, Paper, Grid } from '@mui/material'
import * as busesApi from '../../services/buses'
import * as layoutsApi from '../../services/seatLayouts'

export default function BusesPage() {
  const [model, setModel] = useState('')
  const [reg, setReg] = useState('')
  const [layoutId, setLayoutId] = useState('')
  const [layouts, setLayouts] = useState<any[]>([])
  const [buses, setBuses] = useState<any[]>([])

  useEffect(() => { fetchLayouts(); fetchBuses() }, [])

  async function fetchLayouts() { const data = await layoutsApi.listSeatLayouts(); setLayouts(data) }
  async function fetchBuses() { const data = await busesApi.listBuses(); setBuses(data) }

  async function handleCreate() {
    try {
      await busesApi.createBus({ model, registrationNumber: reg, seatLayoutId: layoutId })
      setModel(''); setReg(''); setLayoutId('')
      await fetchBuses()
    } catch (e) { alert('Failed') }
  }

  return (
    <Box>
      <Typography variant="h6">Buses</Typography>
      <Paper sx={{ p: 2, mt: 2 }}>
        <TextField label="Model" value={model} onChange={(e) => setModel(e.target.value)} sx={{ mr: 1 }} />
        <TextField label="Registration" value={reg} onChange={(e) => setReg(e.target.value)} sx={{ mr: 1 }} />
        <TextField label="Seat layout id" value={layoutId} onChange={(e) => setLayoutId(e.target.value)} sx={{ mr: 1 }} />
        <Button variant="contained" onClick={handleCreate}>Create Bus</Button>
      </Paper>

      <Typography variant="h6" sx={{ mt: 3 }}>Existing buses</Typography>
      <Grid container spacing={2} sx={{ mt: 1 }}>
        {buses.map((b:any) => (
          <Grid item key={b.id} xs={12} sm={6}>
            <Paper sx={{ p: 2 }}>
              <Typography>{b.model} — {b.registrationNumber}</Typography>
              <Typography variant="caption">layout: {b.seatLayoutId}</Typography>
            </Paper>
          </Grid>
        ))}
      </Grid>
    </Box>
  )
}
