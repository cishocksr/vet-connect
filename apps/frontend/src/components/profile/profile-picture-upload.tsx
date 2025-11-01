import { useState, useRef } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Button } from '../ui/button';
import { Avatar, AvatarFallback, AvatarImage } from '../ui/avatar';
import { Camera, X, Loader2 } from 'lucide-react';
import userService from '@/services/user-service';
import { toast } from 'sonner';
import { getInitials } from '@/lib/utils';
import type { User } from '@/types';

interface ProfilePictureUploadProps {
    user: User;
}

export function ProfilePictureUpload({ user }: ProfilePictureUploadProps) {
    const queryClient = useQueryClient();
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [preview, setPreview] = useState<string | null>(null);

    // Get profile picture URL
    const profilePictureUrl = user.profilePictureUrl
        ? `http://localhost:8080${user.profilePictureUrl}`
        : null;

    // Upload mutation
    const uploadMutation = useMutation({
        mutationFn: (file: File) => userService.uploadProfilePicture(file),
        onSuccess: () => {
            toast.success('Profile picture updated!');
            queryClient.invalidateQueries({ queryKey: ['user'] });
            setPreview(null);
        },
        onError: (error: any) => {
            toast.error(error.response?.data?.message || 'Failed to upload picture');
        },
    });

    // Delete mutation
    const deleteMutation = useMutation({
        mutationFn: () => userService.deleteProfilePicture(),
        onSuccess: () => {
            toast.success('Profile picture removed!');
            queryClient.invalidateQueries({ queryKey: ['user'] });
        },
        onError: (error: any) => {
            toast.error(error.response?.data?.message || 'Failed to delete picture');
        },
    });

    const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        if (!file) return;

        // Validate file size (5MB)
        if (file.size > 5 * 1024 * 1024) {
            toast.error('File size must be less than 5MB');
            return;
        }

        // Validate file type
        if (!file.type.startsWith('image/')) {
            toast.error('File must be an image');
            return;
        }

        // Show preview
        const reader = new FileReader();
        reader.onloadend = () => {
            setPreview(reader.result as string);
        };
        reader.readAsDataURL(file);

        // Upload file
        uploadMutation.mutate(file);
    };

    const handleDelete = () => {
        if (window.confirm('Are you sure you want to remove your profile picture?')) {
            deleteMutation.mutate();
        }
    };

    const isLoading = uploadMutation.isPending || deleteMutation.isPending;

    return (
        <div className="flex flex-col items-center gap-4">
            <div className="relative">
                <Avatar className="h-32 w-32">
                    <AvatarImage
                        src={preview || profilePictureUrl || undefined}
                        alt={user.fullName}
                    />
                    <AvatarFallback className="bg-gradient-to-br from-military-gold to-military-army-gold text-military-navy text-3xl font-bold">
                        {getInitials(user.firstName, user.lastName)}
                    </AvatarFallback>
                </Avatar>

                {isLoading && (
                    <div className="absolute inset-0 flex items-center justify-center bg-black/50 rounded-full">
                        <Loader2 className="h-8 w-8 text-white animate-spin" />
                    </div>
                )}
            </div>

            <div className="flex items-center gap-2">
                <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={() => fileInputRef.current?.click()}
                    disabled={isLoading}
                    className="gap-2"
                >
                    <Camera className="h-4 w-4" />
                    {profilePictureUrl ? 'Change Photo' : 'Upload Photo'}
                </Button>

                {profilePictureUrl && (
                    <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={handleDelete}
                        disabled={isLoading}
                        className="gap-2 text-red-600 hover:text-red-700"
                    >
                        <X className="h-4 w-4" />
                        Remove
                    </Button>
                )}
            </div>

            <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={handleFileSelect}
                className="hidden"
            />

            <p className="text-xs text-gray-500 text-center">
                JPG, PNG, or GIF. Max 5MB.
            </p>
        </div>
    );
}