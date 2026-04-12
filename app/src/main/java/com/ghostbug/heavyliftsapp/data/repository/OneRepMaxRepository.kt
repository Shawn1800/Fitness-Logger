package com.ghostbug.heavyliftsapp.data.repository

import com.ghostbug.heavyliftsapp.data.domain.OneRepMaxEntity
import kotlinx.coroutines.flow.SharedFlow

interface OneRepMaxRepository {
    val updates: SharedFlow<Unit>

    suspend fun insert(entity: OneRepMaxEntity)

    suspend fun getChangePercentageForDate(exerciseId: Long, date: Long): Double?

    suspend fun getOneRepMaxForDate(date: Long): List<OneRepMaxEntity>

    suspend fun getLatest(exerciseId: Long): OneRepMaxEntity?

    suspend fun getPersonalBestBefore(exerciseId: Long, date: Long): OneRepMaxEntity?
    
    suspend fun getNext(exerciseId: Long, date: Long): OneRepMaxEntity?

    suspend fun deleteOneRepMax(exerciseId: Long, date: Long)
    
    suspend fun deleteByExercise(exerciseId: Long)
    
    suspend fun update(entity: OneRepMaxEntity)

    suspend fun getByDate(exerciseId: Long, date: Long): OneRepMaxEntity?
}
