package com.ghostbug.heavyliftsapp.data.HealthConnect

import kotlin.time.Clock



import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ghostbug.heavyliftsapp.di.HeavyLifts

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

class DailyActivitySyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val repository = (applicationContext as HeavyLifts)
                .appContainer
                .dailyActivityRepository

            // sync yesterday — today's data may still be changing
            val yesterday = Clock.System
                .todayIn(TimeZone.currentSystemDefault())
                .minus(1, DateTimeUnit.DAY)

            repository.syncToSupabase(yesterday)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // retry up to 3 times before giving up
            if (runAttemptCount < 3) Result.retry()
            else Result.failure()
        }
    }
}