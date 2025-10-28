// ==================== USER TYPES ====================

export interface User {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
    fullName: string;
    branchOfService: BranchOfService;
    branchDisplayName: string;
    addressLine1?: string;
    addressLine2?: string;
    city?: string;
    state?: string;
    zipCode?: string;
    isHomeless: boolean;
    createdAt: string;
}

export type BranchOfService =
    | 'ARMY'
    | 'NAVY'
    | 'AIR_FORCE'
    | 'MARINES'
    | 'COAST_GUARD'
    | 'SPACE_FORCE';

export interface RegisterRequest {
    email: string;
    password: string;
    confirmPassword: string;
    firstName: string;
    lastName: string;
    branchOfService: BranchOfService;
    city?: string;
    state?: string;
    zipCode?: string;
    isHomeless: boolean;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface AuthResponse {
    token: string;
    refreshToken: string;
    user: User;
}

// ==================== RESOURCE TYPES ====================

export interface Resource {
    id: string;
    category: ResourceCategory;
    name: string;
    description: string;
    websiteUrl?: string;
    phoneNumber?: string;
    email?: string;
    addressLine1?: string;
    city?: string;
    state?: string;
    zipCode?: string;
    isNational: boolean;
    eligibilityCriteria?: string;
    createdAt: string;
}

export interface ResourceSummary {
    id: string;
    categoryId: number;
    categoryName: string;
    name: string;
    shortDescription: string;
    city?: string;
    state?: string;
    isNational: boolean;
}

export interface ResourceCategory {
    id: number;
    name: string;
    description: string;
    iconName: string;
}

export interface ResourceCategoryWithCount extends ResourceCategory {
    resourceCount: number;
}

// ==================== SAVED RESOURCE TYPES ====================

export interface SavedResource {
    id: string;
    resource: Resource;
    notes?: string;
    savedAt: string;
}

export interface SaveResourceRequest {
    resourceId: string;
    notes?: string;
}

// ==================== API RESPONSE TYPES ====================

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
    timestamp?: string;
}

export interface PageResponse<T> {
    content: T[];
    pageNumber: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    isFirst: boolean;
    isLast: boolean;
    hasNext: boolean;
    hasPrevious: boolean;
}

export interface ErrorResponse {
    status: number;
    error: string;
    message: string;
    path: string;
    timestamp: string;
}

// ==================== SEARCH & FILTER TYPES ====================

export interface ResourceSearchParams {
    keyword?: string;
    categoryId?: number;
    state?: string;
    page?: number;
    size?: number;
}