package com.ghostbug.heavyliftsapp.data.domain

import kotlin.time.Instant

data class UserProfileEntity(
    val userId:String,
    val userName: String,
    val age:Int?=null,
    val height :Float?=null,
    val userWeight: Float?=null,
    val gender: Gender ?=null,
    val city: String?=null,
    val country: String?=null,
    val profilePic: String?=null,
    val dob: Instant?=null,
    val accountCreated: Instant,
    val accountLastUpdated: Instant?=null,
)
enum class Gender{MALE , FEMALE ,OTHER , PREFER_NOT_TO_SAY}