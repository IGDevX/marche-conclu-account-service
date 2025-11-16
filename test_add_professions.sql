-- Add professions to user with id=1 (the user with keycloak_id 06d05478-a77e-48f0-b5af-5bf7931da714)
-- Run this in your PostgreSQL database

INSERT INTO user_professions (user_id, profession_id) 
VALUES 
    (1, 1),  -- FARMER
    (1, 2),  -- ORGANIC_FARMER
    (1, 5)   -- CHEESEMAKER
ON CONFLICT DO NOTHING;
