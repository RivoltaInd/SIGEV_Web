-- Tabla de usuarios para el servicio de registro e inicio de sesion (AA5).
-- Ejecutar una sola vez en MySQL Workbench.
-- La contrasena NO se guarda en texto plano: se guarda "sal:hash" (PBKDF2-SHA256).
USE sigev_db;

CREATE TABLE IF NOT EXISTS usuario (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    correo      VARCHAR(120) NOT NULL UNIQUE,
    clave_hash  VARCHAR(255) NOT NULL,
    rol         VARCHAR(20)  NOT NULL DEFAULT 'Vendedor'
);
