# Debug Console Commands

Open browser console (F12) and paste these commands to debug the 403 error:

## 1. Check if you're logged in

```javascript
console.log('Token:', localStorage.getItem('token'));
console.log('User:', JSON.parse(localStorage.getItem('user') || 'null'));
console.log('Refresh Token:', localStorage.getItem('refreshToken'));
```

Expected output:
- Token: Should start with "eyJ..."
- User: Should show your user object with email, id, etc.
- Refresh Token: Should also start with "eyJ..."

If any are `null`, you need to log in.

## 2. Test the save endpoint directly

```javascript
const resourceId = 'PASTE_RESOURCE_ID_HERE'; // Get from URL

fetch('http://localhost:8080/api/saved', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer ' + localStorage.getItem('token'),
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    resourceId: resourceId,
    notes: 'Test note'
  })
})
.then(res => {
  console.log('Status:', res.status);
  return res.json();
})
.then(data => console.log('Response:', data))
.catch(err => console.error('Error:', err));
```

Expected responses:
- **201**: Success! Resource saved
- **403**: Token invalid (log out and log back in)
- **409**: Resource already saved (working as intended)
- **404**: Resource not found (check resource ID)

## 3. Decode your JWT token

```javascript
function parseJwt(token) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => 
      '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    ).join(''));
    return JSON.parse(jsonPayload);
  } catch(e) {
    return { error: 'Invalid token' };
  }
}

const token = localStorage.getItem('token');
if (token) {
  const decoded = parseJwt(token);
  console.log('Token payload:', decoded);
  console.log('User ID:', decoded.userId);
  console.log('Token Version:', decoded.tokenVersion);
  console.log('Expires at:', new Date(decoded.exp * 1000));
  console.log('Is expired?', Date.now() > decoded.exp * 1000);
} else {
  console.log('No token found');
}
```

This shows:
- User ID from token
- Token version
- Expiration date
- Whether token is expired

## 4. Force logout and re-login

```javascript
// Clear everything
localStorage.clear();
console.log('Cleared localStorage. Please refresh and log in again.');
```

## 5. Check what's being sent in the actual request

Open Network tab in DevTools, then try to save a resource. Look for the POST request to `/api/saved`:

- **Headers tab**: Check `Authorization` header shows `Bearer eyJ...`
- **Payload tab**: Check `resourceId` is a valid UUID
- **Response tab**: Check error message

## Most Likely Fix

**The token is expired or has a version mismatch.**

Solution:
1. Click logout
2. Log back in
3. Try saving again

This will give you a fresh, valid token.
