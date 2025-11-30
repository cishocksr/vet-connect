import { describe, it, expect, beforeEach, vi } from 'vitest';
import adminService from './admin-service';
import api from './api';

// Mock the api module
vi.mock('./api');

describe('Admin Service', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe('getAllUsers', () => {
        it('should fetch all users with default pagination', async () => {
            const mockResponse = {
                data: {
                    data: {
                        content: [
                            { id: '1', email: 'user1@test.com', role: 'USER' },
                            { id: '2', email: 'user2@test.com', role: 'USER' }
                        ],
                        totalElements: 2,
                        totalPages: 1,
                        currentPage: 0
                    }
                }
            };

            vi.mocked(api.get).mockResolvedValue(mockResponse);

            const result = await adminService.getAllUsers();

            expect(api.get).toHaveBeenCalledWith('/admin/users', {
                params: { page: 0, size: 20, sortBy: 'createdAt', sortDir: 'desc' }
            });
            expect(result.content).toHaveLength(2);
        });

        it('should fetch users with custom pagination', async () => {
            const mockResponse = {
                data: {
                    data: {
                        content: [],
                        totalElements: 0,
                        totalPages: 0,
                        currentPage: 1
                    }
                }
            };

            vi.mocked(api.get).mockResolvedValue(mockResponse);

            await adminService.getAllUsers(1, 10, 'email', 'asc');

            expect(api.get).toHaveBeenCalledWith('/admin/users', {
                params: { page: 1, size: 10, sortBy: 'email', sortDir: 'asc' }
            });
        });
    });

    describe('searchUsers', () => {
        it('should search users by query', async () => {
            const mockResponse = {
                data: {
                    data: {
                        content: [{ id: '1', email: 'john@test.com' }],
                        totalElements: 1
                    }
                }
            };

            vi.mocked(api.get).mockResolvedValue(mockResponse);

            const result = await adminService.searchUsers('john');

            expect(api.get).toHaveBeenCalledWith('/admin/users/search', {
                params: { q: 'john', page: 0, size: 20 }
            });
            expect(result.content).toHaveLength(1);
        });
    });

    describe('getUsersByRole', () => {
        it('should filter users by role', async () => {
            const mockResponse = {
                data: {
                    data: {
                        content: [{ id: '1', role: 'ADMIN' }],
                        totalElements: 1
                    }
                }
            };

            vi.mocked(api.get).mockResolvedValue(mockResponse);

            await adminService.getUsersByRole('ADMIN');

            expect(api.get).toHaveBeenCalledWith('/admin/users/filter/role', {
                params: { role: 'ADMIN', page: 0, size: 20 }
            });
        });
    });

    describe('getUsersByStatus', () => {
        it('should filter users by active status', async () => {
            const mockResponse = {
                data: {
                    data: {
                        content: [{ id: '1', active: true }],
                        totalElements: 1
                    }
                }
            };

            vi.mocked(api.get).mockResolvedValue(mockResponse);

            await adminService.getUsersByStatus(true);

            expect(api.get).toHaveBeenCalledWith('/admin/users/filter/status', {
                params: { active: true, page: 0, size: 20 }
            });
        });
    });

    describe('getHomelessUsers', () => {
        it('should fetch homeless users', async () => {
            const mockResponse = {
                data: {
                    data: {
                        content: [{ id: '1', isHomeless: true }],
                        totalElements: 1
                    }
                }
            };

            vi.mocked(api.get).mockResolvedValue(mockResponse);

            await adminService.getHomelessUsers();

            expect(api.get).toHaveBeenCalledWith('/admin/users/filter/homeless', {
                params: { page: 0, size: 20 }
            });
        });
    });

    describe('getUserDetails', () => {
        it('should fetch user details by ID', async () => {
            const mockUser = { id: 'user-123', email: 'test@test.com', role: 'USER' };
            const mockResponse = {
                data: {
                    data: mockUser
                }
            };

            vi.mocked(api.get).mockResolvedValue(mockResponse);

            const result = await adminService.getUserDetails('user-123');

            expect(api.get).toHaveBeenCalledWith('/admin/users/user-123');
            expect(result).toEqual(mockUser);
        });
    });

    describe('updateUserRole', () => {
        it('should update user role', async () => {
            const mockUser = { id: 'user-123', role: 'ADMIN' };
            const mockResponse = {
                data: {
                    data: mockUser
                }
            };

            vi.mocked(api.put).mockResolvedValue(mockResponse);

            const result = await adminService.updateUserRole('user-123', { role: 'ADMIN' });

            expect(api.put).toHaveBeenCalledWith('/admin/users/user-123/role', { role: 'ADMIN' });
            expect(result.role).toBe('ADMIN');
        });
    });

    describe('suspendUser', () => {
        it('should suspend a user with reason', async () => {
            const mockUser = { id: 'user-123', suspended: true };
            const mockResponse = {
                data: {
                    data: mockUser
                }
            };

            vi.mocked(api.post).mockResolvedValue(mockResponse);

            await adminService.suspendUser('user-123', { reason: 'Policy violation' });

            expect(api.post).toHaveBeenCalledWith('/admin/users/user-123/suspend', {
                reason: 'Policy violation'
            });
        });
    });

    describe('activateUser', () => {
        it('should activate a suspended user', async () => {
            const mockUser = { id: 'user-123', active: true };
            const mockResponse = {
                data: {
                    data: mockUser
                }
            };

            vi.mocked(api.post).mockResolvedValue(mockResponse);

            await adminService.activateUser('user-123');

            expect(api.post).toHaveBeenCalledWith('/admin/users/user-123/activate');
        });
    });

    describe('deleteUser', () => {
        it('should delete a user', async () => {
            vi.mocked(api.delete).mockResolvedValue({});

            await adminService.deleteUser('user-123');

            expect(api.delete).toHaveBeenCalledWith('/admin/users/user-123');
        });
    });

    describe('getSystemStats', () => {
        it('should fetch system statistics', async () => {
            const mockStats = {
                totalUsers: 100,
                activeUsers: 85,
                totalResources: 50
            };
            const mockResponse = {
                data: {
                    data: mockStats
                }
            };

            vi.mocked(api.get).mockResolvedValue(mockResponse);

            const result = await adminService.getSystemStats();

            expect(api.get).toHaveBeenCalledWith('/admin/stats');
            expect(result).toEqual(mockStats);
        });
    });
});