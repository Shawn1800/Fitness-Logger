package com.ghostbug.heavyliftsapp.screens.root

sealed interface AuthGate {
    data object Loading : AuthGate
    data object SignedOut : AuthGate
    data object NeedsOnboarding : AuthGate
    data object Ready : AuthGate
}
