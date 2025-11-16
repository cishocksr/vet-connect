# Testing Strategy & Coverage

## Overview

This document outlines the testing strategy for the VetConnect backend application, current test coverage status, and the roadmap for improving coverage over time.

## Running Tests

```bash
# Run all tests
mvn test

# Run tests with coverage report
mvn clean test jacoco:report

# Run full verification (includes coverage check)
mvn clean verify

# Run specific test class
mvn test -Dtest=UserServiceTest

# View coverage report (after running tests)
open target/site/jacoco/index.html
```

## Current Test Coverage (November 2025)

### Coverage by Package

| Package | Coverage | Status | Priority |
|---------|----------|--------|----------|
| dto.saved | 100% | ✓ Excellent | Maintain |
| dto.user | 100% | ✓ Excellent | Maintain |
| security | 83% | ✓ Excellent | Maintain |
| model.enums | 75% | ✓ Excellent | Maintain |
| dto.common | 67% | ✓ Good | Maintain |
| model | 66% | ✓ Good | Maintain |
| config | 64% | ✓ Good | Maintain |
| dto.resource | 58% | ✓ Good | Maintain |
| util | 43% | ⚠ Needs Work | Medium |
| service | 37% | ⚠ Needs Work | High |
| mapper | 34% | ⚠ Needs Work | High |
| main | 33% | ⚠ Needs Work | Low |
| controller | 26% | ❌ Critical | High |
| exception | 24% | ❌ Critical | High |

### Test Statistics

- **Total Tests**: 178
- **Overall Coverage**: ~45%
- **Coverage Threshold**: 20% (minimum per package)

## Coverage Threshold Strategy

The JaCoCo Maven plugin enforces a **20% minimum line coverage per package**. This intentionally low threshold serves as a baseline quality gate while allowing incremental improvements.

### Why 20%?

1. **Pragmatic Baseline**: Allows builds to pass while establishing a foundation
2. **Incremental Improvement**: Easier to gradually increase than to achieve 60% immediately
3. **Non-Blocking**: Doesn't halt development while coverage improves
4. **Quality Gate**: Still provides value by catching completely untested packages

## Improvement Roadmap

### Phase 1: ✅ Completed (Nov 2025)
- [x] Establish 20% baseline threshold
- [x] Add comprehensive tests for dto.saved package (0% → 100%)
- [x] Add comprehensive tests for dto.user package (14% → 100%)
- [x] Add comprehensive tests for model package (36% → 66%)
- [x] Fix duplicate dependencies in pom.xml
- [x] Document testing strategy

### Phase 2: Target Q1 2026 (30% threshold)
- [ ] Add integration tests for controllers
  - [ ] AdminController tests (priority: high)
  - [ ] UserController tests (priority: high)
  - [ ] SavedResourceController tests (priority: high)
  - [ ] ResourceCategoryController tests (priority: medium)
- [ ] Increase threshold to 30%

### Phase 3: Target Q2 2026 (40% threshold)
- [ ] Add comprehensive exception tests
  - [ ] Custom exception classes
  - [ ] GlobalExceptionHandler coverage
- [ ] Add mapper tests
  - [ ] AdminMapper
  - [ ] SavedResourceMapper
  - [ ] Improve UserMapper coverage
- [ ] Increase threshold to 40%

### Phase 4: Target Q3 2026 (50% threshold)
- [ ] Add comprehensive service tests
  - [ ] SavedResourceService
  - [ ] FileStorageService
  - [ ] ResourceCategoryService
  - [ ] TokenBlacklistService
  - [ ] Improve coverage for UserService, AdminService, RateLimitService
- [ ] Increase threshold to 50%

### Phase 5: Target Q4 2026 (60% threshold)
- [ ] Complete remaining coverage gaps
- [ ] Add edge case tests
- [ ] Add performance tests where applicable
- [ ] Achieve 60% coverage across all packages

## Testing Best Practices

### For Contributors

When adding new code, follow these guidelines:

1. **Write Tests First**: Consider TDD (Test-Driven Development)
2. **Target 60%+ Coverage**: New classes should have good coverage from the start
3. **Test Business Logic**: Focus on testing business rules and edge cases
4. **Use Meaningful Test Names**: Tests should clearly describe what they're testing
5. **Keep Tests Fast**: Unit tests should run in milliseconds
6. **Avoid Test Duplication**: Don't test framework functionality

### Test Types

#### Unit Tests
- Test individual classes in isolation
- Mock external dependencies
- Fast execution
- Located in `src/test/java`

#### Integration Tests
- Test multiple components together
- Use test databases (H2)
- Test REST endpoints
- Named with `*IntegrationTest.java` or `*IntergrationTest.java` suffix

#### Repository Tests
- Test JPA repositories
- Use `@DataJpaTest`
- Test custom queries

### Writing Good Tests

```java
@Test
void descriptiveTestName_givenCondition_thenExpectedBehavior() {
    // Arrange: Set up test data
    User user = TestDataBuilder.createUser();
    
    // Act: Execute the code under test
    UserDTO result = userService.getUserById(user.getId());
    
    // Assert: Verify the results
    assertNotNull(result);
    assertEquals(user.getEmail(), result.getEmail());
}
```

## Mocking Strategy

Use Mockito for mocking dependencies:

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void testUserService() {
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        // ... test logic
    }
}
```

## Coverage Gaps & Known Issues

### High Priority Gaps

1. **Controllers (26% coverage)**
   - Missing: AdminController, UserController, SavedResourceController integration tests
   - Impact: REST API endpoints not fully tested
   - Risk: High (user-facing functionality)

2. **Exceptions (24% coverage)**
   - Missing: Tests for custom exception classes and GlobalExceptionHandler
   - Impact: Error handling not validated
   - Risk: Medium (affects error responses to users)

3. **Mappers (34% coverage)**
   - Missing: AdminMapper, SavedResourceMapper tests
   - Impact: DTO conversions not validated
   - Risk: Medium (could cause data issues)

### Medium Priority Gaps

4. **Services (37% coverage)**
   - Missing: SavedResourceService, FileStorageService, TokenBlacklistService tests
   - Impact: Business logic not fully tested
   - Risk: High (core functionality)

5. **Util (43% coverage)**
   - Missing: Complete InputSanitizer test coverage
   - Impact: Security utility not fully validated
   - Risk: High (security implications)

## CI/CD Integration

### GitHub Actions

The nightly tests workflow (`./github/workflows/nightly-tests.yml`) runs:
- Full test suite
- Code coverage analysis
- Coverage threshold check

If coverage falls below 20% for any package, the build will fail.

### Pull Request Checks

The PR checks workflow monitors coverage but does not fail builds, instead providing warnings when coverage decreases.

## Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [JaCoCo Maven Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)

## Questions?

For questions about testing strategy or coverage requirements, please:
1. Check this document first
2. Review existing tests for examples
3. Ask in pull request comments
4. Contact the maintainers

---

**Last Updated**: November 15, 2025  
**Next Review**: January 2026 (Phase 2 target)
