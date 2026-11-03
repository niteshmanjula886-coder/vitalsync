package com.simats.vitalsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import com.simats.vitalsync.util.WeeklyReportPdfGenerator
import com.simats.vitalsync.util.NotificationReminderEngine
import com.simats.vitalsync.data.ActivitySessionRecord
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
import com.simats.vitalsync.ui.activity.viewmodel.ActivityViewModel
import com.simats.vitalsync.ui.activity.viewmodel.ActivityViewModelFactory
import com.simats.vitalsync.ui.ai.AiChatHistoryScreen
import com.simats.vitalsync.ui.ai.AiChatScreen
import com.simats.vitalsync.ui.ai.WeeklyHealthReportScreen
import com.simats.vitalsync.ui.ai.viewmodel.AiViewModel
import com.simats.vitalsync.ui.ai.viewmodel.AiViewModelFactory
import com.simats.vitalsync.ui.auth.ForgotPasswordScreen
import com.simats.vitalsync.ui.auth.LoginScreen
import com.simats.vitalsync.ui.auth.PasswordSuccessScreen
import com.simats.vitalsync.ui.auth.SignUpScreen
import com.simats.vitalsync.ui.auth.WelcomeScreen
import com.simats.vitalsync.ui.auth.viewmodel.AuthViewModel
import com.simats.vitalsync.ui.auth.viewmodel.AuthViewModelFactory
import com.simats.vitalsync.ui.gym.ActiveWorkoutSessionScreen
import com.simats.vitalsync.ui.gym.AddCustomExerciseScreen
import com.simats.vitalsync.ui.gym.ExerciseLibraryScreen
import com.simats.vitalsync.ui.gym.FitnessProgressDashboardScreen
import com.simats.vitalsync.ui.gym.WorkoutHistoryScreen
import com.simats.vitalsync.ui.gym.WorkoutSplitSelectionScreen
import com.simats.vitalsync.ui.gym.viewmodel.GymViewModel
import com.simats.vitalsync.ui.gym.viewmodel.GymViewModelFactory
import com.simats.vitalsync.ui.home.HomeScreen
import com.simats.vitalsync.ui.nutrition.AddCustomFoodScreen
import com.simats.vitalsync.ui.nutrition.DailyMealHistoryScreen
import com.simats.vitalsync.ui.nutrition.FoodDetailsScreen
import com.simats.vitalsync.ui.nutrition.FoodSearchScreen
import com.simats.vitalsync.ui.nutrition.NutritionDashboardScreen
import com.simats.vitalsync.ui.nutrition.NutritionSettingsScreen
import com.simats.vitalsync.ui.nutrition.viewmodel.NutritionViewModel
import com.simats.vitalsync.ui.nutrition.viewmodel.NutritionViewModelFactory
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
import com.simats.vitalsync.data.device.HealthDeviceManager
import com.simats.vitalsync.data.device.DeviceConnectionStatus
import com.simats.vitalsync.ui.home.BlePairingBottomSheet
import com.simats.vitalsync.ui.vitals.HeartRateViewModel
import com.simats.vitalsync.ui.vitals.HeartRateViewModelFactory
import com.simats.vitalsync.ui.profile.viewmodel.ProfileViewModel
import com.simats.vitalsync.ui.profile.viewmodel.ProfileViewModelFactory
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
import com.simats.vitalsync.ui.vitals.viewmodel.HydrationViewModel
import com.simats.vitalsync.ui.vitals.viewmodel.HydrationViewModelFactory
import com.simats.vitalsync.ui.vitals.viewmodel.VitalsViewModel
import com.simats.vitalsync.ui.vitals.viewmodel.VitalsViewModelFactory
import java.util.Locale

enum class ScreenState {
    SPLASH,
    ONBOARDING,
    WELCOME,
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
    PASSWORD_CHANGED_SUCCESS,
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
            var isDarkModeEnabled by remember { mutableStateOf(false) }

            VitalsyncTheme(darkTheme = isDarkModeEnabled) {
                val context = LocalContext.current

                // Instantiate ViewModels via their custom Factories
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(applicationContext)
                )
                val gymViewModel: GymViewModel = viewModel(
                    factory = GymViewModelFactory()
                )
                val nutritionViewModel: NutritionViewModel = viewModel(
                    factory = NutritionViewModelFactory()
                )
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModelFactory(applicationContext)
                )
                val vitalsViewModel: VitalsViewModel = viewModel(
                    factory = VitalsViewModelFactory(applicationContext)
                )
                val hydrationViewModel: HydrationViewModel = viewModel(
                    factory = HydrationViewModelFactory()
                )
                val heartRateViewModel: HeartRateViewModel = viewModel(
                    factory = HeartRateViewModelFactory(applicationContext)
                )
                val activityViewModel: ActivityViewModel = viewModel(
                    factory = ActivityViewModelFactory()
                )
                val aiViewModel: AiViewModel = viewModel(
                    factory = AiViewModelFactory(application)
                )
                val aiUiState by aiViewModel.uiState.collectAsState()

                val deviceManager = remember { HealthDeviceManager.getInstance(applicationContext) }
                val coroutineScope = rememberCoroutineScope()
                val deviceConnStatus by deviceManager.connectionStatus.collectAsState()
                val connectedDeviceName by deviceManager.connectedDeviceName.collectAsState()
                val connectedDeviceAddress by deviceManager.connectedDeviceAddress.collectAsState()
                val deviceBatteryLevel by deviceManager.batteryLevel.collectAsState()
                val deviceLastSync by deviceManager.lastSyncTimestamp.collectAsState()
                val discoveredDevicesList by deviceManager.discoveredDevices.collectAsState()
                val liveMetrics by deviceManager.liveMetrics.collectAsState()
                val hrUiState by heartRateViewModel.uiState.collectAsState()
                val activitySummaryState by activityViewModel.summaryState.collectAsState()
                val activitySessionsState by activityViewModel.sessionsState.collectAsState()
                var showBlePairingSheet by remember { mutableStateOf(false) }

                // Current logged-in user email
                val currentUser = authViewModel.getLoggedInUser()
                val currentEmail = currentUser?.email ?: ""

                val profileState by profileViewModel.profileState.collectAsState()
                val healthGoalsState by profileViewModel.healthGoalsState.collectAsState()
                val remindersState by profileViewModel.remindersState.collectAsState()
                val appSettingsState by profileViewModel.appSettingsState.collectAsState()
                val healthPermissionsState by profileViewModel.healthPermissionsState.collectAsState()
                val loginSessionsState by profileViewModel.loginSessionsState.collectAsState()
                val notificationsState by profileViewModel.notificationsState.collectAsState()
                val appVersionState by profileViewModel.appVersionState.collectAsState()
                val weeklyReportState by profileViewModel.weeklyReportState.collectAsState()

                val vitalsSummaryState by vitalsViewModel.summaryState.collectAsState()

                LaunchedEffect(currentEmail) {
                    if (currentEmail.isNotBlank()) {
                        deviceManager.userEmail = currentEmail
                        profileViewModel.fetchProfile(email = currentEmail)
                        vitalsViewModel.fetchSummary(email = currentEmail)
                        profileViewModel.fetchHealthGoals(email = currentEmail)
                        profileViewModel.fetchAppSettings(email = currentEmail)
                        heartRateViewModel.fetchAnalytics(currentEmail)
                        activityViewModel.loadSummary(email = currentEmail)
                        activityViewModel.loadSessions(email = currentEmail)
                    }
                }

                var currentScreen by remember { mutableStateOf(ScreenState.SPLASH) }
                var isNewUser by remember { mutableStateOf(false) }
                var activeReadingType by remember { mutableStateOf(VitalReadingType.HEART_RATE) }
                var homeInitialTab by remember { mutableIntStateOf(0) }
                var viewHistoryPreviousScreen by remember { mutableStateOf(ScreenState.START_ACTIVITY) }

                var activeWorkoutSplit by remember { mutableStateOf(WorkoutSplit.PUSH) }
                var selectedMealCategory by remember { mutableStateOf(MealCategory.BREAKFAST) }
                var activeFoodItem by remember { mutableStateOf(repository.initialIndianFoodDatabase[0]) }
                var activeWorkoutExercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }

                // Live Activity Target Goals
                var targetGoalTabState by remember { mutableStateOf("Distance") }
                var targetGoalMinutesState by remember { mutableIntStateOf(30) }
                var targetGoalDistanceKmState by remember { mutableFloatStateOf(5.0f) }

                var calculatedBmiValue by remember { mutableStateOf("--") }
                var calculatedBmiCategory by remember { mutableStateOf("--") }
                var userHeightCm by remember { mutableStateOf("") }
                var userWeightKg by remember { mutableStateOf("") }

                var globalUserName by remember { mutableStateOf("") }
                var globalUserInitials by remember { mutableStateOf("--") }
                var globalUserAge by remember { mutableStateOf("") }
                var globalProfilePhotoBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

                // Reactive Recomposition Trigger for Nutrition Meals
                var nutritionRefreshTrigger by remember { mutableIntStateOf(0) }

                LaunchedEffect(currentScreen) {
                    if (currentEmail.isNotBlank()) {
                        when (currentScreen) {
                            ScreenState.MAIN, ScreenState.VITAL_SIGNS, ScreenState.START_ACTIVITY -> {
                                vitalsViewModel.fetchSummary(email = currentEmail)
                                activityViewModel.loadSummary(email = currentEmail)
                            }
                            ScreenState.WEEKLY_REPORT -> {
                                vitalsViewModel.fetchSummary(email = currentEmail)
                                activityViewModel.loadSummary(email = currentEmail)
                                profileViewModel.fetchWeeklyHealthReport(email = currentEmail)
                            }
                            ScreenState.VIEW_HISTORY -> {
                                activityViewModel.loadSessions(email = currentEmail)
                            }
                            ScreenState.HEART_RATE_DETAIL -> {
                                heartRateViewModel.fetchAnalytics(currentEmail)
                            }
                            ScreenState.REMINDERS -> {
                                profileViewModel.fetchReminders(email = currentEmail)
                            }
                            ScreenState.NOTIFICATIONS -> {
                                profileViewModel.fetchNotifications(email = currentEmail)
                            }
                            ScreenState.PRIVACY_SECURITY -> {
                                profileViewModel.fetchAppSettings(email = currentEmail)
                                profileViewModel.fetchLoginSessions(email = currentEmail)
                            }
                            ScreenState.HEALTH_PERMISSIONS -> {
                                profileViewModel.fetchHealthPermissions(email = currentEmail)
                            }
                            ScreenState.LOGIN_SESSIONS -> {
                                profileViewModel.fetchLoginSessions(email = currentEmail)
                            }
                            ScreenState.VERSION -> {
                                profileViewModel.fetchAppVersion()
                            }
                            else -> {}
                        }
                    }
                }

                LaunchedEffect(appSettingsState) {
                    NotificationReminderEngine.syncReminders(
                        context = context,
                        dailyRemindersEnabled = appSettingsState.dailyReminders,
                        waterIntervalMinutes = appSettingsState.reminderWaterInterval,
                        workoutReminderEnabled = appSettingsState.reminderWorkoutEnabled,
                        sleepReminderEnabled = appSettingsState.reminderSleepEnabled
                    )
                }

                LaunchedEffect(profileState) {
                    val state = profileState
                    if (state.fullName.isNotBlank()) {
                        globalUserName = state.fullName
                    }
                    if (state.initials.isNotBlank() && state.initials != "--") {
                        globalUserInitials = state.initials
                    }
                    val pAge = state.age
                    if (pAge != null && pAge > 0) {
                        globalUserAge = pAge.toString()
                    }
                    val pH = state.heightCm
                    if (pH != null && pH > 0.0) {
                        userHeightCm = if (pH % 1.0 == 0.0) pH.toInt().toString() else pH.toString()
                    }
                    val pW = state.weightKg
                    if (pW != null && pW > 0.0) {
                        userWeightKg = if (pW % 1.0 == 0.0) pW.toInt().toString() else pW.toString()
                    }
                    val h = pH?.toFloat() ?: 0f
                    val w = pW?.toFloat() ?: 0f
                    if (h > 0f && w > 0f) {
                        val bmiVal = w / ((h / 100f) * (h / 100f))
                        calculatedBmiValue = String.format(Locale.US, "%.1f", bmiVal)
                        calculatedBmiCategory = when {
                            bmiVal < 18.5f -> "Underweight"
                            bmiVal < 25.0f -> "Healthy Range"
                            bmiVal < 30.0f -> "Overweight"
                            else -> "Obese"
                        }
                    }
                }

                val summaryData = vitalsSummaryState.summary?.summary
                val hrSummary = summaryData?.heartRate
                val bpSummary = summaryData?.bloodPressure
                val spo2Summary = summaryData?.spo2
                val stepsSummary = summaryData?.steps
                val sleepSummary = summaryData?.sleep
                val hydrationSummary = summaryData?.hydration

                val isLiveHr = liveMetrics.heartRate != null && liveMetrics.heartRate!! > 0
                val heartRateFormatted = when {
                    isLiveHr -> "${liveMetrics.heartRate} BPM"
                    hrSummary != null && hrSummary.bpm > 0 -> "${hrSummary.bpm} BPM"
                    else -> "-- BPM"
                }
                val heartRateStatusFormatted = when {
                    isLiveHr -> "Live"
                    hrSummary != null && hrSummary.bpm > 0 -> hrSummary.status
                    else -> "No data"
                }

                val isLiveSpo2 = liveMetrics.spo2 != null && liveMetrics.spo2!! > 0
                val spo2Formatted = when {
                    isLiveSpo2 -> "${liveMetrics.spo2}%"
                    spo2Summary != null && spo2Summary.percentage > 0 -> "${spo2Summary.percentage}%"
                    else -> "--%"
                }
                val spo2StatusFormatted = when {
                    isLiveSpo2 -> "Live"
                    spo2Summary != null && spo2Summary.percentage > 0 -> spo2Summary.status
                    else -> "No data"
                }

                val isLiveBp = liveMetrics.bloodPressure != null && liveMetrics.bloodPressure!!.first > 0 && liveMetrics.bloodPressure!!.second > 0
                val bpFormatted = when {
                    isLiveBp -> "${liveMetrics.bloodPressure!!.first}/${liveMetrics.bloodPressure!!.second}"
                    bpSummary != null && bpSummary.systolic > 0 && bpSummary.diastolic > 0 -> "${bpSummary.systolic}/${bpSummary.diastolic}"
                    else -> "--/--"
                }
                val bpStatusFormatted = when {
                    isLiveBp -> "Live"
                    bpSummary != null && bpSummary.systolic > 0 -> bpSummary.status
                    else -> "No data"
                }

                val isLiveSteps = liveMetrics.steps != null && liveMetrics.steps!! > 0
                val stepsFormatted = when {
                    isLiveSteps -> String.format(Locale.US, "%,d", liveMetrics.steps)
                    stepsSummary != null && stepsSummary.current > 0 -> String.format(Locale.US, "%,d", stepsSummary.current)
                    else -> "--"
                }
                val stepsStatusFormatted = if (stepsSummary != null && stepsSummary.current > 0) "${stepsSummary.progressPercent}% of goal" else if (isLiveSteps) "From Watch" else "No data"

                val wellnessScoreVal = vitalsSummaryState.summary?.wellnessScore ?: 0
                val wellnessStatusVal = vitalsSummaryState.summary?.wellnessStatus ?: "No data recorded"

                val todayRecords = remember(currentScreen, nutritionRefreshTrigger) {
                    repository.getActivitySessionRecords().filter { it.dateString == repository.getTodayDateString() }
                }
                val localTodaySteps = todayRecords.sumOf { it.steps }
                val localTodayDistance = todayRecords.map { it.distanceKm }.sum()
                val localTodayCalories = todayRecords.sumOf { it.caloriesBurned }
                val localTodayMinutes = todayRecords.sumOf { it.durationSeconds } / 60

                val totalDisplaySteps = maxOf(stepsSummary?.current ?: 0, localTodaySteps)
                val totalDisplayDistance = maxOf(stepsSummary?.distanceKm ?: 0f, localTodayDistance)
                val totalDisplayCalories = maxOf(stepsSummary?.caloriesKcal ?: 0, localTodayCalories)
                val totalDisplayMinutes = maxOf(activitySummaryState.summary?.activeMinutes?.current ?: 0, localTodayMinutes)

                val activityStepsFormatted = if (totalDisplaySteps > 0) "${String.format(Locale.US, "%,d", totalDisplaySteps)} steps" else "-- steps"
                val activityDistFormatted = if (totalDisplayDistance > 0f) String.format(Locale.US, "%.1f km", totalDisplayDistance) else "-- km"
                val activityCalFormatted = if (totalDisplayCalories > 0) "$totalDisplayCalories kcal" else "-- kcal"
                val activityMinutesFormatted = if (totalDisplayMinutes > 0) "$totalDisplayMinutes min" else "-- min"

                val hydState by hydrationViewModel.hydrationState.collectAsState()
                val localSleepRecord = remember(currentScreen, nutritionRefreshTrigger) {
                    repository.getSleepRecord(repository.getTodayDateString())
                }

                val sleepDurationFormatted = when {
                    sleepSummary != null && sleepSummary.durationMinutes > 0 -> sleepSummary.durationFormatted
                    localSleepRecord != null -> "${localSleepRecord.durationMinutes / 60}h ${localSleepRecord.durationMinutes % 60}m"
                    else -> "--"
                }

                val sleepScoreFormatted = when {
                    sleepSummary != null && sleepSummary.durationMinutes > 0 -> "${sleepSummary.quality} (${sleepSummary.sleepScore} score)"
                    localSleepRecord != null -> "Logged (${localSleepRecord.sleepScore} score)"
                    else -> "No sleep logged"
                }

                val consumedWaterMl = if (hydrationSummary != null && hydrationSummary.consumedMl > 0) {
                    hydrationSummary.consumedMl
                } else {
                    hydState.consumedWaterMl
                }

                val targetWaterMl = if (hydrationSummary != null && hydrationSummary.targetMl > 0) {
                    hydrationSummary.targetMl
                } else {
                    hydState.dailyTargetMl
                }

                val hydGlasses = consumedWaterMl / 250f
                val hydTargetGlasses = targetWaterMl / 250f

                val hydrationConsumedFormatted = if (consumedWaterMl > 0) {
                    "${String.format(Locale.US, "%.1f", hydGlasses)} / ${hydTargetGlasses.toInt()} glasses"
                } else {
                    "0 / ${hydTargetGlasses.toInt()} glasses"
                }

                val hydrationSubtextFormatted = if (consumedWaterMl > 0) {
                    "${String.format(Locale.US, "%,d", consumedWaterMl)} ml / ${String.format(Locale.US, "%,d", targetWaterMl)} ml"
                } else {
                    "0 ml / ${String.format(Locale.US, "%,d", targetWaterMl)} ml"
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            if (initialState == ScreenState.SPLASH && targetState == ScreenState.ONBOARDING) {
                                (fadeIn(animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)) +
                                    scaleIn(
                                        animationSpec = spring(
                                            dampingRatio = 0.72f,
                                            stiffness = Spring.StiffnessMediumLow
                                        ),
                                        initialScale = 0.90f
                                    ))
                                    .togetherWith(
                                        fadeOut(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)) +
                                            scaleOut(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing), targetScale = 1.08f)
                                    )
                            } else {
                                val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                                (slideInHorizontally(
                                    animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
                                    initialOffsetX = { fullWidth -> fullWidth * direction }
                                ) + fadeIn(animationSpec = tween(300)))
                                    .togetherWith(
                                        slideOutHorizontally(
                                            animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
                                            targetOffsetX = { fullWidth -> -fullWidth * direction }
                                        ) + fadeOut(animationSpec = tween(280))
                                    )
                            }
                        },
                        label = "AppleSpringPopScreenTransition"
                    ) { screen ->
                        when (screen) {
                            ScreenState.SPLASH -> {
                                SplashScreen(
                                    splashDurationMillis = 2800L,
                                    onTimeout = {
                                        currentScreen = if (authViewModel.isLoggedIn()) {
                                            ScreenState.MAIN
                                        } else {
                                            ScreenState.ONBOARDING
                                        }
                                    }
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
                            ScreenState.WELCOME -> {
                                WelcomeScreen(
                                    userName = globalUserName.split(" ").firstOrNull() ?: "Alex",
                                    onSetUpProfileClick = {
                                        homeInitialTab = 0
                                        currentScreen = ScreenState.MAIN
                                    },
                                    onSkipToDashboardClick = {
                                        homeInitialTab = 0
                                        currentScreen = ScreenState.MAIN
                                    }
                                )
                            }
                            ScreenState.LOGIN -> {
                                LoginScreen(
                                    viewModel = authViewModel,
                                    onLoginSuccess = {
                                        isNewUser = false
                                        homeInitialTab = 0
                                        currentScreen = ScreenState.MAIN
                                    },
                                    onLoginSuccessWithUser = { user ->
                                        if (user != null) {
                                            globalUserName = user.displayName
                                            val parts = globalUserName.trim().split(" ")
                                            globalUserInitials = if (parts.size >= 2) {
                                                (parts[0].take(1) + parts[1].take(1)).uppercase()
                                            } else {
                                                parts.firstOrNull()?.take(2)?.uppercase() ?: "AM"
                                            }
                                        }
                                    },
                                    onNavigateToSignUp = { currentScreen = ScreenState.SIGNUP },
                                    onForgotPasswordClick = { currentScreen = ScreenState.FORGOT_PASSWORD }
                                )
                            }
                            ScreenState.SIGNUP -> {
                                SignUpScreen(
                                    viewModel = authViewModel,
                                    onBackClick = { currentScreen = ScreenState.LOGIN },
                                    onSignUpSuccess = { signedName ->
                                        isNewUser = true
                                        if (signedName.isNotBlank()) {
                                            globalUserName = signedName
                                        }
                                        currentScreen = ScreenState.PROFILE_SETUP
                                    },
                                    onSignUpSuccessWithUser = { user ->
                                        if (user != null) {
                                            globalUserName = user.displayName
                                            val parts = globalUserName.trim().split(" ")
                                            globalUserInitials = if (parts.size >= 2) {
                                                (parts[0].take(1) + parts[1].take(1)).uppercase()
                                            } else {
                                                parts.firstOrNull()?.take(2)?.uppercase() ?: "AM"
                                            }
                                        }
                                    },
                                    onNavigateToLogin = { currentScreen = ScreenState.LOGIN }
                                )
                            }
                            ScreenState.FORGOT_PASSWORD -> {
                                ForgotPasswordScreen(
                                    viewModel = authViewModel,
                                    onBackClick = { currentScreen = ScreenState.LOGIN },
                                    onResetLinkSent = {
                                        currentScreen = ScreenState.PASSWORD_CHANGED_SUCCESS
                                    }
                                )
                            }
                            ScreenState.PROFILE_SETUP -> {
                                ProfileSetupScreen(
                                    onBackClick = { currentScreen = ScreenState.SIGNUP },
                                    onSaveAndContinue = { newName, newAge, newHeight, newWeight ->
                                        if (newName.isNotBlank()) {
                                            globalUserName = newName
                                            val parts = newName.trim().split(" ")
                                            globalUserInitials = if (parts.size >= 2) {
                                                (parts[0].take(1) + parts[1].take(1)).uppercase()
                                            } else {
                                                parts[0].take(2).uppercase()
                                            }
                                        }
                                        if (newAge.isNotBlank()) globalUserAge = newAge.filter { it.isDigit() }
                                        if (newHeight.isNotBlank()) userHeightCm = newHeight.filter { it.isDigit() }
                                        if (newWeight.isNotBlank()) userWeightKg = newWeight.filter { it.isDigit() || it == '.' }

                                        currentScreen = ScreenState.WELCOME
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
                                    userPrimaryGoal = profileState.primaryGoal ?: healthGoalsState.selectedGoal,
                                    userPhotoBitmap = globalProfilePhotoBitmap,
                                    heartRateValue = heartRateFormatted,
                                    heartRateStatus = heartRateStatusFormatted,
                                    spo2Value = spo2Formatted,
                                    spo2Status = spo2StatusFormatted,
                                    bpValue = bpFormatted,
                                    bpStatus = bpStatusFormatted,
                                    stepsValue = stepsFormatted,
                                    stepsStatus = stepsStatusFormatted,
                                    wellnessScore = wellnessScoreVal,
                                    wellnessStatus = wellnessStatusVal,
                                    activitySteps = activityStepsFormatted,
                                    activityDistance = activityDistFormatted,
                                    activityCalories = activityCalFormatted,
                                    activityMinutes = activityMinutesFormatted,
                                    sleepDuration = sleepDurationFormatted,
                                    sleepScore = sleepScoreFormatted,
                                    hydrationConsumed = hydrationConsumedFormatted,
                                    hydrationSubtext = hydrationSubtextFormatted,
                                    isDeviceConnected = (deviceConnStatus == DeviceConnectionStatus.CONNECTED),
                                    connectedDeviceName = connectedDeviceName,
                                    onPairWatchClick = { showBlePairingSheet = true },
                                    showAiInsights = appSettingsState.aiInsights,
                                    onWellnessScoreClick = { currentScreen = ScreenState.WEEKLY_REPORT },
                                    onVitalsClick = { currentScreen = ScreenState.VITAL_SIGNS },
                                    onHeartRateClick = { currentScreen = ScreenState.HEART_RATE_DETAIL },
                                    onBloodPressureClick = { currentScreen = ScreenState.BLOOD_PRESSURE_DETAIL },
                                    onSpO2Click = { currentScreen = ScreenState.SPO2_DETAIL },
                                    onStepsClick = { currentScreen = ScreenState.STEP_COUNT_DETAIL },
                                    onActivityClick = { currentScreen = ScreenState.START_ACTIVITY },
                                    onSleepClick = { currentScreen = ScreenState.SLEEP_DETAIL },
                                    onHydrationClick = { currentScreen = ScreenState.HYDRATION_DETAIL },
                                    onPersonalInformationClick = { currentScreen = ScreenState.PERSONAL_INFORMATION },
                                    onHealthGoalsClick = { currentScreen = ScreenState.HEALTH_GOALS },
                                    onConnectedDevicesClick = { currentScreen = ScreenState.CONNECTED_DEVICES },
                                    onNotificationsClick = { currentScreen = ScreenState.NOTIFICATIONS },
                                    onPrivacySecurityClick = { currentScreen = ScreenState.PRIVACY_SECURITY },
                                    onSettingsClick = { currentScreen = ScreenState.SETTINGS },
                                    onHelpSupportClick = { currentScreen = ScreenState.HELP_SUPPORT },
                                    aiInsightTitle = aiUiState.dailyInsight?.title,
                                    aiInsightSummary = aiUiState.dailyInsight?.summary,
                                    onAskAiClick = { currentScreen = ScreenState.AI_CHAT },
                                    onStartWorkoutSplitClick = { currentScreen = ScreenState.GYM_SPLIT_SELECT },
                                    onOpenNutritionTrackerClick = { currentScreen = ScreenState.NUTRITION_DASHBOARD },
                                    onOpenProgressAnalyticsClick = { currentScreen = ScreenState.FITNESS_PROGRESS },
                                    onViewWorkoutHistoryClick = { currentScreen = ScreenState.WORKOUT_HISTORY },
                                    onLogoutClick = {
                                        authViewModel.logout()
                                        currentScreen = ScreenState.LOGIN
                                    }
                                )
                            }
                            ScreenState.GYM_SPLIT_SELECT -> {
                                WorkoutSplitSelectionScreen(
                                    viewModel = gymViewModel,
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
                                    viewModel = gymViewModel,
                                    userEmail = currentEmail,
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
                                    viewModel = gymViewModel,
                                    userEmail = currentEmail,
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
                                        gymViewModel.saveWorkoutSession(currentEmail, session)
                                        repository.saveWorkoutSession(session)
                                        homeInitialTab = 4
                                        currentScreen = ScreenState.MAIN
                                    }
                                )
                            }
                            ScreenState.WORKOUT_HISTORY -> {
                                WorkoutHistoryScreen(
                                    viewModel = gymViewModel,
                                    userEmail = currentEmail,
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
                                val todayMeals = remember(today, nutritionRefreshTrigger) {
                                    repository.getMealEntriesForDate(today)
                                }
                                NutritionDashboardScreen(
                                    viewModel = nutritionViewModel,
                                    userEmail = currentEmail,
                                    dateString = "Today · $today",
                                    goal = repository.getNutritionGoal(),
                                    loggedMeals = todayMeals,
                                    onBackClick = {
                                        homeInitialTab = 4
                                        currentScreen = ScreenState.MAIN
                                    },
                                    onAddMealClick = { category ->
                                        selectedMealCategory = category
                                        currentScreen = ScreenState.FOOD_SEARCH
                                    },
                                    onDeleteMealEntry = { id ->
                                        repository.deleteMealEntry(id, today)
                                        nutritionRefreshTrigger++
                                    },
                                    onViewMealHistoryClick = { currentScreen = ScreenState.DAILY_MEAL_HISTORY },
                                    onNutritionSettingsClick = { currentScreen = ScreenState.NUTRITION_SETTINGS }
                                )
                            }
                            ScreenState.FOOD_SEARCH -> {
                                FoodSearchScreen(
                                    viewModel = nutritionViewModel,
                                    userEmail = currentEmail,
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
                                    viewModel = nutritionViewModel,
                                    userEmail = currentEmail,
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
                                    viewModel = nutritionViewModel,
                                    userEmail = currentEmail,
                                    foodItem = activeFoodItem,
                                    initialMealCategory = selectedMealCategory,
                                    dateString = today,
                                    onBackClick = { currentScreen = ScreenState.FOOD_SEARCH },
                                    onLogMealSuccess = { mealEntry ->
                                        repository.saveMealEntry(mealEntry)
                                        nutritionRefreshTrigger++
                                        currentScreen = ScreenState.NUTRITION_DASHBOARD
                                    }
                                )
                            }
                            ScreenState.DAILY_MEAL_HISTORY -> {
                                val today = repository.getTodayDateString()
                                val todayMeals = remember(today, nutritionRefreshTrigger) {
                                    repository.getMealEntriesForDate(today)
                                }
                                DailyMealHistoryScreen(
                                    viewModel = nutritionViewModel,
                                    userEmail = currentEmail,
                                    dateString = "Today · $today",
                                    loggedMeals = todayMeals,
                                    onBackClick = { currentScreen = ScreenState.NUTRITION_DASHBOARD },
                                    onDeleteMealEntry = { id ->
                                        repository.deleteMealEntry(id, today)
                                        nutritionRefreshTrigger++
                                    }
                                )
                            }
                            ScreenState.NUTRITION_SETTINGS -> {
                                NutritionSettingsScreen(
                                    viewModel = nutritionViewModel,
                                    userEmail = currentEmail,
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
                                    viewModel = aiViewModel,
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
                                val report = weeklyReportState.report
                                val reportScore = report?.wellnessScore ?: wellnessScoreVal
                                val reportStatus = report?.categoryLabel ?: wellnessStatusVal
                                val reportHrAvg = report?.metrics?.heartRateAvg ?: heartRateFormatted
                                val reportHrStatus = report?.metrics?.heartRateStatus ?: heartRateStatusFormatted
                                val reportBpAvg = report?.metrics?.bpAvg ?: bpFormatted
                                val reportBpStatus = report?.metrics?.bpStatus ?: bpStatusFormatted
                                val reportSpo2Avg = report?.metrics?.spo2Avg ?: spo2Formatted
                                val reportSpo2Status = report?.metrics?.spo2Status ?: spo2StatusFormatted
                                val reportSleepQuality = report?.metrics?.sleepQuality ?: sleepDurationFormatted
                                val reportSleepStatus = report?.metrics?.sleepStatus ?: (if (sleepSummary != null && sleepSummary.durationMinutes > 0) sleepSummary.quality else "No Data")
                                val reportHydrationTotal = report?.metrics?.hydrationTotal ?: (if (hydrationSummary != null && hydrationSummary.consumedMl > 0) "${hydrationSummary.consumedMl} ml" else "-- ml")
                                val reportHydrationStatus = report?.metrics?.hydrationStatus ?: (if (hydrationSummary != null && hydrationSummary.consumedMl > 0) "${hydrationSummary.progressPercent}% of goal" else "No Data")
                                val reportActivitySteps = report?.metrics?.activitySteps ?: activityStepsFormatted
                                val reportActivityStatus = report?.metrics?.activityStatus ?: stepsStatusFormatted
                                val reportAiObs = report?.summaryText?.takeIf { it.isNotBlank() } ?: (if (wellnessScoreVal > 0) "Weekly clinical summary: Vitals tracked across heart rate, SpO2, and activity. Keep up the balanced routine!" else "No weekly vital logs recorded yet. Start tracking or connect a smartwatch to view clinical observations.")

                                WeeklyHealthReportScreen(
                                    wellnessScore = reportScore,
                                    wellnessStatus = reportStatus,
                                    heartRateAvg = reportHrAvg,
                                    heartRateStatus = reportHrStatus,
                                    bpAvg = reportBpAvg,
                                    bpStatus = reportBpStatus,
                                    spo2Avg = reportSpo2Avg,
                                    spo2Status = reportSpo2Status,
                                    sleepQuality = reportSleepQuality,
                                    sleepStatus = reportSleepStatus,
                                    hydrationTotal = reportHydrationTotal,
                                    hydrationStatus = reportHydrationStatus,
                                    activitySteps = reportActivitySteps,
                                    activityStatus = reportActivityStatus,
                                    aiObservationText = reportAiObs,
                                    showAiInsights = appSettingsState.aiInsights,
                                    onBackClick = {
                                        homeInitialTab = 0
                                        currentScreen = ScreenState.MAIN
                                    },
                                    onDownloadPdfClick = {
                                        val file = WeeklyReportPdfGenerator.generatePdf(
                                            context = context,
                                            userName = globalUserName,
                                            userEmail = currentEmail,
                                            wellnessScore = reportScore,
                                            wellnessStatus = reportStatus,
                                            heartRate = reportHrAvg,
                                            heartRateStatus = reportHrStatus,
                                            bp = reportBpAvg,
                                            bpStatus = reportBpStatus,
                                            spo2 = reportSpo2Avg,
                                            spo2Status = reportSpo2Status,
                                            sleep = reportSleepQuality,
                                            sleepStatus = reportSleepStatus,
                                            hydration = reportHydrationTotal,
                                            hydrationStatus = reportHydrationStatus,
                                            steps = reportActivitySteps,
                                            stepsStatus = reportActivityStatus,
                                            observationText = reportAiObs
                                        )
                                        if (file != null && file.exists()) {
                                            WeeklyReportPdfGenerator.sharePdf(context, file)
                                        } else {
                                            Toast.makeText(context, "Could not generate PDF report", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onShareSummaryClick = {
                                        val summaryText = buildString {
                                            appendLine("VitalSync Weekly Health Report")
                                            appendLine("Patient: $globalUserName ($currentEmail)")
                                            appendLine("Wellness Score: $reportScore/100 ($reportStatus)")
                                            appendLine("- Heart Rate: $reportHrAvg ($reportHrStatus)")
                                            appendLine("- Blood Pressure: $reportBpAvg ($reportBpStatus)")
                                            appendLine("- SpO2: $reportSpo2Avg ($reportSpo2Status)")
                                            appendLine("- Sleep: $reportSleepQuality ($reportSleepStatus)")
                                            appendLine("- Hydration: $reportHydrationTotal ($reportHydrationStatus)")
                                            appendLine("- Steps: $reportActivitySteps ($reportActivityStatus)")
                                            appendLine()
                                            appendLine("Observations: $reportAiObs")
                                        }
                                        WeeklyReportPdfGenerator.shareTextSummary(context, summaryText)
                                    }
                                )
                            }
                            ScreenState.REMINDERS -> {
                                RemindersScreen(
                                    reminders = remindersState.reminders,
                                    isLoading = remindersState.isLoading,
                                    onToggleReminder = { reminder, enabled ->
                                        profileViewModel.toggleReminder(reminder, enabled, email = currentEmail)
                                    },
                                    onDeleteReminder = { id ->
                                        profileViewModel.deleteReminder(id, userId = profileState.userId)
                                    },
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
                                    onSaveReminderSuccess = { title, time, freq ->
                                        profileViewModel.addReminder(
                                            title = title,
                                            type = "General",
                                            time = time,
                                            frequency = freq,
                                            userId = profileState.userId,
                                            email = currentEmail
                                        )
                                        currentScreen = ScreenState.REMINDERS
                                    }
                                )
                            }
                            ScreenState.PERSONAL_INFORMATION -> {
                                PersonalInformationScreen(
                                    currentName = globalUserName,
                                    currentInitials = globalUserInitials,
                                    currentHeight = userHeightCm,
                                    currentWeight = userWeightKg,
                                    currentDob = profileState.dob ?: "",
                                    currentGender = profileState.gender ?: "",
                                    currentEmail = currentEmail,
                                    currentPhone = profileState.phone ?: "",
                                    currentPhotoBitmap = globalProfilePhotoBitmap,
                                    onBackClick = {
                                        homeInitialTab = 3
                                        currentScreen = ScreenState.MAIN
                                    },
                                    onSaveChangesClick = { newName, newInitials, newAge, newHeight, newWeight, newPhotoBitmap, newDob, newGender, newPhone ->
                                        globalUserName = newName
                                        globalUserInitials = newInitials
                                        globalUserAge = newAge
                                        userHeightCm = newHeight
                                        userWeightKg = newWeight
                                        globalProfilePhotoBitmap = newPhotoBitmap

                                        profileViewModel.updatePersonalInformation(
                                            newName = newName,
                                            newAge = newAge.toIntOrNull() ?: 0,
                                            newHeightCm = newHeight.toDoubleOrNull() ?: 0.0,
                                            newWeightKg = newWeight.toDoubleOrNull() ?: 0.0,
                                            newPhone = newPhone,
                                            newDob = newDob,
                                            newGender = newGender,
                                            email = currentEmail
                                        )

                                        val h = newHeight.toFloatOrNull() ?: 0f
                                        val w = newWeight.toFloatOrNull() ?: 0f
                                        if (h > 0f && w > 0f) {
                                            val bmiVal = w / ((h / 100f) * (h / 100f))
                                            calculatedBmiValue = String.format(Locale.US, "%.1f", bmiVal)
                                            calculatedBmiCategory = when {
                                                bmiVal < 18.5f -> "Underweight"
                                                bmiVal < 25.0f -> "Healthy Range"
                                                bmiVal < 30.0f -> "Overweight"
                                                else -> "Obese"
                                            }
                                        }

                                        homeInitialTab = 3
                                        currentScreen = ScreenState.MAIN
                                    }
                                )
                            }
                            ScreenState.HEALTH_GOALS -> {
                                val currentGoal = profileState.primaryGoal ?: healthGoalsState.selectedGoal
                                HealthGoalsScreen(
                                    initialGoalId = when (currentGoal.lowercase()) {
                                        "build muscle" -> "1"
                                        "weight loss", "weight management" -> "2"
                                        "improve fitness", "fitness" -> "3"
                                        "endurance" -> "4"
                                        else -> "3"
                                    },
                                    currentGoalTitle = currentGoal,
                                    onGoalSelected = { goalTitle ->
                                        profileViewModel.onGoalSelected(goalTitle = goalTitle, email = currentEmail)
                                    },
                                    onBackClick = {
                                        homeInitialTab = 3
                                        currentScreen = ScreenState.MAIN
                                    },
                                    onSaveGoalClick = { goalId, goalTitle ->
                                        profileViewModel.saveHealthGoal(goalId, goalTitle, email = currentEmail)
                                        homeInitialTab = 3
                                        currentScreen = ScreenState.MAIN
                                    }
                                )
                            }
                            ScreenState.CONNECTED_DEVICES -> {
                                ConnectedDevicesScreen(
                                    connectionStatus = deviceConnStatus,
                                    connectedDeviceName = connectedDeviceName,
                                    connectedDeviceAddress = connectedDeviceAddress,
                                    batteryLevel = deviceBatteryLevel,
                                    lastSyncTimestamp = deviceLastSync,
                                    discoveredDevices = discoveredDevicesList,
                                    onStartScan = { deviceManager.startScan() },
                                    onStopScan = { deviceManager.stopScan() },
                                    onConnectDevice = { dev -> deviceManager.connectDevice(dev) },
                                    onDisconnectDevice = { deviceManager.disconnect() },
                                    onRequestPermissions = { deviceManager.startScan() },
                                    onSyncHealthConnect = { coroutineScope.launch { deviceManager.syncHealthConnectData() } },
                                    onBackClick = {
                                        homeInitialTab = 3
                                        currentScreen = ScreenState.MAIN
                                    }
                                )
                            }
                            ScreenState.NOTIFICATIONS -> {
                                NotificationsScreen(
                                    notifications = notificationsState.notifications,
                                    unreadCount = notificationsState.unreadCount,
                                    isLoading = notificationsState.isLoading,
                                    onMarkAllReadClick = {
                                        profileViewModel.markAllNotificationsRead(email = currentEmail)
                                    },
                                    onBackClick = {
                                        homeInitialTab = 3
                                        currentScreen = ScreenState.MAIN
                                    }
                                )
                            }
                            ScreenState.PRIVACY_SECURITY -> {
                                PrivacySecurityScreen(
                                    aiInsightsEnabled = appSettingsState.aiInsights,
                                    onAiInsightsChange = { enabled ->
                                        profileViewModel.updateAppSettings(email = currentEmail, aiInsights = enabled)
                                    },
                                    usageAnalyticsEnabled = appSettingsState.usageAnalytics,
                                    onUsageAnalyticsChange = { enabled ->
                                        profileViewModel.updateAppSettings(email = currentEmail, usageAnalytics = enabled)
                                    },
                                    activeSessionsCount = if (loginSessionsState.sessions.isNotEmpty()) loginSessionsState.sessions.size else 1,
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
                                    sessions = loginSessionsState.sessions,
                                    isLoading = loginSessionsState.isLoading,
                                    onLogoutOtherSessions = {
                                        profileViewModel.logoutOtherSessions(email = currentEmail)
                                    },
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
                                    isDarkTheme = isDarkModeEnabled,
                                    onDarkThemeToggle = { isDarkModeEnabled = it },
                                    pushNotifications = appSettingsState.pushNotifications,
                                    onPushNotificationsToggle = { enabled ->
                                        profileViewModel.updateAppSettings(email = currentEmail, pushNotifications = enabled)
                                    },
                                    dailyReminders = appSettingsState.dailyReminders,
                                    onDailyRemindersToggle = { enabled ->
                                        profileViewModel.updateAppSettings(email = currentEmail, dailyReminders = enabled)
                                    },
                                    aiInsights = appSettingsState.aiInsights,
                                    onAiInsightsToggle = { enabled ->
                                        profileViewModel.updateAppSettings(email = currentEmail, aiInsights = enabled)
                                    },
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
                                    onUpdatePasswordSuccess = {
                                        currentScreen = ScreenState.PASSWORD_CHANGED_SUCCESS
                                    },
                                    onForgotPasswordClick = { currentScreen = ScreenState.FORGOT_PASSWORD }
                                )
                            }
                            ScreenState.PASSWORD_CHANGED_SUCCESS -> {
                                PasswordSuccessScreen(
                                    isFromProfile = authViewModel.isLoggedIn(),
                                    onContinueClick = {
                                        currentScreen = if (authViewModel.isLoggedIn()) ScreenState.SETTINGS else ScreenState.LOGIN
                                    }
                                )
                            }
                            ScreenState.HEALTH_PERMISSIONS -> {
                                HealthPermissionsScreen(
                                    vitalSigns = healthPermissionsState.vitalSigns,
                                    activity = healthPermissionsState.activity,
                                    sleep = healthPermissionsState.sleep,
                                    hydration = healthPermissionsState.hydration,
                                    writeHealthData = healthPermissionsState.writeHealthData,
                                    onRequestBluetoothPermissions = { deviceManager.startScan() },
                                    onRequestHealthPermissions = {
                                        try {
                                            val intent = Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS").apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            try {
                                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                    data = Uri.fromParts("package", context.packageName, null)
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Health Connect settings not available on this device", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    onSavePermissions = { vit, act, slp, hyd, wrt ->
                                        profileViewModel.saveHealthPermissions(
                                            email = currentEmail,
                                            vitalSigns = vit,
                                            activity = act,
                                            sleep = slp,
                                            hydration = hyd,
                                            writeHealthData = wrt
                                        )
                                        currentScreen = ScreenState.SETTINGS
                                    },
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
                                    versionName = appVersionState.versionName,
                                    buildType = appVersionState.buildType,
                                    features = appVersionState.features,
                                    isLoading = appVersionState.isLoading,
                                    onCheckForUpdates = { profileViewModel.fetchAppVersion() },
                                    onBackClick = { currentScreen = ScreenState.SETTINGS }
                                )
                            }
                            ScreenState.HELP_SUPPORT -> {
                                HelpSupportScreen(
                                    onSubmitInquiry = { subject, message ->
                                        profileViewModel.submitSupportTicket(
                                            subject = subject,
                                            message = message,
                                            email = currentEmail
                                        )
                                    },
                                    onBackClick = {
                                        homeInitialTab = 3
                                        currentScreen = ScreenState.MAIN
                                    }
                                )
                            }
                            ScreenState.VITAL_SIGNS -> {
                                VitalSignsScreen(
                                    heartRateValue = if (hrSummary != null && hrSummary.bpm > 0) "${hrSummary.bpm} bpm" else "--",
                                    heartRateStatus = if (hrSummary != null && hrSummary.bpm > 0) hrSummary.status else "No data available",
                                    heartRateUpdated = if (hrSummary != null && hrSummary.recordedAt.isNotBlank()) hrSummary.recordedAt else "No readings",
                                    bpValue = if (bpSummary != null && bpSummary.systolic > 0 && bpSummary.diastolic > 0) "${bpSummary.systolic}/${bpSummary.diastolic} mmHg" else "--/--",
                                    bpStatus = if (bpSummary != null && bpSummary.systolic > 0) bpSummary.status else "No data available",
                                    bpUpdated = if (bpSummary != null && bpSummary.recordedAt.isNotBlank()) bpSummary.recordedAt else "No readings",
                                    spo2Value = if (spo2Summary != null && spo2Summary.percentage > 0) "${spo2Summary.percentage}%" else "--",
                                    spo2Status = if (spo2Summary != null && spo2Summary.percentage > 0) spo2Summary.status else "No data available",
                                    spo2Updated = if (spo2Summary != null && spo2Summary.recordedAt.isNotBlank()) spo2Summary.recordedAt else "No readings",
                                    weightValue = if (userWeightKg.isNotBlank()) "$userWeightKg kg" else "--",
                                    bmiValue = calculatedBmiValue,
                                    bmiStatus = calculatedBmiCategory,
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
                                LaunchedEffect(currentEmail) {
                                    if (currentEmail.isNotBlank()) {
                                        heartRateViewModel.fetchAnalytics(currentEmail)
                                    }
                                }
                                HeartRateDetailScreen(
                                    uiState = hrUiState,
                                    initialBpm = hrSummary?.bpm ?: 0,
                                    restingBpm = hrSummary?.restingBpm ?: 0,
                                    minBpm = hrSummary?.minBpm ?: 0,
                                    maxBpm = hrSummary?.maxBpm ?: 0,
                                    avgBpm = hrSummary?.bpm ?: 0,
                                    showAiInsights = appSettingsState.aiInsights,
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
                                        viewHistoryPreviousScreen = ScreenState.START_ACTIVITY
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
                                        viewHistoryPreviousScreen = ScreenState.START_ACTIVITY
                                        currentScreen = ScreenState.VIEW_HISTORY
                                    }
                                )
                            }
                            ScreenState.START_ACTIVITY -> {
                                val walkingStats = repository.getActivityStatsByType("Walking")
                                val runningStats = repository.getActivityStatsByType("Running")
                                val cyclingStats = repository.getActivityStatsByType("Cycling")

                                StartActivityScreen(
                                    walkingStats = walkingStats,
                                    runningStats = runningStats,
                                    cyclingStats = cyclingStats,
                                    onBackClick = {
                                        homeInitialTab = 0
                                        currentScreen = ScreenState.MAIN
                                    },
                                    onRefreshStatsClick = {
                                        repository.clearActivitySessionRecords()
                                    },
                                    onViewHistoryClick = {
                                        viewHistoryPreviousScreen = ScreenState.START_ACTIVITY
                                        currentScreen = ScreenState.VIEW_HISTORY
                                    },
                                    onStartTracking = { type, goalTab, targetDurationMinutes, targetDistanceKm ->
                                        targetGoalTabState = goalTab
                                        targetGoalMinutesState = targetDurationMinutes
                                        targetGoalDistanceKmState = targetDistanceKm
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
                                    targetGoalTab = targetGoalTabState,
                                    targetMinutes = targetGoalMinutesState,
                                    targetDistanceKm = targetGoalDistanceKmState,
                                    userWeightKg = userWeightKg.toFloatOrNull() ?: 70.0f,
                                    onBackClick = { currentScreen = ScreenState.START_ACTIVITY },
                                    onFinishTracking = {
                                        currentScreen = ScreenState.START_ACTIVITY
                                    },
                                    onFinishTrackingWithData = { dur, dist, cal, pace, hr, steps ->
                                        val sessionUuid = java.util.UUID.randomUUID().toString()
                                        val targetType = targetGoalTabState.lowercase()
                                        val targetVal = if (targetGoalTabState == "Distance") targetGoalDistanceKmState.toInt() else targetGoalMinutesState
                                        val todayDate = repository.getTodayDateString()

                                        val record = ActivitySessionRecord(
                                            activityType = "Walking",
                                            durationSeconds = dur,
                                            distanceKm = dist,
                                            caloriesBurned = cal,
                                            dateString = todayDate,
                                            sessionUuid = sessionUuid,
                                            targetType = targetType,
                                            targetValue = targetVal,
                                            steps = steps,
                                            avgPace = pace,
                                            trackingSource = "phone_sensor"
                                        )
                                        repository.saveActivitySessionRecord(record)
                                        nutritionRefreshTrigger++

                                        if (currentEmail.isNotBlank()) {
                                            activityViewModel.recordSession(
                                                email = currentEmail,
                                                activityType = "Walking",
                                                durationSeconds = dur,
                                                distanceKm = dist,
                                                caloriesBurned = cal,
                                                avgPace = pace,
                                                avgHeartRate = hr ?: 108,
                                                steps = steps,
                                                sessionUuid = sessionUuid,
                                                targetType = targetType,
                                                targetValue = targetVal,
                                                trackingSource = "phone_sensor",
                                                onComplete = {
                                                    vitalsViewModel.fetchSummary(email = currentEmail)
                                                    activityViewModel.loadSummary(email = currentEmail)
                                                }
                                            )
                                        }
                                        currentScreen = ScreenState.START_ACTIVITY
                                    }
                                )
                            }
                            ScreenState.RUNNING_TRACKING -> {
                                RunningTrackingScreen(
                                    targetGoalTab = targetGoalTabState,
                                    targetMinutes = targetGoalMinutesState,
                                    targetDistanceKm = targetGoalDistanceKmState,
                                    userWeightKg = userWeightKg.toFloatOrNull() ?: 70.0f,
                                    onBackClick = { currentScreen = ScreenState.START_ACTIVITY },
                                    onFinishTracking = {
                                        currentScreen = ScreenState.START_ACTIVITY
                                    },
                                    onFinishTrackingWithData = { dur, dist, cal, pace, hr, steps ->
                                        val sessionUuid = java.util.UUID.randomUUID().toString()
                                        val targetType = targetGoalTabState.lowercase()
                                        val targetVal = if (targetGoalTabState == "Distance") targetGoalDistanceKmState.toInt() else targetGoalMinutesState
                                        val todayDate = repository.getTodayDateString()

                                        val record = ActivitySessionRecord(
                                            activityType = "Running",
                                            durationSeconds = dur,
                                            distanceKm = dist,
                                            caloriesBurned = cal,
                                            dateString = todayDate,
                                            sessionUuid = sessionUuid,
                                            targetType = targetType,
                                            targetValue = targetVal,
                                            steps = steps,
                                            avgPace = pace,
                                            trackingSource = "phone_sensor"
                                        )
                                        repository.saveActivitySessionRecord(record)
                                        nutritionRefreshTrigger++

                                        if (currentEmail.isNotBlank()) {
                                            activityViewModel.recordSession(
                                                email = currentEmail,
                                                activityType = "Running",
                                                durationSeconds = dur,
                                                distanceKm = dist,
                                                caloriesBurned = cal,
                                                avgPace = pace,
                                                avgHeartRate = hr ?: 148,
                                                steps = steps,
                                                sessionUuid = sessionUuid,
                                                targetType = targetType,
                                                targetValue = targetVal,
                                                trackingSource = "phone_sensor",
                                                onComplete = {
                                                    vitalsViewModel.fetchSummary(email = currentEmail)
                                                    activityViewModel.loadSummary(email = currentEmail)
                                                }
                                            )
                                        }
                                        currentScreen = ScreenState.START_ACTIVITY
                                    }
                                )
                            }
                            ScreenState.CYCLING_TRACKING -> {
                                CyclingTrackingScreen(
                                    targetGoalTab = targetGoalTabState,
                                    targetMinutes = targetGoalMinutesState,
                                    targetDistanceKm = targetGoalDistanceKmState,
                                    userWeightKg = userWeightKg.toFloatOrNull() ?: 70.0f,
                                    onBackClick = { currentScreen = ScreenState.START_ACTIVITY },
                                    onFinishTracking = {
                                        currentScreen = ScreenState.START_ACTIVITY
                                    },
                                    onFinishTrackingWithData = { dur, dist, cal, pace, hr, steps ->
                                        val sessionUuid = java.util.UUID.randomUUID().toString()
                                        val targetType = targetGoalTabState.lowercase()
                                        val targetVal = if (targetGoalTabState == "Distance") targetGoalDistanceKmState.toInt() else targetGoalMinutesState
                                        val todayDate = repository.getTodayDateString()

                                        val record = ActivitySessionRecord(
                                            activityType = "Cycling",
                                            durationSeconds = dur,
                                            distanceKm = dist,
                                            caloriesBurned = cal,
                                            dateString = todayDate,
                                            sessionUuid = sessionUuid,
                                            targetType = targetType,
                                            targetValue = targetVal,
                                            steps = steps,
                                            avgPace = pace,
                                            trackingSource = "gps"
                                        )
                                        repository.saveActivitySessionRecord(record)
                                        nutritionRefreshTrigger++

                                        if (currentEmail.isNotBlank()) {
                                            activityViewModel.recordSession(
                                                email = currentEmail,
                                                activityType = "Cycling",
                                                durationSeconds = dur,
                                                distanceKm = dist,
                                                caloriesBurned = cal,
                                                avgPace = pace,
                                                avgHeartRate = hr ?: 138,
                                                steps = steps,
                                                sessionUuid = sessionUuid,
                                                targetType = targetType,
                                                targetValue = targetVal,
                                                trackingSource = "gps",
                                                onComplete = {
                                                    vitalsViewModel.fetchSummary(email = currentEmail)
                                                    activityViewModel.loadSummary(email = currentEmail)
                                                }
                                            )
                                        }
                                        currentScreen = ScreenState.START_ACTIVITY
                                    }
                                )
                            }
                            ScreenState.WORKOUT_TRACKING -> {
                                WorkoutTrackingScreen(
                                    onBackClick = { currentScreen = ScreenState.START_ACTIVITY },
                                    onFinishTracking = {
                                        currentScreen = ScreenState.START_ACTIVITY
                                    },
                                    onFinishTrackingWithData = { dur, dist, cal, pace, hr, steps ->
                                        val sessionUuid = java.util.UUID.randomUUID().toString()
                                        val todayDate = repository.getTodayDateString()
                                        val record = ActivitySessionRecord(
                                            activityType = "Workout",
                                            durationSeconds = dur,
                                            distanceKm = dist,
                                            caloriesBurned = cal,
                                            dateString = todayDate,
                                            sessionUuid = sessionUuid,
                                            targetType = "time",
                                            targetValue = 45,
                                            steps = steps,
                                            avgPace = pace,
                                            trackingSource = "phone_sensor"
                                        )
                                        repository.saveActivitySessionRecord(record)
                                        nutritionRefreshTrigger++

                                        if (currentEmail.isNotBlank()) {
                                            activityViewModel.recordSession(
                                                email = currentEmail,
                                                activityType = "WORKOUT",
                                                durationSeconds = dur,
                                                distanceKm = dist,
                                                caloriesBurned = cal,
                                                avgPace = pace,
                                                avgHeartRate = hr ?: 125,
                                                steps = steps,
                                                sessionUuid = sessionUuid,
                                                targetType = "time",
                                                targetValue = 45,
                                                trackingSource = "phone_sensor",
                                                onComplete = {
                                                    vitalsViewModel.fetchSummary(email = currentEmail)
                                                    activityViewModel.loadSummary(email = currentEmail)
                                                }
                                            )
                                        }
                                        currentScreen = ScreenState.START_ACTIVITY
                                    }
                                )
                            }
                            ScreenState.VIEW_HISTORY -> {
                                val localRecords = repository.getActivitySessionRecords()
                                val remoteRecords = activitySessionsState.sessions.map { s ->
                                    ActivitySessionRecord(
                                        activityType = s.activityType.replaceFirstChar { it.uppercase() },
                                        durationSeconds = s.durationSeconds,
                                        distanceKm = s.distanceKm,
                                        caloriesBurned = s.caloriesBurned,
                                        dateString = s.startTime.split(" ").firstOrNull() ?: repository.getTodayDateString(),
                                        sessionUuid = s.sessionUuid,
                                        targetType = s.targetType,
                                        targetValue = s.targetValue,
                                        steps = s.steps,
                                        avgPace = s.avgPace,
                                        trackingSource = s.trackingSource
                                    )
                                }
                                val combinedRecords = (localRecords + remoteRecords).distinctBy {
                                    if (it.sessionUuid.isNotBlank()) it.sessionUuid else "${it.activityType}_${it.durationSeconds}_${it.distanceKm}"
                                }
                                ViewHistoryScreen(
                                    sessionRecords = combinedRecords,
                                    onBackClick = { currentScreen = viewHistoryPreviousScreen }
                                )
                            }
                            ScreenState.SLEEP_DETAIL -> {
                                SleepDetailScreen(
                                    showAiInsights = appSettingsState.aiInsights,
                                    onBackClick = {
                                        homeInitialTab = 0
                                        currentScreen = ScreenState.MAIN
                                    }
                                )
                            }
                            ScreenState.HYDRATION_DETAIL -> {
                                HydrationDetailScreen(
                                    viewModel = hydrationViewModel,
                                    userEmail = currentEmail,
                                    onBackClick = {
                                        homeInitialTab = 0
                                        currentScreen = ScreenState.MAIN
                                    }
                                )
                            }
                            ScreenState.BLOOD_PRESSURE_DETAIL -> {
                                BloodPressureDetailScreen(
                                    systolic = bpSummary?.systolic ?: 0,
                                    diastolic = bpSummary?.diastolic ?: 0,
                                    status = if (bpSummary != null && bpSummary.systolic > 0) bpSummary.status else "No data available",
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
                                    initialPercentage = spo2Summary?.percentage ?: 0,
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
                                    stepsCurrent = stepsSummary?.current ?: 0,
                                    stepsGoal = if (stepsSummary != null && stepsSummary.goal > 0) stepsSummary.goal else 8000,
                                    distanceKm = stepsSummary?.distanceKm ?: 0f,
                                    caloriesBurned = stepsSummary?.caloriesKcal ?: 0,
                                    activeMinutes = 0,
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
                                    bmiValue = calculatedBmiValue,
                                    bmiCategory = calculatedBmiCategory,
                                    heightCm = if (userHeightCm.isNotBlank()) userHeightCm else "--",
                                    weightKg = if (userWeightKg.isNotBlank()) userWeightKg else "--",
                                    onBackClick = {
                                        homeInitialTab = 0
                                        currentScreen = ScreenState.MAIN
                                    },
                                    onUpdateMetricsClick = { currentScreen = ScreenState.UPDATE_METRICS }
                                )
                            }
                            ScreenState.UPDATE_METRICS -> {
                                UpdateMetricsScreen(
                                    initialHeightCm = if (userHeightCm.isNotBlank() && userHeightCm != "--") userHeightCm else "175",
                                    initialWeightKg = if (userWeightKg.isNotBlank() && userWeightKg != "--") userWeightKg else "68.5",
                                    onBackClick = { currentScreen = ScreenState.BMI_DETAIL },
                                    onSaveMetricsSuccess = { bmi, category, h, w ->
                                        calculatedBmiValue = bmi
                                        calculatedBmiCategory = category
                                        userHeightCm = h
                                        userWeightKg = w

                                        val heightDouble = h.toDoubleOrNull() ?: 175.0
                                        val weightDouble = w.toDoubleOrNull() ?: 68.5

                                        profileViewModel.updatePersonalInformation(
                                            newName = globalUserName,
                                            newAge = globalUserAge.toIntOrNull() ?: 28,
                                            newHeightCm = heightDouble,
                                            newWeightKg = weightDouble,
                                            email = currentEmail
                                        )

                                        val bmiFloat = bmi.toFloatOrNull() ?: 22.4f
                                        vitalsViewModel.addReading(
                                            readingType = "bmi",
                                            valueNumeric = bmiFloat,
                                            valueText = bmi,
                                            unit = "bmi",
                                            statusLabel = category,
                                            email = currentEmail
                                        )

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
                                val currentReadingValue = when (activeReadingType) {
                                    VitalReadingType.HEART_RATE -> "76 BPM"
                                    VitalReadingType.BLOOD_PRESSURE -> "120/80 mmHg"
                                    VitalReadingType.SPO2 -> "98%"
                                    VitalReadingType.HYDRATION -> "+250 ml"
                                    VitalReadingType.STEPS -> "6,245 steps"
                                    VitalReadingType.SLEEP -> "7h 32m"
                                }
                                val currentReadingStatus = "Recorded"

                                AddReadingScreen(
                                    readingType = activeReadingType,
                                    readingValue = currentReadingValue,
                                    readingStatus = currentReadingStatus,
                                    onBackClick = {
                                        currentScreen = ScreenState.VITAL_SIGNS
                                    },
                                    onDoneClick = {
                                        when (activeReadingType) {
                                            VitalReadingType.HEART_RATE -> {
                                                vitalsViewModel.addReading(
                                                    readingType = "heart_rate",
                                                    valueNumeric = 76f,
                                                    valueText = "76 BPM",
                                                    unit = "bpm",
                                                    statusLabel = "Normal",
                                                    email = currentEmail
                                                )
                                            }
                                            VitalReadingType.BLOOD_PRESSURE -> {
                                                vitalsViewModel.addReading(
                                                    readingType = "blood_pressure",
                                                    valueNumeric = 120f,
                                                    valueSecondary = 80f,
                                                    valueText = "120/80 mmHg",
                                                    unit = "mmHg",
                                                    statusLabel = "Normal",
                                                    email = currentEmail
                                                )
                                            }
                                            VitalReadingType.SPO2 -> {
                                                vitalsViewModel.addReading(
                                                    readingType = "spo2",
                                                    valueNumeric = 98f,
                                                    valueText = "98%",
                                                    unit = "%",
                                                    statusLabel = "Optimal",
                                                    email = currentEmail
                                                )
                                            }
                                            VitalReadingType.HYDRATION -> {
                                                hydrationViewModel.addWater(currentEmail, 250, repository.getTodayDateString())
                                                vitalsViewModel.addReading(
                                                    readingType = "hydration",
                                                    valueNumeric = 250f,
                                                    valueText = "+250 ml",
                                                    unit = "ml",
                                                    statusLabel = "Logged",
                                                    email = currentEmail
                                                )
                                            }
                                            VitalReadingType.STEPS -> {
                                                vitalsViewModel.addReading(
                                                    readingType = "steps",
                                                    valueNumeric = 6245f,
                                                    valueText = "6,245 steps",
                                                    unit = "steps",
                                                    statusLabel = "Target Pace",
                                                    email = currentEmail
                                                )
                                            }
                                            VitalReadingType.SLEEP -> {
                                                vitalsViewModel.addReading(
                                                    readingType = "sleep",
                                                    valueNumeric = 7.5f,
                                                    valueText = "7h 32m",
                                                    unit = "hours",
                                                    statusLabel = "Good Quality",
                                                    email = currentEmail
                                                )
                                            }
                                        }
                                        currentScreen = ScreenState.VITAL_SIGNS
                                    }
                                )
                            }
                        }
                    }

                    if (showBlePairingSheet) {
                        BlePairingBottomSheet(
                            deviceManager = deviceManager,
                            onDismiss = { showBlePairingSheet = false }
                        )
                    }
                }
            }
        }
    }
}
