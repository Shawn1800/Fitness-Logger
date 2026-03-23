package com.example.demo103.ui.theme.home

import android.R.attr.onClick
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.demo103.data.entity.ExerciseEntity
import com.example.demo103.data.entity.WorkoutEntryEntity
import com.example.demo103.data.entity.WorkoutWithExercise
import com.example.demo103.ui.theme.exercise_selection.ExerciseEvent
import com.example.demo103.ui.theme.exercise_selection.ExerciseViewModel
import com.example.demo103.ui.theme.home.WorkoutCard
import com.example.demo103.ui.theme.log_workout.LogWorkoutEvent
import com.example.demo103.ui.theme.log_workout.LogWorkoutViewModel

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
    onNavigateToExerciseSelection: () -> Unit,
    onNavigateToLogWorkout: (ExerciseEntity) -> Unit,
) {
    val state by homeViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        homeViewModel.uiEvent.collect { event ->
            when (event) {
                is HomeUiEvent.NavigateToExerciseSelection -> onNavigateToExerciseSelection()
                is HomeUiEvent.NavigateToLogWorkout -> onNavigateToLogWorkout(event.exercise)
            }
        }
    }

    Scaffold(
        containerColor = AppColors.Background,
        floatingActionButton = {
            AddWorkoutFab(onClick = { homeViewModel.onEvent(HomeEvent.OnAddWorkoutClick) })
        }
    ) { paddingValues ->
        HomeContent(
            state = state,
            homeViewModel = homeViewModel,
            paddingValues = paddingValues,
            onDateSelected = { date -> homeViewModel.onEvent(HomeEvent.OnDateSelected(date)) }
        )
    }
}

// ─── Content ──────────────────────────────────────────────────────────────────
@Composable
private fun HomeContent(
    state: HomeState,
    homeViewModel: HomeViewModel,
    paddingValues: PaddingValues,
    onDateSelected: (LocalDate) -> Unit
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
            .background(AppColors.Background)
            .padding(paddingValues)
    ) {
        MonthHeader(monthName = currentMonthName)

        Spacer(modifier = Modifier.height(20.dp))

        WeeklyCalendar(
            pagerState = pagerState,
            totalWeeks = totalWeeks,
            selectedDate = state.selectedDate,
            onDateSelected = onDateSelected
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Thin divider to separate calendar from list
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            color = AppColors.Stroke,
            thickness = 0.5.dp
        )

        when {
            state.isLoading -> LoadingIndicator()
            state.errorMessage != null -> ErrorMessage(message = state.errorMessage)
            state.workouts.isEmpty() -> EmptyWorkoutsMessage()
            else -> WorkoutList(
                workouts = state.workouts,
                onEvent = { event -> homeViewModel.onEvent(event) }
            )
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────
@Composable
private fun MonthHeader(monthName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, start = 20.dp, end = 20.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Text(
            text = monthName,
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp,
            letterSpacing = (-0.5).sp
        )
    }
}

// ─── Calendar ─────────────────────────────────────────────────────────────────
@Composable
private fun WeeklyCalendar(
    pagerState: PagerState,
    totalWeeks: Int,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
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

    // Animate the card color so selection feels snappy
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

// ─── FAB ──────────────────────────────────────────────────────────────────────
@Composable
private fun AddWorkoutFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = AppColors.Primary,
        contentColor = Color.White,
        shape = RoundedCornerShape(18.dp)  // squircle shape
    ) {
        Text(
            text = "+",
            fontSize = 28.sp,
            color = Color.White,
            fontWeight = FontWeight.Light,
            lineHeight = 28.sp
        )
    }
}

// ─── Workout List ─────────────────────────────────────────────────────────────
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
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(
            items = workouts,
            key = { it.exercise.exerciseId }
        ) { workout ->
            WorkoutCard(
                workout = workout,
                AddSetClick = { onEvent(HomeEvent.EditWorkout(workout.exercise)) }
            )
        }
    }
}

// ─── Workout Card ─────────────────────────────────────────────────────────────
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
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        border = BorderStroke(0.5.dp, AppColors.Stroke)  // subtle border lifts card off BG
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Left: color accent bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AppColors.Primary)
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Middle: exercise name + sets
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.exercise.exerciseName,
                    color = AppColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = AddSetClick,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary.copy(alpha = 0.18f),
                        contentColor = AppColors.Primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        text = "Add Set",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Always show first set as preview
                Text(
                    text = "Set 1 · ${workout.sets.first().weight} kg × ${workout.sets.first().reps} reps",
                    color = AppColors.TextSecondary,
                    fontSize = 13.sp
                )

                // Show remaining sets only when expanded
                AnimatedVisibility(visible = expanded) {
                    Column {
                        workout.sets.drop(1).forEachIndexed { index, set ->
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Set ${index + 2} · ${set.weight} kg × ${set.reps} reps",
                                color = AppColors.TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: set count badge + chevron stacked
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.Primary.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${workout.sets.size} sets",
                        color = AppColors.Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
//
                workout.changePercent?.let { percent ->
                    val formatted = String.format("%.1f", percent)
                    val isPositive = percent > 0
                    val isNeutral = percent == 0.0

                    val percentColor = when {
                        isPositive -> Color(0xFF22C55E)   // green
                        isNeutral -> Color(0xFF94A3B8)    // gray
                        else -> Color(0xFFEF4444)         // red
                    }

                    val arrowIcon = when {
                        isPositive -> Icons.Default.KeyboardArrowUp
                        isNeutral -> Icons.Default.Remove
                        else -> Icons.Default.KeyboardArrowDown
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(percentColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = arrowIcon,
                            contentDescription = null,
                            tint = percentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${if (isPositive) "+" else ""}$formatted%",
                            color = percentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = AppColors.TextSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotation)
                )
            }
        }
    }
}

// ─── Empty / Loading / Error States ──────────────────────────────────────────
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
private fun EmptyWorkoutsMessage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "💪", fontSize = 44.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No workouts logged",
            color = AppColors.TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tap + to start today's session",
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
            color = Color(0xFFCF6679),   // softer red — less jarring on dark BG
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
