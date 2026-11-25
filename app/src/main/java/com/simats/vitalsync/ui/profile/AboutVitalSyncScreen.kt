package com.simats.vitalsync.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.theme.CarbonDarkBackground
import com.simats.vitalsync.ui.theme.CarbonDarkSurface
import com.simats.vitalsync.ui.theme.SplashBgDark
import com.simats.vitalsync.ui.theme.SplashBgLight
import com.simats.vitalsync.ui.theme.VitalTextSubtitle
import com.simats.vitalsync.ui.theme.VitalsyncTheme

private enum class AboutLegalDocument {
    PRIVACY_POLICY,
    TERMS_OF_SERVICE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutVitalSyncScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val isDark = MaterialTheme.colorScheme.background == CarbonDarkBackground || MaterialTheme.colorScheme.surface == CarbonDarkSurface
    var activeLegalDoc by remember { mutableStateOf<AboutLegalDocument?>(null) }

    if (activeLegalDoc != null) {
        val doc = activeLegalDoc!!
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { activeLegalDoc = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 22.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (doc == AboutLegalDocument.PRIVACY_POLICY) "Privacy Policy" else "Terms of Service",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Last updated: November 2026",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (doc == AboutLegalDocument.PRIVACY_POLICY) {
                    LegalSectionBlock(
                        title = "1. Health & Device Data Collection",
                        body = "VitalSync stores your heart rate, blood pressure, SpO2, steps, sleep, hydration, BMI, and workout sessions solely to calculate wellness trends and personalized guidance."
                    )
                    LegalSectionBlock(
                        title = "2. Local-First Encryption & Storage",
                        body = "Your daily health metrics, preferences, and activity logs stay protected locally on your device. Cloud synchronization occurs only when signed in with your account."
                    )
                    LegalSectionBlock(
                        title = "3. Health Connect & Bluetooth Access",
                        body = "Heart rate and wearable metrics from Health Connect or Bluetooth devices are read strictly with your explicit permission and are never accessed in the background without consent."
                    )
                    LegalSectionBlock(
                        title = "4. Your Ownership & Control",
                        body = "VitalSync never sells or rents your personal health data to advertisers or brokers. You can clear activity history or manage health permissions anytime."
                    )
                } else {
                    LegalSectionBlock(
                        title = "1. General Wellness Purpose Only",
                        body = "VitalSync is designed for everyday fitness, hydration, sleep, and wellness tracking. It is not a medical device and is not intended to diagnose, treat, cure, or prevent any disease."
                    )
                    LegalSectionBlock(
                        title = "2. Medical Disclaimer",
                        body = "Always consult a qualified physician or healthcare professional before acting on heart rate alerts, blood pressure readings, BMI guidance, or AI wellness insights."
                    )
                    LegalSectionBlock(
                        title = "3. Wearable & Sensor Accuracy",
                        body = "Step counts, GPS routes, calorie estimates, and synced smartwatch BPM readings depend on your device hardware and third-party companion apps."
                    )
                    LegalSectionBlock(
                        title = "4. Acceptable Use & Account Responsibility",
                        body = "You are responsible for keeping your login credentials secure and entering accurate profile measurements (height, weight, age) for reliable wellness calculations."
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { activeLegalDoc = null },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Close",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(36.dp)
            ) {
                BackArrowIcon(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "About VitalSync",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hero VitalSync Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SplashBgDark)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                SplashBgDark,
                                Color(0xFF075947),
                                SplashBgLight
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x28FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        HeartPulseIconWhite(modifier = Modifier.size(28.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "VitalSync",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Sync with your health",
                        fontSize = 13.sp,
                        color = VitalTextSubtitle
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Version 1.0",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "A little clarity. A healthier every day.",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "VitalSync helps you understand your health, build better habits, and take small steps toward feeling your best.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Highlights Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Your health, in one place", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = "Vitals, activity, sleep and hydration.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFFF59E0B)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Insights that make sense", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = "AI-powered guidance for everyday wellness.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // LEARN MORE Links Card
        Text(text = "LEARN MORE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                AboutLinkRow(
                    title = "Privacy Policy",
                    subtitle = "How your health metrics, activity logs, and personal data stay protected on your device.",
                    icon = Icons.Rounded.Description,
                    onClick = { activeLegalDoc = AboutLegalDocument.PRIVACY_POLICY }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                AboutLinkRow(
                    title = "Terms of Service",
                    subtitle = "Usage guidelines, health disclaimers, and terms for using VitalSync.",
                    icon = Icons.Rounded.Article,
                    onClick = { activeLegalDoc = AboutLegalDocument.TERMS_OF_SERVICE }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "VitalSync supports everyday health awareness, and is not a substitute for professional medical advice.\n© 2026 VitalSync. All rights reserved.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LegalSectionBlock(title: String, body: String) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = body,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun AboutLinkRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        ChevronRightIcon(modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun HeartPulseIconWhite(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val heartPath = Path().apply {
            moveTo(w * 0.5f, h * 0.82f)
            cubicTo(w * 0.15f, h * 0.58f, w * 0.02f, h * 0.30f, w * 0.25f, h * 0.14f)
            cubicTo(w * 0.38f, h * 0.06f, w * 0.48f, h * 0.16f, w * 0.50f, h * 0.24f)
            cubicTo(w * 0.52f, h * 0.16f, w * 0.62f, h * 0.06f, w * 0.75f, h * 0.14f)
            cubicTo(w * 0.98f, h * 0.30f, w * 0.85f, h * 0.58f, w * 0.5f, h * 0.82f)
            close()
        }

        drawPath(path = heartPath, color = Color.White, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun ChevronRightIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.3f, h * 0.2f)
            lineTo(w * 0.7f, h * 0.5f)
            lineTo(w * 0.3f, h * 0.8f)
        }

        drawPath(path = path, color = Color(0xFF9CA3AF), style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun AboutVitalSyncScreenPreview() {
    VitalsyncTheme {
        AboutVitalSyncScreen()
    }
}
