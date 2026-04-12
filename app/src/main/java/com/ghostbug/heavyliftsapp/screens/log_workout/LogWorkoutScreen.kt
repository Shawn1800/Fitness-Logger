package com.ghostbug.heavyliftsapp.screens.log_workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// ─── Nothing OS Design System ─────────────────────────────────────────────────

private object NothingColors {
    val Void         = Color(0xFF0A0A0A)
    val Surface0     = Color(0xFF111111)
    val Surface1     = Color(0xFF1A1A1A)
    val Hairline     = Color(0xFF2C2C2C)
    val StrokeWeak   = Color(0xFF1E1E1E)
    val NothingWhite = Color(0xFFFFFFFF)
    val DimWhite     = Color(0xFF8A8A8A)
    val FaintWhite   = Color(0xFF3A3A3A)
    val GlyphRed     = Color(0xFFFF3A3A)
    val DangerDim    = Color(0xFF2A1010)
}

private fun Modifier.dotMatrixBackground(
    dotColor: Color = NothingColors.FaintWhite.copy(alpha = 0.12f),
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
fun LogWorkoutScreen(
    exerciseId: Long,
    dateMillis: Long,
    logWorkoutViewModel: LogWorkoutViewModel = viewModel(),
    onBack: () -> Unit,
) {
    LaunchedEffect(exerciseId) {
        logWorkoutViewModel.onEvent(LogWorkoutEvent.SetExerciseById(exerciseId, dateMillis = dateMillis))
    }

    val state by logWorkoutViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        logWorkoutViewModel.uiEvent.collect { event ->
            when (event) {
                is LogWorkoutUiEvent.NavBackToHome -> onBack()
                is LogWorkoutUiEvent.SendSnackbar  -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = NothingColors.Void,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            NothingSaveFab(onClick = { logWorkoutViewModel.onEvent(LogWorkoutEvent.SaveWorkout) })
        }
    ) { paddingValues ->
        NothingLogWorkoutContent(
            state = state,
            onBack = onBack,
            onEvent = { logWorkoutViewModel.onEvent(it) },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

// ─── Content ──────────────────────────────────────────────────────────────────

@Composable
fun NothingLogWorkoutContent(
    state: LogWorkoutState,
    onBack: () -> Unit,
    onEvent: (LogWorkoutEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NothingColors.Void)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp)
    ) {
        // ── Top bar ───────────────────────────────────────────────
        NothingLogTopBar(onBack = onBack)

        // ── Exercise heading ──────────────────────────────────────
        NothingExerciseHeading(exerciseName = state.exercise?.exerciseName ?: "")

        HorizontalDivider(color = NothingColors.Hairline, thickness = 0.5.dp)

        // ── "Add Set N" label ─────────────────────────────────────
        NothingSetSectionLabel(
            label = "ADD SET",
            number = state.sets.size + 1
        )

        // ── Current unsaved set ───────────────────────────────────
        NothingSetRow(
            setNumber = state.sets.size + 1,
            weight = state.currentWeight,
            reps = state.currentReps,
            onWeightChange = { onEvent(LogWorkoutEvent.UpdateWeight(0, it)) },
            onRepsChange   = { onEvent(LogWorkoutEvent.UpdateReps(0, it)) },
            onDeleteSet    = { onEvent(LogWorkoutEvent.DeleteSet(0)) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Add Set button ────────────────────────────────────────
        NothingAddSetButton(
            onClick = { onEvent(LogWorkoutEvent.OnAddingSets(state.exercise?.id ?: 0)) }
        )

        // ── Logged sets ───────────────────────────────────────────
        if (state.sets.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(color = NothingColors.Hairline, thickness = 0.5.dp)

            NothingSetSectionLabel(
                label = "LOGGED SETS",
                number = null,
                count = state.sets.size
            )

            state.sets.forEach { setItem ->
                NothingSetRow(
                    setNumber  = setItem.sets,
                    weight     = state.editingWeights[setItem.id] ?: setItem.weight.toString(),
                    reps       = state.editingReps[setItem.id] ?: setItem.reps.toString(),
                    onWeightChange = { onEvent(LogWorkoutEvent.UpdateWeight(setItem.id, it)) },
                    onRepsChange   = { onEvent(LogWorkoutEvent.UpdateReps(setItem.id, it)) },
                    onDeleteSet    = { onEvent(LogWorkoutEvent.DeleteSet(setItem.id)) }
                )
                HorizontalDivider(
                    color = NothingColors.StrokeWeak,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@Composable
private fun NothingLogTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 20.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = NothingColors.DimWhite,
                modifier = Modifier.size(20.dp)
            )
        }

        // Decorative glyph dots
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(Modifier.size(5.dp).background(NothingColors.GlyphRed, CircleShape))
            Box(Modifier.size(4.dp).background(NothingColors.FaintWhite, CircleShape))
            Box(Modifier.size(4.dp).background(NothingColors.FaintWhite, CircleShape))
        }
    }
}

// ─── Exercise Heading ─────────────────────────────────────────────────────────

@Composable
private fun NothingExerciseHeading(exerciseName: String) {
    Column(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp)
    ) {
        Text(
            text = "LOG",
            color = NothingColors.DimWhite,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = exerciseName.uppercase(),
            color = NothingColors.NothingWhite,
            fontWeight = FontWeight.Black,
            fontSize = 26.sp,
            letterSpacing = (-0.5).sp,
            lineHeight = 28.sp
        )
    }
}

// ─── Section Label ────────────────────────────────────────────────────────────

@Composable
private fun NothingSetSectionLabel(
    label: String,
    number: Int?,          // for "ADD SET 3" — shows the upcoming set number
    count: Int? = null     // for "LOGGED SETS" — shows total count
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
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
                text = if (number != null) "$label $number" else label,
                color = NothingColors.DimWhite,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        count?.let {
            Text(
                text = "$it TOTAL",
                color = NothingColors.FaintWhite,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ─── Set Row ──────────────────────────────────────────────────────────────────

@Composable
fun NothingSetRow(
    setNumber: Int,
    weight: String,
    reps: String,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onDeleteSet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Set number — monospace terminal badge
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(NothingColors.Surface1, RoundedCornerShape(3.dp))
                .border(0.5.dp, NothingColors.Hairline, RoundedCornerShape(3.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = setNumber.toString().padStart(2, '0'),
                color = NothingColors.DimWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Weight field
        NothingInputField(
            value = weight,
            onValueChange = onWeightChange,
            label = "KG",
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next,
            modifier = Modifier.weight(1f)
        )

        // Reps field
        NothingInputField(
            value = reps,
            onValueChange = onRepsChange,
            label = "REPS",
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
            modifier = Modifier.weight(1f)
        )

        // Delete button
        IconButton(
            onClick = onDeleteSet,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(NothingColors.DangerDim)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete set",
                tint = NothingColors.GlyphRed,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ─── Input Field ──────────────────────────────────────────────────────────────

@Composable
private fun NothingInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = {
            Text(
                text = label,
                fontSize = 9.sp,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )
        },
        singleLine = true,
        // Sharp 3dp corners — Nothing's terminal aesthetic
        shape = RoundedCornerShape(3.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor        = NothingColors.NothingWhite,
            unfocusedTextColor      = NothingColors.NothingWhite,
            focusedBorderColor      = NothingColors.NothingWhite,
            unfocusedBorderColor    = NothingColors.Hairline,
            focusedLabelColor       = NothingColors.DimWhite,
            unfocusedLabelColor     = NothingColors.FaintWhite,
            cursorColor             = NothingColors.GlyphRed,
            focusedContainerColor   = NothingColors.Surface0,
            unfocusedContainerColor = NothingColors.Surface0,
        )
    )
}

// ─── Add Set Button ───────────────────────────────────────────────────────────

@Composable
private fun NothingAddSetButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(48.dp)
            .background(NothingColors.Surface1, RoundedCornerShape(3.dp))
            .border(0.5.dp, NothingColors.Hairline, RoundedCornerShape(3.dp))
            // Tap handled by clickable on the scaffold row — wire through onClick
            .dotMatrixBackground(
                dotColor = NothingColors.FaintWhite.copy(alpha = 0.08f),
                spacing = 10f,
                radius = 0.9f
            ),
        contentAlignment = Alignment.Center
    ) {
        // Use a transparent OutlinedButton so ripple + click still work
        androidx.compose.material3.TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = NothingColors.NothingWhite,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ADD SET",
                color = NothingColors.NothingWhite,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ─── Save FAB ─────────────────────────────────────────────────────────────────

@Composable
fun NothingSaveFab(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = NothingColors.NothingWhite,
        contentColor = NothingColors.Void,
        shape = RoundedCornerShape(4.dp),
        // No elevation — Nothing OS is flat
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Save workout",
            tint = NothingColors.Void,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "SAVE",
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            letterSpacing = 3.sp,
            fontFamily = FontFamily.Monospace,
            color = NothingColors.Void
        )
    }
}