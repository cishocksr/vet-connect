import api from './api';
import type {
    AuthResponse,
    LoginRequest,
    RegisterRequest,
    ApiResponse,
    User
} from '../types';

/**
 * Authentication Service
 *
 * Handles all authentication-related API calls
 */
class AuthService {
    /**
     * Register new user
     */
    async register(data: RegisterRequest): Promise<AuthResponse> {
        const response = await api.post<ApiResponse<AuthResponse>>(
            '/auth/register',
            data
        );
        return response.data.data;
    }

    /**
     * Login user
     */
    async login(data: LoginRequest): Promise<AuthResponse> {
        const response = await api.post<ApiResponse<AuthResponse>>(
            '/auth/login',
            data
        );
        return response.data.data;
    }

    /**
     * Refresh access token
     */
    async refreshToken(refreshToken: string): Promise<AuthResponse> {
        const response = await api.post<ApiResponse<AuthResponse>>(
            '/auth/refresh',
            { refreshToken }
        );
        return response.data.data;
    }

    /**
     * Get current user profile
     */
    async getCurrentUser(): Promise<User> {
        const response = await api.get<ApiResponse<User>>('/auth/me');
        return response.data.data;
    }

    /**
     * Logout (client-side only for now)
     */
    logout(): void {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
    }
}

export default new AuthService();