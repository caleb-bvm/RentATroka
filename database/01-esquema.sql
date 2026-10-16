-- RentATroka | MySQL 8.4 (compatible con MySQL 8.0.16 o posterior).
-- No borra bases de datos ni tablas existentes.
SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS rentatroka
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE rentatroka;

CREATE TABLE IF NOT EXISTS usuarios (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    nombre_usuario VARCHAR(50) NOT NULL,
    clave_hash VARCHAR(255) NOT NULL,
    CONSTRAINT uq_usuarios_nombre_usuario UNIQUE (nombre_usuario)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS clientes (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    documento VARCHAR(30) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    CONSTRAINT uq_clientes_documento UNIQUE (documento)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS vehiculos (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    placa VARCHAR(15) NOT NULL,
    marca VARCHAR(50) NOT NULL,
    modelo VARCHAR(50) NOT NULL,
    tarifa_diaria DECIMAL(10, 2) NOT NULL,
    CONSTRAINT uq_vehiculos_placa UNIQUE (placa),
    CONSTRAINT ck_vehiculos_tarifa CHECK (tarifa_diaria > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS alquileres (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT UNSIGNED NOT NULL,
    vehiculo_id INT UNSIGNED NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    tarifa_diaria_aplicada DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_alquileres_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_alquileres_vehiculo FOREIGN KEY (vehiculo_id)
        REFERENCES vehiculos (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_alquileres_fechas CHECK (fecha_fin > fecha_inicio),
    CONSTRAINT ck_alquileres_tarifa CHECK (tarifa_diaria_aplicada > 0),
    INDEX idx_alquileres_cliente (cliente_id),
    INDEX idx_alquileres_disponibilidad (vehiculo_id, fecha_inicio, fecha_fin)
) ENGINE=InnoDB;

