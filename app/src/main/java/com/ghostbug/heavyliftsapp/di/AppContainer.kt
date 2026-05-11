package com.ghostbug.heavyliftsapp.di


import android.content.Context
import androidx.compose.runtime.remember
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ghostbug.heavyLifts.BuildConfig
import com.ghostbug.heavyliftsapp.data.HealthConnect.DailyActivitySyncWorker

import com.ghostbug.heavyliftsapp.data.UseCase.OneRepMaxUseCase
import com.ghostbug.heavyliftsapp.data.health.HealthConnectManager
import com.ghostbug.heavyliftsapp.data.health.RecordingApiManager
import com.ghostbug.heavyliftsapp.data.repository.AuthRepository
import com.ghostbug.heavyliftsapp.data.repository.AuthRepositoryImpl
import com.ghostbug.heavyliftsapp.data.repository.DailyActivityRepository
import com.ghostbug.heavyliftsapp.data.repository.DailyActivityRepositoryImpl


import com.ghostbug.heavyliftsapp.data.repository.ExerciseRepository
import com.ghostbug.heavyliftsapp.data.repository.ExerciseRepositoryImpl
import com.ghostbug.heavyliftsapp.data.repository.OneRepMaxRepository
import com.ghostbug.heavyliftsapp.data.repository.OneRepMaxRepositoryImpl
import com.ghostbug.heavyliftsapp.data.repository.UserProfileRepository
import com.ghostbug.heavyliftsapp.data.repository.UserProfileRepositoryImpl
import com.ghostbug.heavyliftsapp.data.repository.WorkoutRepository
import com.ghostbug.heavyliftsapp.data.repository.WorkoutRepositoryImpl
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AppContainer(private val context: Context) {
    init{
        scheduleDailySync(context)
    }

    // single supabase client
    val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ) {
        install(Auth)
        install(Postgrest)
//        install(Storage)
    }



    val healthConnectManager: HealthConnectManager by lazy {
        HealthConnectManager(context)
    }

    val recordingApiManager: RecordingApiManager by lazy {
        RecordingApiManager(context)
    }

    // repositories
    val userProfileRepository: UserProfileRepository by lazy {
        UserProfileRepositoryImpl(
            postgrest = supabase.postgrest,
            auth = supabase.auth
        )
    }

    val dailyActivityRepository: DailyActivityRepository by lazy {
        DailyActivityRepositoryImpl(
            postgrest = supabase.postgrest,
            auth = supabase.auth,
            healthConnectManager = healthConnectManager,
            recordingApiManager = recordingApiManager,
            userProfileRepository=userProfileRepository
        )
    }
    val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepositoryImpl(supabase.postgrest, supabase.auth)
    }

    val exerciseRepository: ExerciseRepository by lazy {
        ExerciseRepositoryImpl(supabase.postgrest, supabase.auth)
    }

    val oneRepMaxRepository: OneRepMaxRepository by lazy {
        OneRepMaxRepositoryImpl(supabase.postgrest, supabase.auth)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(auth = supabase.auth,supabase.postgrest)
    }

    val oneRepMaxUseCase: OneRepMaxUseCase by lazy {
        OneRepMaxUseCase(oneRepMaxRepository, workoutRepository)
    }

    fun scheduleDailySync(context: Context) {
        // calculate delay until next midnight
        val now = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 5) // 00:05 so data is fully settled
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_MONTH, 1) // already past midnight, schedule tomorrow
        }
        val initialDelay = midnight.timeInMillis - now.timeInMillis

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // only sync when online
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<DailyActivitySyncWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_activity_sync",          // unique name — prevents duplicate jobs
            ExistingPeriodicWorkPolicy.KEEP, // if already scheduled, don't replace it
            syncRequest
        )
    }
}
