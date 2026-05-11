package com.ghostbug.heavyliftsapp.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.ghostbug.heavyliftsapp.ui.theme.HeavyLiftsColors
import com.ghostbug.heavyliftsapp.ui.theme.HeavyLiftsType

@Composable
fun BottomNavigationBar(
    selectedKey: NavKey,
    onSelectKey: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = Color(0x4D000000),
                        topLeft = Offset(0f, 8.dp.toPx()),
                        cornerRadius = CornerRadius(28.dp.toPx())
                    )
                }
                .background(HeavyLiftsColors.BgOverlay, RoundedCornerShape(28.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TOP_LEVEL_DESTINATIONS.forEach { (route, data) ->
                val isActive = route == selectedKey
                NavTabItem(
                    label = labelFor(route),
                    icon = data.icon,
                    isActive = isActive,
                    onClick = { onSelectKey(route) }
                )
            }
        }
    }
}

@Composable
private fun NavTabItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val accent = HeavyLiftsColors.Accent
    val muted = HeavyLiftsColors.Fg4
    val tint = if (isActive) accent else muted
    Column(
        modifier = Modifier
            .clickable(onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            color = tint,
            fontFamily = HeavyLiftsType.Display,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

private fun labelFor(route: NavKey): String = when (route) {
    is Route.HomeScreen    -> "Home"
    is Route.LeaderBoards  -> "Leaderboard"
    is Route.ProfileScreen -> "Profile"
    else                   -> ""
}
