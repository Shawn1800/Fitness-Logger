package com.ghostbug.heavyLifts.ui.screen.signIn

import SignInState
import SignInUiEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

// ─── Nothing OS Design System ─────────────────────────────────────────────────

private object NothingColors {
    val Void         = Color(0xFF0A0A0A)
    val Surface0     = Color(0xFF111111)
    val Surface1     = Color(0xFF1A1A1A)
    val Hairline     = Color(0xFF2C2C2C)
    val NothingWhite = Color(0xFFFFFFFF)
    val DimWhite     = Color(0xFF8A8A8A)
    val FaintWhite   = Color(0xFF3A3A3A)
    val GlyphRed     = Color(0xFFFF3A3A)
}

private fun Modifier.dotMatrixBackground(
    dotColor: Color = NothingColors.FaintWhite.copy(alpha = 0.13f),
    spacing: Float = 14f,
    radius: Float = 1.1f
): Modifier = this.drawBehind {
    val cols = (size.width / spacing).toInt() + 1
    val rows = (size.height / spacing).toInt() + 1
    for (col in 0..cols) {
        for (row in 0..rows) {
            drawCircle(
                color = dotColor,
                radius = radius,
                center = Offset(col * spacing, row * spacing)
            )
        }
    }
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun SignInScreen(
    state: SignInState,
    onEvent: (SignInEvent) -> Unit,
    uiEvent: SharedFlow<SignInUiEvent>,
    onNavigateToHome: () -> Unit,
    onNavigateToSignUp: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        uiEvent.collect { event ->
            when (event) {
                is SignInUiEvent.NavigateToHome -> onNavigateToHome()
                is SignInUiEvent.ShowError -> {
                    coroutineScope.launch { snackbarHostState.showSnackbar(event.message) }
                }
                SignInUiEvent.NavigateToSignUp -> onNavigateToSignUp()
            }
        }
    }

    Scaffold(
        containerColor = NothingColors.Void,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(NothingColors.Void)
        ) {
            // Full-screen dot matrix on the upper half only — creates a "glyph panel" feel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f)
                    .dotMatrixBackground()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                // ── Glyph logo mark ───────────────────────────────────
                NothingGlyphMark()

                Spacer(modifier = Modifier.height(32.dp))

                // ── Heading ───────────────────────────────────────────
                NothingSignInHeading()

                Spacer(modifier = Modifier.height(48.dp))

                // ── Email field ───────────────────────────────────────
                NothingInputField(
                    value = state.email,
                    onValueChange = { onEvent(SignInEvent.OnEmailChange(it)) },
                    label = "EMAIL",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    trailingIcon = null
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ── Password field ────────────────────────────────────
                NothingInputField(
                    value = state.password,
                    onValueChange = { onEvent(SignInEvent.OnPasswordChange(it)) },
                    label = "PASSWORD",
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = { defaultKeyboardAction(ImeAction.Done) }
                    ),
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Filled.Visibility
                                else
                                    Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = NothingColors.DimWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ── Sign In button ────────────────────────────────────
                NothingSignInButton(
                    isLoading = state.isLoading,
                    onClick = { onEvent(SignInEvent.OnSignInClick) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Sign Up navigation ────────────────────────────────
                NothingSignUpLink(onNavigateToSignUp = onNavigateToSignUp)

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

// ─── Glyph Logo Mark ──────────────────────────────────────────────────────────
// Mimics Nothing's physical glyph interface — a minimal dot composition

@Composable
private fun NothingGlyphMark() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Top arc of dots
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(5.dp).background(NothingColors.FaintWhite, CircleShape))
            Box(Modifier.size(5.dp).background(NothingColors.DimWhite, CircleShape))
            Box(Modifier.size(5.dp).background(NothingColors.FaintWhite, CircleShape))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(5.dp).background(NothingColors.DimWhite, CircleShape))
            // Centre red dot — the glyph focal point
            Box(Modifier.size(8.dp).background(NothingColors.GlyphRed, CircleShape))
            Box(Modifier.size(5.dp).background(NothingColors.DimWhite, CircleShape))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(5.dp).background(NothingColors.FaintWhite, CircleShape))
            Box(Modifier.size(5.dp).background(NothingColors.DimWhite, CircleShape))
            Box(Modifier.size(5.dp).background(NothingColors.FaintWhite, CircleShape))
        }
    }
}

// ─── Heading ──────────────────────────────────────────────────────────────────

@Composable
private fun NothingSignInHeading() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "HEAVY LIFTS",
            color = NothingColors.DimWhite,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "SIGN IN",
            color = NothingColors.NothingWhite,
            fontWeight = FontWeight.Black,
            fontSize = 30.sp,
            letterSpacing = (-1).sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Thin glyph underline
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(2.dp)
                .background(NothingColors.GlyphRed, RoundedCornerShape(1.dp))
        )
    }
}

// ─── Input Field ──────────────────────────────────────────────────────────────

@Composable
private fun NothingInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    modifier: Modifier = Modifier,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)?
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = {
            Text(
                text = label,
                fontSize = 9.sp,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )
        },
        trailingIcon = trailingIcon,
        singleLine = true,
        shape = RoundedCornerShape(3.dp),
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor        = NothingColors.NothingWhite,
            unfocusedTextColor      = NothingColors.NothingWhite,
            focusedBorderColor      = NothingColors.NothingWhite,
            unfocusedBorderColor    = NothingColors.Hairline,
            focusedLabelColor       = NothingColors.DimWhite,
            unfocusedLabelColor     = NothingColors.FaintWhite,
            cursorColor             = NothingColors.GlyphRed,
            focusedContainerColor   = NothingColors.Surface0,
            unfocusedContainerColor = NothingColors.Surface0,
        )
    )
}

// ─── Sign In Button ───────────────────────────────────────────────────────────

@Composable
private fun NothingSignInButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        shape = RoundedCornerShape(3.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor         = NothingColors.NothingWhite,
            contentColor           = NothingColors.Void,
            disabledContainerColor = NothingColors.Surface1,
            disabledContentColor   = NothingColors.FaintWhite
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation  = 0.dp,
            pressedElevation  = 0.dp,
            disabledElevation = 0.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = NothingColors.DimWhite,
                strokeWidth = 1.5.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "AUTHENTICATING",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace,
                color = NothingColors.FaintWhite
            )
        } else {
            Text(
                text = "SIGN IN",
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 3.sp,
                fontFamily = FontFamily.Monospace,
                color = NothingColors.Void
            )
        }
    }
}

// ─── Sign Up Link ─────────────────────────────────────────────────────────────

@Composable
private fun NothingSignUpLink(onNavigateToSignUp: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "NO ACCOUNT?",
            color = NothingColors.FaintWhite,
            fontSize = 9.sp,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(4.dp))
        TextButton(
            onClick = onNavigateToSignUp,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Text(
                text = "REGISTER_",
                color = NothingColors.NothingWhite,
                fontSize = 9.sp,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black
            )
        }
    }
}