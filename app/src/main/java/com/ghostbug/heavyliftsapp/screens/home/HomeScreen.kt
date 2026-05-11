package com.ghostbug.heavyliftsapp.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ghostbug.heavyliftsapp.screens.signUp.SignUpEvent
import com.ghostbug.heavyliftsapp.screens.signUp.SignUpUiEvent
import com.ghostbug.heavyliftsapp.screens.signUp.SignUpViewModel
import com.ghostbug.heavyliftsapp.ui.theme.HeavyLiftsColors
import com.ghostbug.heavyliftsapp.ui.theme.HeavyLiftsType
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

// ─── Screen ──────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    signUpViewModel: SignUpViewModel = viewModel(),
    onNavigateToExerciseSelection: () -> Unit,
    onNavigateToLogWorkout: (Long) -> Unit,
    onNavigateToLogIn: () -> Unit,
    onNavigateToStepTracker: () -> Unit
) {
    val state by homeViewModel.state.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        homeViewModel.onEvent(HomeEvent.RefreshWorkouts)
        launch {
            signUpViewModel.uiEvent.collect { event ->
                if (event is SignUpUiEvent.NavigateToSignIn) onNavigateToLogIn()
            }
        }
        launch {
            homeViewModel.uiEvent.collect { event ->
                when (event) {
                    is HomeUiEvent.NavigateToExerciseSelection -> onNavigateToExerciseSelection()
                    is HomeUiEvent.NavigateToLogWorkout -> onNavigateToLogWorkout(event.exerciseId)
                    is HomeUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                    is HomeUiEvent.NavigateToStepTracker -> onNavigateToStepTracker()
                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                homeViewModel.onEvent(HomeEvent.GetSteps)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (showLogoutDialog) {
        HeavyLogoutDialog(
            onDismissRequest = { showLogoutDialog = false },
            onLogoutClick = {
                showLogoutDialog = false
                signUpViewModel.onEvent(SignUpEvent.OnNavigateToSignIn)
            }
        )
    }

    Scaffold(
        containerColor = HeavyLiftsColors.Bg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        HomeContent(
            state = state,
            paddingValues = paddingValues,
            onDateSelected = { date -> homeViewModel.onEvent(HomeEvent.OnDateSelected(date)) },
            onEvent = { homeViewModel.onEvent(it) },
            onStepClick = { onNavigateToStepTracker() }
        )
    }
}

// ─── Content Layout ───────────────────────────────────────────────────────────

@Composable
private fun HomeContent(
    state: HomeState,
    paddingValues: PaddingValues,
    onDateSelected: (LocalDate) -> Unit,
    onEvent: (HomeEvent) -> Unit,
    onStepClick: () -> Unit
) {
    val horizontalPagerState = rememberPagerState(pageCount = { 2 })

    val totalWeeks = 500
    val pagerState = rememberPagerState(
        pageCount = { totalWeeks },
        initialPage = totalWeeks - 1
    )

    val currentMonthName = remember(pagerState.currentPage) {
        val weeksAgo = (totalWeeks - 1) - pagerState.currentPage
        Clock.System.todayIn(TimeZone.currentSystemDefault())
            .minus(weeksAgo * 7, DateTimeUnit.DAY).month.name
            .lowercase().replaceFirstChar { it.uppercase() }
    }

    val currentYear = remember(pagerState.currentPage) {
        val weeksAgo = (totalWeeks - 1) - pagerState.currentPage
        Clock.System.todayIn(TimeZone.currentSystemDefault())
            .minus(weeksAgo * 7, DateTimeUnit.DAY).year.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(HeavyLiftsColors.Bg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Header ─────────────────────────────────────────────────────────
        HeavyHeader(monthName = currentMonthName, year = currentYear)

        HorizontalDivider(
            color = HeavyLiftsColors.BorderSubtle,
            thickness = 0.5.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Weekly calendar pager ───────────────────────────────────────────
        WeeklyCalendar(
            pagerState = pagerState,
            selectedDate = state.selectedDate,
            onDateSelected = onDateSelected
        )

        Spacer(modifier = Modifier.height(14.dp))

        // ── Horizontal swipe pager (Activity ↔ Sessions) ───────────────────
        HorizontalPager(
            state = horizontalPagerState,
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) { page ->
            when (page) {
                0 -> {
                    Column {
                        Text(
                            text = "Swipe to log exercises →",
                            fontSize = 12.sp,
                            color = HeavyLiftsColors.Fg3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            textAlign = TextAlign.Center
                        )
                        ActivityBanner(state = state, onClick = onStepClick)
                    }
                }
                1 -> {
                    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(HeavyLiftsColors.Accent, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SESSIONS",
                                    color = HeavyLiftsColors.Fg3,
                                    fontFamily = HeavyLiftsType.Display,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.5.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(modifier = Modifier.weight(1f)) {
                                when {
                                    state.isLoading -> HeavyLoadingIndicator()
                                    state.errorMessage != null -> HeavyErrorMessage(message = state.errorMessage)
                                    state.workouts.isEmpty() -> HeavyEmptyState()
                                    else -> WorkoutList(
                                        workouts = state.workouts,
                                        onEvent = onEvent,
                                        state = state
                                    )
                                }
                            }
                        }

                        // FAB overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 16.dp)
                        ) {
                            HeavyFab(
                                onClick = {
                                    if (today == state.selectedDate) onEvent(HomeEvent.OnAddWorkoutClick)
                                    else onEvent(HomeEvent.Message("Previous dates are read only"))
                                },
                                state = state
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun HeavyHeader(monthName: String, year: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp),
    ) {
        Text(
            text = year,
            color = HeavyLiftsColors.Fg3,
            fontFamily = HeavyLiftsType.Display,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
        Text(
            text = monthName,
            color = HeavyLiftsColors.Fg1,
            fontFamily = HeavyLiftsType.Display,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            letterSpacing = (-0.5).sp,
            lineHeight = 36.sp
        )
    }
}

// ─── Activity Banner — 3 rings ────────────────────────────────────────────────

@Composable
private fun ActivityBanner(state: HomeState, onClick: () -> Unit) {
    val stepFill = state.stepProgress.coerceIn(0f, 1f)
    val calFill = (state.todayCalories / 500f).coerceIn(0f, 1f)
    val distFill = (state.todayDistanceKm / 10f).coerceIn(0f, 1f)

    val animStep by animateFloatAsState(targetValue = stepFill, animationSpec = tween(1000), label = "steps")
    val animCal  by animateFloatAsState(targetValue = calFill,  animationSpec = tween(1000), label = "cal")
    val animDist by animateFloatAsState(targetValue = distFill, animationSpec = tween(1000), label = "dist")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(HeavyLiftsColors.BgElevated, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "TODAY",
                    color = HeavyLiftsColors.Fg3,
                    fontFamily = HeavyLiftsType.Display,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.weight(1f))
                Text("›", color = HeavyLiftsColors.Fg3, fontSize = 16.sp)
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActivityRing(
                    progress = animStep,
                    color = HeavyLiftsColors.Accent,
                    value = "%,d".format(state.todaySteps),
                    unit = "steps"
                )
                ActivityRing(
                    progress = animCal,
                    color = HeavyLiftsColors.Success,
                    value = "${state.todayCalories.toInt()}",
                    unit = "kcal"
                )
                ActivityRing(
                    progress = animDist,
                    color = HeavyLiftsColors.Info,
                    value = "%.1f".format(state.todayDistanceKm),
                    unit = "km"
                )
            }
        }
    }
}

@Composable
private fun ActivityRing(progress: Float, color: Color, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(88.dp)) {
            Canvas(modifier = Modifier.size(88.dp)) {
                val strokeWidth = 8.dp.toPx()
                val inset = strokeWidth / 2f
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = Offset(inset, inset)
                drawArc(
                    color = Color.White.copy(alpha = 0.1f),
                    startAngle = -90f, sweepAngle = 360f, useCenter = false,
                    topLeft = topLeft, size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                if (progress > 0f) {
                    drawArc(
                        color = color,
                        startAngle = -90f, sweepAngle = 360f * progress, useCenter = false,
                        topLeft = topLeft, size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    color = HeavyLiftsColors.Fg1,
                    fontFamily = HeavyLiftsType.Display,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp
                )
                Text(
                    text = unit,
                    color = HeavyLiftsColors.Fg3,
                    fontFamily = HeavyLiftsType.Display,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ─── Logout Dialog ────────────────────────────────────────────────────────────

@Composable
fun HeavyLogoutDialog(onDismissRequest: () -> Unit, onLogoutClick: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeavyLiftsColors.BgElevated, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Text(
                text = "Sign out",
                color = HeavyLiftsColors.Fg1,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "You're about to sign out. Your data will remain intact.",
                color = HeavyLiftsColors.Fg3,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HeavyLiftsColors.Fg2),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HeavyLiftsColors.Accent,
                        contentColor = HeavyLiftsColors.Fg1
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Sign out", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── Weekly Calendar ──────────────────────────────────────────────────────────

@Composable
private fun WeeklyCalendar(
    pagerState: PagerState,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val totalWeeks = 500
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) { page ->
        val weeksAgo = (totalWeeks - 1) - page
        val dateInWeek = Clock.System.todayIn(TimeZone.currentSystemDefault())
            .minus(weeksAgo * 7, DateTimeUnit.DAY)
        val mondayOfWeek = dateInWeek.minus(dateInWeek.dayOfWeek.ordinal, DateTimeUnit.DAY)

        WeekRow(
            startDate = mondayOfWeek,
            selectedDate = selectedDate,
            onDateSelected = onDateSelected
        )
    }
}

@Composable
private fun WeekRow(
    startDate: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0..6) {
            val date = startDate.plus(i, DateTimeUnit.DAY)
            HeavyDateItem(
                modifier = Modifier.weight(1f),
                date = date,
                isToday = date == today,
                isSelected = date == selectedDate,
                isFuture = date > today,
                onDateSelected = onDateSelected
            )
        }
    }
}

@Composable
private fun HeavyDateItem(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    isFuture: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayName = date.dayOfWeek.name.take(1)
    val dayNumber = date.day.toString()

    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> HeavyLiftsColors.Accent
            isToday    -> HeavyLiftsColors.BgElevated
            else       -> HeavyLiftsColors.BgElevated
        },
        animationSpec = tween(150),
        label = "dateColor"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .alpha(if (isFuture) 0.25f else 1f)
            .clickable(
                enabled = !isFuture,
//                interactionSource = remember { MutableInteractionSource() },
//                indication = null
            )
                { onDateSelected(date) }
                    .then(
                        if (isSelected) Modifier.drawBehind {
                            drawRoundRect(
                                color = Color(0x38000000),
                                topLeft = Offset(0f, 6.dp.toPx()),
                                size = size,
                                cornerRadius = CornerRadius(20.dp.toPx())
                            )
                        } else Modifier
                    )
                    .heightIn(min = 68.dp)
                    .background(bgColor, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                ) {
                    Text(
                        text = dayName,
                        color = if (isSelected) HeavyLiftsColors.Fg1.copy(alpha = 0.85f) else HeavyLiftsColors.Fg3,
                        fontFamily = HeavyLiftsType.Display,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dayNumber,
                        color = when {
                            isSelected -> HeavyLiftsColors.Fg1
                            isToday -> HeavyLiftsColors.Fg1
                            else -> HeavyLiftsColors.Fg2
                        },
                        fontFamily = HeavyLiftsType.Display,
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                    if (isToday && !isSelected) {
                        Box(
                            modifier = Modifier
                                .background(HeavyLiftsColors.Accent, CircleShape)
                        )
                    }
                }
            }
}

// ─── FAB ─────────────────────────────────────────────────────────────────────

@Composable
private fun HeavyFab(onClick: () -> Unit, state: HomeState) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val isToday = today == state.selectedDate
    FloatingActionButton(
        onClick = onClick,
        containerColor = if (isToday) HeavyLiftsColors.Accent else HeavyLiftsColors.BgChip,
        contentColor = HeavyLiftsColors.Fg1,
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Workout",
            tint = HeavyLiftsColors.Fg1,
            modifier = Modifier.size(24.dp)
        )
    }
}

// ─── Workout List ─────────────────────────────────────────────────────────────

@Composable
fun WorkoutList(
    workouts: List<GroupedWorkout>,
    onEvent: (HomeEvent) -> Unit,
    state: HomeState,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(workouts.size) {
        if (workouts.isNotEmpty()) listState.animateScrollToItem(workouts.lastIndex)
    }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
    ) {
        items(
            items = workouts,
            key = { it.exercise.id }
        ) { workout ->
            HeavyWorkoutCard(
                state = state,
                workout = workout,
                onAddSetClick = if (today == state.selectedDate) {
                    { onEvent(HomeEvent.EditWorkout(workout.exercise.id)) }
                } else {
                    { onEvent(HomeEvent.Message("Previous dates are read only")) }
                }
            )
        }
    }
}

// ─── Workout Card ─────────────────────────────────────────────────────────────

@Composable
private fun HeavyWorkoutCard(
    workout: GroupedWorkout,
    state: HomeState,
    onAddSetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(200),
        label = "chevron"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp)) // 👈 important
            .fillMaxWidth()
            .background(
                if (expanded) HeavyLiftsColors.BgElevated else HeavyLiftsColors.BgElevated,
                RoundedCornerShape(20.dp)
            )
            .clickable {
                expanded = !expanded

            }
    ) {
        // ── Card header ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(36.dp)
                        .background(
                            if (expanded) HeavyLiftsColors.Accent else HeavyLiftsColors.BgChip,
                            RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = workout.exercise.exerciseName,
                        color = HeavyLiftsColors.Fg1,
                        fontFamily = HeavyLiftsType.Display,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = workout.exercise.category,
                        color = HeavyLiftsColors.Fg3,
                        fontFamily = HeavyLiftsType.Body,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                workout.changePercent?.let { percent ->
                    val isPositive = percent >= 0
                    Text(
                        text = (if (isPositive) "+" else "") + "%.0f%%".format(percent),
                        color = if (isPositive) HeavyLiftsColors.Green500 else HeavyLiftsColors.Red500,
                        fontFamily = HeavyLiftsType.Display,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.sp
                    )
                }
                Text(
                    text = "${workout.sets.size}×",
                    color = HeavyLiftsColors.Fg3,
                    fontFamily = HeavyLiftsType.Display,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = HeavyLiftsColors.Fg3,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(chevronRotation)
                )
            }
        }

        // ── Expanded content ──────────────────────────────────────────────
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(220)) + fadeIn(tween(220)),
            exit = shrinkVertically(tween(180)) + fadeOut(tween(180))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        HeavyLiftsColors.BgOverlay,
                        RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                    )
            ) {
                // Sets header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Set",    color = HeavyLiftsColors.Fg4, fontFamily = HeavyLiftsType.Display, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                    Text("Weight", color = HeavyLiftsColors.Fg4, fontFamily = HeavyLiftsType.Display, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                    Text("Reps",   color = HeavyLiftsColors.Fg4, fontFamily = HeavyLiftsType.Display, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                }

                workout.sets.forEachIndexed { index, set ->
                    HeavySetRow(
                        setNumber = index + 1,
                        weight = set.weight,
                        reps = set.reps,
                        isLast = index == workout.sets.lastIndex
                    )
                }

                // Edit button
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                if (today == state.selectedDate) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Button(
                            onClick = onAddSetClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HeavyLiftsColors.AccentSoft,
                                contentColor = HeavyLiftsColors.Accent
                            ),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Edit Workout", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Button(
                            onClick = onAddSetClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HeavyLiftsColors.BgChip,
                                contentColor = HeavyLiftsColors.Fg3
                            ),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text("Read only", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ─── Set Row ──────────────────────────────────────────────────────────────────

@Composable
private fun HeavySetRow(
    setNumber: Int,
    weight: Float,
    reps: Int,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(HeavyLiftsColors.AccentSoft, RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = setNumber.toString(),
                color = HeavyLiftsColors.Accent,
                fontFamily = HeavyLiftsType.Display,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "${weight}kg",
            color = HeavyLiftsColors.Fg1,
            fontFamily = HeavyLiftsType.Display,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "${reps} reps",
            color = HeavyLiftsColors.Fg2,
            fontFamily = HeavyLiftsType.Display,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }

    if (!isLast) {
        HorizontalDivider(
            color = HeavyLiftsColors.BorderSubtle,
            thickness = 0.5.dp,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

// ─── Empty / Loading / Error States ──────────────────────────────────────────

@Composable
private fun HeavyLoadingIndicator() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = HeavyLiftsColors.Accent,
                strokeWidth = 2.dp,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Loading...",
                color = HeavyLiftsColors.Fg3,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun HeavyErrorMessage(message: String) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(HeavyLiftsColors.Danger, CircleShape)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Something went wrong",
                color = HeavyLiftsColors.Danger,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                color = HeavyLiftsColors.Fg3,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun HeavyEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(HeavyLiftsColors.AccentSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = HeavyLiftsColors.Accent,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No sessions yet",
                color = HeavyLiftsColors.Fg1,
                fontFamily = HeavyLiftsType.Display,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Ready for today's session?",
                color = HeavyLiftsColors.Fg3,
                fontSize = 14.sp
            )
        }
    }
}
