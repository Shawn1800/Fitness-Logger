package com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ghostbug.heavyliftsapp.ui.theme.HeavyLiftsColors

@Composable
fun UserProfileScreen1(
    viewModel: UserProfileViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                UserProfileUiEvent.NavToScreen2 -> onNext()
                is UserProfileUiEvent.SendSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HeavyLiftsColors.Bg)
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
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = HeavyLiftsColors.Fg3
                    )
                }
                Spacer(Modifier.weight(1f))
                OnboardingStepProgress(current = 0)
            }

            Spacer(Modifier.height(44.dp))

            Text(
                text = "CHOOSE YOUR\nHANDLE.",
                color = HeavyLiftsColors.Fg1,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 38.sp,
                letterSpacing = (-0.5).sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "this is how others will find you.",
                color = HeavyLiftsColors.Fg3,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(52.dp))

            OnboardingSectionLabel("USERNAME")

            OnboardingTextField(
                value = state.userName,
                onValueChange = { viewModel.onEvent(UserProfileEvent.OnUserNameChanged(it)) },
                placeholder = "e.g., kingruffy",
                error = state.userNameError,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    when {
                        state.isLoading -> CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = HeavyLiftsColors.Fg3
                        )
                        state.userNameError != null -> Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = HeavyLiftsColors.Accent,
                            modifier = Modifier.size(18.dp)
                        )
                        state.userName.length in 3..20 && !state.isLoading -> Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = HeavyLiftsColors.Fg1,
                            modifier = Modifier.size(18.dp)
                        )
                        else -> {}
                    }
                }
            )

            if (state.userNameError == null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "3–20 characters  ·  must be unique",
                    color = HeavyLiftsColors.BgChip,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.3.sp
                )
            }

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(
                        color = if (state.isLoading) HeavyLiftsColors.BgOverlay
                        else HeavyLiftsColors.Fg1,
                        shape = RoundedCornerShape(2.dp)
                    )
                    .clickable(enabled = !state.isLoading) {
                        viewModel.onEvent(UserProfileEvent.NavtoScreen2)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = HeavyLiftsColors.Fg3
                    )
                } else {
                    Text(
                        text = "CONTINUE",
                        color = HeavyLiftsColors.Bg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
