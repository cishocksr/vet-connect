import api from './api';
import type {
    Resource,
    ResourceSummary,
    ResourceCategory,
    ResourceCategoryWithCount,
    ApiResponse,
    PageResponse,
    ResourceSearchParams,
} from '../types';

/**
 * Resource Service
 *
 * Handles all resource-related API calls
 */
class ResourceService {
    /**
     * Get all resources (paginated)
     */
    async getAllResources(page = 0, size = 20): Promise<PageResponse<ResourceSummary>> {
        const response = await api.get<ApiResponse<PageResponse<ResourceSummary>>>(
            '/resources',
            { params: { page, size } }
        );
        return response.data.data;
    }

    /**
     * Search resources
     */
    async searchResources(params: ResourceSearchParams): Promise<PageResponse<ResourceSummary>> {
        const response = await api.get<ApiResponse<PageResponse<ResourceSummary>>>(
            '/resources/search',
            { params }
        );
        return response.data.data;
    }

    /**
     * Get single resource by ID
     */
    async getResourceById(id: string): Promise<Resource> {
        const response = await api.get<ApiResponse<Resource>>(`/resources/${id}`);
        return response.data.data;
    }

    /**
     * Get resources by category
     */
    async getResourcesByCategory(
        categoryId: number,
        page = 0,
        size = 20
    ): Promise<PageResponse<ResourceSummary>> {
        const response = await api.get<ApiResponse<PageResponse<ResourceSummary>>>(
            `/resources/category/${categoryId}`,
            { params: { page, size } }
        );
        return response.data.data;
    }

    /**
     * Get resources by state
     */
    async getResourcesByState(
        state: string,
        page = 0,
        size = 20
    ): Promise<PageResponse<ResourceSummary>> {
        const response = await api.get<ApiResponse<PageResponse<ResourceSummary>>>(
            `/resources/state/${state}`,
            { params: { page, size } }
        );
        return response.data.data;
    }

    /**
     * Get national resources
     */
    async getNationalResources(): Promise<Resource[]> {
        const response = await api.get<ApiResponse<Resource[]>>('/resources/national');
        return response.data.data;
    }

    /**
     * Get all categories
     */
    async getAllCategories(): Promise<ResourceCategory[]> {
        const response = await api.get<ApiResponse<ResourceCategory[]>>('/categories');
        return response.data.data;
    }

    /**
     * Get categories with resource counts
     */
    async getCategoriesWithCounts(): Promise<ResourceCategoryWithCount[]> {
        const response = await api.get<ApiResponse<ResourceCategoryWithCount[]>>(
            '/categories/with-counts'
        );
        return response.data.data;
    }
}

export default new ResourceService();