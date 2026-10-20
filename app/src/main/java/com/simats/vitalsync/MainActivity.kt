package com.simats.vitalsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.simats.vitalsync.ui.home.HomeScreen
import com.simats.vitalsync.ui.onboarding.OnboardingScreen
import com.simats.vitalsync.ui.splash.SplashScreen
import com.simats.vitalsync.ui.theme.VitalsyncTheme

enum class ScreenState {
    SPLASH,
    ONBOARDING,
    MAIN
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VitalsyncTheme {
                var currentScreen by remember { mutableStateOf(ScreenState.SPLASH) }

                Crossfade(
                    targetState = currentScreen,
                    animationSpec = tween(durationMillis = 500),
                    label = "ScreenNavigationCrossfade"
                ) { screen ->
                    when (screen) {
                        ScreenState.SPLASH -> {
                            SplashScreen(
                                splashDurationMillis = 2800L,
                                onTimeout = { currentScreen = ScreenState.ONBOARDING }
                            )
                        }
                        ScreenState.ONBOARDING -> {
                            OnboardingScreen(
                                onFinishOnboarding = { currentScreen = ScreenState.MAIN }
                            )
                        }
                        ScreenState.MAIN -> {
                            HomeScreen()
                        }
                    }
                }
            }
        }
    }
}
