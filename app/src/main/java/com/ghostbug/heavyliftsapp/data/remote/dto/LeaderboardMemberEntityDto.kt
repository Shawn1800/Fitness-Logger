package com.ghostbug.heavyliftsapp.data.remote.dto


import kotlinx.serialization.Serializable
import kotlin.String
import kotlin.time.Instant
import com.ghostbug.heavyliftsapp.data.domain.LeaderboardMemberEntity
import kotlinx.serialization.SerialName

@Serializable
data class LeaderboardMemberEntityDto (
    @SerialName("id")val id:String?=null  ,
    @SerialName("leaderboard_id")val leaderboardId:String,
    @SerialName("user_id")val userId:String ,
    @SerialName("status")val status:String,
    @SerialName("joined_at")val joinedAt: Instant?=null
    )

fun LeaderboardMemberEntityDto.toDomain(): LeaderboardMemberEntity {
    return LeaderboardMemberEntity(
        id = id,
        leaderboardId = leaderboardId,
        userId = userId,
        status = status,
        joinedAt = joinedAt
    )
}

fun LeaderboardMemberEntity.toDto(): LeaderboardMemberEntityDto {
    return LeaderboardMemberEntityDto(
        id = id,
        leaderboardId = leaderboardId,
        userId = userId,
        status = status,
        joinedAt = joinedAt
    )
}