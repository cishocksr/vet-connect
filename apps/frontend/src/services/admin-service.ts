import api from './api';
import type { ApiResponse, PageResponse } from '@/types';
import type {
    AdminUserListDTO,
    AdminUserDetailDTO,
    UpdateUserRoleRequest,
    SuspendUserRequest,
    AdminStatsDTO,
    UserRole
} from '@/types/admin';

/**
 * Admin Service - API calls for admin operations
 */
const adminService = {
    /**
     * Get all users with pagination
     */
    getAllUsers: async (
        page = 0,
        size = 20,
        sortBy = 'createdAt',
        sortDir = 'desc'
    ): Promise<PageResponse<AdminUserListDTO>> => {
        const response = await api.get<ApiResponse<PageResponse<AdminUserListDTO>>>(
            '/admin/users',
            {
                params: { page, size, sortBy, sortDir }
            }
        );
        return response.data.data;
    },

    /**
     * Search users
     */
    searchUsers: async (
        query: string,
        page = 0,
        size = 20
    ): Promise<PageResponse<AdminUserListDTO>> => {
        const response = await api.get<ApiResponse<PageResponse<AdminUserListDTO>>>(
            '/admin/users/search',
            {
                params: { q: query, page, size }
            }
        );
        return response.data.data;
    },

    /**
     * Filter users by role
     */
    getUsersByRole: async (
        role: UserRole,
        page = 0,
        size = 20
    ): Promise<PageResponse<AdminUserListDTO>> => {
        const response = await api.get<ApiResponse<PageResponse<AdminUserListDTO>>>(
            '/admin/users/filter/role',
            {
                params: { role, page, size }
            }
        );
        return response.data.data;
    },

    /**
     * Filter users by active status
     */
    getUsersByStatus: async (
        active: boolean,
        page = 0,
        size = 20
    ): Promise<PageResponse<AdminUserListDTO>> => {
        const response = await api.get<ApiResponse<PageResponse<AdminUserListDTO>>>(
            '/admin/users/filter/status',
            {
                params: { active, page, size }
            }
        );
        return response.data.data;
    },

    /**
     * Get homeless users
     */
    getHomelessUsers: async (
        page = 0,
        size = 20
    ): Promise<PageResponse<AdminUserListDTO>> => {
        const response = await api.get<ApiResponse<PageResponse<AdminUserListDTO>>>(
            '/admin/users/filter/homeless',
            {
                params: { page, size }
            }
        );
        return response.data.data;
    },

    /**
     * Get user details
     */
    getUserDetails: async (userId: string): Promise<AdminUserDetailDTO> => {
        const response = await api.get<ApiResponse<AdminUserDetailDTO>>(
            `/admin/users/${userId}`
        );
        return response.data.data;
    },

    /**
     * Update user role
     */
    updateUserRole: async (
        userId: string,
        request: UpdateUserRoleRequest
    ): Promise<AdminUserDetailDTO> => {
        const response = await api.put<ApiResponse<AdminUserDetailDTO>>(
            `/admin/users/${userId}/role`,
            request
        );
        return response.data.data;
    },

    /**
     * Suspend user
     */
    suspendUser: async (
        userId: string,
        request: SuspendUserRequest
    ): Promise<AdminUserDetailDTO> => {
        const response = await api.post<ApiResponse<AdminUserDetailDTO>>(
            `/admin/users/${userId}/suspend`,
            request
        );
        return response.data.data;
    },

    /**
     * Activate user
     */
    activateUser: async (userId: string): Promise<AdminUserDetailDTO> => {
        const response = await api.post<ApiResponse<AdminUserDetailDTO>>(
            `/admin/users/${userId}/activate`
        );
        return response.data.data;
    },

    /**
     * Delete user
     */
    deleteUser: async (userId: string): Promise<void> => {
        await api.delete(`/admin/users/${userId}`);
    },

    /**
     * Get system statistics
     */
    getSystemStats: async (): Promise<AdminStatsDTO> => {
        const response = await api.get<ApiResponse<AdminStatsDTO>>('/admin/stats');
        return response.data.data;
    }
};

export default adminService;