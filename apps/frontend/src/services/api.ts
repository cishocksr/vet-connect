import axios from 'axios';



// Use runtime config if available, fallback to build-time env var
const getApiBaseUrl = () => {
    if (typeof window !== 'undefined' && (window as any).ENV?.API_BASE_URL) {
        return (window as any).ENV.API_BASE_URL;
    }
    return import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
};

const api = axios.create({
    baseURL: getApiBaseUrl(),
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request interceptor for JWT token
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        } else if (!config.url?.includes('/auth/')) {
            // Only warn if it's not an auth endpoint (login/register don't need tokens)
            console.warn('No token found in localStorage for request:', config.url);
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor for error handling
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        // Handle 401 Unauthorized
        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                const refreshToken = localStorage.getItem('refreshToken');
                if (refreshToken) {
                    const response = await axios.post(
                        `${import.meta.env.VITE_API_BASE_URL}/auth/refresh`,
                        { refreshToken }
                    );

                    // Extract BOTH tokens and user data from response
                    const { token, refreshToken: newRefreshToken, user } = response.data.data;

                    // Update ALL tokens in localStorage
                    localStorage.setItem('token', token);
                    localStorage.setItem('refreshToken', newRefreshToken);

                    // Also update user data if included
                    if (user) {
                        localStorage.setItem('user', JSON.stringify(user));
                    }

                    originalRequest.headers.Authorization = `Bearer ${token}`;
                    return api(originalRequest);
                }
            } catch (refreshError) {
                // Refresh failed, redirect to login
                localStorage.clear();
                window.location.href = '/login';
                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(error);
    }
);

export default api;