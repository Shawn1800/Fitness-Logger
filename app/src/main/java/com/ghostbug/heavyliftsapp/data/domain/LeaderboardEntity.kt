package com.ghostbug.heavyliftsapp.data.domain

import kotlin.time.Instant



data class LeaderboardEntity(
    val id: String?=null,
    val ownerId: String,
    val name: String,
    val createdAt: Instant?=null
)
