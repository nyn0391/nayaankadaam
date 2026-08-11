-- V7__trip_templates_instances.sql
-- Add trip_templates, trip_instances, schedule_rules; add trip_instance_id to trip_seats and bookings

CREATE TABLE IF NOT EXISTS trip_templates (
  id UUID PRIMARY KEY,
  name VARCHAR(255),
  route_id UUID REFERENCES routes(id),
  bus_id UUID REFERENCES buses(id),
  base_price DOUBLE PRECISION,
  notes TEXT,
  created_by UUID,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS trip_instances (
  id UUID PRIMARY KEY,
  template_id UUID REFERENCES trip_templates(id),
  departure_at TIMESTAMP WITH TIME ZONE NOT NULL,
  arrival_at TIMESTAMP WITH TIME ZONE,
  status VARCHAR(32) DEFAULT 'SCHEDULED',
  instance_price DOUBLE PRECISION,
  timezone VARCHAR(64),
  created_by UUID,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS schedule_rules (
  id UUID PRIMARY KEY,
  template_id UUID REFERENCES trip_templates(id),
  rule_type VARCHAR(32) NOT NULL, -- ONE_OFF | DAILY | WEEKLY
  start_date DATE NOT NULL,
  end_date DATE,
  weekdays JSONB, -- for WEEKLY rule, e.g. ["MON","WED"]
  time_of_day VARCHAR(16), -- e.g. "08:30"
  timezone VARCHAR(64),
  created_by UUID,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Add trip_instance_id to trip_seats and bookings
ALTER TABLE trip_seats ADD COLUMN IF NOT EXISTS trip_instance_id UUID REFERENCES trip_instances(id);
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS trip_instance_id UUID REFERENCES trip_instances(id);
