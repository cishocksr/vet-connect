import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/hooks/use-auth.ts';

interface ProtectedRouteProps {
    children: React.ReactNode;
}

/**
 * Protected Route Component
 *
 * Redirects to login if user is not authenticated
 * Preserves the intended destination for redirect after login
 */
export function ProtectedRoute({ children }: ProtectedRouteProps) {
    const { isAuthenticated } = useAuth();
    const location = useLocation();

    if (!isAuthenticated) {
        // Redirect to login but save the location they were trying to access
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    return <>{children}</>;
}