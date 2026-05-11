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
import com.ghostbug.heavyliftsapp.ui.theme.HeavyLiftsColors
import com.ghostbug.heavyliftsapp.ui.theme.HeavyLiftsType

fun Modifier.dotMatrix(
    dotColor: Color = HeavyLiftsColors.BgChip.copy(alpha = 0.14f),
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
                            i == current -> HeavyLiftsColors.Accent
                            i < current  -> HeavyLiftsColors.Fg3
                            else         -> HeavyLiftsColors.BgChip
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
                color = HeavyLiftsColors.BgChip,
                fontFamily = HeavyLiftsType.Body,
                fontSize = 13.sp
            )
        },
        isError = error != null,
        supportingText = error?.let {
            {
                Text(
                    it,
                    color = HeavyLiftsColors.Accent,
                    fontSize = 11.sp,
                    fontFamily = HeavyLiftsType.Body
                )
            }
        },
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = modifier,
        textStyle = TextStyle(
            fontFamily = HeavyLiftsType.Body,
            fontSize = 13.sp
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = HeavyLiftsColors.Fg1,
            unfocusedTextColor = HeavyLiftsColors.Fg2,
            focusedBorderColor = HeavyLiftsColors.Fg1,
            unfocusedBorderColor = HeavyLiftsColors.BorderSubtle,
            cursorColor = HeavyLiftsColors.Fg1,
            errorBorderColor = HeavyLiftsColors.Accent,
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
            .background(HeavyLiftsColors.BgChip, RoundedCornerShape(12.dp))
            .border(1.dp, HeavyLiftsColors.BorderSubtle, RoundedCornerShape(12.dp))
            .padding(top = 8.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selected
            Box(
                modifier = Modifier
                    .clickable { onSelect(index) }
                    .background(
                        if (isSelected) HeavyLiftsColors.Fg1 else Color.Transparent
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) HeavyLiftsColors.Bg else HeavyLiftsColors.Fg3,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = HeavyLiftsType.Body
                )
            }
        }
    }
}

@Composable
fun OnboardingSectionLabel(text: String) {
    Text(
        text = text,
        color = HeavyLiftsColors.Fg3,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 3.sp,
        fontFamily = HeavyLiftsType.Body
    )
    Spacer(Modifier.height(8.dp))
}
