package com.ghostbug.heavyliftsapp.data.repository

import com.ghostbug.heavyliftsapp.data.domain.ActivityGoals
import com.ghostbug.heavyliftsapp.data.domain.DailyActivity
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

interface  DailyActivityRepository {

 // today's data — reads from HC or Recording API
 suspend fun getTodayActivity(): DailyActivity

 // history — reads from Supabase
 suspend fun getActivityHistory(days: Int): List<DailyActivity>

 // sync today's data to Supabase — called by WorkManager
 suspend fun syncToSupabase(date: LocalDate)

 // goals
 suspend fun getCurrentGoal(): ActivityGoals?
 suspend fun saveGoal(stepGoal: Int, calorieGoal: Float)

 suspend fun getStepsByDate(steps:Long,createdAt: Instant): DailyActivity


}