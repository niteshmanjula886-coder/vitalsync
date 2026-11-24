package com.simats.vitalsync.ui.vitals

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private data class BmiTipItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color
)

private data class BmiTipsConfig(
    val headerSubtitle: String,
    val badgeText: String,
    val badgeBg: Color,
    val badgeColor: Color,
    val tips: List<BmiTipItem>
)

@Composable
fun BmiDetailScreen(
    modifier: Modifier = Modifier,
    bmiValue: String = "--",
    bmiCategory: String = "Not set",
    heightCm: String = "--",
    weightKg: String = "--",
    onBackClick: () -> Unit = {},
    onUpdateMetricsClick: () -> Unit = {}
) {
    // 1. Dynamic BMI and category calculation
    val hFloat = heightCm.toFloatOrNull() ?: 0f
    val wFloat = weightKg.toFloatOrNull() ?: 0f
    val liveBmi = if (hFloat > 50f && wFloat > 15f) {
        wFloat / ((hFloat / 100f) * (hFloat / 100f))
    } else {
        bmiValue.toFloatOrNull() ?: 22.4f
    }
    val liveBmiFormatted = String.format(Locale.US, "%.1f", liveBmi)

    val dynamicCategory = when {
        liveBmi < 18.5f -> "Underweight"
        liveBmi <= 24.9f -> "Healthy Range"
        liveBmi <= 29.9f -> "Overweight"
        else -> "Obese"
    }

    val dynamicCategoryColor = when {
        liveBmi < 18.5f -> Color(0xFF38BDF8) // Sky Blue
        liveBmi <= 24.9f -> Color(0xFF0F9F7A) // Vibrant Emerald/Teal
        liveBmi <= 29.9f -> Color(0xFFF59E0B) // Amber
        else -> Color(0xFFEF4444) // Red
    }

    // Vertical Trend Needle Fraction on spectrum bar (15.0 to 35.0 scale)
    val minScale = 15.0f
    val maxScale = 35.0f
    val targetNeedleFraction = ((liveBmi - minScale) / (maxScale - minScale)).coerceIn(0.02f, 0.98f)
    val animatedNeedleFraction by animateFloatAsState(
        targetValue = targetNeedleFraction,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "BmiNeedleFractionAnimation"
    )

    // Dynamic actionable health & weight guidance adapted to BMI category
    val tipsConfig = remember(liveBmi) {
        when {
            liveBmi < 18.5f -> {
                val list = listOf(
                    BmiTipItem(
                        title = "Nutrient-Dense Caloric Surplus",
                        description = "Aim for a moderate daily surplus of 300–500 kcal with wholesome, energy-dense foods such as nuts, avocados, whole grains, and olive oil.",
                        icon = Icons.Rounded.Restaurant,
                        iconBg = Color(0xFFE0F2FE),
                        iconTint = Color(0xFF0284C7)
                    ),
                    BmiTipItem(
                        title = "Progressive Resistance Training",
                        description = "Engage in strength and compound exercises 3–4 times weekly to channel excess calories into functional lean muscle mass.",
                        icon = Icons.Rounded.FitnessCenter,
                        iconBg = Color(0xFFE0F2FE),
                        iconTint = Color(0xFF0284C7)
                    ),
                    BmiTipItem(
                        title = "Adequate High-Quality Protein",
                        description = "Target 1.6–2.0 g of protein per kilogram of body weight each day to fuel muscle hypertrophy and cellular recovery.",
                        icon = Icons.Rounded.Lightbulb,
                        iconBg = Color(0xFFE0F2FE),
                        iconTint = Color(0xFF0284C7)
                    ),
                    BmiTipItem(
                        title = "Consistent Sleep & Recovery",
                        description = "Prioritize 7–9 hours of restful sleep every night to optimize anabolic hormone release and physical regeneration.",
                        icon = Icons.Rounded.Bedtime,
                        iconBg = Color(0xFFE0F2FE),
                        iconTint = Color(0xFF0284C7)
                    )
                )
                BmiTipsConfig(
                    headerSubtitle = "Guidance tailored for healthy weight gain",
                    badgeText = "Healthy Surplus",
                    badgeBg = Color(0xFFE0F2FE),
                    badgeColor = Color(0xFF0284C7),
                    tips = list
                )
            }
            liveBmi <= 24.9f -> {
                val list = listOf(
                    BmiTipItem(
                        title = "Balanced Macronutrient Equilibrium",
                        description = "Maintain your current caloric balance with diverse whole foods, lean proteins, colorful vegetables, and healthy fats.",
                        icon = Icons.Rounded.Restaurant,
                        iconBg = Color(0xFFE8F6F0),
                        iconTint = Color(0xFF0F9F7A)
                    ),
                    BmiTipItem(
                        title = "Active Daily Cardio & Steps",
                        description = "Aim for at least 150 minutes of moderate aerobic activity weekly alongside maintaining 8,000+ daily steps.",
                        icon = Icons.Rounded.DirectionsRun,
                        iconBg = Color(0xFFE8F6F0),
                        iconTint = Color(0xFF0F9F7A)
                    ),
                    BmiTipItem(
                        title = "Optimal Hydration Schedule",
                        description = "Drink 2.5–3.0 liters of water daily to maintain peak metabolic function, cellular hydration, and sustained energy.",
                        icon = Icons.Rounded.WaterDrop,
                        iconBg = Color(0xFFE8F6F0),
                        iconTint = Color(0xFF0F9F7A)
                    ),
                    BmiTipItem(
                        title = "Routine Metabolic Consistency",
                        description = "Keep consistent meal and exercise timing to prevent weight drift and support cardiovascular longevity.",
                        icon = Icons.Rounded.CheckCircle,
                        iconBg = Color(0xFFE8F6F0),
                        iconTint = Color(0xFF0F9F7A)
                    )
                )
                BmiTipsConfig(
                    headerSubtitle = "Guidance tailored for weight maintenance",
                    badgeText = "Optimal Maintenance",
                    badgeBg = Color(0xFFE8F6F0),
                    badgeColor = Color(0xFF0F9F7A),
                    tips = list
                )
            }
            liveBmi <= 29.9f -> {
                val list = listOf(
                    BmiTipItem(
                        title = "Controlled Caloric Deficit",
                        description = "Create a sustainable daily deficit of 300–500 kcal through mindful portion management and eliminating liquid calories.",
                        icon = Icons.Rounded.LocalFireDepartment,
                        iconBg = Color(0xFFFEF3C7),
                        iconTint = Color(0xFFD97706)
                    ),
                    BmiTipItem(
                        title = "High Fiber & Lean Protein",
                        description = "Incorporate 25–35 g of dietary fiber and ample lean protein at each meal to enhance satiety and preserve lean muscle.",
                        icon = Icons.Rounded.Restaurant,
                        iconBg = Color(0xFFFEF3C7),
                        iconTint = Color(0xFFD97706)
                    ),
                    BmiTipItem(
                        title = "Progressive Daily Activity",
                        description = "Elevate your step count toward 10,000 daily steps and include 30–45 minutes of brisk walking or cycling.",
                        icon = Icons.Rounded.DirectionsWalk,
                        iconBg = Color(0xFFFEF3C7),
                        iconTint = Color(0xFFD97706)
                    ),
                    BmiTipItem(
                        title = "Resistance Training Support",
                        description = "Lift weights 2–3 times a week to keep your resting metabolic rate high while body fat decreases.",
                        icon = Icons.Rounded.FitnessCenter,
                        iconBg = Color(0xFFFEF3C7),
                        iconTint = Color(0xFFD97706)
                    )
                )
                BmiTipsConfig(
                    headerSubtitle = "Guidance tailored for healthy weight reduction",
                    badgeText = "Active Deficit",
                    badgeBg = Color(0xFFFEF3C7),
                    badgeColor = Color(0xFFD97706),
                    tips = list
                )
            }
            else -> {
                val list = listOf(
                    BmiTipItem(
                        title = "Structured Caloric Reduction",
                        description = "Focus on whole, unprocessed foods and a steady 500 kcal deficit to promote safe and sustainable fat loss.",
                        icon = Icons.Rounded.LocalFireDepartment,
                        iconBg = Color(0xFFFEE2E2),
                        iconTint = Color(0xFFDC2626)
                    ),
                    BmiTipItem(
                        title = "Low-Impact Cardio Movement",
                        description = "Begin with joint-friendly exercises such as brisk walking, swimming, or stationary cycling 30 minutes daily.",
                        icon = Icons.Rounded.DirectionsWalk,
                        iconBg = Color(0xFFFEE2E2),
                        iconTint = Color(0xFFDC2626)
                    ),
                    BmiTipItem(
                        title = "Prioritize Protein & Fiber",
                        description = "Eat vegetables, legumes, and lean poultry to stabilize blood glucose levels and prevent intense hunger spikes.",
                        icon = Icons.Rounded.Restaurant,
                        iconBg = Color(0xFFFEE2E2),
                        iconTint = Color(0xFFDC2626)
                    ),
                    BmiTipItem(
                        title = "Sleep & Stress Management",
                        description = "Aim for 7–8 hours of consistent sleep and stress reduction practices to regulate cortisol and metabolic health.",
                        icon = Icons.Rounded.Bedtime,
                        iconBg = Color(0xFFFEE2E2),
                        iconTint = Color(0xFFDC2626)
                    )
                )
                BmiTipsConfig(
                    headerSubtitle = "Guidance tailored for actionable weight loss",
                    badgeText = "Weight Reduction",
                    badgeBg = Color(0xFFFEE2E2),
                    badgeColor = Color(0xFFDC2626),
                    tips = list
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFCFB))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        BackArrowIcon(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF111827)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "BMI (Body Mass Index)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    CalendarIcon(modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Current BMI Score Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current BMI Score",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(dynamicCategoryColor.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = dynamicCategory,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = dynamicCategoryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = liveBmiFormatted,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Height: $heightCm cm · Weight: $weightKg kg",
                        fontSize = 13.sp,
                        color = Color(0xFF374151),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // BMI Spectrum Color Gauge Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BMI Spectrum Range",
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF)
                        )
                        Text(
                            text = "Score: $liveBmiFormatted",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = dynamicCategoryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // BMI Spectrum Bar with Vertical Trend Indicator Needle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                    ) {
                        // Horizontal Spectrum Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .align(Alignment.BottomCenter)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            Box(modifier = Modifier.weight(3.5f).fillMaxHeight().background(Color(0xFF38BDF8)))
                            Box(modifier = Modifier.weight(6.4f).fillMaxHeight().background(Color(0xFF0F9F7A)))
                            Box(modifier = Modifier.weight(5.0f).fillMaxHeight().background(Color(0xFFF59E0B)))
                            Box(modifier = Modifier.weight(5.1f).fillMaxHeight().background(Color(0xFFEF4444)))
                        }

                        // Vertical Trend Indicator Needle
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                                .align(Alignment.TopCenter)
                        ) {
                            val needleX = (size.width * animatedNeedleFraction).coerceIn(4.dp.toPx(), size.width - 4.dp.toPx())

                            // Vertical line piercing through the spectrum bar
                            drawLine(
                                color = Color(0xFF111827),
                                start = Offset(needleX, 6.dp.toPx()),
                                end = Offset(needleX, size.height),
                                strokeWidth = 2.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            // Top downward-pointing triangle needle head
                            val pointer = Path().apply {
                                moveTo(needleX, 10.dp.toPx())
                                lineTo(needleX - 5.dp.toPx(), 0f)
                                lineTo(needleX + 5.dp.toPx(), 0f)
                                close()
                            }
                            drawPath(path = pointer, color = Color(0xFF111827))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "< 18.5", fontSize = 10.sp, color = Color(0xFF38BDF8))
                        Text(text = "18.5 – 24.9", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A))
                        Text(text = "25.0 – 29.9", fontSize = 10.sp, color = Color(0xFFF59E0B))
                        Text(text = "≥ 30.0", fontSize = 10.sp, color = Color(0xFFEF4444))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Recommended Tips Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Recommended Tips",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tipsConfig.headerSubtitle,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(tipsConfig.badgeBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tipsConfig.badgeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = tipsConfig.badgeColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    tipsConfig.tips.forEachIndexed { index, tip ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF9FAFB))
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(tip.iconBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tip.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = tip.iconTint
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tip.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = tip.description,
                                    fontSize = 12.sp,
                                    color = Color(0xFF4B5563),
                                    lineHeight = 17.sp
                                )
                            }
                        }

                        if (index < tipsConfig.tips.size - 1) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BMI Categories Reference Card with Dynamic Selection & Vertical Accent Indicator
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BMI categories",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "WHO Standard",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF9CA3AF)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val isUnderweight = liveBmi < 18.5f
                    val isNormal = liveBmi in 18.5f..24.9f
                    val isOverweight = liveBmi in 25.0f..29.9f
                    val isObese = liveBmi >= 30.0f

                    BmiCategoryRow(
                        range = "< 18.5",
                        category = "Underweight",
                        color = Color(0xFF38BDF8),
                        isCurrent = isUnderweight,
                        currentBmiText = if (isUnderweight) liveBmiFormatted else ""
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 8.dp))

                    BmiCategoryRow(
                        range = "18.5 – 24.9",
                        category = "Healthy / Normal weight",
                        color = Color(0xFF0F9F7A),
                        isCurrent = isNormal,
                        currentBmiText = if (isNormal) liveBmiFormatted else ""
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 8.dp))

                    BmiCategoryRow(
                        range = "25.0 – 29.9",
                        category = "Overweight",
                        color = Color(0xFFF59E0B),
                        isCurrent = isOverweight,
                        currentBmiText = if (isOverweight) liveBmiFormatted else ""
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 8.dp))

                    BmiCategoryRow(
                        range = "≥ 30.0",
                        category = "Obese",
                        color = Color(0xFFEF4444),
                        isCurrent = isObese,
                        currentBmiText = if (isObese) liveBmiFormatted else ""
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Action Button
        Button(
            onClick = onUpdateMetricsClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VitalTealDark,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "+ Update Weight & Height",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BmiCategoryRow(
    range: String,
    category: String,
    color: Color,
    isCurrent: Boolean,
    currentBmiText: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isCurrent) Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE8F6F0))
                    .border(1.dp, Color(0xFF0F9F7A).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                else Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Vertical accent indicator line
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = category,
                    fontSize = 14.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                    color = Color(0xFF111827)
                )
                if (isCurrent && currentBmiText.isNotBlank()) {
                    Text(
                        text = "Current State • $currentBmiText",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F9F7A)
                    )
                }
            }
        }

        Text(
            text = range,
            fontSize = 13.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrent) Color(0xFF0F9F7A) else Color(0xFF6B7280)
        )
    }
}

@Composable
private fun BackArrowIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Black
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.75f, h * 0.5f)
            lineTo(w * 0.25f, h * 0.5f)
            moveTo(w * 0.45f, h * 0.25f)
            lineTo(w * 0.25f, h * 0.5f)
            lineTo(w * 0.45f, h * 0.75f)
        }

        drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun CalendarIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val calPath = Path().apply {
            moveTo(0f, h * 0.25f)
            lineTo(w, h * 0.25f)
            lineTo(w, h * 0.85f)
            lineTo(0f, h * 0.85f)
            close()
        }

        drawPath(path = calPath, color = Color(0xFF6B7280), style = Stroke(width = 1.6.dp.toPx()))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun BmiDetailScreenPreview() {
    VitalsyncTheme {
        BmiDetailScreen()
    }
}
