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

        const branding = screen.getAllByText(/vetconnect/i);
        expect(branding.length).toBeGreaterThan(0);
        expect(branding[0]).toBeInTheDocument();
    });

    it('should have proper footer structure', () => {
        const { container } = render(<Footer />);

        // Footer should be present
        const footer = container.querySelector('footer');
        expect(footer).toBeInTheDocument();
    });

    it('should display veterans-focused messaging', () => {
        render(<Footer />);

        // Look for veteran-related text - use getAllByText for multiple matches
        const veteranTexts = screen.queryAllByText(/veteran/i);
        const serviceMemberTexts = screen.queryAllByText(/service member/i);
        const militaryTexts = screen.queryAllByText(/military/i);

        const hasVeteranFocus = veteranTexts.length > 0 || serviceMemberTexts.length > 0 || militaryTexts.length > 0;
        expect(hasVeteranFocus).toBeTruthy();
    });
});