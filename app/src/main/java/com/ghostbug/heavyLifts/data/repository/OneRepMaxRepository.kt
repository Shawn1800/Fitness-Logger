package com.ghostbug.heavyLifts.data.repository


import com.ghostbug.heavyLifts.data.domain.OneRepMaxEntity

import kotlinx.coroutines.flow.Flow

interface OneRepMaxRepository {
    suspend fun insert(entity: OneRepMaxEntity)

    suspend fun getChangePercentageForDate( exerciseId:Int,date:Long,): Double?

    suspend fun getOneRepMaxForDate(date: Long): List<OneRepMaxEntity>

    suspend fun getLatest(exerciseId: Int): OneRepMaxEntity?

    suspend fun getPrevious(exerciseId: Int, date: Long): OneRepMaxEntity?

    suspend fun deleteOneRepMax(exerciseId: Int,date: Long)
}