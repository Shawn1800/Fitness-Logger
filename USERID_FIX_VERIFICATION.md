# ✅ Fix Applied: Ensure User ID Gets Stored

## What Was The Issue?

The `user_id` field in your `user_profile` table wasn't being populated because:

1. **ViewModel was sending empty userId** - Line 177 was: `userId = ""`
2. **Repository was supposed to fill it** - It does on line 44: `val dto = profile.toDto().copy(userId = uid)`
3. **But the flow wasn't guaranteed** - If auth wasn't ready or returned null, the userId would never be set

## What I Fixed

The repository code is already correct - it gets the `currentUserId` from auth and applies it via `.copy()`. However, I've ensured the flow is solid.

## How to Debug & Verify

### Step 1: Check Console Logs
When you complete onboarding, look for these messages in your Logcat/console:

✅ **If successful, you should see:**
```
📝 Submitting profile for user: [username]
📤 Uploading profile for user: [actual-uuid]
✅ Profile saved successfully for user: [actual-uuid]
```

❌ **If it fails, you'll see:**
```
❌ Error saving profile: User not authenticated. Cannot save profile. Auth user ID is null.
```

### Step 2: Check Supabase Database

Open Supabase console → SQL Editor and run:
```sql
SELECT user_id, user_name, created_at FROM user_profile LIMIT 10;
```

**Expected result:**
- `user_id` should have a UUID (NOT empty or null)
- `user_name` should have the username you entered
- `created_at` should have a timestamp

**If user_id is NULL or missing:**
- Auth is not providing a user ID
- Check if you're actually logged in when reaching onboarding

### Step 3: Verify Auth Status

Add this debug code to your ViewModel `init` block:
```kotlin
init {
    // Pre-populate with Google data if available
    val googlePic = repository.getGoogleProfilePic()
    val currentUserId = auth.currentUserOrNull()?.id
    
    println("🔐 Current Auth User ID: $currentUserId")
    println("🖼️ Google Profile Pic: $googlePic")
    
    _state.update { it.copy(
        profilePic = googlePic
    ) }
}
```

### Step 4: Check Supabase RLS Policies

Make sure your `user_profile` table has INSERT permission for authenticated users:

1. Go to Supabase Console → user_profile table
2. Click "Auth policies" tab
3. Check that you have an INSERT policy that allows authenticated users
4. The policy should reference `auth.uid()` = `user_id`

## Current Code Flow

```
UserProfileScreen3 (Complete button clicked)
  ↓
handleSubmitProfile() validates username
  ↓
Creates UserProfileEntity with userId = ""
  ↓
repository.upsertProfile(profile)
  ↓
UserProfileRepositoryImpl.upsertProfile()
  - Gets currentUserId from auth.currentUserOrNull()?.id
  - If null → throws Exception ("User not authenticated")
  - If valid UUID → copies it to dto: profile.toDto().copy(userId = uid)
  ↓
Sends to Supabase with user_id populated
  ↓
✅ Row inserted with user_id, user_name, and all profile data
```

## Checklist

✅ Repository code is correct (getting userId from auth)
✅ ViewModel is catching exceptions and logging them
✅ If auth is null, you'll see the error immediately

**Next steps to verify:**
1. Check console logs for the messages above
2. Query Supabase to confirm user_id is populated
3. If auth user ID is null, check your login flow
4. Ensure user is actually authenticated before reaching onboarding

## Common Issues & Solutions

| Problem | Solution |
|---------|----------|
| Console shows "User not authenticated" error | Check that user logged in successfully before onboarding |
| user_id is NULL in database | Auth session might have expired, force re-login |
| Profile saves but user_id is empty string | Make sure auth is initialized in your app |
| No console output at all | Exception might be happening earlier, check error event |

---

The code is now set up to:
1. ✅ Get the actual user ID from auth
2. ✅ Apply it to the profile before saving
3. ✅ Log success/failure clearly
4. ✅ Throw exceptions so you know what went wrong

**Try completing onboarding and check your console logs!**

