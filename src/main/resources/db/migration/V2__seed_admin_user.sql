INSERT INTO users (
    user_id,
    name,
    enrollment,
    role,
    email,
    disabled,
    password,
    first_access,
    user_tutorial,
    salt,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'Admin Inspectron',
    '00001',
    'ADMIN',
    'admin@inspectron.com',
    FALSE,
    '$2b$10$Z.uim9IX0l5Hjmp7rxqG0u4AbnYQzw/QkZQTLesAcD1r8crK941qC',
    FALSE,
    FALSE,
    '$2b$10$Z.uim9IX0l5Hjmp7rxqG0u',
    NOW(),
    NOW()
);

