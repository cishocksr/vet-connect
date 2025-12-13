import {Card, CardContent} from "@/components/ui/card.tsx";
import {Button} from "@/components/ui/button.tsx";
import {Link} from "react-router-dom";
import {BookmarkCheck, Search} from "lucide-react";

export function EmptyDashboardState() {
    return (
        <Card>
            <CardContent className="text-center py-12">
                <BookmarkCheck className="h-12 w-12 text-gray-400 mx-auto mb-4"/>
                <h3 className="text-lg fomt-semi-bold mb-2">No saved Resource yet</h3>
                <p className="text-gray-600 mb-4">
                    Start explorting and save resources to your dashboard for quick acces
                </p>
                <Button asChild style={{backgroundColor: 'var(--color-primary}'}} className="hover:opacity-90 text-white">
                    <Link to="/resources">
                        <Search className="h-4 w-4 mr-2"/>
                        Browse Resources
                    </Link>
                </Button>
            </CardContent>
        </Card>
    )
}