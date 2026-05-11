package com.ghostbug.heavyliftsapp.data.remote.dto

import com.ghostbug.heavyliftsapp.data.domain.LeaderboardEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
@Serializable
data class LeaderboardEntityDto (
    @SerialName("id")val id: String?=null,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("name")val name: String,
    @SerialName("created_at")val createdAt: Instant?=null
)

fun LeaderboardEntityDto.toDomain(): LeaderboardEntity{

    return LeaderboardEntity(
        id = id,
        ownerId=ownerId,
        name=name,
        createdAt= createdAt
    )
}

fun LeaderboardEntity.toDto(): LeaderboardEntityDto {
    return LeaderboardEntityDto (
        id = id,
        ownerId = ownerId,
        name = name,
        createdAt = createdAt
    )
}