INSERT INTO user_roles (user_id, role_id)
SELECT admin_users.user_id, unregistered_role.id
FROM user_roles admin_users
JOIN user_role_codes admin_role ON admin_role.id = admin_users.role_id
CROSS JOIN user_role_codes unregistered_role
WHERE admin_role.code = 'ADMIN'
  AND unregistered_role.code = 'UNREGISTERED_CLIENT'
ON CONFLICT (user_id, role_id) DO NOTHING;
