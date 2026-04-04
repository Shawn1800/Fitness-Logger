package com.ghostbug.heavyLifts.data.repository

import com.ghostbug.heavyLifts.data.domain.OneRepMaxEntity
import kotlinx.coroutines.flow.SharedFlow

interface OneRepMaxRepository {
    val updates: SharedFlow<Unit>

    suspend fun insert(entity: OneRepMaxEntity)

    suspend fun getChangePercentageForDate(exerciseId: Int, date: Long): Double?

    suspend fun getOneRepMaxForDate(date: Long): List<OneRepMaxEntity>

    suspend fun getLatest(exerciseId: Int): OneRepMaxEntity?

    suspend fun getPersonalBestBefore(exerciseId: Int, date: Long): OneRepMaxEntity?
    
    suspend fun getNext(exerciseId: Int, date: Long): OneRepMaxEntity?

    suspend fun deleteOneRepMax(exerciseId: Int, date: Long)
    
    suspend fun deleteByExercise(exerciseId: Int)
    
    suspend fun update(entity: OneRepMaxEntity)

    suspend fun getByDate(exerciseId: Int, date: Long): OneRepMaxEntity?
}
