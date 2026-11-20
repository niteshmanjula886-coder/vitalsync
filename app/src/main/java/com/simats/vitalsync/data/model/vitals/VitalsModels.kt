package com.simats.vitalsync.data.model.vitals

import com.google.gson.annotations.SerializedName

data class HeartRateSummary(
    @SerializedName("bpm")
    val bpm: Int = 0,
    @SerializedName("status")
    val status: String = "Tap to log",
    @SerializedName("resting_bpm")
    val restingBpm: Int = 0,
    @SerializedName("min_bpm")
    val minBpm: Int = 0,
    @SerializedName("max_bpm")
    val maxBpm: Int = 0,
    @SerializedName("trend")
    val trend: String? = null,
    @SerializedName("recorded_at")
    val recordedAt: String = "",
    @SerializedName("has_data")
    val hasData: Boolean = false
)

data class BloodPressureSummary(
    @SerializedName("systolic", alternate = ["systolic_bp", "systolicBp"])
    val systolic: Int = 0,
    @SerializedName("diastolic", alternate = ["diastolic_bp", "diastolicBp"])
    val diastolic: Int = 0,
    @SerializedName("text")
    val text: String = "--/-- mmHg",
    @SerializedName("badge_color")
    val badgeColor: String = "#9E9E9E",
    @SerializedName("status")
    val status: String = "Tap to log",
    @SerializedName("trend")
    val trend: String? = null,
    @SerializedName("recorded_at")
    val recordedAt: String = "",
    @SerializedName("has_data")
    val hasData: Boolean = false
)

data class BpClinicalEvaluation(
    val statusTitle: String,
    val urgency: String,
    val tier: String,
    val badgeColor: String = "#43A047"
)

/**
 * Evaluates systolic/diastolic blood pressure strictly following AHA/ACC clinical boundaries,
 * prioritizing Hypotension (< 90 / < 60) first before hypertension and optimal bounds.
 */
fun evaluateBloodPressure(systolic: Int?, diastolic: Int?): BpClinicalEvaluation {
    if (systolic == null || diastolic == null || systolic <= 0 || diastolic <= 0) {
        return BpClinicalEvaluation("No Data", "NORMAL", "Optimal", "#9E9E9E")
    }
    // 1. Hypotension / Low Blood Pressure (Check first to avoid falling into normal)
    if (systolic < 90 || diastolic < 60) {
        val urgency = if (systolic < 80 || diastolic < 50) "MEDICAL_ATTENTION" else "NEEDS_IMPROVEMENT"
        return BpClinicalEvaluation("Low Blood Pressure (Hypotension)", urgency, "Alert", "#E53935")
    }
    // 2. Hypertensive Crisis (Emergency)
    if (systolic >= 180 || diastolic >= 120) {
        return BpClinicalEvaluation("Hypertensive Crisis", "MEDICAL_ATTENTION", "Alert", "#D32F2F")
    }
    // 3. Stage 2 Hypertension (Sys >= 140 OR Dia >= 90)
    if (systolic >= 140 || diastolic >= 90) {
        return BpClinicalEvaluation("Stage 2 Hypertension", "NEEDS_IMPROVEMENT", "Needs Attention", "#F57C00")
    }
    // 4. Stage 1 Hypertension (Sys 130-139 OR Dia 80-89)
    if ((systolic in 130..139) || (diastolic in 80..89)) {
        return BpClinicalEvaluation("Stage 1 Hypertension", "NEEDS_IMPROVEMENT", "Needs Attention", "#FFA000")
    }
    // 5. Elevated Blood Pressure (Sys 120-129 AND Dia < 80)
    if (systolic in 120..129 && diastolic < 80) {
        return BpClinicalEvaluation("Elevated Blood Pressure", "NORMAL", "Needs Attention", "#FFB300")
    }
    // 6. Optimal / Normal Blood Pressure (Sys 90-119 AND Dia 60-79)
    if (systolic in 90..119 && diastolic in 60..79) {
        return BpClinicalEvaluation("Optimal Blood Pressure", "NORMAL", "Optimal", "#43A047")
    }
    return BpClinicalEvaluation("Normal Blood Pressure", "NORMAL", "Optimal", "#43A047")
}

data class Spo2Summary(
    @SerializedName("percentage")
    val percentage: Int = 0,
    @SerializedName("status")
    val status: String = "Tap to log",
    @SerializedName("trend")
    val trend: String? = null,
    @SerializedName("recorded_at")
    val recordedAt: String = "",
    @SerializedName("has_data")
    val hasData: Boolean = false
)

data class BmiSummary(
    @SerializedName("value")
    val value: Float = 0f,
    @SerializedName("category")
    val category: String = "Not set",
    @SerializedName("height_cm")
    val heightCm: Float = 0f,
    @SerializedName("weight_kg")
    val weightKg: Float = 0f
)

data class StepsSummary(
    @SerializedName("current")
    val current: Int = 0,
    @SerializedName("goal")
    val goal: Int = 8000,
    @SerializedName("distance_km")
    val distanceKm: Float = 0f,
    @SerializedName("calories_kcal")
    val caloriesKcal: Int = 0,
    @SerializedName("progress_percent")
    val progressPercent: Int = 0
)

data class HydrationSummary(
    @SerializedName("consumed_ml")
    val consumedMl: Int = 0,
    @SerializedName("target_ml")
    val targetMl: Int = 2000,
    @SerializedName("glasses")
    val glasses: Float = 0f,
    @SerializedName("progress_percent")
    val progressPercent: Int = 0
)

data class SleepSummary(
    @SerializedName("duration_minutes")
    val durationMinutes: Int = 0,
    @SerializedName("duration_formatted")
    val durationFormatted: String = "--",
    @SerializedName("sleep_score")
    val sleepScore: Int = 0,
    @SerializedName("quality")
    val quality: String = "No record",
    @SerializedName("bedtime")
    val bedtime: String = "",
    @SerializedName("wake_time")
    val wakeTime: String = ""
)

data class VitalsSummaryData(
    @SerializedName("heart_rate")
    val heartRate: HeartRateSummary = HeartRateSummary(),
    @SerializedName("blood_pressure")
    val bloodPressure: BloodPressureSummary = BloodPressureSummary(),
    @SerializedName("spo2")
    val spo2: Spo2Summary = Spo2Summary(),
    @SerializedName("bmi")
    val bmi: BmiSummary = BmiSummary(),
    @SerializedName("steps")
    val steps: StepsSummary = StepsSummary(),
    @SerializedName("hydration")
    val hydration: HydrationSummary = HydrationSummary(),
    @SerializedName("sleep")
    val sleep: SleepSummary = SleepSummary()
)

data class VitalsSummaryResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("wellness_score")
    val wellnessScore: Int = 0,
    @SerializedName("wellness_status")
    val wellnessStatus: String = "No data recorded",
    @SerializedName("summary")
    val summary: VitalsSummaryData = VitalsSummaryData()
)

data class ChartPointDto(
    @SerializedName("bpm")
    val bpm: Int = 0,
    @SerializedName("time")
    val time: String = ""
)

data class HeartRateDetailData(
    @SerializedName("current_bpm")
    val currentBpm: Int = 0,
    @SerializedName("status")
    val status: String = "No data available",
    @SerializedName("resting_bpm")
    val restingBpm: Int = 0,
    @SerializedName("min_bpm")
    val minBpm: Int = 0,
    @SerializedName("max_bpm")
    val maxBpm: Int = 0,
    @SerializedName("avg_bpm")
    val avgBpm: Int = 0,
    @SerializedName("recorded_at")
    val recordedAt: String = "",
    @SerializedName("chart_points")
    val chartPoints: List<ChartPointDto> = emptyList()
)

data class HeartRateResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("heart_rate")
    val heartRate: HeartRateDetailData? = null
)

data class BloodPressureDetailItem(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("systolic", alternate = ["systolic_bp", "systolicBp"])
    val systolic: Int = 0,
    @SerializedName("diastolic", alternate = ["diastolic_bp", "diastolicBp"])
    val diastolic: Int = 0,
    @SerializedName("formatted")
    val formatted: String = "--/-- mmHg",
    @SerializedName("status")
    val status: String = "No data available",
    @SerializedName("source")
    val source: String = "",
    @SerializedName("recorded_at")
    val recordedAt: String = ""
)

data class BloodPressureResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("latest")
    val latest: BloodPressureDetailItem? = null,
    @SerializedName("history")
    val history: List<BloodPressureDetailItem> = emptyList()
)

data class Spo2DetailItem(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("percentage")
    val percentage: Int = 0,
    @SerializedName("formatted")
    val formatted: String = "--%",
    @SerializedName("status")
    val status: String = "No data available",
    @SerializedName("source")
    val source: String = "",
    @SerializedName("recorded_at")
    val recordedAt: String = ""
)

data class Spo2Response(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("latest")
    val latest: Spo2DetailItem? = null,
    @SerializedName("history")
    val history: List<Spo2DetailItem> = emptyList()
)

data class HealthyWeightRangeDto(
    @SerializedName("min")
    val min: Float = 0f,
    @SerializedName("max")
    val max: Float = 0f
)

data class BmiDetailData(
    @SerializedName("value")
    val value: Float = 0f,
    @SerializedName("category")
    val category: String = "Not set",
    @SerializedName("height_cm")
    val heightCm: Float = 0f,
    @SerializedName("weight_kg")
    val weightKg: Float = 0f,
    @SerializedName("age")
    val age: Int = 0,
    @SerializedName("gender")
    val gender: String = "",
    @SerializedName("healthy_weight_range_kg")
    val healthyWeightRange: HealthyWeightRangeDto = HealthyWeightRangeDto()
)

data class BmiResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("bmi")
    val bmi: BmiDetailData? = null
)

data class VitalReadingItem(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("reading_type")
    val readingType: String = "",
    @SerializedName("value_numeric")
    val valueNumeric: Float? = null,
    @SerializedName("value_secondary")
    val valueSecondary: Float? = null,
    @SerializedName("value_text")
    val valueText: String? = null,
    @SerializedName("unit")
    val unit: String? = null,
    @SerializedName("status_label")
    val statusLabel: String? = null,
    @SerializedName("source")
    val source: String = "Manual Entry",
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("recorded_at")
    val recordedAt: String = ""
)

data class VitalsReadingsResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("count")
    val count: Int = 0,
    @SerializedName("readings")
    val readings: List<VitalReadingItem> = emptyList()
)

data class AddVitalReadingRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("reading_type")
    val readingType: String,
    @SerializedName("value_numeric")
    val valueNumeric: Float? = null,
    @SerializedName("value_secondary")
    val valueSecondary: Float? = null,
    @SerializedName("value_text")
    val valueText: String? = null,
    @SerializedName("unit")
    val unit: String? = null,
    @SerializedName("status_label")
    val statusLabel: String? = null,
    @SerializedName("source")
    val source: String = "Manual Entry",
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("recorded_at")
    val recordedAt: String? = null
)

data class AddVitalReadingResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("reading")
    val reading: VitalReadingItem? = null
)

data class DeleteVitalReadingResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null
)

data class TrendDayDto(
    @SerializedName("day")
    val day: String = "M",
    @SerializedName("date")
    val date: String = "",
    @SerializedName("bpm")
    val bpm: Int? = null,
    @SerializedName("max_bpm")
    val maxBpm: Int? = null,
    @SerializedName("min_bpm")
    val minBpm: Int? = null,
    @SerializedName("category")
    val category: String = "empty",
    @SerializedName("has_data")
    val hasData: Boolean = false,
    @SerializedName("is_today")
    val isToday: Boolean = false
)

data class VitalAnalyticsData(
    @SerializedName("user_id")
    val userId: Int = 0,
    @SerializedName("timeframe")
    val timeframe: String = "week",
    @SerializedName("metric")
    val metric: String = "heart_rate",
    @SerializedName("current_bpm")
    val currentBpm: Int? = null,
    @SerializedName("formatted_time")
    val formattedTime: String = "--",
    @SerializedName("bpm_status")
    val bpmStatus: String = "No data",
    @SerializedName("status_level")
    val statusLevel: String = "empty",
    @SerializedName("average_bpm")
    val averageBpm: Int? = null,
    @SerializedName("highest_bpm")
    val highestBpm: Int? = null,
    @SerializedName("lowest_bpm")
    val lowestBpm: Int? = null,
    @SerializedName("resting_bpm")
    val restingBpm: Int? = null,
    @SerializedName("trend_days")
    val trendDays: List<TrendDayDto> = emptyList(),
    @SerializedName("has_trend_data")
    val hasTrendData: Boolean = false,
    @SerializedName("ai_insight")
    val aiInsight: String = "No readings recorded this week. Wear your smartwatch to track baseline trends."
)

data class VitalAnalyticsResponse(
    @SerializedName("status")
    val status: String = "error",
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String = "",
    @SerializedName("data")
    val data: VitalAnalyticsData? = null
)

data class SyncVitalReadingItem(
    @SerializedName("heart_rate")
    val heartRate: Int? = null,
    @SerializedName("spo2")
    val spo2: Int? = null,
    @SerializedName("systolic_bp")
    val systolicBp: Int? = null,
    @SerializedName("diastolic_bp")
    val diastolicBp: Int? = null,
    @SerializedName("source")
    val source: String = "bluetooth",
    @SerializedName("logged_at")
    val loggedAt: String? = null
)

data class SyncVitalsRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("mac_address")
    val macAddress: String? = null,
    @SerializedName("readings")
    val readings: List<SyncVitalReadingItem> = emptyList()
)

data class SyncVitalsResponse(
    @SerializedName("status")
    val status: String = "error",
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String = "",
    @SerializedName("synced_count")
    val syncedCount: Int = 0
)

data class SleepLogEntryDto(
    @SerializedName("date")
    val date: String = "",
    @SerializedName("duration_minutes")
    val durationMinutes: Int = 0,
    @SerializedName("sleep_score")
    val sleepScore: Int = 0,
    @SerializedName("quality", alternate = ["quality_label"])
    val quality: String = "No Log"
)

data class SleepPayloadDto(
    @SerializedName("date")
    val date: String = "",
    @SerializedName("duration_minutes")
    val durationMinutes: Int = 0,
    @SerializedName("duration_formatted")
    val durationFormatted: String = "--",
    @SerializedName("sleep_score")
    val sleepScore: Int = 0,
    @SerializedName("quality", alternate = ["quality_label"])
    val quality: String = "No record",
    @SerializedName("bedtime")
    val bedtime: String = "",
    @SerializedName("wake_time")
    val wakeTime: String = "",
    @SerializedName("recent_history")
    val recentHistory: List<SleepLogEntryDto> = emptyList()
)

data class SleepDetailResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("sleep")
    val sleep: SleepPayloadDto? = null
)

data class LogSleepRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("date_string")
    val dateString: String,
    @SerializedName("bedtime")
    val bedtime: String,
    @SerializedName("wake_time")
    val wakeTime: String,
    @SerializedName("duration_minutes")
    val durationMinutes: Int,
    @SerializedName("sleep_score")
    val sleepScore: Int,
    @SerializedName("quality_label")
    val qualityLabel: String,
    @SerializedName("action")
    val action: String? = null
)

data class LogSleepResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("duration_minutes")
    val durationMinutes: Int = 0,
    @SerializedName("sleep_score")
    val sleepScore: Int = 0,
    @SerializedName("quality")
    val quality: String? = null,
    @SerializedName("quality_label")
    val qualityLabel: String? = null
)

data class ManualVitalsLogRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("heart_rate")
    val heartRate: Int? = null,
    @SerializedName("spo2")
    val spo2: Int? = null,
    @SerializedName("systolic", alternate = ["systolic_bp", "systolicBp"])
    val systolic: Int? = null,
    @SerializedName("diastolic", alternate = ["diastolic_bp", "diastolicBp"])
    val diastolic: Int? = null,
    @SerializedName("notes")
    val notes: String? = null
)

data class ManualVitalsLogData(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("user_id")
    val userId: Int = 0,
    @SerializedName("heart_rate")
    val heartRate: Int? = null,
    @SerializedName("heart_rate_status")
    val heartRateStatus: String? = null,
    @SerializedName("spo2")
    val spo2: Int? = null,
    @SerializedName("spo2_status")
    val spo2Status: String? = null,
    @SerializedName("systolic", alternate = ["systolic_bp", "systolicBp"])
    val systolic: Int? = null,
    @SerializedName("diastolic", alternate = ["diastolic_bp", "diastolicBp"])
    val diastolic: Int? = null,
    @SerializedName("bp_status")
    val bpStatus: String? = null,
    @SerializedName("source")
    val source: String = "Manual Entry",
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("logged_at")
    val loggedAt: String = ""
)

data class ManualVitalsLogResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: ManualVitalsLogData? = null
)

data class VitalRecentLogDto(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("value")
    val value: String = "",
    @SerializedName("status")
    val status: String = "",
    @SerializedName("date")
    val date: String = "",
    @SerializedName("urgency")
    val urgency: String = "NORMAL"
)

data class VitalDetailGuidanceResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("has_data")
    val hasData: Boolean = false,
    @SerializedName("current_display")
    val currentDisplay: String = "--",
    @SerializedName("current_bpm")
    val currentBpm: Int? = null,
    @SerializedName("current_spo2")
    val currentSpo2: Int? = null,
    @SerializedName("systolic", alternate = ["systolic_bp", "systolicBp"])
    val systolic: Int? = null,
    @SerializedName("diastolic", alternate = ["diastolic_bp", "diastolicBp"])
    val diastolic: Int? = null,
    @SerializedName("status_title")
    val statusTitle: String = "No Readings Yet",
    @SerializedName("tier")
    val tier: String = "Optimal",
    @SerializedName("badge_color")
    val badgeColor: String = "#9E9E9E",
    @SerializedName("urgency")
    val urgency: String = "NORMAL",
    @SerializedName("recorded_at")
    val recordedAt: String = "",
    @SerializedName("explanation")
    val explanation: String = "",
    @SerializedName("recommended_activity")
    val recommendedActivity: String = "",
    @SerializedName("lifestyle_tip")
    val lifestyleTip: String = "",
    @SerializedName("recent_logs")
    val recentLogs: List<VitalRecentLogDto> = emptyList(),
    @SerializedName("model_used")
    val modelUsed: String? = null

)

