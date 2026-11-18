package com.simats.vitalsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import com.simats.vitalsync.ui.theme.AppThemeMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import com.simats.vitalsync.util.WeeklyReportPdfGenerator
import com.simats.vitalsync.data.model.profile.VitalPerformanceBreakdown
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
import com.simats.vitalsync.ui.vitals.LogBloodPressureSheet
import com.simats.vitalsync.ui.vitals.LogHeartRateSheet
import com.simats.vitalsync.ui.vitals.LogSpo2Sheet
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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import com.simats.vitalsync.data.sensor.StepSensorManager
import com.simats.vitalsync.data.local.SessionManager
import com.simats.vitalsync.data.network.RetrofitClient
import com.simats.vitalsync.data.model.ai.AiInsightResponse
import com.simats.vitalsync.data.model.ai.MetricEvaluationDto
import com.simats.vitalsync.data.model.vitals.evaluateBloodPressure
import com.simats.vitalsync.ui.theme.SleepColorStandards
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Locale
import kotlin.math.roundToInt

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
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Configure edge-to-edge system bars so content and themes flow properly
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.show(WindowInsetsCompat.Type.systemBars())

        val repository = FitnessRepository.getInstance(applicationContext)
        val sessionManager = SessionManager.getInstance(applicationContext)
        RetrofitClient.authHeaderProvider = {
            val user = sessionManager.getUser()
            if (user != null && user.id > 0) user.id.toString() else sessionManager.getUserEmail().ifBlank { null }
        }

        setContent {
            var currentThemeMode by remember { mutableStateOf(sessionManager.getThemeMode()) }
            val isSystemDark = isSystemInDarkTheme()
            val isEffectiveDark = when (currentThemeMode) {
                AppThemeMode.SYSTEM -> isSystemDark
                AppThemeMode.DARK -> true
                AppThemeMode.LIGHT -> false
            }

            DisposableEffect(isEffectiveDark) {
                enableEdgeToEdge(
                    statusBarStyle = if (isEffectiveDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isEffectiveDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    }
                )
                window.statusBarColor = if (isEffectiveDark) {
                    android.graphics.Color.parseColor("#0F0F0F")
                } else {
                    android.graphics.Color.parseColor("#F7FBF8")
                }
                window.navigationBarColor = if (isEffectiveDark) {
                    android.graphics.Color.parseColor("#0F0F0F")
                } else {
                    android.graphics.Color.parseColor("#FFFFFF")
                }
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = !isEffectiveDark
                insetsController.isAppearanceLightNavigationBars = !isEffectiveDark
                onDispose { }
            }

            VitalsyncTheme(themeMode = currentThemeMode, darkTheme = isEffectiveDark) {
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
                    factory = ActivityViewModelFactory(applicationContext)
                )
                val aiViewModel: AiViewModel = viewModel(
                    factory = AiViewModelFactory(application)
                )
                val aiUiState by aiViewModel.uiState.collectAsState()

                val stepSensorManager = remember { StepSensorManager.getInstance(applicationContext) }
                val livePhoneSteps by stepSensorManager.liveSteps.collectAsState()
                val livePhoneDistanceKm by stepSensorManager.liveDistanceKm.collectAsState()
                val livePhoneCalories by stepSensorManager.liveCalories.collectAsState()
                val isPhoneSensorActive by stepSensorManager.isSensorActive.collectAsState()

                val activityRecognitionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { _ ->
                    stepSensorManager.startTracking()
                }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    Log.d("FCM", "Notification permission granted: $isGranted")
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                            activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                        } else {
                            stepSensorManager.startTracking()
                        }
                    } else {
                        stepSensorManager.startTracking()
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                            return@addOnCompleteListener
                        }
                        val token = task.result
                        Log.d("FCM", "Token: $token")
                    }
                }
                val hrUiState by heartRateViewModel.uiState.collectAsState()
                val activitySummaryState by activityViewModel.summaryState.collectAsState()
                val activityStepGoalState by activityViewModel.stepGoalState.collectAsState()
                val activitySessionsState by activityViewModel.sessionsState.collectAsState()
                var activeVitalSheet by remember { mutableStateOf<VitalReadingType?>(null) }

                // Current logged-in user (observed reactively across login, signup, profile updates, and logout)
                val currentUser by authViewModel.currentUser.collectAsState()
                val currentEmail = currentUser?.email ?: ""
                var lastLoadedEmail by remember { mutableStateOf<String?>(null) }

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
                val manualLogState by vitalsViewModel.manualLogState.collectAsState()
                val hrDetailState by vitalsViewModel.hrDetailState.collectAsState()
                val spo2DetailState by vitalsViewModel.spo2DetailState.collectAsState()
                val bpDetailState by vitalsViewModel.bpDetailState.collectAsState()
                val vitalsAiInsight by vitalsViewModel.aiInsightState.collectAsState()
                val isVitalsAiLoading by vitalsViewModel.isAiInsightLoading.collectAsState()

                LaunchedEffect(currentEmail) {
                    if (currentEmail.isNotBlank()) {
                        if (lastLoadedEmail != null && !lastLoadedEmail.equals(currentEmail, ignoreCase = true)) {
                            // Clear cached viewmodels/prefs when switching directly from one account to another
                            repository.clearAllUserLocalData()
                            stepSensorManager.resetForAccountSwitch()
                            profileViewModel.resetState()
                            vitalsViewModel.resetState()
                            hydrationViewModel.resetState()
                            heartRateViewModel.resetState()
                            activityViewModel.resetState()
                            aiViewModel.resetState()
                            gymViewModel.resetState()
                            nutritionViewModel.resetState()
                        }
                        lastLoadedEmail = currentEmail

                        // Launch critical startup telemetry asynchronously without blocking UI render
                        profileViewModel.fetchProfile(email = currentEmail)
                        vitalsViewModel.fetchSummary(email = currentEmail)
                        activityViewModel.loadSummary(email = currentEmail)
                        activityViewModel.loadStepGoal(email = currentEmail)
                        hydrationViewModel.loadHydration(currentEmail, repository.getTodayDateString())

                        // Launch secondary optional telemetry in background
                        launch {
                            val initUid = currentUser?.id ?: profileState.userId.takeIf { it > 0 }
                            profileViewModel.fetchHealthGoals(email = currentEmail)
                            profileViewModel.fetchAppSettings(email = currentEmail)
                            profileViewModel.fetchWeeklyHealthReport(userId = initUid, email = currentEmail)
                            aiViewModel.loadDailyInsight(userId = initUid)
                            aiViewModel.loadWeeklyTrends(userId = initUid, email = currentEmail)
                            heartRateViewModel.fetchAnalytics(currentEmail)
                            activityViewModel.loadSessions(email = currentEmail)
                        }
                    }
                }

                val startupNetworkError = listOfNotNull(
                    vitalsSummaryState.errorMessage,
                    activitySummaryState.errorMessage,
                    profileState.errorMessage
                ).firstOrNull { it.isNotBlank() }

                var currentScreen by rememberSaveable { mutableStateOf(ScreenState.SPLASH) }
                var isNewUser by rememberSaveable { mutableStateOf(false) }
                var activeReadingType by remember { mutableStateOf(VitalReadingType.HEART_RATE) }
                var homeInitialTab by rememberSaveable { mutableIntStateOf(0) }
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
                val profileAvatarBitmap by profileViewModel.avatarBitmap.collectAsState()

                LaunchedEffect(profileAvatarBitmap) {
                    globalProfilePhotoBitmap = profileAvatarBitmap
                }

                LaunchedEffect(currentUser?.id, currentUser?.imageUrl, currentUser?.profileImage) {
                    if (currentUser != null) {
                        profileViewModel.loadAvatarFromUser(currentUser)
                    } else {
                        globalProfilePhotoBitmap = null
                    }
                }

                val performFullAccountCleanup: () -> Unit = {
                    authViewModel.logout()
                    repository.clearAllUserLocalData()
                    stepSensorManager.resetForAccountSwitch()
                    profileViewModel.resetState()
                    vitalsViewModel.resetState()
                    hydrationViewModel.resetState()
                    heartRateViewModel.resetState()
                    activityViewModel.resetState()
                    aiViewModel.resetState()
                    gymViewModel.resetState()
                    nutritionViewModel.resetState()
                    globalUserName = ""
                    globalUserInitials = "--"
                    globalUserAge = ""
                    userHeightCm = ""
                    userWeightKg = ""
                    calculatedBmiValue = "--"
                    calculatedBmiCategory = "--"
                    globalProfilePhotoBitmap = null
                    isNewUser = false
                    homeInitialTab = 0
                    lastLoadedEmail = null
                }

                // Reactive Recomposition Trigger for Nutrition Meals
                var nutritionRefreshTrigger by remember { mutableIntStateOf(0) }

                LaunchedEffect(currentScreen) {
                    if (currentEmail.isNotBlank()) {
                        when (currentScreen) {
                            ScreenState.AI_CHAT -> {
                                aiViewModel.loadSessionsAndInitialChat(
                                    userId = profileState.userId.takeIf { it > 0 },
                                    email = currentEmail
                                )
                            }
                            ScreenState.AI_CHAT_HISTORY -> {
                                aiViewModel.loadSessions(
                                    userId = profileState.userId.takeIf { it > 0 },
                                    email = currentEmail
                                )
                            }
                            ScreenState.MAIN -> {
                                val screenUid = profileState.userId.takeIf { it > 0 } ?: currentUser?.id
                                vitalsViewModel.fetchSummary(email = currentEmail)
                                activityViewModel.loadSummary(email = currentEmail)
                                profileViewModel.fetchWeeklyHealthReport(userId = screenUid, email = currentEmail)
                                aiViewModel.loadWeeklyTrends(userId = screenUid, email = currentEmail)
                            }
                            ScreenState.VITAL_SIGNS, ScreenState.START_ACTIVITY -> {
                                vitalsViewModel.fetchSummary(email = currentEmail)
                                activityViewModel.loadSummary(email = currentEmail)
                            }
                            ScreenState.WEEKLY_REPORT -> {
                                val screenUid = profileState.userId.takeIf { it > 0 } ?: currentUser?.id
                                vitalsViewModel.fetchSummary(email = currentEmail)
                                activityViewModel.loadSummary(email = currentEmail)
                                profileViewModel.fetchWeeklyHealthReport(userId = screenUid, email = currentEmail)
                                aiViewModel.loadWeeklyTrends(userId = screenUid, email = currentEmail)
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
                    NotificationReminderEngine.syncHydrationReminder(
                        context = context,
                        enabled = sessionManager.isHydrationReminderEnabled(),
                        mode = sessionManager.getHydrationReminderMode(),
                        timeStr = sessionManager.getHydrationReminderTime()
                    )
                    NotificationReminderEngine.syncStepsReminder(
                        context = context,
                        enabled = sessionManager.isStepsReminderEnabled(),
                        mode = sessionManager.getStepsReminderMode(),
                        timeStr = sessionManager.getStepsReminderTime()
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

                val todayPrefix = remember(currentScreen, nutritionRefreshTrigger) { repository.getTodayDateString() }

                val hasTodayHrSummary = hrSummary != null && hrSummary.bpm > 0 &&
                    (hrSummary.hasData || hrSummary.recordedAt.startsWith(todayPrefix))
                val effectiveHrBpm: Int? = if (hasTodayHrSummary) hrSummary!!.bpm else null
                val heartRateFormatted = effectiveHrBpm?.let { "$it BPM" } ?: "-- BPM"
                val heartRateStatusFormatted = if (hasTodayHrSummary) hrSummary!!.status else "Tap to log"
                val heartRateTrendFormatted = if (hasTodayHrSummary) hrSummary!!.trend else null

                val hasTodaySpo2Summary = spo2Summary != null && spo2Summary.percentage > 0 &&
                    (spo2Summary.hasData || spo2Summary.recordedAt.startsWith(todayPrefix))
                val effectiveSpo2Pct: Int? = if (hasTodaySpo2Summary) spo2Summary!!.percentage else null
                val spo2Formatted = effectiveSpo2Pct?.let { "$it%" } ?: "--%"
                val spo2StatusFormatted = if (hasTodaySpo2Summary) spo2Summary!!.status else "Tap to log"
                val spo2TrendFormatted = if (hasTodaySpo2Summary) spo2Summary!!.trend else null

                val hasTodayBpSummary = bpSummary != null && bpSummary.systolic > 0 && bpSummary.diastolic > 0 &&
                    (bpSummary.hasData || bpSummary.recordedAt.startsWith(todayPrefix))
                val bpFormatted = if (hasTodayBpSummary) {
                    "${bpSummary!!.systolic}/${bpSummary.diastolic} mmHg"
                } else {
                    "--/-- mmHg"
                }
                val bpStatusFormatted = if (hasTodayBpSummary) {
                    evaluateBloodPressure(bpSummary!!.systolic, bpSummary.diastolic).statusTitle
                } else {
                    "Tap to log"
                }
                val bpTrendFormatted = if (hasTodayBpSummary) bpSummary!!.trend else null

                val todayRecords = remember(currentScreen, nutritionRefreshTrigger) {
                    val today = repository.getTodayDateString()
                    repository.getActivitySessionRecords().filter { it.dateString == today || it.dateString == "2025-10-15" }
                }
                val localTodaySteps = todayRecords.sumOf { it.steps }
                val localTodayDistance = todayRecords.map { it.distanceKm }.sum()
                val localTodayCalories = todayRecords.sumOf { it.caloriesBurned }
                val localTodayMinutes = todayRecords.sumOf { it.durationSeconds } / 60

                // Seed sensor with existing verified steps so counter never drops below recorded workouts or backend
                LaunchedEffect(stepsSummary?.current, localTodaySteps) {
                    val verified = maxOf(stepsSummary?.current ?: 0, localTodaySteps)
                    if (verified > 0) {
                        stepSensorManager.seedBaseSteps(verified)
                    }
                }

                var persistedStepGoal by remember(currentEmail) {
                    mutableIntStateOf(sessionManager.getSavedStepGoal() ?: repository.getSavedStepGoal() ?: 0)
                }

                LaunchedEffect(activityStepGoalState.isGoalLoaded, activityStepGoalState.dailyStepGoal) {
                    if (activityStepGoalState.isGoalLoaded && activityStepGoalState.dailyStepGoal >= 1000) {
                        persistedStepGoal = activityStepGoalState.dailyStepGoal
                        sessionManager.saveStepGoal(activityStepGoalState.dailyStepGoal)
                        repository.saveStepGoal(activityStepGoalState.dailyStepGoal)
                    }
                }

                LaunchedEffect(profileState.targetSteps) {
                    val pGoal = profileState.targetSteps
                    if (pGoal != null && pGoal >= 1000 && persistedStepGoal < 1000) {
                        persistedStepGoal = pGoal
                        sessionManager.saveStepGoal(pGoal)
                        repository.saveStepGoal(pGoal)
                    }
                }

                val effectiveGoal = when {
                    persistedStepGoal >= 1000 -> persistedStepGoal
                    activityStepGoalState.isGoalLoaded && activityStepGoalState.dailyStepGoal >= 1000 -> activityStepGoalState.dailyStepGoal
                    profileState.targetSteps != null && profileState.targetSteps!! >= 1000 -> profileState.targetSteps!!
                    stepsSummary != null && stepsSummary.goal > 0 -> stepsSummary.goal
                    (activitySummaryState.summary?.steps?.goal ?: 0) > 0 -> activitySummaryState.summary!!.steps.goal
                    else -> 8000
                }

                // Auto-sync step milestones with backend
                LaunchedEffect(currentEmail, effectiveGoal) {
                    if (currentEmail.isNotBlank()) {
                        stepSensorManager.onStepThresholdReached = { steps, dist, cal ->
                            activityViewModel.syncDailySteps(
                                email = currentEmail,
                                stepCount = steps,
                                targetSteps = effectiveGoal,
                                distanceKm = dist,
                                caloriesKcal = cal,
                                activeMinutes = steps / 100
                            )
                        }
                    }
                }

                val hasPhoneLiveSteps = livePhoneSteps > 0

                val currentEffectiveSteps = maxOf(
                    livePhoneSteps,
                    stepsSummary?.current ?: 0,
                    localTodaySteps
                )

                val effectiveProgressPercent = if (effectiveGoal > 0) {
                    minOf(100, ((currentEffectiveSteps.toFloat() / effectiveGoal) * 100).toInt())
                } else 0

                val effectiveDistance = if (currentEffectiveSteps > 0) {
                    maxOf(livePhoneDistanceKm, stepsSummary?.distanceKm ?: 0f, localTodayDistance, currentEffectiveSteps * 0.00076f)
                } else {
                    maxOf(stepsSummary?.distanceKm ?: 0f, localTodayDistance)
                }

                val effectiveCalories = if (currentEffectiveSteps > 0) {
                    maxOf(livePhoneCalories, stepsSummary?.caloriesKcal ?: 0, localTodayCalories, (currentEffectiveSteps * 0.04f).toInt())
                } else {
                    maxOf(stepsSummary?.caloriesKcal ?: 0, localTodayCalories)
                }

                val effectiveActiveMinutes = maxOf(
                    activitySummaryState.summary?.activeMinutes?.current ?: 0,
                    localTodayMinutes,
                    currentEffectiveSteps / 100
                )

                val stepsFormatted = if (currentEffectiveSteps > 0) {
                    String.format(Locale.US, "%,d", currentEffectiveSteps)
                } else {
                    "--"
                }

                val stepsStatusFormatted = when {
                    currentEffectiveSteps > 0 -> "$effectiveProgressPercent% of goal"
                    hasPhoneLiveSteps -> "Live Sensor"
                    else -> "No data"
                }

                val totalDisplaySteps = currentEffectiveSteps
                val totalDisplayDistance = effectiveDistance
                val totalDisplayCalories = effectiveCalories
                val totalDisplayMinutes = effectiveActiveMinutes

                val activityStepsFormatted = if (totalDisplaySteps > 0) String.format(Locale.US, "%,d steps", totalDisplaySteps) else "-- steps"
                val activityDistFormatted = if (totalDisplayDistance > 0f) String.format(Locale.US, "%.1f km", totalDisplayDistance) else "-- km"
                val activityCalFormatted = if (totalDisplayCalories > 0) "$totalDisplayCalories kcal" else "-- kcal"
                val activityMinutesFormatted = if (totalDisplayMinutes > 0) "$totalDisplayMinutes min" else "-- min"

                val hydState by hydrationViewModel.hydrationState.collectAsState()
                val localSleepRecord = remember(currentScreen, nutritionRefreshTrigger) {
                    repository.getSleepRecord(repository.getTodayDateString())
                }

                val todaySleepMinutes = when {
                    sleepSummary != null && sleepSummary.durationMinutes > 0 -> sleepSummary.durationMinutes
                    localSleepRecord != null && localSleepRecord.durationMinutes > 0 -> localSleepRecord.durationMinutes
                    else -> 0
                }

                val sleepDurationFormatted = when {
                    todaySleepMinutes > 0 -> "${todaySleepMinutes / 60}h ${todaySleepMinutes % 60}m"
                    else -> "--"
                }

                val (evaluatedSleepLabel, evaluatedSleepScore) = remember(todaySleepMinutes, sleepSummary, localSleepRecord) {
                    if (todaySleepMinutes <= 0) {
                        "No sleep logged" to 0
                    } else {
                        val explicitScore = when {
                            sleepSummary != null && sleepSummary.sleepScore > 0 -> sleepSummary.sleepScore
                            localSleepRecord != null && localSleepRecord.sleepScore > 0 -> localSleepRecord.sleepScore
                            else -> 0
                        }
                        val derivedScore = SleepColorStandards.deriveSleepScore(todaySleepMinutes)
                        val pres = SleepColorStandards.getPresentation(todaySleepMinutes, explicitScore)
                        val explicitLabel = when {
                            sleepSummary != null && sleepSummary.quality.isNotBlank() &&
                                (sleepSummary.quality.equals("Excellent", ignoreCase = true) ||
                                    sleepSummary.quality.equals("Optimal", ignoreCase = true) ||
                                    sleepSummary.quality.equals("Fair", ignoreCase = true) ||
                                    sleepSummary.quality.equals("Poor", ignoreCase = true)) -> sleepSummary.quality
                            localSleepRecord != null && localSleepRecord.qualityLabel.isNotBlank() &&
                                (localSleepRecord.qualityLabel.equals("Excellent", ignoreCase = true) ||
                                    localSleepRecord.qualityLabel.equals("Optimal", ignoreCase = true) ||
                                    localSleepRecord.qualityLabel.equals("Fair", ignoreCase = true) ||
                                    localSleepRecord.qualityLabel.equals("Poor", ignoreCase = true)) -> localSleepRecord.qualityLabel
                            else -> pres.qualityLabel
                        }
                        explicitLabel to (if (explicitScore > 0) explicitScore else derivedScore)
                    }
                }

                val sleepCategoryLabel = evaluatedSleepLabel

                val sleepScoreFormatted = when {
                    todaySleepMinutes > 0 && evaluatedSleepScore > 0 ->
                        "$sleepCategoryLabel ($evaluatedSleepScore score)"
                    todaySleepMinutes > 0 -> sleepCategoryLabel
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

                val wellnessScoreVal = (
                    minOf(35f, if (effectiveGoal > 0) (currentEffectiveSteps.toFloat() / effectiveGoal) * 35f else 0f) +
                    minOf(35f, if (targetWaterMl > 0) (consumedWaterMl.toFloat() / targetWaterMl) * 35f else 0f) +
                    minOf(30f, (todaySleepMinutes.toFloat() / 480f) * 30f)
                ).roundToInt().coerceIn(0, 100)

                val wellnessStatusVal = when {
                    currentEffectiveSteps == 0 && consumedWaterMl == 0 && todaySleepMinutes == 0 -> "No data recorded"
                    wellnessScoreVal >= 80 -> "Optimal State"
                    wellnessScoreVal >= 55 -> "Good Progress"
                    else -> "Needs Attention"
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

                val liveSleepScore = evaluatedSleepScore

                val hasMeasuredBiometricsToday = (effectiveHrBpm != null || effectiveSpo2Pct != null || hasTodayBpSummary)

                val localSynthesizedInsight = remember(
                    effectiveHrBpm,
                    effectiveSpo2Pct,
                    hasTodayBpSummary,
                    bpSummary,
                    currentEffectiveSteps,
                    effectiveGoal,
                    todaySleepMinutes,
                    consumedWaterMl,
                    targetWaterMl,
                    wellnessScoreVal
                ) {
                    val evaluations = mutableListOf<MetricEvaluationDto>()
                    val optimalClauses = mutableListOf<String>()
                    val concernClauses = mutableListOf<String>()
                    var lowestMetricName = "Hydration"
                    var lowestSeverity = -1
                    var lowestNormScore = 101f
                    var lowestActionTip = "Keep building steady hydration, movement, and sleep habits today."
                    var hasAlert = false

                    fun registerMetric(
                        metric: String,
                        valueLabel: String,
                        status: String,
                        tier: String,
                        severity: Int,
                        normScore: Float,
                        clause: String,
                        action: String
                    ) {
                        evaluations += MetricEvaluationDto(metric, valueLabel, status, tier, severity)
                        if (severity == 0) {
                            optimalClauses += clause
                        } else {
                            concernClauses += clause
                        }
                        if (tier == "Alert" || tier == "Low / Alert") {
                            hasAlert = true
                        }
                        if (severity > lowestSeverity || (severity == lowestSeverity && normScore < lowestNormScore)) {
                            lowestSeverity = severity
                            lowestNormScore = normScore
                            lowestMetricName = metric
                            lowestActionTip = action
                        }
                    }

                    if (effectiveHrBpm != null) {
                        when {
                            effectiveHrBpm < 60 -> registerMetric(
                                "Heart Rate", "$effectiveHrBpm bpm", "Resting/Athletic", "Optimal", 0, 95f,
                                "Resting heart rate is Optimal (Resting/Athletic) at $effectiveHrBpm bpm.",
                                "Maintain your steady recovery pacing while keeping heart rate at $effectiveHrBpm bpm."
                            )
                            effectiveHrBpm <= 100 -> registerMetric(
                                "Heart Rate", "$effectiveHrBpm bpm", "Normal Resting", "Optimal", 0, 92f,
                                "Heart rate is Optimal (Normal Resting) at $effectiveHrBpm bpm.",
                                "Keep resting heart rate steady around $effectiveHrBpm bpm with light movement and hydration."
                            )
                            effectiveHrBpm <= 120 -> registerMetric(
                                "Heart Rate", "$effectiveHrBpm bpm", "Elevated", "Needs Attention", 1, 55f,
                                "Heart rate is Elevated at $effectiveHrBpm bpm, signaling mild strain or stress.",
                                "Take 5 minutes of slow nasal breathing and rehydrate to lower your $effectiveHrBpm bpm heart rate."
                            )
                            else -> registerMetric(
                                "Heart Rate", "$effectiveHrBpm bpm", "High", "Alert", 2, 20f,
                                "Heart rate is High (Alert) at $effectiveHrBpm bpm and warrants seated rest.",
                                "Pause strenuous effort, rest quietly, hydrate slowly, and recheck your $effectiveHrBpm bpm reading."
                            )
                        }
                    }

                    if (effectiveSpo2Pct != null) {
                        when {
                            effectiveSpo2Pct >= 95 -> registerMetric(
                                "SpO2", "$effectiveSpo2Pct%", "Optimal Oxygenation", "Optimal", 0, 98f,
                                "Blood oxygen is Optimal at $effectiveSpo2Pct% SpO2.",
                                "Blood oxygen is optimal at $effectiveSpo2Pct%; continue steady nasal breathing."
                            )
                            effectiveSpo2Pct >= 90 -> registerMetric(
                                "SpO2", "$effectiveSpo2Pct%", "Mild Hypoxia", "Needs Attention", 1, 50f,
                                "SpO2 shows Mild Hypoxia at $effectiveSpo2Pct%, below the 95% optimal threshold.",
                                "Move to a well-ventilated space, practice slow diaphragmatic breathing, and recheck $effectiveSpo2Pct% SpO2."
                            )
                            else -> registerMetric(
                                "SpO2", "$effectiveSpo2Pct%", "Low Oxygen", "Alert", 2, 15f,
                                "SpO2 is critically low at $effectiveSpo2Pct% (Alert) and requires prompt rest and monitoring.",
                                "Rest upright in fresh air immediately and seek medical evaluation if SpO2 remains below 92% ($effectiveSpo2Pct%)."
                            )
                        }
                    }

                    if (hasTodayBpSummary && bpSummary != null) {
                        val sys = bpSummary.systolic
                        val dia = bpSummary.diastolic
                        val bpVal = "$sys/$dia mmHg"
                        val bpEval = evaluateBloodPressure(sys, dia)
                        when (bpEval.statusTitle) {
                            "Low Blood Pressure (Hypotension)" -> registerMetric(
                                "Blood Pressure", bpVal, bpEval.statusTitle, bpEval.tier, 2,
                                if (sys < 80 || dia < 50) 10f else 18f,
                                "Blood pressure is Critically Low (Hypotension Alert) at $bpVal (< 90 systolic or < 60 diastolic).",
                                "Sit or lie down slowly, hydrate with fluids and electrolytes, and consult a healthcare provider if dizzy near $bpVal."
                            )
                            "Hypertensive Crisis" -> registerMetric(
                                "Blood Pressure", bpVal, bpEval.statusTitle, bpEval.tier, 2, 10f,
                                "Blood pressure sits in Hypertensive Crisis (Alert) at $bpVal.",
                                "Sit quietly for 5 minutes and recheck $bpVal; seek immediate medical attention if elevated pressure persists."
                            )
                            "Stage 2 Hypertension" -> registerMetric(
                                "Blood Pressure", bpVal, bpEval.statusTitle, bpEval.tier, 2, 25f,
                                "Blood pressure falls in Stage 2 Hypertension at $bpVal.",
                                "Limit sodium below 1,500 mg today, avoid heavy straining, and consult a healthcare provider regarding $bpVal."
                            )
                            "Stage 1 Hypertension" -> registerMetric(
                                "Blood Pressure", bpVal, bpEval.statusTitle, bpEval.tier, 1, 45f,
                                "Blood pressure sits in Stage 1 Hypertension at $bpVal.",
                                "Rest quietly for 10 minutes, keep sodium low today, and take a calm 20-minute walk to support $bpVal."
                            )
                            "Elevated Blood Pressure" -> registerMetric(
                                "Blood Pressure", bpVal, bpEval.statusTitle, bpEval.tier, 1, 65f,
                                "Blood pressure is Elevated at $bpVal (120\u2013129 systolic with diastolic below 80).",
                                "Choose potassium-rich foods over salty snacks and take a calm 15-minute walk to ease $bpVal."
                            )
                            else -> registerMetric(
                                "Blood Pressure", bpVal, bpEval.statusTitle, bpEval.tier, 0, 95f,
                                "Blood pressure is Optimal at $bpVal.",
                                "Maintain balanced meals and steady hydration to keep blood pressure near $bpVal."
                            )
                        }
                    }

                    val hydPctRaw = if (targetWaterMl > 0) (consumedWaterMl.toFloat() / targetWaterMl) * 100f else 0f
                    val remMl = maxOf(0, targetWaterMl - consumedWaterMl)
                    val waterFmt = String.format(Locale.US, "%,d", consumedWaterMl)
                    val targetWaterFmt = String.format(Locale.US, "%,d", targetWaterMl)
                    val remWaterFmt = String.format(Locale.US, "%,d", remMl)
                    when {
                        hydPctRaw >= 80f -> registerMetric(
                            "Hydration", "$waterFmt/$targetWaterFmt ml", "Well Hydrated", "Optimal", 0, minOf(100f, hydPctRaw),
                            "Hydration is Optimal at $waterFmt / $targetWaterFmt ml (${hydPctRaw.roundToInt()}%).",
                            "Finish the remaining $remWaterFmt ml gradually today to reach your $targetWaterFmt ml goal."
                        )
                        hydPctRaw >= 40f -> registerMetric(
                            "Hydration", "$waterFmt/$targetWaterFmt ml", "Below Target", "Needs Improvement", 1, hydPctRaw,
                            "Hydration needs improvement at $waterFmt / $targetWaterFmt ml ($remWaterFmt ml remaining).",
                            "Drink 400\u2013500 ml of water over the next 2 hours to close the $remWaterFmt ml gap toward $targetWaterFmt ml."
                        )
                        else -> registerMetric(
                            "Hydration", "$waterFmt/$targetWaterFmt ml", "Critically Dehydrated", "Low / Alert", 2, hydPctRaw,
                            "Hydration is Critically Low at $waterFmt / $targetWaterFmt ml ($remWaterFmt ml deficit).",
                            "Drink 500 ml water right now and aim for ${String.format(Locale.US, "%,d", maxOf(500, (remMl * 0.6f).roundToInt()))} ml more before evening to avoid fatigue."
                        )
                    }

                    val stepsPctRaw = if (effectiveGoal > 0) (currentEffectiveSteps.toFloat() / effectiveGoal) * 100f else 0f
                    val stepsFmt = String.format(Locale.US, "%,d", currentEffectiveSteps)
                    val stepGoalFmt = String.format(Locale.US, "%,d", effectiveGoal)
                    val remSteps = maxOf(0, effectiveGoal - currentEffectiveSteps)
                    val remStepsFmt = String.format(Locale.US, "%,d", remSteps)
                    val stepAddBlock = String.format(Locale.US, "%,d", maxOf(1000, minOf(remSteps, 2500)))
                    when {
                        stepsPctRaw >= 70f -> registerMetric(
                            "Activity", "$stepsFmt/$stepGoalFmt", "Active Day", "Optimal", 0, minOf(100f, stepsPctRaw),
                            "Activity is Optimal at $stepsFmt / $stepGoalFmt steps (${stepsPctRaw.roundToInt()}%).",
                            if (remSteps > 0) "Add a quick $remStepsFmt-step stroll to complete your $stepGoalFmt-step target." else "Maintain gentle stretching after exceeding your $stepGoalFmt-step goal."
                        )
                        stepsPctRaw >= 30f -> registerMetric(
                            "Activity", "$stepsFmt/$stepGoalFmt", "Moderate Activity", "Needs Improvement", 1, stepsPctRaw,
                            "Activity needs improvement at $stepsFmt / $stepGoalFmt steps ($remStepsFmt steps shy of goal).",
                            "Take a brisk 20-minute walk to add ~$stepAddBlock steps toward your $stepGoalFmt-step goal."
                        )
                        else -> registerMetric(
                            "Activity", "$stepsFmt/$stepGoalFmt", "Sedentary / Lagging", "Low", 2, stepsPctRaw,
                            "Activity is Low at $stepsFmt / $stepGoalFmt steps ($remStepsFmt steps remaining today).",
                            "Take a brisk 20-minute walk now to add ~$stepAddBlock steps toward your $stepGoalFmt-step goal."
                        )
                    }

                    val sleepHoursVal = todaySleepMinutes / 60.0
                    when {
                        todaySleepMinutes <= 0 -> {
                            evaluations += MetricEvaluationDto("Sleep", "Not logged", "Not Logged", "Not Logged", 0)
                        }
                        sleepHoursVal >= 7.0 -> registerMetric(
                            "Sleep", sleepDurationFormatted, "Fully Rested", "Optimal", 0, minOf(100f, ((sleepHoursVal / 8.0) * 100.0).toFloat()),
                            "Sleep is Optimal (Fully Rested) at $sleepDurationFormatted.",
                            "Keep your steady bedtime schedule to maintain 7.0+ hrs of Optimal sleep."
                        )
                        sleepHoursVal >= 5.5 -> registerMetric(
                            "Sleep", sleepDurationFormatted, "Mild Sleep Deficit", "Needs Improvement", 1, ((sleepHoursVal / 8.0) * 100.0).toFloat(),
                            "Sleep needs improvement (Mild Sleep Deficit) at $sleepDurationFormatted.",
                            "Wind down 45 minutes earlier tonight without screens to reach 7.5+ hrs of restorative sleep."
                        )
                        else -> registerMetric(
                            "Sleep", sleepDurationFormatted, "Severe Sleep Deprivation", "Low / Alert", 2, maxOf(15f, ((sleepHoursVal / 8.0) * 100.0).toFloat()),
                            "Sleep is Critically Low (Severe Sleep Deprivation) at $sleepDurationFormatted.",
                            "Prioritize an early bedtime tonight to hit 7.5\u20138.0 hrs and recover from severe sleep deficit."
                        )
                    }

                    val computedTier = when {
                        hasAlert -> "ALERT"
                        evaluations.any { it.severity >= 1 } -> "NEEDS_ATTENTION"
                        else -> "OPTIMAL"
                    }

                    val headline = when (computedTier) {
                        "OPTIMAL" -> if (hasMeasuredBiometricsToday) "Optimal Vitals & Habits" else "Strong Daily Balance"
                        "ALERT" -> if (lowestMetricName in listOf("Blood Pressure", "SpO2", "Heart Rate")) "$lowestMetricName Alert" else "Priority Recovery Alert"
                        else -> if (lowestSeverity >= 1) "$lowestMetricName Needs Focus" else "Steady Daily Progress"
                    }

                    val orderedSummaryParts = mutableListOf<String>()
                    if (optimalClauses.isNotEmpty()) {
                        orderedSummaryParts += optimalClauses.take(2).joinToString(" ")
                    }
                    if (concernClauses.isNotEmpty()) {
                        orderedSummaryParts += concernClauses.take(2).joinToString(" ")
                    }
                    if (todaySleepMinutes <= 0) {
                        orderedSummaryParts += "No sleep has been logged yet today."
                    }
                    if (!hasMeasuredBiometricsToday) {
                        orderedSummaryParts += "Tap '+ Log Vitals' to record Heart Rate, SpO2, or Blood Pressure for complete cardiovascular grading."
                    }

                    val finalTip = if (computedTier == "OPTIMAL" && !hasMeasuredBiometricsToday) {
                        "Great daily momentum! Tap '+ Log Vitals' on Home to record Heart Rate, SpO2, or Blood Pressure."
                    } else {
                        lowestActionTip
                    }

                    AiInsightResponse(
                        success = true,
                        score = wellnessScoreVal,
                        statusTitle = headline,
                        overallTier = computedTier,
                        summary = orderedSummaryParts.take(3).joinToString(" "),
                        actionTip = finalTip,
                        metricEvaluations = evaluations,
                        isBtConnected = false,
                        modelUsed = "vitalsync:local-summary"
                    )
                }

                val rawRemoteInsight = aiUiState.aiInsight ?: vitalsAiInsight
                val effectiveHomeAiInsight = if (rawRemoteInsight != null &&
                    rawRemoteInsight.summary.isNotBlank() &&
                    !rawRemoteInsight.summary.contains("adequate", ignoreCase = true) &&
                    !rawRemoteInsight.summary.contains("bluetooth", ignoreCase = true) &&
                    !rawRemoteInsight.summary.contains("smartwatch", ignoreCase = true) &&
                    !rawRemoteInsight.summary.contains("wearable", ignoreCase = true) &&
                    (hasMeasuredBiometricsToday || (!rawRemoteInsight.summary.contains("72 bpm", ignoreCase = true) && !rawRemoteInsight.summary.contains("98%", ignoreCase = true) && !rawRemoteInsight.summary.contains("120/80")))
                ) {
                    val resolvedEvaluations = rawRemoteInsight.metricEvaluations.ifEmpty {
                        localSynthesizedInsight.metricEvaluations
                    }
                    val resolvedTier = rawRemoteInsight.overallTier.ifBlank {
                        localSynthesizedInsight.overallTier
                    }
                    rawRemoteInsight.copy(
                        score = wellnessScoreVal,
                        overallTier = resolvedTier,
                        metricEvaluations = resolvedEvaluations
                    )
                } else {
                    localSynthesizedInsight
                }

                // Synchronize live telemetry with AI insight asynchronously in the background without blocking startup
                LaunchedEffect(currentEmail, effectiveHrBpm, effectiveSpo2Pct, hasTodayBpSummary, consumedWaterMl, sleepDurationFormatted, totalDisplaySteps, effectiveGoal) {
                    if (currentEmail.isNotBlank() && currentScreen != ScreenState.SPLASH) {
                        val uid = currentUser?.id ?: 1
                        aiViewModel.fetchDynamicAiInsight(
                            userId = uid,
                            isBtConnected = false,
                            hydrationMl = consumedWaterMl,
                            sleepText = sleepDurationFormatted,
                            sleepScore = liveSleepScore,
                            steps = totalDisplaySteps,
                            stepGoal = effectiveGoal,
                            targetWaterMl = targetWaterMl
                        )
                    }
                }

                // System Back Navigation Gesture Handler across sub-screens
                val isSubScreen = currentScreen !in listOf(
                    ScreenState.SPLASH,
                    ScreenState.ONBOARDING,
                    ScreenState.WELCOME,
                    ScreenState.LOGIN,
                    ScreenState.MAIN
                )

                BackHandler(enabled = isSubScreen) {
                    when (currentScreen) {
                        ScreenState.AI_CHAT -> {
                            homeInitialTab = 2
                            currentScreen = ScreenState.MAIN
                        }
                        ScreenState.AI_CHAT_HISTORY -> {
                            currentScreen = ScreenState.AI_CHAT
                        }
                        ScreenState.STEP_COUNT_DETAIL,
                        ScreenState.HEART_RATE_DETAIL,
                        ScreenState.BLOOD_PRESSURE_DETAIL,
                        ScreenState.SPO2_DETAIL,
                        ScreenState.SLEEP_DETAIL,
                        ScreenState.HYDRATION_DETAIL,
                        ScreenState.BMI_DETAIL,
                        ScreenState.VITAL_SIGNS -> {
                            homeInitialTab = 0
                            currentScreen = ScreenState.MAIN
                        }
                        ScreenState.EDIT_STEP_GOAL -> {
                            currentScreen = ScreenState.STEP_COUNT_DETAIL
                        }
                        ScreenState.BMI_RESULT,
                        ScreenState.UPDATE_METRICS -> {
                            currentScreen = ScreenState.BMI_DETAIL
                        }
                        ScreenState.START_ACTIVITY -> {
                            homeInitialTab = 1
                            currentScreen = ScreenState.MAIN
                        }
                        ScreenState.WALKING_TRACKING,
                        ScreenState.RUNNING_TRACKING,
                        ScreenState.CYCLING_TRACKING,
                        ScreenState.WORKOUT_TRACKING -> {
                            currentScreen = ScreenState.START_ACTIVITY
                        }
                        ScreenState.VIEW_HISTORY -> {
                            currentScreen = viewHistoryPreviousScreen
                        }
                        ScreenState.SETTINGS,
                        ScreenState.PERSONAL_INFORMATION,
                        ScreenState.HEALTH_GOALS,
                        ScreenState.CONNECTED_DEVICES,
                        ScreenState.NOTIFICATIONS,
                        ScreenState.HEALTH_PERMISSIONS,
                        ScreenState.DATA_MANAGEMENT,
                        ScreenState.ABOUT_VITALSINC,
                        ScreenState.VERSION,
                        ScreenState.HELP_SUPPORT,
                        ScreenState.REMINDERS -> {
                            homeInitialTab = 3
                            currentScreen = ScreenState.MAIN
                        }
                        ScreenState.ADD_REMINDER -> {
                            currentScreen = ScreenState.REMINDERS
                        }
                        ScreenState.CHANGE_PASSWORD,
                        ScreenState.PRIVACY_SECURITY -> {
                            currentScreen = ScreenState.SETTINGS
                        }
                        ScreenState.LOGIN_SESSIONS,
                        ScreenState.PRIVACY_POLICY -> {
                            currentScreen = ScreenState.PRIVACY_SECURITY
                        }
                        ScreenState.PASSWORD_CHANGED_SUCCESS -> {
                            currentScreen = ScreenState.SETTINGS
                        }
                        ScreenState.GYM_SPLIT_SELECT,
                        ScreenState.EXERCISE_LIBRARY,
                        ScreenState.CREATE_CUSTOM_EXERCISE,
                        ScreenState.ACTIVE_WORKOUT,
                        ScreenState.WORKOUT_HISTORY -> {
                            homeInitialTab = 4
                            currentScreen = ScreenState.MAIN
                        }
                        ScreenState.NUTRITION_DASHBOARD -> {
                            homeInitialTab = 0
                            currentScreen = ScreenState.MAIN
                        }
                        ScreenState.FOOD_SEARCH,
                        ScreenState.CREATE_CUSTOM_FOOD,
                        ScreenState.FOOD_DETAILS,
                        ScreenState.DAILY_MEAL_HISTORY,
                        ScreenState.NUTRITION_SETTINGS -> {
                            currentScreen = ScreenState.NUTRITION_DASHBOARD
                        }
                        ScreenState.FITNESS_PROGRESS -> {
                            homeInitialTab = 0
                            currentScreen = ScreenState.MAIN
                        }
                        ScreenState.ADD_READING -> {
                            currentScreen = ScreenState.VITAL_SIGNS
                        }
                        ScreenState.READING_REVIEW -> {
                            currentScreen = ScreenState.ADD_READING
                        }
                        ScreenState.ACTIVITY_DETAIL -> {
                            currentScreen = ScreenState.START_ACTIVITY
                        }
                        ScreenState.WEEKLY_REPORT -> {
                            homeInitialTab = 0
                            currentScreen = ScreenState.MAIN
                        }
                        ScreenState.SIGNUP,
                        ScreenState.FORGOT_PASSWORD -> {
                            currentScreen = ScreenState.LOGIN
                        }
                        ScreenState.PROFILE_SETUP -> {
                            currentScreen = ScreenState.SIGNUP
                        }
                        else -> {}
                    }
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
                                        if (authViewModel.isLoggedIn()) {
                                            homeInitialTab = 0
                                            currentScreen = ScreenState.MAIN
                                        } else {
                                            currentScreen = ScreenState.LOGIN
                                        }
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
                                            profileViewModel.loadAvatarFromUser(user)
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
                                        repository.clearAllUserLocalData()
                                        stepSensorManager.resetForAccountSwitch()
                                        vitalsViewModel.resetState()
                                        hydrationViewModel.resetState()
                                        heartRateViewModel.resetState()
                                        activityViewModel.resetState()
                                        aiViewModel.resetState()
                                        gymViewModel.resetState()
                                        nutritionViewModel.resetState()
                                        globalProfilePhotoBitmap = null
                                        persistedStepGoal = 0
                                        globalUserAge = ""
                                        userHeightCm = ""
                                        userWeightKg = ""
                                        calculatedBmiValue = "--"
                                        calculatedBmiCategory = "--"
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
                                var setupGender by remember { mutableStateOf(profileState.gender ?: "") }
                                var setupActivityLevel by remember { mutableStateOf(profileState.activityLevel ?: "") }
                                var setupPrimaryGoal by remember { mutableStateOf("") }
                                ProfileSetupScreen(
                                    initialName = globalUserName,
                                    initialAge = globalUserAge,
                                    initialGender = setupGender,
                                    initialHeight = userHeightCm,
                                    initialWeight = userWeightKg,
                                    initialActivityLevel = setupActivityLevel,
                                    onBackClick = { currentScreen = ScreenState.SIGNUP },
                                    onSelectionChanged = { selectedGender, selectedActivity ->
                                        setupGender = selectedGender
                                        setupActivityLevel = selectedActivity
                                        profileViewModel.persistQuickProfileSelections(selectedGender, selectedActivity)?.let { updatedUser ->
                                            authViewModel.updateCurrentUser(updatedUser)
                                        }
                                    },
                                    onCompleteProfileDetails = { selectedGender, selectedActivity, selectedGoal ->
                                        setupGender = selectedGender
                                        setupActivityLevel = selectedActivity
                                        setupPrimaryGoal = selectedGoal
                                    },
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

                                        profileViewModel.updatePersonalInformation(
                                            newName = newName.ifBlank { globalUserName },
                                            newAge = globalUserAge.toIntOrNull() ?: 0,
                                            newHeightCm = userHeightCm.toDoubleOrNull() ?: 0.0,
                                            newWeightKg = userWeightKg.toDoubleOrNull() ?: 0.0,
                                            newGender = setupGender,
                                            newActivityLevel = setupActivityLevel,
                                            newPrimaryGoal = setupPrimaryGoal,
                                            userId = currentUser?.id,
                                            email = currentEmail
                                        )

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
                                    heartRateTrend = heartRateTrendFormatted,
                                    spo2Value = spo2Formatted,
                                    spo2Status = spo2StatusFormatted,
                                    spo2Trend = spo2TrendFormatted,
                                    bpValue = bpFormatted,
                                    bpStatus = bpStatusFormatted,
                                    bpTrend = bpTrendFormatted,
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
                                    hasLoggedBiometrics = hasMeasuredBiometricsToday,
                                    onLogVitalsClick = {
                                        currentScreen = ScreenState.VITAL_SIGNS
                                    },
                                    onLogHeartRateClick = {
                                        vitalsViewModel.resetManualLogState()
                                        activeVitalSheet = VitalReadingType.HEART_RATE
                                    },
                                    onLogSpo2Click = {
                                        vitalsViewModel.resetManualLogState()
                                        activeVitalSheet = VitalReadingType.SPO2
                                    },
                                    onLogBloodPressureClick = {
                                        vitalsViewModel.resetManualLogState()
                                        activeVitalSheet = VitalReadingType.BLOOD_PRESSURE
                                    },
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
                                    onNotificationsClick = { currentScreen = ScreenState.NOTIFICATIONS },
                                    onPrivacySecurityClick = { currentScreen = ScreenState.PRIVACY_SECURITY },
                                    onSettingsClick = { currentScreen = ScreenState.SETTINGS },
                                    onHelpSupportClick = { currentScreen = ScreenState.HELP_SUPPORT },
                                    aiInsightTitle = effectiveHomeAiInsight.statusTitle,
                                    aiInsightSummary = effectiveHomeAiInsight.summary,
                                    aiInsight = effectiveHomeAiInsight,
                                    isInsightLoading = aiUiState.isInsightLoading || isVitalsAiLoading,
                                    weeklyReport = weeklyReportState.report ?: aiUiState.weeklyReport,
                                    onAiTabSelected = {
                                        if (currentEmail.isNotBlank()) {
                                            val resolvedUid = profileState.userId.takeIf { it > 0 } ?: currentUser?.id
                                            profileViewModel.fetchWeeklyHealthReport(userId = resolvedUid, email = currentEmail)
                                            aiViewModel.loadWeeklyTrends(
                                                userId = resolvedUid,
                                                email = currentEmail
                                            )
                                        }
                                    },
                                    networkErrorMessage = startupNetworkError,
                                    onRetryNetwork = {
                                        if (currentEmail.isNotBlank()) {
                                            vitalsViewModel.clearSummaryError()
                                            activityViewModel.clearSummaryError()
                                            profileViewModel.clearProfileError()
                                            profileViewModel.fetchProfile(email = currentEmail)
                                            vitalsViewModel.fetchSummary(email = currentEmail)
                                            activityViewModel.loadSummary(email = currentEmail)
                                        }
                                    },
                                    onDismissNetworkError = {
                                        vitalsViewModel.clearSummaryError()
                                        activityViewModel.clearSummaryError()
                                        profileViewModel.clearProfileError()
                                    },
                                    onAskAiClick = { currentScreen = ScreenState.AI_CHAT },
                                    onStartWorkoutSplitClick = { currentScreen = ScreenState.GYM_SPLIT_SELECT },
                                    onOpenNutritionTrackerClick = { currentScreen = ScreenState.NUTRITION_DASHBOARD },
                                    onOpenProgressAnalyticsClick = { currentScreen = ScreenState.FITNESS_PROGRESS },
                                    onViewWorkoutHistoryClick = { currentScreen = ScreenState.WORKOUT_HISTORY },
                                    onLogoutClick = {
                                        performFullAccountCleanup()
                                        persistedStepGoal = 0
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
                                        gymViewModel.saveWorkoutSession(session, currentEmail)
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
                                    userId = profileState.userId.takeIf { it > 0 },
                                    userEmail = currentEmail,
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
                                    sessions = aiUiState.sessions,
                                    onBackClick = { currentScreen = ScreenState.AI_CHAT },
                                    onSelectSession = { session ->
                                        aiViewModel.selectSession(
                                            session = session,
                                            userId = profileState.userId.takeIf { it > 0 },
                                            email = currentEmail
                                        )
                                        currentScreen = ScreenState.AI_CHAT
                                    },
                                    onDeleteSession = { sessId ->
                                        aiViewModel.deleteSession(
                                            sessionId = sessId,
                                            userId = profileState.userId.takeIf { it > 0 },
                                            email = currentEmail
                                        )
                                    },
                                    onSelectConversation = { currentScreen = ScreenState.AI_CHAT },
                                    onNewChatClick = {
                                        aiViewModel.startNewChat()
                                        currentScreen = ScreenState.AI_CHAT
                                    }
                                )
                            }
                            ScreenState.WEEKLY_REPORT -> {
                                val report = weeklyReportState.report ?: aiUiState.weeklyReport
                                val fallbackObsText = if (wellnessScoreVal > 0) {
                                    "Weekly clinical summary: Vitals tracked across heart rate, SpO2, blood pressure, sleep, and activity. Keep up the balanced routine!"
                                } else {
                                    "No weekly vital logs recorded yet. Log your vitals or daily activity to view clinical observations."
                                }

                                val effectiveSleepVal = when {
                                    todaySleepMinutes > 0 && evaluatedSleepScore > 0 -> "$evaluatedSleepScore% - $sleepCategoryLabel"
                                    todaySleepMinutes > 0 -> "${SleepColorStandards.deriveSleepScore(todaySleepMinutes)}% - $sleepCategoryLabel"
                                    else -> "--"
                                }
                                val effectiveSleepStatus = when {
                                    todaySleepMinutes > 0 -> "$sleepDurationFormatted avg"
                                    else -> "No Logs Recorded"
                                }

                                val breakdown = report?.toVitalPerformanceBreakdown(
                                    fallbackScore = wellnessScoreVal,
                                    fallbackStatus = wellnessStatusVal,
                                    fallbackHr = heartRateFormatted,
                                    fallbackHrStatus = heartRateStatusFormatted,
                                    fallbackBp = bpFormatted,
                                    fallbackBpStatus = bpStatusFormatted,
                                    fallbackSpo2 = spo2Formatted,
                                    fallbackSpo2Status = spo2StatusFormatted,
                                    fallbackSleepVal = report?.resolvedBreakdownSleepValue()?.takeIf { it != "--" } ?: effectiveSleepVal,
                                    fallbackSleepStatus = report?.resolvedBreakdownSleepStatus()?.takeIf { it != "No Logs Recorded" } ?: effectiveSleepStatus,
                                    fallbackHydrationVal = if (hydrationSummary != null && hydrationSummary.consumedMl > 0) "${hydrationSummary.consumedMl} ml" else "-- ml",
                                    fallbackHydrationStatus = if (hydrationSummary != null && hydrationSummary.consumedMl > 0) "${hydrationSummary.progressPercent}% of goal" else "No Data",
                                    fallbackStepsVal = activityStepsFormatted,
                                    fallbackStepsStatus = stepsStatusFormatted,
                                    fallbackObs = fallbackObsText
                                ) ?: VitalPerformanceBreakdown(
                                    overallWellnessScore = wellnessScoreVal,
                                    overallStatus = wellnessStatusVal,
                                    heartRateValue = heartRateFormatted,
                                    heartRateStatus = heartRateStatusFormatted,
                                    bpValue = bpFormatted,
                                    bpStatus = bpStatusFormatted,
                                    spo2Value = spo2Formatted,
                                    spo2Status = spo2StatusFormatted,
                                    sleepValue = effectiveSleepVal,
                                    sleepStatus = effectiveSleepStatus,
                                    hydrationValue = if (hydrationSummary != null && hydrationSummary.consumedMl > 0) "${hydrationSummary.consumedMl} ml" else "-- ml",
                                    hydrationStatus = if (hydrationSummary != null && hydrationSummary.consumedMl > 0) "${hydrationSummary.progressPercent}% of goal" else "No Data",
                                    activityValue = activityStepsFormatted,
                                    activityStatus = stepsStatusFormatted,
                                    observationText = fallbackObsText
                                )

                                WeeklyHealthReportScreen(
                                    wellnessScore = breakdown.overallWellnessScore,
                                    wellnessStatus = breakdown.overallStatus,
                                    heartRateAvg = breakdown.heartRateValue,
                                    heartRateStatus = breakdown.heartRateStatus,
                                    bpAvg = breakdown.bpValue,
                                    bpStatus = breakdown.bpStatus,
                                    spo2Avg = breakdown.spo2Value,
                                    spo2Status = breakdown.spo2Status,
                                    sleepQuality = breakdown.sleepValue,
                                    sleepStatus = breakdown.sleepStatus,
                                    hydrationTotal = breakdown.hydrationValue,
                                    hydrationStatus = breakdown.hydrationStatus,
                                    activitySteps = breakdown.activityValue,
                                    activityStatus = breakdown.activityStatus,
                                    aiObservationText = breakdown.observationText,
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
                                            breakdown = breakdown
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
                                            appendLine("Wellness Score: ${breakdown.overallWellnessScore}/100 (${breakdown.overallStatus})")
                                            appendLine()
                                            appendLine("Vital Performance Telemetry:")
                                            appendLine("• Heart Rate: ${breakdown.heartRateValue} (${breakdown.heartRateStatus})")
                                            appendLine("• Blood Pressure: ${breakdown.bpValue} (${breakdown.bpStatus})")
                                            appendLine("• SpO2: ${breakdown.spo2Value} (${breakdown.spo2Status})")
                                            appendLine("• Sleep Quality: ${breakdown.sleepValue} (${breakdown.sleepStatus})")
                                            appendLine("• Hydration Intake: ${breakdown.hydrationValue} (${breakdown.hydrationStatus})")
                                            appendLine("• Physical Activity: ${breakdown.activityValue} (${breakdown.activityStatus})")
                                            appendLine()
                                            appendLine("Clinical & AI Doctor Observations:")
                                            appendLine(breakdown.observationText)
                                            if (breakdown.recommendations.isNotEmpty()) {
                                                appendLine()
                                                appendLine("Key Recommendations:")
                                                breakdown.recommendations.forEach { rec ->
                                                    appendLine("• $rec")
                                                }
                                            }
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
                                var pendingAvatarCleared = false
                                PersonalInformationScreen(
                                    currentName = globalUserName,
                                    currentInitials = globalUserInitials,
                                    currentHeight = userHeightCm,
                                    currentWeight = userWeightKg,
                                    currentDob = profileState.dob ?: "",
                                    currentGender = profileState.gender ?: "",
                                    currentEmail = profileState.email.ifBlank { currentEmail },
                                    currentPhone = profileState.phone ?: "",
                                    currentPhotoBitmap = globalProfilePhotoBitmap,
                                    onBackClick = {
                                        homeInitialTab = 3
                                        currentScreen = ScreenState.MAIN
                                    },
                                    onSaveAvatarFile = { avatarFile, cleared ->
                                        pendingAvatarCleared = cleared
                                        if (avatarFile != null) {
                                            profileViewModel.uploadProfileImage(
                                                imageFile = avatarFile,
                                                userId = currentUser?.id,
                                                email = currentEmail
                                            )
                                            try {
                                                android.graphics.BitmapFactory.decodeFile(avatarFile.absolutePath)?.let {
                                                    globalProfilePhotoBitmap = it.asImageBitmap()
                                                }
                                            } catch (_: Exception) {}
                                        } else if (cleared) {
                                            globalProfilePhotoBitmap = null
                                        }
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
                                            userId = currentUser?.id,
                                            email = currentEmail,
                                            clearProfileImage = pendingAvatarCleared
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
                                // Replaced by Manual Vitals workflow
                                LaunchedEffect(Unit) {
                                    homeInitialTab = 3
                                    currentScreen = ScreenState.MAIN
                                }
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
                                        sessionManager.setAiInsightsEnabled(enabled)
                                        profileViewModel.updateAppSettings(email = currentEmail, aiInsights = enabled)
                                    },
                                    usageAnalyticsEnabled = appSettingsState.usageAnalytics,
                                    onUsageAnalyticsChange = { enabled ->
                                        sessionManager.setUsageAnalyticsEnabled(enabled)
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
                                    themeMode = currentThemeMode,
                                    onThemeModeSelected = { mode ->
                                        currentThemeMode = mode
                                        sessionManager.setThemeMode(mode)
                                    },
                                    isDarkTheme = isEffectiveDark,
                                    onDarkThemeToggle = { enabled ->
                                        val newMode = if (enabled) AppThemeMode.DARK else AppThemeMode.LIGHT
                                        currentThemeMode = newMode
                                        sessionManager.setThemeMode(newMode)
                                    },
                                    pushNotifications = appSettingsState.pushNotifications,
                                    onPushNotificationsToggle = { enabled ->
                                        profileViewModel.updateAppSettings(email = currentEmail, pushNotifications = enabled)
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
                                    heartRateValue = heartRateFormatted,
                                    heartRateStatus = heartRateStatusFormatted,
                                    heartRateUpdated = if (hasTodayHrSummary) hrSummary!!.recordedAt else "No readings today",
                                    bpValue = bpFormatted,
                                    bpStatus = bpStatusFormatted,
                                    bpUpdated = if (hasTodayBpSummary) bpSummary!!.recordedAt else "No readings today",
                                    spo2Value = spo2Formatted,
                                    spo2Status = spo2StatusFormatted,
                                    spo2Updated = if (hasTodaySpo2Summary) spo2Summary!!.recordedAt else "No readings today",
                                    weightValue = if (userWeightKg.isNotBlank()) "$userWeightKg kg" else "--",
                                    bmiValue = calculatedBmiValue,
                                    bmiStatus = calculatedBmiCategory,
                                    onBackClick = {
                                        homeInitialTab = 0
                                        currentScreen = ScreenState.MAIN
                                    },
                                    onAskAiClick = { currentScreen = ScreenState.AI_CHAT },
                                    onHeartRateClick = { currentScreen = ScreenState.HEART_RATE_DETAIL },
                                    onBloodPressureClick = { currentScreen = ScreenState.BLOOD_PRESSURE_DETAIL },
                                    onSpO2Click = { currentScreen = ScreenState.SPO2_DETAIL },
                                    onBmiClick = { currentScreen = ScreenState.BMI_DETAIL },
                                    onAddReadingClick = {
                                        vitalsViewModel.resetManualLogState()
                                        activeVitalSheet = VitalReadingType.HEART_RATE
                                    }
                                )
                            }
                            ScreenState.HEART_RATE_DETAIL -> {
                                LaunchedEffect(currentEmail, currentUser?.id, profileState.userId) {
                                    val uid = currentUser?.id ?: profileState.userId.takeIf { it > 0 }
                                    vitalsViewModel.fetchHrDetails(userId = uid, email = currentEmail.ifBlank { null })
                                    if (currentEmail.isNotBlank()) {
                                        heartRateViewModel.fetchAnalytics(currentEmail, uid)
                                    }
                                }
                                HeartRateDetailScreen(
                                    uiState = hrUiState,
                                    detailState = hrDetailState.data,
                                    isDetailLoading = hrDetailState.isLoading,
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
                                    onAddReadingClick = {
                                        vitalsViewModel.resetManualLogState()
                                        activeVitalSheet = VitalReadingType.HEART_RATE
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
                                    viewModel = activityViewModel,
                                    userEmail = currentEmail,
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
                                    userId = profileState.userId.takeIf { it > 0 },
                                    userEmail = currentEmail,
                                    showAiInsights = appSettingsState.aiInsights,
                                    onSleepLogged = {
                                        if (currentEmail.isNotBlank()) {
                                            vitalsViewModel.fetchSummary(email = currentEmail)
                                            profileViewModel.fetchWeeklyHealthReport(email = currentEmail)
                                            aiViewModel.loadWeeklyTrends(
                                                userId = profileState.userId.takeIf { it > 0 },
                                                email = currentEmail
                                            )
                                        }
                                    },
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
                                LaunchedEffect(currentEmail, currentUser?.id, profileState.userId) {
                                    val uid = currentUser?.id ?: profileState.userId.takeIf { it > 0 }
                                    vitalsViewModel.fetchBpDetails(userId = uid, email = currentEmail.ifBlank { null })
                                }
                                BloodPressureDetailScreen(
                                    detailState = bpDetailState.data,
                                    isDetailLoading = bpDetailState.isLoading,
                                    systolic = bpSummary?.systolic ?: 0,
                                    diastolic = bpSummary?.diastolic ?: 0,
                                    status = if (bpSummary != null && bpSummary.systolic > 0) bpSummary.status else "No data available",
                                    onBackClick = {
                                        homeInitialTab = 0
                                        currentScreen = ScreenState.MAIN
                                    },
                                    onAddReadingClick = {
                                        vitalsViewModel.resetManualLogState()
                                        activeVitalSheet = VitalReadingType.BLOOD_PRESSURE
                                    }
                                )
                            }
                            ScreenState.SPO2_DETAIL -> {
                                LaunchedEffect(currentEmail, currentUser?.id, profileState.userId) {
                                    val uid = currentUser?.id ?: profileState.userId.takeIf { it > 0 }
                                    vitalsViewModel.fetchSpo2Details(userId = uid, email = currentEmail.ifBlank { null })
                                }
                                Spo2DetailScreen(
                                    detailState = spo2DetailState.data,
                                    isDetailLoading = spo2DetailState.isLoading,
                                    initialPercentage = spo2Summary?.percentage ?: 0,
                                    onBackClick = {
                                        homeInitialTab = 0
                                        currentScreen = ScreenState.MAIN
                                    },
                                    onAddReadingClick = {
                                        vitalsViewModel.resetManualLogState()
                                        activeVitalSheet = VitalReadingType.SPO2
                                    }
                                )
                            }
                            ScreenState.STEP_COUNT_DETAIL -> {
                                StepCountDetailScreen(
                                    stepsCurrent = currentEffectiveSteps,
                                    stepsGoal = effectiveGoal,
                                    distanceKm = effectiveDistance,
                                    caloriesBurned = effectiveCalories,
                                    activeMinutes = effectiveActiveMinutes,
                                    onBackClick = {
                                        homeInitialTab = 0
                                        currentScreen = ScreenState.MAIN
                                    },
                                    onEditGoalClick = { currentScreen = ScreenState.EDIT_STEP_GOAL }
                                )
                            }
                            ScreenState.EDIT_STEP_GOAL -> {
                                EditStepGoalScreen(
                                    initialStepGoal = effectiveGoal,
                                    viewModel = activityViewModel,
                                    userEmail = currentEmail,
                                    onBackClick = { currentScreen = ScreenState.STEP_COUNT_DETAIL },
                                    onSaveGoalSuccess = { savedGoal ->
                                        persistedStepGoal = savedGoal
                                        sessionManager.saveStepGoal(savedGoal)
                                        repository.saveStepGoal(savedGoal)
                                        if (currentEmail.isNotBlank()) {
                                            vitalsViewModel.fetchSummary(email = currentEmail)
                                            activityViewModel.loadSummary(email = currentEmail)
                                            val uid = currentUser?.id ?: 1
                                            aiViewModel.fetchDynamicAiInsight(
                                                userId = uid,
                                                isBtConnected = false,
                                                hydrationMl = consumedWaterMl,
                                                sleepText = sleepDurationFormatted,
                                                sleepScore = liveSleepScore,
                                                steps = totalDisplaySteps,
                                                stepGoal = savedGoal,
                                                targetWaterMl = targetWaterMl
                                            )
                                        }
                                        currentScreen = ScreenState.STEP_COUNT_DETAIL
                                    }
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

                    val submitSingleVital: (
                        hr: Int?,
                        spo2: Int?,
                        sys: Int?,
                        dia: Int?,
                        notes: String?,
                        toastMsg: String
                    ) -> Unit = { hr, spo2, sys, dia, notes, toastMsg ->
                        val uid = currentUser?.id ?: profileState.userId.takeIf { it > 0 } ?: 1
                        vitalsViewModel.logManualVitals(
                            heartRate = hr,
                            spo2 = spo2,
                            systolic = sys,
                            diastolic = dia,
                            notes = notes,
                            userId = uid,
                            email = currentEmail.ifBlank { null },
                            onSuccess = {
                                activeVitalSheet = null
                                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                                if (currentEmail.isNotBlank()) {
                                    heartRateViewModel.fetchAnalytics(currentEmail, uid)
                                    profileViewModel.fetchWeeklyHealthReport(email = currentEmail)
                                }
                                aiViewModel.fetchDynamicAiInsight(
                                    userId = uid,
                                    isBtConnected = false,
                                    hydrationMl = consumedWaterMl,
                                    sleepText = sleepDurationFormatted,
                                    sleepScore = liveSleepScore,
                                    steps = totalDisplaySteps,
                                    stepGoal = effectiveGoal,
                                    targetWaterMl = targetWaterMl
                                )
                            }
                        )
                    }

                    when (activeVitalSheet) {
                        VitalReadingType.HEART_RATE -> {
                            LogHeartRateSheet(
                                initialBpm = effectiveHrBpm,
                                isSaving = manualLogState.isSaving,
                                serverError = manualLogState.errorMessage,
                                onDismiss = {
                                    vitalsViewModel.resetManualLogState()
                                    activeVitalSheet = null
                                },
                                onSave = { hr, notes ->
                                    submitSingleVital(hr, null, null, null, notes, "Heart rate logged")
                                }
                            )
                        }
                        VitalReadingType.SPO2 -> {
                            LogSpo2Sheet(
                                initialSpo2 = effectiveSpo2Pct,
                                isSaving = manualLogState.isSaving,
                                serverError = manualLogState.errorMessage,
                                onDismiss = {
                                    vitalsViewModel.resetManualLogState()
                                    activeVitalSheet = null
                                },
                                onSave = { spo2, notes ->
                                    submitSingleVital(null, spo2, null, null, notes, "SpO₂ logged")
                                }
                            )
                        }
                        VitalReadingType.BLOOD_PRESSURE -> {
                            LogBloodPressureSheet(
                                initialSystolic = if (hasTodayBpSummary) bpSummary?.systolic else null,
                                initialDiastolic = if (hasTodayBpSummary) bpSummary?.diastolic else null,
                                isSaving = manualLogState.isSaving,
                                serverError = manualLogState.errorMessage,
                                onDismiss = {
                                    vitalsViewModel.resetManualLogState()
                                    activeVitalSheet = null
                                },
                                onSave = { sys, dia, notes ->
                                    submitSingleVital(null, null, sys, dia, notes, "Blood pressure logged")
                                }
                            )
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}
