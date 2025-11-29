import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '../../__test__/utils.tsx';
import { ProtectedRoute } from './protected-route';
import { useAuthStore } from '../../store/auth-store';
import type { User } from '../../types';
import * as ReactRouter from 'react-router-dom';

// Mock Navigate component to verify redirect behavior
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual<typeof ReactRouter>('react-router-dom');
    return {
        ...actual,
        Navigate: vi.fn(({ to, state }) => (
            <div data-testid="navigate" data-to={to} data-state={JSON.stringify(state)}>
                Redirecting to {to}...
            </div>
        )),
    };
});

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
        // Reset auth store to unauthenticated state
        useAuthStore.setState({
            user: null,
            isAuthenticated: false,
        });
        
        // Clear any mock calls
        vi.clearAllMocks();
    });

    it('should render children when user is authenticated', () => {
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
        expect(screen.queryByTestId('navigate')).not.toBeInTheDocument();
    });

    it('should redirect to login when user is not authenticated', () => {
        render(
            <ProtectedRoute>
                <div>Protected Content</div>
            </ProtectedRoute>
        );

        // Should not render protected content
        expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
        
        // Should render Navigate component redirecting to login
        const navigate = screen.getByTestId('navigate');
        expect(navigate).toBeInTheDocument();
        expect(navigate).toHaveAttribute('data-to', '/login');
    });

    it('should preserve intended location when redirecting to login', () => {
        render(
            <ProtectedRoute>
                <div>Protected Content</div>
            </ProtectedRoute>
        );

        const navigate = screen.getByTestId('navigate');
        const stateAttr = navigate.getAttribute('data-state');
        
        expect(stateAttr).toBeTruthy();
        if (stateAttr) {
            const state = JSON.parse(stateAttr);
            expect(state).toHaveProperty('from');
            expect(state.from).toHaveProperty('pathname');
        }
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
        expect(screen.queryByTestId('navigate')).not.toBeInTheDocument();
    });

    it('should not render children when authentication state is false', () => {
        useAuthStore.setState({
            user: mockUser, // User object exists but not authenticated
            isAuthenticated: false,
        });

        render(
            <ProtectedRoute>
                <div>Protected Content</div>
            </ProtectedRoute>
        );

        expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
        expect(screen.getByTestId('navigate')).toBeInTheDocument();
    });
});
