import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Button } from '../ui/button';
import { Sheet, SheetContent, SheetTrigger } from '../ui/sheet';
import { Menu, Home, Search, BookmarkCheck, User } from 'lucide-react';

export function MobileMenu() {
    const [open, setOpen] = useState(false);

    const menuItems = [
        { icon: Home, label: 'Home', href: '/' },
        { icon: Search, label: 'Resources', href: '/resources' },
        { icon: BookmarkCheck, label: 'Dashboard', href: '/dashboard' },
        { icon: User, label: 'Profile', href: '/profile' },
    ];

    return (
        <Sheet open={open} onOpenChange={setOpen}>
            <SheetTrigger asChild>
                <Button variant="ghost" size="icon" className="md:hidden text-white">
                    <Menu className="h-6 w-6" />
                </Button>
            </SheetTrigger>
            <SheetContent side="left" className="w-64">
                <nav className="flex flex-col gap-4 mt-8">
                    {menuItems.map((item) => (
                        <Link
                            key={item.href}
                            to={item.href}
                            onClick={() => setOpen(false)}
                            className="flex items-center gap-3 px-4 py-3 rounded-lg hover:bg-accent transition-colors"
                        >
                            <item.icon className="h-5 w-5" />
                            <span className="font-medium">{item.label}</span>
                        </Link>
                    ))}
                </nav>
            </SheetContent>
        </Sheet>
    );
}