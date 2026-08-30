-- LogiTrack IQ: esquema MySQL 8+. No elimina datos existentes.

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_usuarios_rol CHECK (rol IN ('ADMIN', 'EMPLEADO', 'AGENTE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS proveedores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    contacto VARCHAR(255),
    dias_entrega INT NOT NULL,
    CONSTRAINT chk_proveedores_dias_entrega CHECK (dias_entrega BETWEEN 1 AND 90)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS productos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    categoria VARCHAR(80) NOT NULL,
    precio DECIMAL(12,2) NOT NULL,
    proveedor_principal_id BIGINT NULL,
    KEY idx_productos_proveedor (proveedor_principal_id),
    CONSTRAINT chk_productos_precio CHECK (precio >= 0.01),
    CONSTRAINT fk_productos_proveedor FOREIGN KEY (proveedor_principal_id)
        REFERENCES proveedores(id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS bodegas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ubicacion VARCHAR(150) NOT NULL,
    capacidad INT NOT NULL,
    encargado_id BIGINT NOT NULL,
    KEY idx_bodegas_encargado (encargado_id),
    CONSTRAINT chk_bodegas_capacidad CHECK (capacidad > 0),
    CONSTRAINT fk_bodegas_encargado FOREIGN KEY (encargado_id)
        REFERENCES usuarios(id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inventarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bodega_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    stock INT NOT NULL,
    UNIQUE KEY uk_inventarios_bodega_producto (bodega_id, producto_id),
    CONSTRAINT chk_inventarios_stock CHECK (stock >= 0),
    CONSTRAINT fk_inventarios_bodega FOREIGN KEY (bodega_id)
        REFERENCES bodegas(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_inventarios_producto FOREIGN KEY (producto_id)
        REFERENCES productos(id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS movimientos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME(6) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    usuario_responsable_id BIGINT NOT NULL,
    bodega_origen_id BIGINT NULL,
    bodega_destino_id BIGINT NULL,
    KEY idx_movimientos_fecha (fecha),
    KEY idx_movimientos_tipo (tipo),
    CONSTRAINT chk_movimientos_tipo CHECK (tipo IN ('ENTRADA', 'SALIDA', 'TRANSFERENCIA')),
    CONSTRAINT chk_movimientos_bodegas CHECK (
        (tipo = 'ENTRADA' AND bodega_origen_id IS NULL AND bodega_destino_id IS NOT NULL)
        OR (tipo = 'SALIDA' AND bodega_origen_id IS NOT NULL AND bodega_destino_id IS NULL)
        OR (tipo = 'TRANSFERENCIA' AND bodega_origen_id IS NOT NULL AND bodega_destino_id IS NOT NULL AND bodega_origen_id <> bodega_destino_id)
    ),
    CONSTRAINT fk_movimientos_usuario FOREIGN KEY (usuario_responsable_id)
        REFERENCES usuarios(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_movimientos_origen FOREIGN KEY (bodega_origen_id)
        REFERENCES bodegas(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_movimientos_destino FOREIGN KEY (bodega_destino_id)
        REFERENCES bodegas(id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS detalle_movimientos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movimiento_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    KEY idx_detalle_movimientos_producto (producto_id),
    CONSTRAINT chk_detalle_movimientos_cantidad CHECK (cantidad > 0),
    CONSTRAINT fk_detalle_movimientos_movimiento FOREIGN KEY (movimiento_id)
        REFERENCES movimientos(id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_detalle_movimientos_producto FOREIGN KEY (producto_id)
        REFERENCES productos(id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ordenes_compra (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    proveedor_id BIGINT NOT NULL,
    bodega_destino_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    total DECIMAL(14,2) NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    creado_por_id BIGINT NOT NULL,
    pdf LONGBLOB NULL,
    fecha_generacion_pdf DATETIME(6) NULL,
    KEY idx_ordenes_estado (estado),
    CONSTRAINT chk_ordenes_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_ordenes_estado CHECK (estado IN ('BORRADOR', 'APROBADA', 'RECIBIDA', 'CANCELADA')),
    CONSTRAINT fk_ordenes_producto FOREIGN KEY (producto_id) REFERENCES productos(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_ordenes_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedores(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_ordenes_bodega FOREIGN KEY (bodega_destino_id) REFERENCES bodegas(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_ordenes_autor FOREIGN KEY (creado_por_id) REFERENCES usuarios(id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS resumenes_panel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE NOT NULL,
    contenido_json LONGTEXT NOT NULL,
    autor_id BIGINT NOT NULL,
    UNIQUE KEY uk_resumenes_panel_fecha (fecha),
    CONSTRAINT fk_resumenes_panel_autor FOREIGN KEY (autor_id)
        REFERENCES usuarios(id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS auditorias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_operacion VARCHAR(20) NOT NULL,
    fecha_hora DATETIME(6) NOT NULL,
    usuario VARCHAR(255) NOT NULL,
    entidad_afectada VARCHAR(255) NOT NULL,
    entidad_id BIGINT NOT NULL,
    valores_anteriores TEXT NULL,
    valores_nuevos TEXT NULL,
    KEY idx_auditorias_fecha (fecha_hora),
    KEY idx_auditorias_entidad (entidad_afectada, entidad_id),
    CONSTRAINT chk_auditorias_tipo CHECK (tipo_operacion IN ('INSERT', 'UPDATE', 'DELETE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
