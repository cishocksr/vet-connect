# Build stage
FROM node:20-alpine AS builder

WORKDIR /app

# Install pnpm
RUN npm install -g pnpm

# Copy package files
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
COPY apps/frontend/package.json ./apps/frontend/

# Install dependencies
RUN pnpm install --frozen-lockfile

# Copy source code
COPY apps/frontend ./apps/frontend
COPY turbo.json ./

# Build arguments for environment variables
ARG VITE_API_BASE_URL
ARG VITE_APP_NAME
ARG VITE_ENVIRONMENT
ARG VITE_ENABLE_ANALYTICS

# Set environment variables for build
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_APP_NAME=$VITE_APP_NAME
ENV VITE_ENVIRONMENT=$VITE_ENVIRONMENT
ENV VITE_ENABLE_ANALYTICS=$VITE_ENABLE_ANALYTICS

# Build the application
WORKDIR /app/apps/frontend
RUN pnpm build

# Production stage
FROM nginx:alpine

# Copy built assets
COPY --from=builder /app/apps/frontend/dist /usr/share/nginx/html

# Copy nginx configuration
COPY apps/frontend/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
