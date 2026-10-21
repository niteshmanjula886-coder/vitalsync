package com.simats.vitalsync.ui.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onHealthPermissionsClick: () -> Unit = {},
    onRemindersClick: () -> Unit = {},
    onDataManagementClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onVersionClick: () -> Unit = {}
) {
    var pushNotifications by remember { mutableStateOf(true) }
    var reminders by remember { mutableStateOf(true) }
    var aiInsights by remember { mutableStateOf(true) }
    var isDarkTheme by remember { mutableStateOf(false) }

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
                text = "Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ACCOUNT Section
        SettingsSectionHeader("ACCOUNT")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            SettingsNavigationRow("🔒 Change Password", onClick = onChangePasswordClick)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // HEALTH Section
        SettingsSectionHeader("HEALTH")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            SettingsNavigationRow("💚 Health Permissions", onClick = onHealthPermissionsClick)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // NOTIFICATIONS Section
        SettingsSectionHeader("NOTIFICATIONS")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                SettingsSwitchRow("🔔 Push Notifications", pushNotifications) { pushNotifications = it }
                HorizontalDivider(color = Color(0xFFF8FAFC))
                
                // Reminders row with Edit button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onRemindersClick() }
                    ) {
                        Text(text = "⏰ Reminders", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1F2937))
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE6F4EA))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Edit ✏️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A))
                        }
                    }

                    Switch(
                        checked = reminders,
                        onCheckedChange = { reminders = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = VitalTealPrimary
                        )
                    )
                }

                HorizontalDivider(color = Color(0xFFF8FAFC))
                SettingsSwitchRow("✨ AI Insights", aiInsights) { aiInsights = it }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // APPEARANCE Section
        SettingsSectionHeader("APPEARANCE")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                SettingsRadioRow("☀️ Light Mode", isSelected = !isDarkTheme) { isDarkTheme = false }
                HorizontalDivider(color = Color(0xFFF8FAFC))
                SettingsRadioRow("🌙 Dark Mode", isSelected = isDarkTheme) { isDarkTheme = true }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PRIVACY Section
        SettingsSectionHeader("PRIVACY")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            SettingsNavigationRow("🛡️ Data Management", onClick = onDataManagementClick)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ABOUT Section
        SettingsSectionHeader("ABOUT")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                SettingsNavigationRow("ℹ️ About VitalSync", onClick = onAboutClick)
                HorizontalDivider(color = Color(0xFFF8FAFC))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onVersionClick() }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⚙️ Version", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1F2937))
                    Text(text = "1.0", fontSize = 14.sp, color = Color(0xFF9CA3AF))
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
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
private fun SettingsNavigationRow(
    title: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1F2937))
        ChevronRightIcon(modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1F2937))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = VitalTealPrimary
            )
        )
    }
}

@Composable
private fun SettingsRadioRow(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1F2937))

        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isSelected) VitalTealPrimary else Color.Transparent)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
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
fun SettingsScreenPreview() {
    VitalsyncTheme {
        SettingsScreen()
    }
}
