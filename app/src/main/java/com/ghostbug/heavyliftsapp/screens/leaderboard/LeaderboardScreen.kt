package com.ghostbug.heavyliftsapp.screens.leaderboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghostbug.heavyliftsapp.ui.theme.HeavyLiftsColors
import com.ghostbug.heavyliftsapp.ui.theme.HeavyLiftsType

private val exercises = listOf("Bench", "Squat", "Deadlift", "OHP")

data class LeaderEntry(
    val rank: Int,
    val name: String,
    val value: Int,
    val unit: String,
    val delta: String,
    val isMe: Boolean,
)

private val strengthEntries = listOf(
    LeaderEntry(1, "Mia K.", 205, "lb", "+18%", false),
    LeaderEntry(2, "You", 185, "lb", "+12%", true),
    LeaderEntry(3, "Jordan", 180, "lb", "+6%", false),
    LeaderEntry(4, "Sam", 155, "lb", "+4%", false),
    LeaderEntry(5, "Priya", 140, "lb", "+9%", false),
)

private val stepEntries = listOf(
    LeaderEntry(1, "Priya", 72840, "steps", "", false),
    LeaderEntry(2, "Mia K.", 68210, "steps", "", false),
    LeaderEntry(3, "You", 58490, "steps", "", true),
    LeaderEntry(4, "Jordan", 54100, "steps", "", false),
    LeaderEntry(5, "Sam", 41320, "steps", "", false),
)

@Composable
fun LeaderboardScreen(){
    Box(
        modifier = Modifier.fillMaxSize()
        , contentAlignment = Alignment.Center
    ){
    Text(
        text = "COMING SOON...",
        color = Color.White,
        fontSize = 24.sp,
        fontFamily = HeavyLiftsType.Display,
        fontWeight = FontWeight.Bold,
    )
}}

//@Composable
//fun LeaderboardScreen() {
//    var mode by remember { mutableStateOf(0) }  // 0 = 1RM, 1 = Steps
//    var selectedExercise by remember { mutableStateOf("Bench") }
//    var weekRange by remember { mutableStateOf(0) }  // 0 = week, 1 = month
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(HeavyLiftsColors.Bg)
//            .verticalScroll(rememberScrollState())
//            .padding(bottom = 90.dp)
//    ) {
//        // ── Header ─────────────────────────────────────────────
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 20.dp, vertical = 20.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = "Friends",
//                color = HeavyLiftsColors.Fg1,
//                fontFamily = HeavyLiftsType.Display,
//                fontSize = 32.sp,
//                fontWeight = FontWeight.Bold,
//            )
//            Box(
//                modifier = Modifier
//                    .background(HeavyLiftsColors.BgChip, RoundedCornerShape(999.dp))
//                    .clickable { }
//                    .padding(horizontal = 14.dp, vertical = 10.dp)
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
//                    Icon(Icons.Default.Share, null, tint = HeavyLiftsColors.Fg1, modifier = Modifier.size(14.dp))
//                    Text("Invite", color = HeavyLiftsColors.Fg1, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
//                }
//            }
//        }
//
//        // ── Mode toggle ─────────────────────────────────────────
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 20.dp)
//                .padding(bottom = 16.dp),
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            ModeChip("1RM Strength", mode == 0) { mode = 0 }
//            ModeChip("Steps", mode == 1) { mode = 1 }
//        }
//
//        if (mode == 0) {
//            // Exercise chip row
//            Row(
//                modifier = Modifier
//                    .horizontalScroll(rememberScrollState())
//                    .padding(horizontal = 20.dp)
//                    .padding(bottom = 20.dp),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                exercises.forEach { ex ->
//                    ExerciseChip(ex, selectedExercise == ex) { selectedExercise = ex }
//                }
//            }
//
//            // Podium
//            Podium(strengthEntries.take(3))
//
//            Spacer(Modifier.height(20.dp))
//
//            // Full ranked list
//            LeaderCard(strengthEntries, showBar = false)
//        } else {
//            // Week/month range chips
//            Row(
//                modifier = Modifier
//                    .padding(horizontal = 20.dp)
//                    .padding(bottom = 20.dp),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                ExerciseChip("This week", weekRange == 0) { weekRange = 0 }
//                ExerciseChip("This month", weekRange == 1) { weekRange = 1 }
//            }
//
//            // Summary banner
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 20.dp)
//                    .padding(bottom = 16.dp)
//                    .background(HeavyLiftsColors.Info, RoundedCornerShape(20.dp))
//                    .padding(18.dp)
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Column {
//                        Text(
//                            "Your steps this week",
//                            color = HeavyLiftsColors.Cream50.copy(alpha = 0.85f),
//                            fontSize = 11.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            letterSpacing = 0.08.sp
//                        )
//                        Text(
//                            "58,490",
//                            color = HeavyLiftsColors.Fg1,
//                            fontFamily = HeavyLiftsType.Display,
//                            fontSize = 28.sp,
//                            fontWeight = FontWeight.Bold,
//                            modifier = Modifier.padding(top = 4.dp)
//                        )
//                    }
//                    Text(
//                        "You're #3 — 9,700 behind Mia.",
//                        color = HeavyLiftsColors.Fg1.copy(alpha = 0.9f),
//                        fontSize = 13.sp,
//                        textAlign = TextAlign.End,
//                        modifier = Modifier.widthIn(max = 140.dp)
//                    )
//                }
//            }
//
//            // Step bars list
//            LeaderCard(stepEntries, showBar = true)
//        }
//    }
//}
//
//// ── Mode toggle chip ──────────────────────────────────────────────────────────
//
//@Composable
//private fun RowScope.ModeChip(label: String, active: Boolean, onClick: () -> Unit) {
//    Box(
//        modifier = Modifier
//            .weight(1f)
//            .background(
//                if (active) HeavyLiftsColors.Accent else Color.Transparent,
//                RoundedCornerShape(999.dp)
//            )
//            .then(
//                if (!active) Modifier.background(
//                    Color.Transparent,
//                    RoundedCornerShape(999.dp)
//                ) else Modifier
//            )
//            .clickable { onClick() }
//            .then(
//                if (!active) Modifier.padding(1.dp) else Modifier
//            ),
//        contentAlignment = Alignment.Center
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .then(
//                    if (!active) Modifier
//                        .background(Color.Transparent, RoundedCornerShape(999.dp))
//                    else Modifier
//                )
//                .padding(vertical = 10.dp, horizontal = 14.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                label,
//                color = if (active) HeavyLiftsColors.Fg1 else HeavyLiftsColors.Fg3,
//                fontSize = 14.sp,
//                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
//            )
//        }
//    }
//}
//
//// ── Exercise chip ─────────────────────────────────────────────────────────────
//
//@Composable
//private fun ExerciseChip(label: String, active: Boolean, onClick: () -> Unit) {
//    Box(
//        modifier = Modifier
//            .background(
//                if (active) HeavyLiftsColors.Fg1 else HeavyLiftsColors.BgChip,
//                RoundedCornerShape(999.dp)
//            )
//            .clickable { onClick() }
//            .padding(horizontal = 14.dp, vertical = 8.dp)
//    ) {
//        Text(
//            label,
//            color = if (active) HeavyLiftsColors.Olive900 else HeavyLiftsColors.Fg3,
//            fontSize = 13.sp,
//            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
//        )
//    }
//}
//
//// ── Podium ────────────────────────────────────────────────────────────────────
//
//@Composable
//private fun Podium(top3: List<LeaderEntry>) {
//    val ordered = listOf(
//        top3.getOrNull(1),
//        top3.getOrNull(0),
//        top3.getOrNull(2)
//    )
//    val heights = listOf(80.dp, 110.dp, 60.dp)
//    val colors  = listOf(HeavyLiftsColors.Info, HeavyLiftsColors.Accent, HeavyLiftsColors.Success)
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 20.dp),
//        horizontalArrangement = Arrangement.spacedBy(12.dp),
//        verticalAlignment = Alignment.Bottom
//    ) {
//        ordered.forEachIndexed { idx, entry ->
//            entry ?: return@forEachIndexed
//            Column(
//                modifier = Modifier.weight(1f),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(52.dp)
//                        .background(colors[idx], CircleShape),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        entry.name.first().toString(),
//                        color = HeavyLiftsColors.Fg1,
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//                Spacer(Modifier.height(6.dp))
//                Text(entry.name, color = HeavyLiftsColors.Fg1, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
//                Text("${entry.value} ${entry.unit}", color = HeavyLiftsColors.Fg3, fontSize = 12.sp)
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(heights[idx])
//                        .padding(top = 10.dp)
//                        .background(colors[idx], RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
//                    contentAlignment = Alignment.TopCenter
//                ) {
//                    Text(
//                        entry.rank.toString(),
//                        color = HeavyLiftsColors.Fg1,
//                        fontSize = 22.sp,
//                        fontWeight = FontWeight.Bold,
//                        modifier = Modifier.padding(top = 10.dp)
//                    )
//                }
//            }
//        }
//    }
//}
//
//// ── Ranked list card ──────────────────────────────────────────────────────────
//
//@Composable
//private fun LeaderCard(entries: List<LeaderEntry>, showBar: Boolean) {
//    val maxVal = entries.maxOfOrNull { it.value } ?: 1
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 20.dp)
//            .background(HeavyLiftsColors.BgElevated, RoundedCornerShape(20.dp))
//            .padding(horizontal = 16.dp)
//    ) {
//        entries.forEachIndexed { i, e ->
//            Column {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 12.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    Text(
//                        "#${e.rank}",
//                        color = if (e.isMe) HeavyLiftsColors.Accent else HeavyLiftsColors.Fg4,
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.SemiBold,
//                        modifier = Modifier.width(26.dp)
//                    )
//                    Box(
//                        modifier = Modifier
//                            .size(if (showBar) 30.dp else 34.dp)
//                            .background(
//                                if (e.isMe) HeavyLiftsColors.Accent else HeavyLiftsColors.BgChip,
//                                CircleShape
//                            ),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            e.name.first().toString(),
//                            color = HeavyLiftsColors.Fg1,
//                            fontSize = 13.sp,
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//                    Text(
//                        e.name,
//                        color = if (e.isMe) HeavyLiftsColors.Fg1 else HeavyLiftsColors.Fg2,
//                        fontSize = if (showBar) 14.sp else 15.sp,
//                        fontWeight = if (e.isMe) FontWeight.Bold else FontWeight.Normal,
//                        modifier = Modifier.weight(1f)
//                    )
//                    Text(
//                        if (showBar) e.value.toString().let { v ->
//                            if (e.value >= 1000) "${e.value / 1000},${"%03d".format(e.value % 1000)}" else v
//                        } else "${e.value} ${e.unit}",
//                        color = HeavyLiftsColors.Fg1,
//                        fontSize = if (showBar) 14.sp else 16.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                    if (!showBar && e.delta.isNotEmpty()) {
//                        Text(
//                            e.delta,
//                            color = HeavyLiftsColors.Accent,
//                            fontSize = 12.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            modifier = Modifier.widthIn(min = 36.dp)
//                        )
//                    }
//                }
//                if (showBar) {
//                    val animWidth by animateFloatAsState(
//                        targetValue = e.value.toFloat() / maxVal,
//                        animationSpec = tween(600),
//                        label = "bar"
//                    )
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(start = 68.dp)
//                            .padding(bottom = 10.dp)
//                            .height(6.dp)
//                            .background(HeavyLiftsColors.BorderSubtle, RoundedCornerShape(999.dp))
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxHeight()
//                                .fillMaxWidth(animWidth)
//                                .background(
//                                    if (e.isMe) HeavyLiftsColors.Accent else HeavyLiftsColors.Info,
//                                    RoundedCornerShape(999.dp)
//                                )
//                        )
//                    }
//                }
//                if (i < entries.lastIndex) {
//                    HorizontalDivider(
//                        color = HeavyLiftsColors.BorderSubtle,
//                        thickness = 0.5.dp
//                    )
//                }
//            }
//        }
//    }
//}
