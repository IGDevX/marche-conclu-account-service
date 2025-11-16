-- Debug: Check if professions data exists

-- 1. Check all users
SELECT id, keycloak_id, siret FROM users;

-- 2. Check all professions
SELECT * FROM professions;

-- 3. Check user_professions join table
SELECT * FROM user_professions;

-- 4. Check professions for user with keycloak_id '1177e41d-b1e0-4890-9dd4-d8ae2dce9917'
SELECT 
    u.id as user_id,
    u.keycloak_id,
    up.profession_id,
    p.id as prof_id,
    p.code,
    p.name_en,
    p.name_fr
FROM users u
LEFT JOIN user_professions up ON u.id = up.user_id
LEFT JOIN professions p ON up.profession_id = p.id
WHERE u.keycloak_id = '1177e41d-b1e0-4890-9dd4-d8ae2dce9917';

-- 5. Insert test data if needed (uncomment to run)
-- INSERT INTO user_professions (user_id, profession_id) 
-- VALUES (1, 1), (1, 2) 
-- ON CONFLICT DO NOTHING;
