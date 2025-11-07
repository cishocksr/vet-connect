import { useState, } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useSearchParams, Link } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import { Badge } from '../components/ui/badge';
import { Skeleton } from '../components/ui/skeleton';
import resourceService from '../services/resource-service';
import type { ResourceSearchParams } from '../types';
import {
    Search,
    MapPin,
    Globe,
    ChevronLeft,
    ChevronRight,
    Filter
} from 'lucide-react';

const US_STATES = [
    'AL', 'AK', 'AZ', 'AR', 'CA', 'CO', 'CT', 'DE', 'FL', 'GA',
    'HI', 'ID', 'IL', 'IN', 'IA', 'KS', 'KY', 'LA', 'ME', 'MD',
    'MA', 'MI', 'MN', 'MS', 'MO', 'MT', 'NE', 'NV', 'NH', 'NJ',
    'NM', 'NY', 'NC', 'ND', 'OH', 'OK', 'OR', 'PA', 'RI', 'SC',
    'SD', 'TN', 'TX', 'UT', 'VT', 'VA', 'WA', 'WV', 'WI', 'WY',
];

export default function ResourcesPage() {
    const [searchParams, setSearchParams] = useSearchParams();

    // Get params from URL
    const categoryIdParam = searchParams.get('category');
    const stateParam = searchParams.get('state');
    const keywordParam = searchParams.get('keyword');
    const pageParam = searchParams.get('page');

    // Local state
    const [keyword, setKeyword] = useState(keywordParam || '');
    const [selectedCategory, setSelectedCategory] = useState(categoryIdParam || '');
    const [selectedState, setSelectedState] = useState(stateParam || '');
    const [currentPage, setCurrentPage] = useState(Number(pageParam) || 0);

    // Fetch categories
    const { data: categories } = useQuery({
        queryKey: ['categories'],
        queryFn: () => resourceService.getAllCategories(),
    });

    // Build search params
    const searchParamsObj: ResourceSearchParams = {
        page: currentPage,
        size: 12,
    };

    if (keyword) searchParamsObj.keyword = keyword;
    if (selectedCategory) searchParamsObj.categoryId = Number(selectedCategory);
    if (selectedState) searchParamsObj.state = selectedState;

    // Fetch resources
    const { data: resourcesData, isLoading } = useQuery({
        queryKey: ['resources', 'search', searchParamsObj],
        queryFn: () => resourceService.searchResources(searchParamsObj),
    });

    // Handle search
    const handleSearch = () => {
        const newParams: Record<string, string> = {};
        if (keyword) newParams.keyword = keyword;
        if (selectedCategory) newParams.category = selectedCategory;
        if (selectedState) newParams.state = selectedState;
        newParams.page = '0';
        setSearchParams(newParams);
        setCurrentPage(0);
    };

    // Handle clear filters
    const handleClearFilters = () => {
        setKeyword('');
        setSelectedCategory('');
        setSelectedState('');
        setCurrentPage(0);
        setSearchParams({});
    };

    // Handle page change
    const handlePageChange = (newPage: number) => {
        setCurrentPage(newPage);
        const newParams: Record<string, string> = {};
        if (keyword) newParams.keyword = keyword;
        if (selectedCategory) newParams.category = selectedCategory;
        if (selectedState) newParams.state = selectedState;
        newParams.page = String(newPage);
        setSearchParams(newParams);
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const hasFilters = keyword || selectedCategory || selectedState;

    return (
        <div className="min-h-screen bg-gray-50 py-8">
            <div className="container mx-auto px-4">
                {/* Header */}
                <div className="mb-8">
                    <h1 className="text-3xl font-bold mb-2">Veteran Resources</h1>
                    <p className="text-gray-600">
                        Search and filter through available resources to find the assistance you need
                    </p>
                </div>

                {/* Search & Filters */}
                <Card className="mb-8">
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <Filter className="h-5 w-5" />
                            Search & Filter
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        {/* Keyword Search */}
                        <div className="space-y-2">
                            <label className="text-sm font-medium">Search Keywords</label>
                            <div className="flex gap-2">
                                <div className="relative flex-1">
                                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                                    <Input
                                        placeholder="Search resources by name or description..."
                                        value={keyword}
                                        onChange={(e) => setKeyword(e.target.value)}
                                        onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                                        className="pl-10"
                                    />
                                </div>
                                <Button onClick={handleSearch} className="bg-military-navy">
                                    Search
                                </Button>
                            </div>
                        </div>

                        {/* Filters Row */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            {/* Category Filter */}
                            <div className="space-y-2">
                                <label className="text-sm font-medium">Category</label>
                                <Select value={selectedCategory || 'all'} onValueChange={(value) => setSelectedCategory(value === 'all' ? '' : value)}>
                                    <SelectTrigger>
                                        <SelectValue placeholder="All categories" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="all">All categories</SelectItem>
                                        {categories?.map((cat) => (
                                            <SelectItem key={cat.id} value={String(cat.id)}>
                                                {cat.name}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>

                            {/* State Filter */}
                            <div className="space-y-2">
                                <label className="text-sm font-medium">State</label>
                                <Select value={selectedState || 'all'} onValueChange={(value) => setSelectedState(value === 'all' ? '' : value)}>
                                    <SelectTrigger>
                                        <SelectValue placeholder="All states" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="all">All states</SelectItem>
                                        {US_STATES.map((state) => (
                                            <SelectItem key={state} value={state}>
                                                {state}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>

                        {/* Clear Filters */}
                        {hasFilters && (
                            <Button
                                variant="outline"
                                onClick={handleClearFilters}
                                className="w-full md:w-auto"
                            >
                                Clear All Filters
                            </Button>
                        )}
                    </CardContent>
                </Card>

                {/* Results Count */}
                {resourcesData && (
                    <div className="mb-6">
                        <p className="text-gray-600">
                            Showing <span className="font-semibold">{resourcesData.content.length}</span> of{' '}
                            <span className="font-semibold">{resourcesData.totalElements}</span> resources
                        </p>
                    </div>
                )}

                {/* Resources Grid */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
                    {isLoading ? (
                        // Loading skeletons
                        Array.from({ length: 6 }).map((_, i) => (
                            <Card key={i}>
                                <CardHeader>
                                    <Skeleton className="h-6 w-3/4 mb-2" />
                                    <Skeleton className="h-4 w-1/2" />
                                </CardHeader>
                                <CardContent>
                                    <Skeleton className="h-4 w-full mb-2" />
                                    <Skeleton className="h-4 w-full mb-2" />
                                    <Skeleton className="h-4 w-2/3" />
                                </CardContent>
                            </Card>
                        ))
                    ) : resourcesData && resourcesData.content.length > 0 ? (
                        resourcesData.content.map((resource) => (
                            <Link key={resource.id} to={`/resources/${resource.id}`}>
                                <Card className="h-full transition-all hover:shadow-lg hover:border-primary-500 cursor-pointer">
                                    <CardHeader>
                                        <div className="flex items-start justify-between gap-2 mb-2">
                                            <Badge variant="secondary" className="text-xs">
                                                {resource.categoryName}
                                            </Badge>
                                            {resource.isNational ? (
                                                <Badge className="bg-military-gold text-white text-xs">
                                                    <Globe className="h-3 w-3 mr-1" />
                                                    National
                                                </Badge>
                                            ) : resource.state && (
                                                <Badge variant="outline" className="text-xs">
                                                    <MapPin className="h-3 w-3 mr-1" />
                                                    {resource.state}
                                                </Badge>
                                            )}
                                        </div>
                                        <CardTitle className="line-clamp-2 text-lg">
                                            {resource.name}
                                        </CardTitle>
                                    </CardHeader>
                                    <CardContent>
                                        <CardDescription className="line-clamp-3">
                                            {resource.shortDescription}
                                        </CardDescription>
                                        {resource.city && resource.state && (
                                            <p className="text-sm text-gray-500 mt-3 flex items-center gap-1">
                                                <MapPin className="h-3 w-3" />
                                                {resource.city}, {resource.state}
                                            </p>
                                        )}
                                    </CardContent>
                                </Card>
                            </Link>
                        ))
                    ) : (
                        // No results
                        <div className="col-span-full text-center py-12">
                            <div className="max-w-md mx-auto">
                                <Search className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                                <h3 className="text-lg font-semibold mb-2">No resources found</h3>
                                <p className="text-gray-600 mb-4">
                                    Try adjusting your filters or search terms
                                </p>
                                {hasFilters && (
                                    <Button variant="outline" onClick={handleClearFilters}>
                                        Clear Filters
                                    </Button>
                                )}
                            </div>
                        </div>
                    )}
                </div>

                {/* Pagination */}
                {resourcesData && resourcesData.totalPages > 1 && (
                    <div className="flex items-center justify-center gap-2">
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handlePageChange(currentPage - 1)}
                            disabled={!resourcesData.hasPrevious}
                        >
                            <ChevronLeft className="h-4 w-4 mr-1" />
                            Previous
                        </Button>

                        <div className="flex items-center gap-2">
                            {Array.from({ length: resourcesData.totalPages }, (_, i) => (
                                <Button
                                    key={i}
                                    variant={currentPage === i ? 'default' : 'outline'}
                                    size="sm"
                                    onClick={() => handlePageChange(i)}
                                    className={currentPage === i ? 'bg-military-navy' : ''}
                                >
                                    {i + 1}
                                </Button>
                            )).slice(
                                Math.max(0, currentPage - 2),
                                Math.min(resourcesData.totalPages, currentPage + 3)
                            )}
                        </div>

                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handlePageChange(currentPage + 1)}
                            disabled={!resourcesData.hasNext}
                        >
                            Next
                            <ChevronRight className="h-4 w-4 ml-1" />
                        </Button>
                    </div>
                )}
            </div>
        </div>
    );
}