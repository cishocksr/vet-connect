#!/bin/bash

# Test script to check if token is valid
# Usage: ./test-token.sh <your-token>

if [ -z "$1" ]; then
    echo "Usage: ./test-token.sh <your-token>"
    echo ""
    echo "Get your token from browser localStorage or console"
    exit 1
fi

TOKEN=$1

echo "Testing token validity..."
echo ""

# Test 1: Check /api/saved endpoint
echo "1. Testing GET /api/saved (should return 200 if authenticated)"
curl -s -w "\nStatus: %{http_code}\n" \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/saved

echo ""
echo ""

# Test 2: Check user info
echo "2. Testing GET /api/users/me (should return user info)"
curl -s -w "\nStatus: %{http_code}\n" \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/users/me

echo ""
