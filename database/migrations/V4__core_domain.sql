-- V4__core_domain.sql
-- Core domain model: operators, buses, seat_layouts, bus_seats, routes, route_stops, trips, trip_stops, trip_seats, bookings, booking_passengers, payments

CREATE TABLE IF NOT EXISTS operators (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR(255) NOT NULL,
  contact_email VARCHAR(255),
  contact_phone VARCHAR(32),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS seat_layouts (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR(255) NOT NULL,
  meta JSONB,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS buses (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  operator_id UUID NOT NULL,
  registration_number VARCHAR(100) NOT NULL UNIQUE,
  model VARCHAR(255),
  seat_layout_id UUID,
  total_seats INTEGER DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE,
  FOREIGN KEY (seat_layout_id) REFERENCES seat_layouts(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS bus_seats (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  bus_id UUID NOT NULL,
  seat_code VARCHAR(50) NOT NULL,
  seat_row INTEGER,
  seat_col INTEGER,
  seat_type VARCHAR(50),
  is_available BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (bus_id) REFERENCES buses(id) ON DELETE CASCADE,
  UNIQUE (bus_id, seat_code)
);

CREATE TABLE IF NOT EXISTS routes (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR(255),
  origin VARCHAR(255),
  destination VARCHAR(255),
  distance_km NUMERIC,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS route_stops (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  route_id UUID NOT NULL,
  stop_order INTEGER NOT NULL,
  stop_name VARCHAR(255) NOT NULL,
  arrival_time TIME,
  departure_time TIME,
  FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS trips (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  route_id UUID NOT NULL,
  bus_id UUID NOT NULL,
  operator_id UUID NOT NULL,
  departure_at TIMESTAMP WITH TIME ZONE NOT NULL,
  arrival_at TIMESTAMP WITH TIME ZONE,
  base_price NUMERIC DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE,
  FOREIGN KEY (bus_id) REFERENCES buses(id) ON DELETE CASCADE,
  FOREIGN KEY (operator_id) REFERENCES operators(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS trip_seats (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  trip_id UUID NOT NULL,
  seat_code VARCHAR(50) NOT NULL,
  is_booked BOOLEAN DEFAULT FALSE,
  price NUMERIC DEFAULT 0,
  FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
  UNIQUE (trip_id, seat_code)
);

CREATE TABLE IF NOT EXISTS bookings (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  trip_id UUID NOT NULL,
  user_id UUID NOT NULL,
  status VARCHAR(50) NOT NULL,
  total_amount NUMERIC DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS booking_passengers (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  booking_id UUID NOT NULL,
  full_name VARCHAR(255) NOT NULL,
  seat_code VARCHAR(50) NOT NULL,
  fare NUMERIC DEFAULT 0,
  FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS payments (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  booking_id UUID NOT NULL,
  provider VARCHAR(100),
  provider_payment_id VARCHAR(255),
  amount NUMERIC DEFAULT 0,
  status VARCHAR(50),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

-- indexes
CREATE INDEX IF NOT EXISTS idx_trips_route_departure ON trips(route_id, departure_at);
CREATE INDEX IF NOT EXISTS idx_trip_seats_trip ON trip_seats(trip_id);
CREATE INDEX IF NOT EXISTS idx_bookings_user ON bookings(user_id);
