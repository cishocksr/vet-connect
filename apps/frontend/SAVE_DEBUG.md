# Save to Dashboard Debug Checklist

## Changes Made

### 1. Added Toast Notifications (`resource-detail-page.tsx`)
- Success toast when resource is saved
- Error toast with specific error message
- User now gets visual feedback

### 2. Fixed API Method Mismatch (`saved-resource-service.ts`)
- Changed `api.put` to `api.patch` for updateNotes to match backend
- Backend expects PATCH /api/saved/{id}/notes

### 3. Added Query Cache Invalidation
- Added `['saved', 'resources']` to invalidation list
- This ensures dashboard refreshes after save

### 4. Added Console Logging
- Logs save request details
- Logs errors to console for debugging

## Testing Checklist

1. **Authentication**
   - [ ] User is logged in (check localStorage for 'token')
   - [ ] Token is valid and not expired

2. **Network Request**
   - [ ] Open browser DevTools (Network tab)
   - [ ] Click "Save to Dashboard"
   - [ ] Check for POST request to `/api/saved`
   - [ ] Verify request payload contains `resourceId` (UUID format)
   - [ ] Check response status (should be 201 Created)

3. **Console Errors**
   - [ ] Open browser console
   - [ ] Look for any JavaScript errors
   - [ ] Check the "Saving resource" log message

4. **Backend Validation**
   - [ ] Resource must exist (valid UUID)
   - [ ] Cannot save same resource twice
   - [ ] Notes must be < 2000 characters

## 403 Forbidden Error - CURRENT ISSUE

### What 403 Means
The backend received your request but rejected it because:
1. **Token is missing** - Not found in localStorage
2. **Token is expired** - JWT has passed expiration time
3. **Token is blacklisted** - You logged out or changed password
4. **Token version mismatch** - Password was changed, invalidating old tokens
5. **Token is malformed** - Not in correct JWT format

### Quick Fixes (Try in Order)

#### Fix 1: Refresh Your Token (MOST COMMON)
1. Log out completely
2. Log back in with your credentials
3. Try saving the resource again

#### Fix 2: Check Token in Console
1. Open browser console (F12)
2. Type: `localStorage.getItem('token')`
3. You should see a long JWT string starting with "eyJ..."
4. If you see `null`, you need to log in

#### Fix 3: Test Token Validity
```bash
# In terminal, run:
./test-token.sh YOUR_TOKEN_HERE

# Or manually:
curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/saved
```

If you get 403, the token is invalid. Log out and log back in.

#### Fix 4: Check Auth Debug Panel
Look at the bottom-right corner of the page - you should see a debug panel showing:
- ✅ Authenticated: true
- User email
- Token preview
- User ID

If any of these are missing or show "None", log out and log back in.

### Backend Token Validation Checks
The backend JWT filter checks (in order):
1. Token format is valid (Bearer <token>)
2. Token signature is valid
3. Token is not expired
4. Token is not blacklisted
5. User's token version matches
6. User exists in database

## Common Issues

### Issue 1: 401 Unauthorized
- **Cause**: Token expired or invalid
- **Fix**: Log out and log back in

### Issue 2: 409 Conflict
- **Cause**: Resource already saved
- **Fix**: Working as intended - check if resource shows as "Saved"

### Issue 3: 404 Not Found
- **Cause**: Invalid resource ID
- **Fix**: Verify resource exists in database

### Issue 4: No visual feedback
- **Cause**: Toast notifications not working
- **Fix**: Check if Toaster component is in App.tsx

### Issue 5: Dashboard not updating
- **Cause**: Query cache not invalidating
- **Fix**: Already added proper invalidation - refresh page manually

## Manual Test

1. Navigate to any resource detail page
2. Click "Save to Dashboard" button
3. Add optional notes
4. Click "Save to Dashboard" in dialog
5. Should see success toast
6. Navigate to `/dashboard`
7. Should see saved resource in the list

## API Endpoints

- **Save**: POST `/api/saved`
  ```json
  {
    "resourceId": "uuid-string",
    "notes": "optional notes"
  }
  ```

- **Get Saved**: GET `/api/saved`
- **Check if Saved**: GET `/api/saved/check/{resourceId}`

## Debug Commands

```bash
# Check if backend is running
curl http://localhost:8080/api/health

# Test save endpoint (replace TOKEN and RESOURCE_ID)
curl -X POST http://localhost:8080/api/saved \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"resourceId": "RESOURCE_UUID"}'
```
