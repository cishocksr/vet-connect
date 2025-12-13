import {Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle} from "@/components/ui/dialog.tsx";
import {Textarea} from "@/components/ui/textarea.tsx";
import {Button} from "@/components/ui/button.tsx";
import {Loader2} from "lucide-react";

interface EditNotesDialogProps {
    resourceName: string;
    notes: string;
    open: boolean;
    onOpenChange: (open: boolean) => void;
    onNotesChange: (notes: string) => void;
    onSave: () => void;
    isPending: boolean;
}

export function EditNotesDialog({
                                    resourceName,
                                    notes,
                                    open,
                                    onOpenChange,
                                    onNotesChange,
                                    onSave,
                                    isPending
                                }: EditNotesDialogProps) {
    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Edit Notes</DialogTitle>
                    <DialogDescription>
                        Add or update your personal notes for "{resourceName}"
                    </DialogDescription>
                </DialogHeader>
                <div className="space-y-4 pt-4">
                    <Textarea
                        value={notes}
                        onChange={(e) => onNotesChange(e.target.value)}
                        rows={6}
                        placeholder="Add notes here..."
                        />
                    <Button
                        onClick={onSave}
                        style={{backgroundColor: 'var(--color-primary)'}}
                        className="w-full hover:opacity-90 text-white"
                        disabled={isPending}
                        >
                        {isPending ? (
                            <>
                                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                Updating...
                            </>
                            ):(
                                'Save Changes'
                            )}
                    </Button>
                </div>
            </DialogContent>
        </Dialog>
    )
                                }