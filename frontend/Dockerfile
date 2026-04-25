# syntax=docker/dockerfile:1.7

# Stage 1: build the React production bundle
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
ARG REACT_APP_API_BASE_URL=/api/v1
ENV REACT_APP_API_BASE_URL=${REACT_APP_API_BASE_URL}
RUN npm run build

# Stage 2: serve static assets with nginx
FROM nginxinc/nginx-unprivileged:1.27-alpine AS runtime
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 8080
