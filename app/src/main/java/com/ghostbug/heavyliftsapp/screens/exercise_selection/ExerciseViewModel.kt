package com.ghostbug.heavyliftsapp.screens.exercise_selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostbug.heavyliftsapp.data.repository.ExerciseRepository
import com.ghostbug.heavyliftsapp.data.repository.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

@OptIn(FlowPreview::class)
class ExerciseViewModel(
    private val repository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _isSearching = MutableStateFlow(false)

    private val _uiEvent = MutableSharedFlow<ExerciseUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var selectedDateMillis: Long = System.currentTimeMillis()

    fun setSelectedDate(millis: Long) {
        selectedDateMillis = millis
    }

    // ── UI State ──────────────────────────────────────────────────────────

    @OptIn(ExperimentalCoroutinesApi::class)
    private val filteredExercises = combine(
        _searchQuery.debounce(300),
        _selectedCategory
    ) { query, category ->
        query to category
    }.onEach {
        _isSearching.value = true
    }.flatMapLatest { (query, category) ->
        flow {
            try {
                val result = when {
                    query.isNotBlank() && category != null ->
                        repository.searchExerciseByCategory(query, category)
                    query.isNotBlank() ->
                        repository.searchExercises(query)
                    category != null ->
                        repository.getExerciseByCategory(category)
                    else ->
                        repository.getAllExercises()
                }
                emit(result)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                emit(emptyList()) // Emit empty on error to avoid crashing the flow
            } finally {
                _isSearching.value = false
            }
        }
    }

    val state: StateFlow<ExerciseState> = combine(
        _searchQuery,
        _selectedCategory,
        _isSearching,
        filteredExercises
    ) { query, category, searching, exercises ->
        ExerciseState(
            exercises = exercises,
            searchQuery = query,
            selectedCategory = category,
            isSearching = searching
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExerciseState()
    )

    fun onEvent(event: ExerciseEvent) {
        when (event) {
            is ExerciseEvent.OnSearchQueryChange -> {
                _searchQuery.value = event.query
            }
            is ExerciseEvent.OnSelectCategory -> {
                _selectedCategory.value = event.category
            }
            is ExerciseEvent.OnClearCategory -> {
                _selectedCategory.value = null
            }
            is ExerciseEvent.OnAddExercise -> {
                viewModelScope.launch {
                    try {
                        val existingEntries = workoutRepository
                            .getWorkoutByExerciseAndDate(event.exercise.id, selectedDateMillis)

                        if (existingEntries.isNotEmpty()) {
                            _uiEvent.emit(ExerciseUiEvent.SendSnackbar(
                                "${event.exercise.exerciseName} is already added for today"
                            ))
                        } else {
                            _uiEvent.emit(ExerciseUiEvent.AddExercise(exercise = event.exercise))
                        }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        _uiEvent.emit(ExerciseUiEvent.SendSnackbar("Error: ${e.message}"))
                    }
                }
            }
        }
    }
}