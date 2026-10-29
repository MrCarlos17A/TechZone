# Sistema de Gestión Integral TechZone · Avance III

**Curso:** Desarrollo Web Integrado  
**Ciclo Académico:** 2026-2 · Ciclo VIII  
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

**TechZone** es una plataforma web integral de comercio electrónico y gestión empresarial (ERP/E-Commerce B2C/B2B) orientada a la comercialización, distribución mayorista/minorista y control logístico de hardware gamer de alta gama (tarjetas gráficas serie RTX 50, monitores OLED/QHD de alta tasa de refresco, procesadores, periféricos y accesorios profesionales).

El proyecto está diseñado bajo una arquitectura limpia y desacoplada de dos capas (**Monorepo Fullstack**):
1. **Frontend SPA:** Interfaz moderna y reactiva construida con **Angular 19 / 21**, gestión de estado granular basada en **Signals**, maquetación con **Tailwind CSS**, soporte integral para **Modo Claro / Modo Oscuro** y renderizado responsive de alta fidelidad.
2. **Backend API REST:** Servidor robusto desarrollado en **Java 25** con **Spring Boot**, autenticación stateless mediante **JWT**, control de accesos por roles (**RBAC**), persistencia transaccional sobre **PostgreSQL en Supabase** y almacenamiento multimedia seguro en **Supabase Storage**.

---

## Estructura del Repositorio

```text
TechZone/
├── backend-techzone/               # API REST - Spring Boot (Java 25)
│   ├── src/main/java/              # Arquitectura en capas: Controller, Model, Repository, Service, Security
│   ├── src/main/resources/         # Configuración y application.properties
│   ├── .env.example                # Plantilla de variables de entorno para desarrollo
│   ├── iniciar.ps1                 # Script de arranque automatizado con detección de Java 25
│   ├── mvnw / mvnw.cmd             # Maven Wrapper
│   └── pom.xml                     # Gestión de dependencias de Maven
├── frontend-techzone/              # Cliente Web SPA - Angular 19 / 21
│   ├── src/app/pages/              # Módulos: Tienda, Checkout, Carrito, Login, Perfil y Backoffice Admin
│   ├── src/app/components/         # Componentes reutilizables (Navbar, Footer, Card, Modales, Toast)
│   ├── src/app/services/           # Servicios HTTP reactivos y estado global mediante Signals
│   ├── angular.json                # Configuración de compilación de Angular CLI
│   └── package.json                # Dependencias y scripts de ejecución
├── .gitignore                      # Reglas maestras de exclusión (secretos, dependencias y binarios)
└── README.md                       # Documentación técnica y funcional del proyecto
```

---

## Tecnologías Utilizadas

### Backend
* **Lenguaje:** Java 25 (OpenJDK 25.0.4.1 / compilado con release 25)
* **Framework:** Spring Boot 3.4+ / 4.x
* **Seguridad & RBAC:** Spring Security con filtros stateless **JWT (JSON Web Tokens)** con firma HMAC-SHA256 y hashing de contraseñas con **BCrypt**.
* **Persistencia y ORM:** Spring Data JPA / Hibernate ORM con optimización de dialecto PostgreSQL y consultas ansiosas `JOIN FETCH`.
* **Base de Datos en la Nube:** PostgreSQL alojado en **Supabase** mediante Transaction Pooler (puerto 6543) con conexiones SSL.
* **Almacenamiento Multimedia:** **Supabase Storage** (buckets de imágenes para marcas, catálogo de productos, banners y comprobantes).
* **Gestor de Construcción:** Apache Maven con Maven Wrapper (`mvnw`).

### Frontend
* **Framework:** Angular 19 / 21 (Arquitectura Standalone Components & Signals reactivos).
* **Diseño y Estilos:** Tailwind CSS con temas duales (*Light Mode* y *Dark Gamer Mode*), efectos de cristal (*glassmorphism*) y diseño fluido.
* **Iconografía:** Google Material Symbols y Bootstrap Icons.
* **Gráficos y Métricas:** Chart.js con gráficos de evolución temporal de ingresos y distribución de ventas por categoría.

---

## Roles y Matriz de Permisos (RBAC)

El sistema implementa control de acceso basado en roles tanto a nivel de endpoints en Spring Security como en la navegación y acciones de interfaz en Angular:

| Rol | Ámbito de Acceso | Permisos y Capacidades |
| :--- | :--- | :--- |
| **Administrador** (`ROLE_ADMINISTRADOR`) | Control Total (`/admin/**`) | Gestión completa de catálogo, categorías, marcas, inventario, órdenes de venta, proveedores, colaboradores, auditoría de seguridad completa con trazabilidad de IP, roles y configuración institucional del sitio. Autorizado para eliminaciones y restauraciones. |
| **Colaborador** (`ROLE_TRABAJADOR`) | Operativo & Ventas (`/admin/**`) | Atención y despacho de pedidos, consulta de inventario y stock crítico, registro de ventas, emisión de boletas/facturas electrónicas y consulta de clientes. **Restricción de seguridad:** botones de eliminación permanente ocultos y bloqueo de acceso a módulos críticos (Auditoría, Roles y Configuración). |
| **Cliente** (`ROLE_CLIENTE`) | Tienda Pública (`/`) | Catálogo con filtros en tiempo real, carrito de compras sincronizado, checkout digital con validación de DNI/RUC, opciones de pago (Yape, Plin, Tarjeta, Transferencia) y módulo de autogestión *"Mis Pedidos / Mi Perfil"*. |

---

## Novedades y Módulos del Avance III

1. **Control de Acceso y Rebranding de Colaboradores:**
   * Sustitución completa de nomenclaturas operativas por el estándar corporativo **"Colaboradores"**.
   * Ocultamiento selectivo de acciones destructivas (tachos de eliminación) y protección a nivel de API REST mediante `hasRole('ADMINISTRADOR')`.

2. **Auditoría & Trazabilidad de Seguridad con Dirección IP:**
   * Módulo de auditoría con selector conmutable de dos vistas:
     * **Acciones del Sistema:** Registro de operaciones en catálogo, modificaciones y eliminaciones accidentales con soporte para **restauración inmediata**, fecha/hora y **Dirección IP** de terminal.
     * **Inicios de Sesión & Accesos:** Bitácora completa de accesos con **Dirección IP**, usuario/correo, agente/navegador, fecha exacta y estado de autenticación (Exitoso / Fallido con motivo).

3. **Matriz de Abastecimiento (Producto - Proveedor):**
   * Visualización transparente de la relación $M:N$ con columnas independientes:
     * **`N°`:** Número correlativo de fila en la lista.
     * **`ID Prod`:** Identificador específico del producto en el catálogo.
     * Costos de compra mayorista, Precio de Venta al Público (PVP), cálculo dinámico de **Margen Bruto de Ganancia (%)** y tiempos de entrega estimados.

4. **Identidad Visual Corporativa:**
   * Integración del logotipo oficial de TechZone con soporte de contraste total en modo claro y modo oscuro.
   * Ajuste de tipografía corporativa en pantallas de autenticación (Login, Registro) y barra lateral del panel administrativo.

5. **Exportación Contable Oficial:**
   * Motor de exportación a libros de cálculo Excel XML para reportes de ventas, balances y catálogo de existencias.

---

## Configuración y Ejecución del Proyecto

### 1. Requisitos Previos
* **Java Development Kit (JDK):** **Java 25** (OpenJDK 25.0.4.1 o compatible) en `JAVA_HOME` o `PATH`.
* **Node.js:** Versión 18+ (LTS recomendada) con `npm`.
* **Supabase:** Instancia de PostgreSQL y bucket `imagenes` en Storage.

---

### 2. Configuración de Variables de Entorno (`.env`)

En la carpeta `backend-techzone`, copia el archivo de plantilla `.env.example` como `.env`:

```bash
cd backend-techzone
cp .env.example .env
```

Configura tus credenciales locales en `backend-techzone/.env`:

```env
# --- API de consulta de DNI (opcional) ---
DNI_API_TOKEN=

# --- Supabase · Proyecto y Storage ---
SUPABASE_URL=https://<TU-PROYECTO>.supabase.co
SUPABASE_SERVICE_KEY=<TU_SERVICE_ROLE_KEY>
SUPABASE_BUCKET=imagenes

# --- Supabase · Base de Datos (Transaction Pooler - Puerto 6543) ---
SUPABASE_DB_URL=jdbc:postgresql://<HOST-POOLER>:6543/postgres
SUPABASE_DB_USER=postgres.<TU-PROYECTO>
SUPABASE_DB_PASSWORD=<TU_PASSWORD>

# --- Firma criptográfica de JWT ---
JWT_SECRET=b1e55296f1efd85a92828162e126b7462039307a210da139a600e912c62d6cc54e7b4f85eea9c51ef28408d54beca088
```

> **Seguridad:** El archivo `.env` y los scripts de respaldo de base de datos se encuentran estrictamente protegidos bajo `.gitignore` y nunca se suben al repositorio.

---

### 3. Puesta en Marcha del Backend (Spring Boot)

En una terminal ubicada en la carpeta `backend-techzone`:

```powershell
# Opción recomendada: Script oficial (valida Java 25 y variables .env)
.\iniciar.ps1

# Opción alternativa: Arranque directo con Maven Wrapper
.\mvnw.cmd spring-boot:run
```

*El API REST quedará disponible en:* `http://localhost:8080`

---

### 4. Puesta en Marcha del Frontend (Angular)

En otra terminal ubicada en la carpeta `frontend-techzone`:

```bash
# 1. Instalar dependencias
npm install

# 2. Iniciar servidor de desarrollo
npm start
```

*La aplicación web estará disponible en:* `http://localhost:4200`
