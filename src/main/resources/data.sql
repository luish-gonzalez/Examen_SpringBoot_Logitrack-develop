-- LogiTrack IQ: semillas académicas MySQL 8+.
-- Ejecutar manualmente tras schema.sql. INSERT IGNORE evita duplicados.

INSERT IGNORE INTO usuarios (id, nombre, username, password, rol, activo) VALUES
 (1, 'Administrador Demo', 'admin', '$2y$10$pn9s0Dxp6M49WYYUOzuW1evoCblZA8EWlIZlz6lYF3UNKN7T7DH62', 'ADMIN', TRUE),
 (2, 'Agente Demo', 'agente', '$2y$10$BH0AqEFTfN9CPA9PZ6364ebyk2bjCA3.Zh4RyysnKJSuadXXAehl.', 'AGENTE', TRUE),
 (3, 'Empleado Demo', 'empleado', '$2y$10$BH0AqEFTfN9CPA9PZ6364ebyk2bjCA3.Zh4RyysnKJSuadXXAehl.', 'EMPLEADO', TRUE);

INSERT IGNORE INTO proveedores (id, nombre, contacto, dias_entrega) VALUES
 (1, 'Proveedor Industrial Andino', 'ventas@andino.demo', 10),
 (2, 'Suministros Nacionales', 'contacto@nacionales.demo', 5),
 (3, 'Tecnología Demo', 'soporte@tecnologia.demo', 7);

INSERT IGNORE INTO bodegas (id, nombre, ubicacion, capacidad, encargado_id) VALUES
 (1, 'Bodega Central', 'Bogotá', 500, 1),
 (2, 'Bodega Norte', 'Medellín', 300, 2),
 (3, 'Bodega Occidente', 'Cali', 250, 3);

INSERT IGNORE INTO productos (id, nombre, categoria, precio, proveedor_principal_id) VALUES
 (1, 'Filtro industrial', 'Mantenimiento', 120000.00, 1),
 (2, 'Guante de seguridad', 'Seguridad industrial', 25000.00, 2),
 (3, 'Sensor de temperatura', 'Tecnología', 85000.00, 3),
 (4, 'Material sin proveedor', 'Demostración', 15000.00, NULL);

-- Compatibilidad heredada: IQ calcula sus saldos únicamente desde movimientos.
INSERT IGNORE INTO inventarios (id, bodega_id, producto_id, stock) VALUES
 (1, 1, 1, 10), (2, 2, 2, 60), (3, 3, 3, 25), (4, 1, 4, 20);

-- Producto 1: entrada 100 hace 20 días y salida 90 ayer; saldo 10.
INSERT IGNORE INTO movimientos
 (id, fecha, tipo, usuario_responsable_id, bodega_origen_id, bodega_destino_id) VALUES
 (1, CURRENT_TIMESTAMP - INTERVAL 20 DAY, 'ENTRADA', 1, NULL, 1),
 (2, CURRENT_TIMESTAMP - INTERVAL 1 DAY, 'SALIDA', 1, 1, NULL),
 (3, CURRENT_TIMESTAMP - INTERVAL 10 DAY, 'ENTRADA', 2, NULL, 2),
 (4, CURRENT_TIMESTAMP - INTERVAL 8 DAY, 'ENTRADA', 1, NULL, 3),
 (5, CURRENT_TIMESTAMP - INTERVAL 3 DAY, 'ENTRADA', 3, NULL, 1);

INSERT IGNORE INTO detalle_movimientos (id, movimiento_id, producto_id, cantidad) VALUES
 (1, 1, 1, 100), (2, 2, 1, 90), (3, 3, 2, 60),
 (4, 4, 3, 25), (5, 5, 4, 20);
