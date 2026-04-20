package com.ghostbug.heavyliftsapp.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostbug.heavyliftsapp.data.UseCase.OneRepMaxUseCase
import com.ghostbug.heavyliftsapp.data.domain.ExerciseEntity
import com.ghostbug.heavyliftsapp.data.domain.WorkoutEntryEntity
import com.ghostbug.heavyliftsapp.data.repository.DailyActivityRepository
import com.ghostbug.heavyliftsapp.data.repository.OneRepMaxRepository
import com.ghostbug.heavyliftsapp.data.repository.WorkoutRepository
import com.ghostbug.heavyliftsapp.screens.home.HomeUiEvent.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.supervisorScope
import java.time.LocalDate
import java.time.ZoneId

data class GroupedWorkout(
    val exercise: ExerciseEntity,
    val sets: List<WorkoutEntryEntity>,
    val changePercent: Float? = null
)

class HomeViewModel(
    private val workoutRepository: WorkoutRepository,
    private val oneRepMaxRepository: OneRepMaxRepository,
    private val dailyActivityRepository: DailyActivityRepository,
) : ViewModel() {


    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<HomeUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var workoutJob: Job? = null

    init {
        val today = LocalDate.now()
        onEvent(HomeEvent.OnDateSelected(today))
        loadTodayActivity()
        
        viewModelScope.launch {
            oneRepMaxRepository.updates.collect {
                _state.value.selectedDateMillis?.let { observeWorkoutsForDate(it) }
            }
        }
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
                        NavigateToExerciseSelection(
                            dateMillis = _state.value.selectedDateMillis
                                ?: System.currentTimeMillis()
                        )
                    )
                }
            }

            is HomeEvent.EditWorkout -> {
                viewModelScope.launch {
                    _uiEvent.emit(
                        NavigateToLogWorkout(
                            exerciseId = event.exerciseId,
                            dateMillis = _state.value.selectedDateMillis
                                ?: System.currentTimeMillis()
                        )
                    )
                }
            }
            is HomeEvent.RefreshWorkouts -> {
                refresh()
            }
            is HomeEvent.message -> {
                viewModelScope.launch {
                    _uiEvent.emit(ShowSnackbar(event.message))
                }
            }
            is HomeEvent.getSteps->loadTodayActivity()
            is HomeEvent.onDateSelected -> TODO()
        }
    }

    fun refresh() {
        _state.value.selectedDateMillis?.let { observeWorkoutsForDate(it) }
    }

    private fun observeWorkoutsForDate(dateMillis: Long) {
        workoutJob?.cancel()
        workoutJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Use supervisorScope so that if one request fails, it doesn't crash the coroutine
                supervisorScope {
                    val workoutsDeferred = async { workoutRepository.getWorkoutByDate(dateMillis) }
                    val maxesDeferred = async { oneRepMaxRepository.getOneRepMaxForDate(dateMillis) }

                    val workouts = try { workoutsDeferred.await() } catch (e: Exception) { emptyList() }
                    val oneRepMaxes = try { maxesDeferred.await() } catch (e: Exception) { emptyList() }

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
                                changePercent = oneRMMap[exerciseId]?.changePercent?.toFloat()
                            )
                        }

                    _state.update { it.copy(workouts = grouped, isLoading = false) }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _state.update { it.copy(isLoading = false, errorMessage = "No internet connection or server error") }
            }
        }
    }

    private fun loadTodayActivity() {
        viewModelScope.launch {
            try {
                val activity = dailyActivityRepository.getTodayActivity()
                val goal = dailyActivityRepository.getCurrentGoal()
                val stepGoal = goal?.stepGoal ?: 10000
//                val stepByDate= dailyActivityRepository.getStepsByDate()

                _state.update {
                    it.copy(
                        todaySteps = activity.steps,
                        stepGoal = stepGoal,
                        stepProgress = (activity.steps.toFloat() / stepGoal).coerceIn(0f, 1f),
                        stepSource = activity.stepSource,
                        todayCalories = activity.caloriesBurned,
                        todayDistanceKm = activity.distanceKm
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
