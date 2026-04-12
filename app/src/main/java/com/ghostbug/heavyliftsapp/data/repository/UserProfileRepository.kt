package com.ghostbug.heavyliftsapp.data.repository

import com.ghostbug.heavyliftsapp.data.domain.Gender
import com.ghostbug.heavyliftsapp.data.domain.UserProfileEntity

interface UserProfileRepository {
    suspend fun getOwnProfile(): UserProfileEntity?
//    suspend fun getProfileById(userId:String): UserProfileEntity? // for leaderboards
    suspend fun upsertProfile(profile: UserProfileEntity)
    suspend fun isUserNameTaken(userName: String): Boolean

     fun getGoogleProfilePic(): String?
}