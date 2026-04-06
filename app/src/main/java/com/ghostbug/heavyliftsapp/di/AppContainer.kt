package com.ghostbug.heavyliftsapp.di


import com.ghostbug.heavyLifts.BuildConfig
import com.ghostbug.heavyliftsapp.data.UseCase.OneRepMaxUseCase
import com.ghostbug.heavyliftsapp.data.repository.AuthRepository
import com.ghostbug.heavyliftsapp.data.repository.AuthRepositoryImpl


import com.ghostbug.heavyliftsapp.data.repository.ExerciseRepository
import com.ghostbug.heavyliftsapp.data.repository.ExerciseRepositoryImpl
import com.ghostbug.heavyliftsapp.data.repository.OneRepMaxRepository
import com.ghostbug.heavyliftsapp.data.repository.OneRepMaxRepositoryImpl
import com.ghostbug.heavyliftsapp.data.repository.WorkoutRepository
import com.ghostbug.heavyliftsapp.data.repository.WorkoutRepositoryImpl
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

class AppContainer {

    // single supabase client
    val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ) {
        install(Auth)
        install(Postgrest)
//        install(Storage)
    }


    @Suppress("unused")
    val authRepository: AuthRepository = AuthRepositoryImpl(supabase.auth)
    @Suppress("unused")
    val exerciseRepository: ExerciseRepository = ExerciseRepositoryImpl(supabase.postgrest, supabase.auth)
    val workoutRepository: WorkoutRepository = WorkoutRepositoryImpl(supabase.postgrest, supabase.auth)
    val oneRepMaxRepository: OneRepMaxRepository = OneRepMaxRepositoryImpl(supabase.postgrest, supabase.auth)
    @Suppress("unused")
    val oneRepMaxUseCase = OneRepMaxUseCase(oneRepMaxRepository, workoutRepository)
}
