# Stage 1: build the Angular app
FROM node:20 AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration production
# If Angular < v17 CLI: RUN npm run build -- --prod

# Stage 2: serve with NGINX
FROM nginx:stable-alpine
# Optional: custom NGINX for SPA routing (see below)
COPY --from=build /app/dist/WebUI /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
