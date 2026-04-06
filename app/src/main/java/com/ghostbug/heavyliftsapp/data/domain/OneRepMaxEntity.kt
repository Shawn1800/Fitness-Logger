package com.ghostbug.heavyliftsapp.data.domain

import kotlinx.serialization.Serializable

@Serializable
data class OneRepMaxEntity(
    val id: Int,
    val exerciseId: Int,
    val curr1RM: Float,
    val prev1RM: Float,
    val changePercent: Float,
    val date: Long,
    val userId: String
)
