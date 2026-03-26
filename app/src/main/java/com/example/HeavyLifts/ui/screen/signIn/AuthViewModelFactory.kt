package com.example.demo103.ui.screen.signIn

import com.example.demo103.ui.screen.log_workout.LogWorkoutViewModel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.demo103.data.UseCase.OneRepMaxUseCase
import com.example.demo103.data.repository.AuthRepository
import com.example.demo103.data.repository.OneRepMaxRepository
import com.example.demo103.data.repository.WorkoutRepository

class AuthViewModelFactory (
    private val authRepository: AuthRepository
): ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}