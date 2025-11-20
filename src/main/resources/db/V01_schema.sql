CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       keycloak_id UUID NOT NULL UNIQUE, -- Foreign key to Keycloak user
                       biography TEXT,
                       website VARCHAR(255),
                       facebook VARCHAR(255),
                       instagram VARCHAR(255),
                       linkedin VARCHAR(255),
    -- Producer-specific fields
                       siret VARCHAR(14),
                       organization_type VARCHAR(255),
                       installation_year INT,
                       employees_count INT,
    -- Restaurant-specific fields
                       service_type VARCHAR(255),
                       cuisine_type VARCHAR(255),
                       hygiene_certifications TEXT,
                       awards TEXT,
    -- Common fields
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Professions table (store both English and French names)
CREATE TABLE professions (
    id SERIAL PRIMARY KEY,
    code VARCHAR(100) UNIQUE,
    name_en VARCHAR(255),
    name_fr VARCHAR(255)
);

-- Join table for many-to-many relationship between users and professions
CREATE TABLE user_professions (
    user_id INT NOT NULL,
    profession_id INT NOT NULL,
    PRIMARY KEY (user_id, profession_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (profession_id) REFERENCES professions(id) ON DELETE CASCADE
);

-- Create a trigger to update the `updated_at` column on row updates
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();