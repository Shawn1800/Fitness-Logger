package com.ghostbug.heavyliftsapp.screens.signIn

import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghostbug.heavyLifts.BuildConfig
import com.ghostbug.heavyliftsapp.ui.theme.HeavyLiftsColors
import com.ghostbug.heavyliftsapp.ui.theme.HeavyLiftsType
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    state: SignInState,
    onEvent: (SignInEvent) -> Unit,
    uiEvent: SharedFlow<SignInUiEvent>,
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToSignUp: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        uiEvent.collect { event ->
            when (event) {
                is SignInUiEvent.NavigateToHome       -> onNavigateToHome()
                is SignInUiEvent.NavigateToOnboarding -> onNavigateToOnboarding()
                SignInUiEvent.NavigateToSignUp        -> onNavigateToSignUp()
                is SignInUiEvent.ShowError ->
                    coroutineScope.launch { snackbarHostState.showSnackbar(event.message) }
                is SignInUiEvent.ShowSuccess ->
                    coroutineScope.launch { snackbarHostState.showSnackbar(event.message) }
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
            Spacer(Modifier.height(72.dp))
            BrandMark()

            Spacer(Modifier.height(28.dp))
            Text(
                text = "HeavyLifts",
                color = HeavyLiftsColors.Fg1,
                fontFamily = HeavyLiftsType.Display,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Welcome back. Ready to lift?",
                color = HeavyLiftsColors.Fg3,
                fontFamily = HeavyLiftsType.Body,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(56.dp))

            GoogleSignInPill(
                isLoading = state.isLoading,
                onTokenReceived = { token, nonce ->
                    onEvent(SignInEvent.OnGoogleSignInResult(token, nonce))
                },
                onError = { msg ->
                    coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                }
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New here?",
                    color = HeavyLiftsColors.Fg3,
                    fontFamily = HeavyLiftsType.Body,
                    fontSize = 13.sp
                )
                TextButton(onClick = onNavigateToSignUp) {
                    Text(
                        text = "Create an account",
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

// ─── Brand mark — circular dumbbell badge ──────────────────────────────────

@Composable
private fun BrandMark() {
    Box(
        Modifier
            .size(80.dp)
            .drawBehind {
                drawRoundRect(
                    color = Color(0x38000000),
                    topLeft = Offset(0f, 6.dp.toPx()),
                    cornerRadius = CornerRadius(80.dp.toPx())
                )
            }
            .background(HeavyLiftsColors.Accent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "HL",
            color = HeavyLiftsColors.Fg1,
            fontFamily = HeavyLiftsType.Display,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─── Google sign-in (orange pill) ──────────────────────────────────────────

@Composable
private fun GoogleSignInPill(
    isLoading: Boolean,
    onTokenReceived: (String, String) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            val rawNonce = java.util.UUID.randomUUID().toString()
            val hashedNonce = hashNonce(rawNonce)
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            scope.launch {
                try {
                    val result = credentialManager.getCredential(context = context, request = request)
                    val credential = result.credential
                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        onTokenReceived(googleIdTokenCredential.idToken, rawNonce)
                    }
                } catch (e: GetCredentialException) {
                    onError(e.message ?: "Sign-in didn't go through. Try again?")
                } catch (e: Exception) {
                    onError(e.message ?: "Something went wrong. Try again?")
                }
            }
        },
        enabled = !isLoading,
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = HeavyLiftsColors.Accent,
            contentColor = HeavyLiftsColors.Fg1,
            disabledContainerColor = HeavyLiftsColors.BgChip,
            disabledContentColor = HeavyLiftsColors.Fg3
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
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
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Signing in…",
                fontFamily = HeavyLiftsType.Display,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Text(
                text = "Continue with Google",
                fontFamily = HeavyLiftsType.Display,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun hashNonce(nonce: String): String {
    val md = java.security.MessageDigest.getInstance("SHA-256")
    val digest = md.digest(nonce.toByteArray())
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}
