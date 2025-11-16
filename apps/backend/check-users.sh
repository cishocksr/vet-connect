#!/bin/bash

# Database connection helper script
# Checks users in the vet_connect database

DB_NAME="${POSTGRES_DB:-vet_connect}"
DB_USER="${POSTGRES_USER:-postgres}"
DB_HOST="${POSTGRES_HOST:-localhost}"
DB_PORT="${POSTGRES_PORT:-5432}"

echo "Connecting to database: $DB_NAME"
echo ""

# Check if running in Docker
if command -v docker &> /dev/null; then
    CONTAINER=$(docker ps --filter "name=postgres" --format "{{.Names}}" | head -1)
    if [ ! -z "$CONTAINER" ]; then
        echo "Found Docker container: $CONTAINER"
        echo ""
        echo "=== ALL USERS ==="
        docker exec -it $CONTAINER psql -U $DB_USER -d $DB_NAME -c "
        SELECT 
            email, 
            first_name || ' ' || last_name as name,
            branch_of_service,
            role,
            to_char(created_at, 'YYYY-MM-DD HH24:MI') as created
        FROM users 
        ORDER BY created_at DESC;
        "
        
        echo ""
        echo "=== TOTAL USER COUNT ==="
        docker exec -it $CONTAINER psql -U $DB_USER -d $DB_NAME -c "SELECT COUNT(*) as total_users FROM users;"
        exit 0
    fi
fi

# Try direct psql connection
echo "Trying direct psql connection..."
echo ""
echo "=== ALL USERS ==="
PGPASSWORD=$POSTGRES_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
SELECT 
    email, 
    first_name || ' ' || last_name as name,
    branch_of_service,
    role,
    to_char(created_at, 'YYYY-MM-DD HH24:MI') as created
FROM users 
ORDER BY created_at DESC;
"

echo ""
echo "=== TOTAL USER COUNT ==="
PGPASSWORD=$POSTGRES_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT COUNT(*) as total_users FROM users;"
