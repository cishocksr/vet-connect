import { describe, it, expect, beforeEach, vi } from 'vitest';
import savedResourceService from './saved-resource-service';
import api from './api';

// Mock the api module
vi.mock('./api');

describe('Saved Resource Service', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe('getSavedResources', () => {
        it('should fetch all saved resources', async () => {
            const mockSavedResources = [
                { id: 'saved-1', resourceId: 'res-1', notes: 'Important' },
                { id: 'saved-2', resourceId: 'res-2', notes: 'Check this out' }
            ];
            const mockResponse = {
                data: {
                    data: mockSavedResources
                }
            };

            vi.mocked(api.get).mockResolvedValue(mockResponse);

            const result = await savedResourceService.getSavedResources();

            expect(api.get).toHaveBeenCalledWith('/saved');
            expect(result).toEqual(mockSavedResources);
            expect(result).toHaveLength(2);
        });

        it('should return empty array when no saved resources', async () => {
            const mockResponse = {
                data: {
                    data: []
                }
            };

            vi.mocked(api.get).mockResolvedValue(mockResponse);

            const result = await savedResourceService.getSavedResources();

            expect(result).toEqual([]);
        });
    });

    describe('saveResource', () => {
        it('should save a resource with notes', async () => {
            const saveRequest = {
                resourceId: 'res-123',
                notes: 'This is helpful'
            };
            const mockSavedResource = {
                id: 'saved-1',
                resourceId: 'res-123',
                notes: 'This is helpful',
                createdAt: '2024-01-01T00:00:00Z'
            };
            const mockResponse = {
                data: {
                    data: mockSavedResource
                }
            };

            vi.mocked(api.post).mockResolvedValue(mockResponse);

            const result = await savedResourceService.saveResource(saveRequest);

            expect(api.post).toHaveBeenCalledWith('/saved', saveRequest);
            expect(result).toEqual(mockSavedResource);
        });

        it('should save a resource without notes', async () => {
            const saveRequest = {
                resourceId: 'res-123',
                notes: ''
            };
            const mockResponse = {
                data: {
                    data: { id: 'saved-1', resourceId: 'res-123', notes: '' }
                }
            };

            vi.mocked(api.post).mockResolvedValue(mockResponse);

            await savedResourceService.saveResource(saveRequest);

            expect(api.post).toHaveBeenCalledWith('/saved', saveRequest);
        });
    });

    describe('updateNotes', () => {
        it('should update notes for saved resource', async () => {
            const updatedResource = {
                id: 'saved-1',
                resourceId: 'res-123',
                notes: 'Updated notes'
            };
            const mockResponse = {
                data: {
                    data: updatedResource
                }
            };

            vi.mocked(api.patch).mockResolvedValue(mockResponse);

            const result = await savedResourceService.updateNotes('saved-1', 'Updated notes');

            expect(api.patch).toHaveBeenCalledWith('/saved/saved-1/notes', {
                notes: 'Updated notes'
            });
            expect(result.notes).toBe('Updated notes');
        });

        it('should clear notes when updating to empty string', async () => {
            const mockResponse = {
                data: {
                    data: { id: 'saved-1', notes: '' }
                }
            };

            vi.mocked(api.patch).mockResolvedValue(mockResponse);

            await savedResourceService.updateNotes('saved-1', '');

            expect(api.patch).toHaveBeenCalledWith('/saved/saved-1/notes', { notes: '' });
        });
    });

    describe('removeSavedResource', () => {
        it('should remove a saved resource', async () => {
            vi.mocked(api.delete).mockResolvedValue({});

            await savedResourceService.removeSavedResource('saved-1');

            expect(api.delete).toHaveBeenCalledWith('/saved/saved-1');
        });
    });

    describe('isResourceSaved', () => {
        it('should return true when resource is saved', async () => {
            const mockResponse = {
                data: {
                    data: true
                }
            };

            vi.mocked(api.get).mockResolvedValue(mockResponse);

            const result = await savedResourceService.isResourceSaved('res-123');

            expect(api.get).toHaveBeenCalledWith('/saved/check/res-123');
            expect(result).toBe(true);
        });

        it('should return false when resource is not saved', async () => {
            const mockResponse = {
                data: {
                    data: false
                }
            };

            vi.mocked(api.get).mockResolvedValue(mockResponse);

            const result = await savedResourceService.isResourceSaved('res-456');

            expect(result).toBe(false);
        });
    });

    describe('getSavedResourceCount', () => {
        it('should return count of saved resources', async () => {
            const mockResponse = {
                data: {
                    data: 5
                }
            };

            vi.mocked(api.get).mockResolvedValue(mockResponse);

            const result = await savedResourceService.getSavedResourceCount();

            expect(api.get).toHaveBeenCalledWith('/saved/count');
            expect(result).toBe(5);
        });

        it('should return zero when no saved resources', async () => {
            const mockResponse = {
                data: {
                    data: 0
                }
            };

            vi.mocked(api.get).mockResolvedValue(mockResponse);

            const result = await savedResourceService.getSavedResourceCount();

            expect(result).toBe(0);
        });
    });
});