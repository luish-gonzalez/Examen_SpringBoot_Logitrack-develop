-- ============================================================
-- LogiTrack
-- Archivo: src/main/resources/data.sql
-- Base de datos: PostgreSQL
-- ============================================================
--
-- Credenciales de prueba:
--
-- Administrador:
-- username: admin
-- password: Admin123
--
-- Empleado:
-- username: empleado
-- password: Empleado123
--
-- Las contraseñas se almacenan cifradas con BCrypt.
-- ============================================================


-- ============================================================
-- Usuarios
-- ============================================================

INSERT INTO usuarios (
    id,
    nombre,
    username,
    password,
    rol,
    activo
)
VALUES
(
    1,
    'Administrador General',
    'admin',
    '$2y$10$pn9s0Dxp6M49WYYUOzuW1evoCblZA8EWlIZlz6lYF3UNKN7T7DH62',
    'ADMIN',
    TRUE
),
(
    2,
    'Laura Martínez',
    'empleado',
    '$2y$10$BH0AqEFTfN9CPA9PZ6364ebyk2bjCA3.Zh4RyysnKJSuadXXAehl.',
    'EMPLEADO',
    TRUE
),
(
    3,
    'Carlos Rodríguez',
    'carlos',
    '$2y$10$BH0AqEFTfN9CPA9PZ6364ebyk2bjCA3.Zh4RyysnKJSuadXXAehl.',
    'EMPLEADO',
    TRUE
),
(
    4,
    'Usuario Inactivo',
    'inactivo',
    '$2y$10$BH0AqEFTfN9CPA9PZ6364ebyk2bjCA3.Zh4RyysnKJSuadXXAehl.',
    'EMPLEADO',
    FALSE
);


-- ============================================================
-- Bodegas
-- ============================================================

INSERT INTO bodegas (
    id,
    nombre,
    ubicacion,
    capacidad,
    encargado_id
)
VALUES
(
    1,
    'Bodega Central',
    'Bogotá',
    5000,
    1
),
(
    2,
    'Bodega Norte',
    'Medellín',
    3500,
    2
),
(
    3,
    'Bodega Occidente',
    'Cali',
    3000,
    3
);


-- ============================================================
-- Productos
-- El stock no se almacena en esta tabla.
-- ============================================================

INSERT INTO productos (
    id,
    nombre,
    categoria,
    precio
)
VALUES
(
    1,
    'Computador portátil',
    'Tecnología',
    2500000.00
),
(
    2,
    'Monitor de 24 pulgadas',
    'Tecnología',
    850000.00
),
(
    3,
    'Silla ergonómica',
    'Muebles',
    620000.00
),
(
    4,
    'Caja de papel tamaño carta',
    'Oficina',
    145000.00
),
(
    5,
    'Taladro eléctrico',
    'Herramientas',
    390000.00
),
(
    6,
    'Kit de limpieza industrial',
    'Aseo',
    180000.00
);


-- ============================================================
-- Inventarios
--
-- Cada registro representa el stock de un producto en una
-- bodega específica.
--
-- Se incluyen algunos registros con stock inferior a 10 para
-- probar la consulta de productos con stock bajo.
-- ============================================================

INSERT INTO inventarios (
    id,
    bodega_id,
    producto_id,
    stock
)
VALUES
-- Bodega Central
(
    1,
    1,
    1,
    22
),
(
    2,
    1,
    2,
    12
),
(
    3,
    1,
    3,
    8
),
(
    4,
    1,
    4,
    50
),
(
    5,
    1,
    5,
    6
),
(
    6,
    1,
    6,
    18
),

-- Bodega Norte
(
    7,
    2,
    1,
    10
),
(
    8,
    2,
    2,
    19
),
(
    9,
    2,
    3,
    15
),
(
    10,
    2,
    4,
    30
),
(
    11,
    2,
    5,
    14
),
(
    12,
    2,
    6,
    7
),

-- Bodega Occidente
(
    13,
    3,
    1,
    5
),
(
    14,
    3,
    2,
    10
),
(
    15,
    3,
    3,
    9
),
(
    16,
    3,
    4,
    25
),
(
    17,
    3,
    5,
    12
),
(
    18,
    3,
    6,
    20
);


-- ============================================================
-- Movimientos
--
-- Los movimientos incluidos son registros históricos de
-- ejemplo. Los valores actuales de inventario representan las
-- existencias después de aplicar estos movimientos.
-- ============================================================

INSERT INTO movimientos (
    id,
    fecha,
    tipo,
    usuario_responsable_id,
    bodega_origen_id,
    bodega_destino_id
)
VALUES
(
    1,
    '2026-07-01 08:30:00',
    'ENTRADA',
    1,
    NULL,
    1
),
(
    2,
    '2026-07-03 10:15:00',
    'ENTRADA',
    2,
    NULL,
    2
),
(
    3,
    '2026-07-05 14:20:00',
    'SALIDA',
    2,
    1,
    NULL
),
(
    4,
    '2026-07-08 09:45:00',
    'TRANSFERENCIA',
    1,
    1,
    3
),
(
    5,
    '2026-07-10 16:10:00',
    'SALIDA',
    3,
    2,
    NULL
),
(
    6,
    '2026-07-12 11:30:00',
    'TRANSFERENCIA',
    2,
    2,
    1
),
(
    7,
    '2026-07-15 08:50:00',
    'ENTRADA',
    3,
    NULL,
    3
),
(
    8,
    '2026-07-18 13:25:00',
    'SALIDA',
    1,
    3,
    NULL
);


-- ============================================================
-- Detalles de los movimientos
-- ============================================================

INSERT INTO detalles_movimiento (
    id,
    movimiento_id,
    producto_id,
    cantidad
)
VALUES
-- Movimiento 1: entrada a Bodega Central
(
    1,
    1,
    1,
    30
),
(
    2,
    1,
    2,
    20
),
(
    3,
    1,
    3,
    15
),

-- Movimiento 2: entrada a Bodega Norte
(
    4,
    2,
    2,
    25
),
(
    5,
    2,
    4,
    40
),
(
    6,
    2,
    5,
    20
),

-- Movimiento 3: salida de Bodega Central
(
    7,
    3,
    1,
    5
),
(
    8,
    3,
    3,
    7
),

-- Movimiento 4: transferencia Central a Occidente
(
    9,
    4,
    1,
    5
),
(
    10,
    4,
    4,
    10
),

-- Movimiento 5: salida de Bodega Norte
(
    11,
    5,
    2,
    6
),
(
    12,
    5,
    5,
    6
),

-- Movimiento 6: transferencia Norte a Central
(
    13,
    6,
    2,
    3
),
(
    14,
    6,
    6,
    8
),

-- Movimiento 7: entrada a Bodega Occidente
(
    15,
    7,
    3,
    12
),
(
    16,
    7,
    6,
    25
),

-- Movimiento 8: salida de Bodega Occidente
(
    17,
    8,
    3,
    3
),
(
    18,
    8,
    6,
    5
);


-- ============================================================
-- Auditorías de ejemplo
--
-- Las auditorías futuras deben generarse automáticamente desde
-- la aplicación. Estos registros solamente permiten probar los
-- filtros y endpoints de consulta.
-- ============================================================

INSERT INTO auditorias (
    id,
    tipo_operacion,
    fecha_hora,
    usuario,
    entidad_afectada,
    entidad_id,
    valores_anteriores,
    valores_nuevos
)
VALUES
(
    1,
    'INSERT',
    '2026-07-01 08:00:00',
    'admin',
    'Bodega',
    1,
    NULL,
    '{"nombre":"Bodega Central","ubicacion":"Bogotá","capacidad":5000}'
),
(
    2,
    'INSERT',
    '2026-07-01 08:05:00',
    'admin',
    'Producto',
    1,
    NULL,
    '{"nombre":"Computador portátil","categoria":"Tecnología","precio":2500000.00}'
),
(
    3,
    'INSERT',
    '2026-07-01 08:30:00',
    'admin',
    'Movimiento',
    1,
    NULL,
    '{"tipo":"ENTRADA","bodegaDestinoId":1}'
),
(
    4,
    'UPDATE',
    '2026-07-06 10:20:00',
    'empleado',
    'Producto',
    2,
    '{"nombre":"Monitor de 24 pulgadas","categoria":"Tecnología","precio":800000.00}',
    '{"nombre":"Monitor de 24 pulgadas","categoria":"Tecnología","precio":850000.00}'
),
(
    5,
    'INSERT',
    '2026-07-08 09:45:00',
    'admin',
    'Movimiento',
    4,
    NULL,
    '{"tipo":"TRANSFERENCIA","bodegaOrigenId":1,"bodegaDestinoId":3}'
),
(
    6,
    'UPDATE',
    '2026-07-11 15:40:00',
    'carlos',
    'Bodega',
    3,
    '{"nombre":"Bodega Occidente","ubicacion":"Cali","capacidad":2500}',
    '{"nombre":"Bodega Occidente","ubicacion":"Cali","capacidad":3000}'
),
(
    7,
    'DELETE',
    '2026-07-14 12:10:00',
    'admin',
    'Producto',
    7,
    '{"nombre":"Producto temporal","categoria":"Prueba","precio":1000.00}',
    NULL
),
(
    8,
    'INSERT',
    '2026-07-18 13:25:00',
    'admin',
    'Movimiento',
    8,
    NULL,
    '{"tipo":"SALIDA","bodegaOrigenId":3}'
);


-- ============================================================
-- Ajuste de las secuencias de identidad
--
-- Como se insertaron identificadores manualmente, se actualizan
-- las secuencias para que los próximos registros generados por
-- PostgreSQL no repitan los mismos identificadores.
-- ============================================================

SELECT setval(
    pg_get_serial_sequence('usuarios', 'id'),
    COALESCE(
        (SELECT MAX(id) FROM usuarios),
        1
    ),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('bodegas', 'id'),
    COALESCE(
        (SELECT MAX(id) FROM bodegas),
        1
    ),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('productos', 'id'),
    COALESCE(
        (SELECT MAX(id) FROM productos),
        1
    ),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('inventarios', 'id'),
    COALESCE(
        (SELECT MAX(id) FROM inventarios),
        1
    ),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('movimientos', 'id'),
    COALESCE(
        (SELECT MAX(id) FROM movimientos),
        1
    ),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence(
        'detalles_movimiento',
        'id'
    ),
    COALESCE(
        (
            SELECT MAX(id)
            FROM detalles_movimiento
        ),
        1
    ),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('auditorias', 'id'),
    COALESCE(
        (SELECT MAX(id) FROM auditorias),
        1
    ),
    TRUE
);


------------------------>

SELECT * FROM usuarios;
SELECT * FROM bodegas;
SELECT * FROM productos;
SELECT * FROM inventarios;
SELECT * FROM movimientos;
SELECT * FROM detalles_movimiento;
SELECT * FROM auditorias;