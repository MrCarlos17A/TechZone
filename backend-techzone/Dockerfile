# ==============================================================
# TechZone - Dockerfile Backend (Spring Boot 4.1.1 & Java 25)
# Multi-stage build optimizado para produccion
# ==============================================================

# Etapa 1: Compilacion y empaquetado
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# Copiar configuracion Maven y wrapper
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Normalizar saltos de linea y otorgar permisos de ejecucion
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Descargar dependencias para aprovechar la cache de capas Docker
RUN ./mvnw dependency:go-offline -B || true

# Copiar codigo fuente del backend y empaquetar jar excluyendo tests
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Etapa 2: Runtime ligero
FROM eclipse-temurin:25-jre AS runner

WORKDIR /app

# Crear usuario de sistema sin privilegios de root para mayor seguridad
RUN useradd -r -u 1001 -m spring || adduser -D -u 1001 spring

# Copiar jar generado desde la etapa de compilacion
COPY --from=builder /app/target/*.jar app.jar

# Asignar permisos al usuario de ejecucion
RUN chown -R spring:spring /app

USER spring

# Puerto estandar de la aplicacion Spring Boot
EXPOSE 8080

# Parametros de memoria y soporte de contenedores para la JVM
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"

# Comando de inicio del backend
ENTRYPOINT ["java", "-jar", "app.jar"]
