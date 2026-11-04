package com.simats.vitalsync.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Standardized High-Contrast Dynamic Sleep Quality & Stage Color Matrix
 */
object SleepColorStandards {

    // 1. Sleep Quality & Duration Threshold Colors
    val OptimalGreen = Color(0xFF4CAF50)          // >= 7.5 hrs / Quality >= 85
    val OptimalGreenBg = Color(0xFFE8F5E9)        // Background Container Tint
    val OptimalGreenText = Color(0xFF2E7D32)      // High-Contrast Badge Text

    val ModerateAmber = Color(0xFFFFC107)         // 6.0 – 7.4 hrs / Quality 70–84
    val ModerateAmberBg = Color(0xFFFFF8E1)       // Background Container Tint
    val ModerateAmberText = Color(0xFFB78103)     // High-Contrast Badge Text

    val DeficitRed = Color(0xFFFF5252)            // < 6.0 hrs / Quality < 70
    val DeficitRedBg = Color(0xFFFFEBEE)          // Background Container Tint
    val DeficitRedText = Color(0xFFC62828)        // High-Contrast Badge Text

    val ExcessivePurple = Color(0xFF7E57C2)       // > 9.5 hrs
    val ExcessivePurpleBg = Color(0xFFEDE7F6)     // Background Container Tint
    val ExcessivePurpleText = Color(0xFF4527A0)   // High-Contrast Badge Text

    // 2. Sleep Stages Palette Map
    val StageDeep = Color(0xFF3F51B5)             // Deep Sleep (Non-REM 3/4)
    val StageLight = Color(0xFF03A9F4)            // Light Sleep (Non-REM 1/2)
    val StageRem = Color(0xFF7E57C2)              // REM Sleep
    val StageAwake = Color(0xFFFF7043)            // Awake / Disruptions

    data class QualityPresentation(
        val status: String,
        val qualityLabel: String,
        val primaryColor: Color,
        val containerColor: Color,
        val badgeTextColor: Color,
        val medicalMeaning: String
    )

    fun getPresentation(durationMinutes: Int, qualityScore: Int? = null): QualityPresentation {
        val hours = durationMinutes / 60.0
        return when {
            hours > 9.5 -> QualityPresentation(
                status = "Excessive",
                qualityLabel = "Long Sleep",
                primaryColor = ExcessivePurple,
                containerColor = ExcessivePurpleBg,
                badgeTextColor = ExcessivePurpleText,
                medicalMeaning = "Hypersomnia / recovery rebound"
            )
            (hours >= 7.5 && hours <= 9.5) || (qualityScore != null && qualityScore >= 85) -> QualityPresentation(
                status = "Optimal",
                qualityLabel = "Restorative",
                primaryColor = OptimalGreen,
                containerColor = OptimalGreenBg,
                badgeTextColor = OptimalGreenText,
                medicalMeaning = "Optimal recovery, restorative sleep"
            )
            (hours >= 6.0 && hours < 7.5) || (qualityScore != null && qualityScore in 70..84) -> QualityPresentation(
                status = "Moderate",
                qualityLabel = "Fair",
                primaryColor = ModerateAmber,
                containerColor = ModerateAmberBg,
                badgeTextColor = ModerateAmberText,
                medicalMeaning = "Mild deficit, acceptable baseline"
            )
            else -> QualityPresentation(
                status = "Deficit",
                qualityLabel = "Sleep Debt",
                primaryColor = DeficitRed,
                containerColor = DeficitRedBg,
                badgeTextColor = DeficitRedText,
                medicalMeaning = "Critical sleep debt, elevated cortisol"
            )
        }
    }
}

