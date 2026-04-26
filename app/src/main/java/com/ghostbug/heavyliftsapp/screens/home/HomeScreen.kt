package com.ghostbug.heavyliftsapp.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ghostbug.heavyliftsapp.screens.signUp.SignUpEvent
import com.ghostbug.heavyliftsapp.screens.signUp.SignUpUiEvent
import com.ghostbug.heavyliftsapp.screens.signUp.SignUpViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

// ─── Nothing OS Design System ────────────────────────────────────────────────
// Inspired by Nothing Phone's dot-matrix glyph interface,
// raw industrial monochrome palette, and stark typographic contrasts.
// ─────────────────────────────────────────────────────────────────────────────

private object NothingColors {
    // Core blacks — layered depth
    val Void         = Color(0xFF0A0A0A) // deepest background
    val Surface0     = Color(0xFF111111) // card base
    val Surface1     = Color(0xFF1A1A1A) // elevated card
    val Surface2     = Color(0xFF222222) // pressed / dimmed

    // Strokes & dividers
    val Hairline     = Color(0xFF2C2C2C)
    val StrokeWeak   = Color(0xFF1E1E1E)

    // Nothing's signature white
    val NothingWhite = Color(0xFFFFFFFF)
    val OffWhite     = Color(0xFFE8E8E8)
    val DimWhite     = Color(0xFF8A8A8A)
    val FaintWhite   = Color(0xFF3A3A3A)

    // Glyph accent — Nothing's signature red-dot
    val GlyphRed     = Color(0xFFFF3A3A)
    val GlyphRedDim  = Color(0xFF3A1010)

    // Semantic
    val Positive     = Color(0xFFE8E8E8) // near-white for gains
    val Negative     = Color(0xFF666666) // dim for losses

}




// ─── Screen ──────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    signUpViewModel: SignUpViewModel = viewModel(),
    onNavigateToExerciseSelection: () -> Unit,
    onNavigateToLogWorkout: (Long) -> Unit,
    onNavigateToLogIn: () -> Unit,
    onNavigateToStepTracker: () ->Unit
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
        NothingLogoutDialog(
            onDismissRequest = { showLogoutDialog = false },
            onLogoutClick = {
                showLogoutDialog = false
                signUpViewModel.onEvent(SignUpEvent.OnNavigateToSignIn)
            }
        )
    }

    Scaffold(
        containerColor = NothingColors.Void,
        snackbarHost = { SnackbarHost(snackbarHostState) },

    ) { paddingValues ->
        HomeContent(
            state = state,
            paddingValues = paddingValues,
            onDateSelected = { date -> homeViewModel.onEvent(HomeEvent.OnDateSelected(date)) },
            onEvent = { homeViewModel.onEvent(it) },
            onStepClick = {onNavigateToStepTracker()}
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

    val horizontalPagerState = rememberPagerState(pageCount = {2})
    Column(modifier = Modifier.fillMaxSize()) {


    val totalWeeks = 500
    val pagerState = rememberPagerState(
        pageCount = { totalWeeks },
        initialPage = totalWeeks - 1
    )

    val currentMonthName = remember(pagerState.currentPage) {
        val weeksAgo = (totalWeeks - 1) - pagerState.currentPage
        Clock.System.todayIn(TimeZone.currentSystemDefault()).minus(weeksAgo * 7, DateTimeUnit.DAY).month.name
    }

    val currentYear = remember(pagerState.currentPage) {
        val weeksAgo = (totalWeeks - 1) - pagerState.currentPage
        Clock.System.todayIn(TimeZone.currentSystemDefault()).minus(weeksAgo * 7, DateTimeUnit.DAY).year.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(top=2.dp)
            .background(NothingColors.Void),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Header ─────────────────────────────────────────────────────────
        NothingHeader(
            monthName = currentMonthName,
            year = currentYear
        )

        // ── Hairline divider ────────────────────────────────────────────────
        HorizontalDivider(
            color = NothingColors.Hairline,
            thickness = 0.5.dp,
            modifier = Modifier.padding(horizontal = 0.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Weekly calendar pager ───────────────────────────────────────────
        WeeklyCalendar(
            pagerState = pagerState,
            selectedDate = state.selectedDate,
            onDateSelected = onDateSelected
        )
        Spacer(modifier = Modifier.height(16.dp))


        HorizontalPager(
            state = horizontalPagerState,
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) { page ->

            when (page) {

                // ── PAGE 0: Activity ───────────────────────────────

                0 -> {
                    Column {
                        Text(
                            text = "SWIPE TO LOG EXERCISES \u2192", // Using an arrow character
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color.LightGray.copy(alpha = 0.6f),
                            letterSpacing = 2.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .align(Alignment.CenterHorizontally),
                            textAlign = TextAlign.Center
                        )
                        ActivityBanner(
                            state = state,
                            onClick = onStepClick
                        )
                    }
                }
                // ── PAGE 1: Sessions ───────────────────────────────


                1 -> {
                    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Spacer(modifier = Modifier.height(8.dp))

                            // Section label
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(NothingColors.GlyphRed, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SESSIONS",
                                    color = NothingColors.DimWhite,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 3.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(modifier = Modifier.weight(1f)) {
                                when {
                                    state.isLoading -> NothingLoadingIndicator()
                                    state.errorMessage != null -> NothingErrorMessage(
                                        message = state.errorMessage
                                    )
                                    state.workouts.isEmpty() -> NothingEmptyState()
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
                            NothingFab(
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
    }
// ─── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun NothingHeader(
    monthName: String,
    year: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp),
    ) {
        Text(
            text = year,
            color = NothingColors.DimWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = monthName,
            color = NothingColors.NothingWhite,
            fontWeight = FontWeight.Black,
            fontSize = 34.sp,
            letterSpacing = (-1).sp,
            lineHeight = 34.sp
        )
    }
}

// ─── Activity Card ────────────────────────────────────────────────────────────

@Composable
private fun ActivityBanner(
    state: HomeState,
    onClick: () -> Unit
) {
    val goalReached = state.stepProgress >= 1f
    val accentColor = if (goalReached) NothingColors.GlyphRed else NothingColors.NothingWhite
    val fill = state.stepProgress.coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = fill,
        animationSpec = tween(1000),
        label = "ringProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .background(NothingColors.Surface0, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Column {
            // Card header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(Modifier.size(4.dp).background(accentColor, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "ACTIVITY",
                    color = NothingColors.DimWhite,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(Modifier.weight(1f))
                Text("›", color = NothingColors.FaintWhite, fontSize = 14.sp)
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Ring progress
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(84.dp)) {
                    Canvas(modifier = Modifier.size(84.dp)) {
                        val strokeWidth = 6.dp.toPx()
                        val inset = strokeWidth / 2f
                        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                        val topLeft = Offset(inset, inset)
                        drawArc(
                            color = NothingColors.FaintWhite,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        if (animatedProgress > 0f) {
                            drawArc(
                                color = accentColor,
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }
                    Text(
                        text = "${(fill * 100).toInt()}%",
                        color = accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(Modifier.width(20.dp))

                Column {
                    Text(
                        text = "%,d".format(state.todaySteps),
                        color = NothingColors.NothingWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "of %,d steps".format(state.stepGoal),
                        color = NothingColors.DimWhite,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (state.todayCalories > 0f) {
                            Column {
                                Text(
                                    text = "${state.todayCalories.toInt()}",
                                    color = NothingColors.OffWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "KCAL",
                                    color = NothingColors.DimWhite,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp
                                )
                            }
                        }
                        if (state.todayDistanceKm > 0f) {
                            Column {
                                Text(
                                    text = "%.1f".format(state.todayDistanceKm),
                                    color = NothingColors.OffWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "KM",
                                    color = NothingColors.DimWhite,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ─── Logout Dialog ────────────────────────────────────────────────────────────

@Composable
fun NothingLogoutDialog(
    onDismissRequest: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NothingColors.Surface0, RoundedCornerShape(4.dp))
                .padding(1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NothingColors.Surface1, RoundedCornerShape(3.dp))
                    .padding(28.dp)
            ) {
                Column {
                    // Glyph accent bar
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(20.dp)
                                .background(NothingColors.GlyphRed)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SIGN OUT",
                            color = NothingColors.NothingWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 3.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "You're about to sign out of your account. Your data will remain intact.",
                        color = NothingColors.DimWhite,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Cancel — outlined, dark
                        OutlinedButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(3.dp),
                            border = BorderStroke(1.dp, NothingColors.Hairline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = NothingColors.DimWhite
                            ),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text(
                                "CANCEL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Confirm — white fill
                        Button(
                            onClick = onLogoutClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(3.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NothingColors.NothingWhite,
                                contentColor = NothingColors.Void
                            ),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text(
                                "CONFIRM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
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
        val dateInWeek = Clock.System.todayIn(TimeZone.currentSystemDefault()).minus(weeksAgo * 7, DateTimeUnit.DAY)
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
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 0..6) {
            val date = startDate.plus(i, DateTimeUnit.DAY)
            NothingDateItem(
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
private fun NothingDateItem(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    isFuture: Boolean,
    onDateSelected: (LocalDate) -> Unit
) {
    val dayName = date.dayOfWeek.name.take(1)
    val dayNumber = date.day.toString()

    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> NothingColors.NothingWhite
            isToday    -> NothingColors.Surface2
            else       -> Color.Transparent
        },
        animationSpec = tween(150),
        label = "dateColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .alpha(if (isFuture) 0.22f else 1f)
            .clickable(enabled = !isFuture) { onDateSelected(date) }
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = dayName,
            color = if (isSelected) NothingColors.NothingWhite else NothingColors.FaintWhite,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(bgColor, RoundedCornerShape(4.dp))
                .then(
                    if (isToday && !isSelected)
                        Modifier
                            .background(Color.Transparent)
                            .clip(RoundedCornerShape(4.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            // Today dot indicator below number
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dayNumber,
                    color = when {
                        isSelected -> NothingColors.Void
                        isToday    -> NothingColors.NothingWhite
                        else       -> NothingColors.DimWhite
                    },
                    fontWeight = if (isSelected || isToday) FontWeight.Black else FontWeight.Normal,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
                if (isToday && !isSelected) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(NothingColors.GlyphRed, CircleShape)
                    )
                }
            }
        }
    }
}

// ─── FAB ─────────────────────────────────────────────────────────────────────

@Composable
private fun NothingFab(onClick: () -> Unit,state: HomeState) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    if (today==state.selectedDate) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = NothingColors.NothingWhite,
            contentColor = NothingColors.Void,
            shape = RoundedCornerShape(6.dp),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Workout",
                tint = NothingColors.Void,
                modifier = Modifier.size(22.dp)
            )
        }
    }
        else{

        FloatingActionButton(
            onClick = onClick,
            containerColor = NothingColors.DimWhite,
            contentColor = NothingColors.DimWhite,
            shape = RoundedCornerShape(6.dp),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Read Only",
                tint = NothingColors.Void,
                modifier = Modifier.size(22.dp)
            )
        }

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
        verticalArrangement = Arrangement.spacedBy(1.dp), // Nothing OS: tight density
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
    ) {
        items(
            items = workouts,
            key = { it.exercise.id }
        ) { workout ->
            NothingWorkoutCard(
                state=state,
                workout = workout,
                onAddSetClick =
                  if (today==state.selectedDate) {
                      { onEvent(HomeEvent.EditWorkout(workout.exercise.id)) }
                  }else {
                      { onEvent(HomeEvent.Message("Previous dates are read only ")) }
                  }
            )

            // Row divider
            HorizontalDivider(
                color = NothingColors.StrokeWeak,
                thickness = 0.5.dp
            )
        }
    }
}

// ─── Workout Card ─────────────────────────────────────────────────────────────

@Composable
private fun NothingWorkoutCard(
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
            .fillMaxWidth()
            .background(if (expanded) NothingColors.Surface0 else NothingColors.Void)
            .clickable { expanded = !expanded }
    ) {
        // ── Card header ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Glyph accent line + exercise info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(
                            if (expanded) NothingColors.GlyphRed else NothingColors.FaintWhite,
                            RoundedCornerShape(1.dp)
                        )
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = workout.exercise.exerciseName.uppercase(),
                        color = NothingColors.NothingWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = workout.exercise.category.uppercase(),
                        color = NothingColors.DimWhite,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Change percent badge — Nothing style: raw text, no pill
                workout.changePercent?.let { percent ->
                    val isPositive = percent >= 0
                    Text(
                        text = (if (isPositive) "+" else "") + "%.0f%%".format(percent),
                        color = if (isPositive) NothingColors.Positive else NothingColors.Negative,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }

//                // Set count — when collapsed
//                AnimatedVisibility(
//                    visible = !expanded,
//                    enter = fadeIn(animationSpec = tween(400)) +
//                            scaleIn(initialScale = 0.92f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)),
//                    exit = fadeOut(animationSpec = tween(400)) +
//                            scaleOut(targetScale = 0.92f)
//                ) {
                    Text(
                        text = "${workout.sets.size}×",
                        color = NothingColors.FaintWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
              //  }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = NothingColors.DimWhite,
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
                    .background(NothingColors.Surface0)
            ) {
                // Sets header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NothingColors.Surface1)
                        .padding(horizontal = 36.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "SET", color = NothingColors.FaintWhite, fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace, letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "WEIGHT", color = NothingColors.FaintWhite, fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace, letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "REPS", color = NothingColors.FaintWhite, fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace, letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Set rows
                workout.sets.forEachIndexed { index, set ->
                    NothingSetRow(
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
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onAddSetClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(3.dp),
                            border = BorderStroke(1.dp, NothingColors.Hairline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = NothingColors.NothingWhite,
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                null,
                                modifier = Modifier.size(14.dp),
                                tint = NothingColors.NothingWhite
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "EDIT WORKOUT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onAddSetClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(3.dp),
                            border = BorderStroke(1.dp, NothingColors.Hairline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = NothingColors.DimWhite,
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                null,
                                modifier = Modifier.size(14.dp),
                                tint = NothingColors.DimWhite
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "READ ONLY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }


                }

            }
        }
    }
}}
// ─── Set Row ──────────────────────────────────────────────────────────────────

@Composable
private fun NothingSetRow(
    setNumber: Int,
    weight: Float,
    reps: Int,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Set number — glyph-style indicator
        Text(
            text = setNumber.toString().padStart(2, '0'),
            color = NothingColors.FaintWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        // Weight — prominent
        Text(
            text = "${weight}kg",
            color = NothingColors.NothingWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )

        // Reps — secondary
        Text(
            text = "${reps}r",
            color = NothingColors.DimWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }

    if (!isLast) {
        HorizontalDivider(
            color = NothingColors.StrokeWeak,
            thickness = 0.5.dp,
            modifier = Modifier.padding(horizontal = 36.dp)
        )
    }
}

// ─── Empty / Loading / Error States ──────────────────────────────────────────

@Composable
private fun NothingLoadingIndicator() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = NothingColors.NothingWhite,
                strokeWidth = 1.5.dp,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "LOADING",
                color = NothingColors.FaintWhite,
                fontSize = 9.sp,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun NothingErrorMessage(message: String) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(NothingColors.GlyphRed, CircleShape)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "ERROR",
                color = NothingColors.GlyphRed,
                fontSize = 10.sp,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                color = NothingColors.DimWhite,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun NothingEmptyState() {
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
            // Nothing-style glyph: three stacked dots
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
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

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                "NO SESSIONS",
                color = NothingColors.NothingWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "SELECT DATE OR TAP +",
                color = NothingColors.FaintWhite,
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}