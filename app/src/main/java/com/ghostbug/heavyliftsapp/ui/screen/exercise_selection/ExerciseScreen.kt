package com.ghostbug.heavyliftsapp.ui.screen.exercise_selection

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

// ─── Nothing OS Design System ─────────────────────────────────────────────────

private object NothingColors {
    val Void         = Color(0xFF0A0A0A)
    val Surface0     = Color(0xFF111111)
    val Surface1     = Color(0xFF1A1A1A)
    val Surface2     = Color(0xFF222222)
    val Hairline     = Color(0xFF2C2C2C)
    val StrokeWeak   = Color(0xFF1E1E1E)
    val NothingWhite = Color(0xFFFFFFFF)
    val OffWhite     = Color(0xFFE8E8E8)
    val DimWhite     = Color(0xFF8A8A8A)
    val FaintWhite   = Color(0xFF3A3A3A)
    val GlyphRed     = Color(0xFFFF3A3A)
    val GlyphRedDim  = Color(0xFF3A1010)
}

// Dot-matrix canvas background — Nothing's signature texture
private fun Modifier.dotMatrixBackground(
    dotColor: Color = NothingColors.FaintWhite.copy(alpha = 0.15f),
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
    onNavigateToLogWorkout: (ExerciseEntity) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ExerciseUiEvent.AddExercise  -> onNavigateToLogWorkout(event.exercise)
                is ExerciseUiEvent.NavigateBack -> onBack()
                is ExerciseUiEvent.SendSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = NothingColors.Void,
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
            .background(NothingColors.Void)
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
        HorizontalDivider(color = NothingColors.Hairline, thickness = 0.5.dp)

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
                tint = NothingColors.DimWhite,
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
                    .background(NothingColors.GlyphRed, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(NothingColors.FaintWhite, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(NothingColors.FaintWhite, CircleShape)
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
            color = NothingColors.DimWhite,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "EXERCISE",
            color = NothingColors.NothingWhite,
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
                color = NothingColors.FaintWhite,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = NothingColors.DimWhite,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = NothingColors.DimWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        singleLine = true,
        // Sharp corners — Nothing OS never rounds search bars heavily
        shape = RoundedCornerShape(3.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor        = NothingColors.NothingWhite,
            unfocusedTextColor      = NothingColors.NothingWhite,
            focusedBorderColor      = NothingColors.NothingWhite,
            unfocusedBorderColor    = NothingColors.Hairline,
            cursorColor             = NothingColors.GlyphRed,
            focusedContainerColor   = NothingColors.Surface0,
            unfocusedContainerColor = NothingColors.Surface0,
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
                    .background(NothingColors.GlyphRed, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = NothingColors.DimWhite,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        count?.let {
            Text(
                text = "$it FOUND",
                color = NothingColors.FaintWhite,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
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
                color = NothingColors.StrokeWeak,
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
                .background(NothingColors.FaintWhite, RoundedCornerShape(1.dp))
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Exercise info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.exerciseName.uppercase(),
                color = NothingColors.NothingWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = exercise.category.uppercase(),
                color = NothingColors.DimWhite,
                fontSize = 9.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Add button — Nothing style: outlined square
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color.Transparent)
                .clip(RoundedCornerShape(3.dp))
                .background(NothingColors.Surface1)
                .clickable { onClickExercise() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add exercise",
                tint = NothingColors.NothingWhite,
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
                color = NothingColors.NothingWhite,
                strokeWidth = 1.5.dp,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "SEARCHING",
                color = NothingColors.FaintWhite,
                fontSize = 9.sp,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.Monospace
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
                        .background(NothingColors.FaintWhite, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(NothingColors.GlyphRed, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(NothingColors.FaintWhite, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = if (hasQuery) "NO MATCH" else "NO EXERCISES",
                color = NothingColors.NothingWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasQuery) "TRY A DIFFERENT TERM" else "DATABASE EMPTY",
                color = NothingColors.FaintWhite,
                fontSize = 9.sp,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace,
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
                    .background(NothingColors.GlyphRed, CircleShape)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "ERROR",
                color = NothingColors.GlyphRed,
                fontSize = 9.sp,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message.uppercase(),
                color = NothingColors.DimWhite,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
                lineHeight = 18.sp
            )
        }
    }
}