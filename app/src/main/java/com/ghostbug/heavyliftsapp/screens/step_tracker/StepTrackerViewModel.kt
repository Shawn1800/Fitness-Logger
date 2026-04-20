package com.ghostbug.heavyliftsapp.screens.step_tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostbug.heavyliftsapp.data.repository.DailyActivityRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



class StepTrackerViewModel(
    private val repository: DailyActivityRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StepTrackerState())
    val state: StateFlow<StepTrackerState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<StepTrackerUiEvent>()
    val uiEvent: SharedFlow<StepTrackerUiEvent> = _uiEvent.asSharedFlow()

    init {
        onEvent(StepTrackerEvent.LoadData)
    }

    fun onEvent(event: StepTrackerEvent) {
        when (event) {
            StepTrackerEvent.LoadData -> loadData()
            StepTrackerEvent.RefreshToday -> refreshToday()
            StepTrackerEvent.OpenGoalEditor -> openGoalEditor()
            StepTrackerEvent.CloseGoalEditor -> closeGoalEditor()
            StepTrackerEvent.SaveGoal -> saveGoal()
            is StepTrackerEvent.OnStepGoalChanged ->
                _state.update { it.copy(editingStepGoal = event.value) }
            is StepTrackerEvent.OnCalorieGoalChanged ->
                _state.update { it.copy(editingCalorieGoal = event.value) }
            is StepTrackerEvent.LoadHistory -> loadHistory(event.days)
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // load in parallel
                val todayDeferred = launch { refreshToday() }
                val goalDeferred = launch { loadGoal() }
                val historyDeferred = launch { loadHistory(7) }

                todayDeferred.join()
                goalDeferred.join()
                historyDeferred.join()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to load data") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun refreshToday() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true) }
            try {
                val today = repository.getTodayActivity()
                val stepGoal = _state.value.stepGoal
                val calorieGoal = _state.value.calorieGoal

                _state.update {
                    it.copy(
                        todaySteps = today.steps,
                        todayCalories = today.caloriesBurned,
                        todayDistanceKm = today.distanceKm,
                        stepSource = today.stepSource,
                        calorieSource = today.calorieSource,
                        stepProgress = (today.steps.toFloat() / stepGoal)
                            .coerceIn(0f, 1f),
                        calorieProgress = (today.caloriesBurned / calorieGoal)
                            .coerceIn(0f, 1f),
                        isSyncing = false
                    )
                }

                // Persist today's data to Supabase in the background
                launch {
                    try { repository.syncToSupabase(today.date) } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                _state.update { it.copy(isSyncing = false) }
            }
        }
    }

    private fun loadGoal() {
        viewModelScope.launch {
            try {
                val goal = repository.getCurrentGoal()
                if (goal != null) {
                    _state.update {
                        it.copy(
                            stepGoal = goal.stepGoal ?: 10000,
                            calorieGoal = goal.calorieGoal ?: 500f,
                            // recalculate progress with loaded goal
                            stepProgress = (it.todaySteps.toFloat() / (goal.stepGoal ?: 10000))
                                .coerceIn(0f, 1f),
                            calorieProgress = (it.todayCalories / (goal.calorieGoal ?: 500f))
                                .coerceIn(0f, 1f)
                        )
                    }
                }
            } catch (e: Exception) {
                // use default goals if fetch fails
            }
        }
    }

    private fun loadHistory(days: Int) {
        viewModelScope.launch {
            try {
                val history = repository.getActivityHistory(days)
                _state.update { it.copy(history = history) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to load history") }
            }
        }
    }

    private fun openGoalEditor() {
        _state.update {
            it.copy(
                isEditingGoal = true,
                editingStepGoal = it.stepGoal.toString(),
                editingCalorieGoal = it.calorieGoal.toInt().toString()
            )
        }
    }

    private fun closeGoalEditor() {
        _state.update {
            it.copy(
                isEditingGoal = false,
                editingStepGoal = "",
                editingCalorieGoal = ""
            )
        }
    }

    private fun saveGoal() {
        val stepGoal = _state.value.editingStepGoal.toIntOrNull()
        val calorieGoal = _state.value.editingCalorieGoal.toFloatOrNull()

        when {
            stepGoal == null || stepGoal < 1000 -> {
                viewModelScope.launch {
                    _uiEvent.emit(
                        StepTrackerUiEvent.ShowSnackbar("Step goal must be at least 1000")
                    )
                }
                return
            }
            stepGoal > 100000 -> {
                viewModelScope.launch {
                    _uiEvent.emit(
                        StepTrackerUiEvent.ShowSnackbar("Step goal seems too high")
                    )
                }
                return
            }
            calorieGoal == null || calorieGoal < 100f -> {
                viewModelScope.launch {
                    _uiEvent.emit(
                        StepTrackerUiEvent.ShowSnackbar("Calorie goal must be at least 100")
                    )
                }
                return
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                repository.saveGoal(stepGoal!!, calorieGoal!!)
                _state.update {
                    it.copy(
                        stepGoal = stepGoal,
                        calorieGoal = calorieGoal,
                        stepProgress = (it.todaySteps.toFloat() / stepGoal)
                            .coerceIn(0f, 1f),
                        calorieProgress = (it.todayCalories / calorieGoal)
                            .coerceIn(0f, 1f),
                        isEditingGoal = false,
                        isLoading = false,
                        goalSaved = true
                    )
                }
                _uiEvent.emit(StepTrackerUiEvent.GoalSavedSuccess)
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _uiEvent.emit(StepTrackerUiEvent.ShowSnackbar("Failed to save goal"))
            }
        }
    }
}