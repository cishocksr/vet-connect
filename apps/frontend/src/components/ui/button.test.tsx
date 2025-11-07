import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '../../__test__/utils.tsx';
import { Button } from './button';

describe('Button', () => {
    it('should render button with text', () => {
        render(<Button>Click me</Button>);

        const button = screen.getByRole('button', { name: /click me/i });
        expect(button).toBeInTheDocument();
    });

    it('should handle click events', () => {
        const handleClick = vi.fn();
        render(<Button onClick={handleClick}>Click me</Button>);

        const button = screen.getByRole('button', { name: /click me/i });
        fireEvent.click(button);

        expect(handleClick).toHaveBeenCalledTimes(1);
    });

    it('should be disabled when disabled prop is true', () => {
        render(<Button disabled>Disabled Button</Button>);

        const button = screen.getByRole('button', { name: /disabled button/i });
        expect(button).toBeDisabled();
    });

    it('should not trigger onClick when disabled', () => {
        const handleClick = vi.fn();
        render(
            <Button disabled onClick={handleClick}>
                Disabled Button
            </Button>
        );

        const button = screen.getByRole('button', { name: /disabled button/i });
        fireEvent.click(button);

        expect(handleClick).not.toHaveBeenCalled();
    });

    it('should render with different variants', () => {
        const { rerender } = render(<Button variant="default">Default</Button>);
        expect(screen.getByRole('button')).toBeInTheDocument();

        rerender(<Button variant="destructive">Destructive</Button>);
        expect(screen.getByRole('button')).toBeInTheDocument();

        rerender(<Button variant="outline">Outline</Button>);
        expect(screen.getByRole('button')).toBeInTheDocument();

        rerender(<Button variant="ghost">Ghost</Button>);
        expect(screen.getByRole('button')).toBeInTheDocument();
    });

    it('should render with different sizes', () => {
        const { rerender } = render(<Button size="default">Default</Button>);
        expect(screen.getByRole('button')).toBeInTheDocument();

        rerender(<Button size="sm">Small</Button>);
        expect(screen.getByRole('button')).toBeInTheDocument();

        rerender(<Button size="lg">Large</Button>);
        expect(screen.getByRole('button')).toBeInTheDocument();

        rerender(<Button size="icon">Icon</Button>);
        expect(screen.getByRole('button')).toBeInTheDocument();
    });

    it('should accept custom className', () => {
        render(<Button className="custom-class">Custom</Button>);

        const button = screen.getByRole('button', { name: /custom/i });
        expect(button).toHaveClass('custom-class');
    });

    it('should render as child component when asChild prop is true', () => {
        render(
            <Button asChild>
                <a href="/test">Link Button</a>
            </Button>
        );

        const link = screen.getByRole('link', { name: /link button/i });
        expect(link).toBeInTheDocument();
        expect(link).toHaveAttribute('href', '/test');
    });
});