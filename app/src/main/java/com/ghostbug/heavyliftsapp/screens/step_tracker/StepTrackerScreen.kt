package com.ghostbug.heavyliftsapp.screens.step_tracker

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.outlinedButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ─── Nothing OS Color Palette ─────────────────────────────────────────────────

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
    val Positive     = Color(0xFFE8E8E8)
    val Negative     = Color(0xFF666666)
}

// ─── Nothing OS Screen ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepTrackerScreen(
    viewModel: StepTrackerViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.onEvent(StepTrackerEvent.RefreshToday)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is StepTrackerUiEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message)
                StepTrackerUiEvent.GoalSavedSuccess ->
                    snackbarHostState.showSnackbar("Goal saved successfully")
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(StepTrackerEvent.RefreshToday)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = NothingColors.Void,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NothingColors.Void,
                    titleContentColor = NothingColors.NothingWhite,
                    actionIconContentColor = NothingColors.DimWhite
                ),
                title = {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "step",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = NothingColors.NothingWhite,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = ".",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = NothingColors.GlyphRed,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "tracker",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = NothingColors.NothingWhite,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                actions = {
                    // Nothing-style edit button: circular, bordered
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(NothingColors.Surface1, CircleShape)
                            .border(1.dp, NothingColors.Hairline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { viewModel.onEvent(StepTrackerEvent.OpenGoalEditor) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit goal",
                                tint = NothingColors.DimWhite,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                }
            )
        }
    ) { paddingValues ->

        PullToRefreshBox(
            isRefreshing = state.isSyncing,
            onRefresh = { viewModel.onEvent(StepTrackerEvent.RefreshToday) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(NothingColors.Void)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = NothingColors.GlyphRed,
                        trackColor = NothingColors.FaintWhite,
                        strokeWidth = 2.dp
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(Modifier.height(4.dp))

                    StepRingCard(
                        steps = state.todaySteps,
                        stepGoal = state.stepGoal,
                        stepProgress = state.stepProgress,
                        calories = state.todayCalories,
                        distanceKm = state.todayDistanceKm,
                        stepSource = state.stepSource,
                        calorieSource = state.calorieSource
                    )

                    StatsRow(
                        calories = state.todayCalories,
                        distanceKm = state.todayDistanceKm,
                        calorieProgress = state.calorieProgress,
                        calorieGoal = state.calorieGoal,
                        calorieSource = state.calorieSource
                    )

                    if (state.history.isNotEmpty()) {
                        WeeklyBarChart(history = state.history)
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    if (state.isEditingGoal) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(StepTrackerEvent.CloseGoalEditor) },
            sheetState = bottomSheetState,
            containerColor = NothingColors.Surface0,
            dragHandle = {
                // Nothing-style drag handle: short, muted
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 4.dp)
                        .width(36.dp)
                        .height(3.dp)
                        .background(NothingColors.FaintWhite, RoundedCornerShape(99.dp))
                )
            },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            tonalElevation = 0.dp
        ) {
            GoalEditorSheet(
                editingStepGoal = state.editingStepGoal,
                editingCalorieGoal = state.editingCalorieGoal,
                isLoading = state.isLoading,
                onStepGoalChanged = { viewModel.onEvent(StepTrackerEvent.OnStepGoalChanged(it)) },
                onCalorieGoalChanged = { viewModel.onEvent(StepTrackerEvent.OnCalorieGoalChanged(it)) },
                onSave = { viewModel.onEvent(StepTrackerEvent.SaveGoal) },
                onCancel = { viewModel.onEvent(StepTrackerEvent.CloseGoalEditor) }
            )
        }
    }
}

// ─── Step Ring Card ───────────────────────────────────────────────────────────

@Composable
private fun StepRingCard(
    steps: Int,
    stepGoal: Int,
    stepProgress: Float,
    calories: Float,
    distanceKm: Float,
    stepSource: String,
    calorieSource: String
) {
    val glyphRed   = NothingColors.GlyphRed
    val trackColor = NothingColors.FaintWhite

    NothingCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left: ring with step count
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(130.dp)
            ) {
                Canvas(modifier = Modifier.size(130.dp)) {
                    val strokeWidth = 10.dp.toPx()
                    val diameter   = size.minDimension - strokeWidth
                    val topLeft    = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize    = Size(diameter, diameter)

                    drawArc(
                        color       = trackColor,
                        startAngle  = -90f,
                        sweepAngle  = 360f,
                        useCenter   = false,
                        topLeft     = topLeft,
                        size        = arcSize,
                        style       = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color       = glyphRed,
                        startAngle  = -90f,
                        sweepAngle  = 360f * stepProgress.coerceIn(0f, 1f),
                        useCenter   = false,
                        topLeft     = topLeft,
                        size        = arcSize,
                        style       = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = "%,d".format(steps),
                        fontSize   = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color      = NothingColors.NothingWhite,
                        letterSpacing = (-1).sp,
                        lineHeight = 24.sp
                    )
                    Text(
                        text     = "steps today",
                        fontSize = 10.sp,
                        color    = NothingColors.DimWhite,
                        letterSpacing = 0.04.sp
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text       = "${(stepProgress * 100).toInt()}%",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = NothingColors.GlyphRed
                    )
                }
            }

            // Right: quick stats
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text  = "Goal: %,d steps".format(stepGoal),
                    fontSize = 12.sp,
                    color = NothingColors.DimWhite
                )

                NothingMiniStat(label = "CALORIES", value = "${calories.toInt()} kcal")
                NothingMiniStat(label = "DISTANCE", value = "%.2f km".format(distanceKm))

                SourceBadge(source = stepSource)
            }
        }
    }
}

@Composable
private fun NothingMiniStat(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text          = label,
            fontSize      = 10.sp,
            fontWeight    = FontWeight.Medium,
            color         = NothingColors.Negative,
            letterSpacing = 0.08.sp
        )
        Text(
            text       = value,
            fontSize   = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color      = NothingColors.OffWhite
        )
    }
}

// ─── Stats Row ────────────────────────────────────────────────────────────────

@Composable
private fun StatsRow(
    calories: Float,
    distanceKm: Float,
    calorieProgress: Float,
    calorieGoal: Float,
    calorieSource: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            modifier      = Modifier.weight(1f),
            label         = "CALORIES",
            value         = "${calories.toInt()}",
            unit          = "kcal",
            subLabel      = "of ${calorieGoal.toInt()} kcal",
            progress      = calorieProgress,
            progressColor = NothingColors.NothingWhite,
            badge         = calorieSource
        )
        StatCard(
            modifier      = Modifier.weight(1f),
            label         = "DISTANCE",
            value         = "%.2f".format(distanceKm),
            unit          = "km",
            subLabel      = null,
            progress      = null,
            progressColor = Color.Transparent,
            badge         = null
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    unit: String,
    subLabel: String?,
    progress: Float?,
    progressColor: Color,
    badge: String?
) {
    NothingCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text          = label,
                fontSize      = 10.sp,
                fontWeight    = FontWeight.Medium,
                color         = NothingColors.Negative,
                letterSpacing = 0.1.sp
            )
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text          = value,
                    fontSize      = 26.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = NothingColors.NothingWhite,
                    letterSpacing = (-0.5).sp,
                    lineHeight    = 26.sp
                )
                Text(
                    text     = unit,
                    fontSize = 13.sp,
                    color    = NothingColors.DimWhite,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
            if (subLabel != null) {
                Text(
                    text     = subLabel,
                    fontSize = 11.sp,
                    color    = NothingColors.DimWhite
                )
            }
            if (progress != null) {
                Spacer(Modifier.height(6.dp))
                NothingLinearProgressBar(
                    progress = progress,
                    color    = progressColor
                )
            }
            if (badge != null) {
                Spacer(Modifier.height(6.dp))
                SourceBadge(source = badge)
            }
        }
    }
}

// ─── Weekly Bar Chart ─────────────────────────────────────────────────────────

@Composable
private fun WeeklyBarChart(
    history: List<com.ghostbug.heavyliftsapp.data.domain.DailyActivity>
) {
    val primaryColor = NothingColors.GlyphRed
    val trackColor   = NothingColors.FaintWhite
    val maxSteps     = history.maxOfOrNull { it.steps }?.toFloat() ?: 1f

    NothingCard {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text       = "last 7 days",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = NothingColors.NothingWhite,
                    letterSpacing = (-0.3).sp
                )
                Text(
                    text     = "%,d total".format(history.take(7).sumOf { it.steps }),
                    fontSize = 11.sp,
                    color    = NothingColors.Negative
                )
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                history.take(7).reversed().forEach { activity ->
                    val barProgress = if (maxSteps > 0) activity.steps / maxSteps else 0f

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .width(18.dp)
                                .weight(1f)
                        ) {
                            val barHeight    = size.height * barProgress
                            val cornerRadius = CornerRadius(5.dp.toPx())

                            drawRoundRect(
                                color        = trackColor,
                                size         = Size(size.width, size.height),
                                cornerRadius = cornerRadius
                            )
                            if (barProgress > 0f) {
                                drawRoundRect(
                                    color        = primaryColor,
                                    topLeft      = Offset(0f, size.height - barHeight),
                                    size         = Size(size.width, barHeight),
                                    cornerRadius = cornerRadius
                                )
                            }
                        }

                        Spacer(Modifier.height(5.dp))

                        Text(
                            text      = activity.date.dayOfWeek.name.take(1),
                            fontSize  = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color     = NothingColors.DimWhite,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.05.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(NothingColors.Hairline)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text     = "Total  %,d".format(history.take(7).sumOf { it.steps }),
                    fontSize = 11.sp,
                    color    = NothingColors.Negative
                )
                Text(
                    text = "Avg  %,d/day".format(
                        if (history.take(7).isNotEmpty())
                            history.take(7).sumOf { it.steps } / history.take(7).size
                        else 0
                    ),
                    fontSize = 11.sp,
                    color    = NothingColors.Negative
                )
            }
        }
    }
}

// ─── Goal Editor Bottom Sheet ─────────────────────────────────────────────────

@Composable
private fun GoalEditorSheet(
    editingStepGoal: String,
    editingCalorieGoal: String,
    isLoading: Boolean,
    onStepGoalChanged: (String) -> Unit,
    onCalorieGoalChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title with red period
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text       = "set daily goals",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = NothingColors.NothingWhite,
                letterSpacing = (-0.5).sp
            )
            Text(
                text       = ".",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = NothingColors.GlyphRed
            )
        }
        Text(
            text     = "Customize your targets",
            fontSize = 12.sp,
            color    = NothingColors.Negative
        )

        NothingTextField(
            value         = editingStepGoal,
            onValueChange = onStepGoalChanged,
            label         = "STEP GOAL",
            placeholder   = "e.g. 10000",
            supporting    = "Minimum 1,000 steps"
        )

        NothingTextField(
            value         = editingCalorieGoal,
            onValueChange = onCalorieGoalChanged,
            label         = "CALORIE GOAL",
            placeholder   = "e.g. 500",
            supporting    = "Active calories to burn"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick  = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                border   = androidx.compose.foundation.BorderStroke(1.dp, NothingColors.Hairline),
                colors   = outlinedButtonColors(
                    contentColor = NothingColors.DimWhite
                )
            ) {
                Text(
                    text       = "Cancel",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick  = onSave,
                enabled  = !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = NothingColors.GlyphRed,
                    contentColor   = NothingColors.Void,
                    disabledContainerColor = NothingColors.GlyphRedDim,
                    disabledContentColor   = NothingColors.DimWhite
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier  = Modifier.size(18.dp),
                        color     = NothingColors.Void,
                        trackColor = NothingColors.GlyphRedDim,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text       = "Save goal",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.02.sp
                    )
                }
            }
        }
    }
}

// ─── Shared Nothing-style composables ─────────────────────────────────────────

/**
 * Nothing OS card: dark surface, 1dp hairline border, 0 elevation, large radius.
 */
@Composable
private fun NothingCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = NothingColors.Surface0),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp, NothingColors.Hairline)
    ) {
        content()
    }
}

/**
 * Nothing OS text field: dark surface, red focus border, all-caps label.
 */
@Composable
private fun NothingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    supporting: String
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = {
            Text(
                text          = label,
                fontSize      = 10.sp,
                fontWeight    = FontWeight.Medium,
                letterSpacing = 0.1.sp
            )
        },
        placeholder   = {
            Text(
                text     = placeholder,
                color    = NothingColors.Negative,
                fontSize = 15.sp
            )
        },
        supportingText = {
            Text(
                text     = supporting,
                color    = NothingColors.Negative,
                fontSize = 11.sp
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier        = Modifier.fillMaxWidth(),
        singleLine      = true,
        shape           = RoundedCornerShape(14.dp),
        textStyle       = TextStyle(
            color      = NothingColors.NothingWhite,
            fontSize   = 16.sp,
            fontWeight = FontWeight.Medium
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor       = NothingColors.GlyphRed,
            unfocusedBorderColor     = NothingColors.Hairline,
            focusedLabelColor        = NothingColors.GlyphRed,
            unfocusedLabelColor      = NothingColors.Negative,
            cursorColor              = NothingColors.GlyphRed,
            focusedContainerColor    = NothingColors.Surface1,
            unfocusedContainerColor  = NothingColors.Surface1,
            disabledContainerColor   = NothingColors.Surface1
        )
    )
}

// ─── Source Badge ─────────────────────────────────────────────────────────────

@Composable
private fun SourceBadge(source: String) {
    val (label, dotColor) = when (source) {
        "health_connect" -> "HEALTH CONNECT" to NothingColors.GlyphRed
        "sensor"         -> "PHONE SENSOR"   to NothingColors.OffWhite
        else             -> "ESTIMATED"      to NothingColors.Negative
    }

    val badgeBg = when (source) {
        "health_connect" -> NothingColors.GlyphRedDim
        "sensor"         -> NothingColors.Surface2
        else             -> NothingColors.Surface2
    }

    val borderColor = when (source) {
        "health_connect" -> NothingColors.GlyphRed.copy(alpha = 0.25f)
        else             -> NothingColors.Hairline
    }

    Row(
        modifier = Modifier
            .background(badgeBg, RoundedCornerShape(99.dp))
            .border(1.dp, borderColor, RoundedCornerShape(99.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(dotColor, CircleShape)
        )
        Text(
            text          = label,
            fontSize      = 10.sp,
            fontWeight    = FontWeight.SemiBold,
            color         = if (source == "health_connect") NothingColors.GlyphRed else NothingColors.DimWhite,
            letterSpacing = 0.06.sp
        )
    }
}

// ─── Linear Progress Bar ──────────────────────────────────────────────────────

@Composable
private fun NothingLinearProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val trackColor = NothingColors.FaintWhite
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
    ) {
        val cornerRadius = CornerRadius(99.dp.toPx())
        drawRoundRect(color = trackColor, cornerRadius = cornerRadius)
        if (progress > 0f) {
            drawRoundRect(
                color        = color,
                size         = Size(size.width * progress.coerceIn(0f, 1f), size.height),
                cornerRadius = cornerRadius
            )
        }
    }
}