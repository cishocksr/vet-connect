import api from './api';
import type { User, ApiResponse, UpdateProfileRequest, UpdateAddressRequest, UpdatePasswordRequest } from '../types';

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
     * Update user profile (partial update)
     */
    async updateProfile(data: UpdateProfileRequest): Promise<User> {
        const response = await api.put<ApiResponse<User>>('/users/profile', data);
        return response.data.data;
    }

    /**
     * Update address specifically
     */
    async updateAddress(data: UpdateAddressRequest): Promise<User> {
        const response = await api.patch<ApiResponse<User>>('/users/address', data);
        return response.data.data;
    }

    /**
     * Change password
     */
    async changePassword(data: UpdatePasswordRequest): Promise<void> {
        await api.put<ApiResponse<void>>('/users/password', data);
    }

    /**
     * Get public profile of another user
     */
    async getUserProfile(userId: string): Promise<User> {
        const response = await api.get<ApiResponse<User>>(`/users/${userId}`);
        return response.data.data;
    }

    /**
     * Upload profile picture
     */
    async uploadProfilePicture(file: File): Promise<User> {
        const formData = new FormData();
        formData.append('file', file);

        const response = await api.post<ApiResponse<User>>(
            '/users/profile-picture',
            formData,
            {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            }
        );
        return response.data.data;
    }

    /**
     * Delete profile picture
     */
    async deleteProfilePicture(): Promise<User> {
        const response = await api.delete<ApiResponse<User>>('/users/profile-picture');
        return response.data.data;
    }
}

export default new UserService();