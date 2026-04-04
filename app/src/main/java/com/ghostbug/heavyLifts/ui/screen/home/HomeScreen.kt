package com.ghostbug.heavyLifts.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import com.ghostbug.heavyLifts.data.domain.ExerciseEntity
import com.ghostbug.heavyLifts.ui.screen.signUp.SignUpEvent
import com.ghostbug.heavyLifts.ui.screen.signUp.SignUpUiEvent
import com.ghostbug.heavyLifts.ui.screen.signUp.SignUpViewModel
import kotlinx.coroutines.launch

// ─── Theme Constants ──────────────────────────────────────────────────────────
private object AppColors {
    val Background    = Color(0xFF101014)
    val Surface       = Color(0xFF1C1C22)
    val SurfaceDimmed = Color(0xFF1C1C22).copy(alpha = 0.4f)
    val Primary       = Color(0xFF8B5CF6)
    val PrimaryDim    = Color(0xFF3B1F72)
    val TextPrimary   = Color(0xFFF1F0FF)
    val TextSecondary = Color(0xFF7B7A8E)
    val Stroke        = Color(0xFF2A2A35)
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    signUpViewModel: SignUpViewModel = viewModel(),
    onNavigateToExerciseSelection: () -> Unit,
    onNavigateToLogWorkout: (ExerciseEntity) -> Unit,
    onNavigateToLogIn: () -> Unit
) {
    val state by homeViewModel.state.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        homeViewModel.onEvent(HomeEvent.RefreshWorkouts)
    }

    LaunchedEffect(Unit) {
        launch {
            signUpViewModel.uiEvent.collect { event ->
                if (event is SignUpUiEvent.NavigateToSignIn) {
                    onNavigateToLogIn()
                }
            }
        }

        launch {
            homeViewModel.uiEvent.collect { event ->
                when (event) {
                    is HomeUiEvent.NavigateToExerciseSelection ->
                        onNavigateToExerciseSelection()

                    is HomeUiEvent.NavigateToLogWorkout ->
                        onNavigateToLogWorkout(event.exercise)
                }
            }
        }
    }

    if (showLogoutDialog) {
        LogoutDialog(
            onDismissRequest = { showLogoutDialog = !showLogoutDialog },
            onLogoutClick = {
                showLogoutDialog = !showLogoutDialog
                signUpViewModel.onEvent(SignUpEvent.OnNavigateToSignIn)
            }
        )
    }

    Scaffold(
        containerColor = AppColors.Background,
        floatingActionButton = {
            AddWorkoutFab(onClick = { homeViewModel.onEvent(HomeEvent.OnAddWorkoutClick) })
        }
    ) { paddingValues ->
        HomeContent(
            state = state,
            paddingValues = paddingValues,
            onDateSelected = { date -> homeViewModel.onEvent(HomeEvent.OnDateSelected(date)) },
            onEvent = { homeViewModel.onEvent(it) },
            onLogoutClick = { showLogoutDialog = !showLogoutDialog }
        )
    }
}

@Composable
private fun HomeContent(
    state: HomeState,
    paddingValues: PaddingValues,
    onDateSelected: (LocalDate) -> Unit,
    onEvent: (HomeEvent) -> Unit,
    onLogoutClick: () -> Unit
) {
    val totalWeeks = 500
    val pagerState = rememberPagerState(
        pageCount = { totalWeeks },
        initialPage = totalWeeks - 1
    )

    val currentMonthName = remember(pagerState.currentPage) {
        val weeksAgo = (totalWeeks - 1) - pagerState.currentPage
        val dateInDisplayedWeek = LocalDate.now().minusWeeks(weeksAgo.toLong())
        dateInDisplayedWeek.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(AppColors.Background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MonthHeader(
            monthName = currentMonthName,
            onLogoutClick = onLogoutClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        WeeklyCalendar(
            pagerState = pagerState,
            selectedDate = state.selectedDate,
            onDateSelected = onDateSelected
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            color = AppColors.Stroke,
            thickness = 0.5.dp
        )

        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading -> LoadingIndicator()
                state.errorMessage != null -> ErrorMessage(message = state.errorMessage)
                state.workouts.isEmpty() -> EmptyWorkoutsMessage()
                else -> WorkoutList(
                    workouts = state.workouts,
                    onEvent = onEvent
                )
            }
        }
    }
}

@Composable
private fun MonthHeader(
    monthName: String,
    onLogoutClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 20.dp, end = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = monthName,
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onLogoutClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Logout",
                tint = AppColors.TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun LogoutDialog(
    onDismissRequest: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Do you want to logout from this account?",
                    modifier = Modifier.padding(16.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss")
                    }
                    TextButton(
                        onClick = onLogoutClick,
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

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
        val mondayOfWeek = LocalDate.now()
            .minusWeeks(weeksAgo.toLong())
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 0..6) {
            val date = startDate.plusDays(i.toLong())
            DateItem(
                date = date,
                isToday = date == LocalDate.now(),
                isSelected = date == selectedDate,
                isFuture = date.isAfter(LocalDate.now()),
                onDateSelected = onDateSelected
            )
        }
    }
}

@Composable
private fun DateItem(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    isFuture: Boolean,
    onDateSelected: (LocalDate) -> Unit
) {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
    val dayNumber = date.dayOfMonth.toString()

    val cardColor by animateColorAsState(
        targetValue = when {
            isSelected -> AppColors.Primary
            isToday    -> AppColors.PrimaryDim
            isFuture   -> AppColors.SurfaceDimmed
            else       -> AppColors.Surface
        },
        label = "dateCardColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .alpha(if (isFuture) 0.28f else 1f)
            .clickable(enabled = !isFuture) { onDateSelected(date) }
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = dayName.uppercase(),
            color = if (isSelected) AppColors.Primary else AppColors.TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Card(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 10.dp else 2.dp
            )
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = dayNumber,
                    color = AppColors.TextPrimary,
                    fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Medium,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun AddWorkoutFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = AppColors.Primary,
        contentColor = Color.White,
        shape = RoundedCornerShape(18.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Workout",
            tint = Color.White
        )
    }
}

@Composable
fun WorkoutList(
    workouts: List<GroupedWorkout>,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(workouts.size) {
        if (workouts.isNotEmpty()) {
            listState.animateScrollToItem(workouts.lastIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(
            items = workouts,
            key = { it.exercise.id}
        ) { workout ->
            WorkoutCard(
                workout = workout,
                AddSetClick = { onEvent(HomeEvent.EditWorkout(workout.exercise)) }
            )
        }
    }
}

@Composable
private fun WorkoutCard(
    workout: GroupedWorkout,
    AddSetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "chevron_rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        border = BorderStroke(1.dp, AppColors.Stroke.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workout.exercise.exerciseName,
                        color = AppColors.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = workout.exercise.category,
                        color = AppColors.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                workout.changePercent?.let { percent ->
                    val isPositive = percent >= 0
                    Surface(
                        color = if (isPositive) Color(0xFF102A1E) else Color(0xFF2A1010),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Text(
                            text = (if (isPositive) "+" else "") + "%.1f%%".format(percent),
                            color = if (isPositive) Color(0xFF4ADE80) else Color(0xFFF87171),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = AppColors.TextSecondary,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .rotate(rotation)
                )
            }

            AnimatedVisibility(visible = !expanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "${workout.sets.size} sets total",
                        color = AppColors.TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(20.dp))

                    workout.sets.forEachIndexed { index, set ->
                        SetRow(
                            setNumber = index + 1,
                            weight = set.weight,
                            reps = set.reps
                        )
                        if (index < workout.sets.lastIndex) {
                            HorizontalDivider(
                                color = AppColors.Stroke.copy(alpha = 0.3f),
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = AddSetClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryDim),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Workout", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SetRow(
    setNumber: Int,
    weight: Float,
    reps: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = AppColors.Stroke,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = setNumber.toString(),
                        color = AppColors.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${weight} kg",
                color = AppColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = "$reps reps",
            color = AppColors.TextSecondary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LoadingIndicator() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppColors.Primary)
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(message, color = Color.Red, textAlign = TextAlign.Center)
    }
}

@Composable
private fun EmptyWorkoutsMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = AppColors.Surface,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = null,
                    tint = AppColors.TextSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "No workouts recorded",
            color = AppColors.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Select a date or tap + to start",
            color = AppColors.TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
