import { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { Button } from '../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card';
import { Badge } from '../components/ui/badge';
import { Skeleton } from '../components/ui/skeleton';
import { Textarea } from '../components/ui/textarea';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '../components/ui/dialog';
import { useAuth } from '../hooks/use-auth.ts';
import resourceService from '../services/resource-service.ts';
import savedResourceService from '../services/saved-resource-service.ts';
import { formatPhoneNumber } from '../lib/utils';
import { toast } from '../hooks/use-toast';
import { AuthDebug } from '../components/debug/auth-debug';
import {
    MapPin,
    Globe,
    Phone,
    Mail,
    ExternalLink,
    BookmarkPlus,
    BookmarkCheck,
    ArrowLeft,
    Building,
    Info,
    CheckCircle2,
    Loader2
} from 'lucide-react';

export default function ResourceDetailPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const { isAuthenticated } = useAuth();
    const [notes, setNotes] = useState('');
    const [isDialogOpen, setIsDialogOpen] = useState(false);

    // Fetch resource details
    const { data: resource, isLoading } = useQuery({
        queryKey: ['resource', id],
        queryFn: () => resourceService.getResourceById(id!),
        enabled: !!id,
    });

    // Check if resource is saved
    const { data: isSaved } = useQuery({
        queryKey: ['saved', 'check', id],
        queryFn: () => savedResourceService.isResourceSaved(id!),
        enabled: !!id && isAuthenticated,
    });

    // Save resource mutation
    const saveMutation = useMutation({
        mutationFn: (data: { resourceId: string; notes?: string }) =>
            savedResourceService.saveResource(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['saved'] });
            queryClient.invalidateQueries({ queryKey: ['saved', 'check', id] });
            queryClient.invalidateQueries({ queryKey: ['saved', 'resources'] });
            setIsDialogOpen(false);
            setNotes('');
            toast.success('Resource saved to dashboard!');
        },
        onError: (error: AxiosError<{ message?: string }>) => {
            console.error('Save error:', error);
            console.error('Error response:', error?.response);
            console.error('Error status:', error?.response?.status);
            console.error('Error data:', error?.response?.data);
            
            let errorMessage = 'Failed to save resource. Please try again.';
            
            if (error?.response?.status === 403) {
                errorMessage = 'Access denied. Please log out and log back in.';
                console.error('403 Forbidden - Token may be invalid or expired');
                console.error('Token in localStorage:', localStorage.getItem('token')?.substring(0, 20) + '...');
            } else if (error?.response?.data?.message) {
                errorMessage = error.response.data.message;
            }
            
            toast.error(errorMessage);
        },
    });

    const handleSave = () => {
        if (!id) return;

        if (!isAuthenticated) {
            navigate('/login', { state: { from: `/resources/${id}` } });
            return;
        }

        console.log('Saving resource:', { resourceId: id, notes: notes || undefined });
        saveMutation.mutate({ resourceId: id, notes: notes || undefined });
    };

    if (isLoading) {
        return (
            <div className="min-h-screen bg-gray-50 py-8">
                <div className="container mx-auto px-4 max-w-4xl">
                    <Skeleton className="h-10 w-32 mb-6" />
                    <Card>
                        <CardHeader>
                            <Skeleton className="h-8 w-3/4 mb-4" />
                            <Skeleton className="h-6 w-1/4" />
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <Skeleton className="h-4 w-full" />
                            <Skeleton className="h-4 w-full" />
                            <Skeleton className="h-4 w-2/3" />
                        </CardContent>
                    </Card>
                </div>
            </div>
        );
    }

    if (!resource) {
        return (
            <div className="min-h-screen bg-gray-50 py-8">
                <div className="container mx-auto px-4 max-w-4xl text-center">
                    <h1 className="text-2xl font-bold mb-4">Resource Not Found</h1>
                    <Button onClick={() => navigate('/resources')}>
                        Back to Resources
                    </Button>
                </div>
            </div>
        );
    }

    return (
        <>
        <AuthDebug />
        <div className="min-h-screen bg-gray-50 py-8">
            <div className="container mx-auto px-4 max-w-4xl">
                {/* Back Button */}
                <Button
                    variant="ghost"
                    onClick={() => navigate(-1)}
                    className="mb-6"
                >
                    <ArrowLeft className="h-4 w-4 mr-2" />
                    Back
                </Button>

                {/* Main Card */}
                <Card>
                    <CardHeader>
                        <div className="flex items-start justify-between gap-4">
                            <div className="flex-1">
                                <div className="flex flex-wrap gap-2 mb-3">
                                    <Badge variant="secondary">
                                        {resource.category.name}
                                    </Badge>
                                    {resource.isNational ? (
                                        <Badge className="bg-military-gold text-white">
                                            <Globe className="h-3 w-3 mr-1" />
                                            National Resource
                                        </Badge>
                                    ) : resource.state && (
                                        <Badge variant="outline">
                                            <MapPin className="h-3 w-3 mr-1" />
                                            {resource.state}
                                        </Badge>
                                    )}
                                </div>
                                <CardTitle className="text-2xl md:text-3xl mb-2">
                                    {resource.name}
                                </CardTitle>
                            </div>

                            {/* Save Button */}
                            {isSaved ? (
                                <Button
                                    variant="outline"
                                    className="border-green-500 text-green-600 hover:bg-green-50"
                                    disabled
                                >
                                    <BookmarkCheck className="h-4 w-4 mr-2" />
                                    Saved
                                </Button>
                            ) : (
                                <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                                    <DialogTrigger asChild>
                                        <Button className="bg-military-navy text-white hover:bg-military-navy/90 hover:text-white">
                                            <BookmarkPlus className="h-4 w-4 mr-2" />
                                            Save
                                        </Button>
                                    </DialogTrigger>
                                    <DialogContent>
                                        <DialogHeader>
                                            <DialogTitle>Save to Dashboard</DialogTitle>
                                            <DialogDescription>
                                                Add optional notes about this resource (e.g., appointment dates, documents needed)
                                            </DialogDescription>
                                        </DialogHeader>
                                        <div className="space-y-4 py-4">
                                            <Textarea
                                                placeholder="Add notes here..."
                                                value={notes}
                                                onChange={(e) => setNotes(e.target.value)}
                                                rows={4}
                                            />
                                            <Button
                                                onClick={handleSave}
                                                className="w-full bg-military-navy text-white hover:bg-military-navy/90 hover:text-white"
                                                disabled={saveMutation.isPending}
                                            >
                                                {saveMutation.isPending ? (
                                                    <>
                                                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                                        Saving...
                                                    </>
                                                ) : (
                                                    <>
                                                        <BookmarkPlus className="mr-2 h-4 w-4" />
                                                        Save to Dashboard
                                                    </>
                                                )}
                                            </Button>
                                        </div>
                                    </DialogContent>
                                </Dialog>
                            )}
                        </div>
                    </CardHeader>

                    <CardContent className="space-y-6">
                        {/* Description */}
                        <div>
                            <h3 className="font-semibold text-lg mb-2 flex items-center gap-2">
                                <Info className="h-5 w-5 text-primary-600" />
                                About This Resource
                            </h3>
                            <p className="text-gray-700 leading-relaxed">{resource.description}</p>
                        </div>

                        {/* Contact Information */}
                        <div className="space-y-3">
                            <h3 className="font-semibold text-lg mb-3 flex items-center gap-2">
                                <Building className="h-5 w-5 text-primary-600" />
                                Contact Information
                            </h3>

                            {resource.websiteUrl && (
<a
                                href={resource.websiteUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
                                >
                                <ExternalLink className="h-5 w-5 text-gray-600" />
                                <div>
                                <p className="text-sm text-gray-600">Website</p>
                                <p className="text-primary-600 font-medium">Visit Website</p>
                                </div>
                                </a>
                                )}

                            {resource.phoneNumber && (
<a
                                href={`tel:${resource.phoneNumber}`}
                                className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
                                >
                                <Phone className="h-5 w-5 text-gray-600" />
                                <div>
                                <p className="text-sm text-gray-600">Phone</p>
                                <p className="font-medium">{formatPhoneNumber(resource.phoneNumber)}</p>
            </div>
        </a>
)}

{resource.email && (
<a
        href={`mailto:${resource.email}`}
    className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
        >
        <Mail className="h-5 w-5 text-gray-600" />
        <div>
        <p className="text-sm text-gray-600">Email</p>
    <p className="font-medium">{resource.email}</p>
</div>
</a>
)}

{resource.addressLine1 && (
    <div className="flex items-start gap-3 p-3 bg-gray-50 rounded-lg">
        <MapPin className="h-5 w-5 text-gray-600 mt-0.5" />
        <div>
            <p className="text-sm text-gray-600 mb-1">Address</p>
            <p className="font-medium">{resource.addressLine1}</p>
            {resource.city && resource.state && (
                <p className="text-gray-700">
                    {resource.city}, {resource.state} {resource.zipCode}
                </p>
            )}
        </div>
    </div>
)}
</div>

{/* Eligibility Criteria */}
{resource.eligibilityCriteria && (
    <div>
        <h3 className="font-semibold text-lg mb-2 flex items-center gap-2">
            <CheckCircle2 className="h-5 w-5 text-primary-600" />
            Eligibility Criteria
        </h3>
        <p className="text-gray-700 leading-relaxed bg-blue-50 p-4 rounded-lg">
            {resource.eligibilityCriteria}
        </p>
    </div>
)}
</CardContent>
</Card>

{/* Similar Resources */}
<div className="mt-8">
    <h2 className="text-xl font-bold mb-4">Explore More Resources</h2>
    <Button
        asChild
        variant="outline"
        className="w-full md:w-auto"
    >
        <Link to={`/resources?category=${resource.category.id}`}>
            View All {resource.category.name} Resources
        </Link>
    </Button>
                </div>
            </div>
        </div>
        </>
    );
}
