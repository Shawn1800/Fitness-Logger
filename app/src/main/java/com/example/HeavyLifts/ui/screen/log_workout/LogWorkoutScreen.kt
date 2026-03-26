package com.example.demo103.ui.screen.log_workout


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demo103.data.entity.ExerciseEntity


private object AppColors {
    val Background    = Color(0xFF101014)
    val Surface       = Color(0xFF1C1C22)
    val SurfaceDimmed = Color(0xFF1C1C22).copy(alpha = 0.4f)
    val Primary       = Color(0xFF8B5CF6)
    val PrimaryDim    = Color(0xFF3B1F72)
    val TextPrimary   = Color(0xFFF1F0FF)
    val TextSecondary = Color(0xFF7B7A8E)
    val Stroke        = Color(0xFF2A2A35)
    val Danger        = Color(0xFFCF6679)   // delete button
}


@Composable
fun LogWorkoutScreen(
    exercise: ExerciseEntity,
    dateMillis: Long,
    logWorkoutViewModel: LogWorkoutViewModel = viewModel(),
    onBack: () -> Unit,
) {
    LaunchedEffect(exercise) {
        logWorkoutViewModel.onEvent(LogWorkoutEvent.SetExercise(exercise, dateMillis = dateMillis))
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
        containerColor = AppColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            SaveBttn(onClick = { logWorkoutViewModel.onEvent(LogWorkoutEvent.SaveWorkout) })
        }
    ) { paddingValues ->
        LogWorkoutContent(
            state = state,
            onEvent = { event -> logWorkoutViewModel.onEvent(event) },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

// ─── Content ──────────────────────────────────────────────────────────────────
@Composable
fun LogWorkoutContent(
    state: LogWorkoutState,
    onEvent: (LogWorkoutEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 88.dp)   // clearance so FAB never overlaps last row
    ) {
        ExerciseName(exerciseName = state.exercise?.exerciseName ?: "")

        Spacer(modifier = Modifier.height(4.dp))

        // Thin divider under the title
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = AppColors.Stroke,
            thickness = 0.5.dp
        )

        // ── "Add Set N" section label ──────────────────────────────
        Text(
            text = "Add Set ${state.sets.size + 1}",
            color = AppColors.Primary,
            modifier = Modifier.padding(top = 16.dp, start = 20.dp, bottom = 8.dp),
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            letterSpacing = 0.6.sp
        )

        // ── Current (unsaved) set row ──────────────────────────────
        ShowSet1(
            setNumber = state.sets.size + 1,
            weight = state.currentWeight,
            reps = state.currentReps,
            onWeightChange = { onEvent(LogWorkoutEvent.UpdateWeight(0, it)) },
            onRepsChange   = { onEvent(LogWorkoutEvent.UpdateReps(0, it)) },
            onDeleteSet    = { onEvent(LogWorkoutEvent.DeleteSet(0)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        AddSets(
            onClick = {
                onEvent(LogWorkoutEvent.OnAddingSets(state.exercise?.exerciseId ?: 0))
            }
        )

        // ── Saved sets ────────────────────────────────────────────
        if (state.sets.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Logged Sets",
                color = AppColors.TextSecondary,
                modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                letterSpacing = 0.6.sp
            )

            state.sets.forEach { setItem ->
                ShowSet1(
                    setNumber  = setItem.sets,
                    weight     = state.editingWeights[setItem.entryId] ?: setItem.weight.toString(),
                    reps       = state.editingReps[setItem.entryId]    ?: setItem.reps.toString(),
                    onWeightChange = { onEvent(LogWorkoutEvent.UpdateWeight(setItem.entryId, it)) },
                    onRepsChange   = { onEvent(LogWorkoutEvent.UpdateReps(setItem.entryId, it)) },
                    onDeleteSet    = { onEvent(LogWorkoutEvent.DeleteSet(setItem.entryId)) }
                )
            }
        }
    }
}

// ─── Exercise Title ───────────────────────────────────────────────────────────
@Composable
fun ExerciseName(exerciseName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, start = 20.dp, end = 20.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Text(
            text = exerciseName,
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp,
            letterSpacing = (-0.5).sp
        )
    }
}

// ─── Set Row ──────────────────────────────────────────────────────────────────
@Composable
fun ShowSet1(
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
            .padding(horizontal = 16.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Set number badge
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AppColors.Primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$setNumber",
                color = AppColors.Primary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        // Weight field
        OutlinedTextField(
            value = weight,
            onValueChange = onWeightChange,
            modifier = Modifier.weight(1f),
            label = { Text("kg", fontSize = 12.sp) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor     = AppColors.TextPrimary,
                unfocusedTextColor   = AppColors.TextPrimary,
                focusedBorderColor   = AppColors.Primary,
                unfocusedBorderColor = AppColors.Stroke,
                focusedLabelColor    = AppColors.Primary,
                unfocusedLabelColor  = AppColors.TextSecondary,
                cursorColor          = AppColors.Primary,
                focusedContainerColor   = AppColors.Surface,
                unfocusedContainerColor = AppColors.Surface,
            )
        )

        // Reps field
        OutlinedTextField(
            value = reps,
            onValueChange = onRepsChange,
            modifier = Modifier.weight(1f),
            label = { Text("reps", fontSize = 12.sp) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor     = AppColors.TextPrimary,
                unfocusedTextColor   = AppColors.TextPrimary,
                focusedBorderColor   = AppColors.Primary,
                unfocusedBorderColor = AppColors.Stroke,
                focusedLabelColor    = AppColors.Primary,
                unfocusedLabelColor  = AppColors.TextSecondary,
                cursorColor          = AppColors.Primary,
                focusedContainerColor   = AppColors.Surface,
                unfocusedContainerColor = AppColors.Surface,
            )
        )

        // Delete button — icon only, no empty label
        IconButton(
            onClick = onDeleteSet,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AppColors.Danger.copy(alpha = 0.12f))
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete set",
                tint = AppColors.Danger,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ─── Add Set Button ───────────────────────────────────────────────────────────
@Composable
fun AddSets(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(48.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AppColors.Primary),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = AppColors.Primary
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Add Set",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

// ─── Save FAB ─────────────────────────────────────────────────────────────────
@Composable
fun SaveBttn(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = AppColors.Primary,
        contentColor = Color.White,
        shape = RoundedCornerShape(18.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Save workout",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Save",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}


