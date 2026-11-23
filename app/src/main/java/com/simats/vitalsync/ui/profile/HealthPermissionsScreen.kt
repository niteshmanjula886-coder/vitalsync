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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.simats.vitalsync.ui.theme.CarbonDarkBackground
import com.simats.vitalsync.ui.theme.CarbonDarkSurface
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HealthPermissionsScreen(
    modifier: Modifier = Modifier,
    vitalSigns: Boolean = true,
    activity: Boolean = true,
    sleep: Boolean = true,
    hydration: Boolean = false,
    writeHealthData: Boolean = true,
    lastSyncTimestamp: Long? = null,
    onRequestHealthPermissions: () -> Unit = {},
    onSavePermissions: (vitalSigns: Boolean, activity: Boolean, sleep: Boolean, hydration: Boolean, writeHealthData: Boolean) -> Unit = { _, _, _, _, _ -> },
    onBackClick: () -> Unit = {}
) {
    var vitalSignsPermission by remember { mutableStateOf(vitalSigns) }
    var activityPermission by remember { mutableStateOf(activity) }
    var sleepPermission by remember { mutableStateOf(sleep) }
    var hydrationPermission by remember { mutableStateOf(hydration) }
    var writeHealthDataPermission by remember { mutableStateOf(writeHealthData) }

    LaunchedEffect(vitalSigns, activity, sleep, hydration, writeHealthData) {
        vitalSignsPermission = vitalSigns
        activityPermission = activity
        sleepPermission = sleep
        hydrationPermission = hydration
        writeHealthDataPermission = writeHealthData
    }

    val isDark = MaterialTheme.colorScheme.background == CarbonDarkBackground || MaterialTheme.colorScheme.surface == CarbonDarkSurface

    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val lastSyncText = remember(lastSyncTimestamp) {
        if (lastSyncTimestamp != null && lastSyncTimestamp > 0) {
            "Last synced at ${timeFormat.format(Date(lastSyncTimestamp))}"
        } else {
            "Cloud synchronization active"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                    text = "Health Permissions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Choose the health data VitalSync accesses to personalize your daily insights.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Health Connect Status Hero Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF142E1F) else Color(0xFFE8F6F0)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Health Connect", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF86EFAC) else Color(0xFF111827))
                            Text(text = lastSyncText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDark) Color(0xFF1F4D33) else Color(0xFFDCFCE7))
                            .clickable { onRequestHealthPermissions() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "Manage", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF86EFAC) else Color(0xFF15803D))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // READ HEALTH DATA Section
            SectionHeader("READ HEALTH DATA")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PermissionToggleRow(
                        title = "Vital Signs",
                        desc = "Heart rate, blood pressure, and SpO2",
                        icon = Icons.Rounded.Favorite,
                        checked = vitalSignsPermission
                    ) { vitalSignsPermission = it }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 12.dp))

                    PermissionToggleRow(
                        title = "Activity",
                        desc = "Steps, distance, and active calories",
                        icon = Icons.Rounded.DirectionsRun,
                        checked = activityPermission
                    ) { activityPermission = it }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 12.dp))

                    PermissionToggleRow(
                        title = "Sleep",
                        desc = "Sleep duration and sleep stages",
                        icon = Icons.Rounded.Bedtime,
                        checked = sleepPermission
                    ) { sleepPermission = it }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 12.dp))

                    PermissionToggleRow(
                        title = "Hydration",
                        desc = "Daily water intake",
                        icon = Icons.Rounded.WaterDrop,
                        checked = hydrationPermission
                    ) { hydrationPermission = it }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // WRITE TO HEALTH CONNECT Section
            SectionHeader("WRITE TO HEALTH CONNECT")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PermissionToggleRow(
                        title = "Write Health Data",
                        desc = "Allow VitalSync to write workouts and nutrition entries to Health Connect",
                        icon = Icons.Rounded.EditNote,
                        checked = writeHealthDataPermission
                    ) {
                        writeHealthDataPermission = it
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom Action Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Button(
                onClick = {
                    onSavePermissions(
                        vitalSignsPermission,
                        activityPermission,
                        sleepPermission,
                        hydrationPermission,
                        writeHealthDataPermission
                    )
                    onBackClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VitalTealDark)
            ) {
                Text(text = "Save Permissions", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onRequestHealthPermissions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(text = "Open Health Connect Settings", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
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
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp, start = 4.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Start
    )
}

@Composable
private fun PermissionToggleRow(
    title: String,
    desc: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0F9F7A)
            )
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun HealthPermissionsScreenPreview() {
    VitalsyncTheme {
        HealthPermissionsScreen()
    }
}
