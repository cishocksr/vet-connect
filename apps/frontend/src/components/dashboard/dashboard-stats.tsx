import {Card, CardDescription, CardHeader, CardTitle} from "@/components/ui/card.tsx";
import type {User} from '@/types'

interface DashboardStatsProps {
    savedResourcesCount: number;
    user: User | null;
}

export function DashboardStats({savedResourcesCount, user}: DashboardStatsProps) {
    return(
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <Card>
                <CardHeader className="pb-3">
                    <CardDescription>Saved Resources</CardDescription>
                    <CardTitle className="text-3xl">
                        {savedResourcesCount}
                    </CardTitle>
                </CardHeader>
            </Card>

            <Card>
                <CardHeader className="pb-3">
                <CardDescription>Branch of Service</CardDescription>
                <CardTitle className="text-3xl">
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
                            : 'Not set'
                        }
                    </CardTitle>
                </CardHeader>
            </Card>

        </div>
    )
}