
-- ============================================
-- PROFESSIONS
-- ============================================

-- Insert sample professions (code, english, french)
INSERT INTO professions (code, name_en, name_fr) VALUES
    ('FARMER', 'Farmer', 'Agriculteur'),
    ('ORGANIC_FARMER', 'Organic Farmer', 'Agriculteur Bio'),
    ('VITICULTOR', 'Vintner', 'Vigneron'),
    ('OLIVE_GROWER', 'Olive Grower', 'Oléiculteur'),
    ('CHEESEMAKER', 'Cheesemaker', 'Fromager');

/*
-- ============================================
-- USERS
-- ============================================

INSERT INTO users (
    id,
    keycloak_id,
    biography,
    website,
    facebook,
    instagram,
    linkedin,
    siret,
    organization_type,
    installation_year,
    employees_count,
    service_type,
    cuisine_type,
    hygiene_certifications,
    awards
) VALUES (
    1,
    '1f40acc3-6fa2-4599-a9ac-184773358935',
    'Passionate organic farmer dedicated to sustainable agriculture and local produce.',
    'https://www.fermeduvalvert.fr',
    'facebook.com/fermeduvalvert',
    '@fermeduvalvert',
    'linkedin.com/in/jean-dupont',
    '12345678901234',
    'Individual Enterprise',
    2015,
    5,
    NULL,
    NULL,
    'AB Certification, Organic Farming',
    'Best Organic Producer 2023'
);

-- ============================================
-- USER PROFESSIONS
-- ============================================

-- Link user to professions (assuming profession IDs based on insertion order)
INSERT INTO user_professions (user_id, profession_id) VALUES
    (1, 2); -- ORGANIC_FARMER
*/