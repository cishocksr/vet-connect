import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen, renderHook, act, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ThemeProvider, useTheme } from '@/contexts/theme-context';

// Test component that uses the theme hook
const TestComponent = () => {
    const { currentTheme, setThemeByBranch, resetTheme } = useTheme();

    return (
        <div>
            <div data-testid="theme-primary">{currentTheme.primary}</div>
            <div data-testid="theme-secondary">{currentTheme.secondary}</div>
            <button onClick={() => setThemeByBranch('ARMY')}>Set Army Theme</button>
            <button onClick={() => setThemeByBranch('NAVY')}>Set Navy Theme</button>
            <button onClick={() => setThemeByBranch('AIR_FORCE')}>Set Air Force Theme</button>
            <button onClick={resetTheme}>Reset Theme</button>
        </div>
    );
};

describe('ThemeContext', () => {
    beforeEach(() => {
        // Clear any custom CSS properties
        document.documentElement.removeAttribute('style');
    });

    it('should provide theme context', () => {
        render(
            <ThemeProvider>
                <TestComponent />
            </ThemeProvider>
        );

        const themePrimary = screen.getByTestId('theme-primary');
        expect(themePrimary).toBeInTheDocument();
    });

    it('should start with default theme', () => {
        render(
            <ThemeProvider>
                <TestComponent />
            </ThemeProvider>
        );

        const themePrimary = screen.getByTestId('theme-primary');
        expect(themePrimary.textContent).toBe('#1976d2'); // Default primary color
    });

    it('should change theme when setThemeByBranch is called', async () => {
        const user = userEvent.setup();
        const { getByText, getByTestId } = render(
            <ThemeProvider>
                <TestComponent />
            </ThemeProvider>
        );

        const armyButton = getByText('Set Army Theme');
        await user.click(armyButton);

        await waitFor(() => {
            const themePrimary = getByTestId('theme-primary');
            expect(themePrimary.textContent).toBe('#000000'); // Army black
        });
    });

    it('should apply different branch themes', async () => {
        const user = userEvent.setup();
        const { getByText, getByTestId } = render(
            <ThemeProvider>
                <TestComponent />
            </ThemeProvider>
        );

        // Set Navy theme
        await user.click(getByText('Set Navy Theme'));
        await waitFor(() => {
            expect(getByTestId('theme-primary').textContent).toBe('#000080'); // Navy blue
        });

        // Set Air Force theme
        await user.click(getByText('Set Air Force Theme'));
        await waitFor(() => {
            expect(getByTestId('theme-primary').textContent).toBe('#00308F'); // Air Force blue
        });
    });

    it('should reset theme to default', async () => {
        const user = userEvent.setup();
        const { getByText, getByTestId } = render(
            <ThemeProvider>
                <TestComponent />
            </ThemeProvider>
        );

        // Change to Army theme
        await user.click(getByText('Set Army Theme'));
        await waitFor(() => {
            expect(getByTestId('theme-primary').textContent).toBe('#000000');
        });

        // Reset to default
        await user.click(getByText('Reset Theme'));
        await waitFor(() => {
            expect(getByTestId('theme-primary').textContent).toBe('#1976d2');
        });
    });

    it('should apply CSS variables to document root', () => {
        render(
            <ThemeProvider>
                <TestComponent />
            </ThemeProvider>
        );

        const root = document.documentElement;
        const primaryColor = root.style.getPropertyValue('--color-primary');

        // Should have applied some theme colors
        expect(primaryColor).toBeTruthy();
    });

    it('should throw error when useTheme is used outside provider', () => {
        // Suppress expected error output
        const originalError = console.error;
        console.error = () => {};

        expect(() => {
            renderHook(() => useTheme());
        }).toThrow('useTheme must be used within ThemeProvider');

        console.error = originalError;
    });

    it('should update CSS variables when theme changes', async () => {
        const user = userEvent.setup();
        const { getByText } = render(
            <ThemeProvider>
                <TestComponent />
            </ThemeProvider>
        );

        const root = document.documentElement;

        // Get initial primary color
        const initialPrimary = root.style.getPropertyValue('--color-primary');

        // Change theme
        await user.click(getByText('Set Army Theme'));

        await waitFor(() => {
            const newPrimary = root.style.getPropertyValue('--color-primary');

            // Color should have changed
            expect(newPrimary).toBeTruthy();
            expect(newPrimary).not.toBe(initialPrimary);
        });
    });

    it('should handle unknown branch by using default theme', () => {
        const { result } = renderHook(() => useTheme(), {
            wrapper: ThemeProvider
        });

        act(() => {
            // @ts-expect-error - Testing with invalid branch
            result.current.setThemeByBranch('INVALID_BRANCH');
        });

        // Should fallback to default theme
        expect(result.current.currentTheme.primary).toBe('#1976d2');
    });

    it('should have all required theme properties', () => {
        const { result } = renderHook(() => useTheme(), {
            wrapper: ThemeProvider
        });

        const theme = result.current.currentTheme;

        expect(theme.primary).toBeDefined();
        expect(theme.primaryDark).toBeDefined();
        expect(theme.primaryLight).toBeDefined();
        expect(theme.secondary).toBeDefined();
        expect(theme.accent).toBeDefined();
        expect(theme.background).toBeDefined();
        expect(theme.text).toBeDefined();
    });
});