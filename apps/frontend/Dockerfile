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

# Build argument for environment
ARG BUILD_ENV=production
ENV NODE_ENV=production

# Build the application with environment-specific config
WORKDIR /app/apps/frontend
RUN if [ "$BUILD_ENV" = "staging" ]; then \
      pnpm build --mode staging; \
    else \
      pnpm build --mode production; \
    fi

# Production stage
FROM nginx:alpine

# Copy built assets
COPY --from=builder /app/apps/frontend/dist /usr/share/nginx/html

# Copy nginx configuration
COPY apps/frontend/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]