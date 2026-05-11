package com.ghostbug.heavyliftsapp


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.launch
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope

import com.ghostbug.heavyliftsapp.ui.theme.Demo103Theme

import io.github.jan.supabase.auth.Auth

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import com.ghostbug.heavyLifts.BuildConfig

import com.ghostbug.heavyliftsapp.navigation.MainNavigation
import kotlinx.coroutines.launch


val supabase = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
) {
    install(Auth)
    install(Postgrest)

}


class MainActivity : ComponentActivity() {
    private var onResetPasswordLinkReceived: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        handleAuthLink(intent)

        setContent {
            Demo103Theme {
                MainNavigation()
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAuthLink(intent)
    }

    private fun handleAuthLink(intent: Intent?) {
        val data = intent?.data
        if (data != null && data.scheme == "heavylifts") {
            // This tells Supabase to process the session from the URL
            // and allows the user to update their password
            onResetPasswordLinkReceived?.invoke()
        }
    }
}



