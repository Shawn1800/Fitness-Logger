package com.ghostbug.heavyliftsapp.screens.log_workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _uiEvent = MutableSharedFlow<LogWorkoutUiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<LogWorkoutUiEvent> = _uiEvent.asSharedFlow()



    fun onEvent(event: LogWorkoutEvent) {
        when (event) {
            is LogWorkoutEvent.OnAddingSets -> handleAddSet(event.exerciseId, event.weight, event.reps)
            is LogWorkoutEvent.DeleteSet -> handleDeleteSet(event.id)
            is LogWorkoutEvent.UpdateReps -> handleUpdateReps(event.id, event.reps)
            is LogWorkoutEvent.UpdateWeight -> handleUpdateWeight(event.id, event.weight)
            is LogWorkoutEvent.SetExerciseById -> handleSetExerciseById(event)
            is LogWorkoutEvent.SaveWorkout -> saveWorkout(event.currentWeight, event.currentReps)
        }
    }

    private fun handleSetExerciseById(event: LogWorkoutEvent.SetExerciseById) {
        // allow reload if the exercise or date changed (VM lives for Activity lifetime)
        if (_state.value.exercise?.id == event.exerciseId && _state.value.dateMillis == event.dateMillis) return
        viewModelScope.launch {
            try {
                val exercise = exerciseRepository.getExerciseById(event.exerciseId)
                if (exercise != null) {
                    val list = workoutRepository.getWorkoutByExerciseAndDate(event.exerciseId, event.dateMillis)
                    _state.update {
                        it.copy(exercise = exercise, dateMillis = event.dateMillis, sets = list)
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

    private fun handleAddSet(exerciseId: Long, weightStr: String, repsStr: String) {
        val weight = weightStr.toDoubleOrNull()
        val reps = repsStr.toIntOrNull()

        if (weight == null || reps == null || weight == 0.0 || reps == 0) {
            sendSnackbar("Enter valid weight and reps")
            return
        }

        _state.update { currentState ->
            val newSet = WorkoutEntryEntity(
                id = -(currentState.sets.size + 1L),
                exerciseId = exerciseId,
                weight = weight.toFloat(),
                reps = reps,
                sets = currentState.sets.size + 1,
                date = System.currentTimeMillis(),
                userId = ""
            )
            currentState.copy(sets = currentState.sets + newSet)
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
        // no-op: logged set text fields use local Compose state; not synced to VM
    }

    private fun handleUpdateWeight(id: Long, weight: String) {
        // no-op: logged set text fields use local Compose state; not synced to VM
    }


    private fun saveWorkout(currentWeightStr: String = "", currentRepsStr: String = "") {
        viewModelScope.launch {
            val currentState = _state.value
            val dateToSave = getNormalizedDate(currentState.dateMillis)

            var allSets = currentState.sets
            val curWeight = currentWeightStr.toDoubleOrNull()
            val curReps = currentRepsStr.toIntOrNull()

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
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                sendSnackbar("Error saving: ${e.message}")
            }

            // Always navigate — user must never be stuck on this screen
            _uiEvent.emit(LogWorkoutUiEvent.NavBackToHome)

            // 1RM recalc is best-effort — runs after navigation, never blocks save
            try {
                val exerciseId = currentState.exercise?.id ?: return@launch
                oneRepMaxUseCase(exerciseId, dateToSave, allSets)
            } catch (_: Exception) { }
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
