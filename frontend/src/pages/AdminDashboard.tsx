import React from 'react'
import { Container, Typography, Paper } from '@mui/material'

export default function AdminDashboard() {
  return (
    <Container>
      <Paper sx={{ p: 3 }}>
        <Typography variant="h5" gutterBottom>Admin Dashboard</Typography>
        <Typography>Admin functions will appear here: users, operators, reports.</Typography>
      </Paper>
    </Container>
  )
}
