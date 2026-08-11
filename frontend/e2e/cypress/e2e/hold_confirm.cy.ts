describe('Seat hold and confirm flow (E2E)', () => {
  const adminToken = 'admin-token'
  let instanceId = null

  it('prepares test data via admin API', () => {
    // create seat layout
    cy.request({
      method: 'POST',
      url: '/api/v1/seat-layouts',
      headers: { Authorization: `Bearer ${adminToken}` },
      body: { name: 'e2e-layout', format: 'json', content: JSON.stringify({ seats: [ { seatCode: '1A', row: 1, col: 1 }, { seatCode: '1B', row: 1, col: 2 } ] }) }
    }).then((res) => {
      expect(res.status).to.eq(200)
      const layout = res.body
      // create bus
      return cy.request({
        method: 'POST',
        url: '/api/v1/buses',
        headers: { Authorization: `Bearer ${adminToken}` },
        body: { model: 'e2e-bus', registrationNumber: 'E2E-1', seatLayoutId: layout.id, totalSeats: 2 }
      })
    }).then((res) => {
      expect(res.status).to.eq(200)
      const bus = res.body
      // create route
      return cy.request({
        method: 'POST',
        url: '/api/v1/routes',
        headers: { Authorization: `Bearer ${adminToken}` },
        body: { code: 'R-E2E', origin: 'A', destination: 'B', stops: '[]' }
      }).then((r) => {
        const route = r.body
        // create template
        return cy.request({
          method: 'POST',
          url: '/api/v1/admin/templates',
          headers: { Authorization: `Bearer ${adminToken}` },
          body: { name: 'T-E2E', routeId: route.id, busId: bus.id, basePrice: 100 }
        })
      }).then((r) => {
        const template = r.body
        // create a one-off schedule rule for today and expand
        const today = new Date().toISOString().slice(0,10)
        return cy.request({
          method: 'POST',
          url: '/api/v1/admin/schedules',
          headers: { Authorization: `Bearer ${adminToken}` },
          body: { templateId: template.id, ruleType: 'ONE_OFF', startDate: today, timeOfDay: '12:00', timezone: 'UTC' }
        }).then((ruleRes) => {
          const rule = ruleRes.body
          return cy.request({
            method: 'POST',
            url: `/api/v1/admin/schedules/${rule.id}/expand?start=${today}&end=${today}`,
            headers: { Authorization: `Bearer ${adminToken}` }
          })
        })
      }).then((expandRes) => {
        expect(expandRes.status).to.eq(200)
        const created = expandRes.body.created
        expect(created).to.have.length.greaterThan(0)
        instanceId = created[0]
      })
    })
  })

  it('performs seat selection, hold and confirm via UI', () => {
    // visit the frontend seat map for the created instance
    cy.visit(`/trips/${instanceId}/seats`)
    // wait for seats to render
    cy.contains('Seat map')
    // click seat 1A
    cy.contains('1A').click()
    // click hold button
    cy.contains('Hold selected seats').click()
    // expect hold token visible
    cy.contains('Hold token').should('exist')
    // confirm booking
    cy.contains('Confirm booking').click()
    // expect redirect to booking page
    cy.url().should('include', '/bookings/')
  })
})
