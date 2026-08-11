# Database design notes

Migrations are managed via Flyway (database/migrations). Initial migration creates core tables and roles.

Key entities to be added in future migrations:
- users, roles, user_roles
- operators, buses, seat_layouts, bus_seats
- routes, route_stops, trips, trip_stops, trip_seats
- bookings, booking_passengers
- payments, refunds
- coupons, coupon_usage

