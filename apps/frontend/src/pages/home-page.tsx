import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { Button } from '../components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Badge } from '../components/ui/badge';
import { Skeleton } from '../components/ui/skeleton';
import resourceService from '@/services/resource-service';
import { useAuth } from '@/hooks/use-auth';
import { MetaTags } from '@/components/seo/meta-tags';
import {
    Shield,
    Home,
    DollarSign,
    GraduationCap,
    Brain,
    Heart,
    ArrowRight,
    Target,
    Users,
    Award,
    BookmarkCheck
} from 'lucide-react';

import type { LucideIcon } from 'lucide-react';

const CATEGORY_ICONS: Record<string, LucideIcon> = {
    'home': Home,
    'dollar-sign': DollarSign,
    'graduation-cap': GraduationCap,
    'brain': Brain,
    'heart-pulse': Heart,
};

const CATEGORY_COLORS: Record<string, string> = {
    'Housing': 'from-blue-500 to-blue-600',
    'Financial': 'from-green-500 to-green-600',
    'Education': 'from-purple-500 to-purple-600',
    'Mental Health': 'from-pink-500 to-pink-600',
    'Healthcare': 'from-red-500 to-red-600',
};

export default function HomePage() {
    const { isAuthenticated, user } = useAuth();

    // Fetch categories with counts
    const { data: categories, isLoading } = useQuery({
        queryKey: ['categories', 'with-counts'],
        queryFn: () => resourceService.getCategoriesWithCounts(),
    });

    return (
        <div className="min-h-screen">
            <MetaTags 
                title="Home"
                description="VetConnect - Your comprehensive resource hub for housing, healthcare, education, and support services. Built for veterans, by veterans."
            />
            {/* Hero Section */}
            <section className="relative bg-gradient-to-br from-military-navy via-military-air-force-blue to-military-space-force-blue text-white py-20 overflow-hidden">
                {/* Animated background pattern */}
                <div className="absolute inset-0 opacity-10">
                    <div className="absolute top-10 left-10 w-64 h-64 bg-military-gold rounded-full blur-3xl"></div>
                    <div className="absolute bottom-10 right-10 w-96 h-96 bg-military-coast-guard-orange rounded-full blur-3xl"></div>
                </div>

                <div className="container mx-auto px-4 relative z-10">
                    <motion.div
                        initial={{ opacity: 0, y: 30 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.8 }}
                        className="max-w-4xl mx-auto text-center"
                    >
                        <motion.div
                            initial={{ scale: 0 }}
                            animate={{ scale: 1 }}
                            transition={{ delay: 0.2, type: "spring", stiffness: 200 }}
                            className="flex justify-center mb-6"
                        >
                            <div className="p-4 bg-white/10 backdrop-blur-sm rounded-full">
                                <Shield className="h-16 w-16 text-military-gold" />
                            </div>
                        </motion.div>

                        <motion.h1
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            transition={{ delay: 0.4 }}
                            className="text-5xl md:text-6xl font-bold mb-6 bg-clip-text text-transparent bg-gradient-to-r from-white via-military-gold to-white"
                        >
                            Welcome to VetConnect
                        </motion.h1>

                        <motion.p
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            transition={{ delay: 0.6 }}
                            className="text-xl mb-8 text-white/90 leading-relaxed"
                        >
                            Your comprehensive resource hub for housing, healthcare, education,
                            and support services. Built for veterans, by veterans.
                        </motion.p>

                        {isAuthenticated && user ? (
                            <motion.div
                                initial={{ opacity: 0, y: 20 }}
                                animate={{ opacity: 1, y: 0 }}
                                transition={{ delay: 0.8 }}
                                className="space-y-4"
                            >
                                <p className="text-lg">
                                    Welcome back, <span className="font-semibold text-military-gold">{user.firstName}</span>! 🎖️
                                </p>
                                <div className="flex flex-wrap gap-4 justify-center">
                                    <Button
                                        asChild
                                        size="lg"
                                        className="border-2 border-white bg-transparent text-white hover:bg-white hover:text-military-navy font-semibold !text-white shadow-lg"
                                    >
                                        <Link to="/resources">
                                            Browse Resources
                                        </Link>
                                    </Button>
                                    <Button
                                        asChild
                                        size="lg"
                                        variant="outline"
                                        className="border-2 border-white bg-transparent text-white hover:bg-white hover:text-military-navy font-semibold"
                                    >
                                        <Link to="/dashboard">
                                            <BookmarkCheck className="mr-2 h-5 w-5" />
                                            View Dashboard
                                        </Link>
                                    </Button>
                                </div>
                            </motion.div>
                        ) : (
                            <motion.div
                                initial={{ opacity: 0, y: 20 }}
                                animate={{ opacity: 1, y: 0 }}
                                transition={{ delay: 0.8 }}
                                className="flex flex-wrap gap-4 justify-center"
                            >
                                <Button
                                    asChild
                                    size="lg"
                                    className="bg-military-gold hover:bg-military-army-gold text-military-navy font-semibold shadow-lg hover:shadow-xl transition-all"
                                >
                                    <Link to="/register">
                                        Get Started
                                        <ArrowRight className="ml-2 h-5 w-5" />
                                    </Link>
                                </Button>
                                <Button
                                    asChild
                                    size="lg"
                                    className="border-2 border-white bg-transparent !text-white hover:bg-white hover:!text-military-navy font-semibold shadow-lg"
                                >
                                    <Link to="/resources">
                                        Browse Resources
                                    </Link>
                                </Button>
                            </motion.div>
                        )}
                    </motion.div>
                </div>
            </section>

            {/* Stats Section */}
            <section className="py-12 bg-white border-b">
                <div className="container mx-auto px-4">
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-4xl mx-auto">
                        {[
                            { icon: Target, label: 'Resources', value: categories?.reduce((sum, cat) => sum + cat.resourceCount, 0) || 0, color: 'text-military-marine-scarlet' },
                            { icon: Users, label: 'Veterans Served', value: '1,000+', color: 'text-military-navy' },
                            { icon: Award, label: 'Categories', value: categories?.length || 0, color: 'text-military-army-green' },
                        ].map((stat, index) => (
                            <motion.div
                                key={stat.label}
                                initial={{ opacity: 0, y: 20 }}
                                whileInView={{ opacity: 1, y: 0 }}
                                viewport={{ once: true }}
                                transition={{ delay: index * 0.1 }}
                                className="text-center"
                            >
                                <stat.icon className={`h-12 w-12 mx-auto mb-3 ${stat.color}`} />
                                <p className="text-3xl font-bold text-gray-900 mb-1">{stat.value}</p>
                                <p className="text-gray-600">{stat.label}</p>
                            </motion.div>
                        ))}
                    </div>
                </div>
            </section>

            {/* Categories Section */}
            <section className="py-16 bg-gradient-to-br from-gray-50 to-blue-50">
                <div className="container mx-auto px-4">
                    <motion.div
                        initial={{ opacity: 0, y: 20 }}
                        whileInView={{ opacity: 1, y: 0 }}
                        viewport={{ once: true }}
                        className="text-center mb-12"
                    >
                        <h2 className="text-4xl font-bold mb-4 bg-clip-text text-transparent bg-gradient-to-r from-military-navy to-military-air-force-blue">
                            Explore Resources by Category
                        </h2>
                        <p className="text-gray-600 max-w-2xl mx-auto text-lg">
                            Find the assistance you need across various categories of veteran services
                        </p>
                    </motion.div>

                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-6xl mx-auto">
                        {isLoading ? (
                            // Loading skeletons
                            Array.from({ length: 5 }).map((_, i) => (
                                <Card key={i}>
                                    <CardHeader>
                                        <Skeleton className="h-10 w-10 rounded-full mb-2" />
                                        <Skeleton className="h-6 w-32" />
                                    </CardHeader>
                                    <CardContent>
                                        <Skeleton className="h-4 w-full mb-2" />
                                        <Skeleton className="h-4 w-24" />
                                    </CardContent>
                                </Card>
                            ))
                        ) : (
                            categories?.map((category, index) => {
                                const Icon = CATEGORY_ICONS[category.iconName] || Shield;
                                const gradient = CATEGORY_COLORS[category.name] || 'from-gray-500 to-gray-600';

                                return (
                                    <motion.div
                                        key={category.id}
                                        initial={{ opacity: 0, y: 20 }}
                                        whileInView={{ opacity: 1, y: 0 }}
                                        viewport={{ once: true }}
                                        transition={{ delay: index * 0.1 }}
                                        whileHover={{ y: -8, transition: { duration: 0.2 } }}
                                    >
                                        <Link
                                            to={`/resources?category=${category.id}`}
                                            className="block group h-full"
                                        >
                                            <Card className="h-full transition-all hover:shadow-2xl border-2 hover:border-military-gold">
                                                <CardHeader>
                                                    <div className="flex items-center justify-between mb-3">
                                                        <motion.div
                                                            whileHover={{ rotate: 360 }}
                                                            transition={{ duration: 0.6 }}
                                                            className={`p-3 bg-gradient-to-br ${gradient} rounded-xl shadow-lg`}
                                                        >
                                                            <Icon className="h-6 w-6 text-white" />
                                                        </motion.div>
                                                        <Badge variant="secondary" className="font-semibold">
                                                            {category.resourceCount}
                                                        </Badge>
                                                    </div>
                                                    <CardTitle className="group-hover:text-military-navy transition-colors text-xl">
                                                        {category.name}
                                                    </CardTitle>
                                                </CardHeader>
                                                <CardContent>
                                                    <CardDescription className="line-clamp-2 text-base">
                                                        {category.description}
                                                    </CardDescription>
                                                    <div className="flex items-center text-military-gold font-medium mt-4 group-hover:gap-2 transition-all">
                                                        <span>Explore</span>
                                                        <ArrowRight className="h-4 w-4 group-hover:translate-x-1 transition-transform" />
                                                    </div>
                                                </CardContent>
                                            </Card>
                                        </Link>
                                    </motion.div>
                                );
                            })
                        )}
                    </div>
                </div>
            </section>

            {/* CTA Section */}
            <section className="py-20 bg-gradient-to-r from-military-navy to-military-air-force-blue text-white">
                <div className="container mx-auto px-4">
                    <motion.div
                        initial={{ opacity: 0, y: 20 }}
                        whileInView={{ opacity: 1, y: 0 }}
                        viewport={{ once: true }}
                        className="max-w-3xl mx-auto text-center"
                    >
                        <h2 className="text-4xl font-bold mb-4">
                            Ready to Get Started?
                        </h2>
                        <p className="text-xl mb-8 text-white/90">
                            Join thousands of veterans who have found the resources they need
                            through VetConnect. Create your free account today.
                        </p>
                        {!isAuthenticated && (
                            <Button
                                asChild
                                size="lg"
                                className="bg-military-gold hover:bg-military-army-gold text-military-navy font-semibold text-lg px-8 py-6 shadow-xl hover:shadow-2xl transition-all"
                            >
                                <Link to="/register">
                                    Create Free Account
                                    <ArrowRight className="ml-2 h-5 w-5" />
                                </Link>
                            </Button>
                        )}
                    </motion.div>
                </div>
            </section>
        </div>
    );
}