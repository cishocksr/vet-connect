import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import { useAuth } from '../hooks/use-auth';
import userService from '../services/user-service';
import { toast } from 'sonner';
import { ArrowLeft, Save, Loader2 } from 'lucide-react';
import type { UpdateProfileRequest, BranchOfService } from '../types';
import {ProfilePictureUpload} from "@/components/profile/profile-picture-upload.tsx";

// Validation schema
const profileSchema = z.object({
    firstName: z.string().min(1, 'First name is required').max(100),
    lastName: z.string().min(1, 'Last name is required').max(100),
    branchOfService: z.enum(['ARMY', 'NAVY', 'AIR_FORCE', 'MARINES', 'COAST_GUARD', 'SPACE_FORCE']),
    addressLine1: z.string().max(255).optional().or(z.literal('')),
    addressLine2: z.string().max(255).optional().or(z.literal('')),
    city: z.string().max(100).optional().or(z.literal('')),
    state: z.string().length(2).optional().or(z.literal('')),
    zipCode: z.string().max(10).optional().or(z.literal('')),
    isHomeless: z.boolean(),
});

type ProfileFormData = z.infer<typeof profileSchema>;

const BRANCHES = [
    { value: 'ARMY', label: 'Army' },
    { value: 'NAVY', label: 'Navy' },
    { value: 'AIR_FORCE', label: 'Air Force' },
    { value: 'MARINES', label: 'Marines' },
    { value: 'COAST_GUARD', label: 'Coast Guard' },
    { value: 'SPACE_FORCE', label: 'Space Force' },
];

const US_STATES = [
    'AL', 'AK', 'AZ', 'AR', 'CA', 'CO', 'CT', 'DE', 'FL', 'GA',
    'HI', 'ID', 'IL', 'IN', 'IA', 'KS', 'KY', 'LA', 'ME', 'MD',
    'MA', 'MI', 'MN', 'MS', 'MO', 'MT', 'NE', 'NV', 'NH', 'NJ',
    'NM', 'NY', 'NC', 'ND', 'OH', 'OK', 'OR', 'PA', 'RI', 'SC',
    'SD', 'TN', 'TX', 'UT', 'VT', 'VA', 'WA', 'WV', 'WI', 'WY',
];

export default function ProfileEditPage() {
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const { user } = useAuth();
    const [isHomeless, setIsHomeless] = useState(user?.isHomeless || false);

    const {
        register,
        handleSubmit,
        setValue,
        formState: { errors, isDirty },
    } = useForm<ProfileFormData>({
        resolver: zodResolver(profileSchema),
        defaultValues: {
            firstName: user?.firstName || '',
            lastName: user?.lastName || '',
            branchOfService: user?.branchOfService || 'ARMY',
            addressLine1: user?.addressLine1 || '',
            addressLine2: user?.addressLine2 || '',
            city: user?.city || '',
            state: user?.state || '',
            zipCode: user?.zipCode || '',
            isHomeless: user?.isHomeless || false,
        },
    });

    // Update profile mutation
    const updateMutation = useMutation({
        mutationFn: (data: UpdateProfileRequest) => userService.updateProfile(data),
        onSuccess: () => {
            toast.success('Profile updated successfully!');
            queryClient.invalidateQueries({ queryKey: ['user'] });
            navigate('/profile');
        },
        onError: (error: any) => {
            toast.error(error.response?.data?.message || 'Failed to update profile');
        },
    });

    const onSubmit = (data: ProfileFormData) => {
        updateMutation.mutate(data);
    };

    if (!user) return null;

    return (
        <div className="min-h-screen bg-gray-50 py-8">
            <div className="container mx-auto px-4 max-w-2xl">
                {/* Header */}
                <div className="flex items-center gap-4 mb-6">
                    <Button
                        variant="ghost"
                        onClick={() => navigate('/profile')}
                        className="gap-2"
                    >
                        <ArrowLeft className="h-4 w-4" />
                        Back to Profile
                    </Button>
                </div>

                <h1 className="text-3xl font-bold mb-8">Edit Profile</h1>

                <form onSubmit={handleSubmit(onSubmit)}>
                    <Card className="mb-6">
                        <CardHeader>
                            <CardTitle>Profile Picture</CardTitle>
                            <CardDescription>Upload a photo to personalize your profile</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <ProfilePictureUpload user={user} />
                        </CardContent>
                    </Card>
                    {/* Personal Information */}
                    <Card className="mb-6">
                        <CardHeader>
                            <CardTitle>Personal Information</CardTitle>
                            <CardDescription>Update your personal details</CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            {/* Name Fields */}
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label htmlFor="firstName">First Name *</Label>
                                    <Input
                                        id="firstName"
                                        {...register('firstName')}
                                        className={errors.firstName ? 'border-red-500' : ''}
                                    />
                                    {errors.firstName && (
                                        <p className="text-sm text-red-600">{errors.firstName.message}</p>
                                    )}
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="lastName">Last Name *</Label>
                                    <Input
                                        id="lastName"
                                        {...register('lastName')}
                                        className={errors.lastName ? 'border-red-500' : ''}
                                    />
                                    {errors.lastName && (
                                        <p className="text-sm text-red-600">{errors.lastName.message}</p>
                                    )}
                                </div>
                            </div>

                            {/* Branch of Service */}
                            <div className="space-y-2">
                                <Label htmlFor="branchOfService">Branch of Service *</Label>
                                <Select
                                    defaultValue={user.branchOfService}
                                    onValueChange={(value) => setValue('branchOfService', value as BranchOfService)}
                                >
                                    <SelectTrigger>
                                        <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                        {BRANCHES.map((branch) => (
                                            <SelectItem key={branch.value} value={branch.value}>
                                                {branch.label}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>
                        </CardContent>
                    </Card>

                    {/* Address Information */}
                    <Card className="mb-6">
                        <CardHeader>
                            <CardTitle>Address Information</CardTitle>
                            <CardDescription>Update your location details (optional)</CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            {/* Homeless Toggle */}
                            <div className="flex items-center space-x-2">
                                <input
                                    type="checkbox"
                                    id="isHomeless"
                                    checked={isHomeless}
                                    onChange={(e) => {
                                        setIsHomeless(e.target.checked);
                                        setValue('isHomeless', e.target.checked);
                                    }}
                                    className="h-4 w-4 rounded border-gray-300"
                                />
                                <Label htmlFor="isHomeless" className="cursor-pointer">
                                    I am currently experiencing homelessness
                                </Label>
                            </div>

                            {!isHomeless && (
                                <>
                                    <div className="space-y-2">
                                        <Label htmlFor="addressLine1">Address Line 1</Label>
                                        <Input
                                            id="addressLine1"
                                            placeholder="123 Main St"
                                            {...register('addressLine1')}
                                        />
                                    </div>

                                    <div className="space-y-2">
                                        <Label htmlFor="addressLine2">Address Line 2</Label>
                                        <Input
                                            id="addressLine2"
                                            placeholder="Apt 4B"
                                            {...register('addressLine2')}
                                        />
                                    </div>

                                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                        <div className="space-y-2">
                                            <Label htmlFor="city">City</Label>
                                            <Input
                                                id="city"
                                                placeholder="Ashburn"
                                                {...register('city')}
                                            />
                                        </div>

                                        <div className="space-y-2">
                                            <Label htmlFor="state">State</Label>
                                            <Select
                                                defaultValue={user.state || ''}
                                                onValueChange={(value) => setValue('state', value)}
                                            >
                                                <SelectTrigger>
                                                    <SelectValue placeholder="Select state" />
                                                </SelectTrigger>
                                                <SelectContent>
                                                    {US_STATES.map((state) => (
                                                        <SelectItem key={state} value={state}>
                                                            {state}
                                                        </SelectItem>
                                                    ))}
                                                </SelectContent>
                                            </Select>
                                        </div>

                                        <div className="space-y-2">
                                            <Label htmlFor="zipCode">Zip Code</Label>
                                            <Input
                                                id="zipCode"
                                                placeholder="20147"
                                                {...register('zipCode')}
                                            />
                                        </div>
                                    </div>
                                </>
                            )}
                        </CardContent>
                    </Card>

                    {/* Action Buttons */}
                    <div className="flex items-center justify-end gap-4">
                        <Button
                            type="button"
                            variant="outline"
                            onClick={() => navigate('/profile')}
                            disabled={updateMutation.isPending}
                        >
                            Cancel
                        </Button>
                        <Button
                            type="submit"
                            disabled={!isDirty || updateMutation.isPending}
                            className="gap-2"
                        >
                            {updateMutation.isPending ? (
                                <>
                                    <Loader2 className="h-4 w-4 animate-spin" />
                                    Saving...
                                </>
                            ) : (
                                <>
                                    <Save className="h-4 w-4" />
                                    Save Changes
                                </>
                            )}
                        </Button>
                    </div>
                </form>
            </div>
        </div>
    );
}