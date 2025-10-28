import api from './api';
import type { User, ApiResponse } from '../types';

/**
 * User Service
 *
 * Handles user profile operations
 */
class UserService {
    /**
     * Get current user's full profile
     */
    async getCurrentProfile(): Promise<User> {
        const response = await api.get<ApiResponse<User>>('/users/profile');
        return response.data.data;
    }

    /**
     * Update user profile
     */
    async updateProfile(data: Partial<User>): Promise<User> {
        const response = await api.put<ApiResponse<User>>('/users/profile', data);
        return response.data.data;
    }

    /**
     * Get public profile of another user
     */
    async getUserProfile(userId: string): Promise<User> {
        const response = await api.get<ApiResponse<User>>(`/users/${userId}`);
        return response.data.data;
    }
}

export default new UserService();