# Theme Switching Fix

## Problem
The color scheme was not changing based on the user's branch of service, even though the theme infrastructure was in place.

## Root Cause
The `ThemeContext` was correctly setting CSS variables (`--color-primary`, `--color-secondary`, etc.) when the user's branch changed, but **no components were actually using these dynamic CSS variables**. Instead, they were using hardcoded Tailwind classes like `bg-military-navy` and `bg-military-green`.

## Solution
1. **Added CSS variables to `index.css`**: Declared the dynamic theme variables in the `:root` so they're available throughout the app
2. **Updated components to use dynamic colors**: Replaced hardcoded military color classes with inline styles that reference the CSS variables

### Files Modified

#### `apps/frontend/src/index.css`
- Added dynamic theme CSS variables to the `@theme` block

#### `apps/frontend/src/pages/profile.tsx`
- Updated Edit Profile button to use `var(--color-primary)`
- Updated Save Changes button to use `var(--color-primary)`
- Updated Branch of Service badge to use `var(--color-primary)`

#### `apps/frontend/src/pages/dashboard.tsx`
- Updated National badge to use `var(--color-secondary)`
- Updated Save Changes button in notes editor to use `var(--color-primary)`
- Updated Browse Resources button to use `var(--color-primary)`

#### `apps/frontend/src/pages/login-page.tsx`
- Updated shield icon background to use `var(--color-primary)`
- Updated Sign In button to use `var(--color-primary)`

#### `apps/frontend/src/pages/register-page.tsx`
- Updated shield icon background to use `var(--color-primary)`
- Updated Sign Up button to use `var(--color-primary)`

#### `apps/frontend/src/components/layout/navbar.tsx`
- Updated Sign Up button to use `var(--color-secondary)`

## How It Works

The flow is now:

1. User logs in or updates their profile with a branch of service
2. `useAuth` hook calls `setThemeByBranch()` from `ThemeContext`
3. `ThemeContext` updates CSS variables on the document root:
   ```javascript
   root.style.setProperty('--color-primary', theme.primary);
   root.style.setProperty('--color-secondary', theme.secondary);
   // etc...
   ```
4. Components use inline styles to reference these variables:
   ```jsx
   <Button style={{ backgroundColor: 'var(--color-primary)' }} ...>
   ```

## Testing

You can test the theme switching:

1. Open `apps/frontend/test-theme.html` in a browser to see the color palette for each branch
2. In the actual app, register with different branches or update your profile to see colors change
3. Key colors:
   - **Army**: Black (#000000) primary, Gold (#FFD700) secondary
   - **Navy**: Navy Blue (#000080) primary, Gold (#FFD700) secondary
   - **Air Force**: Air Force Blue (#00308F) primary, Gold (#FFD700) secondary
   - **Marines**: Scarlet Red (#CC0000) primary, Gold (#FFD700) secondary
   - **Coast Guard**: Blue (#0000FF) primary, Red (#FF0000) secondary
   - **Space Force**: Space Force Blue (#1C2A5A) primary, White (#FFFFFF) secondary

## Future Improvements

Consider creating a reusable component or utility for themed buttons/badges instead of using inline styles everywhere. For example:

```typescript
// Utility function
export const useThemeStyle = (colorVar: 'primary' | 'secondary' | 'accent') => {
  return { backgroundColor: `var(--color-${colorVar})` };
};

// Usage
<Button style={useThemeStyle('primary')} ...>
```

Or create themed component variants:
```typescript
<Button variant="themed-primary" ...>
<Badge variant="themed-secondary" ...>
```
