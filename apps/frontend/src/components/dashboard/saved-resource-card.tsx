import {useState} from "react";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card.tsx";
import {Link} from "react-router-dom";
import {Badge} from "@/components/ui/badge.tsx";
import {Edit, ExternalLink, Mail, MapPin, Phone, Trash2} from "lucide-react";
import {Button} from "@/components/ui/button.tsx";
import {formatPhoneNumber} from "@/lib/utils.ts";
import type {SavedResource} from "@/types";
import {format} from "date-fns";
import {EditNotesDialog} from "@/components/dashboard/edit-notes-dialog.tsx";
import {DeleteResourceDialog} from "@/components/dashboard/delete-resource-dialog.tsx";

interface SavedResourceCardProps {
    savedResource: SavedResource;
    onUpdateNotes: (id: string, notes: string) => void;
    onDelete: (id: string) => void;
    isUpdating: boolean;
    isDeleting: boolean;
}

export function SavedResourceCard({
    savedResource,
    onUpdateNotes,
    onDelete,
    isUpdating,
    isDeleting
} : SavedResourceCardProps) {
    const [showEditDialog, setShowEditDialog] = useState(false);
    const [showDeleteDialog, setShowDeleteDialog] = useState(false);
    const [editNotes, setEditNotes] = useState(savedResource.notes || '');

    const {resource} = savedResource;

    const handleSaveNotes = () => {
        onUpdateNotes(savedResource.id, editNotes);
        setShowEditDialog(false);
    };

    const handleDelete = () => {
        onDelete(savedResource.id);
    }
    return (
        <>
            <Card>
                <CardHeader>
                    <div className="flex items-start justify-between">
                        <div className="flex-1">
                            <Link
                                to={`/resources/${resource.id}`}
                                className="hover:underline"
                                >
                                <CardTitle className="mb-2">{resource.name}</CardTitle>
                            </Link>
                            <div className="flex items-center gap-2 mb-2">
                                <Badge
                                    style={{backgroundColor: 'var(--color-primary)'}}
                                    className="text-white"
                                >
                                    {resource.categoryName}
                                </Badge>
                                <span className="text-xs text-gray-500">
                                    Saved {format(new Date(savedResource.savedAt), 'MMM d, yyyy')}
                                </span>
                            </div>
                        </div>
                        <div className="flex gap-1">
                            <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => {
                                    setEditNotes(savedResource.notes || '');
                                    setShowEditDialog(true);
                                }}
                                className="text-gray-600 hover:text-gray-900"
                                >
                                <Edit className="h-4 w-4" />
                            </Button>
                            <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => setShowDeleteDialog(true)}
                                className="text-red-600 hover:text-red-700"
                            >
                             <Trash2 className="h-4 w-4" />
                            </Button>
                        </div>
                    </div>
                </CardHeader>

                <CardContent className="space-y-3">
                    <CardDescription className="line-clamp-2">
                        {resource.description}
                    </CardDescription>

                    {/* Contact Info*/}
                    <div className="space-y-2 text-sm">
                        {resource.phoneNumber && (
                            <div className="flex items-center gap-2 text-gray-600">
                                <Phone className="h-4 w-4" />
                                <a
                                    href={`tel:${resource.phoneNumber}`}
                                    className="hover:text-promary-600"
                                >
                                    {formatPhoneNumber(resource.phoneNumber)}
                                </a>
                            </div>
                            )}
                        {resource.email && (
                                <div className="flex items-center gap-2 text-gray-600">
                                    <Mail className="h-4 w-4" />
                                    <a
                                        href={`mailto:${resource.email}`}
                                        className="hover:text-primary-600"
                                    >
                                        {resource.email}
                                    </a>
                                </div>
                            )}
                        {resource.city && resource.state && (
                            <div>
                                <MapPin className="h-4 w-4" />
                                <span>{resource.city}, {resource.state}</span>
                            </div>
                        )}
                        {resource.websiteUrl && (
                            <a
                                href={resource.websiteUrl}
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
                    {savedResource.notes && (
                        <div className="mt-3 p-3 bg-yellow-60 border border-yellow-200 rounded-md">
                            <p className="text-xs font-semibold text-yellow-800 mb-1">
                                Your Notes:
                            </p>
                            <p className="text-sm text-gray-700">{savedResource.notes}</p>
                        </div>
                        )}
                </CardContent>
            </Card>

            {/* Edit Notes Dialog */}
            <EditNotesDialog
                resourceName={resource.name}
                notes={editNotes}
                open={showEditDialog}
                onOpenChange={setShowEditDialog}
                onNotesChange={setEditNotes}
                onSave={handleSaveNotes}
                isPending={isUpdating}
                />

            <DeleteResourceDialog
                resourceName={resource.name}
                open={showDeleteDialog}
                onOpenChange={setShowDeleteDialog}
                onConfirm={handleDelete}
                isPending={isDeleting}
            />
        </>
    )
}