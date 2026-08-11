-- V1__init.sql
-- Initial schema: roles, users (minimal) for bootstrap

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS roles (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR(50) NOT NULL UNIQUE,
  description TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  full_name VARCHAR(255),
  email VARCHAR(255) UNIQUE,
  mobile VARCHAR(32) UNIQUE,
  password_hash VARCHAR(255),
  enabled BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS user_roles (
  user_id UUID NOT NULL,
  role_id UUID NOT NULL,
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- seed basic roles
INSERT INTO roles (id, name, description)
VALUES
  (uuid_generate_v4(), 'CUSTOMER', 'End customer'),
  (uuid_generate_v4(), 'BUS_OPERATOR', 'Bus operator'),
  (uuid_generate_v4(), 'ADMIN', 'Administrator')
ON CONFLICT (name) DO NOTHING;
