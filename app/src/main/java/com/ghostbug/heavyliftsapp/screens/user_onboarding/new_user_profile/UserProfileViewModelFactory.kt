package com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ghostbug.heavyliftsapp.data.UseCase.OneRepMaxUseCase
import com.ghostbug.heavyliftsapp.data.repository.AuthRepository
import com.ghostbug.heavyliftsapp.data.repository.ExerciseRepository
import com.ghostbug.heavyliftsapp.data.repository.OneRepMaxRepository

import com.ghostbug.heavyliftsapp.data.health.HealthConnectManager
import com.ghostbug.heavyliftsapp.data.repository.UserProfileRepository
import com.ghostbug.heavyliftsapp.data.repository.WorkoutRepository
import com.ghostbug.heavyliftsapp.screens.log_workout.LogWorkoutViewModel
import com.ghostbug.heavyliftsapp.screens.signUp.SignUpViewModel

class UserProfileViewModelFactory (
    private val repository: UserProfileRepository,
    private val healthConnectManager: HealthConnectManager
): ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserProfileViewModel(repository, healthConnectManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}