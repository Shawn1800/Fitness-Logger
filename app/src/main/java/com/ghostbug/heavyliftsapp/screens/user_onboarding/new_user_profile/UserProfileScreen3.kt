package com.ghostbug.heavyliftsapp.screens.user_onboarding.step3

import HeightUnit
import WeightUnit
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile.UserProfileEvent
import com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile.UserProfileUiEvent
import com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile.UserProfileViewModel

@Composable
fun UserProfileScreen3(
    viewModel: UserProfileViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                UserProfileUiEvent.NavToHome -> onNext()
                is UserProfileUiEvent.SendSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Text(
                text = "Final touches",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Add your location — optional but helps personalise your feed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Profile picture
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.profilePic != null) {
                    AsyncImage(
                        model = state.profilePic,
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Default profile",
                            modifier = Modifier.size(50.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Summary card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Your profile summary",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))

                    SummaryRow("Username", "@${state.userName}")

                    state.age?.let {
                        SummaryRow("Age", "$it years")
                    }

                    state.height?.let { heightCm ->
                        val heightDisplay = when (state.heightUnit) {
                            HeightUnit.FEET -> viewModel.cmToFeetInches(heightCm)
                            HeightUnit.CM -> "%.1f cm".format(heightCm)
                        }
                        SummaryRow("Height", heightDisplay)
                    }

                    state.userWeight?.let { weightKg ->
                        val weightDisplay = when (state.weightUnit) {
                            WeightUnit.KG -> "%.1f kg".format(weightKg)
                            WeightUnit.LBS -> "%.1f lbs".format(weightKg * 2.20462f)
                        }
                        SummaryRow("Weight", weightDisplay)
                    }

                    state.gender?.let { gender ->
                        SummaryRow(
                            label = "Gender",
                            value = gender.name
                                .lowercase()
                                .replace("_", " ")
                                .replaceFirstChar { it.uppercase() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Location (optional)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = state.city ?: "",
                onValueChange = { viewModel.onEvent(UserProfileEvent.OnCityChanged(it)) },
                label = { Text("City") },
                placeholder = { Text("e.g., Mumbai") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.country ?: "",
                onValueChange = { viewModel.onEvent(UserProfileEvent.OnCountryChanged(it)) },
                label = { Text("Country") },
                placeholder = { Text("e.g., India") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Back")
                }
                Button(
                    onClick = { viewModel.onEvent(UserProfileEvent.OnSubmitProfile) },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Complete profile")
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
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}