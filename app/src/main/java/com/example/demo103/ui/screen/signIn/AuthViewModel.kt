package com.example.demo103.ui.screen.signIn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demo103.data.repository.AuthRepository
import com.example.demo103.ui.screen.log_workout.LogWorkoutEvent
import com.example.demo103.ui.screen.signIn.AuthEvent.*
import com.google.android.gms.auth.api.Auth
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel (
          private val repository: AuthRepository= AuthRepository()
) : ViewModel(){

    private val _state = MutableStateFlow(AuthState())

    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AuthUiEvent>()

    val uiEvent : SharedFlow<AuthUiEvent> = _uiEvent.asSharedFlow()

    fun onEvent(event: AuthEvent) {
        when(event){
            is LogIn->{
                viewModelScope.launch {

                    _state.update { it.copy(isLoading = true, errorMessage = null) }

                    val result =repository.login(
                        _state.value.email,
                        _state.value.password
                    )

                    if  (result.isSuccess){
                        _state.update {
                            it.copy(isSuccess=true,
                                isLoading = false,
                                isLoggedIn = true,
                                )
                        }
                       _uiEvent.emit(AuthUiEvent.NavToHome)
                    }else{
                        _state.update {
                            it.copy(
                                errorMessage = result.exceptionOrNull()?.message
                            )
                        }
                    }
                }

            }

            is SignIn->{
                viewModelScope.launch {

                    _state.update { it.copy(isLoading = true, errorMessage = null) }

                    val result =repository.signin(
                        _state.value.email,
                        _state.value.password
                    )

                    if  (result.isSuccess){
                        _state.update {
                            it.copy(isSuccess=true,
                                isLoading = false,
                                isLoggedIn = true,
                            )
                        }
                        _uiEvent.emit(AuthUiEvent.NavToHome)
                    }else{
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.exceptionOrNull()?.message
                            )
                        }
                    }
                }

            }

            is LogOut ->{
                repository.logout()
                _state.value= AuthState()
            }

            is OnEmailChange->{
                _state.update { currentState->
                    currentState.copy(
                        email = event.email
                    )

                }
            }

            is OnPasswordChange->{
                _state.update { currentState->
                    currentState.copy(
                        password = event.password
                    )
                }
            }

        }
    }
}