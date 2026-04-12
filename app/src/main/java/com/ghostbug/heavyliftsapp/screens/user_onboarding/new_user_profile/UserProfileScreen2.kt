package com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile

import HeightUnit
import WeightUnit
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ghostbug.heavyliftsapp.data.domain.Gender

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UserProfileScreen2(
    viewModel: UserProfileViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "Tell us about yourself",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "This helps us personalise your experience",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            // Age
            SectionLabel("Age")
            OutlinedTextField(
                value = state.age?.toString() ?: "",
                onValueChange = { viewModel.onEvent(UserProfileEvent.OnAgeChanged(it)) },
                label = { Text("Age") },
                placeholder = { Text("e.g., 25") },
                isError = state.ageError != null,
                supportingText = {
                    Text(
                        text = state.ageError ?: "Must be 12 or older",
                        color = if (state.ageError != null)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Height
            SectionLabel("Height")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                OutlinedTextField(
                    value = state.heightInputText,
                    onValueChange = { input ->
                        // only allow digits — apostrophe is auto-inserted by ViewModel
                        val digitsOnly = input.filter { it.isDigit() }
                        if (digitsOnly.length <= 3) {
                            viewModel.onEvent(UserProfileEvent.OnHeightChanged(input))
                        }
                    },
                    label = { Text("Height") },
                    placeholder = {
                        Text(if (state.heightUnit == HeightUnit.CM) "e.g., 170" else "e.g., 5'6")
                    },
                    isError = state.heightError != null,
                    supportingText = {
                        Text(
                            text = state.heightError ?: if (state.heightUnit == HeightUnit.CM)
                                "Min 91 cm (3 ft)"
                            else
                                "Type feet then inches e.g. 56 → 5'6",
                            color = if (state.heightError != null)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        // Number (not Decimal) since apostrophe is auto-inserted, no dots needed
                        keyboardType = if (state.heightUnit == HeightUnit.FEET)
                            KeyboardType.Number
                        else
                            KeyboardType.Decimal
                    ),
                    singleLine = true
                )
                UnitToggle(
                    options = listOf("cm", "ft"),
                    selected = if (state.heightUnit == HeightUnit.CM) 0 else 1,
                    onSelect = {
                        viewModel.onEvent(
                            UserProfileEvent.OnHeightUnitChanged(
                                if (it == 0) HeightUnit.CM else HeightUnit.FEET
                            )
                        )
                    },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Weight
            SectionLabel("Weight")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                OutlinedTextField(
                    value = state.weightInputText,
                    onValueChange = { viewModel.onEvent(UserProfileEvent.OnUserWeightChanged(it)) },
                    label = { Text("Weight") },
                    placeholder = {
                        Text(if (state.weightUnit == WeightUnit.KG) "e.g., 70" else "e.g., 154")
                    },
                    isError = state.weightError != null,
                    supportingText = {
                        Text(
                            text = state.weightError ?: if (state.weightUnit == WeightUnit.KG)
                                "30–300 kg"
                            else
                                "66–661 lbs",
                            color = if (state.weightError != null)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                UnitToggle(
                    options = listOf("kg", "lbs"),
                    selected = if (state.weightUnit == WeightUnit.KG) 0 else 1,
                    onSelect = {
                        viewModel.onEvent(
                            UserProfileEvent.OnWeightUnitChanged(
                                if (it == 0) WeightUnit.KG else WeightUnit.LBS
                            )
                        )
                    },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Gender
            SectionLabel("Gender")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Gender.values().forEach { gender ->
                    val selected = state.gender == gender
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.onEvent(UserProfileEvent.OnGenderChanged(gender)) },
                        label = {
                            Text(
                                text = gender.name
                                    .lowercase()
                                    .replace("_", " ")
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = if (selected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(8.dp))

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
                    onClick = { viewModel.onEvent(UserProfileEvent.NavtoScreen3) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Next")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun UnitToggle(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Surface(
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Row {
                options.forEachIndexed { index, label ->
                    val isSelected = index == selected
                    Box(
                        modifier = Modifier
                            .clickable { onSelect(index) }
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}