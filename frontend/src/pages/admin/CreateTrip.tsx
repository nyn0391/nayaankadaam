import React, { useEffect, useState } from 'react'
import { Box, Button, TextField, Typography, Paper, Grid, MenuItem, Select, InputLabel, FormControl } from '@mui/material'
import * as busesApi from '../../services/buses'
import * as routesApi from '../../services/routes'
import * as adminTrips from '../../services/adminTrips'

export default function CreateTripPage() {
  const [buses, setBuses] = useState<any[]>([])
  const [routes, setRoutes] = useState<any[]>([])
  const [selectedBus, setSelectedBus] = useState('')
  const [selectedRoute, setSelectedRoute] = useState('')
  const [basePrice, setBasePrice] = useState(0)
  const [message, setMessage] = useState('')

  useEffect(() => { fetchData() }, [])

  async function fetchData() {
    const b = await busesApi.listBuses()
    const r = await routesApi.listRoutes()
    setBuses(b)
    setRoutes(r)
  }

  async function handleCreate() {
    try {
      if (!selectedBus || !selectedRoute) return setMessage('Select bus and route')
      const body = { busId: selectedBus, routeId: selectedRoute, basePrice }
      const res = await adminTrips.createTripAdmin(body)
      setMessage('Trip created: ' + res.tripId)
    } catch (e:any) { setMessage(e?.response?.data?.error || 'Failed to create trip') }
  }

  return (
    <Box>
      <Typography variant="h6">Create Trip (admin)</Typography>
      <Paper sx={{ p: 2, mt: 2 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={4}>
            <FormControl fullWidth>
              <InputLabel>Bus</InputLabel>
              <Select value={selectedBus} label="Bus" onChange={(e:any) => setSelectedBus(e.target.value)}>
                {buses.map(b => <MenuItem key={b.id} value={b.id}>{b.model} — {b.registrationNumber}</MenuItem>)}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={4}>
            <FormControl fullWidth>
              <InputLabel>Route</InputLabel>
              <Select value={selectedRoute} label="Route" onChange={(e:any) => setSelectedRoute(e.target.value)}>
                {routes.map(r => <MenuItem key={r.id} value={r.id}>{r.code} — {r.origin} → {r.destination}</MenuItem>)}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={4}>
            <TextField label="Base price" type="number" value={basePrice} onChange={(e) => setBasePrice(Number(e.target.value))} fullWidth />
          </Grid>
        </Grid>
        <Box sx={{ mt: 2 }}>
          <Button variant="contained" onClick={handleCreate}>Create Trip</Button>
        </Box>
        {message && <Typography sx={{ mt: 2 }}>{message}</Typography>}
      </Paper>
    </Box>
  )
}
