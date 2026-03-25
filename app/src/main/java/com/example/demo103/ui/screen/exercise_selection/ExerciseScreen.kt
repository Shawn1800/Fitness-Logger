package com.example.demo103.ui.screen.exercise_selection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demo103.data.entity.ExerciseEntity
import kotlin.collections.List

private object AppColors {
    val Background    = Color(0xFF101014)
    val Surface       = Color(0xFF1C1C22)
    val Primary       = Color(0xFF8B5CF6)
    val TextPrimary   = Color(0xFFF1F0FF)
    val TextSecondary = Color(0xFF7B7A8E)
    val Stroke        = Color(0xFF2A2A35)
    val ChipSelected  = Color(0xFF8B5CF6)
    val ChipUnselected = Color(0xFF1C1C22)
}
@Composable
fun ExerciseSelectionScreen(
    viewModel: ExerciseViewModel = viewModel(),
    onNavigateToLogWorkout: (ExerciseEntity) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ExerciseUiEvent.AddExercise    -> onNavigateToLogWorkout(event.exercise)
                is ExerciseUiEvent.NavigateBack   -> onBack()
                is ExerciseUiEvent.SendSnackbar   -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = AppColors.Background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            ExerciseSelectionContent(
                state = state,
                onEvent = { event -> viewModel.onEvent(event) }
            )
        }
    }
}

// ─── Content ──────────────────────────────────────────────────────────────────
@Composable
fun ExerciseSelectionContent(
    state: ExerciseState,
    onEvent: (ExerciseEvent) -> Unit
) {
    Search(
        query            = state.searchQuery,
        selectedCategory = state.selectedCategory,
        isSearching      = state.isSearching,
        exercises        = state.exercises,
        onQueryChange       = { onEvent(ExerciseEvent.OnSearchQueryChange(it)) },
        onCategorySelected  = { onEvent(ExerciseEvent.OnSelectCategory(it)) },
        onCategoryCleared   = { onEvent(ExerciseEvent.OnClearCategory) },
        onAddExercise       = { onEvent(ExerciseEvent.OnAddExercise(it)) }
    )
}

// ─── Search + List ────────────────────────────────────────────────────────────
@Composable
fun Search(
    query: String,
    selectedCategory: String?,
    isSearching: Boolean,
    exercises: List<ExerciseEntity>,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onCategoryCleared: () -> Unit,
    onAddExercise: (ExerciseEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        // ── Page title ────────────────────────────────────────────
        Text(
            text = "Exercises",
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.padding(
                top = 36.dp, start = 20.dp, end = 20.dp, bottom = 16.dp
            )
        )

        // ── Search bar ────────────────────────────────────────────
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = {
                Text(
                    "Search exercises…",
                    color = AppColors.TextSecondary,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = AppColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = AppColors.TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor        = AppColors.TextPrimary,
                unfocusedTextColor      = AppColors.TextPrimary,
                focusedBorderColor      = AppColors.Primary,
                unfocusedBorderColor    = AppColors.Stroke,
                focusedLabelColor       = AppColors.Primary,
                unfocusedLabelColor     = AppColors.TextSecondary,
                cursorColor             = AppColors.Primary,
                focusedContainerColor   = AppColors.Surface,
                unfocusedContainerColor = AppColors.Surface,
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Divider ───────────────────────────────────────────────
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = AppColors.Stroke,
            thickness = 0.5.dp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Results count label ───────────────────────────────────
        if (!isSearching && exercises.isNotEmpty()) {
            Text(
                text = "${exercises.size} exercises",
                color = AppColors.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.4.sp,
                modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
            )
        }

        // ── Body ──────────────────────────────────────────────────
        when {
            isSearching      -> LoadingIndicator()
            exercises.isEmpty() -> EmptyMessage()
            else -> ExerciseList(
                exercises = exercises,
                onClickExercise = onAddExercise
            )
        }
    }
}

// ─── Exercise List ────────────────────────────────────────────────────────────
@Composable
fun ExerciseList(
    exercises: List<ExerciseEntity>,
    onClickExercise: (ExerciseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(items = exercises, key = { it.exerciseId }) { exercise ->
            ExerciseItem(
                exercise = exercise,
                onClickExercise = { onClickExercise(exercise) }
            )
        }
    }
}

// ─── Exercise Item Card ───────────────────────────────────────────────────────
@Composable
private fun ExerciseItem(
    exercise: ExerciseEntity,
    onClickExercise: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        border = BorderStroke(0.5.dp, AppColors.Stroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: accent bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AppColors.Primary)
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Middle: name + category
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.exerciseName,
                    color = AppColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                // Category chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColors.Primary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = exercise.category,
                        color = AppColors.Primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Right: add button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.Primary.copy(alpha = 0.15f))
                    .clickable { onClickExercise() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add exercise",
                    tint = AppColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── States ───────────────────────────────────────────────────────────────────
@Composable
private fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            color = AppColors.Primary,
            strokeWidth = 2.dp,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
private fun EmptyMessage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🔍", fontSize = 44.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No exercises found",
            color = AppColors.TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Try a different name or category",
            color = AppColors.TextSecondary,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            color = Color(0xFFCF6679),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}