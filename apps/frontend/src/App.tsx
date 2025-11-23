import { Routes, Route } from 'react-router-dom';
import { Navbar } from './components/layout/navbar.tsx';
import { Footer } from './components/layout/footer.tsx';
import { ProtectedRoute } from './components/auth/protected-route.tsx';
import { ScrollToTop } from './components/layout/scroll-to-top.tsx';
import {Toaster} from "./components/ui/sonner.tsx";
import { StructuredData } from './components/seo/structured-data';

import HomePage from './pages/home-page';
import LoginPage from './pages/login-page';
import RegisterPage from './pages/register-page';
import ResourcesPage from './pages/resources-page.tsx';
import ResourceDetailPage from './pages/resource-detail-page.tsx';
import DashboardPage from './pages/dashboard.tsx';
import ProfilePage from './pages/profile.tsx';
import ProfileEditPage from "@/pages/profile-edit-page.tsx";
import AdminDashboard from "@/pages/admin/admin-dashboard.tsx";
import {AdminRoute} from "@/components/auth/admin-route.tsx";
import AdminUsers from "@/pages/admin/admin-users.tsx";
import AdminUserDetail from "@/pages/admin/admin-user-detail.tsx";

const organizationSchema = {
    '@context': 'https://schema.org',
    '@type': 'Organization',
    name: 'VetConnect',
    description: 'Comprehensive platform connecting U.S. military veterans with essential resources including housing, healthcare, education, and mental health services.',
    url: 'https://vetconnect.com',
    logo: 'https://vetconnect.com/logo.png',
    sameAs: [
        'https://github.com/cishocksr/vetconnect',
    ],
    contactPoint: {
        '@type': 'ContactPoint',
        contactType: 'Customer Service',
        email: 'support@vetconnect.com',
        availableLanguage: 'English',
    },
};

const websiteSchema = {
    '@context': 'https://schema.org',
    '@type': 'WebSite',
    name: 'VetConnect',
    url: 'https://vetconnect.com',
    potentialAction: {
        '@type': 'SearchAction',
        target: 'https://vetconnect.com/resources?search={search_term_string}',
        'query-input': 'required name=search_term_string',
    },
};

function App() {
    return (
        <div className="flex flex-col min-h-screen">
            <ScrollToTop />
            <StructuredData data={organizationSchema} />
            <StructuredData data={websiteSchema} />

            <Navbar />
            <main className="flex-1">
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />
                    <Route path="/resources" element={<ResourcesPage />} />
                    <Route path="/resources/:id" element={<ResourceDetailPage />} />
                    <Route path="/profile/edit" element={<ProtectedRoute><ProfileEditPage /></ProtectedRoute>} />



                    {/* Protected Routes */}
                    <Route
                        path="/dashboard"
                        element={
                            <ProtectedRoute>
                                <DashboardPage />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/profile"
                        element={
                            <ProtectedRoute>
                                <ProfilePage />
                            </ProtectedRoute>
                        }
                    />
                    {/* Admin Routes */}
                    <Route
                        path="/admin"
                        element={
                            <AdminRoute>
                                <AdminDashboard />
                            </AdminRoute>
                        }
                    />
                    <Route
                        path="/admin/users"
                        element={
                            <AdminRoute>
                                <AdminUsers />
                            </AdminRoute>
                        }
                    />
                    <Route
                        path="/admin/users/:userId"
                        element={
                            <AdminRoute>
                                <AdminUserDetail />
                            </AdminRoute>
                        }
                    />
                </Routes>
            </main>
            <Footer />
            <Toaster />
        </div>
    );
}

export default App;