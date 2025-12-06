import api from './api';
import type {
    SavedResource,
    SaveResourceRequest,
    ApiResponse,
} from '../types';

/**
 * Saved Resource Service
 *
 * Handles user's saved resources (dashboard)
 */
class SavedResourceService {
    /**
     * Get all saved resources for current user
     */
    async getSavedResources(): Promise<SavedResource[]> {
        const response = await api.get<ApiResponse<SavedResource[]>>('/saved');
        return response.data.data;
    }

    /**
     * Save a resources
     */
    async saveResource(data: SaveResourceRequest): Promise<SavedResource> {
        const response = await api.post<ApiResponse<SavedResource>>('/saved', data);
        return response.data.data;
    }

    /**
     * Update notes for saved resources
     */
    async updateNotes(savedResourceId: string, notes: string): Promise<SavedResource> {
        const response = await api.patch<ApiResponse<SavedResource>>(
            `/saved/${savedResourceId}/notes`,
            { notes }
        );
        return response.data.data;
    }

    /**
     * Remove saved resources
     */
    async removeSavedResource(savedResourceId: string): Promise<void> {
        await api.delete(`/saved/${savedResourceId}`);
    }

    /**
     * Check if resources is saved
     */
    async isResourceSaved(resourceId: string): Promise<boolean> {
        const response = await api.get<ApiResponse<boolean>>(
            `/saved/check/${resourceId}`
        );
        return response.data.data;
    }

    /**
     * Get count of saved resources
     */
    async getSavedResourceCount(): Promise<number> {
        const response = await api.get<ApiResponse<number>>('/saved/count');
        return response.data.data;
    }
}

export default new SavedResourceService();