import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { Button } from '../components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Badge } from '../components/ui/badge';
import { Skeleton } from '../components/ui/skeleton';
import { Textarea } from '../components/ui/textarea';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '../components/ui/dialog';
import { useAuth } from '../hooks/use-auth';
import savedResourceService from '../services/saved-resource-service';
import { formatPhoneNumber } from '../lib/utils';
import { format } from 'date-fns';
import {
    BookmarkCheck,
    Trash2,
    Edit,
    ExternalLink,
    Phone,
    MapPin,
    Globe,
    Search,
    Loader2,
    Mail
} from 'lucide-react';

export default function DashboardPage() {
    const { user } = useAuth();
    const queryClient = useQueryClient();
    const [editingId, setEditingId] = useState<string | null>(null);
    const [editNotes, setEditNotes] = useState('');
    const [deleteId, setDeleteId] = useState<string | null>(null);

    // Fetch saved resources
    const { data: savedResources, isLoading } = useQuery({
        queryKey: ['saved', 'resources'],
        queryFn: () => savedResourceService.getSavedResources(),
    });

    // Update notes mutation
    const updateNotesMutation = useMutation({
        mutationFn: ({ id, notes }: { id: string; notes: string }) =>
            savedResourceService.updateNotes(id, notes),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['saved', 'resources'] });
            setEditingId(null);
            setEditNotes('');
        },
    });

    // Delete mutation
    const deleteMutation = useMutation({
        mutationFn: (id: string) => savedResourceService.removeSavedResource(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['saved', 'resources'] });
            queryClient.invalidateQueries({ queryKey: ['saved', 'check'] });
            setDeleteId(null);
        },
    });

    const handleUpdateNotes = (id: string) => {
        updateNotesMutation.mutate({ id, notes: editNotes });
    };

    const handleDelete = (id: string) => {
        deleteMutation.mutate(id);
    };

    return (
        <div className="min-h-screen bg-gray-50 py-8">
            <div className="container mx-auto px-4">
                {/* Header */}
                <div className="mb-8">
                    <h1 className="text-3xl font-bold mb-2">
                        Welcome back, {user?.firstName}! 🎖️
                    </h1>
                    <p className="text-gray-600">
                        Your saved resources and personalized dashboard
                    </p>
                </div>

                {/* Stats Cards */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                    <Card>
                        <CardHeader className="pb-3">
                            <CardDescription>Saved Resources</CardDescription>
                            <CardTitle className="text-3xl">
                                {savedResources?.length || 0}
                            </CardTitle>
                        </CardHeader>
                    </Card>

                    <Card>
                        <CardHeader className="pb-3">
                            <CardDescription>Branch of Service</CardDescription>
                            <CardTitle className="text-xl">
                                {user?.branchDisplayName}
                            </CardTitle>
                        </CardHeader>
                    </Card>

                    <Card>
                        <CardHeader className="pb-3">
                            <CardDescription>Location</CardDescription>
                            <CardTitle className="text-xl">
                                {user?.city && user?.state
                                    ? `${user.city}, ${user.state}`
                                    : 'Not set'}
                            </CardTitle>
                        </CardHeader>
                    </Card>
                </div>

                {/* Saved Resources */}
                <div>
                    <div className="flex items-center justify-between mb-6">
                        <h2 className="text-2xl font-bold flex items-center gap-2">
                            <BookmarkCheck className="h-6 w-6" />
                            Your Saved Resources
                        </h2>
                        <Button asChild variant="outline">
                            <Link to="/resources">
                                <Search className="h-4 w-4 mr-2" />
                                Browse More
                            </Link>
                        </Button>
                    </div>

                    {isLoading ? (
                        // Loading skeletons
                        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                            {Array.from({ length: 4 }).map((_, i) => (
                                <Card key={i}>
                                    <CardHeader>
                                        <Skeleton className="h-6 w-3/4 mb-2" />
                                        <Skeleton className="h-4 w-1/2" />
                                    </CardHeader>
                                    <CardContent>
                                        <Skeleton className="h-4 w-full mb-2" />
                                        <Skeleton className="h-4 w-2/3" />
                                    </CardContent>
                                </Card>
                            ))}
                        </div>
                    ) : savedResources && savedResources.length > 0 ? (
                        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                            {savedResources.map((saved) => (
                                <Card key={saved.id} className="hover:shadow-lg transition-shadow">
                                    <CardHeader>
                                        <div className="flex items-start justify-between gap-2">
                                            <div className="flex-1">
                                                <div className="flex flex-wrap gap-2 mb-2">
                                                    <Badge variant="secondary">
                                                        {saved.resource.category.name}
                                                    </Badge>
                                                    {saved.resource.isNational ? (
                                                        <Badge className="bg-military-gold text-white text-xs">
                                                            <Globe className="h-3 w-3 mr-1" />
                                                            National
                                                        </Badge>
                                                    ) : saved.resource.state && (
                                                        <Badge variant="outline" className="text-xs">
                                                            {saved.resource.state}
                                                        </Badge>
                                                    )}
                                                </div>
                                                <Link to={`/resources/${saved.resource.id}`}>
                                                    <CardTitle className="hover:text-primary-600 transition-colors">
                                                        {saved.resource.name}
                                                    </CardTitle>
                                                </Link>
                                                <p className="text-xs text-gray-500 mt-1">
                                                    Saved {format(new Date(saved.savedAt), 'MMM d, yyyy')}
                                                </p>
                                            </div>

                                            {/* Action Buttons */}
                                            <div className="flex gap-1">
                                                <Dialog
                                                    open={editingId === saved.id}
                                                    onOpenChange={(open) => {
                                                        if (open) {
                                                            setEditingId(saved.id);
                                                            setEditNotes(saved.notes || '');
                                                        } else {
                                                            setEditingId(null);
                                                            setEditNotes('');
                                                        }
                                                    }}
                                                >
                                                    <DialogTrigger asChild>
                                                        <Button variant="ghost" size="sm">
                                                            <Edit className="h-4 w-4" />
                                                        </Button>
                                                    </DialogTrigger>
                                                    <DialogContent>
                                                        <DialogHeader>
                                                            <DialogTitle>Edit Notes</DialogTitle>
                                                            <DialogDescription>
                                                                Update your personal notes for this resource
                                                            </DialogDescription>
                                                        </DialogHeader>
                                                        <div className="space-y-4 py-4">
                                                            <Textarea
                                                                value={editNotes}
                                                                onChange={(e) => setEditNotes(e.target.value)}
                                                                rows={4}
                                                                placeholder="Add notes here..."
                                                            />
                                                            <Button
                                                                onClick={() => handleUpdateNotes(saved.id)}
                                                                className="w-full"
                                                                disabled={updateNotesMutation.isPending}
                                                            >
                                                                {updateNotesMutation.isPending ? (
                                                                    <>
                                                                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                                                        Updating...
                                                                    </>
                                                                ) : (
                                                                    'Save Changes'
                                                                )}
                                                            </Button>
                                                        </div>
                                                    </DialogContent>
                                                </Dialog>

                                                <Dialog
                                                    open={deleteId === saved.id}
                                                    onOpenChange={(open) => setDeleteId(open ? saved.id : null)}
                                                >
                                                    <DialogTrigger asChild>
                                                        <Button variant="ghost" size="sm" className="text-red-600 hover:text-red-700">
                                                            <Trash2 className="h-4 w-4" />
                                                        </Button>
                                                    </DialogTrigger>
                                                    <DialogContent>
                                                        <DialogHeader>
                                                            <DialogTitle>Remove Resource</DialogTitle>
                                                            <DialogDescription>
                                                                Are you sure you want to remove "{saved.resource.name}" from your dashboard?
                                                            </DialogDescription>
                                                        </DialogHeader>
                                                        <div className="flex gap-2 justify-end pt-4">
                                                            <Button
                                                                variant="outline"
                                                                onClick={() => setDeleteId(null)}
                                                            >
                                                                Cancel
                                                            </Button>
                                                            <Button
                                                                variant="destructive"
                                                                onClick={() => handleDelete(saved.id)}
                                                                disabled={deleteMutation.isPending}
                                                            >
                                                                {deleteMutation.isPending ? (
                                                                    <>
                                                                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                                                        Removing...
                                                                    </>
                                                                ) : (
                                                                    'Remove'
                                                                )}
                                                            </Button>
                                                        </div>
                                                    </DialogContent>
                                                </Dialog>
                                            </div>
                                        </div>
                                    </CardHeader>

                                    <CardContent className="space-y-3">
                                        <CardDescription className="line-clamp-2">
                                            {saved.resource.description}
                                        </CardDescription>

                                        {/* Contact Info */}
                                        <div className="space-y-2 text-sm">
                                            {saved.resource.phoneNumber && (
                                                <div className="flex items-center gap-2 text-gray-600">
                                                    <Phone className="h-4 w-4" />
                                                    <a href={`tel:${saved.resource.phoneNumber}`} className="hover:text-primary-600">
                                                        {formatPhoneNumber(saved.resource.phoneNumber)}
                                                    </a>
                                                </div>
                                            )}
                                            {saved.resource.email && (
                                                <div className="flex items-center gap-2 text-gray-600">
                                                    <Mail className="h-4 w-4" />
                                                    <a href={`mailto:${saved.resource.email}`} className="hover:text-primary-600">
                                                        {saved.resource.email}
                                                    </a>
                                                </div>
                                            )}
                                            {saved.resource.city && saved.resource.state && (
                                                <div className="flex items-center gap-2 text-gray-600">
                                                    <MapPin className="h-4 w-4" />
                                                    <span>{saved.resource.city}, {saved.resource.state}</span>
                                                </div>
                                            )}
                                            {saved.resource.websiteUrl && (
<a
                                                href={saved.resource.websiteUrl}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                className="flex items-center gap-2 text-primary-600 hover:text-primary-700"
                                                >
                                                <ExternalLink className="h-4 w-4" />
                                                <span>Visit Website</span>
                                                </a>
                                                )}
                                        </div>

                                        {/* Notes */}
                                        {saved.notes && (
                                            <div className="mt-3 p-3 bg-yellow-50 border border-yellow-200 rounded-md">
                                                <p className="text-xs font-semibold text-yellow-800 mb-1">Your Notes:</p>
                                                <p className="text-sm text-gray-700">{saved.notes}</p>
                                            </div>
                                        )}
                                    </CardContent>
                                </Card>
                            ))}
                        </div>
                        ) : (
                        // Empty state
                        <Card>
                        <CardContent className="text-center py-12">
                        <BookmarkCheck className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                        <h3 className="text-lg font-semibold mb-2">No saved resources yet</h3>
                        <p className="text-gray-600 mb-4">
                        Start exploring and save resources to your dashboard for quick access
                        </p>
                        <Button asChild className="bg-military-navy">
                        <Link to="/resources">
                        <Search className="h-4 w-4 mr-2" />
                        Browse Resources
                        </Link>
                        </Button>
                        </CardContent>
                        </Card>
                        )}
                </div>
            </div>
        </div>
    );
}