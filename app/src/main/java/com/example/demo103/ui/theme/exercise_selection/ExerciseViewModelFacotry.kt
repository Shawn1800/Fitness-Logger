package com.example.demo103.ui.theme.exercise_selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.demo103.data.repository.ExerciseRepository
import com.example.demo103.data.repository.WorkoutRepository

//A Factory helps recreate the ViewModel with the same parameters
// after a configuration change, ensuring seamless user experience

class ExerciseViewModelFactory (
    private val repository: ExerciseRepository
    , private val workoutRepository: WorkoutRepository)
    : ViewModelProvider.Factory
{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseViewModel(repository, workoutRepository  ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}