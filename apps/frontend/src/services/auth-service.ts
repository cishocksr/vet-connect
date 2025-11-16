import api from './api';
import { AxiosError } from 'axios';
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
        console.log('Attempting login with email:', data.email);
        try {
            const response = await api.post<ApiResponse<AuthResponse>>(
                '/auth/login',
                data
            );
            console.log('Login successful');
            return response.data.data;
        } catch (error) {
            if (error instanceof AxiosError) {
                console.error('Login failed:', error?.response?.status, error?.response?.data);
            }
            throw error;
        }
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
     * Logout user and invalidate token server-side
     */
    async logout(): Promise<void> {
        try {
            // Call backend to blacklist token
            await api.post('/auth/logout');
        } catch (error) {
            // Log error but still clear client-side storage
            console.error('Logout API call failed:', error);
        } finally {
            // Always clear local storage, even if API call fails
            localStorage.removeItem('token');
            localStorage.removeItem('refreshToken');
            localStorage.removeItem('user');
        }
    }
}

export default new AuthService();