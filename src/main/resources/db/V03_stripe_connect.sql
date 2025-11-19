-- Add Stripe Connect fields to users table

ALTER TABLE users 
ADD COLUMN stripe_account_id VARCHAR(255),
ADD COLUMN stripe_account_status VARCHAR(50),
ADD COLUMN stripe_onboarding_complete BOOLEAN DEFAULT FALSE;

-- Add index for efficient Stripe account lookups
CREATE INDEX idx_users_stripe_account_id ON users(stripe_account_id);