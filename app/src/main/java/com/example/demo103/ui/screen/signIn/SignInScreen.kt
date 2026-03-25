package com.example.demo103.ui.screen.signIn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignInScreen(
    authViewModel: AuthViewModel,
    NavToHome: () -> Unit,
    NavToLogIn: () -> Unit,
) {

    val state by authViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.uiEvent.collect { event ->
            when (event) {
                AuthUiEvent.NavToHome ->
                        NavToHome()
                AuthUiEvent.NavToSignIn -> NavToLogIn()
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Signin Page", fontSize = 32.sp)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = {
                authViewModel.onEvent(AuthEvent.OnEmailChange(it))
            },
            label = {
                Text(text = "Email")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = {
                authViewModel.onEvent((AuthEvent.OnPasswordChange(it)))
            },
            label = {
                Text(text = "Password")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            authViewModel.onEvent(AuthEvent.SignIn)
        }) {
            Text(text = "Signin")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            NavToLogIn()
        }) {
            Text(text = "Already have account? Login")
        }
    }

}