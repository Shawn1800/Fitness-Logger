package com.ghostbug.heavyliftsapp.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey

@Composable
fun BottomNavigationBar(
    selectedKey: NavKey,
    onSelectKey: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.height(70.dp),
        containerColor = Color.Black,
        contentColor = Color.White,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0)
    ) {
        TOP_LEVEL_DESTINATIONS.forEach { (topLevelDestination, data) ->
            NavigationBarItem(
                selected = topLevelDestination == selectedKey,
                onClick = {
                    onSelectKey(topLevelDestination)
                },
                icon = {
                    Icon(
                        imageVector = data.icon,
                        contentDescription = "icon",
                    )
                },
                // This is where the Nothing OS magic happens
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.DarkGray, // High contrast drop-off for unselected
                    indicatorColor = Color.Transparent, // Removes the Material 3 pill shape

                    // Optional: If you use labels, style them strictly monochrome too
                    selectedTextColor = Color.White,
                    unselectedTextColor = Color.DarkGray
                ),
                alwaysShowLabel = false // Nothing OS heavily favors clean, icon-only navigation
            )
        }
    }
}