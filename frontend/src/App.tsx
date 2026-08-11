import React from 'react'
import { Routes, Route } from 'react-router-dom'
import Container from '@mui/material/Container'
import Home from './pages/Home'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import NavBar from './components/NavBar'
import ProtectedRoute from './components/ProtectedRoute'
import AdminDashboard from './pages/AdminDashboard'
import OperatorDashboard from './pages/OperatorDashboard'
import AdminUsers from './pages/AdminUsers'
import SeatMapPage from './pages/SeatMapPage'

export default function App() {
  return (
    <>
      <NavBar />
      <Container sx={{ mt: 4 }}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route path="/admin" element={
            <ProtectedRoute requiredRoles={["ADMIN"]}>
              <AdminDashboard />
            </ProtectedRoute>
          } />

          <Route path="/admin/users" element={
            <ProtectedRoute requiredRoles={["ADMIN"]}>
              <AdminUsers />
            </ProtectedRoute>
          } />

          <Route path="/operator" element={
            <ProtectedRoute requiredRoles={["BUS_OPERATOR"]}>
              <OperatorDashboard />
            </ProtectedRoute>
          } />

          <Route path="/trips/:tripId/seats" element={<SeatMapPage />} />

        </Routes>
      </Container>
    </>
  )
}
