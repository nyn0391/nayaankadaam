import React from 'react'
import Container from '@mui/material/Container'
import Typography from '@mui/material/Typography'

export default function App() {
  return (
    <Container>
      <Typography variant="h4" component="h1" gutterBottom>
        BusGo — Coming Soon
      </Typography>
      <Typography>
        This is the frontend skeleton. Work will continue in phases: auth, buses, trips, seat selection, booking flow.
      </Typography>
    </Container>
  )
}
