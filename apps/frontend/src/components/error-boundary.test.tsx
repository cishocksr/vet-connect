import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ErrorBoundary } from '@/components/error-boundary';

// Component that throws an error
const ThrowError = ({ shouldThrow = true }: { shouldThrow?: boolean }) => {
    if (shouldThrow) {
        throw new Error('Test error');
    }
    return <div>No error</div>;
};

describe('ErrorBoundary', () => {
    // Suppress console.error for these tests
    const originalError = console.error;
    beforeEach(() => {
        console.error = vi.fn();
    });
    afterEach(() => {
        console.error = originalError;
    });

    it('should render children when no error', () => {
        render(
            <ErrorBoundary>
                <div>Child content</div>
            </ErrorBoundary>
        );

        expect(screen.getByText('Child content')).toBeInTheDocument();
    });

    it('should render error UI when child throws error', () => {
        render(
            <ErrorBoundary>
                <ThrowError />
            </ErrorBoundary>
        );

        expect(screen.getByText('Something went wrong')).toBeInTheDocument();
        expect(screen.getByText(/We apologize for the inconvenience/i)).toBeInTheDocument();
    });

    it('should display Try Again button', () => {
        render(
            <ErrorBoundary>
                <ThrowError />
            </ErrorBoundary>
        );

        expect(screen.getByRole('button', { name: /try again/i })).toBeInTheDocument();
    });

    it('should display Go to Homepage button', () => {
        render(
            <ErrorBoundary>
                <ThrowError />
            </ErrorBoundary>
        );

        expect(screen.getByRole('button', { name: /go to homepage/i })).toBeInTheDocument();
    });

    it('should show AlertTriangle icon', () => {
        const { container } = render(
            <ErrorBoundary>
                <ThrowError />
            </ErrorBoundary>
        );

        // Check for the icon (lucide-react renders as svg)
        const svg = container.querySelector('svg');
        expect(svg).toBeInTheDocument();
    });

    it('should not render children when error occurs', () => {
        render(
            <ErrorBoundary>
                <ThrowError />
            </ErrorBoundary>
        );

        expect(screen.queryByText('No error')).not.toBeInTheDocument();
    });

    it('should call console.error when error is caught', () => {
        const consoleErrorSpy = vi.spyOn(console, 'error');

        render(
            <ErrorBoundary>
                <ThrowError />
            </ErrorBoundary>
        );

        expect(consoleErrorSpy).toHaveBeenCalled();
    });
});