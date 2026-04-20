package com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object OnboardingColors {
    val Void         = Color(0xFF0A0A0A)
    val Surface0     = Color(0xFF111111)
    val Surface1     = Color(0xFF1A1A1A)
    val Surface2     = Color(0xFF222222)
    val Hairline     = Color(0xFF2C2C2C)
    val NothingWhite = Color(0xFFFFFFFF)
    val OffWhite     = Color(0xFFE8E8E8)
    val DimWhite     = Color(0xFF8A8A8A)
    val FaintWhite   = Color(0xFF3A3A3A)
    val GlyphRed     = Color(0xFFFF3A3A)
}

fun Modifier.dotMatrix(
    dotColor: Color = OnboardingColors.FaintWhite.copy(alpha = 0.14f),
    spacing: Float = 16f,
    radius: Float = 1.1f
): Modifier = this.drawBehind {
    val cols = (size.width / spacing).toInt() + 1
    val rows = (size.height / spacing).toInt() + 1
    for (c in 0..cols) for (r in 0..rows)
        drawCircle(dotColor, radius, Offset(c * spacing, r * spacing))
}

@Composable
fun OnboardingStepProgress(current: Int, total: Int = 3) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .height(2.dp)
                    .width(if (i == current) 28.dp else 8.dp)
                    .background(
                        color = when {
                            i == current -> OnboardingColors.GlyphRed
                            i < current  -> OnboardingColors.DimWhite
                            else         -> OnboardingColors.FaintWhite
                        },
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

@Composable
fun OnboardingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                placeholder,
                color = OnboardingColors.FaintWhite,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
        },
        isError = error != null,
        supportingText = error?.let {
            {
                Text(
                    it,
                    color = OnboardingColors.GlyphRed,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = modifier,
        textStyle = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = OnboardingColors.NothingWhite,
            unfocusedTextColor = OnboardingColors.OffWhite,
            focusedBorderColor = OnboardingColors.NothingWhite,
            unfocusedBorderColor = OnboardingColors.Hairline,
            cursorColor = OnboardingColors.NothingWhite,
            errorBorderColor = OnboardingColors.GlyphRed,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent
        )
    )
}

@Composable
fun OnboardingUnitToggle(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .background(OnboardingColors.Surface1, RoundedCornerShape(3.dp))
            .border(1.dp, OnboardingColors.Hairline, RoundedCornerShape(3.dp))
            .padding(top = 8.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selected
            Box(
                modifier = Modifier
                    .clickable { onSelect(index) }
                    .background(
                        if (isSelected) OnboardingColors.NothingWhite else Color.Transparent
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) OnboardingColors.Void else OnboardingColors.DimWhite,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun OnboardingSectionLabel(text: String) {
    Text(
        text = text,
        color = OnboardingColors.DimWhite,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 3.sp,
        fontFamily = FontFamily.Monospace
    )
    Spacer(Modifier.height(8.dp))
}
