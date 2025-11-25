import { describe, it, expect } from 'vitest';
import { render, screen } from '../__test__/utils.tsx';
import HomePage from './home-page';

describe('HomePage', () => {
    it('should render homepage', () => {
        render(<HomePage />);

        // Should have main content
        expect(document.body).toBeInTheDocument();
    });

    it('should display hero section or welcome message', () => {
        render(<HomePage />);

        // Look for hero heading or welcome text
        const heroText = screen.queryByRole('heading', { level: 1 }) ||
            screen.queryByText(/welcome|veteran|resources/i);

        expect(heroText).toBeTruthy();
    });

    it('should display resource categories', () => {
        render(<HomePage />);

        // Look for category cards or links
        const categories = screen.queryByText(/housing/i) ||
            screen.queryByText(/healthcare/i) ||
            screen.queryByText(/education/i) ||
            screen.queryByText(/financial/i);

        expect(categories).toBeTruthy();
    });

    it('should have call-to-action buttons', () => {
        render(<HomePage />);

        // Look for primary CTA buttons - may have multiple
        const ctaLinks = screen.queryAllByRole('link', { name: /browse resources|explore|get started/i });
        const ctaButtons = screen.queryAllByRole('button', { name: /browse resources|explore|get started/i });

        const hasCTA = ctaLinks.length > 0 || ctaButtons.length > 0;
        expect(hasCTA).toBeTruthy();
    });

    it('should render navigation links to key sections', () => {
        render(<HomePage />);

        // Should have links to resources or other pages
        const links = screen.getAllByRole('link');
        expect(links.length).toBeGreaterThan(0);
    });
});