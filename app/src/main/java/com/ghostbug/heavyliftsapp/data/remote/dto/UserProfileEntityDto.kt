package com.ghostbug.heavyliftsapp.data.remote.dto

import com.ghostbug.heavyliftsapp.data.domain.UserProfileEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import com.ghostbug.heavyliftsapp.data.domain.Gender

@Serializable
data class UserProfileEntityDto(
    @SerialName("user_id")
    val userId:String,
    @SerialName("user_name")
    val userName: String,
    @SerialName("age" )
    val age:Int?=null,
    @SerialName("height")
    val height :Float?=null,
    @SerialName("user_weight")
    val userWeight: Float?=null,
    @SerialName("gender")
    val gender: Gender?=null,
    @SerialName("city")
    val city: String?=null,
    @SerialName("country")
    val country: String?=null,
    @SerialName("profile_pic")
    val profilePic: String?=null,
    @SerialName("dob")
    val dob: Instant?=null,
    @SerialName("account_created")
    val accountCreated: Instant,
    @SerialName("account_last_updated")
    val accountLastUpdated: Instant?=null,
)

fun UserProfileEntityDto.toDomain(): UserProfileEntity {
    return UserProfileEntity(
        userId=userId,
        userName = userName,
        age = age,
        height = height,
        userWeight = userWeight,
        gender = gender,
        city = city,
        country = country,
        profilePic = profilePic,
        dob = dob,
        accountCreated = accountCreated,
        accountLastUpdated = accountLastUpdated,
    )
}

fun UserProfileEntity.toDto(): UserProfileEntityDto{
    return UserProfileEntityDto(
        userId=userId,
        userName = userName,
        age = age,
        height = height,
        userWeight = userWeight,
        gender = gender,
        city = city,
        country = country,
        profilePic = profilePic,
        dob = dob,
        accountCreated = accountCreated,
        accountLastUpdated = accountLastUpdated,
    )
}