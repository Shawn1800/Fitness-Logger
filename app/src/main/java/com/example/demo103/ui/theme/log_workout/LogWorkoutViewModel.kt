package com.example.demo103.ui.theme.log_workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demo103.data.entity.WorkoutEntryEntity
import com.example.demo103.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class LogWorkoutViewModel (
    private val repository: WorkoutRepository
) : ViewModel(){
    private val _state = MutableStateFlow(LogWorkoutState())
    val state: StateFlow<LogWorkoutState> = _state.asStateFlow()
    private val _uiEvent = MutableSharedFlow<LogWorkoutUiEvent>()
    val uiEvent:  SharedFlow<LogWorkoutUiEvent> = _uiEvent.asSharedFlow()

    fun onEvent(event: LogWorkoutEvent) {
        when (event) {
            is LogWorkoutEvent.OnAddingSets -> {
                // 1. Get current input values
                val weight = _state.value.currentWeight.toDoubleOrNull()
                val reps = _state.value.currentReps.toIntOrNull()

                if (weight == null || reps == null ||weight ==0.0 || reps==0) {
                    viewModelScope.launch {
                        _uiEvent.emit(LogWorkoutUiEvent.SendSnackbar("Enter valid weight and reps"))
                    }
                    return
                }

                // 2. Add to list and RESET inputs
                _state.update { currentState ->
                    currentState.copy(
                        currentWeight = "",
                        currentReps = "",
                        sets = currentState.sets + WorkoutEntryEntity(
                            entryId = -(currentState.sets.size + 1), // Temporary ID to distinguish unsaved sets
                            exerciseId = event.exerciseId,
                            weight = weight,
                            reps = reps,
                            sets = currentState.sets.size + 1,
                            date = System.currentTimeMillis()
                        )
                    )
                }
            }

            is LogWorkoutEvent.DeleteSet -> {
                viewModelScope.launch {
                    if (event.entryId > 0) {
                        repository.deleteSetById(event.entryId)
                    }
                    _state.update { currentState ->
                        currentState.copy(
                            sets = currentState.sets.filter { it.entryId != event.entryId })
                    }
                }
            }

            is LogWorkoutEvent.UpdateReps -> {
                if (event.entryId == 0) {
                    _state.update { it.copy(currentReps = event.reps) }
                } else {
                    _state.update { currentState ->
                        currentState.copy(
                            editingReps = currentState.editingReps + (event.entryId to event.reps),
                            sets = currentState.sets.map {
                                if (it.entryId == event.entryId) {
                                    // Only update the number if the input is valid
                                    it.copy(reps = event.reps.toIntOrNull() ?: it.reps)
                                } else it
                            })
                    }
                }
            }

            is LogWorkoutEvent.UpdateWeight -> {
                if (event.entryId == 0) {
                    _state.update { it.copy(currentWeight = event.weight) }
                } else {
                    _state.update { currentState ->
                        currentState.copy(
                            editingWeights = currentState.editingWeights + (event.entryId to event.weight),
                            sets = currentState.sets.map {
                                if (it.entryId == event.entryId) {
                                    it.copy(weight = event.weight.toDoubleOrNull() ?: it.weight)
                                } else it
                            })
                    }
                }
            }

            // When loading sets, initialize the maps
            is LogWorkoutEvent.SetExercise -> {
                _state.update { it.copy (exercise = event.exercise, dateMillis = event.dateMillis )}
                viewModelScope.launch {
                    repository.getWorkoutByExerciseAndDate(event.exercise.exerciseId,event.dateMillis).first()
                        .let { list ->
                        _state.update {
                            it.copy(
                                sets = list,
                                editingWeights = list.associate { it.entryId to it.weight.toString() },
                                editingReps = list.associate { it.entryId to it.reps.toString() })
                        }
                    }
                }
            }

            is LogWorkoutEvent.SaveWorkout -> {
                viewModelScope.launch {
                    val dateToSave =
                        _state.value.dateMillis

                    val currentState = _state.value
                    var allSets = currentState.sets
// 2. IMPORTANT: If there is valid text in the current input fields, include it!
                    val curWeight = currentState.currentWeight.toDoubleOrNull()
                    val curReps = currentState.currentReps.toIntOrNull()
                    if (curWeight != null && curReps != null && curWeight > 0 && curReps > 0) {
                        allSets = allSets + WorkoutEntryEntity(
                            exerciseId = currentState.exercise?.exerciseId ?: 0,
                            weight = curWeight,
                            reps = curReps,
                            sets = allSets.size + 1,
                            date = dateToSave
                        )
                    }
                    if (allSets.isEmpty()) {
                        _uiEvent.emit(LogWorkoutUiEvent.SendSnackbar("Add at least 1 set"))
                        return@launch
                    }
// 3. Fix: Use normalized date and handle IDs for Room
                    val finalizedSets = allSets.map {
                        it.copy(
                            entryId = if (it.entryId < 0) 0 else it.entryId,
                            date = dateToSave // This ensures it matches the Home screen query
                        )
                    }
                    try {
                        repository.insertWorkoutEntry(finalizedSets) // Use the finalized list!
                        _uiEvent.emit(LogWorkoutUiEvent.NavBackToHome)
                    } catch (e: Exception) {
                        _uiEvent.emit(LogWorkoutUiEvent.SendSnackbar("Error saving: ${e.message}"))
                    }
                }
            }
        }
    }
}
