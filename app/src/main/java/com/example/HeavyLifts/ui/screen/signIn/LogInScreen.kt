package com.example.demo103.ui.screen.signIn

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun LogInScreen(
    authViewModel: AuthViewModel,
    NavToHome: () -> Unit,
    NavToSignUp: () -> Unit,
) {
    val state by authViewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        authViewModel.uiEvent.collect { event ->
            when (event) {
                AuthUiEvent.NavToHome -> NavToHome()
                AuthUiEvent.NavToSignUp -> NavToSignUp()
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
        if (state.isWaitingForVerification) {
            VerificationUI(
                email = state.email,
                onCheckStatus = { authViewModel.onEvent(AuthEvent.CheckVerificationStatus) },
                onResendEmail = { authViewModel.onEvent(AuthEvent.ResendVerificationEmail) },
                onBackToLogin = { authViewModel.onEvent(AuthEvent.LogOut) },
                isLoading = state.isLoading,
                errorMessage = state.errorMessage
            )
        } else {
            Text(text = "Login", fontSize = 32.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(32.dp))

            val hasError = state.errorMessage != null

            OutlinedTextField(
                value = state.email,
                onValueChange = { authViewModel.onEvent(AuthEvent.OnEmailChange(it)) },
                label = { Text("Email") },
                isError = hasError && (state.errorMessage?.contains("email", true) == true ||
                        state.errorMessage?.contains("account", true) == true),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = { authViewModel.onEvent(AuthEvent.OnPasswordChange(it)) },
                label = { Text("Password") },
                isError = hasError && state.errorMessage?.contains("password", true) == true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            AnimatedVisibility(visible = hasError) {
                Text(
                    text = state.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { authViewModel.onEvent(AuthEvent.LogIn) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(text = "Login")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(" OR ", modifier = Modifier.padding(horizontal = 8.dp), color = Color.Gray, fontSize = 12.sp)
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = {
                    scope.launch {
                        try {
                            val credentialManager = CredentialManager.create(context)
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId("850847731559-1qitub7kfeahpm87e6o7j92fg24i6cqk.apps.googleusercontent.com") 
                                .build()

                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            val result = credentialManager.getCredential(
                                context = context as Activity,
                                request = request
                            )
                            
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                            val idToken = googleIdTokenCredential.idToken
                            authViewModel.onEvent(AuthEvent.GoogleSignIn(idToken))
                            
                        } catch (e: GetCredentialException) {
                            Toast.makeText(context, "Google Sign-In Error: ${e.message}", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Unexpected Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
            ) {
                Text("Sign in with Google")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = NavToSignUp) {
                Text(text = "Don't have an account? Sign up here.")
            }
        }
    }
}

@Composable
fun VerificationUI(
    email: String,
    onCheckStatus: () -> Unit,
    onResendEmail: () -> Unit,
    onBackToLogin: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Verify your email",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "We've sent a verification link to $email. Please click the link in your inbox to continue.",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = onCheckStatus,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("I've verified my email")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onResendEmail,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Resend email")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onBackToLogin) {
            Text("Back to Login")
        }
    }
}
