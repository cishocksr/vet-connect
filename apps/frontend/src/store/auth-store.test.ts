import { describe, it, expect, beforeEach } from 'vitest';
import { useAuthStore } from './auth-store';
import type { User } from '../types';

// Mock localStorage
const localStorageMock = (() => {
    let store: Record<string, string> = {};

    return {
        getItem: (key: string) => store[key] || null,
        setItem: (key: string, value: string) => {
            store[key] = value.toString();
        },
        removeItem: (key: string) => {
            delete store[key];
        },
        clear: () => {
            store = {};
        },
    };
})();

Object.defineProperty(window, 'localStorage', {
    value: localStorageMock,
});

describe('AuthStore', () => {
    const mockUser: User = {
        id: '123',
        email: 'test@example.com',
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

    const mockToken = 'mock-jwt-token';

    beforeEach(() => {
        localStorage.clear();
        // Reset store state
        useAuthStore.setState({
            user: null,
            isAuthenticated: false,
        });
    });

    describe('Initial State', () => {
        it('should have null user when no user in localStorage', () => {
            const store = useAuthStore.getState();
            expect(store.user).toBeNull();
            expect(store.isAuthenticated).toBe(false);
        });

        it('should have user and token data in localStorage structure', () => {
            localStorage.setItem('user', JSON.stringify(mockUser));
            localStorage.setItem('token', mockToken);

            expect(localStorage.getItem('user')).toBe(JSON.stringify(mockUser));
            expect(localStorage.getItem('token')).toBe(mockToken);
        });
    });

    describe('setUser', () => {
        it('should set user and token in state and localStorage', () => {
            // Access the store and call setUser
            const store = useAuthStore.getState();
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (store as any).setUser(mockUser, mockToken);

            const updatedStore = useAuthStore.getState();
            expect(updatedStore.user).toEqual(mockUser);
            expect(updatedStore.isAuthenticated).toBe(true);
            expect(localStorage.getItem('user')).toBe(JSON.stringify(mockUser));
            expect(localStorage.getItem('token')).toBe(mockToken);
        });

        it('should update user if called multiple times', () => {
            const store = useAuthStore.getState();
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (store as any).setUser(mockUser, mockToken);

            const updatedUser = { ...mockUser, firstName: 'Jane', fullName: 'Jane Doe' };
            const newToken = 'new-token';
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (store as any).setUser(updatedUser, newToken);

            const finalStore = useAuthStore.getState();
            expect(finalStore.user?.firstName).toBe('Jane');
            expect(localStorage.getItem('token')).toBe(newToken);
        });
    });

    describe('clearUser', () => {
        it('should clear user from state and localStorage', () => {
            const store = useAuthStore.getState();

            // First set a user
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (store as any).setUser(mockUser, mockToken);
            expect(useAuthStore.getState().isAuthenticated).toBe(true);

            // Then clear
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (store as any).clearUser();

            const clearedStore = useAuthStore.getState();
            expect(clearedStore.user).toBeNull();
            expect(clearedStore.isAuthenticated).toBe(false);
            expect(localStorage.getItem('user')).toBeNull();
            expect(localStorage.getItem('token')).toBeNull();
        });

        it('should handle clearing when no user exists', () => {
            const store = useAuthStore.getState();
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (store as any).clearUser(); // Should not throw

            const clearedStore = useAuthStore.getState();
            expect(clearedStore.user).toBeNull();
            expect(clearedStore.isAuthenticated).toBe(false);
        });
    });

    describe('State Persistence', () => {
        it('should persist authentication across store access', () => {
            const store = useAuthStore.getState();
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (store as any).setUser(mockUser, mockToken);

            // Access store again
            const newStore = useAuthStore.getState();
            expect(newStore.user).toEqual(mockUser);
            expect(newStore.isAuthenticated).toBe(true);
        });

        it('should maintain consistent state between calls', () => {
            const store = useAuthStore.getState();
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (store as any).setUser(mockUser, mockToken);

            // Multiple accesses should return same data
            const state1 = useAuthStore.getState();
            const state2 = useAuthStore.getState();

            expect(state1.user).toEqual(state2.user);
            expect(state1.isAuthenticated).toBe(state2.isAuthenticated);
        });
    });

    describe('LocalStorage Integration', () => {
        it('should sync with localStorage when setting user', () => {
            const store = useAuthStore.getState();
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (store as any).setUser(mockUser, mockToken);

            const storedUser = localStorage.getItem('user');
            const storedToken = localStorage.getItem('token');

            expect(storedUser).toBe(JSON.stringify(mockUser));
            expect(storedToken).toBe(mockToken);
        });

        it('should remove items from localStorage when clearing user', () => {
            const store = useAuthStore.getState();
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (store as any).setUser(mockUser, mockToken);
            expect(localStorage.getItem('token')).toBeTruthy();

            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (store as any).clearUser();

            expect(localStorage.getItem('user')).toBeNull();
            expect(localStorage.getItem('token')).toBeNull();
        });
    });
});