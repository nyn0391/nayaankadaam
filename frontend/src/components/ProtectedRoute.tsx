import React from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

type Props = {
  children: React.ReactNode
  requiredRoles?: string[]
}

export default function ProtectedRoute({ children, requiredRoles }: Props) {
  const auth = useAuth()

  // not authenticated
  if (!auth.accessToken) {
    return <Navigate to="/login" replace />
  }

  // no role restriction
  if (!requiredRoles || requiredRoles.length === 0) {
    return <>{children}</>
  }

  const userRoles = auth.user?.roles || []
  const hasRole = requiredRoles.some(r => userRoles.includes(r))
  if (!hasRole) {
    return <div style={{ padding: 24 }}>
      <h2>Access Denied</h2>
      <p>You do not have permission to access this page.</p>
    </div>
  }

  return <>{children}</>
}
