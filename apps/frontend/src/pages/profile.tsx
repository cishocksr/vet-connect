import { useAuth } from '../hooks/use-auth';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card';
import { Badge } from '../components/ui/badge';
import { User, Mail, MapPin, Shield } from 'lucide-react';

export default function ProfilePage() {
    const { user } = useAuth();

    if (!user) return null;

    return (
        <div className="min-h-screen bg-gray-50 py-8">
            <div className="container mx-auto px-4 max-w-2xl">
                <h1 className="text-3xl font-bold mb-8">Profile</h1>

                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <User className="h-5 w-5" />
                            Personal Information
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div>
                            <label className="text-sm text-gray-600">Full Name</label>
                            <p className="font-medium">{user.fullName}</p>
                        </div>

                        <div>
                            <label className="text-sm text-gray-600 flex items-center gap-2">
                                <Mail className="h-4 w-4" />
                                Email
                            </label>
                            <p className="font-medium">{user.email}</p>
                        </div>

                        <div>
                            <label className="text-sm text-gray-600 flex items-center gap-2">
                                <Shield className="h-4 w-4" />
                                Branch of Service
                            </label>
                            <Badge className="mt-1 bg-military-green">
                                {user.branchDisplayName}
                            </Badge>
                        </div>

                        {(user.city || user.state) && (
                            <div>
                                <label className="text-sm text-gray-600 flex items-center gap-2">
                                    <MapPin className="h-4 w-4" />
                                    Location
                                </label>
                                <p className="font-medium">
                                    {user.city && user.state
                                        ? `${user.city}, ${user.state} ${user.zipCode || ''}`
                                        : 'Not set'}
                                </p>
                            </div>
                        )}
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}