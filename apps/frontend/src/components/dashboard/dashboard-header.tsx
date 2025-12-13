interface DashboardHeaderProps {
    firstName: string;
}

export function DashboardHeader({firstName}: DashboardHeaderProps) {
    return (
        <div className="mb-8">
            <h1 className="text-3xl font-bold mb-2">
                Welcome back, {firstName}! 🎖️
            </h1>
            <p className="text-gray-600">
                Your saved resources and personalized dashboard
            </p>
        </div>
    )
}