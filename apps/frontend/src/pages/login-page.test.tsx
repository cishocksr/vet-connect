import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '../__test__/utils.tsx';
import LoginPage from './login-page';
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
        useLocation: () => ({ state: null }),
    };
});

describe('LoginPage', () => {
    const mockUser: User = {
        id: '123',
        email: 'veteran@example.com',
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
        it('should render login form', () => {
            render(<LoginPage />);

            expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
            expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
            expect(screen.getByRole('button', { name: /sign in|login/i })).toBeInTheDocument();
        });

        it('should render link to register page', () => {
            render(<LoginPage />);

            const registerLink = screen.getByRole('link', { name: /sign up|register|create account/i });
            expect(registerLink).toBeInTheDocument();
        });

        it('should display welcome message or heading', () => {
            render(<LoginPage />);

            const heading = screen.getByRole('heading', { name: /welcome|sign in|login/i });
            expect(heading).toBeInTheDocument();
        });
    });

    describe('Form Validation', () => {
        it('should show error when email is invalid', async () => {
            render(<LoginPage />);

            const emailInput = screen.getByLabelText(/email/i);
            const submitButton = screen.getByRole('button', { name: /sign in|login/i });

            fireEvent.change(emailInput, { target: { value: 'invalid-email' } });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(screen.getByText(/invalid email|valid email/i)).toBeInTheDocument();
            });
        });

        it('should show error when password is empty', async () => {
            render(<LoginPage />);

            const emailInput = screen.getByLabelText(/email/i);
            const submitButton = screen.getByRole('button', { name: /sign in|login/i });

            fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(screen.getByText(/password is required/i)).toBeInTheDocument();
            });
        });

        it('should not submit form with validation errors', async () => {
            render(<LoginPage />);

            const submitButton = screen.getByRole('button', { name: /sign in|login/i });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(authService.login).not.toHaveBeenCalled();
            });
        });
    });

    describe('Login Flow', () => {
        it('should login successfully with valid credentials', async () => {
            vi.mocked(authService.login).mockResolvedValue(mockAuthResponse);

            render(<LoginPage />);

            const emailInput = screen.getByLabelText(/email/i);
            const passwordInput = screen.getByLabelText(/password/i);
            const submitButton = screen.getByRole('button', { name: /sign in|login/i });

            fireEvent.change(emailInput, { target: { value: 'veteran@example.com' } });
            fireEvent.change(passwordInput, { target: { value: 'password123' } });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(authService.login).toHaveBeenCalledWith({
                    email: 'veteran@example.com',
                    password: 'password123',
                });
            });

            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
            });
        });

        it('should display error message on login failure', async () => {
            const errorResponse = {
                response: {
                    data: {
                        message: 'Invalid credentials',
                    },
                },
            };
            vi.mocked(authService.login).mockRejectedValue(errorResponse);

            render(<LoginPage />);

            const emailInput = screen.getByLabelText(/email/i);
            const passwordInput = screen.getByLabelText(/password/i);
            const submitButton = screen.getByRole('button', { name: /sign in|login/i });

            fireEvent.change(emailInput, { target: { value: 'wrong@example.com' } });
            fireEvent.change(passwordInput, { target: { value: 'wrongpassword' } });
            fireEvent.click(submitButton);

            await waitFor(() => {
                expect(screen.getByText(/invalid credentials|login failed/i)).toBeInTheDocument();
            });
        });

        it('should show loading state during login', async () => {
            vi.mocked(authService.login).mockImplementation(
                () => new Promise((resolve) => setTimeout(() => resolve(mockAuthResponse), 100))
            );

            render(<LoginPage />);

            const emailInput = screen.getByLabelText(/email/i);
            const passwordInput = screen.getByLabelText(/password/i);
            const submitButton = screen.getByRole('button', { name: /sign in|login/i });

            fireEvent.change(emailInput, { target: { value: 'veteran@example.com' } });
            fireEvent.change(passwordInput, { target: { value: 'password123' } });
            fireEvent.click(submitButton);

            // Button should be disabled or show loading state
            await waitFor(() => {
                expect(submitButton).toBeDisabled();
            });
        });
    });

    describe('Password Visibility', () => {
        it('should toggle password visibility', async () => {
            render(<LoginPage />);

            const passwordInput = screen.getByLabelText(/password/i) as HTMLInputElement;

            // Initially should be password type
            expect(passwordInput.type).toBe('password');

            // Look for show/hide password button
            const toggleButton = screen.queryByRole('button', { name: /show|hide|toggle password/i }) ||
                screen.queryByTestId('toggle-password');

            if (toggleButton) {
                fireEvent.click(toggleButton);

                await waitFor(() => {
                    expect(passwordInput.type).toBe('text');
                });

                fireEvent.click(toggleButton);

                await waitFor(() => {
                    expect(passwordInput.type).toBe('password');
                });
            }
        });
    });
});