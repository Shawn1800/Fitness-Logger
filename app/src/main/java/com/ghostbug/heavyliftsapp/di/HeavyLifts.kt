package com.ghostbug.heavyliftsapp.di

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

import com.ghostbug.heavyliftsapp.data.health.RecordingApiManager
import com.ghostbug.heavyliftsapp.data.repository.DailyActivityRepository
import com.ghostbug.heavyliftsapp.data.repository.DailyActivityRepositoryImpl
import com.ghostbug.heavyliftsapp.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HeavyLifts : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)

        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    RecordingApiManager(this@HeavyLifts).subscribe()
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
    }


}