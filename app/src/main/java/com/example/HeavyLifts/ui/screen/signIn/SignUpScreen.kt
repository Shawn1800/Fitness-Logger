package com.example.demo103.ui.screen.signIn

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
            // Highlight red if email is invalid or already exists
            isError = hasError && (state.errorMessage?.contains("email", true) == true ||
                    state.errorMessage?.contains("registered", true) == true),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = state.password,
            onValueChange = { authViewModel.onEvent(AuthEvent.OnPasswordChange(it)) },
            label = { Text("Password") },
            // Highlight red if password syntax is wrong
            isError = hasError && state.errorMessage?.contains("password", true) == true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            supportingText = {
                Text("Min. 8 chars, 1 uppercase, 1 number")
            }
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
            modifier = Modifier.fillMaxWidth().height(50.dp),
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

        TextButton(onClick = NavToLogIn) {
            Text(text = "Already have an account? Log in here.")
        }
    }
}