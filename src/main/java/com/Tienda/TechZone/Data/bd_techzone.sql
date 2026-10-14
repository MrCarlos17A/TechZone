--  TechZone - Base de datos completa (puerto 3307)

DROP DATABASE IF EXISTS BD_Techzone;
CREATE DATABASE BD_Techzone
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE BD_Techzone;

--  TABLAS

CREATE TABLE ubigeo (
    id_ubigeo BIGINT NOT NULL,
    departamento VARCHAR(50) NULL,
    provincia VARCHAR(50) NULL,
    distrito VARCHAR(50) NULL,
    vigente BOOLEAN NULL,
    PRIMARY KEY (id_ubigeo)
) ENGINE=InnoDB;

CREATE TABLE tipo_documento (
    id_tipo_documento BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(50) NULL,
    vigente BOOLEAN NULL,
    PRIMARY KEY (id_tipo_documento)
) ENGINE=InnoDB;

CREATE TABLE rol (
    id_rol BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(20) NULL,
    vigente BOOLEAN NULL,
    PRIMARY KEY (id_rol)
) ENGINE=InnoDB;

CREATE TABLE marca (
    id_marca BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(20) NULL,
    vigente BOOLEAN NULL,
    PRIMARY KEY (id_marca)
) ENGINE=InnoDB;

CREATE TABLE categoria (
    id_categoria BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(50) NULL,
    vigente BOOLEAN NULL,
    PRIMARY KEY (id_categoria)
) ENGINE=InnoDB;

CREATE TABLE medio_pago (
    id_medio_pago BIGINT NOT NULL,
    nombre VARCHAR(50) NULL,
    vigente BOOLEAN NULL,
    PRIMARY KEY (id_medio_pago)
) ENGINE=InnoDB;

CREATE TABLE persona (
    id_persona BIGINT NOT NULL AUTO_INCREMENT,
    nombres VARCHAR(100) NULL,
    apellido_paterno VARCHAR(50) NULL,
    apellido_materno VARCHAR(50) NULL,
    id_tipo_documento BIGINT NULL,
    numero_documento VARCHAR(20) NULL,
    telefono VARCHAR(15) NULL,
    direccion VARCHAR(255) NULL,
    referencia VARCHAR(255) NULL,
    id_ubigeo BIGINT NULL,
    fecha_nacimiento DATE NULL,
    fecha_registro TIMESTAMP NULL,
    vigente BOOLEAN NULL,
    PRIMARY KEY (id_persona),
    KEY idx_persona_tipo_documento (id_tipo_documento),
    KEY idx_persona_ubigeo (id_ubigeo),
    CONSTRAINT fk_persona_tipo_doc FOREIGN KEY (id_tipo_documento) REFERENCES tipo_documento (id_tipo_documento) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_persona_ubigeo FOREIGN KEY (id_ubigeo) REFERENCES ubigeo (id_ubigeo) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE proveedor (
    id_proveedor BIGINT NOT NULL AUTO_INCREMENT,
    razon_social VARCHAR(150) NULL,
    id_tipo_documento BIGINT NULL,
    numero_documento VARCHAR(20) NULL,
    telefono_contacto VARCHAR(20) NULL,
    correo VARCHAR(100) NULL,
    nombre_encargado VARCHAR(150) NULL,
    vigente BOOLEAN NULL,
    PRIMARY KEY (id_proveedor),
    KEY idx_proveedor_tipo_documento (id_tipo_documento),
    CONSTRAINT fk_proveedor_tipo_doc FOREIGN KEY (id_tipo_documento) REFERENCES tipo_documento (id_tipo_documento) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE cliente (
    id_cliente BIGINT NOT NULL AUTO_INCREMENT,
    id_persona BIGINT NULL,
    fecha_ultima_compra DATETIME NULL,
    total_gastado DECIMAL(10,2) NULL,
    vigente BOOLEAN NULL,
    PRIMARY KEY (id_cliente),
    KEY idx_cliente_persona (id_persona),
    CONSTRAINT fk_cliente_persona FOREIGN KEY (id_persona) REFERENCES persona (id_persona) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_cliente_total CHECK (total_gastado >= 0)
) ENGINE=InnoDB;

CREATE TABLE empleado (
    id_empleado BIGINT NOT NULL AUTO_INCREMENT,
    id_persona BIGINT NULL,
    fecha_contrato DATE NULL,
    salario DECIMAL(10,2) NULL,
    vigente BOOLEAN NULL,
    PRIMARY KEY (id_empleado),
    KEY idx_empleado_persona (id_persona),
    CONSTRAINT fk_empleado_persona FOREIGN KEY (id_persona) REFERENCES persona (id_persona) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_empleado_salario CHECK (salario >= 0)
) ENGINE=InnoDB;

CREATE TABLE usuario (
    id_usuario BIGINT NOT NULL AUTO_INCREMENT,
    id_persona BIGINT NULL,
    id_rol BIGINT NULL,
    correo VARCHAR(100) NULL,
    contrasena VARCHAR(60) NULL,
    fecha_ultimo_acceso DATETIME NULL,
    estado VARCHAR(20) NULL,
    PRIMARY KEY (id_usuario),
    UNIQUE KEY uk_usuario_correo (correo),
    UNIQUE KEY uk_usuario_persona (id_persona),
    KEY idx_usuario_rol (id_rol),
    CONSTRAINT fk_usuario_persona FOREIGN KEY (id_persona) REFERENCES persona (id_persona) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol (id_rol) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE producto (
    id_producto BIGINT NOT NULL AUTO_INCREMENT,
    id_marca BIGINT NULL,
    id_categoria BIGINT NULL,
    codigo_sku VARCHAR(50) NULL,
    nombre VARCHAR(255) NULL,
    descripcion TEXT NULL,
    caracteristicas LONGTEXT NULL,
    precio_venta DECIMAL(10,2) NULL,
    stock SMALLINT NULL,
    vigente BOOLEAN NULL,
    PRIMARY KEY (id_producto),
    KEY idx_producto_marca (id_marca),
    KEY idx_producto_categoria (id_categoria),
    CONSTRAINT fk_producto_marca FOREIGN KEY (id_marca) REFERENCES marca (id_marca) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_producto_precio CHECK (precio_venta > 0),
    CONSTRAINT chk_producto_stock CHECK (stock >= 0)
) ENGINE=InnoDB;

CREATE TABLE producto_imagen (
    id_producto_imagen BIGINT NOT NULL AUTO_INCREMENT,
    id_producto BIGINT NULL,
    url_imagen VARCHAR(255) NULL,
    es_principal BOOLEAN NULL,
    PRIMARY KEY (id_producto_imagen),
    KEY idx_imagen_producto (id_producto),
    CONSTRAINT fk_imagen_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE producto_proveedor (
    id_producto BIGINT NOT NULL,
    id_proveedor BIGINT NOT NULL,
    precio_compra DECIMAL(10,2) NULL,
    tiempo_pedido_dias TINYINT NULL,
    PRIMARY KEY (id_producto, id_proveedor),
    KEY idx_pp_proveedor (id_proveedor),
    CONSTRAINT fk_pp_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_pp_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedor (id_proveedor) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_pp_precio CHECK (precio_compra > 0),
    CONSTRAINT chk_pp_tiempo CHECK (tiempo_pedido_dias >= 0)
) ENGINE=InnoDB;

CREATE TABLE carrito (
    id_carrito BIGINT NOT NULL AUTO_INCREMENT,
    id_cliente BIGINT NULL,
    fecha_carrito DATETIME NULL,
    PRIMARY KEY (id_carrito),
    KEY idx_carrito_cliente (id_cliente),
    CONSTRAINT fk_carrito_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE detalle_carrito (
    id_detalle_carrito BIGINT NOT NULL AUTO_INCREMENT,
    id_carrito BIGINT NULL,
    id_producto BIGINT NULL,
    cantidad INT NULL,
    PRIMARY KEY (id_detalle_carrito),
    KEY idx_dc_carrito (id_carrito),
    KEY idx_dc_producto (id_producto),
    CONSTRAINT fk_dc_carrito FOREIGN KEY (id_carrito) REFERENCES carrito (id_carrito) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_dc_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_dc_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB;

CREATE TABLE venta (
    id_venta BIGINT NOT NULL AUTO_INCREMENT,
    id_cliente BIGINT NULL,
    id_empleado BIGINT NULL,
    estado VARCHAR(50) NULL,
    fecha_venta DATETIME NULL,
    subtotal DECIMAL(10,2) NULL,
    igv DECIMAL(10,2) NULL,
    total DECIMAL(10,2) NULL,
    direccion_envio VARCHAR(255) NULL,
    PRIMARY KEY (id_venta),
    KEY idx_venta_cliente (id_cliente),
    KEY idx_venta_empleado (id_empleado),
    CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_venta_empleado FOREIGN KEY (id_empleado) REFERENCES empleado (id_empleado) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_venta_totales CHECK (subtotal >= 0 AND igv >= 0 AND total > 0)
) ENGINE=InnoDB;

CREATE TABLE detalle_venta (
    id_detalle_venta BIGINT NOT NULL AUTO_INCREMENT,
    id_venta BIGINT NULL,
    id_producto BIGINT NULL,
    cantidad INT NULL,
    precio_unitario DECIMAL(10,2) NULL,
    sub_total_venta DECIMAL(10,2) NULL,
    PRIMARY KEY (id_detalle_venta),
    KEY idx_dv_venta (id_venta),
    KEY idx_dv_producto (id_producto),
    CONSTRAINT fk_dv_venta FOREIGN KEY (id_venta) REFERENCES venta (id_venta) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_dv_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_dv_cantidad CHECK (cantidad > 0 AND precio_unitario > 0 AND sub_total_venta > 0)
) ENGINE=InnoDB;

CREATE TABLE pago (
    id_pago BIGINT NOT NULL AUTO_INCREMENT,
    id_venta BIGINT NULL,
    id_medio_pago BIGINT NULL,
    monto_pago DECIMAL(10,2) NULL,
    fecha_pago DATETIME NULL,
    codigo_operacion VARCHAR(50) NULL,
    estado_pago VARCHAR(50) NULL,
    PRIMARY KEY (id_pago),
    KEY idx_pago_venta (id_venta),
    KEY idx_pago_medio_pago (id_medio_pago),
    CONSTRAINT fk_pago_venta FOREIGN KEY (id_venta) REFERENCES venta (id_venta) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_pago_medio_pago FOREIGN KEY (id_medio_pago) REFERENCES medio_pago (id_medio_pago) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_pago_monto CHECK (monto_pago > 0)
) ENGINE=InnoDB;

CREATE TABLE configuracion_sitio (
    id_config BIGINT NOT NULL,
    nombre_sitio VARCHAR(80) NULL,
    logo_url VARCHAR(255) NULL,
    tagline VARCHAR(160) NULL,
    email VARCHAR(100) NULL,
    telefono VARCHAR(20) NULL,
    horario VARCHAR(60) NULL,
    whatsapp VARCHAR(20) NULL,
    whatsapp_texto VARCHAR(60) NULL,
    facebook_url VARCHAR(255) NULL,
    instagram_url VARCHAR(255) NULL,
    copyright VARCHAR(120) NULL,
    libro_reclamaciones_url VARCHAR(255) NULL,
    PRIMARY KEY (id_config)
) ENGINE=InnoDB;

--  DATOS

INSERT INTO configuracion_sitio (id_config, nombre_sitio, logo_url, tagline, email, telefono, horario, whatsapp, whatsapp_texto, facebook_url, instagram_url, copyright, libro_reclamaciones_url) VALUES
(1, 'TechZone', '/Assets/IMG/Logo/TechZone.png', 'Tu tienda de tecnologia de confianza.', 'techzone@outlook.com', '+51 967 662 103', 'Lun - Dom, 09:00 a.m. - 21:00 p.m.', '+51 967 662 103', 'Atencion 24/7', 'https://www.facebook.com', 'https://www.instagram.com', 'TECHZONE. Todos los derechos reservados.', '#');

INSERT INTO tipo_documento (nombre, vigente) VALUES
('DNI', TRUE),
('RUC', TRUE),
('Pasaporte', TRUE);

INSERT INTO medio_pago (id_medio_pago, nombre, vigente) VALUES
(1, 'Efectivo', TRUE),
(2, 'Yape/Plin', TRUE),
(3, 'Tarjeta de Credito', TRUE),
(4, 'Tarjeta de Debito', TRUE),
(5, 'Transferencia', TRUE),
(6, 'PagoEfectivo', TRUE);

INSERT INTO ubigeo (id_ubigeo, departamento, provincia, distrito, vigente) VALUES
(150101, 'Lima', 'Lima', 'Lima', TRUE),
(150103, 'Lima', 'Lima', 'Ate', TRUE),
(150107, 'Lima', 'Lima', 'Brena', TRUE),
(150114, 'Lima', 'Lima', 'Comas', TRUE),
(150116, 'Lima', 'Lima', 'El Agustino', TRUE),
(150128, 'Lima', 'Lima', 'La Molina', TRUE),
(150130, 'Lima', 'Lima', 'La Victoria', TRUE),
(150141, 'Lima', 'Lima', 'Lince', TRUE),
(150143, 'Lima', 'Lima', 'Los Olivos', TRUE),
(150147, 'Lima', 'Lima', 'Miraflores', TRUE),
(150150, 'Lima', 'Lima', 'Pueblo Libre', TRUE),
(150154, 'Lima', 'Lima', 'Rimac', TRUE),
(150159, 'Lima', 'Lima', 'San Borja', TRUE),
(150161, 'Lima', 'Lima', 'San Isidro', TRUE),
(150163, 'Lima', 'Lima', 'San Juan de Lurigancho', TRUE),
(150167, 'Lima', 'Lima', 'San Martin de Porres', TRUE),
(150169, 'Lima', 'Lima', 'San Miguel', TRUE),
(150173, 'Lima', 'Lima', 'Santiago de Surco', TRUE),
(150177, 'Lima', 'Lima', 'Surquillo', TRUE),
(150178, 'Lima', 'Lima', 'Villa El Salvador', TRUE),
(150179, 'Lima', 'Lima', 'Villa Maria del Triunfo', TRUE),
(200101, 'Callao', 'Callao', 'Callao', TRUE),
(140101, 'Lambayeque', 'Chiclayo', 'Chiclayo', TRUE),
(140102, 'Lambayeque', 'Chiclayo', 'Cayalti', TRUE),
(140103, 'Lambayeque', 'Chiclayo', 'Chongoyape', TRUE),
(140104, 'Lambayeque', 'Chiclayo', 'Eten', TRUE),
(140105, 'Lambayeque', 'Chiclayo', 'Eten Puerto', TRUE),
(140106, 'Lambayeque', 'Chiclayo', 'Jose Leonardo Ortiz', TRUE),
(140107, 'Lambayeque', 'Chiclayo', 'La Victoria', TRUE),
(140108, 'Lambayeque', 'Chiclayo', 'Lagunas', TRUE),
(140109, 'Lambayeque', 'Chiclayo', 'Monsefu', TRUE),
(140110, 'Lambayeque', 'Chiclayo', 'Nueva Arica', TRUE),
(140111, 'Lambayeque', 'Chiclayo', 'Oyotun', TRUE),
(140112, 'Lambayeque', 'Chiclayo', 'Picsi', TRUE),
(140113, 'Lambayeque', 'Chiclayo', 'Pimentel', TRUE),
(140114, 'Lambayeque', 'Chiclayo', 'Reque', TRUE),
(140115, 'Lambayeque', 'Chiclayo', 'Santa Rosa', TRUE),
(140116, 'Lambayeque', 'Chiclayo', 'Sana', TRUE),
(140117, 'Lambayeque', 'Chiclayo', 'Catache', TRUE),
(140118, 'Lambayeque', 'Chiclayo', 'Chucay', TRUE),
(140119, 'Lambayeque', 'Chiclayo', 'Salas', TRUE),
(140120, 'Lambayeque', 'Chiclayo', 'San Jose', TRUE),
(140121, 'Lambayeque', 'Chiclayo', 'Tucume', TRUE),
(140122, 'Lambayeque', 'Chiclayo', 'Tuman', TRUE),
(140201, 'Lambayeque', 'Ferrenafe', 'Ferrenafe', TRUE),
(140202, 'Lambayeque', 'Ferrenafe', 'Canaris', TRUE),
(140203, 'Lambayeque', 'Ferrenafe', 'Incahuasi', TRUE),
(140204, 'Lambayeque', 'Ferrenafe', 'Manuel Antonio Mesones Muro', TRUE),
(140205, 'Lambayeque', 'Ferrenafe', 'Pitipo', TRUE),
(140206, 'Lambayeque', 'Ferrenafe', 'Pueblo Nuevo', TRUE),
(140301, 'Lambayeque', 'Lambayeque', 'Lambayeque', TRUE),
(140302, 'Lambayeque', 'Lambayeque', 'Chochope', TRUE),
(140303, 'Lambayeque', 'Lambayeque', 'Illimo', TRUE),
(140304, 'Lambayeque', 'Lambayeque', 'Jayanca', TRUE),
(140305, 'Lambayeque', 'Lambayeque', 'Mochumi', TRUE),
(140306, 'Lambayeque', 'Lambayeque', 'Morrope', TRUE),
(140307, 'Lambayeque', 'Lambayeque', 'Motupe', TRUE),
(140308, 'Lambayeque', 'Lambayeque', 'Olmos', TRUE),
(140309, 'Lambayeque', 'Lambayeque', 'Pacora', TRUE),
(140310, 'Lambayeque', 'Lambayeque', 'Salas', TRUE),
(140311, 'Lambayeque', 'Lambayeque', 'San Jose', TRUE),
(140312, 'Lambayeque', 'Lambayeque', 'Tucume', TRUE);

INSERT INTO marca (nombre, vigente) VALUES
('ASUS', TRUE),
('MSI', TRUE),
('GIGABYTE', TRUE),
('CORSAIR', TRUE),
('REDRAGON', TRUE),
('Logitech', TRUE),
('Kingston', TRUE),
('ALIENWARE', TRUE),
('TOSHIBA', TRUE),
('MICRONICS', TRUE);

INSERT INTO categoria (nombre, vigente) VALUES
('Monitores', TRUE),
('Tarjetas de Video', TRUE);

INSERT INTO rol (nombre, vigente) VALUES
('Administrador', TRUE),
('Trabajador', TRUE),
('Usuario', TRUE),
('Cliente', TRUE);

INSERT INTO persona (id_persona, nombres, apellido_paterno, apellido_materno, id_tipo_documento, numero_documento, telefono, direccion, referencia, id_ubigeo, fecha_nacimiento, fecha_registro, vigente) VALUES
(1, 'CARLOS FELIPE', 'ARROYO', 'VEGA', 1, '73234768', '967662103', 'Av. Santa Ana', 'Plaza de Armas', 140122, '2005-04-13', '2026-06-01 08:00:00', TRUE),
(2, 'MARIA ELENA', 'TORRES', 'RIOS', 1, '45123456', '987111222', 'Av. Los Incas 123', 'Frente al parque', 150114, '1992-07-21', '2026-06-01 08:15:00', TRUE),
(3, 'JUAN CARLOS', 'MENDOZA', 'SALAZAR', 1, '407654321', '912333444', 'Jr. Piura 456', 'Al costado de la farmacia', 150143, '1995-03-08', '2026-07-10 10:22:00', TRUE),
(4, 'ANA LUCIA', 'RAMIREZ', 'FLORES', 1, '46891234', '933555666', 'Av. Brasil 789', 'Edificio torre 2, depto 501', 150147, '1998-11-17', '2026-07-25 15:40:00', TRUE),
(5, 'PEDRO ALONSO', 'GUTIERREZ', 'VEGA', 1, '41234987', '944777888', 'Calle Los Pinos 321', 'Cerca al colegio', 150173, '1990-01-30', '2026-08-05 09:10:00', TRUE),
(6, 'SOFIA ANDREA', 'CASTRO', 'LOPEZ', 1, '47563210', '955999000', 'Av. Arequipa 654', 'Piso 3, oficina B', 150161, '2000-06-05', '2026-08-18 17:05:00', TRUE),
(7, 'LUIS MIGUEL', 'HERRERA', 'DIAZ', 1, '43987654', '966121314', 'Jr. Huallaga 987', 'Porton negro', 150101, '1997-09-12', '2026-09-20 11:35:00', TRUE),
(8, 'KARLA VANESSA', 'RIOS', 'PAUCAR', 1, '48112233', '977151617', 'Av. El Sol 147', 'Ultima casa de la cuadra', 150179, '2001-12-25', '2026-09-28 19:48:00', TRUE);

INSERT INTO cliente (id_cliente, id_persona, fecha_ultima_compra, total_gastado, vigente) VALUES
(1, 3, '2026-09-15 10:30:00', 932.50, TRUE),
(2, 4, '2026-09-28 16:45:00', 1425.44, TRUE),
(3, 5, '2026-10-03 11:15:00', 2938.20, TRUE),
(4, 6, '2026-10-08 18:05:00', 2191.14, TRUE);

INSERT INTO empleado (id_empleado, id_persona, fecha_contrato, salario, vigente) VALUES
(1, 2, '2026-06-01', 1850.00, TRUE),
(2, 1, '2026-06-01', 3500.00, TRUE);

INSERT INTO usuario (id_usuario, id_persona, id_rol, correo, contrasena, fecha_ultimo_acceso, estado) VALUES
(1, 1, 1, 'admin@techzone.com', '$2a$10$zzjCJ.NI6NDmpN7C0Lf3euytT0upaStLlY1sVNGwMb8b.uFWNqs06', '2026-10-14 08:30:00', 'activo'),
(2, 2, 2, 'maria.torres@techzone.com', '$2a$10$RV82hpBaeXX7z8rPBJL.CeVLf8qBhZAizvTqkFO6ADQTe4JVG45XG', '2026-10-14 08:45:00', 'activo'),
(3, 3, 4, 'juan.mendoza@gmail.com', '$2a$10$AqC2/QGeswk.PiJc7Bkhtekp69.rC.3M6IRVPkm3KY1BwowwGD37q', '2026-10-12 09:25:00', 'activo'),
(4, 4, 4, 'ana.ramirez@gmail.com', '$2a$10$AqC2/QGeswk.PiJc7Bkhtekp69.rC.3M6IRVPkm3KY1BwowwGD37q', '2026-10-08 18:10:00', 'activo'),
(5, 5, 4, 'pedro.gutierrez@outlook.com', '$2a$10$AqC2/QGeswk.PiJc7Bkhtekp69.rC.3M6IRVPkm3KY1BwowwGD37q', '2026-10-03 11:20:00', 'activo'),
(6, 6, 4, 'sofia.castro@gmail.com', '$2a$10$AqC2/QGeswk.PiJc7Bkhtekp69.rC.3M6IRVPkm3KY1BwowwGD37q', '2026-10-08 18:12:00', 'activo'),
(7, 7, 3, 'luis.herrera@gmail.com', '$2a$10$AqC2/QGeswk.PiJc7Bkhtekp69.rC.3M6IRVPkm3KY1BwowwGD37q', '2026-10-13 11:05:00', 'activo'),
(8, 8, 3, 'karla.rios@gmail.com', '$2a$10$AqC2/QGeswk.PiJc7Bkhtekp69.rC.3M6IRVPkm3KY1BwowwGD37q', '2026-10-13 19:35:00', 'activo');

INSERT INTO producto (id_producto, id_marca, id_categoria, codigo_sku, nombre, descripcion, caracteristicas, precio_venta, stock, vigente) VALUES
(1, 1, 1, 'ASMO01', 'ASUS ROG STRIX XG27ACMES 27" QHD 120HZ', 'Monitor gaming ASUS ROG STRIX 27 pulgadas, resolucion QHD, 120Hz', NULL, 273.73, 14, TRUE),
(2, 1, 1, 'ASMO02', 'ASUS TUF GAMING VG34VQL3A 34" CURVO WQHD 180HZ', 'Monitor curvo ASUS TUF 34 pulgadas, WQHD, 180Hz', NULL, 359.00, 4, TRUE),
(3, 2, 1, 'MSMO01', 'MSI MAG 271QPX QD-OLED 27" WQHD 250HZ', 'Monitor MSI con panel QD-OLED, 27 pulgadas, WQHD, 250Hz', NULL, 258.26, 8, TRUE),
(4, 3, 1, 'GIMO01', 'GIGABYTE GS27FA 27" FHD 180HZ', 'Monitor GIGABYTE 27 pulgadas, Full HD, 180Hz', NULL, 329.00, 4, TRUE),
(5, 1, 2, 'ASTA01', 'ASUS RTX 5080 16GB HATSUNE MIKU OC ROG ASTRAL', 'Tarjeta de video ASUS RTX 5080 16GB edicion Hatsune Miku', NULL, 2490.00, 2, TRUE),
(6, 1, 2, 'ASTA02', 'ASUS RTX 5070 12GB WHITE PRIME OC', 'Tarjeta de video ASUS RTX 5070 12GB color blanco', NULL, 879.00, 11, TRUE),
(7, 1, 2, 'ASTA03', 'ASUS RTX 5080 16GB ROG ASTRAL OC', 'Tarjeta de video ASUS RTX 5080 16GB ROG ASTRAL', NULL, 2090.00, 6, TRUE),
(8, 2, 2, 'MSTA01', 'MSI RTX 5080 12GB TRIO', 'Tarjeta de video MSI RTX 5080 12GB TRIO', NULL, 869.90, 8, TRUE);

INSERT INTO producto_imagen (id_producto, url_imagen, es_principal) VALUES
(1, '/Assets/IMG/Produtos/Monitores/ASUS 27.png', TRUE),
(2, '/Assets/IMG/Produtos/Monitores/ASUS 34.png', TRUE),
(3, '/Assets/IMG/Produtos/Monitores/MSI 27.png', TRUE),
(4, '/Assets/IMG/Produtos/Monitores/GiGABYTE 27.png', TRUE),
(5, '/Assets/IMG/Produtos/Tarjetas_de_Video/ASUS MIKU 5080.png', TRUE),
(6, '/Assets/IMG/Produtos/Tarjetas_de_Video/ASUS PRIME 5070.png', TRUE),
(7, '/Assets/IMG/Produtos/Tarjetas_de_Video/ASUS 5080.png', TRUE),
(8, '/Assets/IMG/Produtos/Tarjetas_de_Video/MSI TRIO 5070.png', TRUE);

INSERT INTO proveedor (razon_social, id_tipo_documento, numero_documento, telefono_contacto, correo, nombre_encargado, vigente) VALUES
('ASUSTeK Computer Inc.', 2, '20558662471', '987654321', 'contacto@asus.com.pe', 'Roberto Salazar', TRUE),
('Micro-Star INTL Co., Ltd.', 2, '20512345678', '912345678', 'ventas@msi.com', 'Maria Torres', TRUE),
('GIGABYTE Technology Co., Ltd.', 2, '20698765432', '923456789', 'ventas@gigabyte.com', 'Juan Perez', TRUE),
('NVIDIA Corporation', 2, '20456789123', '934567890', 'ventas@nvidia.com', 'Carlos Mendez', TRUE),
('CORSAIR Gaming, Inc.', 2, '20534567891', '945678901', 'ventas@corsair.com', 'Ana Lopez', TRUE),
('Logitech International S.A.', 2, '20645678912', '956789012', 'ventas@logitech.com', 'Pedro Ruiz', TRUE),
('Kingston Technology Company, Inc.', 2, '20467891234', '967890123', 'ventas@kingston.com', 'Luis Garcia', TRUE),
('Dell Technologies Inc.', 2, '20578912345', '978901234', 'ventas@dell.com', 'Sofia Martinez', TRUE),
('HP Inc.', 2, '20689123456', '989012345', 'ventas@hp.com', 'Diego Rodriguez', TRUE),
('Lenovo Group Limited', 2, '20491234567', '990123456', 'ventas@lenovo.com', 'Carmen Vargas', TRUE),
('Synexo Corporation', 2, '20601234567', '901234567', 'ventas@synexo.com', 'Miguel Sanchez', TRUE),
('Ingram Micro Inc.', 2, '20512345679', '912345670', 'ventas@ingrammicro.com', 'Rosa Chen', TRUE),
('Arrow Electronics, Inc.', 2, '20623456789', '923456780', 'ventas@arrow.com', 'Fernando Torres', TRUE),
('Avnet, Inc.', 2, '20434567890', '934567891', 'ventas@avnet.com', 'Patricia Gomez', TRUE),
('Tech Data Corporation', 2, '20545678901', '945678902', 'ventas@techdata.com', 'Andres Lopez', TRUE),
('Wortmann AG (Terra Computer)', 2, '20656789012', '956789013', 'ventas@wortmann.com', 'Elena Rodriguez', TRUE),
('Acer Inc.', 2, '20467890123', '967890124', 'ventas@acer.com', 'Roberto Diaz', TRUE),
('Seagate Technology LLC', 2, '20578901234', '978901235', 'ventas@seagate.com', 'Monica Herrera', TRUE),
('Western Digital Corporation', 2, '20689012345', '989012346', 'ventas@westerndigital.com', 'Jorge Ramos', TRUE),
('Samsung Electronics Co., Ltd.', 2, '20490123456', '990123457', 'ventas@samsung.com', 'Lucia Fernandez', TRUE);

INSERT INTO producto_proveedor (id_producto, id_proveedor, precio_compra, tiempo_pedido_dias) VALUES
(1, 11, 200.00, 5),
(2, 12, 275.00, 3),
(5, 13, 2100.00, 10),
(3, 2, 190.00, 4),
(8, 2, 735.00, 8),
(4, 3, 240.00, 6),
(1, 1, 205.00, 7),
(7, 1, 1850.00, 12);

INSERT INTO carrito (id_carrito, id_cliente, fecha_carrito) VALUES
(1, 1, '2026-10-13 11:00:00'),
(2, 2, '2026-10-13 19:30:00');

INSERT INTO detalle_carrito (id_detalle_carrito, id_carrito, id_producto, cantidad) VALUES
(1, 1, 1, 1),
(2, 1, 3, 2),
(3, 2, 6, 1),
(4, 2, 4, 2);

INSERT INTO venta (id_venta, id_cliente, id_empleado, estado, fecha_venta, subtotal, igv, total, direccion_envio) VALUES
(1, 1, NULL, 'COMPLETADA', '2026-09-15 10:30:00', 790.25, 142.25, 932.50, 'Jr. Piura 456, Los Olivos, Lima'),
(2, 2, NULL, 'COMPLETADA', '2026-09-28 16:45:00', 1208.00, 217.44, 1425.44, 'Av. Brasil 789, Miraflores, Lima'),
(3, 3, 1, 'COMPLETADA', '2026-10-03 11:15:00', 2490.00, 448.20, 2938.20, 'Calle Los Pinos 321, Santiago de Surco, Lima'),
(4, 4, NULL, 'COMPLETADA', '2026-10-08 18:05:00', 1856.90, 334.24, 2191.14, 'Av. Arequipa 654, San Isidro, Lima'),
(5, 1, NULL, 'PENDIENTE', '2026-10-12 09:20:00', 359.00, 64.62, 423.62, 'Jr. Piura 456, Los Olivos, Lima'),
(6, 2, 1, 'CANCELADA', '2026-10-10 14:50:00', 2363.73, 425.47, 2789.20, 'Av. Brasil 789, Miraflores, Lima');

INSERT INTO detalle_venta (id_detalle_venta, id_venta, id_producto, cantidad, precio_unitario, sub_total_venta) VALUES
(1, 1, 1, 1, 273.73, 273.73),
(2, 1, 3, 2, 258.26, 516.52),
(3, 2, 6, 1, 879.00, 879.00),
(4, 2, 4, 1, 329.00, 329.00),
(5, 3, 5, 1, 2490.00, 2490.00),
(6, 4, 4, 3, 329.00, 987.00),
(7, 4, 8, 1, 869.90, 869.90),
(8, 5, 2, 1, 359.00, 359.00),
(9, 6, 7, 1, 2090.00, 2090.00),
(10, 6, 1, 1, 273.73, 273.73);

INSERT INTO pago (id_pago, id_venta, id_medio_pago, monto_pago, fecha_pago, codigo_operacion, estado_pago) VALUES
(1, 1, 1, 932.50, '2026-09-15 10:32:00', 'OP-000001TZ', 'CONFIRMADO'),
(2, 2, 3, 1425.44, '2026-09-28 16:47:00', 'OP-000002TZ', 'CONFIRMADO'),
(3, 3, 2, 2938.20, '2026-10-03 11:18:00', 'OP-000003TZ', 'CONFIRMADO'),
(4, 4, 5, 2191.14, '2026-10-08 18:08:00', 'OP-000004TZ', 'CONFIRMADO'),
(5, 5, 2, 423.62, '2026-10-12 09:22:00', 'OP-000005TZ', 'PENDIENTE'),
(6, 6, 3, 2789.20, '2026-10-10 14:53:00', 'OP-000006TZ', 'RECHAZADO');