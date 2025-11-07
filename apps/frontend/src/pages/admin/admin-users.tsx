import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select';
import adminService from '@/services/admin-service';
import { UserRole } from '@/types/admin';
import {
    Search,
    ChevronLeft,
    ChevronRight,
    UserCheck,
    UserX,
    Shield,
    Home
} from 'lucide-react';

type FilterType = 'all' | 'role' | 'status' | 'homeless' | 'search';

export default function AdminUsers() {
    const [page, setPage] = useState(0);
    const [filterType, setFilterType] = useState<FilterType>('all');
    const [roleFilter, setRoleFilter] = useState<UserRole>(UserRole.USER);
    const [statusFilter, setStatusFilter] = useState<boolean>(true);
    const [searchQuery, setSearchQuery] = useState('');
    const [searchInput, setSearchInput] = useState('');

    const { data, isLoading } = useQuery({
        queryKey: ['admin-users', page, filterType, roleFilter, statusFilter, searchQuery],
        queryFn: () => {
            switch (filterType) {
                case 'role':
                    return adminService.getUsersByRole(roleFilter, page);
                case 'status':
                    return adminService.getUsersByStatus(statusFilter, page);
                case 'homeless':
                    return adminService.getHomelessUsers(page);
                case 'search':
                    return adminService.searchUsers(searchQuery, page);
                default:
                    return adminService.getAllUsers(page);
            }
        }
    });

    const handleSearch = () => {
        setSearchQuery(searchInput);
        setFilterType('search');
        setPage(0);
    };

    const handleFilterChange = (type: FilterType) => {
        setFilterType(type);
        setPage(0);
        if (type !== 'search') {
            setSearchQuery('');
            setSearchInput('');
        }
    };

    return (
        <div className="container mx-auto px-4 py-8">
            <div className="flex items-center justify-between mb-8">
                <h1 className="text-3xl font-bold">User Management</h1>
                <Link to="/admin">
                    <Button variant="outline">Back to Dashboard</Button>
                </Link>
            </div>

            {/* Filters */}
            <Card className="mb-6">
                <CardHeader>
                    <CardTitle>Filters</CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                        {/* Search */}
                        <div className="md:col-span-2 flex gap-2">
                            <Input
                                placeholder="Search by name, email, or location..."
                                value={searchInput}
                                onChange={(e) => setSearchInput(e.target.value)}
                                onKeyDown={(e) => {
                                    if (e.key === 'Enter') handleSearch();
                                }}
                            />
                            <Button onClick={handleSearch}>
                                <Search className="h-4 w-4" />
                            </Button>
                        </div>

                        {/* Filter Type */}
                        <Select
                            value={filterType}
                            onValueChange={(value) => handleFilterChange(value as FilterType)}
                        >
                            <SelectTrigger>
                                <SelectValue placeholder="Filter by..." />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="all">All Users</SelectItem>
                                <SelectItem value="role">By Role</SelectItem>
                                <SelectItem value="status">By Status</SelectItem>
                                <SelectItem value="homeless">Homeless Veterans</SelectItem>
                            </SelectContent>
                        </Select>

                        {/* Role Filter (if filter type is role) */}
                        {filterType === 'role' && (
                            <Select
                                value={roleFilter}
                                onValueChange={(value) => {
                                    setRoleFilter(value as UserRole);
                                    setPage(0);
                                }}
                            >
                                <SelectTrigger>
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value={UserRole.USER}>Users</SelectItem>
                                    <SelectItem value={UserRole.ADMIN}>Admins</SelectItem>
                                </SelectContent>
                            </Select>
                        )}

                        {/* Status Filter (if filter type is status) */}
                        {filterType === 'status' && (
                            <Select
                                value={statusFilter.toString()}
                                onValueChange={(value) => {
                                    setStatusFilter(value === 'true');
                                    setPage(0);
                                }}
                            >
                                <SelectTrigger>
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="true">Active</SelectItem>
                                    <SelectItem value="false">Suspended</SelectItem>
                                </SelectContent>
                            </Select>
                        )}
                    </div>
                </CardContent>
            </Card>

            {/* Users Table */}
            <Card>
                <CardContent className="p-0">
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead className="bg-muted">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium uppercase">User</th>
                                <th className="px-6 py-3 text-left text-xs font-medium uppercase">Branch</th>
                                <th className="px-6 py-3 text-left text-xs font-medium uppercase">Location</th>
                                <th className="px-6 py-3 text-left text-xs font-medium uppercase">Role</th>
                                <th className="px-6 py-3 text-left text-xs font-medium uppercase">Status</th>
                                <th className="px-6 py-3 text-left text-xs font-medium uppercase">Joined</th>
                                <th className="px-6 py-3 text-left text-xs font-medium uppercase">Actions</th>
                            </tr>
                            </thead>
                            <tbody className="divide-y">
                            {isLoading ? (
                                [...Array(10)].map((_, i) => (
                                    <tr key={i}>
                                        {[...Array(7)].map((_, j) => (
                                            <td key={j} className="px-6 py-4">
                                                <Skeleton className="h-4 w-full" />
                                            </td>
                                        ))}
                                    </tr>
                                ))
                            ) : data?.content && data.content.length > 0 ? (
                                data.content.map((user) => (
                                    <tr key={user.id} className="hover:bg-muted/50">
                                        <td className="px-6 py-4">
                                            <div className="flex flex-col">
                                                <span className="font-medium">{user.fullName}</span>
                                                <span className="text-sm text-muted-foreground">{user.email}</span>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 text-sm">{user.branchOfService}</td>
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-2">
                                                <span className="text-sm">{user.city}, {user.state}</span>
                                                {user.isHomeless && (
                                                    <Home className="h-4 w-4 text-orange-600" />
                                                )}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <Badge variant={user.role === UserRole.ADMIN ? 'default' : 'secondary'}>
                                                {user.role === UserRole.ADMIN && <Shield className="h-3 w-3 mr-1" />}
                                                {user.role}
                                            </Badge>
                                        </td>
                                        <td className="px-6 py-4">
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
                                        </td>
                                        <td className="px-6 py-4 text-sm">
                                            {new Date(user.createdAt).toLocaleDateString()}
                                        </td>
                                        <td className="px-6 py-4">
                                            <Link to={`/admin/users/${user.id}`}>
                                                <Button variant="outline" size="sm">
                                                    View Details
                                                </Button>
                                            </Link>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan={7} className="px-6 py-8 text-center text-muted-foreground">
                                        No users found
                                    </td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                    </div>

                    {/* Pagination */}
                    {data && data.totalPages > 1 && (
                        <div className="flex items-center justify-between px-6 py-4 border-t">
                            <div className="text-sm text-muted-foreground">
                                Showing {data.pageNumber * data.pageSize + 1} to{' '}
                                {Math.min((data.pageNumber + 1) * data.pageSize, data.totalElements)} of{' '}
                                {data.totalElements} users
                            </div>
                            <div className="flex gap-2">
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={() => setPage(page - 1)}
                                    disabled={data.isFirst}
                                >
                                    <ChevronLeft className="h-4 w-4" />
                                    Previous
                                </Button>
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={() => setPage(page + 1)}
                                    disabled={data.isLast}
                                >
                                    Next
                                    <ChevronRight className="h-4 w-4" />
                                </Button>
                            </div>
                        </div>
                    )}
                </CardContent>
            </Card>
        </div>
    );
}