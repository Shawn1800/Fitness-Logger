package com.ghostbug.heavyliftsapp.screens.signUp

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghostbug.heavyliftsapp.ui.theme.HeavyLiftsColors
import com.ghostbug.heavyliftsapp.ui.theme.HeavyLiftsType
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(
    state: SignUpState,
    onEvent: (SignUpEvent) -> Unit,
    uiEvent: SharedFlow<SignUpUiEvent>,
    onNavigateToUserProfileScreen1: () -> Unit,
    onNavigateToSignIn: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        uiEvent.collect { event ->
            when (event) {
                is SignUpUiEvent.NavigateToSignIn -> onNavigateToSignIn()
                is SignUpUiEvent.NavToScreen1 -> {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Account created. Welcome.") }
                    onNavigateToUserProfileScreen1()
                }
                is SignUpUiEvent.ShowError ->
                    coroutineScope.launch { snackbarHostState.showSnackbar(event.message) }
                SignUpUiEvent.NavigateToHome -> { /* unused */ }
                SignUpUiEvent.NavigateToOnboarding -> { /* unused */ }
            }
        }
    }

    Scaffold(
        containerColor = HeavyLiftsColors.Bg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(HeavyLiftsColors.Bg)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))
            BrandMark()

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Create your account",
                color = HeavyLiftsColors.Fg1,
                fontFamily = HeavyLiftsType.Display,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Track your lifts. Watch your 1RM climb.",
                color = HeavyLiftsColors.Fg3,
                fontFamily = HeavyLiftsType.Body,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(36.dp))

            HLLabel("Email")
            HLField(
                value = state.email,
                onValueChange = { onEvent(SignUpEvent.OnEmailChange(it)) },
                placeholder = "you@example.com",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                trailingIcon = null
            )

            Spacer(Modifier.height(14.dp))

            HLLabel("Password")
            HLField(
                value = state.password,
                onValueChange = { onEvent(SignUpEvent.OnPasswordChange(it)) },
                placeholder = "At least 6 characters",
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = HeavyLiftsColors.Fg3,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )

            Spacer(Modifier.height(28.dp))

            CreateAccountPill(
                isLoading = state.isLoading,
                onClick = { onEvent(SignUpEvent.OnSignUpClick) }
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already lifting?",
                    color = HeavyLiftsColors.Fg3,
                    fontFamily = HeavyLiftsType.Body,
                    fontSize = 13.sp
                )
                TextButton(onClick = { onEvent(SignUpEvent.OnNavigateToSignIn) }) {
                    Text(
                        text = "Sign in",
                        color = HeavyLiftsColors.Accent,
                        fontFamily = HeavyLiftsType.Display,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun BrandMark() {
    Box(
        Modifier
            .size(72.dp)
            .drawBehind {
                drawRoundRect(
                    color = Color(0x38000000),
                    topLeft = Offset(0f, 6.dp.toPx()),
                    cornerRadius = CornerRadius(72.dp.toPx())
                )
            }
            .background(HeavyLiftsColors.Accent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "HL",
            color = HeavyLiftsColors.Fg1,
            fontFamily = HeavyLiftsType.Display,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HLLabel(text: String) {
    Text(
        text = text,
        color = HeavyLiftsColors.Fg3,
        fontFamily = HeavyLiftsType.Display,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
private fun HLField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)?
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                placeholder,
                color = HeavyLiftsColors.Fg4,
                fontFamily = HeavyLiftsType.Body,
                fontSize = 14.sp
            )
        },
        trailingIcon = trailingIcon,
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = HeavyLiftsColors.Fg1,
            unfocusedTextColor = HeavyLiftsColors.Fg2,
            focusedBorderColor = HeavyLiftsColors.Accent,
            unfocusedBorderColor = HeavyLiftsColors.BorderSubtle,
            cursorColor = HeavyLiftsColors.Accent,
            focusedContainerColor = HeavyLiftsColors.BgElevated,
            unfocusedContainerColor = HeavyLiftsColors.BgElevated
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
private fun CreateAccountPill(isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = HeavyLiftsColors.Accent,
            contentColor = HeavyLiftsColors.Fg1,
            disabledContainerColor = HeavyLiftsColors.BgChip,
            disabledContentColor = HeavyLiftsColors.Fg3
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp, pressedElevation = 0.dp, disabledElevation = 0.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = Color(0x38000000),
                    topLeft = Offset(0f, 6.dp.toPx()),
                    cornerRadius = CornerRadius(28.dp.toPx())
                )
            }
            .height(56.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = HeavyLiftsColors.Fg1,
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Creating account…",
                fontFamily = HeavyLiftsType.Display,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Text(
                text = "Create account",
                fontFamily = HeavyLiftsType.Display,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
