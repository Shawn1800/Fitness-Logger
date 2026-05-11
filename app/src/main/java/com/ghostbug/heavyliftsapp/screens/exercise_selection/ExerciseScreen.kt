package com.ghostbug.heavyliftsapp.screens.exercise_selection

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ghostbug.heavyliftsapp.data.domain.ExerciseEntity
import com.ghostbug.heavyliftsapp.ui.theme.HeavyLiftsColors
import com.ghostbug.heavyliftsapp.ui.theme.HeavyLiftsType

// Dot-matrix canvas background — Nothing's signature texture
private fun Modifier.dotMatrixBackground(
    dotColor: Color = HeavyLiftsColors.BgChip.copy(alpha = 0.15f),
    spacing: Float = 14f,
    radius: Float = 1.1f
): Modifier = this.drawBehind {
    val cols = (size.width / spacing).toInt() + 1
    val rows = (size.height / spacing).toInt() + 1
    for (col in 0..cols) {
        for (row in 0..rows) {
            drawCircle(
                color = dotColor,
                radius = radius,
                center = Offset(col * spacing, row * spacing)
            )
        }
    }
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun ExerciseSelectionScreen(
    viewModel: ExerciseViewModel = viewModel(),
    onNavigateToLogWorkout: (Long) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ExerciseUiEvent.AddExercise  -> onNavigateToLogWorkout(event.exercise.id)
                is ExerciseUiEvent.NavigateBack -> onBack()
                is ExerciseUiEvent.SendSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = HeavyLiftsColors.Bg,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            ExerciseSelectionContent(
                state = state,
                onBack = onBack,
                onEvent = { viewModel.onEvent(it) }
            )
        }
    }
}

// ─── Content ──────────────────────────────────────────────────────────────────

@Composable
fun ExerciseSelectionContent(
    state: ExerciseState,
    onBack: () -> Unit,
    onEvent: (ExerciseEvent) -> Unit
) {
    NothingSearchLayout(
        query            = state.searchQuery,
        isSearching      = state.isSearching,
        exercises        = state.exercises,
        onBack           = onBack,
        onQueryChange    = { onEvent(ExerciseEvent.OnSearchQueryChange(it)) },
        onAddExercise    = { onEvent(ExerciseEvent.OnAddExercise(it)) }
    )
}

// ─── Main Layout ──────────────────────────────────────────────────────────────

@Composable
fun NothingSearchLayout(
    query: String,
    isSearching: Boolean,
    exercises: List<ExerciseEntity>,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onAddExercise: (ExerciseEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HeavyLiftsColors.Bg)
    ) {
        // ── Top bar ───────────────────────────────────────────────
        NothingTopBar(onBack = onBack)

        // ── Page heading ──────────────────────────────────────────
        NothingPageHeading(resultCount = if (!isSearching) exercises.size else null)

        // ── Search field ──────────────────────────────────────────
        NothingSearchField(
            query = query,
            onQueryChange = onQueryChange
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Section label row ─────────────────────────────────────
        HorizontalDivider(color = HeavyLiftsColors.BorderSubtle, thickness = 0.5.dp)

        NothingSectionLabel(
            label = "EXERCISES",
            count = if (!isSearching && exercises.isNotEmpty()) exercises.size else null
        )

        // ── Body ──────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            when {
                isSearching        -> NothingLoadingIndicator()
                exercises.isEmpty() -> NothingEmptyState(hasQuery = query.isNotEmpty())
                else               -> NothingExerciseList(
                    exercises = exercises,
                    onClickExercise = onAddExercise
                )
            }
        }
    }
}

// ─── Top Bar ─────────────────────────────────────────────────────────────────

@Composable
private fun NothingTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 20.dp, top = 12.dp, bottom = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = HeavyLiftsColors.Fg3,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Decorative glyph dots — top-right signature
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(HeavyLiftsColors.Accent, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(HeavyLiftsColors.BgChip, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(HeavyLiftsColors.BgChip, CircleShape)
            )
        }
    }
}

// ─── Page Heading ─────────────────────────────────────────────────────────────

@Composable
private fun NothingPageHeading(resultCount: Int?) {
    Column(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp)
    ) {
        Text(
            text = "SELECT",
            color = HeavyLiftsColors.Fg3,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            fontFamily = HeavyLiftsType.Display
        )
        Text(
            text = "EXERCISE",
            color = HeavyLiftsColors.Fg1,
            fontWeight = FontWeight.Black,
            fontSize = 30.sp,
            letterSpacing = (-1).sp,
            lineHeight = 30.sp
        )
    }
}

// ─── Search Field ─────────────────────────────────────────────────────────────

@Composable
private fun NothingSearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        placeholder = {
            Text(
                text = "SEARCH_",
                color = HeavyLiftsColors.BgChip,
                fontSize = 13.sp,
                fontFamily = HeavyLiftsType.Display,
                letterSpacing = 1.sp
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = HeavyLiftsColors.Fg3,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = HeavyLiftsColors.Fg3,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        singleLine = true,
        // Sharp corners — Nothing OS never rounds search bars heavily
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor        = HeavyLiftsColors.Fg1,
            unfocusedTextColor      = HeavyLiftsColors.Fg1,
            focusedBorderColor      = HeavyLiftsColors.Fg1,
            unfocusedBorderColor    = HeavyLiftsColors.BorderSubtle,
            cursorColor             = HeavyLiftsColors.Accent,
            focusedContainerColor   = HeavyLiftsColors.BgElevated,
            unfocusedContainerColor = HeavyLiftsColors.BgElevated,
        )
    )
}

// ─── Section Label ────────────────────────────────────────────────────────────

@Composable
private fun NothingSectionLabel(label: String, count: Int?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(HeavyLiftsColors.Accent, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = HeavyLiftsColors.Fg3,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                fontFamily = HeavyLiftsType.Display
            )
        }

        count?.let {
            Text(
                text = "$it FOUND",
                color = HeavyLiftsColors.BgChip,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                fontFamily = HeavyLiftsType.Display
            )
        }
    }
}

// ─── Exercise List ────────────────────────────────────────────────────────────

@Composable
fun NothingExerciseList(
    exercises: List<ExerciseEntity>,
    onClickExercise: (ExerciseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        items(items = exercises, key = { it.id }) { exercise ->
            NothingExerciseItem(
                exercise = exercise,
                onClickExercise = { onClickExercise(exercise) }
            )
            HorizontalDivider(
                color = HeavyLiftsColors.BorderSubtle,
                thickness = 0.5.dp
            )
        }
    }
}

// ─── Exercise Item ────────────────────────────────────────────────────────────

@Composable
private fun NothingExerciseItem(
    exercise: ExerciseEntity,
    onClickExercise: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClickExercise() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glyph accent bar — flips red on press via clickable ripple region
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(32.dp)
                .background(HeavyLiftsColors.BgChip, RoundedCornerShape(1.dp))
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Exercise info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.exerciseName.uppercase(),
                color = HeavyLiftsColors.Fg1,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                fontFamily = HeavyLiftsType.Display,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = exercise.category.uppercase(),
                color = HeavyLiftsColors.Fg3,
                fontSize = 9.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 2.sp,
                fontFamily = HeavyLiftsType.Display
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Add button — Nothing style: outlined square
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color.Transparent)
                .clip(RoundedCornerShape(12.dp))
                .background(HeavyLiftsColors.BgChip)
                .clickable { onClickExercise() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add exercise",
                tint = HeavyLiftsColors.Fg1,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ─── Loading ──────────────────────────────────────────────────────────────────

@Composable
private fun NothingLoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = HeavyLiftsColors.Fg1,
                strokeWidth = 1.5.dp,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "SEARCHING",
                color = HeavyLiftsColors.BgChip,
                fontSize = 9.sp,
                letterSpacing = 3.sp,
                fontFamily = HeavyLiftsType.Display
            )
        }
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun NothingEmptyState(hasQuery: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .dotMatrixBackground(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glyph: cross pattern using dots
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(HeavyLiftsColors.BgChip, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(HeavyLiftsColors.Accent, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(HeavyLiftsColors.BgChip, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = if (hasQuery) "NO MATCH" else "NO EXERCISES",
                color = HeavyLiftsColors.Fg1,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                fontFamily = HeavyLiftsType.Display
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasQuery) "TRY A DIFFERENT TERM" else "DATABASE EMPTY",
                color = HeavyLiftsColors.BgChip,
                fontSize = 9.sp,
                letterSpacing = 2.sp,
                fontFamily = HeavyLiftsType.Display,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Error ────────────────────────────────────────────────────────────────────

@Composable
private fun NothingErrorMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(HeavyLiftsColors.Accent, CircleShape)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "ERROR",
                color = HeavyLiftsColors.Accent,
                fontSize = 9.sp,
                letterSpacing = 3.sp,
                fontFamily = HeavyLiftsType.Display,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message.uppercase(),
                color = HeavyLiftsColors.Fg3,
                fontSize = 11.sp,
                fontFamily = HeavyLiftsType.Display,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
                lineHeight = 18.sp
            )
        }
    }
}