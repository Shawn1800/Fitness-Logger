package com.example.demo103.ui.screen.signIn

import android.R.attr.onClick
import android.R.attr.text
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.util.TableInfo

@Composable
fun LogInScreen(
    authViewModel: AuthViewModel,
    NavToHome: () -> Unit,
    NavToSignIn: () -> Unit,
    ) {

    val state by authViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.uiEvent.collect { event ->
            when (event) {
                AuthUiEvent.NavToHome -> NavToHome()
                AuthUiEvent.NavToSignIn -> NavToSignIn()
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Login Page", fontSize = 32.sp)

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
            print("Login clicked ")
            authViewModel.onEvent(AuthEvent.LogIn)
        }) {
            Text(text = "Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            NavToSignIn()
        }) {
            Text(text = "Don't have an account?Signup.")
        }
    }

}