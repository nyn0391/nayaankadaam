-- V8__seat_holds.sql
-- Create seat_holds table to support seat locking/holds

CREATE TABLE IF NOT EXISTS seat_holds (
  id UUID PRIMARY KEY,
  trip_instance_id UUID NOT NULL REFERENCES trip_instances(id),
  seat_code VARCHAR(64) NOT NULL,
  user_id UUID,
  hold_token VARCHAR(128) UNIQUE NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
  metadata JSONB
);

CREATE INDEX IF NOT EXISTS idx_seat_holds_instance_seat ON seat_holds(trip_instance_id, seat_code);
CREATE INDEX IF NOT EXISTS idx_seat_holds_expires_at ON seat_holds(expires_at);
CREATE INDEX IF NOT EXISTS idx_seat_holds_hold_token ON seat_holds(hold_token);
