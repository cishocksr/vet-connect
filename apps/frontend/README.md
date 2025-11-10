# VetConnect Frontend

Modern React TypeScript application for the VetConnect platform.

## 🛠️ Technology Stack

- **Framework:** React 18 with TypeScript
- **Build Tool:** Vite
- **Styling:** Tailwind CSS
- **UI Components:** Shadcn/ui
- **State Management:** Zustand + TanStack Query
- **Form Handling:** React Hook Form + Zod
- **Routing:** React Router DOM v6
- **HTTP Client:** Axios

## 📁 Project Structure
```
src/
├── components/        # Reusable React components
│   ├── ui/           # Shadcn/ui components
│   ├── auth/         # Authentication components
│   ├── resources/    # Resource-related components
│   └── layout/       # Layout components
├── pages/            # Page components (routes)
├── hooks/            # Custom React hooks
├── services/         # API service layer
├── store/            # Zustand stores
├── types/            # TypeScript type definitions
├── utils/            # Utility functions
└── lib/              # Library configurations
```

## 🚀 Getting Started

### Prerequisites
- Node.js 20+
- pnpm (`npm install -g pnpm`)

### Installation
```bash
# Install dependencies
pnpm install

# Start development server
pnpm dev

# Build for production
pnpm build

# Preview production build
pnpm preview
```

## 🧪 Testing
```bash
# Run tests
pnpm test

# Run tests with coverage
pnpm test:coverage

# Run tests in watch mode
pnpm test:watch
```

## 📝 Environment Variables

Create `.env` file:
```bash
VITE_API_BASE_URL=http://localhost:8080/api
```

For production, create `.env.production`:
```bash
VITE_API_BASE_URL=https://api.vetconnect.com/api
```

## 🎨 UI Components

This project uses [Shadcn/ui](https://ui.shadcn.com/) components. All UI components are in `src/components/ui/`.

### Adding New Components
```bash
npx shadcn-ui@latest add button
```

## 🔐 Authentication Flow

1. User registers/logs in via AuthService
2. JWT tokens stored in localStorage
3. Axios interceptor adds token to requests
4. Token refresh handled automatically
5. Protected routes redirect to login if unauthenticated

## 📦 Key Features

- **Resource Discovery:** Browse and search veteran resources
- **Saved Resources:** Bookmark resources with personal notes
- **User Dashboard:** Manage saved resources
- **Profile Management:** Update user profile and preferences
- **Military Branch Themes:** Dynamic UI theming based on branch of service
- **Responsive Design:** Mobile-first, works on all devices

## 🏗️ Build & Deployment

### Docker Build
```bash
docker build -t vetconnect-frontend .
docker run -p 80:80 vetconnect-frontend
```

### Production Deployment

The frontend is containerized with Nginx. See `Dockerfile` and `nginx.conf` for configuration.

## 🤝 Contributing

See [CONTRIBUTING.md](../../CONTRIBUTING.md) for contribution guidelines.

## 📄 License

Apache License 2.0
