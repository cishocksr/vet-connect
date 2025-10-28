import { Shield } from 'lucide-react';
import { Link } from 'react-router-dom';

export function Footer() {
    const currentYear = new Date().getFullYear();

    return (
        <footer className="border-t bg-gray-50 mt-auto">
            <div className="container mx-auto px-4 py-8">
                <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
                    {/* Brand */}
                    <div className="col-span-1 md:col-span-2">
                        <Link to="/" className="flex items-center gap-2 font-bold text-xl mb-4">
                            <Shield className="h-6 w-6 text-military-navy" />
                            <span className="bg-gradient-to-r from-military-navy to-military-green bg-clip-text text-transparent">
                VetConnect
              </span>
                        </Link>
                        <p className="text-sm text-gray-600 mb-4">
                            Connecting veterans with essential resources for housing, healthcare,
                            education, and support services across the nation.
                        </p>
                        <p className="text-xs text-gray-500">
                            Built with ❤️ for those who served
                        </p>
                    </div>

                    {/* Quick Links */}
                    <div>
                        <h3 className="font-semibold mb-4">Quick Links</h3>
                        <ul className="space-y-2 text-sm">
                            <li>
                                <Link to="/" className="text-gray-600 hover:text-primary-600">
                                    Home
                                </Link>
                            </li>
                            <li>
                                <Link to="/resources" className="text-gray-600 hover:text-primary-600">
                                    Browse Resources
                                </Link>
                            </li>
                            <li>
                                <Link to="/dashboard" className="text-gray-600 hover:text-primary-600">
                                    Dashboard
                                </Link>
                            </li>
                        </ul>
                    </div>

                    {/* Resources */}
                    <div>
                        <h3 className="font-semibold mb-4">Categories</h3>
                        <ul className="space-y-2 text-sm">
                            <li className="text-gray-600">Housing</li>
                            <li className="text-gray-600">Financial Aid</li>
                            <li className="text-gray-600">Education</li>
                            <li className="text-gray-600">Mental Health</li>
                            <li className="text-gray-600">Healthcare</li>
                        </ul>
                    </div>
                </div>

                <div className="border-t mt-8 pt-8 text-center text-sm text-gray-600">
                    <p>&copy; {currentYear} VetConnect. All rights reserved.</p>
                    <p className="mt-2 text-xs">
                        This is a learning project created to benefit veterans. 🎖️
                    </p>
                </div>
            </div>
        </footer>
    );
}