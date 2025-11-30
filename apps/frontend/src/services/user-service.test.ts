import { describe, it, expect, beforeEach, vi } from 'vitest';
import userService from './user-service';
import api from './api';

// Mock the api module
vi.mock('./api');

describe('User Service', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    const mockUser = {
        id: 'user-123',
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
        fullName: 'John Doe',
        branchOfService: 'ARMY',
        city: 'Detroit',
        state: 'MI',
        zipCode: '48201'
    };

    describe('getCurrentProfile', () => {
        it('should fetch current user profile', async () => {
            const mockResponse = {
                data: {
                    data: mockUser
                }
            };

            vi.mocked(api.get).mockResolvedValue(mockResponse);

            const result = await userService.getCurrentProfile();

            expect(api.get).toHaveBeenCalledWith('/users/profile');
            expect(result).toEqual(mockUser);
        });
    });

    describe('updateProfile', () => {
        it('should update user profile', async () => {
            const updateData = {
                firstName: 'Jane',
                lastName: 'Smith'
            };
            const updatedUser = { ...mockUser, ...updateData, fullName: 'Jane Smith' };
            const mockResponse = {
                data: {
                    data: updatedUser
                }
            };

            vi.mocked(api.put).mockResolvedValue(mockResponse);

            const result = await userService.updateProfile(updateData);

            expect(api.put).toHaveBeenCalledWith('/users/profile', updateData);
            expect(result.firstName).toBe('Jane');
            expect(result.lastName).toBe('Smith');
        });

        it('should handle partial profile updates', async () => {
            const updateData = { firstName: 'Jane' };
            const mockResponse = {
                data: {
                    data: { ...mockUser, firstName: 'Jane' }
                }
            };

            vi.mocked(api.put).mockResolvedValue(mockResponse);

            await userService.updateProfile(updateData);

            expect(api.put).toHaveBeenCalledWith('/users/profile', updateData);
        });
    });

    describe('updateAddress', () => {
        it('should update user address', async () => {
            const addressData = {
                addressLine1: '456 Oak St',
                city: 'Ann Arbor',
                state: 'MI',
                zipCode: '48104'
            };
            const mockResponse = {
                data: {
                    data: { ...mockUser, ...addressData }
                }
            };

            vi.mocked(api.patch).mockResolvedValue(mockResponse);

            const result = await userService.updateAddress(addressData);

            expect(api.patch).toHaveBeenCalledWith('/users/address', addressData);
            expect(result.city).toBe('Ann Arbor');
        });
    });

    describe('changePassword', () => {
        it('should change user password', async () => {
            const passwordData = {
                currentPassword: 'oldPass123!',
                newPassword: 'newPass456!',
                confirmNewPassword: 'newPass456!'
            };

            vi.mocked(api.put).mockResolvedValue({ data: { data: undefined } });

            await userService.changePassword(passwordData);

            expect(api.put).toHaveBeenCalledWith('/users/password', passwordData);
        });
    });

    describe('getUserProfile', () => {
        it('should fetch public profile of another user', async () => {
            const otherUser = { ...mockUser, id: 'other-user-456' };
            const mockResponse = {
                data: {
                    data: otherUser
                }
            };

            vi.mocked(api.get).mockResolvedValue(mockResponse);

            const result = await userService.getUserProfile('other-user-456');

            expect(api.get).toHaveBeenCalledWith('/users/other-user-456');
            expect(result.id).toBe('other-user-456');
        });
    });

    describe('uploadProfilePicture', () => {
        it('should upload profile picture', async () => {
            const mockFile = new File(['image'], 'profile.jpg', { type: 'image/jpeg' });
            const updatedUser = { ...mockUser, profilePictureUrl: 'https://example.com/pic.jpg' };
            const mockResponse = {
                data: {
                    data: updatedUser
                }
            };

            vi.mocked(api.post).mockResolvedValue(mockResponse);

            const result = await userService.uploadProfilePicture(mockFile);

            expect(api.post).toHaveBeenCalledWith(
                '/users/profile-picture',
                expect.any(FormData),
                {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    }
                }
            );
            expect(result).toEqual(updatedUser);
        });

        it('should handle FormData correctly', async () => {
            const mockFile = new File(['image'], 'test.png', { type: 'image/png' });
            const mockResponse = {
                data: {
                    data: mockUser
                }
            };

            vi.mocked(api.post).mockResolvedValue(mockResponse);

            await userService.uploadProfilePicture(mockFile);

            const callArgs = vi.mocked(api.post).mock.calls[0];
            expect(callArgs[0]).toBe('/users/profile-picture');
            expect(callArgs[1]).toBeInstanceOf(FormData);
        });
    });

    describe('deleteProfilePicture', () => {
        it('should delete profile picture', async () => {
            const updatedUser = { ...mockUser, profilePictureUrl: null };
            const mockResponse = {
                data: {
                    data: updatedUser
                }
            };

            vi.mocked(api.delete).mockResolvedValue(mockResponse);

            const result = await userService.deleteProfilePicture();

            expect(api.delete).toHaveBeenCalledWith('/users/profile-picture');
            expect(result).toEqual(updatedUser);
        });
    });
});