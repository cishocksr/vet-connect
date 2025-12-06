import { useAuth } from '../../hooks/use-auth';

/**
 * Debug component to display authentication state
 * Remove this after debugging
 */
export function AuthDebug() {
    const { user, isAuthenticated } = useAuth();
    const token = localStorage.getItem('token');
    const refreshToken = localStorage.getItem('refreshToken');

    if (import.meta.env.PROD) return null;
    return (
        <div style={{
            position: 'fixed',
            bottom: '10px',
            right: '10px',
            background: '#1a1a1a',
            color: '#fff',
            padding: '10px',
            borderRadius: '5px',
            fontSize: '12px',
            maxWidth: '300px',
            zIndex: 9999,
            fontFamily: 'monospace'
        }}>
            <div style={{ fontWeight: 'bold', marginBottom: '5px' }}>🔍 Auth Debug</div>
            <div>Authenticated: {isAuthenticated ? '✅' : '❌'}</div>
            <div>User: {user?.email || 'None'}</div>
            <div>Token: {token ? `${token.substring(0, 15)}...` : 'None'}</div>
            <div>Refresh: {refreshToken ? `${refreshToken.substring(0, 15)}...` : 'None'}</div>
            <div>User ID: {user?.id || 'None'}</div>
        </div>
    );
}
