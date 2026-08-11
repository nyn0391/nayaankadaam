-- V6__admin_audit_columns.sql
-- Add created_by audit columns to buses, routes, and trips

ALTER TABLE buses ADD COLUMN IF NOT EXISTS created_by UUID;
ALTER TABLE routes ADD COLUMN IF NOT EXISTS created_by UUID;
ALTER TABLE trips ADD COLUMN IF NOT EXISTS created_by UUID;
