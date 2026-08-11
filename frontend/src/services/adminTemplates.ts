import createAxiosInstance from './api'

export type TripTemplate = {
  id?: string
  name?: string
  routeId?: string
  busId?: string
  basePrice?: number
  notes?: string
}

export async function listTemplates(accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.get('/admin/templates')
  return res.data as TripTemplate[]
}

export async function createTemplate(body: TripTemplate, accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.post('/admin/templates', body)
  return res.data
}
