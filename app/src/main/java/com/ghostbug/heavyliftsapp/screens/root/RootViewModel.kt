package com.ghostbug.heavyliftsapp.screens.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ghostbug.heavyliftsapp.data.repository.UserProfileRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RootViewModel(
    private val auth: Auth,
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    private val _gate = MutableStateFlow<AuthGate>(AuthGate.Loading)
    val gate: StateFlow<AuthGate> = _gate.asStateFlow()

    init {
        viewModelScope.launch {
            auth.sessionStatus.collectLatest { status ->
                _gate.value = when (status) {
                    is SessionStatus.Initializing -> AuthGate.Loading
                    is SessionStatus.Authenticated -> {
                        if (userProfileRepository.isProfileComplete()) AuthGate.Ready
                        else AuthGate.NeedsOnboarding
                    }
                    is SessionStatus.NotAuthenticated -> AuthGate.SignedOut
                    is SessionStatus.RefreshFailure -> AuthGate.SignedOut
                }
            }
        }
    }
}

class RootViewModelFactory(
    private val auth: Auth,
    private val userProfileRepository: UserProfileRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RootViewModel(auth, userProfileRepository) as T
    }
}
