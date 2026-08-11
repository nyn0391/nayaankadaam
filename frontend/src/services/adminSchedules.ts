import createAxiosInstance from './api'

export type ScheduleRulePayload = {
  templateId: string
  ruleType: 'ONE_OFF' | 'DAILY' | 'WEEKLY'
  startDate: string // YYYY-MM-DD
  endDate?: string
  weekdays?: string // JSON string array e.g. '["MONDAY","WEDNESDAY"]'
  timeOfDay: string // '08:30'
  timezone?: string
}

export async function createSchedule(body: ScheduleRulePayload, accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.post('/admin/schedules', body)
  return res.data
}

export async function expandRule(ruleId: string, start: string, end: string, accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.post(`/admin/schedules/${ruleId}/expand?start=${start}&end=${end}`)
  return res.data
}

export async function expandAll(start: string, end: string, accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.post(`/admin/schedules/expandAll?start=${start}&end=${end}`)
  return res.data
}

export async function listRules(accessToken?: string) {
  const inst = createAxiosInstance(accessToken)
  const res = await inst.get('/admin/schedules')
  return res.data
}
