import React from 'react'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { TextField, Button, Box, Typography, Alert } from '@mui/material'
import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'

const schema = yup.object({
  fullName: yup.string().required('Full name is required'),
  email: yup.string().email('Must be a valid email').required('Email is required'),
  mobile: yup.string().optional(),
  password: yup.string().min(8, 'Password must be at least 8 characters').required('Password is required')
}).required()

export default function RegisterPage() {
  const { register, handleSubmit, formState: { errors } } = useForm({ resolver: yupResolver(schema) })
  const auth = useAuth()
  const navigate = useNavigate()
  const [serverError, setServerError] = React.useState<string | null>(null)

  const onSubmit = async (data: any) => {
    setServerError(null)
    try {
      await auth.register(data.fullName, data.email, data.mobile, data.password)
      navigate('/')
    } catch (err: any) {
      setServerError(err?.response?.data?.error?.message || 'Registration failed')
    }
  }

  return (
    <Box maxWidth={480} mx="auto">
      <Typography variant="h5" gutterBottom>Register</Typography>
      {serverError && <Alert severity="error">{serverError}</Alert>}
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <TextField label="Full Name" fullWidth margin="normal" {...register('fullName')} error={!!errors.fullName} helperText={errors.fullName?.message as string} />
        <TextField label="Email" fullWidth margin="normal" {...register('email')} error={!!errors.email} helperText={errors.email?.message as string} />
        <TextField label="Mobile" fullWidth margin="normal" {...register('mobile')} error={!!errors.mobile} helperText={errors.mobile?.message as string} />
        <TextField label="Password" type="password" fullWidth margin="normal" {...register('password')} error={!!errors.password} helperText={errors.password?.message as string} />
        <Button type="submit" variant="contained" sx={{ mt: 2 }}>Register</Button>
      </form>
    </Box>
  )
}
