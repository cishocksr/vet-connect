import React, { createContext, useContext, useEffect, useState } from 'react';
import type {BranchOfService} from '../types';
import { branchThemes, defaultTheme } from '../config/branch-themes';
import type { BranchTheme } from '../config/branch-themes';

interface ThemeContextType {
    currentTheme: BranchTheme;
    setThemeByBranch: (branch: BranchOfService) => void;
    resetTheme: () => void;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [currentTheme, setCurrentTheme] = useState<BranchTheme>(defaultTheme);

    const applyTheme = (theme: BranchTheme) => {
        const root = document.documentElement;
        root.style.setProperty('--color-primary', theme.primary);
        root.style.setProperty('--color-primary-dark', theme.primaryDark);
        root.style.setProperty('--color-primary-light', theme.primaryLight);
        root.style.setProperty('--color-secondary', theme.secondary);
        root.style.setProperty('--color-accent', theme.accent);
        root.style.setProperty('--color-background', theme.background);
        root.style.setProperty('--color-text', theme.text);
    };

    const setThemeByBranch = (branch: BranchOfService) => {
        const theme = branchThemes[branch] || defaultTheme;
        setCurrentTheme(theme);
        applyTheme(theme);
    };

    const resetTheme = () => {
        setCurrentTheme(defaultTheme);
        applyTheme(defaultTheme);
    };

    useEffect(() => {
        applyTheme(defaultTheme);
    }, []);

    return (
        <ThemeContext.Provider value={{ currentTheme, setThemeByBranch, resetTheme }}>
            {children}
        </ThemeContext.Provider>
    );
};

// eslint-disable-next-line react-refresh/only-export-components
export const useTheme = () => {
    const context = useContext(ThemeContext);
    if (!context) {
        throw new Error('useTheme must be used within ThemeProvider');
    }
    return context;
};