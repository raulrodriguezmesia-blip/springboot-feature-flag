-- Initial data for Feature Flag Service
-- Insert admin user with BCrypt hashed password for "admin"
INSERT INTO app_user (username, password, roles, enabled) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrqjH9HjVQ4K8kqU0gVHqF3K2mG5K7W', 'ROLE_ADMIN', true),
('user', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrqjH9HjVQ4K8kqU0gVHqF3K2mG5K7W', 'ROLE_USER', true);
