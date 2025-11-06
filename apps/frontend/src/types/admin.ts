// Use const object and type union instead of enum
export const UserRole = {
    USER: 'USER',
    ADMIN: 'ADMIN'
} as const;

export type UserRole = typeof UserRole[keyof typeof UserRole];

export interface AdminUserListDTO {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
    fullName: string;
    branchOfService: string;
    role: UserRole;
    isActive: boolean;
    isHomeless: boolean;
    city: string;
    state: string;
    createdAt: string;
    lastLoginAt: string | null;
}

export interface AdminUserDetailDTO {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
    fullName: string;
    branchOfService: string;
    role: UserRole;
    addressLine1: string;
    addressLine2: string | null;
    city: string;
    state: string;
    zipCode: string;
    isHomeless: boolean;
    isActive: boolean;
    suspendedAt: string | null;
    suspendedReason: string | null;
    createdAt: string;
    updatedAt: string;
    lastLoginAt: string | null;
    profilePictureUrl: string | null;
}

export interface UpdateUserRoleRequest {
    role: UserRole;
}

export interface SuspendUserRequest {
    reason: string;
}

export interface AdminStatsDTO {
    totalUsers: number;
    activeUsers: number;
    suspendedUsers: number;
    homelessUsers: number;
    newUsersThisMonth: number;
    newUsersToday: number;
    usersByBranch: Record<string, number>;
    usersByState: Record<string, number>;
}