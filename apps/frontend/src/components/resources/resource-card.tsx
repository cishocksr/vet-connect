import { Link } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../ui/card';
import { Badge } from '../ui/badge';
import { Globe, MapPin } from 'lucide-react';
import type { ResourceSummary } from '@/types';

interface ResourceCardProps {
    resource: ResourceSummary;
}

export function ResourceCard({ resource }: ResourceCardProps) {
    return (
        <Link to={`/resources/${resource.id}`}>
            <Card className="h-full flex flex-col hover:shadow-xl transition-all duration-300 hover:-translate-y-1 border-l-4 border-l-primary group">
                <CardHeader className="pb-3">
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
                    <CardTitle className="line-clamp-2 text-lg leading-tight group-hover:text-primary transition-colors">
                        {resource.name}
                    </CardTitle>
                </CardHeader>
                <CardContent className="flex-1">
                    <CardDescription className="line-clamp-3">
                        {resource.shortDescription}
                    </CardDescription>
                    {resource.city && resource.state && (
                        <p className="text-sm text-muted-foreground mt-3 flex items-center gap-1">
                            <MapPin className="h-3 w-3" />
                            {resource.city}, {resource.state}
                        </p>
                    )}
                </CardContent>
            </Card>
        </Link>
    );
}