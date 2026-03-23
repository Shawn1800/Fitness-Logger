package com.example.demo103.ui.theme.exercise_selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demo103.data.repository.ExerciseRepository
import com.example.demo103.data.repository.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class ExerciseViewModel(
    private val repository : ExerciseRepository,
    private val workoutRepository: WorkoutRepository
): ViewModel() {
    //------------------------------------------------------------------------------------------//
    // we don't use this here as we let the pipeline handle search and category selection
    // it does not work with flat map latest and combine
    // private val _exerciseState = MutableStateFlow(ExerciseState())
    // val exercise_state: StateFlow<ExerciseState> = _exerciseState.asStateFlow()
    //------------------------------------------------------------------------------------------//
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

// 1. Create a flow for the filtered exercises based on search and category
    @OptIn(ExperimentalCoroutinesApi::class)
    private val filteredExercises = combine(_searchQuery.debounce(300), _selectedCategory) { query, category ->
        query to category
    }.onEach {
        _isSearching.value = true
    }.flatMapLatest { (query, category) ->
        val flow = when {
            query.isNotBlank() && category != null -> repository.searchExerciseByCategory(query, category)
            query.isNotBlank() -> repository.searchExercises(query)
            category != null -> repository.getExerciseByCategory(category)

            else -> repository.getAllExercises()
        }
        flow.onEach { _isSearching.value = false }
    }

    // 2. Final UI State: Combine all triggers into a single StateFlow
    @OptIn(FlowPreview::class)
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
                _searchQuery.value = event.query            //value of the state becomes the query
            }
            is ExerciseEvent.OnSelectCategory -> {
                _selectedCategory.value = event.category //value of the state becomes the category
            }
            is ExerciseEvent.OnClearCategory -> {
                _selectedCategory.value = null //value of the state becomes null
            }
//             when i press onclick this runs
            is ExerciseEvent.OnAddExercise -> {
                viewModelScope.launch {
                    // Query the DB for entries matching this exercise and the selected day
                    val existingEntries = workoutRepository
                        .getWorkoutByExerciseAndDate(event.exercise.exerciseId, selectedDateMillis)
                      // Get the current snapshot from the Flow

                    if (existingEntries.isNotEmpty()) {
                        // If it exists, send a message to the UI
                        _uiEvent.emit(ExerciseUiEvent.SendSnackbar("${event.exercise.exerciseName} is already added for today"))
                    } else {
                        // Otherwise, proceed with navigation
                        _uiEvent.emit(ExerciseUiEvent.AddExercise(exercise = event.exercise))
                    }
                }
            }

        }
    }

    }
