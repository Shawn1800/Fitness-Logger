package com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile

import HeightUnit
import WeightUnit
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ghostbug.heavyliftsapp.data.domain.Gender

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserProfileScreen2(
    viewModel: UserProfileViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                UserProfileUiEvent.NavToScreen3 -> onNext()
                is UserProfileUiEvent.SendSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingColors.Void)
            .dotMatrix()
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp, bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = OnboardingColors.DimWhite
                    )
                }
                Spacer(Modifier.weight(1f))
                OnboardingStepProgress(current = 1)
            }

            Spacer(Modifier.height(44.dp))

            Text(
                text = "BODY STATS.",
                color = OnboardingColors.NothingWhite,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 38.sp,
                letterSpacing = (-0.5).sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "helps us calculate your calories accurately.",
                color = OnboardingColors.DimWhite,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(40.dp))

            // ── Age ──────────────────────────────────────────────────────────
            OnboardingSectionLabel("AGE")

            OnboardingTextField(
                value = state.age?.toString() ?: "",
                onValueChange = { viewModel.onEvent(UserProfileEvent.OnAgeChanged(it)) },
                placeholder = "e.g., 25",
                error = state.ageError,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(28.dp))

            // ── Height ───────────────────────────────────────────────────────
            OnboardingSectionLabel("HEIGHT")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                OnboardingTextField(
                    value = state.heightInputText,
                    onValueChange = { input ->
                        val digitsOnly = input.filter { it.isDigit() }
                        if (digitsOnly.length <= 3) {
                            viewModel.onEvent(UserProfileEvent.OnHeightChanged(input))
                        }
                    },
                    placeholder = if (state.heightUnit == HeightUnit.CM) "e.g., 170" else "e.g., 5'6",
                    error = state.heightError,
                    keyboardType = if (state.heightUnit == HeightUnit.FEET)
                        KeyboardType.Number else KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
                OnboardingUnitToggle(
                    options = listOf("cm", "ft"),
                    selected = if (state.heightUnit == HeightUnit.CM) 0 else 1,
                    onSelect = {
                        viewModel.onEvent(
                            UserProfileEvent.OnHeightUnitChanged(
                                if (it == 0) HeightUnit.CM else HeightUnit.FEET
                            )
                        )
                    }
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Weight ───────────────────────────────────────────────────────
            OnboardingSectionLabel("WEIGHT")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                OnboardingTextField(
                    value = state.weightInputText,
                    onValueChange = { viewModel.onEvent(UserProfileEvent.OnUserWeightChanged(it)) },
                    placeholder = if (state.weightUnit == WeightUnit.KG) "e.g., 70" else "e.g., 154",
                    error = state.weightError,
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
                OnboardingUnitToggle(
                    options = listOf("kg", "lbs"),
                    selected = if (state.weightUnit == WeightUnit.KG) 0 else 1,
                    onSelect = {
                        viewModel.onEvent(
                            UserProfileEvent.OnWeightUnitChanged(
                                if (it == 0) WeightUnit.KG else WeightUnit.LBS
                            )
                        )
                    }
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Gender ───────────────────────────────────────────────────────
            OnboardingSectionLabel("GENDER")

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Gender.values().forEach { gender ->
                    val selected = state.gender == gender
                    val label = gender.name
                        .lowercase()
                        .replace("_", " ")
                        .replaceFirstChar { it.uppercase() }

                    Box(
                        modifier = Modifier
                            .background(
                                color = if (selected) OnboardingColors.NothingWhite
                                else Color.Transparent,
                                shape = RoundedCornerShape(2.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (selected) OnboardingColors.NothingWhite
                                else OnboardingColors.Hairline,
                                shape = RoundedCornerShape(2.dp)
                            )
                            .clickable { viewModel.onEvent(UserProfileEvent.OnGenderChanged(gender)) }
                            .padding(horizontal = 16.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (selected) OnboardingColors.Void else OnboardingColors.DimWhite,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            // ── Buttons ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .border(1.dp, OnboardingColors.Hairline, RoundedCornerShape(2.dp))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "BACK",
                        color = OnboardingColors.DimWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .background(OnboardingColors.NothingWhite, RoundedCornerShape(2.dp))
                        .clickable { viewModel.onEvent(UserProfileEvent.NavtoScreen3) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CONTINUE",
                        color = OnboardingColors.Void,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
