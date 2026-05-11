package com.ghostbug.heavyliftsapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val HeavyLiftsColorScheme = darkColorScheme(
    primary           = HeavyLiftsColors.Accent,
    onPrimary         = HeavyLiftsColors.Fg1,
    primaryContainer  = HeavyLiftsColors.AccentSoft,
    onPrimaryContainer = HeavyLiftsColors.Accent,
    secondary         = HeavyLiftsColors.BgChip,
    onSecondary       = HeavyLiftsColors.Fg1,
    tertiary          = HeavyLiftsColors.Info,
    onTertiary        = HeavyLiftsColors.Fg1,
    background        = HeavyLiftsColors.Bg,
    onBackground      = HeavyLiftsColors.Fg1,
    surface           = HeavyLiftsColors.BgElevated,
    onSurface         = HeavyLiftsColors.Fg1,
    surfaceVariant    = HeavyLiftsColors.BgChip,
    onSurfaceVariant  = HeavyLiftsColors.Fg2,
    error             = HeavyLiftsColors.Danger,
    onError           = HeavyLiftsColors.Fg1,
    outline           = HeavyLiftsColors.BorderDefault,
    outlineVariant    = HeavyLiftsColors.BorderSubtle
)

private val HeavyLiftsTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = HeavyLiftsType.Display,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp,
        color = HeavyLiftsColors.Fg1
    ),
    headlineLarge = TextStyle(
        fontFamily = HeavyLiftsType.Display,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp,
        color = HeavyLiftsColors.Fg1
    ),
    headlineMedium = TextStyle(
        fontFamily = HeavyLiftsType.Display,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        color = HeavyLiftsColors.Fg1
    ),
    headlineSmall = TextStyle(
        fontFamily = HeavyLiftsType.Display,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = HeavyLiftsColors.Fg1
    ),
    titleLarge = TextStyle(
        fontFamily = HeavyLiftsType.Display,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = HeavyLiftsColors.Fg1
    ),
    titleMedium = TextStyle(
        fontFamily = HeavyLiftsType.Display,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = HeavyLiftsColors.Fg1
    ),
    bodyLarge = TextStyle(
        fontFamily = HeavyLiftsType.Body,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 23.sp,
        color = HeavyLiftsColors.Fg2
    ),
    bodyMedium = TextStyle(
        fontFamily = HeavyLiftsType.Body,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = HeavyLiftsColors.Fg2
    ),
    bodySmall = TextStyle(
        fontFamily = HeavyLiftsType.Body,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        color = HeavyLiftsColors.Fg3
    ),
    labelLarge = TextStyle(
        fontFamily = HeavyLiftsType.Display,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = HeavyLiftsColors.Fg1
    ),
    labelMedium = TextStyle(
        fontFamily = HeavyLiftsType.Body,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = HeavyLiftsColors.Fg2
    ),
    labelSmall = TextStyle(
        fontFamily = HeavyLiftsType.Display,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.8.sp,
        color = HeavyLiftsColors.Fg3
    )
)

@Composable
fun Demo103Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HeavyLiftsColorScheme,
        typography = HeavyLiftsTypography,
        content = content
    )
}
