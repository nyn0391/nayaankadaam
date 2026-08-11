import React, { useEffect, useState } from 'react'
import { Box, Button, TextField, Typography, Paper, Grid } from '@mui/material'
import * as layoutsApi from '../../services/seatLayouts'

export default function SeatLayoutEditor() {
  const [name, setName] = useState('')
  const [content, setContent] = useState('')
  const [format, setFormat] = useState<'json'|'svg'>('json')
  const [layouts, setLayouts] = useState<any[]>([])

  useEffect(() => { fetchLayouts() }, [])

  async function fetchLayouts() {
    const data = await layoutsApi.listSeatLayouts()
    setLayouts(data)
  }

  async function handleSave() {
    try {
      const payload = { name, description: '', format, content }
      await layoutsApi.createSeatLayout(payload)
      await fetchLayouts()
      setName('')
      setContent('')
    } catch (e) { alert('Failed to save') }
  }

  return (
    <Box>
      <Typography variant="h6">Seat Layout Editor</Typography>
      <Paper sx={{ p: 2, mt: 2 }}>
        <TextField label="Name" value={name} onChange={(e) => setName(e.target.value)} fullWidth sx={{ mb: 2 }} />
        <TextField label="Content (JSON or SVG)" value={content} onChange={(e) => setContent(e.target.value)} fullWidth multiline rows={10} sx={{ mb: 2 }} />
        <Button variant="contained" onClick={handleSave}>Save layout</Button>
      </Paper>

      <Typography variant="h6" sx={{ mt: 3 }}>Saved layouts</Typography>
      <Grid container spacing={2} sx={{ mt: 1 }}>
        {layouts.map((l:any) => (
          <Grid item key={l.id} xs={12} sm={6}>
            <Paper sx={{ p: 2 }}>
              <Typography>{l.name}</Typography>
              <Typography variant="caption">{l.format}</Typography>
            </Paper>
          </Grid>
        ))}
      </Grid>
    </Box>
  )
}
