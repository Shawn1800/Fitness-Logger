package com.ghostbug.heavyliftsapp.data.repository

import com.ghostbug.heavyliftsapp.data.domain.Gender
import com.ghostbug.heavyliftsapp.data.domain.UserProfileEntity

interface UserProfileRepository {
    suspend fun getOwnProfile(): UserProfileEntity?
//    suspend fun getProfileById(userId:String): UserProfileEntity? // for leaderboards
    suspend fun upsertProfile(profile: UserProfileEntity)
    suspend fun deleteOwnProfile()
    suspend fun isUserNameTaken(userName: String): Boolean
    suspend fun isProfileComplete(): Boolean

     fun getGoogleProfilePic(): String?
}