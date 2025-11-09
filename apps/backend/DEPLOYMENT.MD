# VetConnect Backend - Production Deployment Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Environment Variables](#environment-variables)
3. [Pre-Deployment Checklist](#pre-deployment-checklist)
4. [Deployment Steps](#deployment-steps)
5. [Post-Deployment Verification](#post-deployment-verification)
6. [Monitoring & Maintenance](#monitoring--maintenance)
7. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software
- Java 21 or higher
- PostgreSQL 15 or higher
- Redis 7 or higher
- Maven 3.9 or higher (for building)

### Required Accounts/Services
- Database server (PostgreSQL)
- Redis server (for token blacklist and rate limiting)
- File storage for uploads
- (Optional) APM service (Datadog, New Relic, etc.)
- (Optional) Log aggregation (ELK, Splunk, etc.)

---

## Environment Variables

### Required (CRITICAL - Must Set)

```bash
# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# JWT Configuration
JWT_SECRET=<64+ character secret from: openssl rand -base64 64>
JWT_EXPIRATION=86400000  # 24 hours
JWT_REFRESH_EXPIRATION=604800000  # 7 days

# Database
POSTGRES_HOST=<your-db-host>
POSTGRES_PORT=5432
POSTGRES_DB=vet_connect
POSTGRES_USER=<your-db-user>
POSTGRES_PASSWORD=<strong-password-16+-chars>

# Redis
REDIS_HOST=<your-redis-host>
REDIS_PORT=6379
REDIS_PASSWORD=<redis-password-from: openssl rand -base64 32>

# CORS (your actual domain - NO localhost!)
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com

# Database Pool
DB_POOL_MAX=20
DB_POOL_MIN=5

# File Upload
FILE_UPLOAD_DIR=/app/uploads/profile-pictures

# Trusted Proxies (if behind load balancer)
TRUSTED_PROXY_IPS=10.0.1.100,10.0.1.101

# Logging
LOG_FILE=/var/log/vet-connect/application.log