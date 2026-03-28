package com.example.heavyLifts.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.heavyLifts.data.UseCase.OneRepMaxUseCase
import com.example.heavyLifts.data.repository.OneRepMaxRepository
import com.example.heavyLifts.data.repository.WorkoutRepository

class HomeViewModelFactory(
    private val repository: WorkoutRepository,
    private val oneRepMaxRepository: OneRepMaxRepository,
    private val oneRepMaxUseCase: OneRepMaxUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, oneRepMaxRepository ,oneRepMaxUseCase ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}