# Sistema de Gestión de TechZone

**Curso:** Desarrollo Web Integrado  
**Ciclo Académico:** 2026-2  
**Repositorio Oficial:** [https://github.com/MrCarlos17A/TechZone.git](https://github.com/MrCarlos17A/TechZone.git)

---

## Integrantes del Proyecto

| N° | Apellidos y Nombres | Código Universitario |
| :---: | :--- | :---: |
| 1 | Arroyo Vega, Carlos Felipe | U23214686 |

---

## Descripción del Proyecto

TechZone es una aplicación web integral diseñada para la gestión, control de inventario y comercialización de productos tecnológicos, desarrollada bajo el patrón de arquitectura MVC (Modelo-Vista-Controlador).

---

## Tecnologías Utilizadas

- **Lenguaje:** Java 21
- **Framework:** Spring Boot 4.1.1
- **Gestor de dependencias y construcción:** Apache Maven
- **Motor de plantillas:** Thymeleaf
- **Base de Datos:** MySQL Server
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

## Configuración y Ejecución Local

### 1. Requisitos previos
- **JDK 21** instalado y configurado en las variables de entorno.
- **Maven 3.9+** (o usar el Maven Wrapper incluido `./mvnw`).
- **MySQL Server 8.0+** en ejecución con la base de datos correspondiente creada.

### 2. Configuración de Base de Datos
Asegúrate de ajustar las credenciales de conexión en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/techzone_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=tu_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
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