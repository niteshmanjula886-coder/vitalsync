package com.simats.vitalsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.simats.vitalsync.data.Exercise
import com.simats.vitalsync.data.FitnessRepository
import com.simats.vitalsync.data.MealCategory
import com.simats.vitalsync.data.WorkoutSplit
import com.simats.vitalsync.ui.activity.ActivityType
import com.simats.vitalsync.ui.activity.CyclingTrackingScreen
import com.simats.vitalsync.ui.activity.RunningTrackingScreen
import com.simats.vitalsync.ui.activity.StartActivityScreen
import com.simats.vitalsync.ui.activity.ViewHistoryScreen
import com.simats.vitalsync.ui.activity.WalkingTrackingScreen
import com.simats.vitalsync.ui.activity.WorkoutTrackingScreen
import com.simats.vitalsync.ui.ai.AiChatHistoryScreen
import com.simats.vitalsync.ui.ai.AiChatScreen
import com.simats.vitalsync.ui.ai.WeeklyHealthReportScreen
import com.simats.vitalsync.ui.auth.ForgotPasswordScreen
import com.simats.vitalsync.ui.auth.LoginScreen
import com.simats.vitalsync.ui.auth.SignUpScreen
import com.simats.vitalsync.ui.gym.ActiveWorkoutSessionScreen
import com.simats.vitalsync.ui.gym.AddCustomExerciseScreen
import com.simats.vitalsync.ui.gym.ExerciseLibraryScreen
import com.simats.vitalsync.ui.gym.FitnessProgressDashboardScreen
import com.simats.vitalsync.ui.gym.WorkoutHistoryScreen
import com.simats.vitalsync.ui.gym.WorkoutSplitSelectionScreen
import com.simats.vitalsync.ui.home.HomeScreen
import com.simats.vitalsync.ui.nutrition.AddCustomFoodScreen
import com.simats.vitalsync.ui.nutrition.DailyMealHistoryScreen
import com.simats.vitalsync.ui.nutrition.FoodDetailsScreen
import com.simats.vitalsync.ui.nutrition.FoodSearchScreen
import com.simats.vitalsync.ui.nutrition.NutritionDashboardScreen
import com.simats.vitalsync.ui.nutrition.NutritionSettingsScreen
import com.simats.vitalsync.ui.onboarding.OnboardingScreen
import com.simats.vitalsync.ui.profile.AboutVitalSyncScreen
import com.simats.vitalsync.ui.profile.AddReminderScreen
import com.simats.vitalsync.ui.profile.ChangePasswordScreen
import com.simats.vitalsync.ui.profile.ConnectedDevicesScreen
import com.simats.vitalsync.ui.profile.DataManagementScreen
import com.simats.vitalsync.ui.profile.HealthGoalsScreen
import com.simats.vitalsync.ui.profile.HealthPermissionsScreen
import com.simats.vitalsync.ui.profile.HelpSupportScreen
import com.simats.vitalsync.ui.profile.LoginSessionsScreen
import com.simats.vitalsync.ui.profile.NotificationsScreen
import com.simats.vitalsync.ui.profile.PersonalInformationScreen
import com.simats.vitalsync.ui.profile.PrivacyPolicyScreen
import com.simats.vitalsync.ui.profile.PrivacySecurityScreen
import com.simats.vitalsync.ui.profile.ProfileSetupScreen
import com.simats.vitalsync.ui.profile.RemindersScreen
import com.simats.vitalsync.ui.profile.SettingsScreen
import com.simats.vitalsync.ui.profile.VersionScreen
import com.simats.vitalsync.ui.splash.SplashScreen
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import com.simats.vitalsync.ui.vitals.ActivityDetailScreen
import com.simats.vitalsync.ui.vitals.AddReadingScreen
import com.simats.vitalsync.ui.vitals.BloodPressureDetailScreen
import com.simats.vitalsync.ui.vitals.BmiDetailScreen
import com.simats.vitalsync.ui.vitals.BmiResultScreen
import com.simats.vitalsync.ui.vitals.EditStepGoalScreen
import com.simats.vitalsync.ui.vitals.HeartRateDetailScreen
import com.simats.vitalsync.ui.vitals.HydrationDetailScreen
import com.simats.vitalsync.ui.vitals.ReadingReviewScreen
import com.simats.vitalsync.ui.vitals.SleepDetailScreen
import com.simats.vitalsync.ui.vitals.Spo2DetailScreen
import com.simats.vitalsync.ui.vitals.StepCountDetailScreen
import com.simats.vitalsync.ui.vitals.UpdateMetricsScreen
import com.simats.vitalsync.ui.vitals.VitalReadingType
import com.simats.vitalsync.ui.vitals.VitalSignsScreen
import java.util.Locale

enum class ScreenState {
    SPLASH,
    ONBOARDING,
    LOGIN,
    SIGNUP,
    FORGOT_PASSWORD,
    PROFILE_SETUP,
    MAIN,
    AI_CHAT,
    AI_CHAT_HISTORY,
    WEEKLY_REPORT,
    REMINDERS,
    ADD_REMINDER,
    VITAL_SIGNS,
    HEART_RATE_DETAIL,
    READING_REVIEW,
    ACTIVITY_DETAIL,
    START_ACTIVITY,
    WALKING_TRACKING,
    RUNNING_TRACKING,
    CYCLING_TRACKING,
    WORKOUT_TRACKING,
    VIEW_HISTORY,
    SLEEP_DETAIL,
    HYDRATION_DETAIL,
    BLOOD_PRESSURE_DETAIL,
    SPO2_DETAIL,
    STEP_COUNT_DETAIL,
    EDIT_STEP_GOAL,
    BMI_DETAIL,
    UPDATE_METRICS,
    BMI_RESULT,
    PERSONAL_INFORMATION,
    HEALTH_GOALS,
    CONNECTED_DEVICES,
    NOTIFICATIONS,
    PRIVACY_SECURITY,
    LOGIN_SESSIONS,
    PRIVACY_POLICY,
    SETTINGS,
    CHANGE_PASSWORD,
    HEALTH_PERMISSIONS,
    DATA_MANAGEMENT,
    ABOUT_VITALSINC,
    VERSION,
    HELP_SUPPORT,
    ADD_READING,
    GYM_SPLIT_SELECT,
    EXERCISE_LIBRARY,
    CREATE_CUSTOM_EXERCISE,
    ACTIVE_WORKOUT,
    WORKOUT_HISTORY,
    NUTRITION_DASHBOARD,
    FOOD_SEARCH,
    CREATE_CUSTOM_FOOD,
    FOOD_DETAILS,
    DAILY_MEAL_HISTORY,
    NUTRITION_SETTINGS,
    FITNESS_PROGRESS
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = FitnessRepository.getInstance(applicationContext)

        setContent {
            VitalsyncTheme {
                var currentScreen by remember { mutableStateOf(ScreenState.SPLASH) }
                var isNewUser by remember { mutableStateOf(false) }
                var activeReadingType by remember { mutableStateOf(VitalReadingType.HEART_RATE) }
                var homeInitialTab by remember { mutableIntStateOf(0) }
                var viewHistoryPreviousScreen by remember { mutableStateOf(ScreenState.ACTIVITY_DETAIL) }

                var activeWorkoutSplit by remember { mutableStateOf(WorkoutSplit.PUSH) }
                var selectedMealCategory by remember { mutableStateOf(MealCategory.BREAKFAST) }
                var activeFoodItem by remember { mutableStateOf(repository.initialIndianFoodDatabase[0]) }
                var activeWorkoutExercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }

                var calculatedBmiValue by remember { mutableStateOf("22.4") }
                var calculatedBmiCategory by remember { mutableStateOf("Healthy Range") }
                var userHeightCm by remember { mutableStateOf("175") }
                var userWeightKg by remember { mutableStateOf("68.5") }

                var globalUserName by remember { mutableStateOf("Alex Morgan") }
                var globalUserInitials by remember { mutableStateOf("AM") }
                var globalUserAge by remember { mutableStateOf("28") }
                var globalProfilePhotoBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

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
                                onSkipClick = {
                                    homeInitialTab = 0
                                    currentScreen = ScreenState.MAIN
                                },
                                onFinishOnboarding = { currentScreen = ScreenState.LOGIN }
                            )
                        }
                        ScreenState.LOGIN -> {
                            LoginScreen(
                                onLoginSuccess = {
                                    isNewUser = false
                                    homeInitialTab = 0
                                    currentScreen = ScreenState.MAIN
                                },
                                onNavigateToSignUp = { currentScreen = ScreenState.SIGNUP },
                                onForgotPasswordClick = { currentScreen = ScreenState.FORGOT_PASSWORD }
                            )
                        }
                        ScreenState.SIGNUP -> {
                            SignUpScreen(
                                onBackClick = { currentScreen = ScreenState.LOGIN },
                                onSignUpSuccess = {
                                    isNewUser = true
                                    currentScreen = ScreenState.PROFILE_SETUP
                                },
                                onNavigateToLogin = { currentScreen = ScreenState.LOGIN }
                            )
                        }
                        ScreenState.FORGOT_PASSWORD -> {
                            ForgotPasswordScreen(
                                onBackClick = { currentScreen = ScreenState.LOGIN },
                                onResetLinkSent = { }
                            )
                        }
                        ScreenState.PROFILE_SETUP -> {
                            ProfileSetupScreen(
                                onBackClick = { currentScreen = ScreenState.LOGIN },
                                onSaveAndContinue = {
                                    homeInitialTab = 0
                                    currentScreen = ScreenState.MAIN
                                }
                            )
                        }
                        ScreenState.MAIN -> {
                            HomeScreen(
                                initialTab = homeInitialTab,
                                userName = globalUserName,
                                userInitials = globalUserInitials,
                                userAge = globalUserAge,
                                userHeight = userHeightCm,
                                userWeight = userWeightKg,
                                userPhotoBitmap = globalProfilePhotoBitmap,
                                onVitalsClick = { currentScreen = ScreenState.VITAL_SIGNS },
                                onHeartRateClick = { currentScreen = ScreenState.HEART_RATE_DETAIL },
                                onBloodPressureClick = { currentScreen = ScreenState.BLOOD_PRESSURE_DETAIL },
                                onSpO2Click = { currentScreen = ScreenState.SPO2_DETAIL },
                                onStepsClick = { currentScreen = ScreenState.STEP_COUNT_DETAIL },
                                onActivityClick = { currentScreen = ScreenState.ACTIVITY_DETAIL },
                                onSleepClick = { currentScreen = ScreenState.SLEEP_DETAIL },
                                onHydrationClick = { currentScreen = ScreenState.HYDRATION_DETAIL },
                                onPersonalInformationClick = { currentScreen = ScreenState.PERSONAL_INFORMATION },
                                onHealthGoalsClick = { currentScreen = ScreenState.HEALTH_GOALS },
                                onConnectedDevicesClick = { currentScreen = ScreenState.CONNECTED_DEVICES },
                                onNotificationsClick = { currentScreen = ScreenState.NOTIFICATIONS },
                                onPrivacySecurityClick = { currentScreen = ScreenState.PRIVACY_SECURITY },
                                onSettingsClick = { currentScreen = ScreenState.SETTINGS },
                                onHelpSupportClick = { currentScreen = ScreenState.HELP_SUPPORT },
                                onAskAiClick = { currentScreen = ScreenState.AI_CHAT },
                                onStartWorkoutSplitClick = { currentScreen = ScreenState.GYM_SPLIT_SELECT },
                                onOpenNutritionTrackerClick = { currentScreen = ScreenState.NUTRITION_DASHBOARD },
                                onOpenProgressAnalyticsClick = { currentScreen = ScreenState.FITNESS_PROGRESS },
                                onViewWorkoutHistoryClick = { currentScreen = ScreenState.WORKOUT_HISTORY },
                                onLogoutClick = { currentScreen = ScreenState.LOGIN }
                            )
                        }
                        ScreenState.GYM_SPLIT_SELECT -> {
                            WorkoutSplitSelectionScreen(
                                onBackClick = {
                                    homeInitialTab = 4
                                    currentScreen = ScreenState.MAIN
                                },
                                onStartSplitWorkout = { split ->
                                    activeWorkoutSplit = split
                                    activeWorkoutExercises = emptyList()
                                    currentScreen = ScreenState.ACTIVE_WORKOUT
                                }
                            )
                        }
                        ScreenState.EXERCISE_LIBRARY -> {
                            ExerciseLibraryScreen(
                                exerciseList = repository.getExerciseLibrary(),
                                onBackClick = { currentScreen = ScreenState.ACTIVE_WORKOUT },
                                onExerciseSelected = { ex ->
                                    activeWorkoutExercises = activeWorkoutExercises + ex
                                    currentScreen = ScreenState.ACTIVE_WORKOUT
                                },
                                onCreateCustomExerciseClick = { currentScreen = ScreenState.CREATE_CUSTOM_EXERCISE }
                            )
                        }
                        ScreenState.CREATE_CUSTOM_EXERCISE -> {
                            AddCustomExerciseScreen(
                                onBackClick = { currentScreen = ScreenState.EXERCISE_LIBRARY },
                                onSaveCustomExerciseSuccess = { customEx ->
                                    repository.addCustomExercise(customEx)
                                    activeWorkoutExercises = activeWorkoutExercises + customEx
                                    currentScreen = ScreenState.ACTIVE_WORKOUT
                                }
                            )
                        }
                        ScreenState.ACTIVE_WORKOUT -> {
                            ActiveWorkoutSessionScreen(
                                split = activeWorkoutSplit,
                                initialExercises = activeWorkoutExercises,
                                onBackClick = {
                                    homeInitialTab = 4
                                    currentScreen = ScreenState.MAIN
                                },
                                onAddExerciseClick = { currentScreen = ScreenState.EXERCISE_LIBRARY },
                                onFinishSession = { session ->
                                    repository.saveWorkoutSession(session)
                                    homeInitialTab = 4
                                    currentScreen = ScreenState.MAIN
                                }
                            )
                        }
                        ScreenState.WORKOUT_HISTORY -> {
                            WorkoutHistoryScreen(
                                workoutSessions = repository.getAllWorkoutSessions(),
                                onBackClick = {
                                    homeInitialTab = 4
                                    currentScreen = ScreenState.MAIN
                                },
                                onReuseTemplate = { session ->
                                    activeWorkoutSplit = session.split
                                    activeWorkoutExercises = session.exercises.map { it.exercise }
                                    currentScreen = ScreenState.ACTIVE_WORKOUT
                                }
                            )
                        }
                        ScreenState.NUTRITION_DASHBOARD -> {
                            val today = repository.getTodayDateString()
                            NutritionDashboardScreen(
                                dateString = "Today · $today",
                                goal = repository.getNutritionGoal(),
                                loggedMeals = repository.getMealEntriesForDate(today),
                                onBackClick = {
                                    homeInitialTab = 4
                                    currentScreen = ScreenState.MAIN
                                },
                                onAddMealClick = { category ->
                                    selectedMealCategory = category
                                    currentScreen = ScreenState.FOOD_SEARCH
                                },
                                onViewMealHistoryClick = { currentScreen = ScreenState.DAILY_MEAL_HISTORY },
                                onNutritionSettingsClick = { currentScreen = ScreenState.NUTRITION_SETTINGS }
                            )
                        }
                        ScreenState.FOOD_SEARCH -> {
                            FoodSearchScreen(
                                mealCategory = selectedMealCategory,
                                foodDatabase = repository.getFoodDatabase(),
                                onBackClick = { currentScreen = ScreenState.NUTRITION_DASHBOARD },
                                onFoodSelected = { food ->
                                    activeFoodItem = food
                                    currentScreen = ScreenState.FOOD_DETAILS
                                },
                                onCreateCustomFoodClick = { currentScreen = ScreenState.CREATE_CUSTOM_FOOD }
                            )
                        }
                        ScreenState.CREATE_CUSTOM_FOOD -> {
                            AddCustomFoodScreen(
                                onBackClick = { currentScreen = ScreenState.FOOD_SEARCH },
                                onSaveCustomFoodSuccess = { newFood ->
                                    repository.addCustomFoodItem(newFood)
                                    activeFoodItem = newFood
                                    currentScreen = ScreenState.FOOD_DETAILS
                                }
                            )
                        }
                        ScreenState.FOOD_DETAILS -> {
                            val today = repository.getTodayDateString()
                            FoodDetailsScreen(
                                foodItem = activeFoodItem,
                                initialMealCategory = selectedMealCategory,
                                dateString = today,
                                onBackClick = { currentScreen = ScreenState.FOOD_SEARCH },
                                onLogMealSuccess = { mealEntry ->
                                    repository.saveMealEntry(mealEntry)
                                    currentScreen = ScreenState.NUTRITION_DASHBOARD
                                }
                            )
                        }
                        ScreenState.DAILY_MEAL_HISTORY -> {
                            val today = repository.getTodayDateString()
                            DailyMealHistoryScreen(
                                dateString = "Today · $today",
                                loggedMeals = repository.getMealEntriesForDate(today),
                                onBackClick = { currentScreen = ScreenState.NUTRITION_DASHBOARD },
                                onDeleteMealEntry = { id ->
                                    repository.deleteMealEntry(id, today)
                                    currentScreen = ScreenState.NUTRITION_DASHBOARD
                                }
                            )
                        }
                        ScreenState.NUTRITION_SETTINGS -> {
                            NutritionSettingsScreen(
                                initialGoal = repository.getNutritionGoal(),
                                onBackClick = { currentScreen = ScreenState.NUTRITION_DASHBOARD },
                                onSaveGoalsSuccess = { updatedGoal ->
                                    repository.saveNutritionGoal(updatedGoal)
                                    currentScreen = ScreenState.NUTRITION_DASHBOARD
                                }
                            )
                        }
                        ScreenState.FITNESS_PROGRESS -> {
                            FitnessProgressDashboardScreen(
                                onBackClick = {
                                    homeInitialTab = 4
                                    currentScreen = ScreenState.MAIN
                                }
                            )
                        }
                        ScreenState.AI_CHAT -> {
                            AiChatScreen(
                                onBackClick = {
                                    homeInitialTab = 2
                                    currentScreen = ScreenState.MAIN
                                },
                                onChatHistoryClick = { currentScreen = ScreenState.AI_CHAT_HISTORY }
                            )
                        }
                        ScreenState.AI_CHAT_HISTORY -> {
                            AiChatHistoryScreen(
                                onBackClick = { currentScreen = ScreenState.AI_CHAT },
                                onSelectConversation = { currentScreen = ScreenState.AI_CHAT },
                                onNewChatClick = { currentScreen = ScreenState.AI_CHAT }
                            )
                        }
                        ScreenState.WEEKLY_REPORT -> {
                            WeeklyHealthReportScreen(
                                onBackClick = {
                                    homeInitialTab = 2
                                    currentScreen = ScreenState.MAIN
                                }
                            )
                        }
                        ScreenState.REMINDERS -> {
                            RemindersScreen(
                                onBackClick = {
                                    homeInitialTab = 3
                                    currentScreen = ScreenState.MAIN
                                },
                                onAddReminderClick = { currentScreen = ScreenState.ADD_REMINDER }
                            )
                        }
                        ScreenState.ADD_REMINDER -> {
                            AddReminderScreen(
                                onBackClick = { currentScreen = ScreenState.REMINDERS },
                                onSaveReminderSuccess = { currentScreen = ScreenState.REMINDERS }
                            )
                        }
                        ScreenState.PERSONAL_INFORMATION -> {
                            PersonalInformationScreen(
                                currentName = globalUserName,
                                currentInitials = globalUserInitials,
                                currentHeight = userHeightCm,
                                currentWeight = userWeightKg,
                                currentPhotoBitmap = globalProfilePhotoBitmap,
                                onBackClick = {
                                    homeInitialTab = 3
                                    currentScreen = ScreenState.MAIN
                                },
                                onSaveChangesClick = { newName, newInitials, newAge, newHeight, newWeight, newPhotoBitmap ->
                                    globalUserName = newName
                                    globalUserInitials = newInitials
                                    globalUserAge = newAge
                                    userHeightCm = newHeight
                                    userWeightKg = newWeight
                                    globalProfilePhotoBitmap = newPhotoBitmap

                                    val h = newHeight.toFloatOrNull() ?: 175f
                                    val w = newWeight.toFloatOrNull() ?: 68.5f
                                    val bmiVal = if (h > 0) w / ((h / 100f) * (h / 100f)) else 22.4f
                                    calculatedBmiValue = String.format(Locale.US, "%.1f", bmiVal)
                                    calculatedBmiCategory = when {
                                        bmiVal < 18.5f -> "Underweight"
                                        bmiVal < 25.0f -> "Healthy Range"
                                        bmiVal < 30.0f -> "Overweight"
                                        else -> "Obese"
                                    }

                                    homeInitialTab = 3
                                    currentScreen = ScreenState.MAIN
                                }
                            )
                        }
                        ScreenState.HEALTH_GOALS -> {
                            HealthGoalsScreen(
                                onBackClick = {
                                    homeInitialTab = 3
                                    currentScreen = ScreenState.MAIN
                                },
                                onSaveGoalClick = {
                                    homeInitialTab = 3
                                    currentScreen = ScreenState.MAIN
                                }
                            )
                        }
                        ScreenState.CONNECTED_DEVICES -> {
                            ConnectedDevicesScreen(
                                onBackClick = {
                                    homeInitialTab = 3
                                    currentScreen = ScreenState.MAIN
                                }
                            )
                        }
                        ScreenState.NOTIFICATIONS -> {
                            NotificationsScreen(
                                onBackClick = {
                                    homeInitialTab = 3
                                    currentScreen = ScreenState.MAIN
                                }
                            )
                        }
                        ScreenState.PRIVACY_SECURITY -> {
                            PrivacySecurityScreen(
                                onBackClick = {
                                    homeInitialTab = 3
                                    currentScreen = ScreenState.MAIN
                                },
                                onLoginSessionsClick = { currentScreen = ScreenState.LOGIN_SESSIONS },
                                onPrivacyPolicyClick = { currentScreen = ScreenState.PRIVACY_POLICY }
                            )
                        }
                        ScreenState.LOGIN_SESSIONS -> {
                            LoginSessionsScreen(
                                onBackClick = { currentScreen = ScreenState.PRIVACY_SECURITY }
                            )
                        }
                        ScreenState.PRIVACY_POLICY -> {
                            PrivacyPolicyScreen(
                                onBackClick = { currentScreen = ScreenState.PRIVACY_SECURITY }
                            )
                        }
                        ScreenState.SETTINGS -> {
                            SettingsScreen(
                                onBackClick = {
                                    homeInitialTab = 3
                                    currentScreen = ScreenState.MAIN
                                },
                                onChangePasswordClick = { currentScreen = ScreenState.CHANGE_PASSWORD },
                                onHealthPermissionsClick = { currentScreen = ScreenState.HEALTH_PERMISSIONS },
                                onRemindersClick = { currentScreen = ScreenState.REMINDERS },
                                onDataManagementClick = { currentScreen = ScreenState.DATA_MANAGEMENT },
                                onAboutClick = { currentScreen = ScreenState.ABOUT_VITALSINC },
                                onVersionClick = { currentScreen = ScreenState.VERSION }
                            )
                        }
                        ScreenState.CHANGE_PASSWORD -> {
                            ChangePasswordScreen(
                                onBackClick = { currentScreen = ScreenState.SETTINGS },
                                onUpdatePasswordSuccess = { currentScreen = ScreenState.SETTINGS },
                                onForgotPasswordClick = { currentScreen = ScreenState.FORGOT_PASSWORD }
                            )
                        }
                        ScreenState.HEALTH_PERMISSIONS -> {
                            HealthPermissionsScreen(
                                onBackClick = { currentScreen = ScreenState.SETTINGS }
                            )
                        }
                        ScreenState.DATA_MANAGEMENT -> {
                            DataManagementScreen(
                                onBackClick = { currentScreen = ScreenState.SETTINGS }
                            )
                        }
                        ScreenState.ABOUT_VITALSINC -> {
                            AboutVitalSyncScreen(
                                onBackClick = { currentScreen = ScreenState.SETTINGS }
                            )
                        }
                        ScreenState.VERSION -> {
                            VersionScreen(
                                onBackClick = { currentScreen = ScreenState.SETTINGS }
                            )
                        }
                        ScreenState.HELP_SUPPORT -> {
                            HelpSupportScreen(
                                onBackClick = {
                                    homeInitialTab = 3
                                    currentScreen = ScreenState.MAIN
                                }
                            )
                        }
                        ScreenState.VITAL_SIGNS -> {
                            VitalSignsScreen(
                                onBackClick = {
                                    homeInitialTab = 0
                                    currentScreen = ScreenState.MAIN
                                },
                                onHeartRateClick = { currentScreen = ScreenState.HEART_RATE_DETAIL },
                                onBloodPressureClick = { currentScreen = ScreenState.BLOOD_PRESSURE_DETAIL },
                                onSpO2Click = { currentScreen = ScreenState.SPO2_DETAIL },
                                onBmiClick = { currentScreen = ScreenState.BMI_DETAIL },
                                onAddReadingClick = {
                                    activeReadingType = VitalReadingType.HEART_RATE
                                    currentScreen = ScreenState.ADD_READING
                                }
                            )
                        }
                        ScreenState.HEART_RATE_DETAIL -> {
                            HeartRateDetailScreen(
                                onBackClick = {
                                    homeInitialTab = 0
                                    currentScreen = ScreenState.MAIN
                                },
                                onReadingReviewClick = { currentScreen = ScreenState.READING_REVIEW }
                            )
                        }
                        ScreenState.READING_REVIEW -> {
                            val (metricName, valStr, rangeStr) = when (activeReadingType) {
                                VitalReadingType.HEART_RATE -> Triple("Heart rate", "104 BPM", "60–90 BPM")
                                VitalReadingType.BLOOD_PRESSURE -> Triple("Blood pressure", "140/90 mmHg", "120/80 mmHg")
                                VitalReadingType.SPO2 -> Triple("SpO₂", "92%", "95–100%")
                                else -> Triple("Heart rate", "104 BPM", "60–90 BPM")
                            }
                            ReadingReviewScreen(
                                metricName = metricName,
                                currentValue = valStr,
                                usualRange = rangeStr,
                                onBackClick = {
                                    homeInitialTab = 0
                                    currentScreen = ScreenState.MAIN
                                },
                                onRecheckClick = {
                                    currentScreen = ScreenState.ADD_READING
                                },
                                onViewHistoryClick = {
                                    viewHistoryPreviousScreen = ScreenState.READING_REVIEW
                                    currentScreen = ScreenState.VIEW_HISTORY
                                }
                            )
                        }
                        ScreenState.ACTIVITY_DETAIL -> {
                            ActivityDetailScreen(
                                onBackClick = {
                                    homeInitialTab = 0
                                    currentScreen = ScreenState.MAIN
                                },
                                onStartActivityClick = { currentScreen = ScreenState.START_ACTIVITY },
                                onViewHistoryClick = {
                                    viewHistoryPreviousScreen = ScreenState.ACTIVITY_DETAIL
                                    currentScreen = ScreenState.VIEW_HISTORY
                                }
                            )
                        }
                        ScreenState.START_ACTIVITY -> {
                            StartActivityScreen(
                                onBackClick = { currentScreen = ScreenState.ACTIVITY_DETAIL },
                                onStartTracking = { type ->
                                    currentScreen = when (type) {
                                        ActivityType.WALKING -> ScreenState.WALKING_TRACKING
                                        ActivityType.RUNNING -> ScreenState.RUNNING_TRACKING
                                        ActivityType.CYCLING -> ScreenState.CYCLING_TRACKING
                                        ActivityType.WORKOUT -> ScreenState.WORKOUT_TRACKING
                                    }
                                }
                            )
                        }
                        ScreenState.WALKING_TRACKING -> {
                            WalkingTrackingScreen(
                                onBackClick = { currentScreen = ScreenState.START_ACTIVITY },
                                onFinishTracking = { currentScreen = ScreenState.ACTIVITY_DETAIL }
                            )
                        }
                        ScreenState.RUNNING_TRACKING -> {
                            RunningTrackingScreen(
                                onBackClick = { currentScreen = ScreenState.START_ACTIVITY },
                                onFinishTracking = { currentScreen = ScreenState.ACTIVITY_DETAIL }
                            )
                        }
                        ScreenState.CYCLING_TRACKING -> {
                            CyclingTrackingScreen(
                                onBackClick = { currentScreen = ScreenState.START_ACTIVITY },
                                onFinishTracking = { currentScreen = ScreenState.ACTIVITY_DETAIL }
                            )
                        }
                        ScreenState.WORKOUT_TRACKING -> {
                            WorkoutTrackingScreen(
                                onBackClick = { currentScreen = ScreenState.START_ACTIVITY },
                                onFinishTracking = { currentScreen = ScreenState.ACTIVITY_DETAIL }
                            )
                        }
                        ScreenState.VIEW_HISTORY -> {
                            ViewHistoryScreen(
                                onBackClick = { currentScreen = viewHistoryPreviousScreen }
                            )
                        }
                        ScreenState.SLEEP_DETAIL -> {
                            SleepDetailScreen(
                                onBackClick = {
                                    homeInitialTab = 0
                                    currentScreen = ScreenState.MAIN
                                }
                            )
                        }
                        ScreenState.HYDRATION_DETAIL -> {
                            HydrationDetailScreen(
                                onBackClick = {
                                    homeInitialTab = 0
                                    currentScreen = ScreenState.MAIN
                                }
                            )
                        }
                        ScreenState.BLOOD_PRESSURE_DETAIL -> {
                            BloodPressureDetailScreen(
                                onBackClick = {
                                    homeInitialTab = 0
                                    currentScreen = ScreenState.MAIN
                                },
                                onAddReadingClick = {
                                    activeReadingType = VitalReadingType.BLOOD_PRESSURE
                                    currentScreen = ScreenState.ADD_READING
                                }
                            )
                        }
                        ScreenState.SPO2_DETAIL -> {
                            Spo2DetailScreen(
                                onBackClick = {
                                    homeInitialTab = 0
                                    currentScreen = ScreenState.MAIN
                                },
                                onAddReadingClick = {
                                    activeReadingType = VitalReadingType.SPO2
                                    currentScreen = ScreenState.ADD_READING
                                }
                            )
                        }
                        ScreenState.STEP_COUNT_DETAIL -> {
                            StepCountDetailScreen(
                                onBackClick = {
                                    homeInitialTab = 0
                                    currentScreen = ScreenState.MAIN
                                },
                                onEditGoalClick = { currentScreen = ScreenState.EDIT_STEP_GOAL }
                            )
                        }
                        ScreenState.EDIT_STEP_GOAL -> {
                            EditStepGoalScreen(
                                onBackClick = { currentScreen = ScreenState.STEP_COUNT_DETAIL },
                                onSaveGoalSuccess = { currentScreen = ScreenState.STEP_COUNT_DETAIL }
                            )
                        }
                        ScreenState.BMI_DETAIL -> {
                            BmiDetailScreen(
                                onBackClick = {
                                    homeInitialTab = 0
                                    currentScreen = ScreenState.MAIN
                                },
                                onUpdateMetricsClick = { currentScreen = ScreenState.UPDATE_METRICS }
                            )
                        }
                        ScreenState.UPDATE_METRICS -> {
                            UpdateMetricsScreen(
                                onBackClick = { currentScreen = ScreenState.BMI_DETAIL },
                                onSaveMetricsSuccess = { bmi, category, h, w ->
                                    calculatedBmiValue = bmi
                                    calculatedBmiCategory = category
                                    userHeightCm = h
                                    userWeightKg = w
                                    currentScreen = ScreenState.BMI_RESULT
                                }
                            )
                        }
                        ScreenState.BMI_RESULT -> {
                            BmiResultScreen(
                                bmiValue = calculatedBmiValue,
                                bmiCategory = calculatedBmiCategory,
                                heightCm = userHeightCm,
                                weightKg = userWeightKg,
                                onBackClick = { currentScreen = ScreenState.BMI_DETAIL },
                                onDoneClick = { currentScreen = ScreenState.BMI_DETAIL }
                            )
                        }
                        ScreenState.ADD_READING -> {
                            AddReadingScreen(
                                readingType = activeReadingType,
                                onBackClick = {
                                    homeInitialTab = 0
                                    currentScreen = ScreenState.MAIN
                                },
                                onDoneClick = {
                                    currentScreen = ScreenState.READING_REVIEW
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
