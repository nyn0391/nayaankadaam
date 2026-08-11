import React, { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { Box, Button, Chip, MenuItem, Select, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Typography, Snackbar, Alert } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'

type Role = { id: string; name: string; description?: string }
type User = { id: string; fullName?: string; email?: string; mobile?: string; roles?: string[] }

export default function AdminUsers() {
  const auth = useAuth()
  const axios = auth.axiosInstance()
  const [users, setUsers] = useState<User[]>([])
  const [roles, setRoles] = useState<Role[]>([])
  const [assigning, setAssigning] = useState<Record<string, string>>({})
  const [msg, setMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null)

  useEffect(() => {
    fetchRoles()
    fetchUsers()
  }, [])

  async function fetchRoles() {
    try {
      const res = await axios.get('/admin/roles')
      const data = res.data
      if (data && data.data) setRoles(data.data)
    } catch (e: any) {
      setMsg({ type: 'error', text: 'Failed to fetch roles' })
    }
  }

  async function fetchUsers() {
    try {
      const res = await axios.get('/admin/users')
      setUsers(res.data || [])
    } catch (e: any) {
      setMsg({ type: 'error', text: 'Failed to fetch users' })
    }
  }

  const handleAssignChange = (userId: string, value: string) => {
    setAssigning(prev => ({ ...prev, [userId]: value }))
  }

  const assignRole = async (userId: string) => {
    const roleName = assigning[userId]
    if (!roleName) return setMsg({ type: 'error', text: 'Select a role to assign' })
    try {
      await axios.post(`/admin/users/${userId}/roles`, { roleName })
      setMsg({ type: 'success', text: 'Role assigned' })
      fetchUsers()
    } catch (e: any) {
      setMsg({ type: 'error', text: e?.response?.data?.error?.message || 'Failed to assign role' })
    }
  }

  const revokeRole = async (userId: string, roleName: string) => {
    try {
      await axios.delete(`/admin/users/${userId}/roles/${roleName}`)
      setMsg({ type: 'success', text: 'Role revoked' })
      fetchUsers()
    } catch (e: any) {
      setMsg({ type: 'error', text: e?.response?.data?.error?.message || 'Failed to revoke role' })
    }
  }

  return (
    <Box>
      <Typography variant="h5" gutterBottom>Users & Roles</Typography>
      <TableContainer component={Paper} sx={{ mb: 2 }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Roles</TableCell>
              <TableCell>Assign Role</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {users.map(u => (
              <TableRow key={u.id}>
                <TableCell>{u.fullName || '-'}</TableCell>
                <TableCell>{u.email || u.mobile}</TableCell>
                <TableCell>
                  {(u.roles || []).map(r => (
                    <Chip key={r} label={r} onDelete={() => revokeRole(u.id, r)} sx={{ mr: 1, mb: 1 }} />
                  ))}
                </TableCell>
                <TableCell>
                  <Select value={assigning[u.id] || ''} onChange={(e) => handleAssignChange(u.id, e.target.value)} displayEmpty sx={{ mr: 1, minWidth: 180 }}>
                    <MenuItem value="">Select role</MenuItem>
                    {roles.map(r => (
                      <MenuItem key={r.id} value={r.name}>{r.name}</MenuItem>
                    ))}
                  </Select>
                  <Button variant="contained" onClick={() => assignRole(u.id)}>Assign</Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Snackbar open={!!msg} autoHideDuration={4000} onClose={() => setMsg(null)}>
        {msg ? <Alert severity={msg.type}>{msg.text}</Alert> : null}
      </Snackbar>
    </Box>
  )
}
