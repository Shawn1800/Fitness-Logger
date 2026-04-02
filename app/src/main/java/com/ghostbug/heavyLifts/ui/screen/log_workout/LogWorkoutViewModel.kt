package com.ghostbug.heavyLifts.ui.screen.log_workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostbug.heavyLifts.data.UseCase.OneRepMaxUseCase
import com.ghostbug.heavyLifts.data.domain.WorkoutEntryEntity
import com.ghostbug.heavyLifts.data.repository.OneRepMaxRepository
import com.ghostbug.heavyLifts.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import java.time.Instant
import java.time.ZoneId
import kotlin.collections.filter
import kotlin.collections.map

class LogWorkoutViewModel (
    private val workoutRepository: WorkoutRepository,
    private val oneRepMaxRepository: OneRepMaxRepository,
    private val oneRepMaxUseCase: OneRepMaxUseCase
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

                if (weight == null || reps == null || weight == 0.0 || reps == 0) {
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
                            id = -(currentState.sets.size + 1), // Temporary ID to distinguish unsaved sets
                            exerciseId = event.exerciseId,
                            weight = weight.toFloat(),
                            reps = reps,
                            sets = currentState.sets.size + 1,
                            date = System.currentTimeMillis(),
                            userId = ""
                        )
                    )
                }
            }

            is LogWorkoutEvent.DeleteSet -> {
                viewModelScope.launch {
                    if (event.id > 0) {
                        workoutRepository.deleteSetById(event.id)
                    }
                    val updatedSets = _state.value.sets.filter { it.id != event.id }
                    _state.update { currentState ->
                        currentState.copy(
                            sets = updatedSets
                        )
                    }
                    val exerciseId = _state.value.exercise?.id ?: return@launch
                    val date = _state.value.dateMillis

                    if (updatedSets.isEmpty()) {
                        oneRepMaxRepository.deleteOneRepMax(exerciseId, date)
                    } else {
                        oneRepMaxUseCase(
                            exerciseId = exerciseId,
                            date = date,
                            sets = updatedSets
                        )
                    }

                }
            }

            is LogWorkoutEvent.UpdateReps -> {
                if (event.id == 0) {
                    _state.update { it.copy(currentReps = event.reps) }
                } else {
                    _state.update { currentState ->
                        currentState.copy(
                            editingReps = currentState.editingReps + (event.id to event.reps),
                            sets = currentState.sets.map {
                                if (it.id == event.id) {
                                    // Only update the number if the input is valid
                                    it.copy(reps = event.reps.toIntOrNull() ?: it.reps)
                                } else it
                            })
                    }
                }
            }

            is LogWorkoutEvent.UpdateWeight -> {
                if (event.id == 0) {
                    _state.update { it.copy(currentWeight = event.weight) }
                } else {
                    _state.update { currentState ->
                        currentState.copy(
                            editingWeights = currentState.editingWeights + (event.id to event.weight),
                            sets = currentState.sets.map {
                                if (it.id == event.id) {
                                    it.copy(weight = event.weight.toFloatOrNull() ?: it.weight)
                                } else it
                            })
                    }
                }
            }

            // When loading sets, initialize the maps
            is LogWorkoutEvent.SetExercise -> {
                _state.update { it.copy(exercise = event.exercise, dateMillis = event.dateMillis) }
                viewModelScope.launch {
                    workoutRepository.getWorkoutByExerciseAndDate(
                        event.exercise.id,
                        event.dateMillis
                    )
                        .let { list ->
                            _state.update { state ->
                                state.copy(
                                    sets = list,
                                    editingWeights = list.associate { entry -> entry.id to entry.weight.toString() },
                                    editingReps = list.associate { entry -> entry.id to entry.reps.toString() })
                            }
                        }
                }
            }

            is LogWorkoutEvent.SaveWorkout -> {
                viewModelScope.launch {
                    val currentState = _state.value

                    // ✅ Compute noon of the selected date — avoids boundary issues
                    val selectedDate = Instant.ofEpochMilli(currentState.dateMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()

                    val dateToSave = selectedDate
                        .atTime(12, 0)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()

                    var allSets = currentState.sets

                    // Include any unsaved input still in the text fields
                    val curWeight = currentState.currentWeight.toDoubleOrNull()
                    val curReps = currentState.currentReps.toIntOrNull()
                    if (curWeight != null && curReps != null && curWeight > 0 && curReps > 0) {
                        allSets = allSets + WorkoutEntryEntity(
                            exerciseId = currentState.exercise?.id ?: 0,
                            weight = curWeight.toFloat(),
                            reps = curReps,
                            sets = allSets.size + 1,
                            date = dateToSave,
                            userId = "",
                            id = -(allSets.size + 1)
                        )
                    }

                    if (allSets.isEmpty()) {
                        _uiEvent.emit(LogWorkoutUiEvent.SendSnackbar("Add at least 1 set"))
                        return@launch
                    }

                    // Normalize all sets: fix set numbers and pin to noon of selected date
                    val finalizedSets = allSets.mapIndexed { index, entry ->
                        entry.copy(
                            id = if (entry.id < 0) 0 else entry.id,
                            sets = index + 1,
                            date = dateToSave  // ✅ noon of selected date, never on boundary
                        )
                    }

                    // Only insert NEW sets — existing (id > 0) are already in Supabase
                    val newSets = finalizedSets.filter { it.id == 0 }

                    android.util.Log.d(
                        "WORKOUT_DEBUG",
                        "SAVING → dateToSave=${Instant.ofEpochMilli(dateToSave)}, newSets=${newSets.size}"
                    )
                    newSets.forEach {
                        android.util.Log.d(
                            "WORKOUT_DEBUG",
                            "  SET → date=${Instant.ofEpochMilli(it.date)}, exerciseId=${it.exerciseId}"
                        )
                    }

                    try {
                        if (newSets.isNotEmpty()) {
                            workoutRepository.insertWorkoutEntry(newSets)
                        }

                        _state.update {
                            it.copy(
                                sets = finalizedSets,
                                currentWeight = "",
                                currentReps = ""
                            )
                        }

                        // Calculate 1RM in background — don't block navigation
                        val exerciseId = currentState.exercise?.id
                        if (exerciseId != null) {
                            viewModelScope.launch {
                                try {
                                    oneRepMaxUseCase(
                                        exerciseId = exerciseId,
                                        date = dateToSave,
                                        sets = finalizedSets
                                    )
                                } catch (e: Exception) {
                                    if (e is CancellationException) throw e
                                }
                            }
                        }

                        _uiEvent.emit(LogWorkoutUiEvent.NavBackToHome)

                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        _uiEvent.emit(LogWorkoutUiEvent.SendSnackbar("Error saving: ${e.message}"))
                    }
                }
            }
        }
    }}