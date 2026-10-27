package com.simats.vitalsync.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF34D399),            // Bright Emerald Green Accent
    secondary = Color(0xFF065F46),          // Deep Mint Selected Pill
    tertiary = Color(0xFFA7F3D0),
    background = Color(0xFF0F172A),         // Slate Dark Background
    surface = Color(0xFF1E293B),            // Dark Slate Card Surface
    surfaceVariant = Color(0xFF2E1065),     // Deep Purple Hero Card Surface
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF8FAFC),       // Crisp White Primary Text
    onSurface = Color(0xFFF8FAFC),          // Crisp White Surface Text
    onSurfaceVariant = Color(0xFF94A3B8),   // Medium Slate Grey Secondary Text
    outlineVariant = Color(0xFF334155)      // Subtle Dark Card Border
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
    outlineVariant = VitalSoftBorder        // #E8EEEA
)

@Composable
fun VitalsyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
