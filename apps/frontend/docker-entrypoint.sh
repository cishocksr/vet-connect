#!/bin/sh
# Replace config.js with environment-specific values at runtime

cat > /usr/share/nginx/html/config.js << EOF
window.ENV = {
  API_BASE_URL: '${API_BASE_URL:-http://localhost:8080/api}'
};
EOF

# Start nginx
nginx -g 'daemon off;'
