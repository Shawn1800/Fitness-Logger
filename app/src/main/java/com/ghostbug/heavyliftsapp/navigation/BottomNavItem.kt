package com.ghostbug.heavyliftsapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val icon : ImageVector,

)
val TOP_LEVEL_DESTINATIONS= mapOf(

    Route.HomeScreen to BottomNavItem(
        icon = Icons.Outlined.Home

    ),

    Route.LeaderBoards to BottomNavItem(
        icon = Icons.Outlined.Leaderboard,

    ),

    Route.ProfileScreen  to BottomNavItem(
        icon = Icons.Outlined.Person,

    )
)