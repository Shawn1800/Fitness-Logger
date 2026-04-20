package com.ghostbug.heavyliftsapp.screens.profile

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ghostbug.heavyliftsapp.data.domain.Gender

private object NothingColors {
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
    val GlyphRedDim  = Color(0xFF3A1010)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.onEvent(ProfileEvent.LoadProfile)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                ProfileUiEvent.NavigateToSignIn -> onLogoutClick()
                ProfileUiEvent.AccountDeleted -> onLogoutClick()
                ProfileUiEvent.ProfileSaved ->
                    snackbarHostState.showSnackbar("Profile saved")
                is ProfileUiEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    if (state.showLogoutDialog) {
        LogoutDialog(
            onConfirm = { viewModel.onEvent(ProfileEvent.Logout) },
            onDismiss = { viewModel.onEvent(ProfileEvent.HideLogoutDialog) }
        )
    }

    if (state.showDeleteDialog) {
        DeleteAccountDialog(
            isDeleting = state.isDeleting,
            onConfirm = { viewModel.onEvent(ProfileEvent.DeleteAccount) },
            onDismiss = { viewModel.onEvent(ProfileEvent.HideDeleteDialog) }
        )
    }

    Scaffold(
        containerColor = NothingColors.Void,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(NothingColors.Void)
        ) {
            // header
            ProfileHeader(
                isEditing = state.isEditing,
                isSaving = state.isSaving,
                onBack = onBackClick,
                onEdit = { viewModel.onEvent(ProfileEvent.StartEditing) },
                onSave = { viewModel.onEvent(ProfileEvent.SaveProfile) },
                onCancel = { viewModel.onEvent(ProfileEvent.CancelEditing) }
            )

            HorizontalDivider(color = NothingColors.Hairline, thickness = 0.5.dp)

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = NothingColors.NothingWhite,
                        strokeWidth = 1.5.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Spacer(Modifier.height(24.dp))

                    // profile picture + username
                    ProfilePictureSection(
                        profilePic = state.profilePic,
                        userName = state.userName,
                        email = state.email
                    )

                    Spacer(Modifier.height(32.dp))

                    // username field
                    SectionLabel("Username")
                    if (state.isEditing) {
                        NothingTextField(
                            value = state.userName,
                            onValueChange = {
                                viewModel.onEvent(ProfileEvent.OnUserNameChanged(it))
                            },
                            placeholder = "Username",
                            error = state.userNameError
                        )
                    } else {
                        ProfileRow(label = "Username", value = "@${state.userName}")
                    }

                    NothingDivider()

                    // age
                    SectionLabel("Age")
                    if (state.isEditing) {
                        NothingTextField(
                            value = state.ageInputText,
                            onValueChange = {
                                viewModel.onEvent(ProfileEvent.OnAgeChanged(it))
                            },
                            placeholder = "Age",
                            keyboardType = KeyboardType.Number
                        )
                    } else {
                        ProfileRow(
                            label = "Age",
                            value = state.age?.let { "$it years" } ?: "—"
                        )
                    }

                    NothingDivider()

                    // height
                    SectionLabel("Height")
                    if (state.isEditing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            NothingTextField(
                                value = state.heightInputText,
                                onValueChange = { input ->
                                    val digits = input.filter { it.isDigit() }
                                    if (state.heightUnit == HeightUnit.FEET) {
                                        if (digits.length <= 3)
                                            viewModel.onEvent(ProfileEvent.OnHeightChanged(input))
                                    } else {
                                        viewModel.onEvent(ProfileEvent.OnHeightChanged(input))
                                    }
                                },
                                placeholder = if (state.heightUnit == HeightUnit.CM)
                                    "e.g. 170" else "e.g. 5'10",
                                keyboardType = if (state.heightUnit == HeightUnit.FEET)
                                    KeyboardType.Number else KeyboardType.Decimal,
                                modifier = Modifier.weight(1f)
                            )
                            NothingUnitToggle(
                                options = listOf("cm", "ft"),
                                selected = if (state.heightUnit == HeightUnit.CM) 0 else 1,
                                onSelect = {
                                    viewModel.onEvent(
                                        ProfileEvent.OnHeightUnitChanged(
                                            if (it == 0) HeightUnit.CM else HeightUnit.FEET
                                        )
                                    )
                                }
                            )
                        }
                    } else {
                        ProfileRow(
                            label = "Height",
                            value = state.height?.let {
                                val totalInches = (it / 2.54f).toInt()
                                "${totalInches / 12}'${totalInches % 12}\" / ${"%.0f".format(it)} cm"
                            } ?: "—"
                        )
                    }

                    NothingDivider()

                    // weight
                    SectionLabel("Weight")
                    if (state.isEditing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            NothingTextField(
                                value = state.weightInputText,
                                onValueChange = {
                                    viewModel.onEvent(ProfileEvent.OnUserWeightChanged(it))
                                },
                                placeholder = if (state.weightUnit == WeightUnit.KG)
                                    "e.g. 70" else "e.g. 154",
                                keyboardType = KeyboardType.Decimal,
                                modifier = Modifier.weight(1f)
                            )
                            NothingUnitToggle(
                                options = listOf("kg", "lbs"),
                                selected = if (state.weightUnit == WeightUnit.KG) 0 else 1,
                                onSelect = {
                                    viewModel.onEvent(
                                        ProfileEvent.OnWeightUnitChanged(
                                            if (it == 0) WeightUnit.KG else WeightUnit.LBS
                                        )
                                    )
                                }
                            )
                        }
                    } else {
                        ProfileRow(
                            label = "Weight",
                            value = state.userWeight?.let {
                                "${"%.1f".format(it)} kg / ${"%.1f".format(it * 2.20462f)} lbs"
                            } ?: "—"
                        )
                    }

                    NothingDivider()

                    // gender
                    SectionLabel("Gender")
                    if (state.isEditing) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Gender.values().forEach { gender ->
                                val selected = state.gender == gender
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        viewModel.onEvent(ProfileEvent.OnGenderChanged(gender))
                                    },
                                    label = {
                                        Text(
                                            text = gender.name.lowercase()
                                                .replace("_", " ")
                                                .replaceFirstChar { it.uppercase() },
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (selected) NothingColors.Void
                                            else NothingColors.DimWhite
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NothingColors.NothingWhite,
                                        containerColor = NothingColors.Surface1
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = NothingColors.Hairline,
                                        selectedBorderColor = NothingColors.NothingWhite
                                    ),
                                    shape = RoundedCornerShape(3.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    } else {
                        ProfileRow(
                            label = "Gender",
                            value = state.gender?.name?.lowercase()
                                ?.replace("_", " ")
                                ?.replaceFirstChar { it.uppercase() } ?: "—"
                        )
                    }

                    NothingDivider()

                    // location
                    SectionLabel("Location")
                    if (state.isEditing) {
                        NothingTextField(
                            value = state.city ?: "",
                            onValueChange = {
                                viewModel.onEvent(ProfileEvent.OnCityChanged(it))
                            },
                            placeholder = "City"
                        )
                        Spacer(Modifier.height(8.dp))
                        NothingTextField(
                            value = state.country ?: "",
                            onValueChange = {
                                viewModel.onEvent(ProfileEvent.OnCountryChanged(it))
                            },
                            placeholder = "Country"
                        )
                    } else {
                        val location = listOfNotNull(state.city, state.country)
                            .joinToString(", ")
                        ProfileRow(
                            label = "Location",
                            value = location.ifBlank { "—" }
                        )
                    }

                    NothingDivider()

                    // email — read only always
                    ProfileRow(label = "Email", value = state.email.ifBlank { "—" })

                    Spacer(Modifier.height(40.dp))

                    // logout button
                    OutlinedButton(
                        onClick = { viewModel.onEvent(ProfileEvent.ShowLogoutDialog) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(3.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, NothingColors.Hairline
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NothingColors.OffWhite
                        )
                    ) {
                        Text(
                            text = "SIGN OUT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // delete account button
                    OutlinedButton(
                        onClick = { viewModel.onEvent(ProfileEvent.ShowDeleteDialog) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(3.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, NothingColors.GlyphRed.copy(alpha = 0.4f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NothingColors.GlyphRed
                        )
                    ) {
                        Text(
                            text = "DELETE ACCOUNT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeader(
    isEditing: Boolean,
    isSaving: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = if (isEditing) onCancel else onBack) {
            Icon(
                imageVector = if (isEditing) Icons.Default.Close
                else Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = NothingColors.DimWhite,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = if (isEditing) "EDITING" else "PROFILE",
            color = NothingColors.NothingWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            fontFamily = FontFamily.Monospace
        )

        if (isEditing) {
            IconButton(onClick = onSave, enabled = !isSaving) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = NothingColors.NothingWhite,
                        strokeWidth = 1.5.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        tint = NothingColors.NothingWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = NothingColors.DimWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ─── Profile picture ──────────────────────────────────────────────────────────

@Composable
private fun ProfilePictureSection(
    profilePic: String?,
    userName: String,
    email: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (profilePic != null) {
            AsyncImage(
                model = profilePic,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .border(1.dp, NothingColors.Hairline, CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(NothingColors.Surface1)
                    .border(1.dp, NothingColors.Hairline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = NothingColors.DimWhite,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "@$userName",
                color = NothingColors.NothingWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = email,
                color = NothingColors.DimWhite,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ─── Logout dialog ────────────────────────────────────────────────────────────

@Composable
private fun LogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NothingColors.Surface0, RoundedCornerShape(4.dp))
                .padding(1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NothingColors.Surface1, RoundedCornerShape(3.dp))
                    .padding(28.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(20.dp)
                                .background(NothingColors.NothingWhite)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "SIGN OUT",
                            color = NothingColors.NothingWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 3.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "You will be signed out of your account. Your data will be saved.",
                        color = NothingColors.DimWhite,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(3.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, NothingColors.Hairline
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = NothingColors.DimWhite
                            )
                        ) {
                            Text(
                                "CANCEL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(3.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NothingColors.NothingWhite,
                                contentColor = NothingColors.Void
                            )
                        ) {
                            Text(
                                "SIGN OUT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Delete dialog ────────────────────────────────────────────────────────────

@Composable
private fun DeleteAccountDialog(
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NothingColors.Surface0, RoundedCornerShape(4.dp))
                .padding(1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NothingColors.Surface1, RoundedCornerShape(3.dp))
                    .padding(28.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(20.dp)
                                .background(NothingColors.GlyphRed)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "DELETE ACCOUNT",
                            color = NothingColors.GlyphRed,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 3.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "This will permanently delete your account and all data. This cannot be undone.",
                        color = NothingColors.DimWhite,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(3.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, NothingColors.Hairline
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = NothingColors.DimWhite
                            )
                        ) {
                            Text(
                                "CANCEL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = onConfirm,
                            enabled = !isDeleting,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(3.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NothingColors.GlyphRed,
                                contentColor = NothingColors.NothingWhite
                            )
                        ) {
                            if (isDeleting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = NothingColors.NothingWhite,
                                    strokeWidth = 1.5.dp
                                )
                            } else {
                                Text(
                                    "DELETE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Reusable components ──────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Spacer(Modifier.height(16.dp))
    Text(
        text = text.uppercase(),
        color = NothingColors.DimWhite,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 3.sp,
        fontFamily = FontFamily.Monospace
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = NothingColors.DimWhite,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = NothingColors.NothingWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun NothingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                placeholder,
                color = NothingColors.FaintWhite,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
        },
        isError = error != null,
        supportingText = error?.let {
            { Text(it, color = NothingColors.GlyphRed, fontSize = 11.sp,
                fontFamily = FontFamily.Monospace) }
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = NothingColors.NothingWhite,
            unfocusedTextColor = NothingColors.OffWhite,
            focusedBorderColor = NothingColors.NothingWhite,
            unfocusedBorderColor = NothingColors.Hairline,
            cursorColor = NothingColors.NothingWhite,
            errorBorderColor = NothingColors.GlyphRed
        ),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp
        )
    )
}

@Composable
private fun NothingUnitToggle(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .background(NothingColors.Surface1, RoundedCornerShape(3.dp))
            .border(1.dp, NothingColors.Hairline, RoundedCornerShape(3.dp))
            .padding(top = 8.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selected
            Box(
                modifier = Modifier
                    .clickable { onSelect(index) }
                    .background(
                        if (isSelected) NothingColors.NothingWhite
                        else Color.Transparent
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) NothingColors.Void
                    else NothingColors.DimWhite,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun NothingDivider() {
    Spacer(Modifier.height(12.dp))
    HorizontalDivider(color = NothingColors.Hairline, thickness = 0.5.dp)
    Spacer(Modifier.height(4.dp))
}