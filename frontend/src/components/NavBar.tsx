import React from 'react'
import AppBar from '@mui/material/AppBar'
import Toolbar from '@mui/material/Toolbar'
import Typography from '@mui/material/Typography'
import Button from '@mui/material/Button'
import { Link as RouterLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function NavBar() {
  const auth = useAuth()
  const navigate = useNavigate()

  const handleLogout = async () => {
    await auth.logout()
    navigate('/')
  }

  return (
    <AppBar position="static">
      <Toolbar>
        <Typography variant="h6" component={RouterLink} to="/" sx={{ color: 'inherit', textDecoration: 'none', flexGrow: 1 }}>
          BusGo
        </Typography>
        {auth.user ? (
          <>
            <Typography sx={{ mr: 2 }}>{auth.user.fullName || auth.user.email}</Typography>
            {auth.user.roles?.includes('ADMIN') && <Button color="inherit" component={RouterLink} to="/admin">Admin</Button>}
            {auth.user.roles?.includes('BUS_OPERATOR') && <Button color="inherit" component={RouterLink} to="/operator">Operator</Button>}
            <Button color="inherit" onClick={handleLogout}>Logout</Button>
          </>
        ) : (
          <>
            <Button color="inherit" component={RouterLink} to="/login">Login</Button>
            <Button color="inherit" component={RouterLink} to="/register">Register</Button>
          </>
        )}
      </Toolbar>
    </AppBar>
  )
}
