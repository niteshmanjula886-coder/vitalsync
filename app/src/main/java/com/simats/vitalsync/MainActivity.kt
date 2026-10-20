package com.simats.vitalsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.simats.vitalsync.ui.activity.ActivityType
import com.simats.vitalsync.ui.activity.CyclingTrackingScreen
import com.simats.vitalsync.ui.activity.RunningTrackingScreen
import com.simats.vitalsync.ui.activity.StartActivityScreen
import com.simats.vitalsync.ui.activity.ViewHistoryScreen
import com.simats.vitalsync.ui.activity.WalkingTrackingScreen
import com.simats.vitalsync.ui.activity.WorkoutTrackingScreen
import com.simats.vitalsync.ui.auth.AddGoogleAccountScreen
import com.simats.vitalsync.ui.auth.ForgotPasswordScreen
import com.simats.vitalsync.ui.auth.GoogleAccountChooserBottomSheet
import com.simats.vitalsync.ui.auth.LoginScreen
import com.simats.vitalsync.ui.auth.SignUpScreen
import com.simats.vitalsync.ui.auth.VerificationScreen
import com.simats.vitalsync.ui.home.HomeScreen
import com.simats.vitalsync.ui.onboarding.OnboardingScreen
import com.simats.vitalsync.ui.profile.AboutVitalSyncScreen
import com.simats.vitalsync.ui.profile.ChangePasswordScreen
import com.simats.vitalsync.ui.profile.ConnectedDevicesScreen
import com.simats.vitalsync.ui.profile.DataManagementScreen
import com.simats.vitalsync.ui.profile.HealthGoalsScreen
import com.simats.vitalsync.ui.profile.HealthPermissionsScreen
import com.simats.vitalsync.ui.profile.HelpSupportScreen
import com.simats.vitalsync.ui.profile.NotificationsScreen
import com.simats.vitalsync.ui.profile.PersonalInformationScreen
import com.simats.vitalsync.ui.profile.PrivacySecurityScreen
import com.simats.vitalsync.ui.profile.ProfileSetupScreen
import com.simats.vitalsync.ui.profile.SettingsScreen
import com.simats.vitalsync.ui.profile.VersionScreen
import com.simats.vitalsync.ui.splash.SplashScreen
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import com.simats.vitalsync.ui.vitals.ActivityDetailScreen
import com.simats.vitalsync.ui.vitals.AddReadingScreen
import com.simats.vitalsync.ui.vitals.BloodPressureDetailScreen
import com.simats.vitalsync.ui.vitals.BmiDetailScreen
import com.simats.vitalsync.ui.vitals.BmiResultScreen
import com.simats.vitalsync.ui.vitals.HeartRateDetailScreen
import com.simats.vitalsync.ui.vitals.HydrationDetailScreen
import com.simats.vitalsync.ui.vitals.SleepDetailScreen
import com.simats.vitalsync.ui.vitals.Spo2DetailScreen
import com.simats.vitalsync.ui.vitals.StepCountDetailScreen
import com.simats.vitalsync.ui.vitals.UpdateMetricsScreen
import com.simats.vitalsync.ui.vitals.VitalReadingType
import com.simats.vitalsync.ui.vitals.VitalSignsScreen

enum class ScreenState {
    SPLASH,
    ONBOARDING,
    LOGIN,
    SIGNUP,
    FORGOT_PASSWORD,
    ADD_GOOGLE_ACCOUNT,
    GOOGLE_VERIFY,
    PROFILE_SETUP,
    MAIN,
    VITAL_SIGNS,
    HEART_RATE_DETAIL,
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
    BMI_DETAIL,
    UPDATE_METRICS,
    BMI_RESULT,
    PERSONAL_INFORMATION,
    HEALTH_GOALS,
    CONNECTED_DEVICES,
    NOTIFICATIONS,
    PRIVACY_SECURITY,
    SETTINGS,
    CHANGE_PASSWORD,
    HEALTH_PERMISSIONS,
    DATA_MANAGEMENT,
    ABOUT_VITALSINC,
    VERSION,
    HELP_SUPPORT,
    ADD_READING
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VitalsyncTheme {
                var currentScreen by remember { mutableStateOf(ScreenState.SPLASH) }
                var showGoogleAccountChooser by remember { mutableStateOf(false) }
                var selectedGoogleEmail by remember { mutableStateOf("alex.morgan@gmail.com") }
                var isNewUser by remember { mutableStateOf(false) }
                var activeReadingType by remember { mutableStateOf(VitalReadingType.HEART_RATE) }

                var calculatedBmiValue by remember { mutableStateOf("22.4") }
                var calculatedBmiCategory by remember { mutableStateOf("Healthy Range") }
                var userHeightCm by remember { mutableStateOf("175") }
                var userWeightKg by remember { mutableStateOf("68.5") }

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
                                onSkipClick = { currentScreen = ScreenState.MAIN },
                                onFinishOnboarding = { currentScreen = ScreenState.LOGIN }
                            )
                        }
                        ScreenState.LOGIN -> {
                            LoginScreen(
                                onLoginSuccess = {
                                    isNewUser = false
                                    currentScreen = ScreenState.MAIN
                                },
                                onGoogleLoginClick = { showGoogleAccountChooser = true },
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
                                onNavigateToLogin = { currentScreen = ScreenState.LOGIN },
                                onGoogleSignUpClick = { showGoogleAccountChooser = true }
                            )
                        }
                        ScreenState.FORGOT_PASSWORD -> {
                            ForgotPasswordScreen(
                                onBackClick = { currentScreen = ScreenState.LOGIN },
                                onResetLinkSent = { }
                            )
                        }
                        ScreenState.ADD_GOOGLE_ACCOUNT -> {
                            AddGoogleAccountScreen(
                                onBackClick = { currentScreen = ScreenState.LOGIN },
                                onAccountAdded = { email ->
                                    isNewUser = true
                                    selectedGoogleEmail = email
                                    currentScreen = ScreenState.GOOGLE_VERIFY
                                }
                            )
                        }
                        ScreenState.GOOGLE_VERIFY -> {
                            VerificationScreen(
                                email = selectedGoogleEmail,
                                onBackClick = { currentScreen = ScreenState.LOGIN },
                                onVerifySuccess = {
                                    if (isNewUser) {
                                        currentScreen = ScreenState.PROFILE_SETUP
                                    } else {
                                        currentScreen = ScreenState.MAIN
                                    }
                                }
                            )
                        }
                        ScreenState.PROFILE_SETUP -> {
                            ProfileSetupScreen(
                                onBackClick = { currentScreen = ScreenState.LOGIN },
                                onSaveAndContinue = { currentScreen = ScreenState.MAIN }
                            )
                        }
                        ScreenState.MAIN -> {
                            HomeScreen(
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
                                onLogoutClick = { currentScreen = ScreenState.LOGIN }
                            )
                        }
                        ScreenState.PERSONAL_INFORMATION -> {
                            PersonalInformationScreen(
                                onBackClick = { currentScreen = ScreenState.MAIN },
                                onSaveChangesClick = { currentScreen = ScreenState.MAIN }
                            )
                        }
                        ScreenState.HEALTH_GOALS -> {
                            HealthGoalsScreen(
                                onBackClick = { currentScreen = ScreenState.MAIN },
                                onSaveGoalClick = { currentScreen = ScreenState.MAIN }
                            )
                        }
                        ScreenState.CONNECTED_DEVICES -> {
                            ConnectedDevicesScreen(
                                onBackClick = { currentScreen = ScreenState.MAIN }
                            )
                        }
                        ScreenState.NOTIFICATIONS -> {
                            NotificationsScreen(
                                onBackClick = { currentScreen = ScreenState.MAIN }
                            )
                        }
                        ScreenState.PRIVACY_SECURITY -> {
                            PrivacySecurityScreen(
                                onBackClick = { currentScreen = ScreenState.MAIN }
                            )
                        }
                        ScreenState.SETTINGS -> {
                            SettingsScreen(
                                onBackClick = { currentScreen = ScreenState.MAIN },
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
                                onBackClick = { currentScreen = ScreenState.MAIN }
                            )
                        }
                        ScreenState.VITAL_SIGNS -> {
                            VitalSignsScreen(
                                onBackClick = { currentScreen = ScreenState.MAIN },
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
                                onBackClick = { currentScreen = ScreenState.MAIN }
                            )
                        }
                        ScreenState.ACTIVITY_DETAIL -> {
                            ActivityDetailScreen(
                                onBackClick = { currentScreen = ScreenState.MAIN },
                                onStartActivityClick = { currentScreen = ScreenState.START_ACTIVITY },
                                onViewHistoryClick = { currentScreen = ScreenState.VIEW_HISTORY }
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
                                onBackClick = { currentScreen = ScreenState.ACTIVITY_DETAIL }
                            )
                        }
                        ScreenState.SLEEP_DETAIL -> {
                            SleepDetailScreen(
                                onBackClick = { currentScreen = ScreenState.MAIN }
                            )
                        }
                        ScreenState.HYDRATION_DETAIL -> {
                            HydrationDetailScreen(
                                onBackClick = { currentScreen = ScreenState.MAIN }
                            )
                        }
                        ScreenState.BLOOD_PRESSURE_DETAIL -> {
                            BloodPressureDetailScreen(
                                onBackClick = { currentScreen = ScreenState.MAIN },
                                onAddReadingClick = {
                                    activeReadingType = VitalReadingType.BLOOD_PRESSURE
                                    currentScreen = ScreenState.ADD_READING
                                }
                            )
                        }
                        ScreenState.SPO2_DETAIL -> {
                            Spo2DetailScreen(
                                onBackClick = { currentScreen = ScreenState.MAIN },
                                onAddReadingClick = {
                                    activeReadingType = VitalReadingType.SPO2
                                    currentScreen = ScreenState.ADD_READING
                                }
                            )
                        }
                        ScreenState.STEP_COUNT_DETAIL -> {
                            StepCountDetailScreen(
                                onBackClick = { currentScreen = ScreenState.MAIN },
                                onEditGoalClick = { /* Handle Edit Goal */ }
                            )
                        }
                        ScreenState.BMI_DETAIL -> {
                            BmiDetailScreen(
                                onBackClick = { currentScreen = ScreenState.MAIN },
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
                                onBackClick = { currentScreen = ScreenState.MAIN },
                                onDoneClick = { currentScreen = ScreenState.MAIN }
                            )
                        }
                    }
                }

                // Modal Bottom Sheet for Google Account Chooser
                if (showGoogleAccountChooser) {
                    GoogleAccountChooserBottomSheet(
                        onDismissRequest = { showGoogleAccountChooser = false },
                        onAccountSelected = { account ->
                            isNewUser = false
                            selectedGoogleEmail = account.email
                            showGoogleAccountChooser = false
                            currentScreen = ScreenState.GOOGLE_VERIFY
                        },
                        onAddAnotherAccount = {
                            isNewUser = true
                            showGoogleAccountChooser = false
                            currentScreen = ScreenState.ADD_GOOGLE_ACCOUNT
                        }
                    )
                }
            }
        }
    }
}
