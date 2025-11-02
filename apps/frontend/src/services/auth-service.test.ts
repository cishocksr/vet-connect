import { describe, it, expect, beforeEach, vi } from 'vitest';
import authService from './auth-service';
import api from './api';
import type { AuthResponse, LoginRequest, RegisterRequest, User } from '../types';

// Mock the api module
vi.mock('./api');

describe('AuthService', () => {
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
        localStorage.clear();
    });

    describe('register', () => {
        it('should register a new user successfully', async () => {
            const registerRequest: RegisterRequest = {
                email: 'newvet@example.com',
                password: 'password123',
                confirmPassword: 'password123',
                firstName: 'John',
                lastName: 'Doe',
                branchOfService: 'ARMY',
                city: 'Arlington',
                state: 'VA',
                zipCode: '22201',
                isHomeless: false,
            };

            vi.mocked(api.post).mockResolvedValue({
                data: {
                    success: true,
                    message: 'User registered successfully',
                    data: mockAuthResponse,
                },
            });

            const result = await authService.register(registerRequest);

            expect(api.post).toHaveBeenCalledWith('/auth/register', registerRequest);
            expect(result).toEqual(mockAuthResponse);
            expect(result.token).toBe('mock-jwt-token');
            expect(result.user.email).toBe('veteran@example.com');
        });

        it('should throw error when registration fails', async () => {
            const registerRequest: RegisterRequest = {
                email: 'existing@example.com',
                password: 'password123',
                confirmPassword: 'password123',
                firstName: 'John',
                lastName: 'Doe',
                branchOfService: 'ARMY',
                city: 'Arlington',
                state: 'VA',
                zipCode: '22201',
                isHomeless: false,
            };

            const error = new Error('Email already exists');
            vi.mocked(api.post).mockRejectedValue(error);

            await expect(authService.register(registerRequest)).rejects.toThrow(
                'Email already exists'
            );
        });
    });

    describe('login', () => {
        it('should login user successfully', async () => {
            const loginRequest: LoginRequest = {
                email: 'veteran@example.com',
                password: 'password123',
            };

            vi.mocked(api.post).mockResolvedValue({
                data: {
                    success: true,
                    message: 'Login successful',
                    data: mockAuthResponse,
                },
            });

            const result = await authService.login(loginRequest);

            expect(api.post).toHaveBeenCalledWith('/auth/login', loginRequest);
            expect(result).toEqual(mockAuthResponse);
            expect(result.token).toBeDefined();
            expect(result.refreshToken).toBeDefined();
        });

        it('should throw error when login fails with invalid credentials', async () => {
            const loginRequest: LoginRequest = {
                email: 'wrong@example.com',
                password: 'wrongpassword',
            };

            const error = new Error('Invalid credentials');
            vi.mocked(api.post).mockRejectedValue(error);

            await expect(authService.login(loginRequest)).rejects.toThrow(
                'Invalid credentials'
            );
        });
    });

    describe('refreshToken', () => {
        it('should refresh access token successfully', async () => {
            const refreshToken = 'old-refresh-token';
            const newAuthResponse: AuthResponse = {
                ...mockAuthResponse,
                token: 'new-jwt-token',
                refreshToken: 'new-refresh-token',
            };

            vi.mocked(api.post).mockResolvedValue({
                data: {
                    success: true,
                    message: 'Token refreshed successfully',
                    data: newAuthResponse,
                },
            });

            const result = await authService.refreshToken(refreshToken);

            expect(api.post).toHaveBeenCalledWith('/auth/refresh', { refreshToken });
            expect(result.token).toBe('new-jwt-token');
            expect(result.refreshToken).toBe('new-refresh-token');
        });

        it('should throw error when refresh token is invalid', async () => {
            const refreshToken = 'invalid-token';
            const error = new Error('Invalid refresh token');
            vi.mocked(api.post).mockRejectedValue(error);

            await expect(authService.refreshToken(refreshToken)).rejects.toThrow(
                'Invalid refresh token'
            );
        });
    });

    describe('getCurrentUser', () => {
        it('should get current user profile', async () => {
            vi.mocked(api.get).mockResolvedValue({
                data: {
                    success: true,
                    message: 'User retrieved successfully',
                    data: mockUser,
                },
            });

            const result = await authService.getCurrentUser();

            expect(api.get).toHaveBeenCalledWith('/auth/me');
            expect(result).toEqual(mockUser);
            expect(result.id).toBe('123');
            expect(result.email).toBe('veteran@example.com');
        });

        it('should throw error when user is not authenticated', async () => {
            const error = new Error('Unauthorized');
            vi.mocked(api.get).mockRejectedValue(error);

            await expect(authService.getCurrentUser()).rejects.toThrow('Unauthorized');
        });
    });

    describe('logout', () => {
        it('should clear authentication tokens from localStorage', () => {
            // Set tokens in localStorage
            localStorage.setItem('token', 'mock-token');
            localStorage.setItem('refreshToken', 'mock-refresh-token');
            localStorage.setItem('user', JSON.stringify(mockUser));

            // Call logout
            authService.logout();

            // Verify tokens are removed
            expect(localStorage.getItem('token')).toBeNull();
            expect(localStorage.getItem('refreshToken')).toBeNull();
            expect(localStorage.getItem('user')).toBeNull();
        });

        it('should handle logout when no tokens exist', () => {
            // Should not throw error
            expect(() => authService.logout()).not.toThrow();

            // Verify localStorage is still clean
            expect(localStorage.getItem('token')).toBeNull();
            expect(localStorage.getItem('refreshToken')).toBeNull();
            expect(localStorage.getItem('user')).toBeNull();
        });
    });
});