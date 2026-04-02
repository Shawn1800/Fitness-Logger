package com.ghostbug.heavyLifts.data.remote.dto

import com.ghostbug.heavyLifts.data.domain.OneRepMaxEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class OneRepMaxEntityDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("exercise_id") val exerciseId: Int,
    @SerialName("curr_1rm") val curr1RM: Float,
    @SerialName("prev_1rm") val prev1RM: Float,
    @SerialName("change_percent") val changePercent: Float,
    @SerialName("date") val date: String,
    @SerialName("user_id") val userId: String
)

fun OneRepMaxEntityDto.toDomain(): OneRepMaxEntity {
    return OneRepMaxEntity(
        id = id ?: 0,
        exerciseId = exerciseId,
        curr1RM = curr1RM,
        prev1RM = prev1RM,
        changePercent = changePercent,
        date = Instant.parse(date).toEpochMilli(),
        userId = userId
    )
}

fun OneRepMaxEntity.toDto(): OneRepMaxEntityDto {
    return OneRepMaxEntityDto(
        id = if (id == 0) null else id,
        exerciseId = exerciseId,
        curr1RM = curr1RM,
        prev1RM = prev1RM,
        changePercent = changePercent,
        date = Instant.ofEpochMilli(date).toString(),
        userId = userId
    )
}
