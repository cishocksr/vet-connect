# VetConnect - Veteran Resource Platform

<div align="center">

![VetConnect Logo](docs/images/logo.png)

**Connecting U.S. Veterans with Essential Resources**

[![Build Status](https://github.com/cishocksr/vetconnect/workflows/CI/badge.svg)](https://github.com/cishocksr/vetconnect/actions)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![codecov](https://codecov.io/gh/cishocksr/vetconnect/branch/main/graph/badge.svg)](https://codecov.io/gh/cishockst/vetconnect)

[Features](#features) • [Quick Start](#quick-start) • [Documentation](#documentation) • [Contributing](#contributing)

</div>

---

## 🎖️ About VetConnect

VetConnect is a comprehensive web platform designed to help U.S. military veterans easily discover and access essential resources including:

- 🏠 **Housing Assistance** - Emergency shelter, transitional housing, permanent supportive housing
- 💰 **Financial Aid** - VA benefits, emergency funds, debt assistance
- 🎓 **Education & Training** - GI Bill programs, vocational training, career counseling
- 🧠 **Mental Health Services** - PTSD treatment, counseling, crisis support
- 🏥 **Healthcare** - VA medical centers, community health clinics, specialized care

### Why VetConnect?

Veterans often struggle to navigate the complex landscape of available resources. VetConnect provides:
- ✅ Centralized resource discovery
- ✅ Location-based search (state and nationwide resources)
- ✅ Personalized dashboard for saved resources
- ✅ Detailed resource information with contact details
- ✅ Military branch-themed interface for familiarity

---

## 🛠️ Technology Stack

**Frontend:**
- React 18 + TypeScript
- Vite for fast development
- Tailwind CSS for styling
- TanStack Query for state management
- React Hook Form + Zod for validation

**Backend:**
- Java 21 + Spring Boot 3.5
- PostgreSQL 15 database
- Redis for token blacklist & rate limiting
- JWT authentication with refresh tokens
- Liquibase for database migrations

**Infrastructure:**
- Docker & Docker Compose
- GitHub Actions CI/CD
- AWS deployment (RDS, ElastiCache, ECS)
- Nginx reverse proxy

---

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Node.js 20+
- Docker & Docker Compose
- pnpm (`npm install -g pnpm`)

### Local Development
```bash
# 1. Clone repository
git clone https://github.com/cishocksr/vetconnect.git
cd vetconnect

# 2. Set up environment variables
cp apps/backend/.env.example apps/backend/.env
cp apps/frontend/.env.example apps/frontend/.env

# Edit .env files with your local configuration

# 3. Start infrastructure (PostgreSQL + Redis)
docker-compose up -d

# 4. Start backend
cd apps/backend
mvn spring-boot:run

# 5. Start frontend (new terminal)
cd apps/frontend
pnpm install
pnpm dev
```

Access the application at `http://localhost:5173`

---

## 📚 Documentation

- [Backend API Documentation](apps/backend/README.md) - Complete API reference
- [Frontend Documentation](apps/frontend/README.md) - Component library and architecture
- [Deployment Guide](apps/backend/DEPLOYMENT.MD) - Production deployment instructions
- [Contributing Guide](CONTRIBUTING.md) - How to contribute

---

## 🏗️ Project Structure
```
vetconnect/
├── apps/
│   ├── backend/          # Spring Boot REST API
│   ├── frontend/         # React TypeScript application
│   └── docs/            # Documentation site (optional)
├── .github/
│   └── workflows/       # CI/CD pipelines
├── docker-compose.yml   # Local development infrastructure
└── turbo.json          # Monorepo configuration
```

---

## 🔐 Security Features

- ✅ JWT authentication with token blacklisting
- ✅ Redis-backed refresh token rotation
- ✅ BCrypt password hashing
- ✅ Rate limiting on authentication endpoints
- ✅ CORS protection
- ✅ SQL injection prevention via JPA
- ✅ Input validation on all endpoints
- ✅ Secure file uploads with type validation
- ✅ OWASP dependency scanning

---

## 🧪 Testing
```bash
# Backend tests
cd apps/backend
mvn test
mvn jacoco:report  # Generate coverage report

# Frontend tests
cd apps/frontend
pnpm test
pnpm test:coverage
```

---

## 🔧 Environment Variables

### Backend (`apps/backend/.env`)
```bash
# Database
POSTGRES_DB=vet_connect
POSTGRES_USER=vetconnect_user
POSTGRES_PASSWORD=<generate with: openssl rand -base64 32>
POSTGRES_HOST=localhost
POSTGRES_PORT=5432

# JWT (CRITICAL: Generate unique secret for production)
JWT_SECRET=<generate with: openssl rand -base64 64>
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
JWT_ISSUER_URI=http://localhost:8080

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=<leave empty for dev, set for prod>

# CORS (CRITICAL: Set to production domain in prod)
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000

# Profile
SPRING_PROFILES_ACTIVE=dev
```

### Frontend (`apps/frontend/.env`)
```bash
VITE_API_BASE_URL=http://localhost:8080/api
```

## 🤝 Contributing

This project welcomes contributions from the community. Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

---

## 📄 License

Licensed under the Apache License 2.0 - see [LICENSE](LICENSE) file for details.

---

## 💙 Acknowledgments

Built with dedication to serve those who served. Thank you to all U.S. military veterans for your service.

**Note:** This is a learning project designed to help veterans access resources while demonstrating modern full-stack development practices.

---

## 📧 Contact

- Project Repository: [https://github.com/cishocksr/vetconnect](https://github.com/cishocksr/vetconnect)
- Report Issues: [https://github.com/cishocksr/vetconnect/issues](https://github.com/cishocksr/vetconnect/issues)

---

<div align="center">
Made with ❤️ for U.S. Veterans
</div>
