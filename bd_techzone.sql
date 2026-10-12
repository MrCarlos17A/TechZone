-- ══════════════════════════════════════════════════════════════
-- TechZone - Script completo MySQL
-- Base de datos: BD_Techzone (puerto 3307)
-- ══════════════════════════════════════════════════════════════

DROP DATABASE IF EXISTS BD_Techzone;
CREATE DATABASE BD_Techzone
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE BD_Techzone;

-- ══════════════════════════════════════════════════════════════
-- 1. TABLAS CATÁLOGO
-- ══════════════════════════════════════════════════════════════

CREATE TABLE ubigeo (
    id_ubigeo MEDIUMINT NOT NULL,
    departamento VARCHAR(50) NULL,
    provincia VARCHAR(50) NULL,
    distrito VARCHAR(50) NULL,
    vigente BOOLEAN NULL,
    PRIMARY KEY (id_ubigeo)
) ENGINE=InnoDB;

CREATE TABLE tipo_documento (
    id_tipo_documento TINYINT NOT NULL,
    nombre VARCHAR(50) NULL,
    vigente BOOLEAN NULL,
    PRIMARY KEY (id_tipo_documento)
) ENGINE=InnoDB;

CREATE TABLE rol (
    id_rol TINYINT NOT NULL,
    nombre VARCHAR(20) NULL,
    vigente BOOLEAN NULL,
    PRIMARY KEY (id_rol)
) ENGINE=InnoDB;

CREATE TABLE marca (
    id_marca TINYINT NOT NULL,
    nombre VARCHAR(20) NULL,
    vigente BOOLEAN NULL,
    PRIMARY KEY (id_marca)
) ENGINE=InnoDB;

CREATE TABLE categoria (
    id_categoria TINYINT NOT NULL,
    nombre VARCHAR(50) NULL,
    vigente BOOLEAN NULL,
    PRIMARY KEY (id_categoria)
) ENGINE=InnoDB;

CREATE TABLE medio_pago (
    id_medio_pago TINYINT NOT NULL,
    nombre VARCHAR(50) NULL,
    vigente BOOLEAN NULL,
    PRIMARY KEY (id_medio_pago)
) ENGINE=InnoDB;

-- ══════════════════════════════════════════════════════════════
-- 2. ENTIDADES PRINCIPALES
-- ══════════════════════════════════════════════════════════════

CREATE TABLE persona (
    id_persona BIGINT NOT NULL AUTO_INCREMENT,
    nombres VARCHAR(100) NULL,
    apellido_paterno VARCHAR(50) NULL,
    apellido_materno VARCHAR(50) NULL,
    id_tipo_documento TINYINT NULL,
    numero_documento VARCHAR(20) NULL,
    telefono VARCHAR(12) NULL,
    direccion VARCHAR(255) NULL,
    referencia VARCHAR(255) NULL,
    id_ubigeo MEDIUMINT NULL,
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
    id_proveedor SMALLINT NOT NULL AUTO_INCREMENT,
    razon_social VARCHAR(150) NULL,
    id_tipo_documento TINYINT NULL,
    numero_documento VARCHAR(20) NULL,
    telefono_contacto VARCHAR(20) NULL,
    correo VARCHAR(100) NULL,
    nombre_encargado VARCHAR(150) NULL,
    vigente BOOLEAN NULL,
    PRIMARY KEY (id_proveedor),
    KEY idx_proveedor_tipo_documento (id_tipo_documento),
    CONSTRAINT fk_proveedor_tipo_doc FOREIGN KEY (id_tipo_documento) REFERENCES tipo_documento (id_tipo_documento) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ══════════════════════════════════════════════════════════════
-- 3. DERIVADOS DE PERSONA
-- ══════════════════════════════════════════════════════════════

CREATE TABLE cliente (
    id_cliente BIGINT NOT NULL AUTO_INCREMENT,
    id_persona BIGINT NULL,
    fecha_ultima_compra DATETIME NULL,
    total_gastado DECIMAL(10,2) NULL,
    PRIMARY KEY (id_cliente),
    KEY idx_cliente_persona (id_persona),
    CONSTRAINT fk_cliente_persona FOREIGN KEY (id_persona) REFERENCES persona (id_persona) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE empleado (
    id_empleado BIGINT NOT NULL AUTO_INCREMENT,
    id_persona BIGINT NULL,
    fecha_contrato DATE NULL,
    salario DECIMAL(10,2) NULL,
    PRIMARY KEY (id_empleado),
    KEY idx_empleado_persona (id_persona),
    CONSTRAINT fk_empleado_persona FOREIGN KEY (id_persona) REFERENCES persona (id_persona) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE usuario (
    id_usuario BIGINT NOT NULL AUTO_INCREMENT,
    id_persona BIGINT NULL,
    id_rol TINYINT NULL,
    correo VARCHAR(100) NULL,
    contrasena VARCHAR(60) NULL,
    fecha_ultimo_acceso DATETIME NULL,
    estado VARCHAR(20) NULL,
    PRIMARY KEY (id_usuario),
    KEY idx_usuario_persona (id_persona),
    KEY idx_usuario_rol (id_rol),
    CONSTRAINT fk_usuario_persona FOREIGN KEY (id_persona) REFERENCES persona (id_persona) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol (id_rol) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ══════════════════════════════════════════════════════════════
-- 4. PRODUCTOS, IMÁGENES Y PROVEEDORES
-- ══════════════════════════════════════════════════════════════

CREATE TABLE producto (
    id_producto BIGINT NOT NULL AUTO_INCREMENT,
    id_marca TINYINT NULL,
    id_categoria TINYINT NULL,
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
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria) ON DELETE RESTRICT ON UPDATE CASCADE
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
    id_proveedor SMALLINT NOT NULL,
    precio_compra DECIMAL(10,2) NULL,
    tiempo_pedido_dias TINYINT NULL,
    PRIMARY KEY (id_producto, id_proveedor),
    KEY idx_pp_proveedor (id_proveedor),
    CONSTRAINT fk_pp_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_pp_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedor (id_proveedor) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ══════════════════════════════════════════════════════════════
-- 5. CARRITO DE COMPRAS
-- ══════════════════════════════════════════════════════════════

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
    CONSTRAINT fk_dc_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ══════════════════════════════════════════════════════════════
-- 6. VENTAS, DETALLE DE VENTA Y PAGOS
-- ══════════════════════════════════════════════════════════════

CREATE TABLE venta (
    id_venta INT NOT NULL AUTO_INCREMENT,
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
    CONSTRAINT fk_venta_empleado FOREIGN KEY (id_empleado) REFERENCES empleado (id_empleado) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE detalle_venta (
    id_detalle_venta INT NOT NULL AUTO_INCREMENT,
    id_venta INT NULL,
    id_producto BIGINT NULL,
    cantidad INT NULL,
    precio_unitario DECIMAL(10,2) NULL,
    sub_total_venta DECIMAL(10,2) NULL,
    PRIMARY KEY (id_detalle_venta),
    KEY idx_dv_venta (id_venta),
    KEY idx_dv_producto (id_producto),
    CONSTRAINT fk_dv_venta FOREIGN KEY (id_venta) REFERENCES venta (id_venta) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_dv_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE pago (
    id_pago INT NOT NULL AUTO_INCREMENT,
    id_venta INT NULL,
    id_medio_pago TINYINT NULL,
    monto_pago DECIMAL(10,2) NULL,
    fecha_pago DATETIME NULL,
    codigo_operacion VARCHAR(50) NULL,
    estado_pago VARCHAR(50) NULL,
    PRIMARY KEY (id_pago),
    KEY idx_pago_venta (id_venta),
    KEY idx_pago_medio_pago (id_medio_pago),
    CONSTRAINT fk_pago_venta FOREIGN KEY (id_venta) REFERENCES venta (id_venta) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_pago_medio_pago FOREIGN KEY (id_medio_pago) REFERENCES medio_pago (id_medio_pago) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ──────────────────────────────────────────────────────────────
-- ROLES
-- ──────────────────────────────────────────────────────────────
INSERT INTO rol (id_rol, nombre, vigente) VALUES
(1, 'Administrador', TRUE),
(2, 'Trabajador', TRUE),
(3, 'Cliente', TRUE);

-- ──────────────────────────────────────────────────────────────
-- TIPOS DE DOCUMENTO
-- ──────────────────────────────────────────────────────────────
INSERT INTO tipo_documento (id_tipo_documento, nombre, vigente) VALUES
(1, 'DNI', TRUE),
(3, 'RUC', TRUE),
(4, 'Pasaporte', TRUE);

-- ──────────────────────────────────────────────────────────────
-- MEDIOS DE PAGO
-- ──────────────────────────────────────────────────────────────
INSERT INTO medio_pago (id_medio_pago, nombre, vigente) VALUES
(1, 'Efectivo', TRUE),
(2, 'Yape', TRUE),
(3, 'Plin', TRUE),
(4, 'Tarjeta de Credito', TRUE),
(5, 'Tarjeta de Debito', TRUE),
(6, 'Transferencia', TRUE),
(7, 'PagoEfectivo', TRUE);

-- ──────────────────────────────────────────────────────────────
-- UBIGEOS - Lima, Callao, Lambayeque
-- ──────────────────────────────────────────────────────────────
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

-- ──────────────────────────────────────────────────────────────
-- MARCAS
-- ──────────────────────────────────────────────────────────────
INSERT INTO marca (id_marca, nombre, vigente) VALUES
(1, 'ASUS', TRUE),
(2, 'MSI', TRUE),
(3, 'GIGABYTE', TRUE),
(4, 'CORSAIR', TRUE),
(5, 'REDRAGON', TRUE),
(6, 'Logitech', TRUE),
(7, 'Kingston', TRUE),
(8, 'ALIENWARE', TRUE),
(9, 'TOSHIBA', TRUE),
(10, 'MICRONICS', TRUE);

-- ──────────────────────────────────────────────────────────────
-- CATEGORIAS
-- ──────────────────────────────────────────────────────────────
INSERT INTO categoria (id_categoria, nombre, vigente) VALUES
(1, 'Monitores', TRUE),
(2, 'Tarjetas de Video', TRUE);

-- ──────────────────────────────────────────────────────────────
-- PRODUCTOS
-- ──────────────────────────────────────────────────────────────
INSERT INTO producto (id_producto, id_marca, id_categoria, codigo_sku, nombre, descripcion, caracteristicas, precio_venta, stock, vigente) VALUES
(1, 1, 1, 'ASMO01', 'ASUS ROG STRIX XG27ACMES 27\" QHD 120HZ', 'Monitor gaming ASUS ROG STRIX 27 pulgadas, resolucion QHD, 120Hz', NULL, 273.73, 15, TRUE),
(2, 1, 1, 'ASMO02', 'ASUS TUF GAMING VG34VQL3A 34\" CURVO WQHD 180HZ', 'Monitor curvo ASUS TUF 34 pulgadas, WQHD, 180Hz', NULL, 359.00, 5, TRUE),
(3, 2, 1, 'MSMO01', 'MSI MAG 271QPX QD-OLED 27\" WQHD 250HZ', 'Monitor MSI con panel QD-OLED, 27 pulgadas, WQHD, 250Hz', NULL, 258.26, 10, TRUE),
(4, 3, 1, 'GIMO01', 'GIGABYTE GS27FA 27\" FHD 180HZ', 'Monitor GIGABYTE 27 pulgadas, Full HD, 180Hz', NULL, 329.00, 8, TRUE),
(5, 1, 2, 'ASTA01', 'ASUS RTX 5080 16GB HATSUNE MIKU OC ROG ASTRAL', 'Tarjeta de video ASUS RTX 5080 16GB edicion Hatsune Miku', NULL, 2490.00, 3, TRUE),
(6, 1, 2, 'ASTA02', 'ASUS RTX 5070 12GB WHITE PRIME OC', 'Tarjeta de video ASUS RTX 5070 12GB color blanco', NULL, 879.00, 12, TRUE),
(7, 1, 2, 'ASTA03', 'ASUS RTX 5080 16GB ROG ASTRAL OC', 'Tarjeta de video ASUS RTX 5080 16GB ROG ASTRAL', NULL, 2090.00, 6, TRUE),
(8, 2, 2, 'MSTA01', 'MSI RTX 5080 12GB TRIO', 'Tarjeta de video MSI RTX 5080 12GB TRIO', NULL, 869.90, 9, TRUE);

-- ──────────────────────────────────────────────────────────────
-- IMAGENES DE PRODUCTO
-- ──────────────────────────────────────────────────────────────
INSERT INTO producto_imagen (id_producto, url_imagen, es_principal) VALUES
(1, '/Assets/IMG/Produtos/Monitores/ASUS 27.png', TRUE),
(2, '/Assets/IMG/Produtos/Monitores/ASUS 34.png', TRUE),
(3, '/Assets/IMG/Produtos/Monitores/MSI 27.png', TRUE),
(4, '/Assets/IMG/Produtos/Monitores/GiGABYTE 27.png', TRUE),
(5, '/Assets/IMG/Produtos/Tarjetas_de_Video/ASUS MIKU 5080.png', TRUE),
(6, '/Assets/IMG/Produtos/Tarjetas_de_Video/ASUS PRIME 5070.png', TRUE),
(7, '/Assets/IMG/Produtos/Tarjetas_de_Video/ASUS 5080.png', TRUE),
(8, '/Assets/IMG/Produtos/Tarjetas_de_Video/MSI TRIO 5070.png', TRUE);

-- ──────────────────────────────────────────────────────────────
-- PROVEEDORES
-- ──────────────────────────────────────────────────────────────
INSERT INTO proveedor (razon_social, id_tipo_documento, numero_documento, telefono_contacto, correo, nombre_encargado, vigente) VALUES
('ASUSTeK Computer Inc.', 3, '20558662471', '987654321', 'contacto@asus.com.pe', 'Roberto Salazar', TRUE),
('Micro-Star INTL Co., Ltd.', 3, '20512345678', '912345678', 'ventas@msi.com', 'Maria Torres', TRUE),
('GIGABYTE Technology Co., Ltd.', 3, '20698765432', '923456789', 'ventas@gigabyte.com', 'Juan Perez', TRUE),
('NVIDIA Corporation', 3, '20456789123', '934567890', 'ventas@nvidia.com', 'Carlos Mendez', TRUE),
('CORSAIR Gaming, Inc.', 3, '20534567891', '945678901', 'ventas@corsair.com', 'Ana Lopez', TRUE),
('Logitech International S.A.', 3, '20645678912', '956789012', 'ventas@logitech.com', 'Pedro Ruiz', TRUE),
('Kingston Technology Company, Inc.', 3, '20467891234', '967890123', 'ventas@kingston.com', 'Luis Garcia', TRUE),
('Dell Technologies Inc.', 3, '20578912345', '978901234', 'ventas@dell.com', 'Sofia Martinez', TRUE),
('HP Inc.', 3, '20689123456', '989012345', 'ventas@hp.com', 'Diego Rodriguez', TRUE),
('Lenovo Group Limited', 3, '20491234567', '990123456', 'ventas@lenovo.com', 'Carmen Vargas', TRUE),
('Synexo Corporation', 3, '20601234567', '901234567', 'ventas@synexo.com', 'Miguel Sanchez', TRUE),
('Ingram Micro Inc.', 3, '20512345679', '912345670', 'ventas@ingrammicro.com', 'Rosa Chen', TRUE),
('Arrow Electronics, Inc.', 3, '20623456789', '923456780', 'ventas@arrow.com', 'Fernando Torres', TRUE),
('Avnet, Inc.', 3, '20434567890', '934567891', 'ventas@avnet.com', 'Patricia Gomez', TRUE),
('Tech Data Corporation', 3, '20545678901', '945678902', 'ventas@techdata.com', 'Andres Lopez', TRUE),
('Wortmann AG (Terra Computer)', 3, '20656789012', '956789013', 'ventas@wortmann.com', 'Elena Rodriguez', TRUE),
('Acer Inc.', 3, '20467890123', '967890124', 'ventas@acer.com', 'Roberto Diaz', TRUE),
('Seagate Technology LLC', 3, '20578901234', '978901235', 'ventas@seagate.com', 'Monica Herrera', TRUE),
('Western Digital Corporation', 3, '20689012345', '989012346', 'ventas@westerndigital.com', 'Jorge Ramos', TRUE),
('Samsung Electronics Co., Ltd.', 3, '20490123456', '990123457', 'ventas@samsung.com', 'Lucia Fernandez', TRUE);

INSERT INTO persona (nombres, apellido_paterno, apellido_materno, id_tipo_documento, numero_documento, telefono, direccion, referencia, id_ubigeo, fecha_nacimiento, fecha_registro, vigente) VALUES
('CARLOS FELIPE', 'ARROYO', 'VEGA', 1, '73234768', '967662103', 'Av. Santa Ana', 'Plaza', 140122, '2005-04-13', NOW(), TRUE);

INSERT INTO usuario (id_persona, id_rol, correo, contrasena, fecha_ultimo_acceso, estado) VALUES
(1, 1, 'admin@techzone.com', '123456', NULL, 'activo');