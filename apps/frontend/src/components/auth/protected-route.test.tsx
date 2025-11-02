import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen } from '../../__test__/utils';
import { ProtectedRoute } from './protected-route';
import { useAuthStore } from '../../store/auth-store';
import type { User } from '../../types';

describe('ProtectedRoute', () => {
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

    beforeEach(() => {
        useAuthStore.setState({
            user: null,
            isAuthenticated: false,
        });
    });

    it('should render children when user is authenticated', () => {
        // Set authenticated state
        useAuthStore.setState({
            user: mockUser,
            isAuthenticated: true,
        });

        render(
            <ProtectedRoute>
                <div>Protected Content</div>
            </ProtectedRoute>
        );

        expect(screen.getByText('Protected Content')).toBeInTheDocument();
    });

    it('should redirect to login when user is not authenticated', () => {
        // User is not authenticated (default state from beforeEach)
        render(
            <ProtectedRoute>
                <div>Protected Content</div>
            </ProtectedRoute>
        );

        // Should not render protected content
        expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
    });

    it('should render complex children when authenticated', () => {
        useAuthStore.setState({
            user: mockUser,
            isAuthenticated: true,
        });

        render(
            <ProtectedRoute>
                <div>
                    <h1>Dashboard</h1>
                    <p>Welcome back, {mockUser.firstName}!</p>
                </div>
            </ProtectedRoute>
        );

        expect(screen.getByText('Dashboard')).toBeInTheDocument();
        expect(screen.getByText(`Welcome back, ${mockUser.firstName}!`)).toBeInTheDocument();
    });
});