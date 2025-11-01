import { Routes, Route } from 'react-router-dom';
import { Navbar } from './components/layout/navbar.tsx';
import { Footer } from './components/layout/footer.tsx';
import { ProtectedRoute } from './components/auth/protected-route.tsx';
import { ScrollToTop } from './components/layout/scroll-to-top.tsx';

// Pages (we'll create these next)
import HomePage from './pages/home-page';
import LoginPage from './pages/login-page';
import RegisterPage from './pages/register-page';
import ResourcesPage from './pages/resources-page.tsx';
import ResourceDetailPage from './pages/resource-detail-page.tsx';
import DashboardPage from './pages/dashboard.tsx';
import ProfilePage from './pages/profile.tsx';
import ProfileEditPage from "@/pages/profile-edit-page.tsx";

function App() {
    return (
        <div className="flex flex-col min-h-screen">
            <ScrollToTop />

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
                </Routes>
            </main>
            <Footer />
        </div>
    );
}

export default App;