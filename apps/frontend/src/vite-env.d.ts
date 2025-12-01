/// <reference types="vite/client" />

// Extend Vite's ImportMetaEnv with our custom variables
interface ImportMetaEnv {
    readonly VITE_API_BASE_URL: string
    readonly VITE_APP_NAME?: string
    readonly VITE_ENVIRONMENT?: string
    readonly VITE_ENABLE_ANALYTICS?: string
}

interface ImportMeta {
    readonly env: ImportMetaEnv
}

// Window extension for runtime env (if needed)
declare global {
    interface Window {
        ENV?: {
            API_BASE_URL: string;
        };
    }
}

export {};