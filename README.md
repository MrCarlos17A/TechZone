# Sistema de Gestión Integral TechZone · Avance III

**Curso:** Desarrollo Web Integrado  
**Ciclo Académico:** 2026-2 · Ciclo VIII  
**Repositorio Oficial:** [https://github.com/MrCarlos17A/TechZone.git](https://github.com/MrCarlos17A/TechZone.git)  
**Estado del Pipeline CI:** [![TechZone CI](https://github.com/MrCarlos17A/TechZone/actions/workflows/ci.yml/badge.svg)](https://github.com/MrCarlos17A/TechZone/actions/workflows/ci.yml)  

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

TechZone es una plataforma web integral de comercio electrónico y gestión empresarial (ERP/E-Commerce B2C/B2B) orientada a la comercialización, distribución mayorista/minorista y control logístico de hardware gamer de alta gama (tarjetas gráficas serie RTX 50, monitores OLED/QHD de alta tasa de refresco, procesadores, periféricos y accesorios profesionales).

El proyecto está diseñado bajo una arquitectura desacoplada de dos capas (Monorepo Fullstack) con soporte integral para despliegue containerizado en Docker:
1. **Frontend SPA:** Interfaz moderna y reactiva construida con Angular 21, gestión de estado granular basada en Signals, maquetación con Tailwind CSS, soporte integral para Modo Claro / Modo Oscuro, carrito híbrido con persistencia local aislada por usuario, temporizador de inactividad de 25 minutos para personal administrativo y suite de pruebas unitarias con Vitest.
2. **Backend API REST:** Servidor robusto desarrollado en Java 25 con Spring Boot 4.1.1, autenticación stateless mediante JWT con firma de ciclo de vida de instancia del servidor (Server Boot Signature), control de accesos por roles (RBAC), gestión de parámetros fiscales SUNAT, persistencia transaccional sobre PostgreSQL en Supabase, almacenamiento multimedia seguro en Supabase Storage y suite de pruebas unitarias con JUnit 5 y Mockito.
3. **Orquestación en Contenedores:** Configuración completa mediante Docker y Docker Compose para levantar frontend (Nginx) y backend (JVM Temurin) con un único comando sin requerir dependencias locales.

---

## Estructura del Repositorio (Monorepo)

```text
TechZone/
├── docker-compose.yml                          # Orquestación de contenedores (Frontend Nginx + Backend Spring Boot)
├── backend-techzone/                           # API REST - Spring Boot (Java 25)
│   ├── Dockerfile                              # Multi-stage build con Eclipse Temurin (JDK 25 compilador + JRE 25 ejecutor)
│   ├── .dockerignore                           # Exclusión de artefactos compilados, .env y temporales
│   ├── src/main/java/com/tienda/backend_techzone/
│   │   ├── Controller/                         # Endpoints REST agrupados en /api (Auth, Producto, Venta, DNI, Carrito, Admin)
│   │   ├── Model/                              # Entidades JPA (Persona, Usuario, Cliente, Empleado, Producto, Venta, Pago, ConfiguracionSitio)
│   │   ├── Repository/                         # Acceso a datos Spring Data JPA y consultas especializadas
│   │   ├── Service/                            # Lógica de negocio, novedades híbridas, ventas, configuración y transacciones ACID
│   │   ├── Security/                           # Spring Security, filtros JWT con Server Boot ID, hashing BCrypt y control RBAC
│   │   └── BackendTechzoneApplication.java     # Clase principal de arranque de Spring Boot
│   ├── src/test/java/com/tienda/backend_techzone/
│   │   ├── Security/JwtServiceTest.java        # Pruebas unitarias de emisión, validación y expiración de tokens
│   │   └── Service/
│   │       ├── DniServiceTest.java             # Pruebas de consulta y normalización de identidad
│   │       ├── ProductoServiceTest.java        # Pruebas de catálogo, destacados y novedades
│   │       └── VentaServiceTest.java           # Pruebas de órdenes, totales, IGV y ciclo de estados
│   ├── src/main/resources/
│   │   └── application.properties              # Configuración de base de datos Supabase, pool Hikari y JWT
│   ├── src/test/resources/
│   │   └── application.properties              # Configuración aislada con H2 en memoria para pruebas
│   ├── iniciar.ps1                             # Script de arranque automatizado local con detección de Java 25
│   ├── mvnw / mvnw.cmd                         # Maven Wrapper oficial
│   └── pom.xml                                 # Dependencias del proyecto (Spring Boot, JPA, JWT, Postgres)
│
├── frontend-techzone/                          # Cliente Web SPA - Angular 21
│   ├── Dockerfile                              # Multi-stage build con Node 22 (compilación) y Nginx Alpine (servidor web)
│   ├── nginx.conf                              # Configuración Nginx: SPA routing (try_files), compresión Gzip y proxy a /api/
│   ├── .dockerignore                           # Exclusión de node_modules, dist y temporales
│   ├── src/app/
│   │   ├── pages/                              # Vistas principales de la aplicación
│   │   │   ├── home/                           # Página principal y catálogo de destacados (9 ítems)
│   │   │   ├── novedades/                      # Módulo de novedades híbridas (lanzamientos y recientes)
│   │   │   ├── productos/                      # Catálogo interactivo con filtros (categoría, marca, precio)
│   │   │   ├── producto-detalle/               # Ficha técnica, galería de imágenes y compras directas
│   │   │   ├── carrito/                        # Carrito híbrido persistente con soporte para visitantes e inicio de pago
│   │   │   ├── checkout/                       # Pasarela de compra con validación DNI/RENIEC y selección de pago
│   │   │   ├── login/                          # Autenticación, redirección inteligente y control de expiración
│   │   │   ├── registro/                       # Alta de nuevos clientes con verificación de identidad
│   │   │   ├── perfil/                         # Gestión de datos personales del usuario autenticado
│   │   │   ├── mis-pedidos/                    # Historial de compras y visor de comprobantes oficiales SUNAT
│   │   │   └── admin/                          # Panel backoffice con gestión fiscal, inventario y conteos en tiempo real
│   │   ├── components/                         # Componentes reutilizables de UI (Navbar, Footer, Tarjetas, Toasts)
│   │   ├── services/                           # Servicios HTTP reactivos y estado global con Signals
│   │   │   ├── auth.service.ts                 # Autenticación, sesión volátil en sessionStorage y handshake /me
│   │   │   ├── carrito.service.ts              # Carrito persistente con aislamiento estricto por usuario y fusión
│   │   │   ├── inactividad.service.ts          # Temporizador de 25 minutos para personal administrativo con aviso preventivo
│   │   │   ├── configuracion.service.ts        # Datos comerciales y parámetros tributarios SUNAT
│   │   │   └── ...                             # Servicios de productos, ventas, temas y notificaciones
│   │   ├── interceptors/                       # Interceptor HTTP con inyección Bearer y logout inmediato ante 401
│   │   └── models/                             # Interfaces TypeScript y tipado estricto del dominio
│   ├── src/app/pages/mis-pedidos/
│   │   └── mis-pedidos.component.spec.ts       # Pruebas unitarias de comprobantes electrónicos con Vitest
│   ├── src/styles.css                          # Estilos globales y reglas de impresión oficial (@media print)
│   ├── package.json                            # Dependencias npm (Angular 21, Tailwind CSS, Vitest)
│   └── angular.json                            # Configuración de compilación de Angular
│
├── .github/                                    # Automatización CI/CD con GitHub Actions
│   └── workflows/ci.yml                        # Pipeline de integración continua (Spring Boot + Angular)
├── .gitignore                                  # Reglas maestras de exclusión (.env, target, dist, node_modules)
└── README.md                                   # Documentación técnica, diagramas de flujo y manual operativo
```

---

## Flujos y Arquitectura del Sistema

### 1. Arquitectura General y Comunicación entre Capas
```mermaid
flowchart TD
    subgraph FRONTEND["Capa Cliente (Angular 21 SPA)"]
        UI_STORE["Tienda Web / Novedades"]
        UI_CHECKOUT["Checkout / Pasarela"]
        UI_VOUCHER["Visor de Comprobantes SUNAT"]
        UI_ADMIN["Panel Admin con Signals"]
        CART_SVC["Carrito Persistente Aislado"]
        IDLE_SVC["Inactividad 25m (Staff)"]
    end

    subgraph BACKEND["Capa de Negocio (Spring Boot / Java 25)"]
        GW["Filtro JWT con Server Boot Signature"]
        C_AUTH["Auth API"]
        C_DNI["DNI API / RENIEC"]
        C_PROD["Producto API / Novedades"]
        C_VENTA["Venta API / Comprobantes"]
        C_CART["Carrito API"]
        C_ADMIN["Admin API / Configuración Fiscal"]
    end

    subgraph CLOUD["Servicios Cloud y Externos"]
        SUPA_DB[("PostgreSQL en Supabase")]
        SUPA_STORAGE["Supabase Storage (CDN)"]
        RENIEC["API DNI (apis.net.pe)"]
    end

    UI_STORE -->|GET /api/productos/novedades| C_PROD
    C_PROD -->|Consultas optimizadas| SUPA_DB
    C_PROD -.->|URLs CDN| SUPA_STORAGE

    CART_SVC -->|Sincronización de Carrito| C_CART
    C_CART --> SUPA_DB

    UI_CHECKOUT -->|GET /api/dni/:numero| C_DNI
    C_DNI -->|Consulta identidad| RENIEC

    UI_CHECKOUT -->|POST /api/ventas| C_VENTA
    C_VENTA -->|Transacción ACID| SUPA_DB

    UI_VOUCHER -->|GET /api/ventas/mis-pedidos/:id| C_VENTA
    UI_ADMIN -->|Token JWT + RBAC| GW
    GW --> C_ADMIN
    C_ADMIN --> SUPA_DB
    IDLE_SVC -.->|Auto-Logout a los 25 min| UI_ADMIN
```

---

### 2. Ciclo de Vida de una Venta, Pago y Despacho
```mermaid
sequenceDiagram
    autonumber
    actor Cliente as Cliente
    participant Front as Angular SPA
    participant Back as Spring Boot API
    participant RENIEC as Servicio RENIEC
    participant DB as PostgreSQL Supabase
    actor Admin as Administrador / Operador

    Cliente->>Front: Agrega componentes al carrito (Persistencia local aislada)
    Cliente->>Front: Procede al Checkout e ingresa DNI / RUC
    Front->>Back: GET /api/dni/73234768
    Back->>RENIEC: Consulta servicio de identidad
    RENIEC-->>Back: Nombres, apellidos y validación
    Back-->>Front: Datos normalizados del titular
    Cliente->>Front: Selecciona medio de pago y emite compra
    Front->>Back: POST /api/ventas (Detalle + Comprobante)
    Back->>DB: Inicia Transacción ACID
    Back->>DB: Descuenta stock en tabla 'producto'
    Back->>DB: Registra cabecera 'venta' y 'detalle_venta'
    Back->>DB: Registra transacción en 'pago' (CONFIRMADO)
    Back->>DB: Vacía el carrito del usuario tras compra exitosa
    Back-->>Front: Orden generada con código OP-000001
    
    Admin->>Front: Ingresa al Registro de Pagos (/admin)
    Front->>Back: GET /api/admin/ventas
    Back-->>Front: Listado con badge y estado
    Admin->>Front: Clic en botón "Doc" o identificador "V-1"
    Front->>Back: GET /api/admin/ventas/1
    Back-->>Front: VentaCompleta (cliente, items, pagos, comprobante con datos SUNAT)
    Front-->>Admin: Abre modal de Boleta/Factura oficial para imprimir o guardar en PDF
```

---

### 3. Lógica del Algoritmo de Novedades Híbridas
```mermaid
flowchart TD
    START["Petición GET /api/productos/novedades"] --> QUERY["Consulta SQL en PostgreSQL (Supabase)"]
    
    QUERY --> COND1["1° Prioridad: novedad = true<br/>(Productos destacados manualmente por el Administrador)"]
    QUERY --> COND2["2° Prioridad: fecha_ingreso DESC<br/>(Últimos ingresos registrados en almacén)"]
    
    COND1 --> MERGE["Fusión sin duplicados<br/>(LinkedHashSet en Java)"]
    COND2 --> MERGE
    
    MERGE --> JSON["Respuesta JSON ordenada<br/>(40 productos vigentes)"]
    JSON --> UI["Renderizado en Vista Novedades"]
    
    UI --> B1["Badge: 'Lanzamiento'<br/>(Si novedad = true)"]
    UI --> B2["Badge: 'Nuevo Ingreso'<br/>(Si es novedad por fecha reciente)"]
    
    ADMIN["Acción Admin: Toggle Novedad<br/>PUT /api/admin/productos/:id/toggle-novedad"] --> BD_UPDATE["Actualiza columna 'novedad' en BD"]
    BD_UPDATE --> QUERY
```

---

### 4. Ciclo de Facturación Electrónica y Representación Impresa (SUNAT)
```mermaid
flowchart TD
    TRIGGER["Usuario solicita ver comprobante<br/>(Cliente o Administrador)"] --> FETCH["Carga VentaCompleta desde API REST<br/>(/api/ventas/... o /api/admin/ventas/...)"]
    
    FETCH --> VALIDATE{"Longitud Documento<br/>o Razón Social"}
    VALIDATE -- "11 dígitos o SAC/SRL" --> FACTURA["Serie: F001 - Correlativo 8 dígitos<br/>Título: FACTURA DE VENTA ELECTRÓNICA"]
    VALIDATE -- "8 dígitos (DNI)" --> BOLETA["Serie: B001 - Correlativo 8 dígitos<br/>Título: BOLETA DE VENTA ELECTRÓNICA"]
    
    FACTURA --> ASSEMBLE["Ensamblado de Datos Tributarios de la Empresa:<br/>- RUC Oficial (11 dígitos): 20608945123<br/>- Razón Social: TECHZONE GAMING STORE S.A.C.<br/>- Dirección Fiscal y Sede Central<br/>- Leyenda y Autorización SUNAT<br/>- Desglose: Op. Gravada, IGV (18%) y Total"]
    BOLETA --> ASSEMBLE
    
    ASSEMBLE --> SEC["Generación de Código QR y Hash Digital Simulado SUNAT"]
    SEC --> MODAL["Visualización en Pantalla (Modal Oficial)"]
    
    MODAL --> PRINT["Acción: Imprimir / Guardar PDF (window.print)"]
    PRINT --> CSS["Reglas de estilo @media print:<br/>- Oculta sidebar, header, navbar y botones<br/>- Formato de página A4 con tipografía nítida<br/>- Marco limpio y apto para archivo fiscal"]
```

---

### 5. Seguridad Defensiva, Bloqueo de Intentos y Auditoría IP
```mermaid
flowchart TD
    START["Inicio: Intento de Login"] --> CAPTURA["Captura IP y User-Agent del cliente"]
    CAPTURA --> BUSCA{"¿Existe usuario en BD?"}
    
    BUSCA -- No --> LOG_FAIL1["Registra intento fallido en auditoría"] --> RET_401["Error 401: Credenciales inválidas"]
    
    BUSCA -- Sí --> CHECK_LOCK{"¿Cuenta bloqueada?"}
    CHECK_LOCK -- Sí --> RET_423["Error 423: Bloqueo temporal por seguridad"]
    
    CHECK_LOCK -- No --> PASS{"¿Contraseña válida (BCrypt)?"}
    PASS -- No --> INC_FAIL["Incrementa intentos fallidos (+1)"]
    INC_FAIL --> REACH_LIMIT{"¿Alcanzó 5 intentos fallidos?"}
    REACH_LIMIT -- Sí --> LOCK_USER["Aplica bloqueo temporal por 15 min"]
    REACH_LIMIT -- No --> LOG_FAIL2["Registra intento en auditoría"]
    
    PASS -- Sí --> RESET["Resetea contador de intentos a 0"]
    RESET --> GEN_JWT["Genera tokens JWT con claim 'sid' (Server Boot ID)"]
    GEN_JWT --> LOG_OK["Registra login exitoso con IP en bitácora"]
    LOG_OK --> SEND_TOKEN["200 OK: Acceso concedido con roles"]
```

---

### 6. Integración y Validación de Identidad (API DNI y RENIEC)
```mermaid
flowchart TD
    subgraph UI["Formularios Cliente (Angular)"]
        F_REG["Registro de Cuenta"]
        F_PERF["Mi Perfil de Usuario"]
        F_COLAB["Alta de Colaborador"]
        F_CHECKOUT["Checkout de Compra"]
    end

    subgraph BACK["Backend Spring Boot (DniService)"]
        CACHE{"¿DNI existe en BD Local?"}
        DB_HIT["Recupera persona de PostgreSQL"]
        API_CALL["Consulta externa a RENIEC (API REST)"]
        NORM["Normaliza nombres, apellidos y fecha"]
        DTO["Genera respuesta unificada JSON"]
    end

    subgraph FRONT_RES["Reactividad en Cliente (Signals)"]
        AUTO["Autocompleta campos del formulario"]
        BADGE["Muestra indicador de identidad validada"]
    end

    F_REG -->|DNI 8 dígitos| CACHE
    F_PERF -->|DNI 8 dígitos| CACHE
    F_COLAB -->|DNI 8 dígitos| CACHE
    F_CHECKOUT -->|DNI 8 dígitos| CACHE

    CACHE -- Sí --> DB_HIT --> DTO
    CACHE -- No --> API_CALL --> NORM --> DTO

    DTO --> AUTO --> BADGE
```

---

### 7. Ciclo de Vida de Sesión, Inactividad de 25 Minutos y Firma de Instancia
```mermaid
flowchart TD
    subgraph BACKEND_BOOT["Arranque del Backend (Java 25)"]
        GEN_SID["Genera INSTANCIA_SERVIDOR_ID único en memoria<br/>(UUID aleatorio al inicializar JwtService)"]
    end

    subgraph LOGIN_FLOW["Inicio de Sesión"]
        AUTH_REQ["Usuario envía credenciales a /api/auth/login"]
        JWT_EMIT["Emite JWT firmado con claim 'sid' = INSTANCIA_SERVIDOR_ID"]
        FRONT_SAVE["Frontend almacena sesión en sessionStorage<br/>(Se destruye automáticamente al cerrar pestaña o navegador)"]
    end

    subgraph INACTIVITY["Control de Inactividad (Panel Admin / Staff)"]
        TIMER_START["Inicia contador de inactividad física (25 minutos)"]
        USER_ACT["Usuario mueve mouse, pulsa tecla o hace clic"] -->|Throttle cada 2s| RESET_TIMER["Reinicia contador a 0"]
        
        TIMER_START --> CHECK_23{"¿Transcurrieron 23 minutos?"}
        CHECK_23 -- Sí --> MODAL_WARN["Muestra modal con cuenta regresiva de 120s:<br/>'Sesión a punto de expirar'"]
        
        MODAL_WARN --> BTN_CONT["Clic en 'Continuar trabajando' o interacción"] --> RESET_TIMER
        MODAL_WARN --> TIMEOUT_25{"¿Se cumplieron los 25 minutos?"}
        TIMEOUT_25 -- Sí --> FORCE_LOGOUT["Ejecuta authService.logout()<br/>Limpia sessionStorage y redirige a /login?inactividad=true"]
    end

    subgraph SERVER_RESTART["Detección de Reinicio del Servidor"]
        SRV_DOWN["Servidor se apaga o reinicia"] --> NEW_SID["Se genera nuevo INSTANCIA_SERVIDOR_ID"]
        OLD_REQ["Petición entrante con token de instancia previa"]
        NEW_SID --> COMP_SID{"¿sid del token coincide con la instancia actual?"}
        OLD_REQ --> COMP_SID
        COMP_SID -- No --> ERR_401["BadJwtException: 401 Unauthorized"]
        ERR_401 --> INTERCEPTOR["AuthInterceptor detecta 401: Cierra sesión de inmediato"]
    end

    GEN_SID --> JWT_EMIT
    AUTH_REQ --> JWT_EMIT --> FRONT_SAVE --> TIMER_START
```

---

### 8. Arquitectura del Carrito Híbrido Persistente y Aislamiento por Usuario
```mermaid
flowchart TD
    subgraph CLIENT_STORAGE["Almacenamiento Local Aislado (LocalStorage Namespacing)"]
        KEY_GUEST["Clave: 'techzone_cart_guest'<br/>(Productos de visitante anónimo)"]
        KEY_USER_A["Clave: 'techzone_cart_user_101'<br/>(Carrito privado de Carlos)"]
        KEY_USER_B["Clave: 'techzone_cart_user_102'<br/>(Carrito privado de María)"]
    end

    subgraph ACTIONS["Acciones de Compra"]
        ADD_ANON["Visitante agrega productos sin iniciar sesión"]
        ADD_ANON --> KEY_GUEST
        VIEW_CART["Accede a /carrito: visualiza items, precios y subtotales"]
        KEY_GUEST --> VIEW_CART
        
        CLICK_PAY["Clic en 'Proceder al Pago'"]
        CLICK_PAY --> CHECK_AUTH{"¿Está autenticado?"}
        CHECK_AUTH -- No --> GO_LOGIN["Redirige a /login?returnUrl=/checkout"]
        CHECK_AUTH -- Sí --> GO_CHECKOUT["Navega a /checkout"]
    end

    subgraph LOGIN_MERGE["Fusión de Carrito (Cart Merge)"]
        GO_LOGIN --> SUBMIT_LOGIN["Inicia sesión exitosamente"]
        SUBMIT_LOGIN --> DETECT_GUEST{"¿Había items en 'techzone_cart_guest'?"}
        
        DETECT_GUEST -- Sí --> UPLOAD_ITEMS["Envía items locales al backend vía POST /api/carrito/items"]
        UPLOAD_ITEMS --> CLEAR_GUEST["Elimina 'techzone_cart_guest'"]
        CLEAR_GUEST --> FETCH_BD["Carga carrito unificado desde Supabase PostgreSQL"]
        DETECT_GUEST -- No --> FETCH_BD
        
        FETCH_BD --> SAVE_USER["Respalda en clave privada 'techzone_cart_user_{id}'"]
        SAVE_USER --> GO_CHECKOUT
    end

    subgraph LOGOUT_SAFETY["Seguridad al Cambiar de Cuenta en la Misma Computadora"]
        LOGOUT_EVT["Carlos cierra sesión"] --> CLEAR_MEM["Limpia memoria activa de pantalla"]
        MARIA_LOGIN["María inicia sesión en la misma PC"] --> LOAD_MARIA["Carga exclusivamente 'techzone_cart_user_102' y BD de María"]
        LOAD_MARIA -.-> ISOLATED["Aislamiento garantizado: María jamás ve los productos de Carlos"]
    end
```

---

### 9. Arquitectura de Containerización con Docker Compose y Nginx
```mermaid
flowchart TD
    subgraph DOCKER_HOST["Host Docker (docker compose up --build)"]
        subgraph NET["Red Puente Privada (techzone-net)"]
            
            subgraph FRONT_CONT["Contenedor Frontend (techzone-frontend)"]
                NGINX["Servidor Web Nginx (Alpine)<br/>Puerto interno: 80"]
                DIST["Artefactos compilados Angular 21 SPA<br/>(/usr/share/nginx/html)"]
                GZIP["Compresión Gzip activa"]
                SPA_ROUTING["try_files $uri $uri/ /index.html"]
            end

            subgraph BACK_CONT["Contenedor Backend (techzone-backend)"]
                JVM["Eclipse Temurin 25 JRE<br/>Usuario: spring:spring (no root)"]
                SPRING["Spring Boot 4.1.1 API REST<br/>Puerto interno: 8080"]
                ENV_VARS["Variables de entorno (.env):<br/>SUPABASE_DB_URL, JWT_SECRET, etc."]
            end
        end

        PORT_4200["Puerto Host: 4200"] -->|Mapeo 4200:80| NGINX
        PORT_8080["Puerto Host: 8080"] -->|Mapeo 8080:8080| SPRING
        
        NGINX -->|Proxy inverso interno: location /api/| SPRING
    end

    subgraph CLIENT_BROWSER["Navegador del Usuario"]
        BROWSER["Usuario accede a http://localhost:4200"]
    end

    subgraph CLOUD_EXT["Infraestructura Cloud Externa"]
        SUPABASE_EXT[("PostgreSQL en Supabase (Puerto 6543)")]
        STORAGE_EXT["Supabase Storage CDN"]
    end

    BROWSER --> PORT_4200
    SPRING -->|Conexión JDBC SSL| SUPABASE_EXT
    SPRING -->|Upload multimedia| STORAGE_EXT
```

---

## Matriz de Roles, Políticas de Inactividad y Permisos (RBAC)

| Rol | Ámbito | Política de Inactividad | Permisos y Capacidades |
| :--- | :--- | :--- | :--- |
| **Administrador** (`ROLE_ADMINISTRADOR`) | Panel Completo (`/admin/**`) | **25 minutos de inactividad física** (aviso preventivo de 120s al min 23). | Control total de inventario, fijado de novedades editoriales, configuración tributaria del sitio (RUC, razón social, dirección fiscal), inspección y emisión de comprobantes, gestión de proveedores, colaboradores, usuarios y auditoría forense con trazabilidad de IP. |
| **Colaborador** (`ROLE_TRABAJADOR`) | Operativo & Ventas (`/admin/**`) | **25 minutos de inactividad física** (aviso preventivo de 120s al min 23). | Despacho de órdenes de compra, consulta de inventario y stock crítico, verificación de transacciones de pago, visualización de comprobantes fiscales y consulta de clientes. Módulos de auditoría y configuración bloqueados. |
| **Cliente** (`ROLE_CLIENTE`) | Tienda Pública (`/`) | **8 horas continuas** (sin corte por inactividad física; se destruye al cerrar la pestaña). | Catálogo interactivo con filtros, sección de novedades, carrito híbrido persistente con aislamiento por cuenta, checkout digital con validación de DNI/RUC, pasarela de pago, historial en "Mis Pedidos" y visor de comprobantes electrónicos oficiales. |

---

## Catálogo de Endpoints REST (API Reference)

| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/auth/login` | Público | Autenticación de credenciales, control de intentos y generación de JWT con Server Boot ID. |
| `POST` | `/api/auth/registro` | Público | Alta de nuevo cliente en la plataforma con validación previa de identidad. |
| `GET` | `/api/auth/me` | Autenticado | Endpoint de handshake para validar vigencia e instancia activa del token. |
| `GET` | `/api/configuracion` | Público | Obtiene los datos comerciales y parámetros tributarios SUNAT de la empresa. |
| `PUT` | `/api/admin/configuracion` | Admin | Actualiza logotipo, RUC fiscal, razón social, dirección y leyenda oficial SUNAT. |
| `GET` | `/api/productos` | Público | Listado paginado de catálogo con filtros de categoría, marca y stock. |
| `GET` | `/api/productos/destacados` | Público | Obtiene los 9 productos destacados para la portada de inicio. |
| `GET` | `/api/productos/novedades` | Público | Lista ordenada bajo lógica híbrida (fijados manuales + ingresos recientes). |
| `PUT` | `/api/admin/productos/{id}/toggle-novedad` | Admin | Alterna la marca editorial de novedad de un producto. |
| `GET` | `/api/dni/{numero}` | Público | Consulta de documento de identidad con validación en RENIEC. |
| `GET` | `/api/carrito` | Autenticado | Obtiene los ítems y totales del carrito sincronizados en la base de datos. |
| `POST` | `/api/carrito/items` | Autenticado | Agrega o incrementa un producto en el carrito de la cuenta activa. |
| `PUT` | `/api/carrito/items/{id}`| Autenticado | Actualiza la cantidad de un ítem en el carrito del usuario. |
| `DELETE`| `/api/carrito/items/{id}`| Autenticado | Elimina un ítem específico del carrito del usuario. |
| `DELETE`| `/api/carrito` | Autenticado | Vacía por completo el carrito de la cuenta activa. |
| `POST` | `/api/ventas` | Autenticado | Creación de orden de compra, descuento de stock y registro de pago. |
| `GET` | `/api/ventas/mis-pedidos` | Autenticado | Historial de órdenes realizadas por el cliente autenticado. |
| `GET` | `/api/ventas/mis-pedidos/{id}` | Autenticado | Detalle completo de orden y comprobante fiscal del cliente. |
| `GET` | `/api/admin/ventas/{id}` | Admin / Colab | Detalle de venta y datos completos de transacción para panel admin. |
| `PATCH`| `/api/admin/ventas/{id}/estado` | Admin / Colab | Actualiza el estado logístico del pedido (PREPARACION, CAMINO, ENTREGADO). |

---

## Calidad de Software y Automatización CI/CD

El repositorio cuenta con integración continua automatizada mediante **GitHub Actions** configurada en `.github/workflows/ci.yml`. Cada confirmación a la rama `main` ejecuta en paralelo los flujos de validación:

### Resumen de Pruebas Automatizadas
* **Backend (Java 25 + Spring Boot + JUnit 5 + Mockito):**
  * `JwtServiceTest`: Emisión, validación y expiración de tokens seguros con identificación de instancia.
  * `DniServiceTest`: Consumo de API externa, sanitización y caché local.
  * `ProductoServiceTest`: Filtrado de productos vigentes, listado de novedades y toggle de estado.
  * `VentaServiceTest`: Registro de compras, cálculo de subtotal, IGV (18%), total y ciclo de estados.
  * *Resultado:* 16 pruebas ejecutadas exitosamente con base de datos H2 en memoria.
* **Frontend (Angular 21 + Vitest):**
  * `mis-pedidos.component.spec.ts`: Detección de Boleta vs Factura, carga de comprobante y generación de hash digital.
  * `producto-detalle.component.spec.ts`: Inicialización de componentes, galería de fotos y navegación.
  * `toast.service.spec.ts` y `tema.service.spec.ts`: Gestión de notificaciones y cambio de tema Claro / Oscuro.
  * `app.spec.ts`: Montaje general de la aplicación SPA con inyección de monitoreo de inactividad.
  * *Resultado:* 17 pruebas unitarias ejecutadas con éxito al 100%.

---

## Guía de Puesta en Marcha

### Opción 1: Despliegue con Docker Compose (Recomendado)
Levanta toda la infraestructura (Frontend en Nginx + Backend en Spring Boot) con un solo comando, sin requerir la instalación previa de Java 25, Maven ni Node.js en la máquina host:

```bash
docker compose up --build
```
*(O `docker-compose up --build` en versiones clásicas)*

* **Frontend (Angular + Nginx):** `http://localhost:4200`
* **Backend (Spring Boot API):** `http://localhost:8080`

Para detener los contenedores:
```bash
docker compose down
```

---

### Opción 2: Ejecución Local Rápida (Desarrollo Nativo)

#### 1. Requisitos del Entorno
* **Java Development Kit (JDK):** Java 25 configurado en `JAVA_HOME`.
* **Node.js:** Versión 20+ o 22 con gestor `npm`.
* **PostgreSQL:** Instancia activa en Supabase (Transaction Pooler - Puerto 6543).

#### 2. Configuración de Variables (`backend-techzone/.env`)
Copia la plantilla `.env.example` en `backend-techzone/` como `.env` y define tus credenciales:
```bash
cd backend-techzone
cp .env.example .env
```

#### 3. Ejecución del Backend
```powershell
cd backend-techzone
.\iniciar.ps1
```
*API REST disponible en:* `http://localhost:8080`

#### 4. Ejecución del Frontend
```bash
cd frontend-techzone
npm install
npm start
```
*Aplicación web disponible en:* `http://localhost:4200`
