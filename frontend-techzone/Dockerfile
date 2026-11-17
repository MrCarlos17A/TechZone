# ==============================================================
# TechZone - Dockerfile Frontend (Angular 21 & Node 22)
# Multi-stage build con Nginx para servir la aplicacion SPA
# ==============================================================

# Etapa 1: Compilacion de la aplicacion Angular
FROM node:22-alpine AS builder

WORKDIR /app

# Copiar manifiestos de dependencias
COPY package*.json ./

# Instalar dependencias exactas
RUN npm ci --legacy-peer-deps

# Copiar codigo fuente del frontend
COPY . .

# Compilar proyecto en modo produccion
RUN npm run build

# Etapa 2: Servidor web ultraligero con Nginx
FROM nginx:alpine AS runner

# Copiar configuracion optimizada de Nginx con soporte SPA y proxy
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copiar artefactos web generados por Angular
COPY --from=builder /app/dist/frontend-techzone/browser /usr/share/nginx/html

# Garantizar compatibilidad con index.csr.html si es generado por Angular SSR/CSR
RUN if [ -f /usr/share/nginx/html/index.csr.html ] && [ ! -f /usr/share/nginx/html/index.html ]; then \
        cp /usr/share/nginx/html/index.csr.html /usr/share/nginx/html/index.html; \
    fi

# Exponer el puerto HTTP estandar
EXPOSE 80

# Iniciar servidor Nginx en primer plano
CMD ["nginx", "-g", "daemon off;"]
