package com.example.HeavyLifts.ui.screen.log_workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.HeavyLifts.data.UseCase.OneRepMaxUseCase
import com.example.HeavyLifts.data.repository.OneRepMaxRepository
import com.example.HeavyLifts.data.repository.WorkoutRepository

class LogWorkoutViewModelFactory (
    private val repository: WorkoutRepository,
    private val oneRepMaxRepository: OneRepMaxRepository,
    private val oneRepMaxUseCase: OneRepMaxUseCase
): ViewModelProvider.Factory
{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LogWorkoutViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LogWorkoutViewModel(repository, oneRepMaxRepository ,oneRepMaxUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

