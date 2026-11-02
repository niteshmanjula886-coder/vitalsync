package com.simats.vitalsync.data.model.vitals

import com.google.gson.annotations.SerializedName

data class HeartRateSummary(
    @SerializedName("bpm")
    val bpm: Int = 0,
    @SerializedName("status")
    val status: String = "No readings yet",
    @SerializedName("resting_bpm")
    val restingBpm: Int = 0,
    @SerializedName("min_bpm")
    val minBpm: Int = 0,
    @SerializedName("max_bpm")
    val maxBpm: Int = 0,
    @SerializedName("recorded_at")
    val recordedAt: String = ""
)

data class BloodPressureSummary(
    @SerializedName("systolic")
    val systolic: Int = 0,
    @SerializedName("diastolic")
    val diastolic: Int = 0,
    @SerializedName("text")
    val text: String = "--/-- mmHg",
    @SerializedName("status")
    val status: String = "No readings yet",
    @SerializedName("recorded_at")
    val recordedAt: String = ""
)

data class Spo2Summary(
    @SerializedName("percentage")
    val percentage: Int = 0,
    @SerializedName("status")
    val status: String = "No readings yet",
    @SerializedName("recorded_at")
    val recordedAt: String = ""
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
    @SerializedName("systolic")
    val systolic: Int = 0,
    @SerializedName("diastolic")
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

