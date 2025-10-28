import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User } from '../types';

interface AuthState {
    user: User | null;
    token: string | null;
    refreshToken: string | null;
    isAuthenticated: boolean;

    // Actions
    setAuth: (user: User, token: string, refreshToken: string) => void;
    updateUser: (user: User) => void;
    logout: () => void;
}

/**
 * Authentication Store
 *
 * Manages user authentication state
 * Persists to localStorage automatically
 */
export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            user: null,
            token: null,
            refreshToken: null,
            isAuthenticated: false,

            setAuth: (user, token, refreshToken) => {
                // Store tokens in localStorage for API interceptor
                localStorage.setItem('token', token);
                localStorage.setItem('refreshToken', refreshToken);

                set({
                    user,
                    token,
                    refreshToken,
                    isAuthenticated: true,
                });
            },

            updateUser: (user) => {
                set({ user });
            },

            logout: () => {
                // Clear localStorage
                localStorage.removeItem('token');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('user');

                set({
                    user: null,
                    token: null,
                    refreshToken: null,
                    isAuthenticated: false,
                });
            },
        }),
        {
            name: 'auth-storage', // localStorage key
            partialize: (state) => ({
                user: state.user,
                token: state.token,
                refreshToken: state.refreshToken,
                isAuthenticated: state.isAuthenticated,
            }),
        }
    )
);