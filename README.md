# Sistema de Gestión de TechZone

**Curso:** Desarrollo Web Integrado  
**Ciclo Académico:** 2026-2  
**Repositorio Oficial:** [https://github.com/MrCarlos17A/TechZone.git](https://github.com/MrCarlos17A/TechZone.git)  

---

## Integrantes del Proyecto

| N° | Apellidos y Nombres | Código Universitario |
| :---: | :--- | :---: |
| 1 | Arroyo Vega, Carlos Felipe | U23214686 |
| 2 | De La Cruz Becerra, Jesús Miguel | U22321531 |
| 3 | Ipanaque Bravo, Gabriel Arturo | U21209609 |
| 4 | Santamaria Damian, Jose Jhonatan | U23222412 |
| 5 | Suarez Paz, Jhon Antonio | U23257973 |
| 6 | Taboada Mimbela, Gabriela Alexandra | U23271730 |

---

## Descripción del Proyecto

**TechZone** es una plataforma web integral de comercio electrónico y gestión empresarial (ERP/E-Commerce) de alta gama orientada a la comercialización, distribución y control de inventario de componentes gamer y hardware de última generación (tarjetas gráficas RTX serie 50, monitores QHD/OLED, procesadores, periféricos y accesorios de alto rendimiento).

El sistema opera bajo una arquitectura desacoplada y escalable en dos capas:
1. **Frontend SPA:** Interfaz de usuario reactiva, accesible y optimizada desarrollada en **Angular 19 / 21** con diseño moderno, soporte completo para **Modo Claro / Modo Oscuro** y gestión de estado mediante **Signals**.
2. **Backend API REST:** Servicios de negocio, persistencia transaccional, seguridad robusta y almacenamiento en la nube construidos sobre **Java 25** y **Spring Boot**.

---

## Tecnologías Utilizadas

### Backend
* **Lenguaje:** Java 25 (OpenJDK 25.0.4.1 / compilado con release 25)
* **Framework:** Spring Boot 4.x / 3.4+
* **Seguridad y Autorización:** Spring Security con autenticación stateless mediante **JWT (JSON Web Tokens)**, firma criptográfica HMAC-SHA256 y hashing de contraseñas con **BCrypt**.
* **Persistencia y ORM:** Spring Data JPA / Hibernate ORM con optimización de dialecto PostgreSQL y carga ansiosa `JOIN FETCH` para integridad relacional.
* **Base de Datos:** PostgreSQL en la nube (**Supabase**) mediante Transaction Pooler (puerto 6543) y preparación de conexiones seguras SSL.
* **Almacenamiento en la Nube:** **Supabase Storage** (buckets de imágenes para marcas, productos, periféricos y comprobantes).
* **Gestor de Dependencias y Construcción:** Apache Maven con Maven Wrapper (`mvnw`).

### Frontend
* **Framework:** Angular 19 / 21 (Arquitectura Standalone Components & Signals reactivos).
* **Diseño y Estilos:** Tailwind CSS con temas duales (Light Mode y Dark Mode Gamer), efectos de cristal (*glassmorphism*) y bordes de alta definición.
* **Iconografía:** Google Material Symbols y Bootstrap Icons.
* **Gráficos y Métricas:** Chart.js con gráficos de evolución temporal de ingresos y distribución de ventas por categoría.

---

## Dependencias Clave del Backend (`pom.xml`)

| Dependencia | Propósito |
| :--- | :--- |
| `spring-boot-starter-web` | Controladores API REST y procesamiento de peticiones HTTP con Tomcat 11. |
| `spring-boot-starter-data-jpa` | Abstracción de repositorios y mapeo relacional con PostgreSQL. |
| `postgresql` | Driver JDBC oficial para la conexión con la base de datos de Supabase. |
| `spring-boot-starter-security` | Seguridad, control de accesos basado en roles (RBAC) y filtros de autorización JWT. |
| `jjwt-api / jjwt-impl / jjwt-jackson` | Generación, firmado y verificación de tokens de acceso seguro. |
| `spring-boot-starter-validation` | Validación declarativa de reglas de negocio en DTOs, registros y compras. |
| `lombok` | Reducción de código repetitivo mediante `@Getter`, `@Setter`, `@Builder` y constructores. |

---

## Roles y Permisos del Sistema

| Rol | Alcance y Acceso |
| :--- | :--- |
| **Administrador** | Acceso total al panel de administración (`/admin/**`). Gestión de inventario, categorías, marcas con logotipos, galería multimedia en Supabase Storage, 20 proveedores oficiales, usuarios, roles, métricas de ventas y configuración institucional del sitio. |
| **Trabajador** | Acceso operativo (`/admin/**`). Control y cambio de estados de pedidos/ventas, monitoreo de stock crítico y consulta de clientes/comprobantes. |
| **Usuario / Cliente** | Tienda pública (`/`), catálogo de productos con búsqueda en tiempo real, carrito sincronizado, pasarela de compra digital (Yape, Plin, Tarjetas, Transferencias) y panel *"Mis Pedidos / Mi Perfil"*. |

> **Nota:** La jerarquía del modelo de datos sigue la estructura `Persona` → `Usuario` → `Cliente` / `Empleado`. Los nombres y apellidos completos (paterno y materno) se muestran unificados en todo el sistema y en los carritos de compra.

---

## Funcionalidades Principales

* **E-Commerce Gamer:**
  * Catálogo dinámico con búsqueda instantánea y filtros rápidos por stock (En Stock, Stock Crítico $\le 5$, Sin Stock).
  * Carrito de compras reactivo sincronizado con el backend.
  * Pasarela de Checkout con validación de datos de entrega, documento de identidad (DNI / RUC) y selección de método de pago.
* **Panel de Administración (Backoffice):**
  * Dashboard de analítica con métricas de ingresos, ticket promedio y gráficos interactivos.
  * **Gestor de Imágenes de Supabase Storage:** subida de archivos locales o enlace por URL externa, vinculación/edición directa del producto asignado y marcado de imagen principal.
  * **Directorio de Ubigeos del Perú:** consulta distrital/provincial con badges de estado vigencia interactivos.
  * **Directorio de Proveedores:** catálogo completo con los 20 proveedores y distribuidores tecnológicos oficiales.
  * **Auditoría y Seguridad:** registro en tiempo real de inicios de sesión, detección de intentos fallidos con bloqueo preventivo temporal de 15 minutos e identificación de IP y agentes.
* **Experiencia de Usuario (UI/UX):**
  * Alternador de **Modo Claro** y **Modo Oscuro** persistente en toda la aplicación.
  * Footer con contraste y separación visual en ambos modos temáticos.
  * Estandarización de textos: conversión automática a mayúsculas para nombres y direcciones (estándar RENIEC/SUNAT) y correos electrónicos en minúsculas.

---

## Configuración y Ejecución del Proyecto

### 1. Requisitos Previos
* **Java Development Kit (JDK):** **Java 25** (OpenJDK 25.0.4.1 o superior) configurado en `JAVA_HOME` o `PATH`.
* **Node.js:** Versión 18+ con gestor de paquetes `npm`.
* **Maven:** Incluido a través de Maven Wrapper (`.\mvnw.cmd`).
* **Supabase:** Instancia de PostgreSQL y bucket `imagenes` configurado en Storage.

---

### 2. Configuración de Variables de Entorno (`.env`)

En la carpeta `backend-techzone`, copia la plantilla `.env.example` y nómbrala `.env`:

```bash
cd backend-techzone
cp .env.example .env
```

Configura tus credenciales en `backend-techzone/.env`:

```env
# --- API de consulta de DNI (opcional) ---
DNI_API_TOKEN=

# --- Supabase · Proyecto y Storage ---
SUPABASE_URL=https://<TU-REF>.supabase.co
SUPABASE_SERVICE_KEY=<TU_SERVICE_ROLE_KEY>
SUPABASE_BUCKET=imagenes

# --- Supabase · Base de datos (Transaction Pooler - Puerto 6543) ---
SUPABASE_DB_URL=jdbc:postgresql://<HOST-POOLER>:6543/postgres
SUPABASE_DB_USER=postgres.<TU-REF>
SUPABASE_DB_PASSWORD=<TU_PASSWORD>

# --- Firma criptográfica de JWT (mínimo 32 caracteres) ---
JWT_SECRET=b1e55296f1efd85a92828162e126b7462039307a210da139a600e912c62d6cc54e7b4f85eea9c51ef28408d54beca088
```

> **Nota:** El backend carga automáticamente las variables del archivo `.env` al iniciar.

---

### 3. Puesta en Marcha de la Base de Datos

Ejecuta el script maestro con la estructura relacional e inserciones iniciales en el SQL Editor de tu proyecto en Supabase:
```
backend-techzone/src/main/java/com/tienda/backend_techzone/Data/bd_techzone_supabase.sql
```

---

### 4. Ejecución del Backend (Java 25)

Abre una terminal PowerShell en la carpeta `backend-techzone`:

```powershell
# Opción A: Script oficial de arranque (detecta Java 25 y valida variables)
.\iniciar.ps1

# Opción B: Arranque directo con Maven Wrapper
.\mvnw.cmd spring-boot:run
```

*El API REST estará disponible en:* `http://localhost:8080`

---

### 5. Ejecución del Frontend (Angular)

Abre otra terminal en la carpeta `frontend-techzone`:

```bash
# 1. Instalar dependencias
npm install

# 2. Iniciar servidor de desarrollo
npm start
```

*La aplicación web estará disponible en:* `http://localhost:4200`
