# 🔴 CRITICAL ISSUE: User Profile Not Saving to Database

## Problem Analysis

Your `user_profile` table is not receiving any data from the app. I identified **3 critical issues**:

---

## 🔴 ISSUE #1: Missing Email Field (MOST CRITICAL)

**Location:** `UserProfileViewModel.kt` line 177-186

**The Problem:**
```kotlin
val profile = UserProfileEntity(
    userId = "", 
    userName = currentState.userName,
    // ... other fields ...
    dob = currentState.dob,
    // ❌ MISSING: email field!
    isPremiumUser = currentState.isPremiumUser,
    accountCreated = accountCreatedTime,
    accountLastUpdated = now
)
```

**Why it fails:**
- The `UserProfileEntity` constructor requires an `email` parameter (it's NOT optional)
- You're not passing it, so the object creation likely fails
- Check your `UserProfileEntity.kt` - the email field is: `val email : String` (non-nullable)

**Current Code Shows:**
```kotlin
val email = "",  // ❌ You're setting it to empty string!
```

But wait - I see the DTO expects email too. Let me check if it's in the ViewModel...

Actually, I see the issue now - **you're missing the email line entirely!**

---

## 🔴 ISSUE #2: Incorrect DTO Serialization in Screen3

**Location:** `UserProfileScreen3.kt` line 185-187

**The Problem:**
```kotlin
Button(
    onClick = {
        viewModel.onEvent(UserProfileEvent.OnSubmitProfile)
        onNext()  // ❌ This navigates BEFORE the profile is saved!
    },
```

**Why it fails:**
- You call `onNext()` immediately AFTER emitting the event
- The actual save happens asynchronously in `viewModelScope.launch`
- `onNext()` executes before the data is actually saved
- Navigation happens before error handling can occur

---

## 🔴 ISSUE #3: Silent Exception Swallowing

**Location:** `UserProfileRepositoryImpl.kt` line 48-54

**The Problem:**
```kotlin
override suspend fun upsertProfile(profile: UserProfileEntity) {
    val uid = currentUserId ?: return  // ❌ Silent return if no user!
    val dto = profile.toDto().copy(userId = uid)
    withContext(Dispatchers.IO) {
        try {
            postgrest.from(TABLE_USER_PROFILE)
                .upsert(dto) {
                    onConflict = "user_id"
                }
        } catch (e: Exception) {
            println("Error upserting profile: ${e.message}")  // ❌ Only printed to console!
        }
    }
}
```

**Why it fails:**
- If `currentUserId` is null, the function silently returns
- Exceptions are caught but only logged to console
- No feedback to the UI that save failed
- Auth might not be ready yet when profile is being saved

---

## ✅ SOLUTIONS

### Fix #1: Add Missing Email Field to ViewModel

```kotlin
val profile = UserProfileEntity(
    userId = "",
    userName = currentState.userName,
    age = currentState.age,
    height = currentState.height,
    userWeight = currentState.userWeight,
    gender = currentState.gender,
    city = currentState.city,
    country = currentState.country,
    profilePic = currentState.profilePic,
    dob = currentState.dob,
    email = auth.currentUserOrNull()?.email ?: "",  // ✅ ADD THIS!
    isPremiumUser = currentState.isPremiumUser,
    accountCreated = accountCreatedTime,
    accountLastUpdated = now
)
```

---

### Fix #2: Wait for Save Before Navigation in Screen3

Change from:
```kotlin
Button(
    onClick = {
        viewModel.onEvent(UserProfileEvent.OnSubmitProfile)
        onNext()  // ❌ Wrong - navigates immediately
    },
```

To:
```kotlin
Button(
    onClick = {
        viewModel.onEvent(UserProfileEvent.OnSubmitProfile)
        // ✅ Don't call onNext() here - let the ViewModel handle it
        // The NavToHome event will trigger navigation
    },
```

---

### Fix #3: Add Error Handling to Repository

Wrap the upsert in a Result type or throw exceptions:

```kotlin
override suspend fun upsertProfile(profile: UserProfileEntity) {
    val uid = currentUserId ?: throw Exception("User not authenticated")
    
    val dto = profile.toDto().copy(userId = uid)
    withContext(Dispatchers.IO) {
        try {
            postgrest.from(TABLE_USER_PROFILE)
                .upsert(dto) {
                    onConflict = "user_id"
                }
            println("✅ Profile saved successfully for user: $uid")
        } catch (e: Exception) {
            println("❌ Error upserting profile: ${e.message}")
            e.printStackTrace()
            throw e  // ✅ Re-throw so ViewModel can catch it
        }
    }
}
```

---

### Fix #4: Update ViewModel to Handle Auth

In the `handleSubmitProfile()` function, add:

```kotlin
private fun handleSubmitProfile() {
    viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        
        try {
            val currentState = _state.value
            
            if (currentState.userName.isBlank() || currentState.userNameError != null) {
                _state.update { it.copy(userNameError = "Username is required to continue") }
                return@launch
            }

            val existingProfile = repository.getOwnProfile()
            val now = Clock.System.now()
            val accountCreatedTime = existingProfile?.accountCreated ?: now

            val profile = UserProfileEntity(
                userId = "",
                userName = currentState.userName,
                age = currentState.age,
                height = currentState.height,
                userWeight = currentState.userWeight,
                gender = currentState.gender,
                city = currentState.city,
                country = currentState.country,
                profilePic = currentState.profilePic,
                dob = currentState.dob,
                email = "",  // ✅ Make sure this is set!
                isPremiumUser = currentState.isPremiumUser,
                accountCreated = accountCreatedTime,
                accountLastUpdated = now
            )

            repository.upsertProfile(profile)
            _state.update { it.copy(isLoading = false) }
            _uiEvent.emit(UserProfileUiEvent.NavToHome)
            
        } catch (e: Exception) {
            println("❌ Save failed: ${e.message}")
            e.printStackTrace()
            _state.update { it.copy(isLoading = false) }
            // Emit error event if you have one
        }
    }
}
```

---

## 🔍 Debugging Checklist

Before applying fixes, check these:

1. **Check Supabase Console:**
   - Go to SQL Editor
   - Run: `SELECT * FROM user_profile;`
   - Is it completely empty?

2. **Check Auth Status:**
   - Is `auth.currentUserOrNull()?.id` actually returning a user ID?
   - Or is it null when you submit?

3. **Check Console Output:**
   - Look for the error message from the catch block
   - Add breakpoints and debug

4. **Check Supabase RLS Policies:**
   - Go to user_profile table → Auth policies
   - Is there a policy allowing inserts for authenticated users?
   - Check the policy on INSERT for the user_id column

5. **Check Supabase Table Definition:**
   - Is `user_id` the primary key?
   - Is it a foreign key reference to `auth.users(id)`?
   - Are all fields nullable except `user_id`?

---

## 📋 Summary

| Issue | Cause | Solution |
|-------|-------|----------|
| **Missing Email** | Not passed to entity | Add email field to profile creation |
| **Early Navigation** | onNext() called before async save | Let ViewModel handle navigation |
| **Silent Failures** | Exceptions caught but not re-thrown | Re-throw or return Result |
| **Auth Timing** | User ID might not be available | Check currentUserId is not null |

---

## 🚨 NEXT STEPS

Apply the fixes in this order:
1. Fix the ViewModel to include email and proper error handling
2. Fix Screen3 to not call onNext() directly
3. Fix Repository to re-throw exceptions
4. Test with Supabase console to verify data is inserted

