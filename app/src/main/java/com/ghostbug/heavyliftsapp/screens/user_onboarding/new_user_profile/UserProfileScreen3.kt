package com.ghostbug.heavyliftsapp.screens.user_onboarding.step3

import HeightUnit
import WeightUnit
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ghostbug.heavyliftsapp.screens.home.HomeEvent
import com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile.OnboardingColors
import com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile.OnboardingSectionLabel
import com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile.OnboardingStepProgress
import com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile.OnboardingTextField
import com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile.UserProfileEvent
import com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile.UserProfileUiEvent
import com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile.UserProfileViewModel
import com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile.dotMatrix

@Composable
fun UserProfileScreen3(
    viewModel: UserProfileViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                UserProfileUiEvent.NavToHome -> onNext()
                UserProfileUiEvent.NavToScreen2->onBack()
                is UserProfileUiEvent.SendSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> {}
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
                OnboardingStepProgress(current = 2)
            }

            Spacer(Modifier.height(44.dp))

            Text(
                text = "FINAL\nDETAILS.",
                color = OnboardingColors.NothingWhite,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 38.sp,
                letterSpacing = (-0.5).sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "optional — but makes the experience yours.",
                color = OnboardingColors.DimWhite,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(36.dp))

            // ── Profile picture ───────────────────────────────────────────────
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (state.profilePic != null) {
                    AsyncImage(
                        model = state.profilePic,
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .border(1.dp, OnboardingColors.Hairline, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(OnboardingColors.Surface1)
                            .border(1.dp, OnboardingColors.Hairline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp),
                            tint = OnboardingColors.FaintWhite
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Summary card ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OnboardingColors.Surface1, RoundedCornerShape(4.dp))
                    .border(1.dp, OnboardingColors.Hairline, RoundedCornerShape(4.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "PROFILE SUMMARY",
                    color = OnboardingColors.DimWhite,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    fontFamily = FontFamily.Monospace
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(OnboardingColors.Hairline)
                )

                SummaryRow("HANDLE", "@${state.userName}")

                state.age?.let { SummaryRow("AGE", "$it yrs") }

                state.height?.let { heightCm ->
                    val display = when (state.heightUnit) {
                        HeightUnit.FEET -> viewModel.cmToFeetInches(heightCm)
                        HeightUnit.CM -> "%.1f cm".format(heightCm)
                    }
                    SummaryRow("HEIGHT", display)
                }

                state.userWeight?.let { weightKg ->
                    val display = when (state.weightUnit) {
                        WeightUnit.KG -> "%.1f kg".format(weightKg)
                        WeightUnit.LBS -> "%.1f lbs".format(weightKg * 2.20462f)
                    }
                    SummaryRow("WEIGHT", display)
                }

                state.gender?.let { gender ->
                    SummaryRow(
                        label = "GENDER",
                        value = gender.name.lowercase()
                            .replace("_", " ")
                            .replaceFirstChar { it.uppercase() }
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Location (optional) ───────────────────────────────────────────
            OnboardingSectionLabel("LOCATION  (OPTIONAL)")

            OnboardingTextField(
                value = state.city ?: "",
                onValueChange = { viewModel.onEvent(UserProfileEvent.OnCityChanged(it)) },
                placeholder = "city",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OnboardingTextField(
                value = state.country ?: "",
                onValueChange = { viewModel.onEvent(UserProfileEvent.OnCountryChanged(it)) },
                placeholder = "country",
                modifier = Modifier.fillMaxWidth()
            )

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
                        .background(
                            color = if (state.isLoading) OnboardingColors.Surface2
                            else OnboardingColors.GlyphRed,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .clickable(enabled = !state.isLoading) {
                            viewModel.onEvent(UserProfileEvent.OnSubmitProfile)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = OnboardingColors.NothingWhite
                        )
                    } else {
                        Text(
                            text = "LET'S GO.",
                            color = OnboardingColors.NothingWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = OnboardingColors.DimWhite,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = OnboardingColors.OffWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}
