export interface BranchTheme {
    primary: string;
    primaryDark: string;
    primaryLight: string;
    secondary: string;
    accent: string;
    background: string;
    text: string;
}

export const branchThemes: Record<string, BranchTheme> = {
    ARMY: {
        primary: '#000000',        // Black
        primaryDark: '#1a1a1a',
        primaryLight: '#333333',
        secondary: '#FFD700',      // Gold
        accent: '#FDB913',
        background: '#f5f5f5',
        text: '#000000',
    },
    NAVY: {
        primary: '#000080',        // Navy Blue
        primaryDark: '#00005a',
        primaryLight: '#1a1a9e',
        secondary: '#FFD700',      // Gold
        accent: '#FDB913',
        background: '#f0f4f8',
        text: '#000080',
    },
    AIR_FORCE: {
        primary: '#00308F',        // Air Force Blue
        primaryDark: '#002366',
        primaryLight: '#1a4ca8',
        secondary: '#FFD700',      // Gold/Yellow
        accent: '#B0BEC5',         // Silver
        background: '#e8f4f8',
        text: '#00308F',
    },
    MARINES: {
        primary: '#CC0000',        // Scarlet Red
        primaryDark: '#990000',
        primaryLight: '#e61a1a',
        secondary: '#FFD700',      // Gold
        accent: '#B8860B',         // Dark Gold
        background: '#fff5f5',
        text: '#CC0000',
    },
    COAST_GUARD: {
        primary: '#0000FF',        // Coast Guard Blue
        primaryDark: '#0000cc',
        primaryLight: '#1a1aff',
        secondary: '#FF0000',      // Red
        accent: '#FFD700',         // Gold
        background: '#f0f8ff',
        text: '#0000FF',
    },
    SPACE_FORCE: {
        primary: '#1C2A5A',        // Space Force Blue
        primaryDark: '#0f1829',
        primaryLight: '#2a3f6f',
        secondary: '#FFFFFF',      // White
        accent: '#C0C0C0',         // Silver
        background: '#0a0e1a',
        text: '#FFFFFF',
    },
};

export const defaultTheme: BranchTheme = {
    primary: '#1976d2',
    primaryDark: '#115293',
    primaryLight: '#4791db',
    secondary: '#dc004e',
    accent: '#f50057',
    background: '#ffffff',
    text: '#000000',
};