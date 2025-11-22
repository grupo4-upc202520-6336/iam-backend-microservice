-- 1. Tabla de Roles
-- (Asumo que 'Role' tiene un ID numérico y un nombre,
-- basado en tus imágenes anteriores y el código de User.java)
CREATE TABLE roles (
                       id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(255) NOT NULL UNIQUE
);

-- 2. Tabla de Usuarios (¡LA MÁS IMPORTANTE!)
CREATE TABLE users (
    -- ID DEBE SER VARCHAR para que funcione con JWT y los demás servicios
                       id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                       created_at TIMESTAMP,
                       updated_at TIMESTAMP,

                       email VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(120) NOT NULL
);

-- 3. Tabla Pivote (Join Table)
CREATE TABLE user_roles (
    -- Llave foránea a users.id (VARCHAR)
                            user_id BIGINT NOT NULL,

    -- Llave foránea a roles.id (BIGINT)
                            role_id BIGINT NOT NULL,

    -- Llave primaria compuesta
                            PRIMARY KEY (user_id, role_id),

    -- Definición de las llaves foráneas
                            FOREIGN KEY (user_id) REFERENCES users(id),
                            FOREIGN KEY (role_id) REFERENCES roles(id)
);