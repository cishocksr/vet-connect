import { create } from 'zustand';
import type { User } from '../types';

interface AuthState {
    user: User | null;
    isAuthenticated: boolean;
    isAdmin: boolean;  // ADD THIS LINE
    setUser: (user: User, token: string) => void;
    clearUser: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    isAuthenticated: !!localStorage.getItem('token'),
    isAdmin: JSON.parse(localStorage.getItem('user') || 'null')?.role === 'ADMIN',

    setUser: (user: User, token: string, refreshToken?: string) => {  // ADD refreshToken param
        localStorage.setItem('user', JSON.stringify(user));
        localStorage.setItem('token', token);
        if (refreshToken) {  // ADD THIS
            localStorage.setItem('refreshToken', refreshToken);
        }
        set({
            user,
            isAuthenticated: true,
            isAdmin: user.role === 'ADMIN'
        });
    },

    // ADD THIS NEW METHOD
    updateTokens: (token: string, refreshToken: string) => {
        localStorage.setItem('token', token);
        localStorage.setItem('refreshToken', refreshToken);
        set({ isAuthenticated: true });
    },

    clearUser: () => {
        localStorage.removeItem('user');
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');  // ADD THIS
        set({
            user: null,
            isAuthenticated: false,
            isAdmin: false
        });
    },
}));
