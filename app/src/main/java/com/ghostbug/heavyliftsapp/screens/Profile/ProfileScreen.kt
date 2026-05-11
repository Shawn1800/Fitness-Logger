package com.ghostbug.heavyliftsapp.screens.profile

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ghostbug.heavyliftsapp.data.domain.Gender
import com.ghostbug.heavyliftsapp.ui.theme.HeavyLiftsColors
import com.ghostbug.heavyliftsapp.ui.theme.HeavyLiftsType

// HeavyLifts Profile screen — warm olive / cream / orange brand.
// Mirrors the Claude design "ProfileScreenV1": avatar hero, stat tiles, body grid,
// editable fields, and friendly sign-out / delete actions.

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.onEvent(ProfileEvent.LoadProfile) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                ProfileUiEvent.NavigateToSignIn -> onLogoutClick()
                ProfileUiEvent.AccountDeleted   -> onLogoutClick()
                ProfileUiEvent.ProfileSaved     -> snackbarHostState.showSnackbar("Profile saved")
                is ProfileUiEvent.ShowSnackbar  -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    if (state.showLogoutDialog) {
        SignOutDialog(
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
        containerColor = HeavyLiftsColors.Bg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(HeavyLiftsColors.Bg)
        ) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = HeavyLiftsColors.Accent,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(28.dp)
                    )
                }
                return@Box
            }

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                ProfileTopBar(
                    isEditing = state.isEditing,
                    isSaving = state.isSaving,
                    onBack = onBackClick,
                    onEdit = { viewModel.onEvent(ProfileEvent.StartEditing) },
                    onSave = { viewModel.onEvent(ProfileEvent.SaveProfile) },
                    onCancel = { viewModel.onEvent(ProfileEvent.CancelEditing) }
                )

                ProfileHero(
                    profilePic = state.profilePic,
                    userName = state.userName,
                    email = state.email
                )

                Spacer(Modifier.height(16.dp))
//                StatsRow(streak = 7, workouts = 48, totalLifted = "312K")

                Spacer(Modifier.height(20.dp))
                if (state.isEditing) {
                    ProfileEditCard(state = state, onEvent = viewModel::onEvent)
                } else {
                    ProfileDetailsCard(state = state)
                }

                Spacer(Modifier.height(20.dp))
                AccountActions(
                    onSignOut = { viewModel.onEvent(ProfileEvent.ShowLogoutDialog) },
                    onDeleteAccount = { viewModel.onEvent(ProfileEvent.ShowDeleteDialog) }
                )

                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

// ─── Top bar ──────────────────────────────────────────────────────────────────

@Composable
private fun ProfileTopBar(
    isEditing: Boolean,
    isSaving: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = if (isEditing) onCancel else onBack) {
            Icon(
                imageVector = if (isEditing) Icons.Default.Close
                              else Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = HeavyLiftsColors.Fg2,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = if (isEditing) "Edit profile" else "Profile",
            color = HeavyLiftsColors.Fg1,
            fontFamily = HeavyLiftsType.Display,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )
        if (isEditing) {
            IconButton(onClick = onSave, enabled = !isSaving) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = HeavyLiftsColors.Accent,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        tint = HeavyLiftsColors.Accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        } else {
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = HeavyLiftsColors.Fg2,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── Hero (avatar, name, email) ──────────────────────────────────────────────

@Composable
private fun ProfileHero(profilePic: String?, userName: String, email: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(96.dp)
                .drawBehind {
                    drawRoundRect(
                        color = Color(0x38000000),
                        topLeft = Offset(0f, 6.dp.toPx()),
                        cornerRadius = CornerRadius(96.dp.toPx())
                    )
                }
                .clip(CircleShape)
                .background(HeavyLiftsColors.Accent),
            contentAlignment = Alignment.Center
        ) {
            if (profilePic != null) {
                AsyncImage(
                    model = profilePic,
                    contentDescription = "Profile picture",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = (userName.firstOrNull()?.uppercase() ?: "S"),
                    color = HeavyLiftsColors.Fg1,
                    fontFamily = HeavyLiftsType.Display,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = userName.ifBlank { "Lifter" },
            color = HeavyLiftsColors.Fg1,
            fontFamily = HeavyLiftsType.Display,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (email.isNotBlank()) "@${userName.ifBlank { "you" }} · $email"
                   else "@${userName.ifBlank { "you" }}",
            color = HeavyLiftsColors.Fg3,
            fontFamily = HeavyLiftsType.Body,
            fontSize = 13.sp
        )
    }
}

// ─── Stats row (streak / workouts / volume) ──────────────────────────────────

@Composable
private fun StatsRow(streak: Int, workouts: Int, totalLifted: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatTile(value = streak.toString(), unit = "streak", accent = true, modifier = Modifier.weight(1f))
        StatTile(value = workouts.toString(), unit = "workouts", modifier = Modifier.weight(1f))
        StatTile(value = totalLifted, unit = "lb lifted", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(
    value: String,
    unit: String,
    accent: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(HeavyLiftsColors.BgElevated, RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (accent) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = HeavyLiftsColors.Accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = value,
                    color = HeavyLiftsColors.Accent,
                    fontFamily = HeavyLiftsType.Display,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(
                text = value,
                color = HeavyLiftsColors.Fg1,
                fontFamily = HeavyLiftsType.Display,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = unit,
            color = HeavyLiftsColors.Fg3,
            fontFamily = HeavyLiftsType.Body,
            fontSize = 11.sp
        )
    }
}

// ─── Read-only details card ──────────────────────────────────────────────────

@Composable
private fun ProfileDetailsCard(state: ProfileState) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(HeavyLiftsColors.BgElevated, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        SectionHeader("About you")
        DetailRow("Username", "@${state.userName.ifBlank { "—" }}")
        Hairline()
        DetailRow("Age", state.age?.let { "$it years" } ?: "—")
        Hairline()
        DetailRow("Height", state.height?.let {
            val totalInches = (it / 2.54f).toInt()
            "${totalInches / 12}'${totalInches % 12}\" · ${"%.0f".format(it)} cm"
        } ?: "—")
        Hairline()
        DetailRow("Weight", state.userWeight?.let {
            "${"%.1f".format(it)} kg · ${"%.1f".format(it * 2.20462f)} lbs"
        } ?: "—")
        Hairline()
        DetailRow("Gender", state.gender?.name?.lowercase()
            ?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: "—")
        Hairline()
        DetailRow("Location", listOfNotNull(state.city, state.country)
            .joinToString(", ").ifBlank { "—" })
        Hairline()
        DetailRow("Email", state.email.ifBlank { "—" })
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SectionHeader(text: String) {
    Spacer(Modifier.height(12.dp))
    Text(
        text = text,
        color = HeavyLiftsColors.Fg1,
        fontFamily = HeavyLiftsType.Display,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = HeavyLiftsColors.Fg3,
            fontFamily = HeavyLiftsType.Body,
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = HeavyLiftsColors.Fg1,
            fontFamily = HeavyLiftsType.Display,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun Hairline() {
    HorizontalDivider(color = HeavyLiftsColors.BorderSubtle, thickness = 0.5.dp)
}

// ─── Editable fields card ────────────────────────────────────────────────────

@Composable
private fun ProfileEditCard(
    state: ProfileState,
    onEvent: (ProfileEvent) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(HeavyLiftsColors.BgElevated, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionHeader("About you")

        FieldLabel("Username")
        HLTextField(
            value = state.userName,
            onValueChange = { onEvent(ProfileEvent.OnUserNameChanged(it)) },
            placeholder = "Username",
            error = state.userNameError
        )

        FieldLabel("Age")
        HLTextField(
            value = state.ageInputText,
            onValueChange = { onEvent(ProfileEvent.OnAgeChanged(it)) },
            placeholder = "e.g. 25",
            keyboardType = KeyboardType.Number
        )

        FieldLabel("Height")
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HLTextField(
                value = state.heightInputText,
                onValueChange = {
                    val digits = it.filter { ch -> ch.isDigit() }
                    if (state.heightUnit == HeightUnit.FEET) {
                        if (digits.length <= 3) onEvent(ProfileEvent.OnHeightChanged(it))
                    } else {
                        onEvent(ProfileEvent.OnHeightChanged(it))
                    }
                },
                placeholder = if (state.heightUnit == HeightUnit.CM) "e.g. 175" else "e.g. 5'10",
                keyboardType = if (state.heightUnit == HeightUnit.FEET)
                    KeyboardType.Number else KeyboardType.Decimal,
                modifier = Modifier.weight(1f)
            )
            UnitToggle(
                options = listOf("cm", "ft"),
                selected = if (state.heightUnit == HeightUnit.CM) 0 else 1,
                onSelect = {
                    onEvent(ProfileEvent.OnHeightUnitChanged(
                        if (it == 0) HeightUnit.CM else HeightUnit.FEET
                    ))
                }
            )
        }

        FieldLabel("Weight")
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HLTextField(
                value = state.weightInputText,
                onValueChange = { onEvent(ProfileEvent.OnUserWeightChanged(it)) },
                placeholder = if (state.weightUnit == WeightUnit.KG) "e.g. 70" else "e.g. 154",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f)
            )
            UnitToggle(
                options = listOf("kg", "lbs"),
                selected = if (state.weightUnit == WeightUnit.KG) 0 else 1,
                onSelect = {
                    onEvent(ProfileEvent.OnWeightUnitChanged(
                        if (it == 0) WeightUnit.KG else WeightUnit.LBS
                    ))
                }
            )
        }

        FieldLabel("Gender")
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Gender.values().forEach { gender ->
                val selected = state.gender == gender
                GenderChip(
                    label = gender.name.lowercase().replace("_", " ")
                        .replaceFirstChar { it.uppercase() },
                    selected = selected,
                    onClick = { onEvent(ProfileEvent.OnGenderChanged(gender)) }
                )
            }
        }

        FieldLabel("Location")
        HLTextField(
            value = state.city ?: "",
            onValueChange = { onEvent(ProfileEvent.OnCityChanged(it)) },
            placeholder = "City"
        )
        HLTextField(
            value = state.country ?: "",
            onValueChange = { onEvent(ProfileEvent.OnCountryChanged(it)) },
            placeholder = "Country"
        )

        FieldLabel("Email")
        Text(
            text = state.email.ifBlank { "—" },
            color = HeavyLiftsColors.Fg2,
            fontFamily = HeavyLiftsType.Body,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = HeavyLiftsColors.Fg3,
        fontFamily = HeavyLiftsType.Display,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp
    )
}

@Composable
private fun GenderChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) HeavyLiftsColors.Accent else HeavyLiftsColors.BgChip
    val fg = if (selected) HeavyLiftsColors.Fg1 else HeavyLiftsColors.Fg2
    Box(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = fg,
            fontFamily = HeavyLiftsType.Display,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun HLTextField(
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
                color = HeavyLiftsColors.Fg4,
                fontFamily = HeavyLiftsType.Body,
                fontSize = 14.sp
            )
        },
        isError = error != null,
        supportingText = error?.let {
            { Text(it, color = HeavyLiftsColors.Danger, fontSize = 11.sp,
                fontFamily = HeavyLiftsType.Body) }
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = HeavyLiftsColors.Fg1,
            unfocusedTextColor = HeavyLiftsColors.Fg2,
            focusedContainerColor = HeavyLiftsColors.Bg,
            unfocusedContainerColor = HeavyLiftsColors.Bg,
            focusedBorderColor = HeavyLiftsColors.Accent,
            unfocusedBorderColor = HeavyLiftsColors.BorderSubtle,
            cursorColor = HeavyLiftsColors.Accent,
            errorBorderColor = HeavyLiftsColors.Danger
        ),
        textStyle = TextStyle(
            fontFamily = HeavyLiftsType.Body,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = HeavyLiftsColors.Fg1
        )
    )
}

@Composable
private fun UnitToggle(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(HeavyLiftsColors.Bg)
            .border(1.dp, HeavyLiftsColors.BorderSubtle, RoundedCornerShape(14.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selected
            Box(
                Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (isSelected) HeavyLiftsColors.Accent else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) HeavyLiftsColors.Fg1 else HeavyLiftsColors.Fg3,
                    fontFamily = HeavyLiftsType.Display,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─── Account actions ─────────────────────────────────────────────────────────

@Composable
private fun AccountActions(onSignOut: () -> Unit, onDeleteAccount: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(HeavyLiftsColors.BgElevated, RoundedCornerShape(20.dp))
    ) {
        SettingRow(label = "Sign out", color = HeavyLiftsColors.Fg1, onClick = onSignOut)
        Hairline()
        SettingRow(label = "Delete account", color = HeavyLiftsColors.Danger, onClick = onDeleteAccount)
    }
}

@Composable
private fun SettingRow(label: String, color: Color, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = color,
            fontFamily = HeavyLiftsType.Display,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "›",
            color = HeavyLiftsColors.Fg3,
            fontFamily = HeavyLiftsType.Display,
            fontSize = 18.sp
        )
    }
}

// ─── Dialogs ─────────────────────────────────────────────────────────────────

@Composable
private fun SignOutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    HLDialog(
        title = "Sign out?",
        body = "You can sign back in any time. Your data is safe.",
        confirmLabel = "Sign out",
        confirmColor = HeavyLiftsColors.Accent,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
private fun DeleteAccountDialog(isDeleting: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    HLDialog(
        title = "Delete account?",
        body = "This permanently removes your account and all your lifts. This can't be undone.",
        confirmLabel = if (isDeleting) "Deleting…" else "Delete",
        confirmColor = HeavyLiftsColors.Danger,
        confirmEnabled = !isDeleting,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
private fun HLDialog(
    title: String,
    body: String,
    confirmLabel: String,
    confirmColor: Color,
    confirmEnabled: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(HeavyLiftsColors.BgElevated, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Text(
                text = title,
                color = HeavyLiftsColors.Fg1,
                fontFamily = HeavyLiftsType.Display,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = body,
                color = HeavyLiftsColors.Fg3,
                fontFamily = HeavyLiftsType.Body,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
            Spacer(Modifier.height(20.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HeavyLiftsColors.BorderDefault),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HeavyLiftsColors.Fg2)
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = HeavyLiftsType.Display,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Button(
                    onClick = onConfirm,
                    enabled = confirmEnabled,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = confirmColor,
                        contentColor = HeavyLiftsColors.Fg1
                    )
                ) {
                    Text(
                        text = confirmLabel,
                        fontFamily = HeavyLiftsType.Display,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
