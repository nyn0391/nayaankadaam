import React from 'react'
import { Container, Typography } from '@mui/material'

export default function Home() {
  return (
    <Container>
      <Typography variant="h4" gutterBottom>Welcome to BusGo (Frontend Skeleton)</Typography>
      <Typography>Use the navigation to register or login. Auth pages call backend APIs.</Typography>
    </Container>
  )
}
