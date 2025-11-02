import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '../__test__/utils';
import RegisterPage from './register-page';
import authService from '../services/auth-service';
import { useAuthStore } from '../store/auth-store';
import type { AuthResponse, User } from '../types';

// Mock services
vi.mock('../services/auth-service');

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockNavigate,
    };
});

describe('RegisterPage', () => {
    const mockUser: User = {
        id: '123',
        email: 'newvet@example.com',
        firstName: 'John',
        lastName: 'Doe',
        fullName: 'John Doe',
        branchOfService: 'ARMY',
        branchDisplayName: 'United States Army',
        addressLine1: '123 Main St',
        city: 'Arlington',
        state: 'VA',
        zipCode: '22201',
        isHomeless: false,
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
    };

    const mockAuthResponse: AuthResponse = {
        token: 'mock-jwt-token',
        refreshToken: 'mock-refresh-token',
        user: mockUser,
    };

    beforeEach(() => {
        vi.clearAllMocks();
        useAuthStore.setState({
            user: null,
            isAuthenticated: false,
        });
    });

    describe('Rendering', () => {
        it('should render registration form with all required fields', () => {
            render(<RegisterPage />);

            expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
            expect(screen.getByLabelText(/first name/i)).toBeInTheDocument();
            expect(screen.getByLabelText(/last name/i)).toBeInTheDocument();
            expect(screen.getByLabelText(/branch of service|branch/i)).toBeInTheDocument();
            expect(screen.getAllByLabelText(/password/i).length).toBeGreaterThanOrEqual(1);
        });

        it('should render submit button', () => {
            render(<RegisterPage />);

            const submitButton = screen.getByRole('button', { name: /sign up|register|create account/i });
            expect(submitButton).toBeInTheDocument();
        });

        it('should render link to login page', () => {
            render(<RegisterPage />);

            const loginLink = screen.getByRole('link', { name: /sign in|login|already have account/i });
            expect(loginLink).toBeInTheDocument();
        });
    });

    describe('Form Validation', () => {
        it('should show error for invalid email', async () => {
            render(<RegisterPage />);

            const emailInput = screen.getByLabelText(/email/i);
            const submitButton = screen.getByRole('button', { name: /sign up|register|create account/i });

            fireEvent.change(emailInput, { target: { value: 'invalid-email' } });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(screen.getByText(/invalid email|valid email/i)).toBeInTheDocument();
            });
        });

        it('should show error when passwords do not match', async () => {
            render(<RegisterPage />);

            const passwordInputs = screen.getAllByLabelText(/password/i);
            const passwordInput = passwordInputs[0];
            const confirmPasswordInput = passwordInputs[1] || screen.getByLabelText(/confirm password/i);
            const submitButton = screen.getByRole('button', { name: /sign up|register|create account/i });

            fireEvent.change(passwordInput, { target: { value: 'password123' } });
            fireEvent.change(confirmPasswordInput, { target: { value: 'password456' } });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(screen.getByText(/passwords.*match|passwords.*same/i)).toBeInTheDocument();
            });
        });

        it('should show error for short password', async () => {
            render(<RegisterPage />);

            const passwordInput = screen.getAllByLabelText(/password/i)[0];
            const submitButton = screen.getByRole('button', { name: /sign up|register|create account/i });

            fireEvent.change(passwordInput, { target: { value: '123' } });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(screen.getByText(/password.*at least|password.*minimum/i)).toBeInTheDocument();
            });
        });

        it('should require first and last name', async () => {
            render(<RegisterPage />);

            const submitButton = screen.getByRole('button', { name: /sign up|register|create account/i });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(screen.getByText(/first name.*required/i)).toBeInTheDocument();
                expect(screen.getByText(/last name.*required/i)).toBeInTheDocument();
            });
        });

        it('should require branch of service selection', async () => {
            render(<RegisterPage />);

            const submitButton = screen.getByRole('button', { name: /sign up|register|create account/i });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(screen.getByText(/branch.*required|select.*branch/i)).toBeInTheDocument();
            });
        });
    });

    describe('Registration Flow', () => {
        it('should register successfully with valid data', async () => {
            vi.mocked(authService.register).mockResolvedValue(mockAuthResponse);

            render(<RegisterPage />);

            // Fill in all required fields
            fireEvent.change(screen.getByLabelText(/email/i), {
                target: { value: 'newvet@example.com' },
            });
            fireEvent.change(screen.getByLabelText(/first name/i), {
                target: { value: 'John' },
            });
            fireEvent.change(screen.getByLabelText(/last name/i), {
                target: { value: 'Doe' },
            });

            const passwordInputs = screen.getAllByLabelText(/password/i);
            fireEvent.change(passwordInputs[0], { target: { value: 'password123' } });

            const confirmPasswordInput = passwordInputs[1] || screen.getByLabelText(/confirm password/i);
            fireEvent.change(confirmPasswordInput, { target: { value: 'password123' } });

            // Select branch of service
            const branchSelect = screen.getByLabelText(/branch of service|branch/i);
            fireEvent.change(branchSelect, { target: { value: 'ARMY' } });

            const submitButton = screen.getByRole('button', { name: /sign up|register|create account/i });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(authService.register).toHaveBeenCalled();
            });

            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
            });
        });

        it('should display error on registration failure', async () => {
            const errorResponse = {
                response: {
                    data: {
                        message: 'Email already exists',
                    },
                },
            };
            vi.mocked(authService.register).mockRejectedValue(errorResponse);

            render(<RegisterPage />);

            // Fill form
            fireEvent.change(screen.getByLabelText(/email/i), {
                target: { value: 'existing@example.com' },
            });
            fireEvent.change(screen.getByLabelText(/first name/i), {
                target: { value: 'John' },
            });
            fireEvent.change(screen.getByLabelText(/last name/i), {
                target: { value: 'Doe' },
            });

            const passwordInputs = screen.getAllByLabelText(/password/i);
            fireEvent.change(passwordInputs[0], { target: { value: 'password123' } });

            const confirmPasswordInput = passwordInputs[1] || screen.getByLabelText(/confirm password/i);
            fireEvent.change(confirmPasswordInput, { target: { value: 'password123' } });

            const branchSelect = screen.getByLabelText(/branch of service|branch/i);
            fireEvent.change(branchSelect, { target: { value: 'ARMY' } });

            const submitButton = screen.getByRole('button', { name: /sign up|register|create account/i });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(screen.getByText(/email already exists|registration failed/i)).toBeInTheDocument();
            });
        });

        it('should show loading state during registration', async () => {
            vi.mocked(authService.register).mockImplementation(
                () => new Promise((resolve) => setTimeout(() => resolve(mockAuthResponse), 100))
            );

            render(<RegisterPage />);

            // Fill minimum required fields
            fireEvent.change(screen.getByLabelText(/email/i), {
                target: { value: 'test@example.com' },
            });
            fireEvent.change(screen.getByLabelText(/first name/i), {
                target: { value: 'John' },
            });
            fireEvent.change(screen.getByLabelText(/last name/i), {
                target: { value: 'Doe' },
            });

            const passwordInputs = screen.getAllByLabelText(/password/i);
            fireEvent.change(passwordInputs[0], { target: { value: 'password123' } });

            const confirmPasswordInput = passwordInputs[1] || screen.getByLabelText(/confirm password/i);
            fireEvent.change(confirmPasswordInput, { target: { value: 'password123' } });

            const branchSelect = screen.getByLabelText(/branch of service|branch/i);
            fireEvent.change(branchSelect, { target: { value: 'ARMY' } });

            const submitButton = screen.getByRole('button', { name: /sign up|register|create account/i });
            fireEvent.click(submitButton);

            // Should be disabled during submission
            await waitFor(() => {
                expect(submitButton).toBeDisabled();
            });
        });
    });

    describe('Optional Fields', () => {
        it('should allow registration without optional address fields', async () => {
            vi.mocked(authService.register).mockResolvedValue(mockAuthResponse);

            render(<RegisterPage />);

            // Fill only required fields
            fireEvent.change(screen.getByLabelText(/email/i), {
                target: { value: 'test@example.com' },
            });
            fireEvent.change(screen.getByLabelText(/first name/i), {
                target: { value: 'John' },
            });
            fireEvent.change(screen.getByLabelText(/last name/i), {
                target: { value: 'Doe' },
            });

            const passwordInputs = screen.getAllByLabelText(/password/i);
            fireEvent.change(passwordInputs[0], { target: { value: 'password123' } });

            const confirmPasswordInput = passwordInputs[1] || screen.getByLabelText(/confirm password/i);
            fireEvent.change(confirmPasswordInput, { target: { value: 'password123' } });

            const branchSelect = screen.getByLabelText(/branch of service|branch/i);
            fireEvent.change(branchSelect, { target: { value: 'ARMY' } });

            const submitButton = screen.getByRole('button', { name: /sign up|register|create account/i });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(authService.register).toHaveBeenCalled();
            });
        });
    });
});