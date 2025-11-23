# VetConnect Backend API

<div align="center">

![Build Status](https://github.com/cishocksr/vet-connect/workflows/Backend%20CI%2FCD/badge.svg)
[![codecov](https://codecov.io/gh/cishocksr/vet-connect/branch/main/graph/badge.svg)](https://codecov.io/gh/cishocksr/vet-connect)
[![CodeQL](https://github.com/cishocksr/vet-connect/workflows/CodeQL%20Security%20Scan/badge.svg)](https://github.com/cishocksr/vet-connect/security/code-scanning)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=vetconnect-backend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=vetconnect-backend)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-21-blue.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

**A RESTful API connecting veterans with essential resources and services**

[Features](#-features) •
[Quick Start](#-quick-start) •
[API Documentation](#-api-documentation) •
[Contributing](#-contributing)

</div>
```

---

## 🔐 Step 7: Set Up GitHub Secrets

Go to your GitHub repository → Settings → Secrets and Variables → Actions

Add these secrets:
```
CODECOV_TOKEN=<your-codecov-token>
DOCKER_USERNAME=<your-docker-hub-username>
DOCKER_PASSWORD=<your-docker-hub-password>
SONAR_TOKEN=<your-sonarcloud-token>
SLACK_WEBHOOK=<your-slack-webhook-url>

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-21-blue.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)
![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)

**A RESTful API connecting veterans with essential resources and services**

[Features](#-features) •
[Quick Start](#-quick-start) •
[API Documentation](#-api-documentation) •
[Architecture](#-architecture) •
[Contributing](#-contributing)

</div>

---

## 📋 Table of Contents

- [About the Project](#about-the-project)
- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [Configuration](#-configuration)
- [API Documentation](#-api-documentation)
- [Project Structure](#-project-structure)
- [Database Schema](#-database-schema)
- [Security](#-security)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)
- [License](#-license)

---

## About the Project

VetConnect is a platform designed to help veterans find and access essential resources including:
- 🏠 Housing assistance
- 💰 Financial aid
- 🎓 Educational benefits
- 🧠 Mental health services
- 🏥 Healthcare facilities

This backend API provides secure, scalable endpoints for managing users, resources, and saved items with JWT-based authentication.

---

## ✨ Features

### Core Functionality
- ✅ **User Authentication** - JWT-based secure registration and login
- ✅ **Resource Management** - CRUD operations for veteran resources
- ✅ **Advanced Search** - Filter resources by location, category, and keywords
- ✅ **Saved Resources** - Users can bookmark resources with personal notes
- ✅ **Category System** - Organized resource categories with icons
- ✅ **Profile Management** - User profile and address management
- ✅ **Geolocation Support** - State-based and national resource filtering

### Technical Features
- 🔐 Spring Security with JWT authentication
- 📊 PostgreSQL database with Liquibase migrations
- 📝 Comprehensive API documentation with Swagger/OpenAPI
- ✅ Input validation with Bean Validation
- 🎯 RESTful API design with standardized responses
- 🔄 Transactional data management
- 📦 Lombok for cleaner code
- 🐳 Docker support for PostgreSQL

---

## 🛠 Technology Stack

| Category | Technology | Version |
|----------|-----------|---------|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 3.5.6 |
| **Security** | Spring Security + JWT | Latest |
| **Database** | PostgreSQL | 15 |
| **Migration** | Liquibase | 4.30.0 |
| **ORM** | Hibernate/JPA | 6.6.5 |
| **API Docs** | SpringDoc OpenAPI | 2.7.0 |
| **Build Tool** | Maven | 3.9.x |
| **JWT Library** | jjwt | 0.12.5 |

### Dependencies
```xml
<!-- Core -->
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-validation

<!-- Database -->
- postgresql
- liquibase-core

<!-- Security -->
- jjwt-api, jjwt-impl, jjwt-jackson

<!-- Documentation -->
- springdoc-openapi-starter-webmvc-ui

<!-- Dev Tools -->
- lombok
- spring-boot-devtools
```

---

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

- ☕ **Java 21** or higher ([Download](https://adoptium.net/))
- 📦 **Maven 3.9+** ([Download](https://maven.apache.org/download.cgi))
- 🐳 **Docker & Docker Compose** ([Download](https://docs.docker.com/get-docker/))
- 🔧 **Git** ([Download](https://git-scm.com/downloads))

### Verify Installation
```bash
java -version    # Should show Java 21+
mvn -version     # Should show Maven 3.9+
docker --version # Should show Docker 20+
```

---

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/cishocksr/vet-connect.git
cd vet-connect/apps/backend
```

### 2. Start PostgreSQL Database
```bash
# From project root
cd ../../
docker-compose up -d

# Verify database is running
docker-compose ps
```

### 3. Configure Application

The default configuration works with Docker. If using local PostgreSQL, update `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vetconnect
    username: postgres
    password: postgres
```

### 4. Build the Project
```bash
cd apps/backend
mvn clean install
```

### 5. Run the Application
```bash
mvn spring-boot:run
```

The application will start on **http://localhost:8080**

### 6. Access Swagger UI

Open your browser and navigate to:

**🔗 http://localhost:8080/swagger-ui.html**

---

## ⚙️ Configuration

### Application Properties

**File:** `src/main/resources/application.yml`

#### Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vetconnect
    username: postgres
    password: postgres
```

#### JWT Configuration
```yaml
jwt:
  secret: your-256-bit-secret-key-here  # Change in production!
  expiration: 86400000      # 24 hours
  refresh-expiration: 604800000  # 7 days
```

#### Server Configuration
```yaml
server:
  port: 8080
  servlet:
    context-path: /
```

#### CORS Configuration
```yaml
app:
  cors:
    allowed-origins: http://localhost:5173,http://localhost:3000
```

### Environment Variables

For production, use environment variables instead of hardcoded values:
```bash
export JWT_SECRET="your-super-secure-secret-key-at-least-256-bits"
export DATABASE_URL="jdbc:postgresql://prod-host:5432/vetconnect"
export DATABASE_USERNAME="prod_user"
export DATABASE_PASSWORD="prod_password"
```

---

## 📚 API Documentation

### Swagger UI

Interactive API documentation available at:
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs

### API Endpoints Overview

#### Authentication (`/api/auth`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | ❌ |
| POST | `/api/auth/login` | Login user | ❌ |
| POST | `/api/auth/refresh` | Refresh access token | ❌ |
| POST | `/api/auth/logout` | Logout user | ❌ |

#### Users (`/api/users`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/users/profile` | Get current user profile | ✅ |
| PUT | `/api/users/profile` | Update user profile | ✅ |
| PATCH | `/api/users/address` | Update user address | ✅ |
| PUT | `/api/users/password` | Change password | ✅ |

#### Resources (`/api/resources`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/resources` | Get all resources (paginated) | ❌ |
| GET | `/api/resources/{id}` | Get resource by ID | ❌ |
| GET | `/api/resources/search` | Search resources | ❌ |
| GET | `/api/resources/state/{state}` | Get resources by state | ❌ |
| GET | `/api/resources/national` | Get national resources | ❌ |
| POST | `/api/resources` | Create resource | ✅ |
| PUT | `/api/resources/{id}` | Update resource | ✅ |
| DELETE | `/api/resources/{id}` | Delete resource | ✅ |

#### Categories (`/api/categories`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/categories` | Get all categories | ❌ |
| GET | `/api/categories/with-counts` | Get categories with resource counts | ❌ |
| GET | `/api/categories/{id}` | Get category by ID | ❌ |

#### Saved Resources (`/api/saved`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/saved` | Get user's saved resources | ✅ |
| POST | `/api/saved` | Save a resource | ✅ |
| PATCH | `/api/saved/{id}/notes` | Update notes | ✅ |
| DELETE | `/api/saved/{id}` | Unsave resource | ✅ |

### Example API Calls

#### Register a User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "veteran@example.com",
    "password": "SecurePassword123!",
    "firstName": "John",
    "lastName": "Doe",
    "branchOfService": "ARMY",
    "city": "Ashburn",
    "state": "VA",
    "zipCode": "20147",
    "isHomeless": false
  }'
```

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "veteran@example.com",
    "password": "SecurePassword123!"
  }'
```

#### Search Resources
```bash
curl -X GET "http://localhost:8080/api/resources/search?keyword=housing&state=VA&page=0&size=10"
```

#### Get User Profile (Authenticated)
```bash
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

---

## 📁 Project Structure
```
apps/backend/
├── src/
│   ├── main/
│   │   ├── java/com/vetconnect/
│   │   │   ├── config/              # Configuration classes
│   │   │   │   ├── CorsConfig.java
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   │
│   │   │   ├── controller/          # REST Controllers
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── UserController.java
│   │   │   │   ├── ResourceController.java
│   │   │   │   ├── ResourceCategoryController.java
│   │   │   │   └── SavedResourceController.java
│   │   │   │
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── auth/            # Auth DTOs
│   │   │   │   ├── user/            # User DTOs
│   │   │   │   ├── resource/        # Resource DTOs
│   │   │   │   ├── saved/           # Saved resource DTOs
│   │   │   │   └── common/          # Common response DTOs
│   │   │   │
│   │   │   ├── exception/           # Custom exceptions
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── EmailAlreadyExistsException.java
│   │   │   │   └── ...
│   │   │   │
│   │   │   ├── mapper/              # Entity-DTO mappers
│   │   │   │   ├── UserMapper.java
│   │   │   │   ├── ResourceMapper.java
│   │   │   │   └── ...
│   │   │   │
│   │   │   ├── model/               # JPA Entities
│   │   │   │   ├── User.java
│   │   │   │   ├── Resource.java
│   │   │   │   ├── ResourceCategory.java
│   │   │   │   ├── SavedResource.java
│   │   │   │   └── enums/
│   │   │   │       └── BranchOfService.java
│   │   │   │
│   │   │   ├── repository/          # Spring Data JPA Repositories
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── ResourceRepository.java
│   │   │   │   ├── ResourceCategoryRepository.java
│   │   │   │   └── SavedResourcesRepository.java
│   │   │   │
│   │   │   ├── security/            # Security components
│   │   │   │   ├── CustomUserDetails.java
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   └── JwtAuthenticationEntryPoint.java
│   │   │   │
│   │   │   ├── service/             # Business logic
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── ResourceService.java
│   │   │   │   ├── ResourceCategoryService.java
│   │   │   │   └── SavedResourceService.java
│   │   │   │
│   │   │   └── VetConnectApplication.java  # Main application
│   │   │
│   │   └── resources/
│   │       ├── application.yml      # Application configuration
│   │       └── db/
│   │           └── changelog/       # Liquibase migrations
│   │               ├── db.changelog-master.xml
│   │               └── changes/
│   │                   ├── 001-create-users-table.xml
│   │                   ├── 002-create-resources-categories-table.xml
│   │                   ├── 003-create-resources-table.xml
│   │                   ├── 004-create-saved-resources-table.xml
│   │                   └── 005-insert-initial-categories.xml
│   │
│   └── test/                        # Test classes
│       └── java/com/vetconnect/
│           └── BackendApplicationTests.java
│
├── pom.xml                          # Maven configuration
├── docker-compose.yml               # Docker Compose for PostgreSQL
└── README.md                        # This file
```

---

## 🗄️ Database Schema

### Entity Relationship Diagram
```
┌─────────────────┐
│     USERS       │
├─────────────────┤
│ id (PK)         │──┐
│ email (UNIQUE)  │  │
│ password_hash   │  │
│ first_name      │  │
│ last_name       │  │
│ branch_of_service│ │
│ address         │  │
│ is_homeless     │  │
│ created_at      │  │
│ updated_at      │  │
└─────────────────┘  │
                     │
                     │ 1:N
                     │
    ┌────────────────┼────────────────┐
    │                │                │
    │                │                │
    ▼                │                ▼
┌───────────────┐   │    ┌──────────────────┐
│ SAVED_RESOURCES│  │    │ RESOURCES        │
├───────────────┤   │    ├──────────────────┤
│ id (PK)       │   │    │ id (PK)          │
│ user_id (FK)  │───┘    │ category_id (FK) │──┐
│ resource_id(FK)│────────│ name             │  │
│ notes         │        │ description      │  │
│ saved_at      │        │ website_url      │  │
└───────────────┘        │ phone_number     │  │
                         │ email            │  │
                         │ address          │  │
                         │ is_national      │  │
                         │ created_at       │  │
                         └──────────────────┘  │
                                               │
                                               │ N:1
                                               │
                                               ▼
                                   ┌──────────────────────┐
                                   │ RESOURCE_CATEGORIES  │
                                   ├──────────────────────┤
                                   │ id (PK)              │
                                   │ name                 │
                                   │ description          │
                                   │ icon_name            │
                                   └──────────────────────┘
```

### Initial Categories

The database is pre-populated with these resource categories:

1. **Housing** 🏠 - Shelter and permanent housing resources
2. **Financial** 💰 - Financial assistance and benefits
3. **Education** 🎓 - Educational benefits and training
4. **Mental Health** 🧠 - Mental health services and PTSD support
5. **Healthcare** 🏥 - Primary care and health services

---

## 🔐 Security

### Authentication Flow

1. **User Registration**
    - User submits registration form
    - Password is hashed using BCrypt
    - User record is created in database
    - JWT access and refresh tokens are generated
    - Tokens are returned to client

2. **User Login**
    - User submits email and password
    - Password is verified against stored hash
    - JWT tokens are generated and returned

3. **Accessing Protected Endpoints**
    - Client includes JWT in `Authorization` header
    - Format: `Authorization: Bearer <token>`
    - Server validates token and extracts user info
    - Request is processed if token is valid

### JWT Configuration

- **Algorithm:** HS512
- **Access Token Expiration:** 24 hours
- **Refresh Token Expiration:** 7 days
- **Secret Key:** Minimum 256 bits (configurable in `application.yml`)

### Security Best Practices

- ✅ Passwords hashed with BCrypt
- ✅ JWT tokens signed with HS512
- ✅ CORS configured for frontend origins
- ✅ SQL injection prevention via JPA
- ✅ Input validation on all endpoints
- ✅ HTTPS recommended for production

### Changing JWT Secret

**⚠️ IMPORTANT:** Change the JWT secret before deploying to production!
```yaml
jwt:
  secret: ${JWT_SECRET:your-production-secret-at-least-256-bits-long}
```

Or set via environment variable:
```bash
export JWT_SECRET="your-super-secure-production-secret-key"
```

---

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=BackendApplicationTests
```

### Test Coverage
```bash
mvn clean test jacoco:report
```

View coverage report at: `target/site/jacoco/index.html`

### Manual API Testing

#### Using Swagger UI
1. Start the application
2. Navigate to http://localhost:8080/swagger-ui.html
3. Use the "Try it out" feature to test endpoints

#### Using curl
See [Example API Calls](#example-api-calls) section

#### Using Postman
Import the OpenAPI spec from:
http://localhost:8080/api-docs

---

## 🚢 Deployment

### Building for Production
```bash
# Build JAR file
mvn clean package -DskipTests

# JAR will be in target/backend-0.0.1-SNAPSHOT.jar
```

### Running the JAR
```bash
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Docker Deployment

#### Build Docker Image

Create `Dockerfile`:
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:
```bash
# Build image
docker build -t vetconnect-backend:latest .

# Run container
docker run -p 8080:8080 \
  -e JWT_SECRET=your-secret \
  -e DATABASE_URL=jdbc:postgresql://host:5432/vetconnect \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=postgres \
  vetconnect-backend:latest
```

### Production Checklist

- [ ] Change JWT secret
- [ ] Use production database credentials
- [ ] Enable HTTPS/SSL
- [ ] Configure proper CORS origins
- [ ] Set up monitoring and logging
- [ ] Configure database backups
- [ ] Set appropriate JVM memory settings
- [ ] Use environment variables for sensitive data
- [ ] Enable database connection pooling
- [ ] Configure rate limiting

---

## 🔧 Troubleshooting

### Common Issues

#### Database Connection Failed
```
Connection to localhost:5432 refused
```
**Solution:** Make sure PostgreSQL is running
```bash
docker-compose up -d
docker-compose ps
```

#### JWT Validation Error
```
JWT validation error: Compact JWT strings may not contain whitespace
```
**Solution:** Make sure token is formatted correctly:
```
Authorization: Bearer <token>
```
No extra spaces before or after the token!

#### Liquibase Migration Failed
```
liquibase.exception.LockException: Could not acquire change log lock
```
**Solution:** Clear the lock
```sql
UPDATE databasechangeloglock SET locked = FALSE;
```

#### Port Already in Use
```
Web server failed to start. Port 8080 was already in use
```
**Solution:** Change port in `application.yml` or kill process using port 8080
```bash
# macOS/Linux
lsof -ti:8080 | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Logging

Enable debug logging:
```yaml
logging:
  level:
    com.vetconnect: DEBUG
    org.springframework.security: DEBUG
```

---

## 🤝 Contributing

We welcome contributions! This project is a learning experience and aims to benefit veterans.

### Development Workflow

1. **Fork the repository**
2. **Create a feature branch**
```bash
   git checkout -b feature/amazing-feature
```
3. **Make your changes**
4. **Commit your changes**
```bash
   git commit -m "Add some amazing feature"
```
5. **Push to the branch**
```bash
   git push origin feature/amazing-feature
```
6. **Open a Pull Request**

### Code Style

- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public methods
- Keep methods small and focused
- Write unit tests for new features

### Commit Message Guidelines
```
feat: Add resource search by zip code
fix: Correct JWT expiration validation
docs: Update API documentation
refactor: Simplify ResourceService logic
test: Add tests for AuthController
```

---

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---

## 👥 Authors

- **Christopher Shockley** - *Initial work* - [https://github.com/cishocksr](https://github.com/cishocksr)

---

## 🙏 Acknowledgments

- Built with ❤️ for veterans
- Spring Boot community for excellent documentation
- All contributors who help improve this platform
- Veterans who provide feedback and feature requests

---

## 📞 Support

If you encounter any issues or have questions:

- 📧 Email: support@vetconnect.com
- 🐛 Issues: [GitHub Issues](https://github.com/cishocksr/vet-connect/issues)
- 💬 Discussions: [GitHub Discussions](https://github.com/cishocksr/vet-connect/discussions)

---

## 🗺️ Roadmap

- [ ] Add real-time notifications
- [ ] Implement resource ratings and reviews
- [ ] Add veteran-to-veteran messaging
- [ ] Integrate with external veteran services APIs
- [ ] Add mobile app support
- [ ] Implement advanced analytics
- [ ] Add multi-language support
- [ ] Create admin dashboard

---

<div align="center">

**Made with ❤️ for those who served**

⭐ Star this repo if you find it helpful!

</div>