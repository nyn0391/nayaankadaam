import React from 'react'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { TextField, Button, Box, Typography, Alert } from '@mui/material'
import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'

const schema = yup.object({
  username: yup.string().required('Email or mobile is required'),
  password: yup.string().required('Password is required')
}).required()

export default function LoginPage() {
  const { register, handleSubmit, formState: { errors } } = useForm({ resolver: yupResolver(schema) })
  const auth = useAuth()
  const navigate = useNavigate()
  const [serverError, setServerError] = React.useState<string | null>(null)

  const onSubmit = async (data: any) => {
    setServerError(null)
    try {
      await auth.login(data.username, data.password)
      navigate('/')
    } catch (err: any) {
      setServerError(err?.response?.data?.error?.message || 'Login failed')
    }
  }

  return (
    <Box maxWidth={480} mx="auto">
      <Typography variant="h5" gutterBottom>Login</Typography>
      {serverError && <Alert severity="error">{serverError}</Alert>}
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <TextField label="Email or Mobile" fullWidth margin="normal" {...register('username')} error={!!errors.username} helperText={errors.username?.message as string} />
        <TextField label="Password" type="password" fullWidth margin="normal" {...register('password')} error={!!errors.password} helperText={errors.password?.message as string} />
        <Button type="submit" variant="contained" sx={{ mt: 2 }}>Login</Button>
      </form>
    </Box>
  )
}
