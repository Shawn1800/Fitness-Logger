package com.ghostbug.heavyLifts.di


import android.content.Context
import com.ghostbug.heavyLifts.BuildConfig
import com.ghostbug.heavyLifts.data.UseCase.OneRepMaxUseCase
import com.ghostbug.heavyLifts.data.repository.AuthRepository
import com.ghostbug.heavyLifts.data.repository.AuthRepositoryImpl


import com.ghostbug.heavyLifts.data.repository.ExerciseRepository
import com.ghostbug.heavyLifts.data.repository.ExerciseRepositoryImpl
import com.ghostbug.heavyLifts.data.repository.OneRepMaxRepository
import com.ghostbug.heavyLifts.data.repository.OneRepMaxRepositoryImpl
import com.ghostbug.heavyLifts.data.repository.WorkoutRepository
import com.ghostbug.heavyLifts.data.repository.WorkoutRepositoryImpl
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

class AppContainer(context: Context) {

    // single supabase client
    val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ) {
        install(Auth)
        install(Postgrest)
//        install(Storage)
    }


    val authRepository: AuthRepository = AuthRepositoryImpl(supabase.auth)
    val exerciseRepository: ExerciseRepository = ExerciseRepositoryImpl(supabase.postgrest, supabase.auth)
    val workoutRepository: WorkoutRepository = WorkoutRepositoryImpl(supabase.postgrest, supabase.auth)
    val oneRepMaxRepository: OneRepMaxRepository = OneRepMaxRepositoryImpl(supabase.postgrest, supabase.auth)
    val oneRepMaxUseCase = OneRepMaxUseCase(oneRepMaxRepository, workoutRepository)
}
