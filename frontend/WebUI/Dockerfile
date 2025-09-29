# Stage 1: build Angular app
FROM node:20 AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration production
# (Angular <17 CLI would be: npm run build -- --prod)

# Stage 2: serve with NGINX
FROM nginx:stable-alpine
COPY --from=build /app/dist/WebUI/browser /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
