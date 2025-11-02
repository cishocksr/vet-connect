import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../hooks/use-auth';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card';
import { Badge } from '../components/ui/badge';
import { Button } from '../components/ui/button';
import { ProfilePictureUpload } from '../components/profile/profile-picture-upload';
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from '../components/ui/dialog';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '../components/ui/select';
import userService from '../services/user-service';
import { User, Mail, MapPin, Shield, Edit, Loader2 } from 'lucide-react';
import type { BranchOfService } from '../types';

export default function ProfilePage() {
    const { user, setUser } = useAuth();
    const queryClient = useQueryClient();
    const [isEditOpen, setIsEditOpen] = useState(false);

    // Form state
    const [firstName, setFirstName] = useState(user?.firstName || '');
    const [lastName, setLastName] = useState(user?.lastName || '');
    const [branchOfService, setBranchOfService] = useState<BranchOfService | ''>(
        user?.branchOfService || ''
    );
    const [addressLine1, setAddressLine1] = useState(user?.addressLine1 || '');
    const [addressLine2, setAddressLine2] = useState(user?.addressLine2 || '');
    const [city, setCity] = useState(user?.city || '');
    const [state, setState] = useState(user?.state || '');
    const [zipCode, setZipCode] = useState(user?.zipCode || '');

    // Update profile mutation
    const updateMutation = useMutation({
        mutationFn: (data: Parameters<typeof userService.updateProfile>[0]) => userService.updateProfile(data),
        onSuccess: (updatedUser) => {
            setUser(updatedUser);
            queryClient.invalidateQueries({ queryKey: ['user', 'profile'] });
            setIsEditOpen(false);
        },
    });

    const handleSave = () => {
        updateMutation.mutate({
            firstName,
            lastName,
            branchOfService: branchOfService as BranchOfService,
            addressLine1,
            addressLine2,
            city,
            state,
            zipCode,
        });
    };

    if (!user) return null;

    return (
        <div className="min-h-screen bg-gray-50 py-8">
            <div className="container mx-auto px-4 max-w-2xl">
                <div className="flex justify-between items-center mb-8">
                    <h1 className="text-3xl font-bold">Profile</h1>

                    {/* Edit Profile Button */}
                    <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
                        <DialogTrigger asChild>
                            <Button className="bg-military-navy hover:bg-military-navy/90 text-white">
                                <Edit className="h-4 w-4 mr-2" />
                                Edit Profile
                            </Button>
                        </DialogTrigger>
                        <DialogContent className="max-w-md max-h-[90vh] overflow-y-auto">
                            <DialogHeader>
                                <DialogTitle>Edit Profile</DialogTitle>
                                <DialogDescription>
                                    Update your personal information
                                </DialogDescription>
                            </DialogHeader>

                            <div className="space-y-4 py-4">
                                {/* First Name */}
                                <div className="space-y-2">
                                    <Label htmlFor="firstName">First Name</Label>
                                    <Input
                                        id="firstName"
                                        value={firstName}
                                        onChange={(e) => setFirstName(e.target.value)}
                                    />
                                </div>

                                {/* Last Name */}
                                <div className="space-y-2">
                                    <Label htmlFor="lastName">Last Name</Label>
                                    <Input
                                        id="lastName"
                                        value={lastName}
                                        onChange={(e) => setLastName(e.target.value)}
                                    />
                                </div>

                                {/* Branch of Service */}
                                <div className="space-y-2">
                                    <Label htmlFor="branch">Branch of Service</Label>
                                    <Select
                                        value={branchOfService}
                                        onValueChange={(value) =>
                                            setBranchOfService(value as BranchOfService)
                                        }
                                    >
                                        <SelectTrigger>
                                            <SelectValue placeholder="Select branch" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="ARMY">Army</SelectItem>
                                            <SelectItem value="NAVY">Navy</SelectItem>
                                            <SelectItem value="AIR_FORCE">Air Force</SelectItem>
                                            <SelectItem value="MARINES">Marines</SelectItem>
                                            <SelectItem value="COAST_GUARD">Coast Guard</SelectItem>
                                            <SelectItem value="SPACE_FORCE">Space Force</SelectItem>
                                        </SelectContent>
                                    </Select>
                                </div>

                                {/* Address Line 1 */}
                                <div className="space-y-2">
                                    <Label htmlFor="addressLine1">Address Line 1</Label>
                                    <Input
                                        id="addressLine1"
                                        value={addressLine1}
                                        onChange={(e) => setAddressLine1(e.target.value)}
                                        placeholder="123 Main St"
                                    />
                                </div>

                                {/* Address Line 2 */}
                                <div className="space-y-2">
                                    <Label htmlFor="addressLine2">Address Line 2 (Optional)</Label>
                                    <Input
                                        id="addressLine2"
                                        value={addressLine2}
                                        onChange={(e) => setAddressLine2(e.target.value)}
                                        placeholder="Apt 4B"
                                    />
                                </div>

                                {/* City */}
                                <div className="space-y-2">
                                    <Label htmlFor="city">City</Label>
                                    <Input
                                        id="city"
                                        value={city}
                                        onChange={(e) => setCity(e.target.value)}
                                    />
                                </div>

                                {/* State */}
                                <div className="space-y-2">
                                    <Label htmlFor="state">State</Label>
                                    <Input
                                        id="state"
                                        value={state}
                                        onChange={(e) => setState(e.target.value.toUpperCase())}
                                        maxLength={2}
                                        placeholder="VA"
                                    />
                                </div>

                                {/* Zip Code */}
                                <div className="space-y-2">
                                    <Label htmlFor="zipCode">Zip Code</Label>
                                    <Input
                                        id="zipCode"
                                        value={zipCode}
                                        onChange={(e) => setZipCode(e.target.value)}
                                        placeholder="20147"
                                    />
                                </div>

                                {/* Save Button */}
                                <Button
                                    onClick={handleSave}
                                    className="w-full bg-military-navy hover:bg-military-navy/90 text-white"
                                    disabled={updateMutation.isPending}
                                >
                                    {updateMutation.isPending ? (
                                        <>
                                            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                            Saving...
                                        </>
                                    ) : (
                                        'Save Changes'
                                    )}
                                </Button>
                            </div>
                        </DialogContent>
                    </Dialog>
                </div>

                {/* Profile Picture Upload Component */}
                <div className="mb-8">
                    <ProfilePictureUpload user={user} />
                </div>

                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <User className="h-5 w-5" />
                            Personal Information
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div>
                            <label className="text-sm text-gray-600">Full Name</label>
                            <p className="font-medium">{user.fullName}</p>
                        </div>

                        <div>
                            <label className="text-sm text-gray-600 flex items-center gap-2">
                                <Mail className="h-4 w-4" />
                                Email
                            </label>
                            <p className="font-medium">{user.email}</p>
                        </div>

                        <div>
                            <label className="text-sm text-gray-600 flex items-center gap-2">
                                <Shield className="h-4 w-4" />
                                Branch of Service
                            </label>
                            <Badge className="mt-1 bg-military-green">
                                {user.branchDisplayName}
                            </Badge>
                        </div>

                        {(user.city || user.state) && (
                            <div>
                                <label className="text-sm text-gray-600 flex items-center gap-2">
                                    <MapPin className="h-4 w-4" />
                                    Location
                                </label>
                                <p className="font-medium">
                                    {user.city && user.state
                                        ? `${user.city}, ${user.state} ${user.zipCode || ''}`
                                        : 'Not set'}
                                </p>
                            </div>
                        )}
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}