package com.ghostbug.heavyliftsapp.data.repository

import com.ghostbug.heavyliftsapp.data.domain.UserProfileEntity
import com.ghostbug.heavyliftsapp.data.remote.dto.UserProfileEntityDto
import com.ghostbug.heavyliftsapp.data.remote.dto.toDomain
import com.ghostbug.heavyliftsapp.data.remote.dto.toDto
import com.ghostbug.heavyliftsapp.supabase
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class UserProfileRepositoryImpl(
    private val postgrest: Postgrest,
    private val auth: Auth
) : UserProfileRepository {
    private companion object {
        const val TABLE_USER_PROFILE = "user_profile"
    }

    private val currentUserId: String?
        get() = auth.currentUserOrNull()?.id

    override suspend fun getOwnProfile(): UserProfileEntity? {
        val uid = currentUserId ?: return null
        try {
            return withContext(Dispatchers.IO) {
                postgrest.from(TABLE_USER_PROFILE)
                    .select {
                        filter {
                            eq("user_id", uid)
                        }
                    }
                    .decodeList<UserProfileEntityDto>()
                    .firstOrNull()
                    ?.toDomain()
            }
        } catch (e: Exception) {
            return null
        }
    }

    override suspend fun upsertProfile(profile: UserProfileEntity) {
        val uid = currentUserId ?: throw Exception("Not authenticated")
        val dto = profile.toDto().copy(userId = uid)
        withContext(Dispatchers.IO) {
            val exists = postgrest.from(TABLE_USER_PROFILE)
                .select { filter { eq("user_id", uid) } }
                .decodeList<UserProfileEntityDto>()
                .isNotEmpty()

            if (exists) {
                postgrest.from(TABLE_USER_PROFILE)
                    .update(dto) {
                        filter { eq("user_id", uid) }
                    }
            } else {
                postgrest.from(TABLE_USER_PROFILE)
                    .insert(dto)
            }
        }
    }

    override suspend fun deleteOwnProfile() {
        val uid = currentUserId ?: return
        withContext(Dispatchers.IO) {
            postgrest.from(TABLE_USER_PROFILE).delete {
                filter { eq("user_id", uid) }
            }
        }
    }

    override suspend fun isUserNameTaken(userName: String): Boolean {
        val uid = currentUserId ?: return false
        return withContext(Dispatchers.IO) {
            try {
                val result = postgrest.from(TABLE_USER_PROFILE)
                    .select {
                        filter {
                            eq("user_name", userName)
                            neq("user_id", uid)
                        }
                    }
                    .decodeList<UserProfileEntityDto>()

                // If the list is not empty, someone else has this name
                result.isNotEmpty()

            } catch (e: Exception) {
                println("Error checking username: ${e.message}")
                true
            }
        }
    }

    override fun getGoogleProfilePic(): String? {
        val user = supabase.auth.currentUserOrNull()

        // 2. Access the user metadata
        val metadata = user?.userMetadata

        // 3. Extract the avatar URL (Supabase normalizes this to 'avatar_url')
        val avatarUrl = metadata?.get("avatar_url")?.jsonPrimitive?.contentOrNull

        // Fallback to 'picture' if 'avatar_url' is somehow null
        val pictureUrl = metadata?.get("picture")?.jsonPrimitive?.contentOrNull

        return avatarUrl ?: pictureUrl
    }
}