DROP TABLE IF EXISTS
    user_roles,
    roles,
    users;

CREATE TABLE IF NOT EXISTS users
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    last_name   VARCHAR(50) NOT NULL,
    email       VARCHAR(100) UNIQUE,
    password    VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS roles
(
    id          BIGSERIAL PRIMARY KEY,
    authority   VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_roles
(
    user_id BIGINT,
    role_id BIGINT
);

INSERT INTO users (name, last_name, email)
VALUES ('admin', 'admin', 'admin@admin.ru');

INSERT INTO roles (authority) values ('ROLE_ADMIN');
INSERT INTO roles (authority) values ('ROLE_USER');

INSERT INTO user_roles (role_id, user_id) VALUES (1, 1);