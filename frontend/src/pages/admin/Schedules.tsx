import React, { useEffect, useState } from 'react'
import { Box, Button, TextField, Typography, Paper, Grid, MenuItem, Select, InputLabel, FormControl } from '@mui/material'
import * as templatesApi from '../../services/adminTemplates'
import * as schedulesApi from '../../services/adminSchedules'

export default function SchedulesPage() {
  const [templates, setTemplates] = useState<any[]>([])
  const [rules, setRules] = useState<any[]>([])
  const [selectedTemplate, setSelectedTemplate] = useState('')
  const [ruleType, setRuleType] = useState<'ONE_OFF' | 'DAILY' | 'WEEKLY'>('DAILY')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [weekdays, setWeekdays] = useState('')
  const [timeOfDay, setTimeOfDay] = useState('08:30')
  const [timezone, setTimezone] = useState('UTC')
  const [message, setMessage] = useState('')

  useEffect(() => { fetchData() }, [])

  async function fetchData() {
    try {
      const t = await templatesApi.listTemplates()
      setTemplates(t)
      // list rules if endpoint exists; backend may not have GET /admin/schedules implemented — handle failures
      try {
        const r = await schedulesApi.listRules()
        setRules(r)
      } catch (e) {
        setRules([])
      }
    } catch (e) {
      setMessage('Failed to fetch templates')
    }
  }

  async function handleCreateRule() {
    try {
      if (!selectedTemplate) return setMessage('Select a template')
      const payload: any = {
        templateId: selectedTemplate,
        ruleType,
        startDate,
        timeOfDay,
        timezone
      }
      if (endDate) payload.endDate = endDate
      if (ruleType === 'WEEKLY') payload.weekdays = weekdays ? JSON.stringify(weekdays.split(',').map(s => s.trim().toUpperCase())) : '[]'
      const res = await schedulesApi.createSchedule(payload)
      setMessage('Schedule created')
      await fetchData()
    } catch (e:any) { setMessage(e?.response?.data?.error || 'Failed to create schedule') }
  }

  async function handleExpand(ruleId: string) {
    try {
      // ask for range
      const start = prompt('Start date (YYYY-MM-DD)', new Date().toISOString().slice(0,10))
      if (!start) return
      const end = prompt('End date (YYYY-MM-DD)', start)
      if (!end) return
      const res = await schedulesApi.expandRule(ruleId, start, end)
      setMessage(`Expanded: created ${res.createdCount}`)
      await fetchData()
    } catch (e:any) { setMessage(e?.response?.data?.error || 'Failed to expand rule') }
  }

  async function handleExpandAll() {
    try {
      const start = prompt('Start date (YYYY-MM-DD)', new Date().toISOString().slice(0,10))
      if (!start) return
      const end = prompt('End date (YYYY-MM-DD)', start)
      if (!end) return
      const res = await schedulesApi.expandAll(start, end)
      setMessage(`Expanded all: created ${res.createdCount}`)
      await fetchData()
    } catch (e:any) { setMessage(e?.response?.data?.error || 'Failed to expand all') }
  }

  return (
    <Box>
      <Typography variant="h6">Schedules</Typography>

      <Paper sx={{ p: 2, mt: 2 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <FormControl fullWidth>
              <InputLabel>Template</InputLabel>
              <Select value={selectedTemplate} label="Template" onChange={(e:any) => setSelectedTemplate(e.target.value)}>
                {templates.map(t => <MenuItem key={t.id} value={t.id}>{t.name || t.id}</MenuItem>)}
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12} sm={3}>
            <FormControl fullWidth>
              <InputLabel>Rule</InputLabel>
              <Select value={ruleType} label="Rule" onChange={(e:any) => setRuleType(e.target.value)}>
                <MenuItem value={'ONE_OFF'}>One-off</MenuItem>
                <MenuItem value={'DAILY'}>Daily</MenuItem>
                <MenuItem value={'WEEKLY'}>Weekly</MenuItem>
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12} sm={3}>
            <TextField label="Start date" type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} fullWidth InputLabelProps={{ shrink: true }} />
          </Grid>

          {ruleType !== 'ONE_OFF' && (
            <>
              <Grid item xs={12} sm={3}>
                <TextField label="End date" type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} fullWidth InputLabelProps={{ shrink: true }} />
              </Grid>
              <Grid item xs={12} sm={3}>
                <TextField label="Weekdays (comma)" value={weekdays} onChange={(e) => setWeekdays(e.target.value)} fullWidth placeholder="MONDAY,WEDNESDAY" />
              </Grid>
            </>
          )}

          <Grid item xs={12} sm={3}>
            <TextField label="Time of day (HH:mm)" value={timeOfDay} onChange={(e) => setTimeOfDay(e.target.value)} fullWidth />
          </Grid>

          <Grid item xs={12} sm={3}>
            <TextField label="Timezone" value={timezone} onChange={(e) => setTimezone(e.target.value)} fullWidth />
          </Grid>

          <Grid item xs={12}>
            <Button variant="contained" onClick={handleCreateRule}>Create Schedule</Button>
            <Button variant="outlined" onClick={handleExpandAll} sx={{ ml: 2 }}>Expand All</Button>
          </Grid>
        </Grid>

        {message && <Typography sx={{ mt: 2 }}>{message}</Typography>}
      </Paper>

      <Typography variant="h6" sx={{ mt: 3 }}>Existing rules</Typography>
      <Grid container spacing={2} sx={{ mt: 1 }}>
        {rules.map((r:any) => (
          <Grid item key={r.id} xs={12} sm={6}>
            <Paper sx={{ p: 2 }}>
              <Typography>{r.ruleType} — template: {r.templateId}</Typography>
              <Typography variant="caption">start: {r.startDate} end: {r.endDate || '-'}</Typography>
              <Typography variant="caption">time: {r.timeOfDay} tz: {r.timezone}</Typography>
              <Box sx={{ mt: 1 }}>
                <Button size="small" onClick={() => handleExpand(r.id)}>Expand</Button>
              </Box>
            </Paper>
          </Grid>
        ))}
      </Grid>
    </Box>
  )
}
