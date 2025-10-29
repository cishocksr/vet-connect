import { useAuthStore } from '../store/auth-store';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import authService from '../services/auth-service';
import type { LoginRequest, RegisterRequest } from '@/types';
import { useNavigate } from 'react-router-dom';

/**
 * Custom hook for authentication
 *
 * Provides login, register, logout functionality
 * and auth state
 */
export function useAuth() {
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const { user, isAuthenticated, setAuth, logout: clearAuth } = useAuthStore();

    // Login mutation
    const loginMutation = useMutation({
        mutationFn: (data: LoginRequest) => authService.login(data),
        onSuccess: (response) => {
            setAuth(response.user, response.token, response.refreshToken);
            queryClient.invalidateQueries({ queryKey: ['user'] });
            navigate('/');
        },
    });

    // Register mutation
    const registerMutation = useMutation({
        mutationFn: (data: RegisterRequest) => authService.register(data),
        onSuccess: (response) => {
            setAuth(response.user, response.token, response.refreshToken);
            queryClient.invalidateQueries({ queryKey: ['user'] });
            navigate('/');
        },
    });

    // Get current user query
    const { data: currentUser, isLoading: isLoadingUser } = useQuery({
        queryKey: ['user', 'me'],
        queryFn: () => authService.getCurrentUser(),
        enabled: isAuthenticated,
        staleTime: 5 * 60 * 1000, // 5 minutes
    });

    // Logout function
    const logout = () => {
        clearAuth();
        queryClient.clear();
        navigate('/login');
    };

    return {
        user: currentUser || user,
        isAuthenticated,
        isLoadingUser,
        login: loginMutation.mutate,
        register: registerMutation.mutate,
        logout,
        isLoggingIn: loginMutation.isPending,
        isRegistering: registerMutation.isPending,
        loginError: loginMutation.error,
        registerError: registerMutation.error,
    };
}