import { useAuthStore } from '../store/auth-store';
import { useNavigate } from 'react-router-dom';
import authService from '../services/auth-service';
import type { LoginRequest, RegisterRequest, User } from '../types';

export function useAuth() {
    const { user, isAuthenticated, setUser, clearUser } = useAuthStore();
    const navigate = useNavigate();

    const login = async (credentials: LoginRequest) => {
        const response = await authService.login(credentials);
        setUser(response.user, response.token);
        navigate('/dashboard');
    };

    const register = async (data: RegisterRequest) => {
        const response = await authService.register(data);
        setUser(response.user, response.token);
        navigate('/dashboard');
    };

    const logout = () => {
        clearUser();
        navigate('/login');
    };

    // Add updateUser function for profile updates
    const updateUser = (updatedUser: User) => {
        const currentToken = localStorage.getItem('token');
        if (currentToken) {
            setUser(updatedUser, currentToken);
        }
    };

    return {
        user,
        isAuthenticated,
        login,
        register,
        logout,
        setUser: updateUser, // Expose as setUser
    };
}