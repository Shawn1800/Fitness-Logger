package com.ghostbug.heavyLifts.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostbug.heavyLifts.data.UseCase.OneRepMaxUseCase
import com.ghostbug.heavyLifts.data.domain.ExerciseEntity
import com.ghostbug.heavyLifts.data.domain.WorkoutEntryEntity
import com.ghostbug.heavyLifts.data.repository.OneRepMaxRepository
import com.ghostbug.heavyLifts.data.repository.WorkoutRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import java.time.LocalDate
import java.time.ZoneId

data class GroupedWorkout(
    val exercise: ExerciseEntity,
    val sets: List<WorkoutEntryEntity>,
    val changePercent: Double? = null
)

class HomeViewModel(
    private val workoutRepository: WorkoutRepository,
    private val oneRepMaxRepository: OneRepMaxRepository,
    private val oneRepMaxUseCase: OneRepMaxUseCase
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
                    _uiEvent.emit(
                        HomeUiEvent.NavigateToExerciseSelection(
                            dateMillis = _state.value.selectedDateMillis ?: System.currentTimeMillis() // ✅
                        )
                    )
                }
            }
            is HomeEvent.EditWorkout -> {
                viewModelScope.launch {
                    _uiEvent.emit(
                        HomeUiEvent.NavigateToLogWorkout(
                            exercise = event.exercise,
                            dateMillis = _state.value.selectedDateMillis ?: System.currentTimeMillis() // ✅
                        )
                    )
                }
            }
            is HomeEvent.RefreshWorkouts -> {
                // Refresh workouts for the currently selected date
                val dateMillis = _state.value.selectedDateMillis
                if (dateMillis != null) {
                    observeWorkoutsForDate(dateMillis)
                }
            }
        }
    }

    fun refresh() {
        val dateMillis = _state.value.selectedDateMillis
        if (dateMillis != null) {
            observeWorkoutsForDate(dateMillis)
        }
    }

    private fun observeWorkoutsForDate(dateMillis: Long) {
        workoutJob?.cancel()
        workoutJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val workoutsDeferred = async { workoutRepository.getWorkoutByDate(dateMillis) }
                val maxesDeferred = async { oneRepMaxRepository.getOneRepMaxForDate(dateMillis) }

                val workouts = workoutsDeferred.await()
                val oneRepMaxes = maxesDeferred.await()

                val oneRMMap = oneRepMaxes.associateBy { it.exerciseId }

                val grouped = workouts
                    .groupBy { it.exercise.id }
                    .map { (exerciseId, entries) ->
                        GroupedWorkout(
                            exercise = entries.first().exercise,
                            sets = entries.map { workoutWithExercise ->
                                WorkoutEntryEntity(
                                    id = workoutWithExercise.id,
                                    exerciseId = workoutWithExercise.exercise.id,
                                    weight = workoutWithExercise.weight.toFloat(),
                                    reps = workoutWithExercise.reps,
                                    sets = workoutWithExercise.sets,
                                    date = workoutWithExercise.date,
                                    userId = workoutWithExercise.userId
                                )
                            },
                            changePercent = oneRMMap[exerciseId]?.changePercent?.toDouble()
                        )
                    }

                _state.update { it.copy(workouts = grouped, isLoading = false) }

            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
