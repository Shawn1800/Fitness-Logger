package com.example.demo103.ui.theme.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demo103.data.entity.ExerciseEntity
import com.example.demo103.data.entity.WorkoutEntryEntity
import com.example.demo103.data.repository.WorkoutRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class GroupedWorkout(
    val exercise: ExerciseEntity,
    val sets: List<WorkoutEntryEntity>
)
class HomeViewModel(
    private val repository: WorkoutRepository
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

//            is HomeEvent.OnDeleteWorkout->{
//                viewModelScope.launch {
//                     repository.
//                }
//            }

        }
    }

    private fun observeWorkoutsForDate(dateMillis: Long) {
        workoutJob?.cancel()
        workoutJob = viewModelScope.launch {
            repository.getWorkoutByDate(dateMillis)
                .collect { workouts ->
                    val grouped = workouts
                        .groupBy { it.exercise.exerciseId }
                        .map { (_, entries) ->
                            GroupedWorkout(
                                exercise = entries.first().exercise,
                                sets = entries.map { it.workoutEntry }
                            )
                        }
                    _state.update { it.copy(workouts = grouped) }
                }
        }
    }
}