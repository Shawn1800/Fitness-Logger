package com.ghostbug.heavyliftsapp.screens.log_workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ghostbug.heavyliftsapp.data.UseCase.OneRepMaxUseCase
import com.ghostbug.heavyliftsapp.data.repository.ExerciseRepository
import com.ghostbug.heavyliftsapp.data.repository.OneRepMaxRepository
import com.ghostbug.heavyliftsapp.data.repository.WorkoutRepository

class LogWorkoutViewModelFactory (
    private val repository: WorkoutRepository,
    private val oneRepMaxRepository: OneRepMaxRepository,
    private val oneRepMaxUseCase: OneRepMaxUseCase,
    private val exerciseRepository: ExerciseRepository
): ViewModelProvider.Factory
{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LogWorkoutViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LogWorkoutViewModel(repository, oneRepMaxRepository, oneRepMaxUseCase, exerciseRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

