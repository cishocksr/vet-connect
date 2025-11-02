import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Button } from '../ui/button';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from '../ui/dropdown-menu';
import {Avatar, AvatarFallback, AvatarImage} from '../ui/avatar';
import { useAuth } from '@/hooks/use-auth';
import { getInitials } from '@/lib/utils';
import {
    Home,
    Search,
    BookmarkCheck,
    User,
    LogOut,
    Menu,
    Shield
} from 'lucide-react';

export function Navbar() {
    const { user, isAuthenticated, logout } = useAuth();
    const navigate = useNavigate();

    return (
        <motion.nav
            initial={{ y: -100 }}
            animate={{ y: 0 }}
            transition={{ duration: 0.3 }}
            className="sticky top-0 z-50 w-full border-b bg-gradient-to-r from-military-navy via-military-air-force-blue to-military-navy shadow-lg"
        >
            <div className="container mx-auto px-4">
                <div className="flex h-16 items-center justify-between">
                    {/* Logo */}
                    <Link to="/" className="flex items-center gap-2 group">
                        <motion.div
                            whileHover={{ rotate: 360, scale: 1.1 }}
                            transition={{ duration: 0.6 }}
                        >
                            <Shield className="h-7 w-7 text-military-gold" />
                        </motion.div>
                        <span className="font-bold text-xl text-white group-hover:text-military-gold transition-colors">
              VetConnect
            </span>
                    </Link>

                    {/* Desktop Navigation */}
                    <div className="hidden md:flex items-center gap-6">
                        <Link
                            to="/"
                            className="flex items-center gap-2 text-sm font-medium text-white hover:text-military-gold transition-colors"
                        >
                            <Home className="h-4 w-4" />
                            Home
                        </Link>
                        <Link
                            to="/resources"
                            className="flex items-center gap-2 text-sm font-medium text-white hover:text-military-gold transition-colors"
                        >
                            <Search className="h-4 w-4" />
                            Resources
                        </Link>
                        {isAuthenticated && (
                            <Link
                                to="/dashboard"
                                className="flex items-center gap-2 text-sm font-medium text-white hover:text-military-gold transition-colors"
                            >
                                <BookmarkCheck className="h-4 w-4" />
                                Dashboard
                            </Link>
                        )}
                    </div>

                    {/* Auth Buttons / User Menu */}
                    <div className="flex items-center gap-4">
                        {isAuthenticated && user ? (
                            <DropdownMenu>
                                <DropdownMenuTrigger asChild>
                                    <Button variant="ghost" className="relative h-10 w-10 rounded-full hover:bg-white/10">
                                        <Avatar>
                                            {user.profilePictureUrl && (
                                                <AvatarImage
                                                    src={`http://localhost:8080${user.profilePictureUrl}`}
                                                    alt={user.fullName}
                                                />
                                            )}
                                            <AvatarFallback className="bg-gradient-to-br from-military-gold to-military-army-gold text-military-navy font-bold">
                                                {getInitials(user.firstName, user.lastName)}
                                            </AvatarFallback>
                                        </Avatar>
                                    </Button>
                                </DropdownMenuTrigger>
                                <DropdownMenuContent
                                    align="end"
                                    className="w-56 z-[100] bg-white dark:bg-gray-800 shadow-lg border border-gray-200"
                                >
                                    <DropdownMenuLabel>
                                        <div className="flex flex-col space-y-1">
                                            <p className="text-sm font-medium text-gray-900">{user.fullName}</p>
                                            <p className="text-xs text-gray-500">{user.email}</p>
                                            <p className="text-xs text-military-green font-medium">
                                                {user.branchDisplayName}
                                            </p>
                                        </div>
                                    </DropdownMenuLabel>
                                    <DropdownMenuSeparator />
                                    <DropdownMenuItem onClick={() => navigate('/dashboard')}>
                                        <BookmarkCheck className="mr-2 h-4 w-4" />
                                        Dashboard
                                    </DropdownMenuItem>
                                    <DropdownMenuItem onClick={() => navigate('/profile')}>
                                        <User className="mr-2 h-4 w-4" />
                                        Profile
                                    </DropdownMenuItem>
                                    <DropdownMenuSeparator />
                                    <DropdownMenuItem onClick={logout} variant="destructive">
                                        <LogOut className="mr-2 h-4 w-4" />
                                        Logout
                                    </DropdownMenuItem>
                                </DropdownMenuContent>
                            </DropdownMenu>
                        ) : (
                            <>
                                <Button
                                    variant="ghost"
                                    onClick={() => navigate('/login')}
                                    className="hidden md:inline-flex text-white hover:text-military-gold hover:bg-white/10"
                                >
                                    Login
                                </Button>
                                <Button
                                    onClick={() => navigate('/register')}
                                    className="bg-military-gold hover:bg-military-army-gold text-military-navy font-semibold"
                                >
                                    Sign Up
                                </Button>
                            </>
                        )}

                        {/* Mobile Menu */}
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild className="md:hidden">
                                <Button variant="ghost" size="icon" className="text-white hover:bg-white/10">
                                    <Menu className="h-5 w-5" />
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end" className="w-48">
                                <DropdownMenuItem onClick={() => navigate('/')}>
                                    <Home className="mr-2 h-4 w-4" />
                                    Home
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => navigate('/resources')}>
                                    <Search className="mr-2 h-4 w-4" />
                                    Resources
                                </DropdownMenuItem>
                                {isAuthenticated && (
                                    <DropdownMenuItem onClick={() => navigate('/dashboard')}>
                                        <BookmarkCheck className="mr-2 h-4 w-4" />
                                        Dashboard
                                    </DropdownMenuItem>
                                )}
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </div>
                </div>
            </div>
        </motion.nav>
    );
}