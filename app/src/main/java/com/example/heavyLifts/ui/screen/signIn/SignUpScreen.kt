package com.example.heavyLifts.ui.screen.signIn

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel,
    NavToHome: () -> Unit,
    NavToLogIn: () -> Unit,
) {
    val state by authViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.uiEvent.collect { event ->
            when (event) {
                AuthUiEvent.NavToHome -> NavToHome()
                AuthUiEvent.NavToLogIn -> NavToLogIn()
                else -> Unit
            }
        }
    }

    if (state.isWaitingForVerification) {
        EmailVerificationScreen(
            email = state.email,
            isLoading = state.isLoading,
            errorMessage = state.errorMessage,
            onCheckVerification = { authViewModel.onEvent(AuthEvent.CheckVerificationStatus) },
            onResendEmail = { authViewModel.onEvent(AuthEvent.ResendVerificationEmail) }
        )
    } else {
        SignUpFormScreen(
            state = state,
            authViewModel = authViewModel,
            onNavigateToLogIn = NavToLogIn
        )
    }
}

@Composable
private fun SignUpFormScreen(
    state: AuthState,
    authViewModel: AuthViewModel,
    onNavigateToLogIn: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Account",
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        val hasError = state.errorMessage != null

        // Email Field
        OutlinedTextField(
            value = state.email,
            onValueChange = { authViewModel.onEvent(AuthEvent.OnEmailChange(it)) },
            label = { Text("Email") },
            isError = hasError && (state.errorMessage.contains("email", true) ||
                    state.errorMessage.contains("registered", true)),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !state.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = state.password,
            onValueChange = { authViewModel.onEvent(AuthEvent.OnPasswordChange(it)) },
            label = { Text("Password") },
            isError = hasError && state.errorMessage.contains("password", true),
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            supportingText = {
                Text("Min. 8 chars, 1 uppercase, 1 number")
            },
            enabled = !state.isLoading
        )

        // Error Message Display
        AnimatedVisibility(visible = hasError) {
            Text(
                text = state.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sign Up Button
        Button(
            onClick = { authViewModel.onEvent(AuthEvent.SignUp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = "Sign Up")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogIn) {
            Text(text = "Already have an account? Log in here.")
        }
    }
}

@Composable
private fun EmailVerificationScreen(
    email: String,
    isLoading: Boolean,
    errorMessage: String?,
    onCheckVerification: () -> Unit,
    onResendEmail: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Verify Your Email",
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "We sent a verification link to:",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = email,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Click the verification link in your email and then check back here.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Helpful tips if email not received
        androidx.compose.material3.Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Didn't receive the email?",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Check your spam/junk folder\n" +
                            "• Wait a few minutes and refresh\n" +
                            "• Click 'Resend' below to get a new link",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show error message if verification failed
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Check Verification Button
        Button(
            onClick = onCheckVerification,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("I've Verified My Email")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resend Email Button
        TextButton(
            onClick = onResendEmail,
            enabled = !isLoading
        ) {
            Text("Didn't receive the email? Resend")
        }
    }
}

