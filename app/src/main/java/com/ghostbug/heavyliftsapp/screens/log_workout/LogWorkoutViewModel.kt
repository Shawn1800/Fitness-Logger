package com.ghostbug.heavyliftsapp.screens.log_workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.savedState
import androidx.savedstate.serialization.encodeToSavedState
import com.ghostbug.heavyliftsapp.data.UseCase.OneRepMaxUseCase
import com.ghostbug.heavyliftsapp.data.domain.WorkoutEntryEntity
import com.ghostbug.heavyliftsapp.data.repository.ExerciseRepository
import com.ghostbug.heavyliftsapp.data.repository.OneRepMaxRepository
import com.ghostbug.heavyliftsapp.data.repository.WorkoutRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

class LogWorkoutViewModel(
    private val workoutRepository: WorkoutRepository,
    private val oneRepMaxRepository: OneRepMaxRepository,
    private val oneRepMaxUseCase: OneRepMaxUseCase,
    private val exerciseRepository: ExerciseRepository,

) : ViewModel() {


    private val _state = MutableStateFlow(LogWorkoutState())
    val state: StateFlow<LogWorkoutState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<LogWorkoutUiEvent>()
    val uiEvent: SharedFlow<LogWorkoutUiEvent> = _uiEvent.asSharedFlow()

    fun onEvent(event: LogWorkoutEvent) {
        when (event) {
            is LogWorkoutEvent.OnAddingSets -> handleAddSet(event.exerciseId)
            is LogWorkoutEvent.DeleteSet -> handleDeleteSet(event.id)
            is LogWorkoutEvent.UpdateReps -> handleUpdateReps(event.id, event.reps)
            is LogWorkoutEvent.UpdateWeight -> handleUpdateWeight(event.id, event.weight)
            is LogWorkoutEvent.SetExerciseById -> handleSetExerciseById(event)
            is LogWorkoutEvent.SaveWorkout -> saveWorkout()
        }
    }

    private fun handleSetExerciseById(event: LogWorkoutEvent.SetExerciseById) {
        viewModelScope.launch {
            try {
                val exercise = exerciseRepository.getExerciseById(event.exerciseId)
                if (exercise != null) {
                    _state.update { it.copy(exercise = exercise, dateMillis = event.dateMillis) }

                    val list = workoutRepository.getWorkoutByExerciseAndDate(event.exerciseId, event.dateMillis)
                    _state.update { state ->
                        state.copy(
                            sets = list,
                            editingWeights = list.associate { it.id to it.weight.toString() },
                            editingReps = list.associate { it.id to it.reps.toString() }
                        )
                    }
                } else {
                    sendSnackbar("Exercise not found")
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                sendSnackbar("Error loading exercise: ${e.message}")
            }
        }
    }

    // ...existing code...

    private fun handleAddSet(exerciseId: Long) {
        val weight = _state.value.currentWeight.toDoubleOrNull()
        val reps = _state.value.currentReps.toIntOrNull()

        if (weight == null || reps == null || weight == 0.0 || reps == 0) {
            sendSnackbar("Enter valid weight and reps")
            return
        }

        _state.update { currentState ->
            val newSet = WorkoutEntryEntity(
                id = -(currentState.sets.size + 1L), // Temporary ID
                exerciseId = exerciseId,
                weight = weight.toFloat(),
                reps = reps,
                sets = currentState.sets.size + 1,
                date = System.currentTimeMillis(),
                userId = ""
            )
            currentState.copy(
                currentWeight = "",
                currentReps = "",
                sets = currentState.sets + newSet
            )
        }
    }

    private fun handleDeleteSet(id: Long) {
        viewModelScope.launch {
            try {
                if (id > 0) workoutRepository.deleteSetById(id)
                
                val updatedSets = _state.value.sets.filter { it.id != id }
                _state.update { it.copy(sets = updatedSets) }

                val exerciseId = _state.value.exercise?.id ?: return@launch
                val normalizedDate = getNormalizedDate(_state.value.dateMillis)

                if (updatedSets.isEmpty()) {
                    oneRepMaxRepository.deleteOneRepMax(exerciseId, normalizedDate)
                } else {
                    oneRepMaxUseCase(exerciseId, normalizedDate, updatedSets)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                sendSnackbar("Error deleting: ${e.message}")
            }
        }
    }

    private fun handleUpdateReps(id: Long, reps: String) {
        if (id == 0L) {
            _state.update { it.copy(currentReps = reps) }
        } else {
            _state.update { currentState ->
                currentState.copy(
                    editingReps = currentState.editingReps + (id to reps),
                    sets = currentState.sets.map {
                        if (it.id == id) it.copy(reps = reps.toIntOrNull() ?: it.reps) else it
                    }
                )
            }
        }
    }

    private fun handleUpdateWeight(id: Long, weight: String) {
        if (id == 0L) {
            _state.update { it.copy(currentWeight = weight) }
        } else {
            _state.update { currentState ->
                currentState.copy(
                    editingWeights = currentState.editingWeights + (id to weight),
                    sets = currentState.sets.map {
                        if (it.id == id) it.copy(weight = weight.toFloatOrNull() ?: it.weight) else it
                    }
                )
            }
        }
    }


    private fun saveWorkout() {
        viewModelScope.launch {
            val currentState = _state.value
            val dateToSave = getNormalizedDate(currentState.dateMillis)
            
            var allSets = currentState.sets
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
                    id = -(allSets.size + 1L)
                )
            }

            if (allSets.isEmpty()) {
                sendSnackbar("Add at least 1 set")
                return@launch
            }

            val finalizedSets = allSets.mapIndexed { index, entry ->
                entry.copy(
                    id = if (entry.id < 0) 0 else entry.id,
                    sets = index + 1,
                    date = dateToSave
                )
            }

            val newSets = finalizedSets.filter { it.id == 0L }

            try {
                if (newSets.isNotEmpty()) {
                    workoutRepository.insertWorkoutEntry(newSets)
                }
                val exerciseId= currentState.exercise?.id?: return@launch
                val refreshed = workoutRepository.getWorkoutByExerciseAndDate(
                   exerciseId,dateToSave
                )

                _state.update { 
                    it.copy(sets = refreshed,
                        editingWeights = refreshed.associate { it.id to it.weight.toString() },
                        editingReps = refreshed.associate { it.id to it.reps.toString() },
                        currentWeight = "",
                        currentReps = "")
                }


                    // Sequentially calculate 1RM before navigating to ensure it's not cancelled
                    oneRepMaxUseCase(exerciseId, dateToSave, refreshed)


                _uiEvent.emit(LogWorkoutUiEvent.NavBackToHome)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                sendSnackbar("Error saving: ${e.message}")
            }
        }
    }

    private fun getNormalizedDate(millis: Long): Long {
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .atTime(12, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun sendSnackbar(message: String) {
        viewModelScope.launch { _uiEvent.emit(LogWorkoutUiEvent.SendSnackbar(message)) }
    }
}
