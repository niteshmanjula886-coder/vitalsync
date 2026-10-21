package com.simats.vitalsync.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.simats.vitalsync.ui.theme.VitalTealPrimary
import com.simats.vitalsync.ui.theme.VitalsyncTheme

@Composable
fun HelpSupportScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

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
                text = "Help & Support",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "How can we help?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Tap any topic or guide below to view detailed descriptions.",
            fontSize = 13.sp,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search topics or questions", fontSize = 14.sp, color = Color(0xFF9CA3AF)) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = VitalTealPrimary,
                unfocusedBorderColor = Color(0xFFE5E7EB)
            ),
            trailingIcon = { Text("🔍", fontSize = 16.sp) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // FREQUENTLY ASKED QUESTIONS Section
        SectionHeader("FREQUENTLY ASKED QUESTIONS")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                ExpandableSupportRow(
                    title = "💗 Wellness Tracking",
                    description = "Track your daily vitals including Heart Rate, Blood Pressure, SpO₂, Step Count, Activity, Sleep, and Hydration. VitalSync automatically analyzes your trends over time to give you a personalized Wellness Score out of 100."
                )
                HorizontalDivider(color = Color(0xFFF8FAFC), modifier = Modifier.padding(horizontal = 16.dp))
                ExpandableSupportRow(
                    title = "📱 Devices & Synchronization",
                    description = "Seamlessly sync with your Smart Watch, Fitness Band, Pulse Oximeter, or Health Connect. Your readings update automatically in real-time so your health profile is always up to date."
                )
                HorizontalDivider(color = Color(0xFFF8FAFC), modifier = Modifier.padding(horizontal = 16.dp))
                ExpandableSupportRow(
                    title = "🛡️ Account & Privacy",
                    description = "Your health data is encrypted and stored securely on your device. You have full control over data sharing, biometric security, and account management from your Privacy & Security settings."
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // GUIDES Section
        SectionHeader("GUIDES")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                ExpandableSupportRow(
                    title = "📖 Getting Started with VitalSync",
                    description = "Welcome to VitalSync! Start by completing your profile setup, logging your first reading or workout session, setting up daily reminders, and asking VitalSync AI for personalized health insights."
                )
                HorizontalDivider(color = Color(0xFFF8FAFC), modifier = Modifier.padding(horizontal = 16.dp))
                ExpandableSupportRow(
                    title = "⌚ Connect Your First Device",
                    description = "Go to Connected Devices under Profile settings, ensure Bluetooth is turned on, tap Connect next to your wearable device, and follow the simple pairing prompt to sync your health data."
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Contact Support Expandable Card
        ExpandableContactSupportCard()

        Spacer(modifier = Modifier.height(20.dp))

        // Emergency Notice Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFE6F4EA))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "ℹ️", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "For urgent medical concerns, contact local emergency services. App support cannot provide medical advice.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF065F46)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun ExpandableSupportRow(
    title: String,
    description: String
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            ChevronIcon(
                isExpanded = isExpanded,
                modifier = Modifier.size(16.dp)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(12.dp)
                ) {
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = Color(0xFF4B5563),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandableContactSupportCard() {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "💬 Contact Support", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "We're here to help with app questions", fontSize = 12.sp, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Mon-Fri, 9 AM - 6 PM • Typical reply: 1-2 days", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                }

                ChevronIcon(isExpanded = isExpanded, modifier = Modifier.size(16.dp))
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFE8F6F0))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(text = "📧 Email: support@vitalsync.com", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "💬 In-App Helpdesk: Active 24/7", fontSize = 12.sp, color = Color(0xFF0F9F7A), fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Response Time: Under 24 hours. Our dedicated health-tech support team is ready to assist you!", fontSize = 12.sp, color = Color(0xFF4B5563))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF9CA3AF),
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
    )
}

@Composable
private fun ChevronIcon(
    isExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            if (isExpanded) {
                moveTo(w * 0.2f, h * 0.7f)
                lineTo(w * 0.5f, h * 0.3f)
                lineTo(w * 0.8f, h * 0.7f)
            } else {
                moveTo(w * 0.3f, h * 0.2f)
                lineTo(w * 0.7f, h * 0.5f)
                lineTo(w * 0.3f, h * 0.8f)
            }
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
fun HelpSupportScreenPreview() {
    VitalsyncTheme {
        HelpSupportScreen()
    }
}
