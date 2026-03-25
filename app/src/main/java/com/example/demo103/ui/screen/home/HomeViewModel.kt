package com.example.demo103.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demo103.data.UseCase.OneRepMaxUseCase
import com.example.demo103.data.entity.ExerciseEntity
import com.example.demo103.data.entity.WorkoutEntryEntity
import com.example.demo103.data.repository.OneRepMaxRepository
import com.example.demo103.data.repository.WorkoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class GroupedWorkout(
    val exercise: ExerciseEntity,
    val sets: List<WorkoutEntryEntity>,
    val changePercent:Double? =null
)
class HomeViewModel(
    private val repository: WorkoutRepository,
    private val oneRepMaxRepository: OneRepMaxRepository,
    private val oneRepMaxUseCase : OneRepMaxUseCase

) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<HomeUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var workoutJob: Job? = null

    init {
        onEvent(HomeEvent.OnDateSelected(LocalDate.now()))
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnDateSelected -> {
                val millis = event.date
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                _state.update {
                    it.copy(
                        selectedDate = event.date,
                        selectedDateMillis = millis
                    )
                }
                observeWorkoutsForDate(millis)
            }

            is HomeEvent.OnAddWorkoutClick -> {
                viewModelScope.launch {
                    _uiEvent.emit(HomeUiEvent.NavigateToExerciseSelection)
                }
            }
             is HomeEvent.EditWorkout->{
                 viewModelScope.launch {
                     _uiEvent.emit(HomeUiEvent.NavigateToLogWorkout(event.exercise))
                 }
             }
        }
    }

    private fun observeWorkoutsForDate(dateMillis: Long) {
        workoutJob?.cancel()
        workoutJob = viewModelScope.launch {
            combine(
                repository.getWorkoutByDate(dateMillis),
                oneRepMaxRepository.getOneRepMaxForDate(dateMillis)
            ) { workouts, oneRepMaxes ->
                val oneRMMap = oneRepMaxes.associateBy { it.exerciseId }
                
                workouts
                    .groupBy { it.exercise.exerciseId }
                    .map { (exerciseId, entries) ->
                        GroupedWorkout(
                            exercise = entries.first().exercise,
                            sets = entries.map { it.workoutEntry },
                            changePercent = oneRMMap[exerciseId]?.changePercent
                        )
                    }
            }
            .flowOn(Dispatchers.IO)
            .collect { grouped ->
                _state.update { it.copy(workouts = grouped) }
            }
        }
    }
}