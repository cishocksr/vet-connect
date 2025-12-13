import {Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle} from "@/components/ui/dialog.tsx";
import {Button} from "@/components/ui/button.tsx";
import {Loader2} from "lucide-react";

interface DeleteResourceDialogProps {
    resourceName: string;
    open: boolean;
    onOpenChange: (open: boolean) => void;
    onConfirm: () => void;
    isPending: boolean;
}

export function DeleteResourceDialog({
                                         resourceName,
                                         open,
                                         onOpenChange,
                                         onConfirm,
                                         isPending,
                                     }: DeleteResourceDialogProps){
    return(
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Remove Resource</DialogTitle>
                    <DialogDescription>
                        Are you sure you want to remove "{resourceName}" from your dashboard?
                    </DialogDescription>
                </DialogHeader>
                <div className="flex gap-2 justify-end pt-4">
                    <Button
                        variant="outline"
                        onClick={() => onOpenChange(false)}
                        className="text-gray-700 hover:text-gray-900"
                        disabled={isPending}
                    >Cancel
                    </Button>
                    <Button
                        variant="destructive"
                        onClick={onConfirm}
                        disabled={isPending}
                        className="text-white"
                    >
                        {isPending ? (
                            <>
                                <Loader2 className="mr-2 h-4 w-4 animate-sping" />
                                Removing...
                            </>
                        ): (
                            'Remove'
                        )}

                    </Button>
                </div>
            </DialogContent>
        </Dialog>
    );
                                     }