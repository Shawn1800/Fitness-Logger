package com.ghostbug.heavyliftsapp.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostbug.heavyliftsapp.data.domain.ExerciseEntity
import com.ghostbug.heavyliftsapp.data.domain.WorkoutEntryEntity
import com.ghostbug.heavyliftsapp.data.repository.DailyActivityRepository
import com.ghostbug.heavyliftsapp.data.repository.OneRepMaxRepository
import com.ghostbug.heavyliftsapp.data.repository.WorkoutRepository
import com.ghostbug.heavyliftsapp.screens.home.HomeUiEvent.*
import com.ghostbug.heavyliftsapp.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.todayIn
import kotlin.time.Clock


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
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        onEvent(HomeEvent.OnDateSelected(today))
        loadActivityForDate(today)

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
                    .atStartOfDayIn(TimeZone.currentSystemDefault())
                    .toEpochMilliseconds()

                _state.update {
                    it.copy(
                        selectedDate = event.date,
                        selectedDateMillis = millis
                    )
                }
                observeWorkoutsForDate(millis)
                loadActivityForDate(event.date)
            }

            is HomeEvent.OnAddWorkoutClick -> {
                viewModelScope.launch {
                    _uiEvent.emit(
                        NavigateToExerciseSelection(
                            dateMillis = _state.value.selectedDateMillis ?: System.currentTimeMillis()
                        )
                    )
                }
            }

            is HomeEvent.EditWorkout -> {
                viewModelScope.launch {
                    _uiEvent.emit(
                        NavigateToLogWorkout(
                            exerciseId = event.exerciseId,
                            dateMillis = _state.value.selectedDateMillis ?: System.currentTimeMillis()
                        )
                    )
                }
            }

            is HomeEvent.RefreshWorkouts -> refresh()

            is HomeEvent.Message -> {
                viewModelScope.launch { _uiEvent.emit(ShowSnackbar(event.message)) }
            }

            // ON_RESUME refresh — reload activity for the currently selected date
            is HomeEvent.GetSteps -> loadActivityForDate(_state.value.selectedDate)

            is HomeEvent.OnStepsChanged -> loadActivityForDate(_state.value.selectedDate)
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
                supervisorScope {
                    val workoutsDeferred = async { workoutRepository.getWorkoutByDate(dateMillis) }
                    val maxesDeferred = async { oneRepMaxRepository.getOneRepMaxForDate(dateMillis) }

                    val workouts = try { workoutsDeferred.await() } catch (_: Exception) { emptyList() }
                    val oneRepMaxes = try { maxesDeferred.await() } catch (_: Exception) { emptyList() }

                    val oneRMMap = oneRepMaxes.associateBy { it.exerciseId }

                    val grouped = workouts
                        .groupBy { it.exercise.id }
                        .map { (exerciseId, entries) ->
                            GroupedWorkout(
                                exercise = entries.first().exercise,
                                sets = entries.map { w ->
                                    WorkoutEntryEntity(
                                        id = w.id,
                                        exerciseId = w.exercise.id,
                                        weight = w.weight.toFloat(),
                                        reps = w.reps,
                                        sets = w.sets,
                                        date = w.date,
                                        userId = w.userId
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

    // Today → live sensor data from Health Connect
    // Past date → historical data from Supabase
    private fun loadActivityForDate(date: LocalDate) {
        viewModelScope.launch {
            try {
                val activity = if (date == Clock.System.todayIn(TimeZone.currentSystemDefault())) {
                    dailyActivityRepository.getTodayActivity()
                } else {
                    dailyActivityRepository.getActivityByDate(date)
                }
                val goal = dailyActivityRepository.getCurrentGoal()
                val stepGoal = goal?.stepGoal ?: 10000

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
            } catch (_: Exception) { }
        }
    }
}
