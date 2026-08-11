import React, { useEffect, useState } from 'react'
import { Box, Button, TextField, Typography, Paper, Grid } from '@mui/material'
import * as routesApi from '../../services/routes'

export default function RoutesPage() {
  const [code, setCode] = useState('')
  const [origin, setOrigin] = useState('')
  const [destination, setDestination] = useState('')
  const [stops, setStops] = useState('')
  const [routes, setRoutes] = useState<any[]>([])

  useEffect(() => { fetchRoutes() }, [])

  async function fetchRoutes() { const data = await routesApi.listRoutes(); setRoutes(data) }

  async function handleCreate() {
    try {
      const payload = { code, origin, destination, stops: JSON.stringify(stops ? stops.split(',').map(s => s.trim()) : []) }
      await routesApi.createRoute(payload)
      setCode(''); setOrigin(''); setDestination(''); setStops('')
      await fetchRoutes()
    } catch (e) { alert('Failed to create route') }
  }

  return (
    <Box>
      <Typography variant="h6">Routes</Typography>
      <Paper sx={{ p: 2, mt: 2 }}>
        <TextField label="Code" value={code} onChange={(e) => setCode(e.target.value)} sx={{ mr: 1 }} />
        <TextField label="Origin" value={origin} onChange={(e) => setOrigin(e.target.value)} sx={{ mr: 1 }} />
        <TextField label="Destination" value={destination} onChange={(e) => setDestination(e.target.value)} sx={{ mr: 1 }} />
        <TextField label="Stops (comma separated)" value={stops} onChange={(e) => setStops(e.target.value)} sx={{ mr: 1, mt: 1 }} fullWidth />
        <Button variant="contained" onClick={handleCreate} sx={{ mt: 1 }}>Create Route</Button>
      </Paper>

      <Typography variant="h6" sx={{ mt: 3 }}>Existing routes</Typography>
      <Grid container spacing={2} sx={{ mt: 1 }}>
        {routes.map((r:any) => (
          <Grid item key={r.id} xs={12} sm={6}>
            <Paper sx={{ p: 2 }}>
              <Typography>{r.code} — {r.origin} → {r.destination}</Typography>
              <Typography variant="caption">stops: {r.stops}</Typography>
            </Paper>
          </Grid>
        ))}
      </Grid>
    </Box>
  )
}
