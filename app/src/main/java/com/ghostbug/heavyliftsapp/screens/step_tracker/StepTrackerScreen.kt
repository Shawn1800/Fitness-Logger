package com.ghostbug.heavyliftsapp.screens.step_tracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.ghostbug.heavyliftsapp.ui.theme.HeavyLiftsColors
import com.ghostbug.heavyliftsapp.ui.theme.HeavyLiftsType

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
        containerColor = HeavyLiftsColors.Bg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HeavyLiftsColors.Bg,
                    titleContentColor = HeavyLiftsColors.Fg1,
                    actionIconContentColor = HeavyLiftsColors.Fg3
                ),
                title = {
                    Text(
                        text = "Step tracker",
                        fontFamily = HeavyLiftsType.Display,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = HeavyLiftsColors.Fg1,
                        letterSpacing = (-0.25).sp
                    )
                },
                actions = {
                    // Nothing-style edit button: circular, bordered
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(HeavyLiftsColors.BgChip, CircleShape)
                            .border(1.dp, HeavyLiftsColors.BorderSubtle, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { viewModel.onEvent(StepTrackerEvent.OpenGoalEditor) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit goal",
                                tint = HeavyLiftsColors.Fg3,
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
                .background(HeavyLiftsColors.Bg)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = HeavyLiftsColors.Accent,
                        trackColor = HeavyLiftsColors.BgChip,
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
                        WeeklyBarChart(history = state.history, stepGoal = state.stepGoal)
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
            containerColor = HeavyLiftsColors.BgElevated,
            dragHandle = {
                // Nothing-style drag handle: short, muted
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 4.dp)
                        .width(36.dp)
                        .height(3.dp)
                        .background(HeavyLiftsColors.BgChip, RoundedCornerShape(99.dp))
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
    val glyphRed   = HeavyLiftsColors.Accent
    val trackColor = HeavyLiftsColors.BgChip

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
                        color      = HeavyLiftsColors.Fg1,
                        letterSpacing = (-1).sp,
                        lineHeight = 24.sp
                    )
                    Text(
                        text     = "steps today",
                        fontSize = 10.sp,
                        color    = HeavyLiftsColors.Fg3,
                        letterSpacing = 0.04.sp
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text       = "${(stepProgress * 100).toInt()}%",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = HeavyLiftsColors.Accent
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
                    color = HeavyLiftsColors.Fg3
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
            color         = HeavyLiftsColors.Fg4,
            letterSpacing = 0.08.sp
        )
        Text(
            text       = value,
            fontSize   = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color      = HeavyLiftsColors.Fg2
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
            progressColor = HeavyLiftsColors.Fg1,
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
                color         = HeavyLiftsColors.Fg4,
                letterSpacing = 0.1.sp
            )
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text          = value,
                    fontSize      = 26.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = HeavyLiftsColors.Fg1,
                    letterSpacing = (-0.5).sp,
                    lineHeight    = 26.sp
                )
                Text(
                    text     = unit,
                    fontSize = 13.sp,
                    color    = HeavyLiftsColors.Fg3,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
            if (subLabel != null) {
                Text(
                    text     = subLabel,
                    fontSize = 11.sp,
                    color    = HeavyLiftsColors.Fg3
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
    history: List<com.ghostbug.heavyliftsapp.data.domain.DailyActivity>,
    stepGoal: Int = 10_000
) {
    val days      = history.take(7)
    val weekDays  = days.reversed()
    val rawMax    = days.maxOfOrNull { it.steps }?.toFloat() ?: 0f
    val maxSteps  = maxOf(rawMax * 1.05f, stepGoal.toFloat())
    val goalFrac  = (stepGoal.toFloat() / maxSteps).coerceIn(0f, 1f)
    val todayDate = days.firstOrNull()?.date

    val totalSteps   = days.sumOf { it.steps }
    val avgSteps     = if (days.isNotEmpty()) totalSteps / days.size else 0
    val bestSteps    = days.maxOfOrNull { it.steps } ?: 0
    val daysHitGoal  = days.count { it.steps >= stepGoal }
    val hitGoal      = daysHitGoal > 0

    val barColor      = HeavyLiftsColors.Accent
    val dimBarColor   = HeavyLiftsColors.Accent.copy(alpha = 0.35f)
    val gridColor     = HeavyLiftsColors.BorderSubtle
    val goalDashColor = HeavyLiftsColors.Accent.copy(alpha = 0.55f)

    NothingCard {
        Column(modifier = Modifier.padding(20.dp)) {

            // ── Header ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text          = "weekly activity",
                        fontSize      = 14.sp,
                        fontWeight    = FontWeight.SemiBold,
                        color         = HeavyLiftsColors.Fg1,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text          = "last 7 days",
                        fontSize      = 10.sp,
                        color         = HeavyLiftsColors.Fg3,
                        letterSpacing = 0.3.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .background(
                            if (hitGoal) HeavyLiftsColors.AccentSoft else HeavyLiftsColors.BgOverlay,
                            RoundedCornerShape(99.dp)
                        )
                        .border(
                            1.dp,
                            if (hitGoal) HeavyLiftsColors.Accent.copy(alpha = 0.3f) else HeavyLiftsColors.BorderSubtle,
                            RoundedCornerShape(99.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(
                                if (hitGoal) HeavyLiftsColors.Accent else HeavyLiftsColors.Fg3,
                                CircleShape
                            )
                    )
                    Text(
                        text          = "$daysHitGoal/7 goals",
                        fontSize      = 10.sp,
                        fontWeight    = FontWeight.SemiBold,
                        color         = if (hitGoal) HeavyLiftsColors.Accent else HeavyLiftsColors.Fg3,
                        letterSpacing = 0.05.sp,
                        fontFamily    = FontFamily.Monospace
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── Chart ─────────────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth()) {

                // Y-axis labels aligned to bar area (offset by step-label row: 14dp + 2dp spacer)
                Column(
                    modifier = Modifier
                        .width(30.dp)
                        .padding(top = 16.dp)
                        .height(110.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(maxSteps, maxSteps * 0.5f, 0f).forEach { v ->
                        Text(
                            text      = barChartCompactSteps(v.toInt()),
                            fontSize  = 8.sp,
                            color     = HeavyLiftsColors.Fg3,
                            fontFamily = HeavyLiftsType.Body,
                            textAlign = TextAlign.End,
                            modifier  = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {

                    // Step count labels above each bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        weekDays.forEach { activity ->
                            val isToday = activity.date == todayDate
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (activity.steps > 0) {
                                    Text(
                                        text       = barChartCompactSteps(activity.steps),
                                        fontSize   = 7.sp,
                                        color      = if (isToday) HeavyLiftsColors.Fg1 else HeavyLiftsColors.Fg3,
                                        fontFamily = HeavyLiftsType.Body,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                        textAlign  = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(2.dp))

                    // Bar area with overlaid gridlines + goal line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Subtle horizontal gridlines at 0%, 50%, 100%
                            listOf(0f, 0.5f, 1f).forEach { fraction ->
                                val y = size.height * (1f - fraction)
                                drawLine(
                                    color       = gridColor,
                                    start       = Offset(0f, y),
                                    end         = Offset(size.width, y),
                                    strokeWidth = 0.5.dp.toPx()
                                )
                            }
                            // Dashed goal line
                            val goalY = size.height * (1f - goalFrac)
                            val dash = 6.dp.toPx()
                            val gap  = 3.dp.toPx()
                            var x = 0f
                            while (x < size.width) {
                                drawLine(
                                    color       = goalDashColor,
                                    start       = Offset(x, goalY),
                                    end         = Offset(minOf(x + dash, size.width), goalY),
                                    strokeWidth = 1.dp.toPx()
                                )
                                x += dash + gap
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            weekDays.forEach { activity ->
                                val isToday   = activity.date == todayDate
                                val frac      = if (maxSteps > 0) (activity.steps / maxSteps).coerceIn(0f, 1f) else 0f
                                val fillColor = if (isToday) barColor else dimBarColor

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Canvas(
                                        modifier = Modifier
                                            .width(if (isToday) 20.dp else 15.dp)
                                            .fillMaxHeight()
                                    ) {
                                        val corner = CornerRadius(4.dp.toPx())
                                        drawRoundRect(
                                            color        = HeavyLiftsColors.BgChip,
                                            size         = Size(size.width, size.height),
                                            cornerRadius = corner
                                        )
                                        if (frac > 0f) {
                                            val fillH = size.height * frac
                                            drawRoundRect(
                                                color        = fillColor,
                                                topLeft      = Offset(0f, size.height - fillH),
                                                size         = Size(size.width, fillH),
                                                cornerRadius = corner
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Day labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        weekDays.forEach { activity ->
                            val isToday = activity.date == todayDate
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activity.date.dayOfWeek.name
                                        .take(3)
                                        .lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    fontSize   = 8.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color      = if (isToday) HeavyLiftsColors.Accent else HeavyLiftsColors.Fg3,
                                    fontFamily = HeavyLiftsType.Body,
                                    textAlign  = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Goal line legend
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(modifier = Modifier.width(14.dp).height(8.dp)) {
                    val dash = 4.dp.toPx(); val gap = 2.dp.toPx(); var cx = 0f
                    while (cx < size.width) {
                        drawLine(
                            color       = goalDashColor,
                            start       = Offset(cx, size.height / 2),
                            end         = Offset(minOf(cx + dash, size.width), size.height / 2),
                            strokeWidth = 1.dp.toPx()
                        )
                        cx += dash + gap
                    }
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text      = "goal · %,d steps".format(stepGoal),
                    fontSize  = 9.sp,
                    color     = HeavyLiftsColors.Fg3,
                    fontFamily = HeavyLiftsType.Body
                )
            }

            Spacer(Modifier.height(12.dp))

            Box(Modifier.fillMaxWidth().height(1.dp).background(HeavyLiftsColors.BorderSubtle))

            Spacer(Modifier.height(12.dp))

            // Footer: 3 stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BarChartFooterStat("TOTAL",    "%,d".format(totalSteps))
                BarChartFooterStat("AVG / DAY", "%,d".format(avgSteps))
                BarChartFooterStat("BEST DAY", "%,d".format(bestSteps))
            }
        }
    }
}

private fun barChartCompactSteps(steps: Int): String = when {
    steps <= 0      -> "—"
    steps >= 1_000  -> "%.1fk".format(steps / 1000f)
    else            -> steps.toString()
}

@Composable
private fun BarChartFooterStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text          = label,
            fontSize      = 8.sp,
            color         = HeavyLiftsColors.Fg3,
            letterSpacing = 1.sp,
            fontFamily    = FontFamily.Monospace
        )
        Text(
            text       = value,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Bold,
            color      = HeavyLiftsColors.Fg2,
            fontFamily = HeavyLiftsType.Body
        )
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
                color      = HeavyLiftsColors.Fg1,
                letterSpacing = (-0.5).sp
            )
            Text(
                text       = ".",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = HeavyLiftsColors.Accent
            )
        }
        Text(
            text     = "Customize your targets",
            fontSize = 12.sp,
            color    = HeavyLiftsColors.Fg4
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
                border   = androidx.compose.foundation.BorderStroke(1.dp, HeavyLiftsColors.BorderSubtle),
                colors   = outlinedButtonColors(
                    contentColor = HeavyLiftsColors.Fg3
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
                    containerColor = HeavyLiftsColors.Accent,
                    contentColor   = HeavyLiftsColors.Bg,
                    disabledContainerColor = HeavyLiftsColors.AccentSoft,
                    disabledContentColor   = HeavyLiftsColors.Fg3
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier  = Modifier.size(18.dp),
                        color     = HeavyLiftsColors.Bg,
                        trackColor = HeavyLiftsColors.AccentSoft,
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
        colors    = CardDefaults.cardColors(containerColor = HeavyLiftsColors.BgElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp, HeavyLiftsColors.BorderSubtle)
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
                color    = HeavyLiftsColors.Fg4,
                fontSize = 15.sp
            )
        },
        supportingText = {
            Text(
                text     = supporting,
                color    = HeavyLiftsColors.Fg4,
                fontSize = 11.sp
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier        = Modifier.fillMaxWidth(),
        singleLine      = true,
        shape           = RoundedCornerShape(14.dp),
        textStyle       = TextStyle(
            color      = HeavyLiftsColors.Fg1,
            fontSize   = 16.sp,
            fontWeight = FontWeight.Medium
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor       = HeavyLiftsColors.Accent,
            unfocusedBorderColor     = HeavyLiftsColors.BorderSubtle,
            focusedLabelColor        = HeavyLiftsColors.Accent,
            unfocusedLabelColor      = HeavyLiftsColors.Fg4,
            cursorColor              = HeavyLiftsColors.Accent,
            focusedContainerColor    = HeavyLiftsColors.BgChip,
            unfocusedContainerColor  = HeavyLiftsColors.BgChip,
            disabledContainerColor   = HeavyLiftsColors.BgChip
        )
    )
}

// ─── Source Badge ─────────────────────────────────────────────────────────────

@Composable
private fun SourceBadge(source: String) {
    val (label, dotColor) = when (source) {
        "health_connect" -> "HEALTH CONNECT" to HeavyLiftsColors.Accent
        "sensor"         -> "PHONE SENSOR"   to HeavyLiftsColors.Fg2
        else             -> "ESTIMATED"      to HeavyLiftsColors.Fg4
    }

    val badgeBg = when (source) {
        "health_connect" -> HeavyLiftsColors.AccentSoft
        "sensor"         -> HeavyLiftsColors.BgOverlay
        else             -> HeavyLiftsColors.BgOverlay
    }

    val borderColor = when (source) {
        "health_connect" -> HeavyLiftsColors.Accent.copy(alpha = 0.25f)
        else             -> HeavyLiftsColors.BorderSubtle
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
            color         = if (source == "health_connect") HeavyLiftsColors.Accent else HeavyLiftsColors.Fg3,
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
    val trackColor = HeavyLiftsColors.BgChip
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