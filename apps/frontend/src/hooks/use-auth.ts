import { useAuthStore } from '../store/auth-store';
import { useNavigate } from 'react-router-dom';
import { useEffect } from 'react';
import authService from '../services/auth-service';
import type { LoginRequest, RegisterRequest, User } from '../types';
import { useTheme } from "@/contexts/theme-context";

// ADD THIS: Define the return type interface
interface UseAuthReturn {
    user: User | null;
    isAuthenticated: boolean;
    isAdmin: boolean;
    login: (credentials: LoginRequest) => Promise<void>;
    register: (data: RegisterRequest, options?: { onSuccess?: () => void }) => Promise<void>;
    logout: () => void;
    setUser: (updatedUser: User) => void;
}

export function useAuth(): UseAuthReturn {  // ADD RETURN TYPE HERE
    const { user, isAuthenticated, isAdmin, setUser, clearUser } = useAuthStore();
    const { setThemeByBranch, resetTheme } = useTheme();
    const navigate = useNavigate();

    // Apply theme when user loads (on app initialization/refresh)
    useEffect(() => {
        if (user?.branchOfService) {
            setThemeByBranch(user.branchOfService);
        }
    }, [user, setThemeByBranch]);

    const login = async (credentials: LoginRequest) => {
        const response = await authService.login(credentials);
        setUser(response.user, response.token);

        // Apply theme after login
        if (response.user.branchOfService) {
            setThemeByBranch(response.user.branchOfService);
        }

        navigate('/dashboard');
    };

    const register = async (data: RegisterRequest, options?: { onSuccess?: () => void }) => {
        const response = await authService.register(data);
        setUser(response.user, response.token);

        // Apply theme after registration
        if (response.user.branchOfService) {
            setThemeByBranch(response.user.branchOfService);
        }

        if (options?.onSuccess) {
            options.onSuccess();
        } else {
            navigate('/dashboard');
        }
    };

    const logout = () => {
        clearUser();
        resetTheme(); // Reset theme to default
        navigate('/login');
    };

    // Add updateUser function for profile updates
    const updateUser = (updatedUser: User) => {
        const currentToken = localStorage.getItem('token');
        if (currentToken) {
            setUser(updatedUser, currentToken);

            // Apply theme when profile updates
            if (updatedUser.branchOfService) {
                setThemeByBranch(updatedUser.branchOfService);
            }
        }
    };

    return {
        user,
        isAuthenticated,
        isAdmin,
        login,
        register,
        logout,
        setUser: updateUser,
    };
}