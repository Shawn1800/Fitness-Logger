package com.ghostbug.heavyliftsapp.screens.step_tracker

import com.ghostbug.heavyliftsapp.data.domain.ActivityGoals
import com.ghostbug.heavyliftsapp.data.domain.DailyActivity

data class StepTrackerState(
    // today
    val todaySteps: Int = 0,
    val todayCalories: Float = 0f,
    val todayDistanceKm: Float = 0f,
    val stepSource: String = "sensor",
    val calorieSource: String = "estimated",

    // goal
    val stepGoal: Int = 10000,
    val calorieGoal: Float = 500f,

    // progress 0.0 to 1.0
    val stepProgress: Float = 0f,
    val calorieProgress: Float = 0f,

    // history for chart
    val history: List<DailyActivity> = emptyList(),

    // goal editing
    val editingStepGoal: String = "",
    val editingCalorieGoal: String = "",
    val isEditingGoal: Boolean = false,

    // ui
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val goalSaved: Boolean = false
)