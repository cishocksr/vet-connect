import { useAuthStore } from '../store/auth-store';
import { useNavigate } from 'react-router-dom';
import { useEffect } from 'react'; // ADD THIS
import authService from '../services/auth-service';
import type { LoginRequest, RegisterRequest, User } from '../types';
import { useTheme } from "@/contexts/theme-context";

export function useAuth() {
    const { user, isAuthenticated, setUser, clearUser } = useAuthStore();
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
        login,
        register,
        logout,
        setUser: updateUser,
    };
}