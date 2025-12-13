import {BookmarkCheck, Search} from "lucide-react";
import {Button} from "@/components/ui/button.tsx";
import {SavedResourceCard} from "@/components/dashboard/saved-resource-card.tsx";
import type {SavedResource} from "@/types";
import {Link} from "react-router-dom";
import {EmptyDashboardState} from "@/components/dashboard/empty-dashboard-state.tsx";
import {Card, CardContent, CardHeader} from "@/components/ui/card.tsx";
import {Skeleton} from "@/components/ui/skeleton.tsx";

interface SavedResourceListProps {
    savedResources: SavedResource[] | undefined;
    isLoading: boolean;
    onUpdateNotes: (id: string, notes: string) => void;
    onDelete: (id: string) => void;
    isUpdating: boolean;
    isDeleting: boolean;
}

export function SavedResourceList({
    savedResources,
    isLoading,
    onUpdateNotes,
    onDelete,
    isUpdating,
    isDeleting
}: SavedResourceListProps) {
    return(
        <div>
            {/*Header*/}
            <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-bold flex items-center gap-2">
                    <BookmarkCheck className='h-6 w-6'/>
                    Your Saved Resources
                </h2>
                <Button asChild variant="outline" className="text-gray-700 hover:text-gray-900">
                    <Link to="/resources">
                        <Search className="h-4 w-4 mr-2"/>
                        Browse More
                    </Link>
                </Button>
            </div>

        {/* Loading State*/}
            {isLoading && (
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    {Array.from({length: 4}).map((_,i) => (
                        <Card key={i}>
                            <CardHeader>
                                <Skeleton className="h-6 w-3/4 mb-2" />
                                <Skeleton className="h-4 w-1/2"/>
                            </CardHeader>
                            <CardContent>
                                <Skeleton className="h-4 w-full mb-2"/>
                                <Skeleton className="h-4 w-2/3"/>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            )}

        {/* Resource List */}
            {isLoading && savedResources && savedResources.length > 0 && (
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    {savedResources.map((saved) => (
                        <SavedResourceCard
                            key={saved.id}
                            savedResource={saved}
                            onUpdateNotes={onUpdateNotes}
                            onDelete={onDelete}
                            isUpdating={isUpdating}
                            isDeleting={isDeleting}
                        />
                    ) )}
                </div>
                )}
        {/* Empty State */}
            {!isLoading && savedResources && savedResources.length === 0 && (
                <EmptyDashboardState />
            )}
        </div>
    )
}