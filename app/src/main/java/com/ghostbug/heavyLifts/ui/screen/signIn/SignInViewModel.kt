import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostbug.heavyLifts.ui.screen.signIn.SignInEvent
import com.ghostbug.heavyLifts.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignInViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    val state: StateFlow<SignInState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SignInUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onEvent(event: SignInEvent) {
        when (event) {
            is SignInEvent.OnEmailChange -> {
                _state.update { it.copy(email = event.email) }
            }
            is SignInEvent.OnPasswordChange -> {
                _state.update { it.copy(password = event.password) }
            }
            is SignInEvent.OnSignInClick -> signIn()
            is SignInEvent.OnNavigateToSignUp -> {
                viewModelScope.launch {
                    _uiEvent.emit(SignInUiEvent.NavigateToSignUp)
                }
            }
        }
    }

    private fun signIn() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val success = authRepository.signIn(
                email = _state.value.email,
                password = _state.value.password
            )
            if (success) {
                _uiEvent.emit(SignInUiEvent.NavigateToHome)
            } else {
                _uiEvent.emit(SignInUiEvent.ShowError("Invalid email or password"))
            }
            _state.update { it.copy(isLoading = false) }
        }
    }
}

data class SignInState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false
)

sealed class SignInUiEvent {
    object NavigateToHome : SignInUiEvent()
    object NavigateToSignUp : SignInUiEvent()
    data class ShowError(val message: String) : SignInUiEvent()
}