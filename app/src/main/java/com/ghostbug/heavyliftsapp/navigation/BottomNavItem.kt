package com.ghostbug.heavyliftsapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(val icon: ImageVector)

val TOP_LEVEL_DESTINATIONS = mapOf(
    Route.HomeScreen    to BottomNavItem(icon = Icons.Filled.FitnessCenter),
    Route.LeaderBoards  to BottomNavItem(icon = Icons.Filled.EmojiEvents),
    Route.ProfileScreen to BottomNavItem(icon = Icons.Filled.Person)
)
