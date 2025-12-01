import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor } from '../__test__/utils.tsx';
import { useAuth } from './use-auth';
import authService from '../services/auth-service';
import { useAuthStore } from '../store/auth-store';
import type { User, AuthResponse } from '../types';

// Mock services
vi.mock('../services/auth-service');

// Mock only useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockNavigate,
    };
});

describe('useAuth', () => {
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
        role: 'VETERAN',
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
        useAuthStore.setState({
            user: null,
            isAuthenticated: false,
        });
        // Mock authService.logout to resolve successfully
        vi.mocked(authService.logout).mockResolvedValue(undefined);
    });

    describe('Initial State', () => {
        it('should return unauthenticated state initially', () => {
            const { result } = renderHook(() => useAuth());

            expect(result.current.user).toBeNull();
            expect(result.current.isAuthenticated).toBe(false);
        });
    });

    describe('login', () => {
        it('should login user successfully and navigate to dashboard', async () => {
            vi.mocked(authService.login).mockResolvedValue(mockAuthResponse);

            const { result } = renderHook(() => useAuth());

            await result.current.login({
                email: 'veteran@example.com',
                password: 'password123',
            });

            await waitFor(() => {
                expect(result.current.user).toEqual(mockUser);
                expect(result.current.isAuthenticated).toBe(true);
            });

            expect(authService.login).toHaveBeenCalledWith({
                email: 'veteran@example.com',
                password: 'password123',
            });
            expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
        });

        it('should throw error on login failure', async () => {
            const error = new Error('Invalid credentials');
            vi.mocked(authService.login).mockRejectedValue(error);

            const { result } = renderHook(() => useAuth());

            await expect(
                result.current.login({
                    email: 'wrong@example.com',
                    password: 'wrongpass',
                })
            ).rejects.toThrow('Invalid credentials');

            expect(result.current.user).toBeNull();
            expect(result.current.isAuthenticated).toBe(false);
            expect(mockNavigate).not.toHaveBeenCalled();
        });
    });

    describe('register', () => {
        it('should register user successfully and navigate to dashboard', async () => {
            vi.mocked(authService.register).mockResolvedValue(mockAuthResponse);

            const { result } = renderHook(() => useAuth());

            await result.current.register({
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
            });

            await waitFor(() => {
                expect(result.current.user).toEqual(mockUser);
                expect(result.current.isAuthenticated).toBe(true);
            });

            expect(authService.register).toHaveBeenCalledWith({
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
            });
            expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
        });

        it('should throw error on registration failure', async () => {
            const error = new Error('Email already exists');
            vi.mocked(authService.register).mockRejectedValue(error);

            const { result } = renderHook(() => useAuth());

            await expect(
                result.current.register({
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
                })
            ).rejects.toThrow('Email already exists');

            expect(mockNavigate).not.toHaveBeenCalled();
        });
    });

    describe('logout', () => {
        it('should logout user, clear state, and navigate to login', async () => {
            // First set up authenticated state using store directly
            const store = useAuthStore.getState();
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (store as any).setUser(mockUser, 'mock-token');

            const { result } = renderHook(() => useAuth());

            // Verify user is authenticated
            expect(result.current.user).toEqual(mockUser);
            expect(result.current.isAuthenticated).toBe(true);

            // Logout
            await result.current.logout();

            // Verify state is cleared
            await waitFor(() => {
                expect(result.current.user).toBeNull();
                expect(result.current.isAuthenticated).toBe(false);
            });
            expect(mockNavigate).toHaveBeenCalledWith('/login');
            expect(localStorage.getItem('token')).toBeNull();
            expect(localStorage.getItem('user')).toBeNull();
        });

        it('should handle logout when already logged out', async () => {
            const { result } = renderHook(() => useAuth());

            // Should not throw
            await expect(result.current.logout()).resolves.not.toThrow();

            expect(mockNavigate).toHaveBeenCalledWith('/login');
        });
    });

    describe('updateUser via setUser method', () => {
        it('should update user profile when token exists', () => {
            // Set initial state with token using store
            const store = useAuthStore.getState();
            localStorage.setItem('token', 'mock-token');
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (store as any).setUser(mockUser, 'mock-token');

            const { result } = renderHook(() => useAuth());

            const updatedUser: User = {
                ...mockUser,
                firstName: 'Jane',
                fullName: 'Jane Doe'
            };

            // Call setUser from hook result
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            const hookResult = result.current as any;
            hookResult.setUser(updatedUser);

            // Get fresh state
            const finalState = useAuthStore.getState();
            expect(finalState.user?.firstName).toBe('Jane');
            expect(finalState.user?.fullName).toBe('Jane Doe');
        });

        it('should not update user if no token exists', () => {
            const { result } = renderHook(() => useAuth());

            const updatedUser: User = {
                ...mockUser,
                firstName: 'Jane'
            };

            // Call setUser from hook result
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            const hookResult = result.current as any;
            hookResult.setUser(updatedUser);

            // User should remain null
            const finalState = useAuthStore.getState();
            expect(finalState.user).toBeNull();
        });
    });

    describe('Integration', () => {
        it('should complete full auth flow: register -> logout -> login', async () => {
            vi.mocked(authService.register).mockResolvedValue(mockAuthResponse);
            vi.mocked(authService.login).mockResolvedValue(mockAuthResponse);

            const { result } = renderHook(() => useAuth());

            // Register
            await result.current.register({
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
            });

            await waitFor(() => {
                expect(result.current.isAuthenticated).toBe(true);
            });

            // Logout
            await result.current.logout();
            await waitFor(() => {
                expect(result.current.isAuthenticated).toBe(false);
            });

            // Login again
            await result.current.login({
                email: 'newvet@example.com',
                password: 'password123',
            });

            await waitFor(() => {
                expect(result.current.isAuthenticated).toBe(true);
            });
        });
    });
});