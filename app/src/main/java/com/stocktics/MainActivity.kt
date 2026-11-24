package com.stocktics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.stocktics.data.session.UserSessionManager
import com.stocktics.presentation.auth.AuthScreen
import com.stocktics.presentation.main.MainAppScreen
import com.stocktics.ui.theme.StockticsTheme
import com.stocktics.ui.theme.ThemeManager

import com.stocktics.presentation.splash.StockticsSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeManager.init(this)
        handleDeepLinkIntent(intent)
        setContent {
            val isDark = when (ThemeManager.currentTheme) {
                ThemeManager.ThemeMode.DARK -> true
                ThemeManager.ThemeMode.LIGHT -> false
                ThemeManager.ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            StockticsTheme(darkTheme = isDark) {
                StockticsApp()
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: android.content.Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "stocktics" && data.host == "indmoney-callback") {
            com.stocktics.data.indmoney.IndmoneySyncManager.handleCallbackSuccess(applicationContext)
        }
    }
}

@Composable
fun StockticsApp() {
    val context = LocalContext.current
    var showSplashScreen by remember { mutableStateOf(true) }
    
    // Auto-load persisted login session if user previously signed in
    var loggedInUserEmail by remember {
        mutableStateOf<String?>(UserSessionManager.getLoggedInUserEmail(context).takeIf { it.isNotBlank() })
    }

    Crossfade(
        targetState = showSplashScreen,
        animationSpec = tween(durationMillis = 350),
        label = "splashCrossfade"
    ) { isSplash ->
        if (isSplash) {
            StockticsSplashScreen(
                onAnimationFinished = { showSplashScreen = false },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Crossfade(
                targetState = loggedInUserEmail,
                animationSpec = tween(durationMillis = 250),
                label = "authCrossfade"
            ) { userEmail ->
                if (userEmail != null) {
                    MainAppScreen(
                        userEmail = userEmail,
                        onLogout = {
                            UserSessionManager.clearUserSession(context)
                            loggedInUserEmail = null
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AuthScreen(
                        onLoginSuccess = { email, userId, token ->
                            UserSessionManager.saveUserSession(context, email, userId, token)
                            loggedInUserEmail = email
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}