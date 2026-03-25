package com.example.demo103.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.demo103.data.UseCase.OneRepMaxUseCase
import com.example.demo103.data.repository.OneRepMaxRepository
import com.example.demo103.data.repository.WorkoutRepository

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