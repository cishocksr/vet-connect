# Veteran Resource Hub - Development TODO

> **Project Goal**: Build a comprehensive resource platform for Veterans to find housing, financial, education, mental health, and healthcare resources.

## 🎯 Tech Stack

- **Frontend**: React + TypeScript + Vite
- **Backend**: Java Spring Boot
- **Database**: PostgreSQL
- **Monorepo**: Turborepo + pnpm
- **AI Integration**: OpenAI/Claude API + pgvector
- **Deployment**: Docker + GitHub Actions

---

## 📋 Development Phases

### **Phase 1: Project Foundation** (Week 1)

#### 1.1 Initial Setup
- [ ] Initialize monorepo with Turborepo (`npx create-turbo@latest veteran-resource-hub`)
- [ ] Configure pnpm workspace (`pnpm-workspace.yaml`)
- [ ] Set up Git repository with proper `.gitignore`
- [ ] Create project README with setup instructions
- [ ] Set up environment variable structure (`.env.example` files)
- [ ] Create monorepo structure:
  ```
  veteran-resource-hub/
  ├── apps/
  │   ├── frontend/
  │   └── backend/
  ├── packages/
  │   └── shared-types/
  ├── pnpm-workspace.yaml
  ├── turbo.json
  └── package.json
  ```

#### 1.2 Database Setup
- [ ] Install PostgreSQL locally or via Docker
- [ ] Create database: `veteran_resource_hub`
- [ ] Design database schema (see schema section below)
- [ ] Set up Liquibase for database migrations
- [ ] Create initial migration scripts
- [ ] Add sample seed data for testing

---

### **Phase 2: Backend Development** (Weeks 2-3)

#### 2.1 Spring Boot Project Setup
- [ ] Create Spring Boot project with Spring Initializr
- [ ] Add dependencies:
  - [ ] Spring Web
  - [ ] Spring Security
  - [ ] Spring Data JPA
  - [ ] PostgreSQL Driver
  - [ ] Lombok
  - [ ] JWT (io.jsonwebtoken)
  - [ ] Bean Validation
  - [ ] Liquibase
- [ ] Configure `application.yml` for dev/prod environments
- [ ] Set up proper package structure:
  ```
  com.veteranresourcehub/
  ├── config/
  ├── controller/
  ├── service/
  ├── repository/
  ├── model/
  ├── dto/
  ├── exception/
  └── security/
  ```

#### 2.2 Authentication & Security
- [ ] Create User entity and repository
- [ ] Implement UserService with BCrypt password hashing
- [ ] Build JWT utility class (generate, validate, refresh tokens)
- [ ] Create SecurityConfig with JWT filter
- [ ] Implement AuthController:
  - [ ] `POST /api/auth/register`
  - [ ] `POST /api/auth/login`
  - [ ] `POST /api/auth/refresh`
  - [ ] `GET /api/auth/me`
- [ ] Add input validation with `@Valid` annotations
- [ ] Implement custom validation for branch of service enum

#### 2.3 Resource Management
- [ ] Create Resource entity and repository
- [ ] Create ResourceCategory entity and repository
- [ ] Build repositories with custom JPA queries
- [ ] Implement ResourceService:
  - [ ] Filter by category
  - [ ] Search by location
  - [ ] Search by keywords
  - [ ] Pagination support
- [ ] Create ResourceController:
  - [ ] `GET /api/resources` (with filters, pagination)
  - [ ] `GET /api/resources/{id}`
  - [ ] `GET /api/categories`
  - [ ] `GET /api/resources/search?q={query}&category={cat}&state={state}`

#### 2.4 User Dashboard
- [ ] Create SavedResource entity and repository
- [ ] Implement SavedResourceService
- [ ] Create DashboardController:
  - [ ] `POST /api/dashboard/resources/{resourceId}`
  - [ ] `DELETE /api/dashboard/resources/{resourceId}`
  - [ ] `GET /api/dashboard/resources`
  - [ ] `PUT /api/dashboard/resources/{id}/notes`

#### 2.5 Exception Handling & Logging
- [ ] Create `@ControllerAdvice` GlobalExceptionHandler
- [ ] Implement custom exceptions (ResourceNotFoundException, etc.)
- [ ] Add SLF4J logging throughout services
- [ ] Create standardized API response wrapper DTOs
- [ ] Add request/response logging interceptor

#### 2.6 API Documentation
- [ ] Add Springdoc OpenAPI dependency
- [ ] Configure Swagger UI
- [ ] Add API annotations to controllers
- [ ] Test at `http://localhost:8080/swagger-ui.html`

---

### **Phase 3: Frontend Development** (Weeks 4-5)

#### 3.1 React Project Setup
- [ ] Create Vite + React + TypeScript project
- [ ] Install dependencies:
  - [ ] React Router DOM
  - [ ] Axios
  - [ ] TanStack Query (React Query)
  - [ ] Tailwind CSS
  - [ ] Shadcn/ui components
  - [ ] React Hook Form
  - [ ] Zod (validation)
  - [ ] Zustand (state management)
  - [ ] Lucide React (icons)
- [ ] Configure Tailwind CSS with custom theme
- [ ] Set up Shadcn/ui components
- [ ] Create folder structure:
  ```
  src/
  ├── components/
  │   ├── ui/
  │   ├── auth/
  │   ├── resources/
  │   └── dashboard/
  ├── pages/
  ├── hooks/
  ├── services/
  ├── store/
  ├── types/
  ├── utils/
  └── lib/
  ```

#### 3.2 Shared Types Package
- [ ] Create `packages/shared-types` package
- [ ] Define TypeScript interfaces:
  - [ ] User types
  - [ ] Resource types
  - [ ] Category types
  - [ ] API response types
- [ ] Export types for use in frontend

#### 3.3 Authentication Flow
- [ ] Create auth store with Zustand
- [ ] Build Register page with React Hook Form + Zod
- [ ] Build Login page
- [ ] Create ProtectedRoute component
- [ ] Implement axios interceptor for JWT tokens
- [ ] Handle token refresh logic
- [ ] Create Logout functionality
- [ ] Add persistent auth state (localStorage)

#### 3.4 Resource Discovery
- [ ] Create HomePage with category cards
- [ ] Build ResourceListPage with filters:
  - [ ] Category filter dropdown
  - [ ] State/location filter
  - [ ] Search bar
  - [ ] "National Resources" toggle
- [ ] Create ResourceCard component
- [ ] Build ResourceDetailPage with full information
- [ ] Implement pagination component
- [ ] Add "Save to Dashboard" button with loading state

#### 3.5 User Dashboard
- [ ] Create DashboardPage layout
- [ ] Display saved resources in grid/list view
- [ ] Add notes modal/input for each resource
- [ ] Implement remove from dashboard with confirmation
- [ ] Create empty state component when no saved resources
- [ ] Add filter/sort for saved resources

#### 3.6 UI/UX Polish
- [ ] Create responsive navigation bar
- [ ] Add loading skeletons for all data fetching
- [ ] Implement error boundaries
- [ ] Add toast notifications (success/error messages)
- [ ] Create 404 Not Found page
- [ ] Ensure WCAG accessibility standards (aria labels, keyboard navigation)
- [ ] Add form field error messages
- [ ] Implement optimistic UI updates

---

### **Phase 4: AI Integration** (Week 6)

#### 4.1 Database Extension for AI
- [ ] Enable pgvector extension in PostgreSQL
- [ ] Add `embedding` column to resources table
- [ ] Create vector index for similarity search
- [ ] Generate embeddings for existing resources

#### 4.2 AI-Powered Resource Recommendations
- [ ] Add OpenAI/Claude API dependency to backend
- [ ] Create `AIService` class
- [ ] Implement embedding generation
- [ ] Create endpoint: `POST /api/ai/recommend`
- [ ] Build recommendation logic considering:
  - [ ] User's branch of service
  - [ ] User's location
  - [ ] Previously saved resources
  - [ ] User's search history
- [ ] Add "AI Recommendations" section to dashboard UI

#### 4.3 Smart Semantic Search
- [ ] Implement semantic search using embeddings
- [ ] Create natural language search endpoint
- [ ] Update search to combine keyword + semantic results
- [ ] Add "AI-Enhanced Search" badge in UI
- [ ] Test with queries like:
  - "I need help finding a job"
  - "Mental health support for PTSD"
  - "Housing assistance"

#### 4.4 Chatbot Assistant (Bonus)
- [ ] Add chat interface component
- [ ] Create WebSocket/polling for chat
- [ ] Implement `POST /api/ai/chat` endpoint
- [ ] Design system prompt for veteran assistance
- [ ] Can guide users to relevant categories
- [ ] Can answer questions about resources
- [ ] Add chat history persistence

---

### **Phase 5: Testing** (Week 7)

#### 5.1 Backend Testing
- [ ] Write JUnit 5 tests for services
- [ ] Create integration tests with `@SpringBootTest`
- [ ] Test security configurations
- [ ] Add MockMvc tests for controllers
- [ ] Test database repositories
- [ ] Test JWT generation and validation
- [ ] Test exception handling
- [ ] Aim for 70%+ code coverage
- [ ] Run tests with `mvn test`

#### 5.2 Frontend Testing
- [ ] Set up Vitest configuration
- [ ] Write unit tests for utility functions
- [ ] Create React Testing Library component tests
- [ ] Test form validation logic
- [ ] Test protected route behavior
- [ ] Test auth store logic
- [ ] Mock API calls with MSW (Mock Service Worker)
- [ ] Add E2E tests with Playwright (bonus)

---

### **Phase 6: DevOps & Deployment** (Week 8)

#### 6.1 Containerization
- [ ] Create `Dockerfile` for Spring Boot backend
- [ ] Create `Dockerfile` for React frontend
- [ ] Create `docker-compose.yml` for local development
- [ ] Create production `docker-compose.prod.yml`
- [ ] Add `.dockerignore` files
- [ ] Test full stack with Docker locally

#### 6.2 CI/CD Pipeline
- [ ] Create `.github/workflows/ci.yml`:
  - [ ] Run backend tests on PR
  - [ ] Run frontend tests on PR
  - [ ] Lint check
  - [ ] Build Docker images
- [ ] Create `.github/workflows/deploy.yml`:
  - [ ] Deploy backend on merge to main
  - [ ] Deploy frontend on merge to main
- [ ] Set up GitHub Secrets for deployment

#### 6.3 Deployment
- [ ] Deploy PostgreSQL to Supabase/Railway/Neon
- [ ] Deploy backend to Railway/Render/Fly.io
- [ ] Deploy frontend to Vercel/Netlify
- [ ] Configure environment variables for production
- [ ] Set up CORS properly for production
- [ ] Test production deployment
- [ ] Set up custom domain (optional)

#### 6.4 Documentation
- [ ] Write comprehensive README.md:
  - [ ] Project overview
  - [ ] Tech stack
  - [ ] Setup instructions
  - [ ] API endpoints
  - [ ] Environment variables
- [ ] Add architecture diagram (draw.io or excalidraw)
- [ ] Create API documentation page (Swagger)
- [ ] Write CONTRIBUTING.md
- [ ] Add LICENSE file
- [ ] Record demo video for portfolio
- [ ] Create screenshots for README

---

## 🎨 Bonus Features (If Time Permits)

- [ ] Email verification on registration
- [ ] Password reset flow via email
- [ ] User profile editing page
- [ ] Resource rating/review system
- [ ] Admin panel for managing resources
- [ ] Export saved resources as PDF
- [ ] SMS notifications for new resources (Twilio)
- [ ] Dark mode toggle
- [ ] Multi-language support (i18n)
- [ ] Mobile app with React Native (future)

---

## 📊 Database Schema

### Core Tables

```sql
-- users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    branch_of_service VARCHAR(50) NOT NULL, 
    -- Army, Navy, Air Force, Marines, Coast Guard, Space Force
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(2),
    zip_code VARCHAR(10),
    is_homeless BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- resource_categories table
CREATE TABLE resource_categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon_name VARCHAR(50)
);

-- Initial categories to insert
INSERT INTO resource_categories (name, description, icon_name) VALUES
('Housing', 'Resources for finding shelter and permanent housing', 'home'),
('Financial', 'Financial assistance, benefits, and employment', 'dollar-sign'),
('Education', 'Educational benefits, training, and scholarships', 'graduation-cap'),
('Mental Health', 'Mental health services, counseling, and PTSD support', 'brain'),
('Healthcare', 'Primary care, dental, and general health services', 'heart-pulse');

-- resources table
CREATE TABLE resources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id INTEGER REFERENCES resource_categories(id),
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    website_url VARCHAR(500),
    phone_number VARCHAR(20),
    email VARCHAR(255),
    address_line1 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(2),
    zip_code VARCHAR(10),
    is_national BOOLEAN DEFAULT FALSE,
    eligibility_criteria TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- saved_resources table (for personal dashboard)
CREATE TABLE saved_resources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    resource_id UUID REFERENCES resources(id) ON DELETE CASCADE,
    notes TEXT,
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, resource_id)
);

-- Indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_resources_category ON resources(category_id);
CREATE INDEX idx_resources_state ON resources(state);
CREATE INDEX idx_saved_resources_user ON saved_resources(user_id);
CREATE INDEX idx_saved_resources_resource ON saved_resources(resource_id);

-- For AI features (Phase 4)
CREATE EXTENSION IF NOT EXISTS vector;
ALTER TABLE resources ADD COLUMN embedding vector(1536);
CREATE INDEX ON resources USING ivfflat (embedding vector_cosine_ops);
```

---

## 🚀 Getting Started

```bash
# Clone the repository
git clone <your-repo-url>
cd veteran-resource-hub

# Install pnpm globally
npm install -g pnpm

# Install all dependencies
pnpm install

# Set up environment variables
cp apps/backend/.env.example apps/backend/.env
cp apps/frontend/.env.example apps/frontend/.env

# Start PostgreSQL (via Docker)
docker-compose up -d postgres

# Run database migrations
cd apps/backend
./mvnw liquibase:update

# Start backend (from apps/backend)
./mvnw spring-boot:run

# Start frontend (from apps/frontend, new terminal)
pnpm dev
```

---

## 💡 Architecture Decisions

| Decision | Rationale |
|----------|-----------|
| **Monorepo** | Easier local development, shared types, single CI/CD |
| **JWT Authentication** | Stateless, scalable, industry standard |
| **PostgreSQL** | Relational data, JSON support, pgvector for AI |
| **Liquibase** | Professional database migration management |
| **TanStack Query** | Best practice for API state in React |
| **Zustand** | Lightweight state management, easy to learn |
| **Turborepo** | Fast builds, caching, parallel execution |

---

## 📈 What This Showcases

✅ Full-stack development (React + Spring Boot)  
✅ TypeScript expertise  
✅ RESTful API design  
✅ SQL database design & optimization  
✅ Authentication & security (JWT, BCrypt)  
✅ Modern tooling (monorepo, pnpm, Docker)  
✅ AI integration capabilities  
✅ Testing practices (unit + integration)  
✅ DevOps (CI/CD, containerization)  
✅ Responsive design  
✅ Real-world problem solving  

---

## 📝 Notes

- Keep this TODO updated as you progress
- Check off items as you complete them
- Add notes about challenges or decisions
- Update with any additional features you implement
- Track time spent on each phase for portfolio discussion

**Good luck, and thank you for building something that helps veterans! 🇺🇸**