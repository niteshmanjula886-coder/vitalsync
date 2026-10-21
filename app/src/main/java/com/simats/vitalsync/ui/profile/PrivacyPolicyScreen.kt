package com.simats.vitalsync.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.theme.VitalsyncTheme

@Composable
fun PrivacyPolicyScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFCFB))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
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
                    color = Color(0xFF111827)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Privacy Policy",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Last updated: October 15, 2025",
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Policy Section 1: Data Collection
        PolicySectionCard(
            title = "1. Data Collection",
            content = "VitalSync collects device health metrics (Heart Rate, Blood Pressure, SpO₂, Step Count, Activity, Sleep, Hydration, and Gym Logs) exclusively to provide personalized wellness insights and calculations."
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Policy Section 2: Storage & Security
        PolicySectionCard(
            title = "2. Encryption & Local Storage",
            content = "Your personal health logs are encrypted at rest using AES-256 standard encryption and stored locally on your device. VitalSync operates on a privacy-first local repository architecture."
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Policy Section 3: Data Ownership
        PolicySectionCard(
            title = "3. Your Privacy Rights",
            content = "You have full ownership of your health records. You can export, modify, or clear your nutrition and workout history at any time directly through app settings."
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Policy Section 4: Third-Party Sharing
        PolicySectionCard(
            title = "4. Third-Party Sharing",
            content = "VitalSync does NOT sell, rent, or trade your personal health logs with third-party advertisers or data brokers. Integrations with Health Connect occur strictly with your explicit permission."
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Verified Shield Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFE6F4EA))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🛡️", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Encrypted & Verified Privacy",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46)
                    )
                    Text(
                        text = "Your data stays private to you. Learn more in Help & Support.",
                        fontSize = 11.sp,
                        color = Color(0xFF047857)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun PolicySectionCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = content,
                fontSize = 13.sp,
                color = Color(0xFF4B5563),
                lineHeight = 18.sp
            )
        }
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
fun PrivacyPolicyScreenPreview() {
    VitalsyncTheme {
        PrivacyPolicyScreen()
    }
}
