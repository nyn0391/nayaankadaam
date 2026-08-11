import React from 'react'
import { Container, Typography, Paper, Button } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'

export default function AdminDashboard() {
  return (
    <Container>
      <Paper sx={{ p: 3 }}>
        <Typography variant="h5" gutterBottom>Admin Dashboard</Typography>
        <Typography sx={{ mb: 2 }}>Admin functions will appear here: users, operators, reports.</Typography>
        <Button variant="contained" component={RouterLink} to="/admin/users">Manage Users</Button>
      </Paper>
    </Container>
  )
}
