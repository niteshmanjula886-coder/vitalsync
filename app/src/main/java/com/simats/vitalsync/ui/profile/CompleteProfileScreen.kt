package com.simats.vitalsync.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * CompleteProfileScreen alias wrapping ProfileSetupScreen.
 * Provides the "Complete Your Profile" onboarding workflow with
 * reliable Material 3 ModalBottomSheet selectors for Gender and Daily Activity Level.
 */
@Composable
fun CompleteProfileScreen(
    modifier: Modifier = Modifier,
    initialName: String = "",
    initialAge: String = "",
    initialGender: String = "",
    initialHeight: String = "",
    initialWeight: String = "",
    initialActivityLevel: String = "",
    onBackClick: () -> Unit = {},
    onSelectionChanged: (gender: String, activityLevel: String) -> Unit = { _, _ -> },
    onCompleteProfileDetails: (gender: String, activityLevel: String, primaryGoal: String) -> Unit = { _, _, _ -> },
    onSaveAndContinue: (name: String, age: String, height: String, weight: String) -> Unit = { _, _, _, _ -> }
) {
    ProfileSetupScreen(
        modifier = modifier,
        initialName = initialName,
        initialAge = initialAge,
        initialGender = initialGender,
        initialHeight = initialHeight,
        initialWeight = initialWeight,
        initialActivityLevel = initialActivityLevel,
        onBackClick = onBackClick,
        onSelectionChanged = onSelectionChanged,
        onCompleteProfileDetails = onCompleteProfileDetails,
        onSaveAndContinue = onSaveAndContinue
    )
}

