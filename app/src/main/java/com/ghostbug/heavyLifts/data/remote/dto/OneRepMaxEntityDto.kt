package com.ghostbug.heavyLifts.data.remote.dto

import com.ghostbug.heavyLifts.data.domain.OneRepMaxEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

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
    val parsedDate = if (date.contains("T")) {
        OffsetDateTime.parse(date).toLocalDate()
    } else {
        LocalDate.parse(date)
    }
    
    return OneRepMaxEntity(
        id = id ?: 0,
        exerciseId = exerciseId,
        curr1RM = curr1RM,
        prev1RM = prev1RM,
        changePercent = changePercent,
        // ✅ parse YYYY-MM-DD or ISO timestamp as local date, convert to millis
        date = parsedDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli(),
        userId = userId
    )
}

fun OneRepMaxEntity.toDto(): OneRepMaxEntityDto {
    val isoDate = Instant.ofEpochMilli(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .atTime(12, 0)                        // ✅ noon, safe from boundary issues
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toString()                            // → "2026-04-02T06:30:00Z"
    return OneRepMaxEntityDto(
        id = if (id == 0) null else id,
        exerciseId = exerciseId,
        curr1RM = curr1RM,
        prev1RM = prev1RM,
        changePercent = changePercent,
        date = isoDate,
        userId = userId
    )
}