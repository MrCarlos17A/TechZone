# Sistema de Gestión de TechZone

**Curso:** Desarrollo Web Integrado  
**Ciclo Académico:** 2026-2  
**Repositorio Oficial:** [https://github.com/MrCarlos17A/TechZone.git](https://github.com/MrCarlos17A/TechZone.git)

---

## Integrantes del Proyecto

| N° | Apellidos y Nombres | Código Universitario |
| :---: | :--- | :---: |
| 1 | Arroyo Vega, Carlos Felipe | U23214686 |
| 2 | Santamaria Damian, Jose Jhonatan | U23222412 |
| 3 | Ipanaque Bravo, Gabriel Arturo | U21209609 |
| 4 | De La Cruz Becerra, Jesús Miguel | U22321531 |
| 5| Suarez Paz Jhon Antonio | U23257973 |
| 6| Taboada Mimbela, Gabriela Alexandra| U23271730 |

---

## Descripción del Proyecto

TechZone es una aplicación web integral diseñada para la gestión, control de inventario y comercialización de productos tecnológicos, desarrollada bajo el patrón de arquitectura MVC (Modelo-Vista-Controlador).

---

## Tecnologías Utilizadas

- **Lenguaje:** Java 25
- **Framework:** Spring Boot 4.1.1
- **Gestor de dependencias y construcción:** Apache Maven
- **Motor de plantillas:** Thymeleaf
- **Base de Datos:** MySQL Server 8.0+ (puerto 3307)
- **Seguridad:** Spring Security (autenticación y autorización)
- **Herramientas de desarrollo:** Lombok, Spring Boot Maven Plugin

---

## Dependencias del Proyecto (`pom.xml`)

| Dependencia | Propósito |
| :--- | :--- |
| `spring-boot-starter-web` | Creación de controladores REST/MVC y manejo de solicitudes HTTP. |
| `spring-boot-starter-thymeleaf` | Motor de plantillas para renderizar vistas HTML dinámicas. |
| `spring-boot-starter-data-jpa` | Abstracción de persistencia y operaciones con base de datos relacional. |
| `mysql-connector-j` | Driver oficial de conexión para MySQL. |
| `spring-boot-starter-security` | Mecanismos de autenticación, control de sesiones y autorización. |
| `spring-boot-starter-validation` | Validación declarativa de datos en formularios y entidades. |
| `lombok` | Reducción de código repetitivo mediante anotaciones (`@Data`, `@Builder`, etc.). |
| `spring-boot-starter-test` | Framework y utilidades para pruebas unitarias y de integración. |

---

## Roles del Sistema

| ID | Rol | Acceso |
| :---: | :--- | :--- |
| 1 | **Administrador** | Panel `/admin/**` completo (gestion completa). |
| 2 | **Trabajador** | Panel `/admin/**` (mismas secciones que Administrador. con restringciones de modulos). |
| 3 | **Usuario** | Solo compra desde la tienda pública (`/`). |
| 4 | **Cliente** | Solo compra desde la tienda pública (`/`). |

> **Nota:** Los roles **Usuario** y **Cliente los asigna únicamente el sistema**; no se pueden crear manualmente desde el panel. Al registrar una cuenta desde la tienda, se crea con rol Cliente. Si un empleado compra, conserva su rol y además se le genera su ficha de Cliente.

---

## Funcionalidades Principales

- **Tienda pública** (`/`): catálogo de productos, carrito de compras y registro de clientes.
- **Panel de administración** (`/admin/**`): gestión de productos, marcas, categorías, proveedores, medios de pago, usuarios, empleados, ventas y reportes.
- **Módulo de Clientes (solo lectura):** listado y detalle de clientes con su historial de compras; la edición y eliminación se gestiona desde el módulo de Usuarios.
- **Máquina de estados de venta:** las ventas transitan por estados (Pendiente → Pagado → Enviado → Entregado, o Cancelado) con validaciones de transición.
- **Eliminación lógica:** los registros se desactivan (`vigente = false`) en lugar de borrarse físicamente, preservando el historial.
- **Consulta de DNI (local-first):** primero busca el documento en la base de datos local; solo si no existe consulta la API externa (`apis.net.pe`), con control de rate limit por IP.
- **Autenticación y autorización:** Spring Security con roles, rutas protegidas y CSRF habilitado.

---

## Configuración y Ejecución Local

### 1. Requisitos previos
- **JDK 25** instalado y configurado en las variables de entorno (`JAVA_HOME`).
- **Maven 3.9+** (o usar el Maven Wrapper incluido `./mvnw`).
- **MySQL Server 8.0+** en ejecución en el puerto **3307**.

### 2. Configuración de Base de Datos

La base de datos se crea desde cero con el script incluido en el repositorio:

```
src/main/java/com/Tienda/TechZone/Data/bd_techzone.sql
```

Ejecútalo contra tu servidor MySQL (puerto 3307). El script elimina y recrea la base `BD_Techzone` con todas las tablas y datos de prueba (productos, usuarios, clientes, ventas, etc.).

Ajusta las credenciales de conexión en `src/main/resources/application.properties`:

### application.properties
```
spring.datasource.url=jdbc:mysql://localhost:3307/BD_Techzone?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

spring.sql.init.mode=never

spring.thymeleaf.cache=false

spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB

dni.api.url=https://api.apis.net.pe/v1/dni
dni.api.token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjo2NDQsImV4cCI6MTc2NTUwMjk2OX0.u-EQv5frP4I97ssdCDJ7bgFCuZlRNDEzi5RLVvz1RCs
```

### 3. Compilación y ejecución

Desde la terminal en la raíz del proyecto:

```bash
# Compilar y empaquetar el proyecto
./mvnw clean package -DskipTests

# Ejecutar la aplicación
./mvnw spring-boot:run
```

La aplicación estará accesible en: `http://localhost:8080`

> En Windows, usa `.\\mvnw.cmd` en lugar de `./mvnw`.

---

## Credenciales de Prueba

El script `bd_techzone.sql` siembra las siguientes cuentas (contraseñas encriptadas con BCrypt):

| Correo | Contraseña | Rol |
| :--- | :--- | :--- |
| `admin@techzone.com` | `Admin123**` | Administrador |
---

## Estructura de la Base de Datos

El script crea 20 tablas: `ubigeo`, `tipo_documento`, `rol`, `marca`, `categoria`, `medio_pago`, `persona`, `proveedor`, `cliente`, `empleado`, `usuario`, `producto`, `producto_imagen`, `producto_proveedor`, `carrito`, `detalle_carrito`, `venta`, `detalle_venta`, `pago` y `configuracion_sitio`.

El modelo sigue una jerarquía: **Persona → Usuario → Cliente / Empleado**. Una misma persona puede ser a la vez Cliente (ficha de compras) y Empleado (ficha laboral), conservando un único usuario de acceso.
