# Stage 1: Build the React app
FROM node:18-alpine as build

# Set working directory
WORKDIR /app

# Copy package.json and package-lock.json for caching
COPY package*.json ./

# Install dependencies with legacy-peer-deps for React 19 compatibility
RUN npm ci --legacy-peer-deps

# Copy source files
COPY . .

# Run the build script
RUN npm run build

# Stage 2: Serve the build using nginx
FROM nginx:stable-alpine

# Copy the build folder from the build stage
COPY --from=build /app/build /usr/share/nginx/html

# Expose HTTP port
EXPOSE 80

# Run nginx foreground process
CMD ["nginx", "-g", "daemon off;"]
