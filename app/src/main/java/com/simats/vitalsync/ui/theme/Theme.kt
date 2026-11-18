package com.simats.vitalsync.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CarbonDarkAccent,             // #10A37F (ChatGPT Signature Emerald Green)
    secondary = Color(0xFF1B382B),          // Subtle Emerald Charcoal Container (crisp contrast for #10A37F)
    tertiary = Color(0xFF34D399),
    background = CarbonDarkBackground,      // #0F0F0F (YouTube/ChatGPT True Carbon Black)
    surface = CarbonDarkSurface,            // #1E1E1E (ChatGPT Elevated Dark Gray)
    surfaceVariant = CarbonDarkSurfaceVariant, // #2A2A2A (Neutral Charcoal Gray)
    onPrimary = Color.White,
    onSecondary = CarbonDarkAccent,
    onBackground = CarbonDarkTextPrimary,   // #F1F1F1 (Crisp Off-White)
    onSurface = CarbonDarkTextPrimary,      // #F1F1F1
    onSurfaceVariant = CarbonDarkTextSecondary, // #AAAAAA (YouTube Neutral Muted Gray)
    outline = CarbonDarkBorder,             // #333333 (Subtle 1.dp matte gray border)
    outlineVariant = CarbonDarkBorder       // #333333
)

private val LightColorScheme = lightColorScheme(
    primary = VitalPrimaryMint,             // #00A884
    secondary = VitalNavSelectedBg,          // #DDF7EC
    tertiary = VitalTextFooterTitle,
    background = VitalMainBackground,        // #F7FBF8
    surface = VitalCardBg,                  // #FFFFFF
    surfaceVariant = VitalWellnessCardBg,    // #F3E8FF (Purple)
    onPrimary = Color.White,
    onSecondary = VitalPrimaryDark,
    onBackground = VitalTextPrimary,        // #14202D
    onSurface = VitalTextPrimary,           // #14202D
    onSurfaceVariant = VitalTextSecondary,  // #7B858D
    outline = VitalSoftBorder,
    outlineVariant = VitalSoftBorder        // #E8EEEA
)

@Composable
fun VitalsyncTheme(
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    darkTheme: Boolean = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        else -> isSystemInDarkTheme()
    },
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
                if (darkTheme) {
                    window.statusBarColor = android.graphics.Color.parseColor("#0F0F0F")
                    window.navigationBarColor = android.graphics.Color.parseColor("#0F0F0F")
                } else {
                    window.statusBarColor = android.graphics.Color.parseColor("#F7FBF8")
                    window.navigationBarColor = android.graphics.Color.parseColor("#FFFFFF")
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
