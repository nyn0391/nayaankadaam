-- V5__buses_routes_seat_layouts.sql
-- Adds seat_layouts, buses, routes tables and links trips to buses/routes

CREATE TABLE IF NOT EXISTS seat_layouts (
  id UUID PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  format VARCHAR(32) NOT NULL,
  content TEXT NOT NULL,
  meta JSONB,
  created_by UUID,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS buses (
  id UUID PRIMARY KEY,
  operator_id UUID,
  model VARCHAR(255),
  registration_number VARCHAR(128),
  seat_layout_id UUID REFERENCES seat_layouts(id),
  total_seats INTEGER,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS routes (
  id UUID PRIMARY KEY,
  code VARCHAR(64),
  origin VARCHAR(255),
  destination VARCHAR(255),
  stops JSONB,
  distance_km DOUBLE PRECISION,
  duration_minutes INTEGER,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- link trips to bus and route (nullable for existing rows)
ALTER TABLE trips
  ADD COLUMN IF NOT EXISTS bus_id UUID REFERENCES buses(id),
  ADD COLUMN IF NOT EXISTS route_id UUID REFERENCES routes(id);
