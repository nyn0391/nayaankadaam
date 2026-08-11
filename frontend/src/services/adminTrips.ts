import createAxiosInstance from './api'

export async function createTripAdmin(body: any, accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.post('/admin/trips', body)
  return res.data
}
