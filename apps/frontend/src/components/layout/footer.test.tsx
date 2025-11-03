import { describe, it, expect } from 'vitest';
import { render, screen } from '../../__test__/utils.tsx';
import { Footer } from './footer';

describe('Footer', () => {
    it('should render footer element', () => {
        render(<Footer />);

        const footer = screen.getByRole('contentinfo');
        expect(footer).toBeInTheDocument();
    });

    it('should display copyright information', () => {
        render(<Footer />);

        const currentYear = new Date().getFullYear();
        const copyrightText = screen.getByText(new RegExp(`${currentYear}`, 'i'));

        expect(copyrightText).toBeInTheDocument();
    });

    it('should contain VetConnect branding', () => {
        render(<Footer />);

        const branding = screen.getByText(/vetconnect/i);
        expect(branding).toBeInTheDocument();
    });

    it('should have proper footer structure', () => {
        const { container } = render(<Footer />);

        // Footer should be present
        const footer = container.querySelector('footer');
        expect(footer).toBeInTheDocument();
    });

    it('should display veterans-focused messaging', () => {
        render(<Footer />);

        // Look for veteran-related text
        const veteranText = screen.queryByText(/veteran/i) ||
            screen.queryByText(/service member/i) ||
            screen.queryByText(/military/i);

        expect(veteranText).toBeTruthy();
    });
});