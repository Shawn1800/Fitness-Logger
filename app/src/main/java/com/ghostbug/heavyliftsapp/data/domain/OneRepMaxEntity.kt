package com.ghostbug.heavyliftsapp.data.domain

import kotlinx.serialization.Serializable


data class OneRepMaxEntity(
    val id: Long=0L,
    val exerciseId: Long,
    val curr1RM: Float,
    val prev1RM: Float,
    val changePercent: Float,
    val date: Long,
    val userId: String
)
