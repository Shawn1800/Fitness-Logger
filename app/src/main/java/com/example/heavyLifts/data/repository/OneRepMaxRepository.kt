package com.example.heavyLifts.data.repository

import com.example.heavyLifts.data.dao.OneRepMaxEntityDao
import com.example.heavyLifts.data.dao.WorkoutEntryEntityDao
import com.example.heavyLifts.data.entity.OneRepMaxEntity
import kotlinx.coroutines.flow.Flow

class OneRepMaxRepository(
    private val oneRepMaxEntityDao: OneRepMaxEntityDao,
    private val workoutEntryEntityDao: WorkoutEntryEntityDao
) {

    suspend fun insert(entity: OneRepMaxEntity){
        oneRepMaxEntityDao.insert(entity)
    }
    suspend fun getChangePercentageForDate( exerciseId:Int,date:Long,): Double? {
       return  oneRepMaxEntityDao.getChangePercentageForDate(exerciseId,date)
    }

    fun getOneRepMaxForDate(date: Long): Flow<List<OneRepMaxEntity>> {
        return oneRepMaxEntityDao.getOneRepMaxForDate(date)
    }

    suspend fun getLatest(exerciseId: Int): OneRepMaxEntity?{
        return oneRepMaxEntityDao.getLatest(exerciseId)
    }

    suspend fun getPrevious(exerciseId: Int, date: Long): OneRepMaxEntity? {
        return oneRepMaxEntityDao.getPrevious(exerciseId, date)
    }

    suspend fun deleteOneRepMax(exerciseId: Int,date: Long){
        oneRepMaxEntityDao.deleteOneRepMax(exerciseId,date)
    }

}