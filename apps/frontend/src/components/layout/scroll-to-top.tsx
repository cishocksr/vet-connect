import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';

/**
 * ScrollToTop Component
 *
 * Automatically scrolls to the top of the page whenever the route changes.
 * This improves UX by ensuring users see the top of each new page.
 *
 * HOW IT WORKS:
 * - useLocation hook detects route changes
 * - useEffect runs whenever pathname changes
 * - window.scrollTo scrolls to top of page
 *
 * USAGE:
 * Add <ScrollToTop /> inside BrowserRouter, before Routes
 */
export function ScrollToTop() {
    const { pathname } = useLocation();

    useEffect(() => {
        // Scroll to top whenever pathname changes
        window.scrollTo({
            top: 0,
            left: 0,
            behavior: 'instant' // Use 'smooth' for smooth scrolling
        });
    }, [pathname]); // Re-run effect when pathname changes

    // This component doesn't render anything
    return null;
}