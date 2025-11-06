import { Navigate } from 'react-router-dom';
import { useAuthStore } from '@/store/auth-store';

interface AdminRouteProps {
    children: React.ReactNode;
}

/**
 * Admin Route Component
 *
 * Redirects to home if user is not authenticated or not an admin
 */
export function AdminRoute({ children }: AdminRouteProps) {
    const { isAuthenticated, isAdmin } = useAuthStore();

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (!isAdmin) {
        return <Navigate to="/dashboard" replace />;
    }

    return <>{children}</>;
}