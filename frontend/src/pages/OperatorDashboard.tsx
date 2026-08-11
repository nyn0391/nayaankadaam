import React from 'react'
import { Container, Typography, Paper } from '@mui/material'

export default function OperatorDashboard() {
  return (
    <Container>
      <Paper sx={{ p: 3 }}>
        <Typography variant="h5" gutterBottom>Operator Dashboard</Typography>
        <Typography>Operator functions will appear here: buses, trips, bookings.</Typography>
      </Paper>
    </Container>
  )
}
