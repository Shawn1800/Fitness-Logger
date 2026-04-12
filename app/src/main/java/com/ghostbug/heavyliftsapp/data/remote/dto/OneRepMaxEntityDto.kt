package com.ghostbug.heavyliftsapp.data.remote.dto

import com.ghostbug.heavyliftsapp.data.domain.OneRepMaxEntity
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class OneRepMaxEntityDto(
    @SerialName("id") val id: Long?=null,
    @SerialName("exercise_id") val exerciseId: Long,
    @SerialName("curr_1rm") val curr1RM: Float,
    @SerialName("prev_1rm") val prev1RM: Float,
    @SerialName("change_percent") val changePercent: Float,
    @SerialName("date") val date: Instant,
    @SerialName("user_id") val userId: String
)

fun OneRepMaxEntityDto.toDomain(): OneRepMaxEntity {

    return OneRepMaxEntity(
        id = id?:0L ,
        exerciseId = exerciseId,
        curr1RM = curr1RM,
        prev1RM = prev1RM,
        changePercent = changePercent,
        date = date.toEpochMilliseconds(),
        userId = userId
    )
}

fun OneRepMaxEntity.toDto(): OneRepMaxEntityDto {
    return OneRepMaxEntityDto(
        id = if (id == 0L)null else id,
        exerciseId = exerciseId,
        curr1RM = curr1RM,
        prev1RM = prev1RM,
        changePercent = changePercent,
        date = Instant.fromEpochMilliseconds(date),
        userId = userId
    )
}