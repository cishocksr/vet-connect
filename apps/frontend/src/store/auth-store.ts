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
    isAdmin: JSON.parse(localStorage.getItem('user') || 'null')?.role === 'ADMIN',  // ADD THIS LINE

    setUser: (user: User, token: string) => {
        localStorage.setItem('user', JSON.stringify(user));
        localStorage.setItem('token', token);
        set({
            user,
            isAuthenticated: true,
            isAdmin: user.role === 'ADMIN'  // ADD THIS LINE
        });
    },

    clearUser: () => {
        localStorage.removeItem('user');
        localStorage.removeItem('token');
        set({
            user: null,
            isAuthenticated: false,
            isAdmin: false  // ADD THIS LINE
        });
    },
}));