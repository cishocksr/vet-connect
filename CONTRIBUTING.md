# Contributing to VetConnect

Thank you for your interest in contributing to VetConnect! This platform helps veterans access essential resources, and your contributions make a real difference.

## 🎯 Code of Conduct

By participating in this project, you agree to maintain a respectful and inclusive environment for all contributors.

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Node.js 20+
- Docker & Docker Compose
- pnpm

### Local Development Setup
```bash
# 1. Fork and clone the repository
git clone https://github.com/YOUR_USERNAME/vetconnect.git
cd vetconnect

# 2. Set up environment variables
cp apps/backend/.env.example apps/backend/.env
cp apps/frontend/.env.example apps/frontend/.env

# 3. Start infrastructure
docker-compose up -d

# 4. Start backend
cd apps/backend
mvn spring-boot:run

# 5. Start frontend
cd apps/frontend
pnpm install
pnpm dev
```

## 📝 How to Contribute

### Reporting Bugs
- Use GitHub Issues
- Include: OS, Java version, Node version, steps to reproduce
- Add logs and screenshots if applicable

### Suggesting Features
- Open an issue with the "feature request" label
- Describe the problem it solves for veterans
- Explain your proposed solution

### Submitting Code

#### 1. **Create a Branch**
```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/bug-description
```

#### 2. **Make Your Changes**
- Write clean, readable code
- Follow existing code style
- Add tests for new functionality
- Update documentation as needed

####

3. **Commit Your Changes**
   Follow conventional commit format:
```bash
git commit -m "feat: add resource filtering by zip code"
git commit -m "fix: correct JWT token expiration validation"
git commit -m "docs: update API documentation for new endpoint"
git commit -m "test: add unit tests for ResourceService"
```

#### 4. **Push and Create Pull Request**
```bash
git push origin feature/your-feature-name
```

Then create a Pull Request on GitHub with:
- Clear description of changes
- Link to related issue
- Screenshots/GIFs for UI changes
- Test evidence

## 🧪 Testing Requirements

### Backend
```bash
cd apps/backend
mvn test                    # Run tests
mvn jacoco:report          # Generate coverage report
```

**Minimum coverage:** 60%

### Frontend
```bash
cd apps/frontend
pnpm test                   # Run tests
pnpm test:coverage         # Generate coverage report
```

**Minimum coverage:** 60%

## 📐 Code Style Guidelines

### Java (Backend)
- Follow Java naming conventions
- Use meaningful variable/method names
- Add JavaDoc for public methods
- Keep methods focused and small (<50 lines)
- Use Lombok annotations to reduce boilerplate

Example:
```java
/**
 * Retrieves resources by category and state
 *
 * @param categoryId Resource category ID
 * @param state US state abbreviation
 * @return List of matching resources
 */
public List<Resource> getResourcesByCategoryAndState(Long categoryId, String state) {
    // Implementation
}
```

### TypeScript (Frontend)
- Use TypeScript strict mode
- Define proper interfaces for all data structures
- Use functional components with hooks
- Follow React best practices

Example:
```typescript
interface ResourceCardProps {
  resource: Resource;
  onSave: (resourceId: string) => void;
}

export const ResourceCard: React.FC<ResourceCardProps> = ({ resource, onSave }) => {
  // Implementation
};
```

## 🔍 Code Review Process

1. **Automated Checks**: CI/CD runs tests, linting, security scans
2. **Maintainer Review**: Code is reviewed for quality and correctness
3. **Feedback**: You may be asked to make changes
4. **Approval**: Once approved, code is merged

## 🎖️ Recognition

Contributors will be recognized in:
- README.md contributors section
- Release notes
- Project documentation

## ❓ Questions?

- Open a GitHub Discussion
- Tag maintainers in issues
- Email: dev@vetconnect.com

---

## Thank You! 🙏

Your contribution helps veterans find the resources they need. Every line of code, every documentation improvement, every bug report makes a difference.

**Made with ❤️ for those who served**
```

---

### 2️⃣ **Missing LICENSE File (Root Level)**

Create `LICENSE`:
```
Apache License
Version 2.0, January 2004
http://www.apache.org/licenses/

Copyright 2025 Christopher Shockley

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.