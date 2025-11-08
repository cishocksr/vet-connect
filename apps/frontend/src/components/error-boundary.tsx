import React, { Component, ErrorInfo, ReactNode } from 'react';
import { AlertTriangle } from 'lucide-react';
import { Button } from './ui/button';

interface Props {
    children: ReactNode;
    fallback?: ReactNode;
}

interface State {
    hasError: boolean;
    error?: Error;
}

export class ErrorBoundary extends Component<Props, State> {
    public state: State = {
        hasError: false
    };

    public static getDerivedStateFromError(error: Error): State {
        return { hasError: true, error };
    }

    public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
        console.error('Error boundary caught error:', error, errorInfo);
        // TODO: Send to error reporting service (Sentry, etc.)
    }

    private handleReset = () => {
        this.setState({ hasError: false, error: undefined });
    };

    public render() {
        if (this.state.hasError) {
            return (
                <div className="min-h-screen flex items-center justify-center p-4">
                    <div className="max-w-md w-full text-center">
                        <AlertTriangle className="w-16 h-16 text-destructive mx-auto mb-4" />
                        <h1 className="text-2xl font-bold mb-2">Something went wrong</h1>
                        <p className="text-muted-foreground mb-6">
                            We apologize for the inconvenience. Please try refreshing the page.
                        </p>
                        <div className="space-y-2">
                            <Button onClick={this.handleReset} className="w-full">
                                Try Again
                            </Button>
                            <Button
                                onClick={() => window.location.href = '/'}
                                variant="outline"
                                className="w-full"
                            >
                                Go to Homepage
                            </Button>
                        </div>
                    </div>
                </div>
            );
        }

        return this.props.children;
    }
}