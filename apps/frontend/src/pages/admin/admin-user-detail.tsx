import { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Label } from '@/components/ui/label';
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog';
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { toast } from 'sonner';  // CHANGED: Import directly from sonner
import adminService from '@/services/admin-service';
import { UserRole } from '@/types/admin';
import {
    ArrowLeft,
    Shield,
    UserCheck,
    UserX,
    Trash2,
    Edit,
    Home,
    MapPin,
    Mail,
    Calendar,
    Clock
} from 'lucide-react';

export default function AdminUserDetail() {
    const { userId } = useParams<{ userId: string }>();
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const [showRoleDialog, setShowRoleDialog] = useState(false);
    const [showSuspendDialog, setShowSuspendDialog] = useState(false);
    const [showDeleteDialog, setShowDeleteDialog] = useState(false);
    const [selectedRole, setSelectedRole] = useState<UserRole>(UserRole.USER);
    const [suspendReason, setSuspendReason] = useState('');

    // Fetch user details
    const { data: user, isLoading } = useQuery({
        queryKey: ['admin-user-detail', userId],
        queryFn: () => adminService.getUserDetails(userId!),
        enabled: !!userId
    });

    // Update role mutation
    const updateRoleMutation = useMutation({
        mutationFn: (role: UserRole) => adminService.updateUserRole(userId!, { role }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-user-detail', userId] });
            queryClient.invalidateQueries({ queryKey: ['admin-users'] });
            toast.success('User role updated successfully');  // CHANGED
            setShowRoleDialog(false);
        },
        onError: (error: any) => {
            toast.error(error.response?.data?.message || 'Failed to update user role');  // CHANGED
        }
    });

    // Suspend user mutation
    const suspendMutation = useMutation({
        mutationFn: (reason: string) => adminService.suspendUser(userId!, { reason }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-user-detail', userId] });
            queryClient.invalidateQueries({ queryKey: ['admin-users'] });
            toast.success('User suspended successfully');  // CHANGED
            setShowSuspendDialog(false);
            setSuspendReason('');
        },
        onError: (error: any) => {
            toast.error(error.response?.data?.message || 'Failed to suspend user');  // CHANGED
        }
    });

    // Activate user mutation
    const activateMutation = useMutation({
        mutationFn: () => adminService.activateUser(userId!),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-user-detail', userId] });
            queryClient.invalidateQueries({ queryKey: ['admin-users'] });
            toast.success('User activated successfully');  // CHANGED
        },
        onError: (error: any) => {
            toast.error(error.response?.data?.message || 'Failed to activate user');  // CHANGED
        }
    });

    // Delete user mutation
    const deleteMutation = useMutation({
        mutationFn: () => adminService.deleteUser(userId!),
        onSuccess: () => {
            toast.success('User deleted successfully');  // CHANGED
            navigate('/admin/users');
        },
        onError: (error: any) => {
            toast.error(error.response?.data?.message || 'Failed to delete user');  // CHANGED
        }
    });

    const handleRoleUpdate = () => {
        updateRoleMutation.mutate(selectedRole);
    };

    const handleSuspend = () => {
        if (!suspendReason.trim()) {
            toast.error('Please provide a reason for suspension');  // CHANGED
            return;
        }
        suspendMutation.mutate(suspendReason);
    };

    const handleActivate = () => {
        activateMutation.mutate();
    };

    const handleDelete = () => {
        deleteMutation.mutate();
    };

    if (isLoading) {
        return (
            <div className="container mx-auto px-4 py-8">
                <Skeleton className="h-10 w-64 mb-8" />
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                    <div className="lg:col-span-2">
                        <Card>
                            <CardHeader>
                                <Skeleton className="h-6 w-48" />
                            </CardHeader>
                            <CardContent className="space-y-4">
                                {[...Array(8)].map((_, i) => (
                                    <Skeleton key={i} className="h-4 w-full" />
                                ))}
                            </CardContent>
                        </Card>
                    </div>
                    <div>
                        <Card>
                            <CardHeader>
                                <Skeleton className="h-6 w-32" />
                            </CardHeader>
                            <CardContent className="space-y-2">
                                {[...Array(4)].map((_, i) => (
                                    <Skeleton key={i} className="h-10 w-full" />
                                ))}
                            </CardContent>
                        </Card>
                    </div>
                </div>
            </div>
        );
    }

    if (!user) {
        return (
            <div className="container mx-auto px-4 py-8">
                <p className="text-center text-muted-foreground">User not found</p>
            </div>
        );
    }

    return (
        <div className="container mx-auto px-4 py-8">
            {/* Header */}
            <div className="flex items-center justify-between mb-8">
                <div className="flex items-center gap-4">
                    <Link to="/admin/users">
                        <Button variant="ghost" size="icon">
                            <ArrowLeft className="h-5 w-5" />
                        </Button>
                    </Link>
                    <div>
                        <h1 className="text-3xl font-bold">{user.fullName}</h1>
                        <p className="text-muted-foreground">{user.email}</p>
                    </div>
                </div>
                <div className="flex gap-2">
                    <Badge variant={user.isActive ? 'default' : 'destructive'}>
                        {user.isActive ? (
                            <>
                                <UserCheck className="h-3 w-3 mr-1" />
                                Active
                            </>
                        ) : (
                            <>
                                <UserX className="h-3 w-3 mr-1" />
                                Suspended
                            </>
                        )}
                    </Badge>
                    <Badge variant={user.role === UserRole.ADMIN ? 'default' : 'secondary'}>
                        {user.role === UserRole.ADMIN && <Shield className="h-3 w-3 mr-1" />}
                        {user.role}
                    </Badge>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* User Information */}
                <div className="lg:col-span-2 space-y-6">
                    {/* Basic Info */}
                    <Card>
                        <CardHeader>
                            <CardTitle>Personal Information</CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <Label className="text-muted-foreground">First Name</Label>
                                    <p className="font-medium">{user.firstName}</p>
                                </div>
                                <div>
                                    <Label className="text-muted-foreground">Last Name</Label>
                                    <p className="font-medium">{user.lastName}</p>
                                </div>
                            </div>
                            <div>
                                <Label className="text-muted-foreground">Email</Label>
                                <div className="flex items-center gap-2">
                                    <Mail className="h-4 w-4 text-muted-foreground" />
                                    <p className="font-medium">{user.email}</p>
                                </div>
                            </div>
                            <div>
                                <Label className="text-muted-foreground">Branch of Service</Label>
                                <p className="font-medium">{user.branchOfService}</p>
                            </div>
                        </CardContent>
                    </Card>

                    {/* Address Info */}
                    <Card>
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <MapPin className="h-5 w-5" />
                                Address Information
                            </CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            {user.isHomeless ? (
                                <div className="flex items-center gap-2 text-orange-600">
                                    <Home className="h-5 w-5" />
                                    <span className="font-medium">Currently Homeless</span>
                                </div>
                            ) : (
                                <>
                                    <div>
                                        <Label className="text-muted-foreground">Address Line 1</Label>
                                        <p className="font-medium">{user.addressLine1}</p>
                                    </div>
                                    {user.addressLine2 && (
                                        <div>
                                            <Label className="text-muted-foreground">Address Line 2</Label>
                                            <p className="font-medium">{user.addressLine2}</p>
                                        </div>
                                    )}
                                    <div className="grid grid-cols-3 gap-4">
                                        <div>
                                            <Label className="text-muted-foreground">City</Label>
                                            <p className="font-medium">{user.city}</p>
                                        </div>
                                        <div>
                                            <Label className="text-muted-foreground">State</Label>
                                            <p className="font-medium">{user.state}</p>
                                        </div>
                                        <div>
                                            <Label className="text-muted-foreground">ZIP Code</Label>
                                            <p className="font-medium">{user.zipCode}</p>
                                        </div>
                                    </div>
                                </>
                            )}
                        </CardContent>
                    </Card>

                    {/* Account Info */}
                    <Card>
                        <CardHeader>
                            <CardTitle>Account Information</CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <Label className="text-muted-foreground flex items-center gap-2">
                                        <Calendar className="h-4 w-4" />
                                        Joined
                                    </Label>
                                    <p className="font-medium">
                                        {new Date(user.createdAt).toLocaleDateString('en-US', {
                                            year: 'numeric',
                                            month: 'long',
                                            day: 'numeric'
                                        })}
                                    </p>
                                </div>
                                <div>
                                    <Label className="text-muted-foreground flex items-center gap-2">
                                        <Clock className="h-4 w-4" />
                                        Last Login
                                    </Label>
                                    <p className="font-medium">
                                        {user.lastLoginAt
                                            ? new Date(user.lastLoginAt).toLocaleDateString('en-US', {
                                                year: 'numeric',
                                                month: 'long',
                                                day: 'numeric'
                                            })
                                            : 'Never'}
                                    </p>
                                </div>
                            </div>
                            {!user.isActive && user.suspendedAt && (
                                <div className="border-l-4 border-red-500 pl-4 py-2 bg-red-50">
                                    <Label className="text-red-700">Suspended</Label>
                                    <p className="text-sm text-red-600 mt-1">
                                        {new Date(user.suspendedAt).toLocaleDateString('en-US', {
                                            year: 'numeric',
                                            month: 'long',
                                            day: 'numeric'
                                        })}
                                    </p>
                                    {user.suspendedReason && (
                                        <>
                                            <Label className="text-red-700 mt-2">Reason</Label>
                                            <p className="text-sm text-red-600">{user.suspendedReason}</p>
                                        </>
                                    )}
                                </div>
                            )}
                        </CardContent>
                    </Card>
                </div>

                {/* Actions Sidebar */}
                <div className="space-y-4">
                    <Card>
                        <CardHeader>
                            <CardTitle>Admin Actions</CardTitle>
                            <CardDescription>Manage this user account</CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-2">
                            {/* Update Role */}
                            <Button
                                className="w-full justify-start"
                                variant="outline"
                                onClick={() => {
                                    setSelectedRole(user.role);
                                    setShowRoleDialog(true);
                                }}
                            >
                                <Shield className="h-4 w-4 mr-2" />
                                Update Role
                            </Button>

                            {/* Suspend/Activate */}
                            {user.isActive ? (
                                <Button
                                    className="w-full justify-start"
                                    variant="outline"
                                    onClick={() => setShowSuspendDialog(true)}
                                    disabled={user.role === UserRole.ADMIN}
                                >
                                    <UserX className="h-4 w-4 mr-2" />
                                    Suspend User
                                </Button>
                            ) : (
                                <Button
                                    className="w-full justify-start"
                                    variant="outline"
                                    onClick={handleActivate}
                                >
                                    <UserCheck className="h-4 w-4 mr-2" />
                                    Activate User
                                </Button>
                            )}

                            {/* Edit User Info */}
                            <Button
                                className="w-full justify-start"
                                variant="outline"
                                onClick={() => toast.info('User editing feature coming soon')}  // CHANGED
                            >
                                <Edit className="h-4 w-4 mr-2" />
                                Edit Information
                            </Button>

                            {/* Delete User */}
                            <Button
                                className="w-full justify-start"
                                variant="destructive"
                                onClick={() => setShowDeleteDialog(true)}
                                disabled={user.role === UserRole.ADMIN}
                            >
                                <Trash2 className="h-4 w-4 mr-2" />
                                Delete User
                            </Button>
                        </CardContent>
                    </Card>
                </div>
            </div>

            {/* Update Role Dialog */}
            <Dialog open={showRoleDialog} onOpenChange={setShowRoleDialog}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Update User Role</DialogTitle>
                        <DialogDescription>
                            Change the role for {user.fullName}
                        </DialogDescription>
                    </DialogHeader>
                    <div className="py-4">
                        <Label>Role</Label>
                        <Select value={selectedRole} onValueChange={(value) => setSelectedRole(value as UserRole)}>
                            <SelectTrigger className="mt-2">
                                <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value={UserRole.USER}>User</SelectItem>
                                <SelectItem value={UserRole.ADMIN}>Admin</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setShowRoleDialog(false)}>
                            Cancel
                        </Button>
                        <Button onClick={handleRoleUpdate} disabled={updateRoleMutation.isPending}>
                            {updateRoleMutation.isPending ? 'Updating...' : 'Update Role'}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* Suspend Dialog */}
            <Dialog open={showSuspendDialog} onOpenChange={setShowSuspendDialog}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Suspend User</DialogTitle>
                        <DialogDescription>
                            Provide a reason for suspending {user.fullName}'s account
                        </DialogDescription>
                    </DialogHeader>
                    <div className="py-4">
                        <Label>Reason for Suspension</Label>
                        <Textarea
                            className="mt-2"
                            placeholder="Enter reason..."
                            value={suspendReason}
                            onChange={(e) => setSuspendReason(e.target.value)}
                            rows={4}
                        />
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setShowSuspendDialog(false)}>
                            Cancel
                        </Button>
                        <Button
                            variant="destructive"
                            onClick={handleSuspend}
                            disabled={suspendMutation.isPending}
                        >
                            {suspendMutation.isPending ? 'Suspending...' : 'Suspend User'}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* Delete Confirmation Dialog */}
            <Dialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Delete User</DialogTitle>
                        <DialogDescription>
                            Are you sure you want to permanently delete {user.fullName}'s account? This action cannot be undone.
                        </DialogDescription>
                    </DialogHeader>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setShowDeleteDialog(false)}>
                            Cancel
                        </Button>
                        <Button
                            variant="destructive"
                            onClick={handleDelete}
                            disabled={deleteMutation.isPending}
                        >
                            {deleteMutation.isPending ? 'Deleting...' : 'Delete User'}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}