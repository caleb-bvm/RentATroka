SET NAMES utf8mb4;
USE rentatroka;

-- Cuenta de demostración: admin / Admin123!
-- Hash PBKDF2-HMAC-SHA256, 600000 iteraciones y sal individual.
-- Reejecutar los datos conserva los registros que ya existen.
INSERT INTO usuarios (id, nombre, nombre_usuario, clave_hash) VALUES
    (1, 'Administrador', 'admin', 'pbkdf2_sha256$600000$b/knvC+x6knMG1iRODZ2hw==$+qokPQfBs6o/OVl3vhYuIYnk6lGBhtBaIM7xkbCv8b8=')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO clientes (id, nombre, documento, telefono) VALUES
    (1, 'Ana Martínez', 'DOC-001', '7000-1001'),
    (2, 'Luis Hernández', 'DOC-002', '7000-1002'),
    (3, 'María López', 'DOC-003', '7000-1003')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO vehiculos (id, placa, marca, modelo, tarifa_diaria) VALUES
    (1, 'P001-001', 'Toyota', 'Hilux', 65.00),
    (2, 'P002-002', 'Nissan', 'Frontier', 60.00),
    (3, 'P003-003', 'Mitsubishi', 'L200', 62.50)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO alquileres (id, cliente_id, vehiculo_id, fecha_inicio, fecha_fin, tarifa_diaria_aplicada) VALUES
    (1, 1, 1, '2026-11-02', '2026-11-05', 65.00),
    (2, 2, 2, '2026-11-03', '2026-11-07', 60.00)
ON DUPLICATE KEY UPDATE id = id;

