package com.ghostbug.heavyliftsapp.screens.step_tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ghostbug.heavyliftsapp.data.repository.DailyActivityRepository



class StepTrackerViewModelFactory (
    private val repository: DailyActivityRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StepTrackerViewModel(repository) as T
        }
    }
