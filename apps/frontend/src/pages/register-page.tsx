import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import { useAuth } from '@/hooks/use-auth.ts';
import { Shield } from 'lucide-react';
import type { RegisterRequest, BranchOfService } from '@/types';

// Register form validation schema
const registerSchema = z.object({
    email: z.string().email('Invalid email address'),
    password: z.string()
        .min(8, 'Password must be at least 8 characters')
        .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
        .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
        .regex(/[0-9]/, 'Password must contain at least one number'),
    confirmPassword: z.string(),
    firstName: z.string().min(2, 'First name must be at least 2 characters'),
    lastName: z.string().min(2, 'Last name must be at least 2 characters'),
    branchOfService: z.string().min(1, 'Please select your branch of service'),
    city: z.string().optional(),
    state: z.string().optional(),
    zipCode: z.string().optional(),
    isHomeless: z.boolean(),
}).refine((data) => data.password === data.confirmPassword, {
    message: "Passwords don't match",
    path: ['confirmPassword'],
});

type RegisterFormData = z.infer<typeof registerSchema>;

const BRANCHES: { value: BranchOfService; label: string }[] = [
    { value: 'ARMY', label: 'Army' },
    { value: 'NAVY', label: 'Navy' },
    { value: 'AIR_FORCE', label: 'Air Force' },
    { value: 'MARINES', label: 'Marines' },
    { value: 'COAST_GUARD', label: 'Coast Guard' },
    { value: 'SPACE_FORCE', label: 'Space Force' },
];

const US_STATES = [
    'AL', 'AK', 'AZ', 'AR', 'CA', 'CO', 'CT', 'DE', 'FL', 'GA',
    'HI', 'ID', 'IL', 'IN', 'IA', 'KS', 'KY', 'LA', 'ME', 'MD',
    'MA', 'MI', 'MN', 'MS', 'MO', 'MT', 'NE', 'NV', 'NH', 'NJ',
    'NM', 'NY', 'NC', 'ND', 'OH', 'OK', 'OR', 'PA', 'RI', 'SC',
    'SD', 'TN', 'TX', 'UT', 'VT', 'VA', 'WA', 'WV', 'WI', 'WY',
];

export default function RegisterPage() {
    const { register: registerUser } = useAuth();
    const navigate = useNavigate();
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMessage, setErrorMessage] = useState<string>('');

    const {
        register,
        handleSubmit,
        formState: { errors },
        setValue,
    } = useForm<RegisterFormData>({
        resolver: zodResolver(registerSchema),
        defaultValues: {
            isHomeless: false,
        },
    });

    const onSubmit = async (data: RegisterFormData) => {
        try {
            setIsSubmitting(true);
            setErrorMessage('');

            // Remove confirmPassword before sending to backend
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            const { confirmPassword, ...registerData } = data;

            await registerUser(registerData as RegisterRequest, {
                onSuccess: () => {
                    navigate('/dashboard');
                },
            });
        } catch (error: unknown) {
            console.error('Registration failed:', error);

            // Display error message to user
            const message = error && typeof error === 'object' && 'response' in error
                ? (error.response as { data?: { message?: string } })?.data?.message
                : error instanceof Error
                ? error.message
                : 'Registration failed. Please try again.';
            setErrorMessage(message || 'Registration failed. Please try again.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center bg-gradient-to-br from-primary-50 to-military-green/10 px-4 py-12">
            <Card className="w-full max-w-2xl">
                <CardHeader className="space-y-1 text-center">
                    <div className="flex justify-center mb-4">
                        <div className="p-3 bg-military-navy rounded-full">
                            <Shield className="h-8 w-8 text-white" />
                        </div>
                    </div>
                    <CardTitle className="text-2xl font-bold">Join VetConnect</CardTitle>
                    <CardDescription>
                        Create an account to access veteran resources and support
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                        {/* Error Message Display */}
                        {errorMessage && (
                            <div className="p-3 bg-red-50 border border-red-200 rounded-md">
                                <p className="text-sm text-red-600">{errorMessage}</p>
                            </div>
                        )}

                        {/* Name Fields */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div className="space-y-2">
                                <Label htmlFor="firstName">First Name *</Label>
                                <Input
                                    id="firstName"
                                    placeholder="John"
                                    {...register('firstName')}
                                    className={errors.firstName ? 'border-red-500' : ''}
                                />
                                {errors.firstName && (
                                    <p className="text-sm text-red-600">{errors.firstName.message}</p>
                                )}
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="lastName">Last Name *</Label>
                                <Input
                                    id="lastName"
                                    placeholder="Doe"
                                    {...register('lastName')}
                                    className={errors.lastName ? 'border-red-500' : ''}
                                />
                                {errors.lastName && (
                                    <p className="text-sm text-red-600">{errors.lastName.message}</p>
                                )}
                            </div>
                        </div>

                        {/* Email */}
                        <div className="space-y-2">
                            <Label htmlFor="email">Email *</Label>
                            <Input
                                id="email"
                                type="email"
                                placeholder="veteran@example.com"
                                {...register('email')}
                                className={errors.email ? 'border-red-500' : ''}
                            />
                            {errors.email && (
                                <p className="text-sm text-red-600">{errors.email.message}</p>
                            )}
                        </div>

                        {/* Branch of Service */}
                        {/* Branch of Service */}
                        <div className="space-y-2">
                            <Label htmlFor="branchOfService">Branch of Service *</Label>
                            <Select
                                onValueChange={(value) => setValue('branchOfService', value)}
                            >
                                <SelectTrigger
                                    className={`bg-white ${errors.branchOfService ? 'border-red-500' : ''}`}
                                >
                                    <SelectValue placeholder="Select your branch" />
                                </SelectTrigger>
                                <SelectContent className="bg-white">
                                    {BRANCHES.map((branch) => (
                                        <SelectItem
                                            key={branch.value}
                                            value={branch.value}
                                            className="cursor-pointer"
                                        >
                                            {branch.label}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                            {errors.branchOfService && (
                                <p className="text-sm text-red-600">{errors.branchOfService.message}</p>
                            )}
                        </div>

                        {/* Password Fields */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div className="space-y-2">
                                <Label htmlFor="password">Password *</Label>
                                <div className="relative">
                                    <Input
                                        id="password"
                                        type={showPassword ? 'text' : 'password'}
                                        placeholder="••••••••"
                                        {...register('password')}
                                        className={errors.password ? 'border-red-500' : ''}
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setShowPassword(!showPassword)}
                                        className="absolute right-3 top-1/2 -translate-y-1/2 text-sm text-gray-500 hover:text-gray-700"
                                    >
                                        {showPassword ? 'Hide' : 'Show'}
                                    </button>
                                </div>
                                {errors.password && (
                                    <p className="text-sm text-red-600">{errors.password.message}</p>
                                )}
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="confirmPassword">Confirm Password *</Label>
                                <div className="relative">
                                    <Input
                                        id="confirmPassword"
                                        type={showConfirmPassword ? 'text' : 'password'}
                                        placeholder="••••••••"
                                        {...register('confirmPassword')}
                                        className={errors.confirmPassword ? 'border-red-500' : ''}
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                        className="absolute right-3 top-1/2 -translate-y-1/2 text-sm text-gray-500 hover:text-gray-700"
                                    >
                                        {showConfirmPassword ? 'Hide' : 'Show'}
                                    </button>
                                </div>
                                {errors.confirmPassword && (
                                    <p className="text-sm text-red-600">{errors.confirmPassword.message}</p>
                                )}
                            </div>
                        </div>

                        {/* Location Fields (Optional) */}
                        <div className="space-y-4">
                            <p className="text-sm font-medium text-gray-700">Location (Optional)</p>
                            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                <div className="space-y-2">
                                    <Label htmlFor="city">City</Label>
                                    <Input
                                        id="city"
                                        placeholder="Ashburn"
                                        {...register('city')}
                                    />
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="state">State</Label>
                                    <Select
                                        onValueChange={(value) => setValue('state', value)}
                                    >
                                        <SelectTrigger>
                                            <SelectValue placeholder="Select state" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            {US_STATES.map((state) => (
                                                <SelectItem key={state} value={state}>
                                                    {state}
                                                </SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="zipCode">ZIP Code</Label>
                                    <Input
                                        id="zipCode"
                                        placeholder="20147"
                                        {...register('zipCode')}
                                    />
                                </div>
                            </div>
                        </div>

                        {/* Homeless Checkbox */}
                        <div className="flex items-center space-x-2">
                            <input
                                type="checkbox"
                                id="isHomeless"
                                {...register('isHomeless')}
                                className="h-4 w-4 rounded border-gray-300"
                            />
                            <Label htmlFor="isHomeless" className="text-sm font-normal cursor-pointer">
                                I am currently experiencing homelessness or housing insecurity
                            </Label>
                        </div>


                        {/* Submit Button */}
                        <Button
                            type="submit"
                            className="w-full bg-military-navy hover:bg-military-navy/90 text-white font-medium"
                            disabled={isSubmitting}
                        >
                            {isSubmitting ? 'Creating Account...' : 'Create Account'}
                        </Button>

                        {/* Login Link */}
                        <div className="text-center text-sm">
                            <span className="text-gray-600">Already have an account? </span>
                            <Link
                                to="/login"
                                className="text-military-navy font-medium hover:underline"
                            >
                                Sign in
                            </Link>
                        </div>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
}