import { describe, it, expect, beforeEach, vi } from 'vitest';
import resourceService from './resource-service';
import api from './api';
import type { Resource, ResourceSummary, ResourceCategory, PageResponse, ResourceSearchParams } from '../types';

// Mock the api module
vi.mock('./api');

describe('ResourceService', () => {
    const mockCategory: ResourceCategory = {
        id: 1,
        name: 'Housing',
        description: 'Housing resources for veterans',
        iconName: 'home',
    };

    const mockResourceSummary: ResourceSummary = {
        id: 'res-123',
        categoryId: 1,
        categoryName: 'Housing',
        name: 'VA Housing Assistance',
        shortDescription: 'Comprehensive housing assistance for veterans',
        city: 'Washington',
        state: 'DC',
        isNational: true,
    };

    const mockResource: Resource = {
        id: 'res-123',
        category: mockCategory,
        name: 'VA Housing Assistance',
        description: 'Comprehensive housing assistance for veterans',
        websiteUrl: 'https://va.gov/housing',
        phoneNumber: '1-800-827-1000',
        email: 'housing@va.gov',
        addressLine1: '810 Vermont Ave NW',
        city: 'Washington',
        state: 'DC',
        zipCode: '20420',
        isNational: true,
        eligibilityCriteria: 'All veterans',
        createdAt: '2024-01-01T00:00:00Z',
    };

    const mockPageResponse: PageResponse<ResourceSummary> = {
        content: [mockResourceSummary],
        pageNumber: 0,
        pageSize: 10,
        totalElements: 1,
        totalPages: 1,
        isFirst: true,
        isLast: true,
        hasNext: false,
        hasPrevious: false,
    };

    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe('getAllResources', () => {
        it('should fetch resources with default pagination', async () => {
            vi.mocked(api.get).mockResolvedValue({
                data: {
                    success: true,
                    message: 'Resources retrieved successfully',
                    data: mockPageResponse,
                },
            });

            const result = await resourceService.getAllResources();

            expect(api.get).toHaveBeenCalledWith('/resources', {
                params: {
                    page: 0,
                    size: 20,
                },
            });
            expect(result.content).toHaveLength(1);
            expect(result.content[0]).toEqual(mockResourceSummary);
        });

        it('should fetch resources with custom pagination', async () => {
            vi.mocked(api.get).mockResolvedValue({
                data: {
                    success: true,
                    message: 'Resources retrieved successfully',
                    data: mockPageResponse,
                },
            });

            await resourceService.getAllResources(2, 50);

            expect(api.get).toHaveBeenCalledWith('/resources', {
                params: {
                    page: 2,
                    size: 50,
                },
            });
        });

        it('should handle empty results', async () => {
            const emptyResponse: PageResponse<ResourceSummary> = {
                ...mockPageResponse,
                content: [],
                totalElements: 0,
            };

            vi.mocked(api.get).mockResolvedValue({
                data: {
                    success: true,
                    message: 'No resources found',
                    data: emptyResponse,
                },
            });

            const result = await resourceService.getAllResources();

            expect(result.content).toHaveLength(0);
            expect(result.totalElements).toBe(0);
        });
    });

    describe('getResourceById', () => {
        it('should fetch a single resources by id', async () => {
            vi.mocked(api.get).mockResolvedValue({
                data: {
                    success: true,
                    message: 'Resource retrieved successfully',
                    data: mockResource,
                },
            });

            const result = await resourceService.getResourceById('res-123');

            expect(api.get).toHaveBeenCalledWith('/resources/res-123');
            expect(result).toEqual(mockResource);
            expect(result.id).toBe('res-123');
        });

        it('should throw error when resources not found', async () => {
            const error = new Error('Resource not found');
            vi.mocked(api.get).mockRejectedValue(error);

            await expect(resourceService.getResourceById('invalid-id')).rejects.toThrow(
                'Resource not found'
            );
        });
    });

    describe('searchResources', () => {
        it('should search resources with keyword only', async () => {
            const searchParams: ResourceSearchParams = {
                keyword: 'housing',
            };

            vi.mocked(api.get).mockResolvedValue({
                data: {
                    success: true,
                    message: 'Search completed successfully',
                    data: mockPageResponse,
                },
            });

            const result = await resourceService.searchResources(searchParams);

            expect(api.get).toHaveBeenCalledWith('/resources/search', {
                params: {
                    keyword: 'housing',
                },
            });
            expect(result.content[0].name).toContain('Housing');
        });

        it('should search resources with multiple filters', async () => {
            const searchParams: ResourceSearchParams = {
                keyword: 'mental health',
                categoryId: 4,
                state: 'VA',
                page: 1,
                size: 20,
            };

            vi.mocked(api.get).mockResolvedValue({
                data: {
                    success: true,
                    message: 'Search completed successfully',
                    data: mockPageResponse,
                },
            });

            await resourceService.searchResources(searchParams);

            expect(api.get).toHaveBeenCalledWith('/resources/search', {
                params: {
                    keyword: 'mental health',
                    categoryId: 4,
                    state: 'VA',
                    page: 1,
                    size: 20,
                },
            });
        });

        it('should filter by category only', async () => {
            const searchParams: ResourceSearchParams = {
                categoryId: 1,
            };

            vi.mocked(api.get).mockResolvedValue({
                data: {
                    success: true,
                    message: 'Search completed successfully',
                    data: mockPageResponse,
                },
            });

            const result = await resourceService.searchResources(searchParams);

            expect(api.get).toHaveBeenCalledWith('/resources/search', {
                params: {
                    categoryId: 1,
                },
            });
            expect(result.content[0].categoryId).toBe(1);
        });

        it('should filter by state only', async () => {
            const searchParams: ResourceSearchParams = {
                state: 'CA',
            };

            vi.mocked(api.get).mockResolvedValue({
                data: {
                    success: true,
                    message: 'Search completed successfully',
                    data: mockPageResponse,
                },
            });

            await resourceService.searchResources(searchParams);

            expect(api.get).toHaveBeenCalledWith('/resources/search', {
                params: {
                    state: 'CA',
                },
            });
        });

        it('should handle search with no results', async () => {
            const searchParams: ResourceSearchParams = {
                keyword: 'nonexistent',
            };

            const emptyResponse: PageResponse<ResourceSummary> = {
                ...mockPageResponse,
                content: [],
                totalElements: 0,
            };

            vi.mocked(api.get).mockResolvedValue({
                data: {
                    success: true,
                    message: 'No results found',
                    data: emptyResponse,
                },
            });

            const result = await resourceService.searchResources(searchParams);

            expect(result.content).toHaveLength(0);
        });
    });

    describe('getResourcesByCategory', () => {
        it('should fetch resources by category id', async () => {
            vi.mocked(api.get).mockResolvedValue({
                data: {
                    success: true,
                    message: 'Resources retrieved successfully',
                    data: mockPageResponse,
                },
            });

            const result = await resourceService.getResourcesByCategory(1);

            expect(api.get).toHaveBeenCalledWith('/resources/category/1', {
                params: { page: 0, size: 20 },
            });
            expect(result.content[0].categoryId).toBe(1);
        });

        it('should fetch resources by category with custom pagination', async () => {
            vi.mocked(api.get).mockResolvedValue({
                data: {
                    success: true,
                    message: 'Resources retrieved successfully',
                    data: mockPageResponse,
                },
            });

            await resourceService.getResourcesByCategory(2, 1, 10);

            expect(api.get).toHaveBeenCalledWith('/resources/category/2', {
                params: { page: 1, size: 10 },
            });
        });
    });

    describe('getResourcesByState', () => {
        it('should fetch resources by state', async () => {
            vi.mocked(api.get).mockResolvedValue({
                data: {
                    success: true,
                    message: 'Resources retrieved successfully',
                    data: mockPageResponse,
                },
            });

            const result = await resourceService.getResourcesByState('VA');

            expect(api.get).toHaveBeenCalledWith('/resources/state/VA', {
                params: { page: 0, size: 20 },
            });
            expect(result.content[0].state).toBe('DC'); // Mock data has DC
        });
    });

    describe('getNationalResources', () => {
        it('should fetch all national resources', async () => {
            vi.mocked(api.get).mockResolvedValue({
                data: {
                    success: true,
                    message: 'National resources retrieved',
                    data: [mockResource],
                },
            });

            const result = await resourceService.getNationalResources();

            expect(api.get).toHaveBeenCalledWith('/resources/national');
            expect(result).toHaveLength(1);
            expect(result[0].isNational).toBe(true);
        });
    });

    describe('getAllCategories', () => {
        it('should fetch all resources categories', async () => {
            vi.mocked(api.get).mockResolvedValue({
                data: {
                    success: true,
                    message: 'Categories retrieved successfully',
                    data: [mockCategory],
                },
            });

            const result = await resourceService.getAllCategories();

            expect(api.get).toHaveBeenCalledWith('/categories');
            expect(result).toHaveLength(1);
            expect(result[0].name).toBe('Housing');
        });
    });

    describe('getCategoriesWithCounts', () => {
        it('should fetch categories with resources counts', async () => {
            const categoryWithCount = {
                ...mockCategory,
                resourceCount: 42,
            };

            vi.mocked(api.get).mockResolvedValue({
                data: {
                    success: true,
                    message: 'Categories with counts retrieved',
                    data: [categoryWithCount],
                },
            });

            const result = await resourceService.getCategoriesWithCounts();

            expect(api.get).toHaveBeenCalledWith('/categories/with-counts');
            expect(result[0].resourceCount).toBe(42);
        });
    });
});