package com.simats.vitalsync.ui.gym

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.vitalsync.ui.gym.viewmodel.GymViewModel
import com.simats.vitalsync.ui.gym.viewmodel.GymViewModelFactory
import com.simats.vitalsync.ui.theme.VitalsyncTheme

/**
 * GymModeScreen serves as the entry-point adapter for the dedicated 3-tab GymScreen.
 * It ensures full backward compatibility with HomeScreen navigation while delegating
 * to the redesigned Workouts, Nutrition, and Analytics tracker.
 */
@Composable
fun GymModeScreen(
    modifier: Modifier = Modifier,
    viewModel: GymViewModel = viewModel(factory = GymViewModelFactory()),
    onStartWorkoutSplitClick: () -> Unit = {},
    onOpenNutritionTrackerClick: () -> Unit = {},
    onOpenProgressAnalyticsClick: () -> Unit = {},
    onViewWorkoutHistoryClick: () -> Unit = {},
    onFinishGymSession: () -> Unit = {}
) {
    GymScreen(
        modifier = modifier,
        viewModel = viewModel,
        onStartWorkoutSplitClick = onStartWorkoutSplitClick,
        onOpenNutritionTrackerClick = onOpenNutritionTrackerClick,
        onOpenProgressAnalyticsClick = onOpenProgressAnalyticsClick,
        onViewWorkoutHistoryClick = onViewWorkoutHistoryClick,
        onFinishGymSession = onFinishGymSession
    )
}

@Preview(showBackground = true)
@Composable
fun GymModeScreenPreview() {
    VitalsyncTheme {
        GymModeScreen()
    }
}
