import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../hooks/use-auth';
import savedResourceService from '../services/saved-resource-service';
import {DashboardHeader} from "@/components/dashboard/dashboard-header.tsx";
import {DashboardStats} from "@/components/dashboard/dashboard-stats.tsx";
import {SavedResourceList} from "@/components/dashboard/saved-resource-list.tsx";

export default function DashboardPage() {
    const { user } = useAuth();
    const queryClient = useQueryClient();

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
        },
    });

    // Delete mutation
    const deleteMutation = useMutation({
        mutationFn: (id: string) => savedResourceService.removeSavedResource(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['saved', 'resources'] });
            queryClient.invalidateQueries({ queryKey: ['saved', 'check'] });
        },
    });

    const handleUpdateNotes = (id: string, notes: string) => {
        updateNotesMutation.mutate({ id, notes });
    };

    const handleDelete = (id: string) => {
        deleteMutation.mutate(id);
    };

    return (
        <div className="min-h-screen bg-gray-50 py-8">
            <div className="container mx-auto px-4">
                {/* Header */}
                <DashboardHeader firstName={user?.firstName || 'Veteran'} />

                {/* Stats Cards */}
                <DashboardStats
                    savedResourcesCount={savedResources?.length || 0}
                    user={user}
                />

                {/* Saved Resources */}
                <SavedResourceList
                    savedResources={savedResources}
                    isLoading={isLoading}
                    onUpdateNotes={handleUpdateNotes}
                    onDelete={handleDelete}
                    isUpdating={updateNotesMutation.isPending}
                    isDeleting={deleteMutation.isPending}
                />
            </div>
        </div>
    );
}