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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.simats.vitalsync.data.model.profile.ReminderItem
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalsyncTheme

@Composable
fun RemindersScreen(
    modifier: Modifier = Modifier,
    reminders: List<ReminderItem> = emptyList(),
    isLoading: Boolean = false,
    onToggleReminder: (ReminderItem, Boolean) -> Unit = { _, _ -> },
    onDeleteReminder: (Int) -> Unit = {},
    onBackClick: () -> Unit = {},
    onAddReminderClick: () -> Unit = {}
) {
    val activeCount = reminders.count { it.isEnabled }

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
                    text = "Reminders",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Make healthy habits part of your day.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.secondary)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$activeCount scheduled",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (activeCount > 0) "Reminders synced with cloud" else "No active reminders",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (reminders.isEmpty() && !isLoading) {
                // Fallback default visual reminders
                ReminderCard(
                    icon = "💧",
                    title = "Drink Water",
                    schedule = "Every day",
                    time = "8:00 AM",
                    checked = true,
                    onCheckedChange = { }
                )
                Spacer(modifier = Modifier.height(14.dp))
                ReminderCard(
                    icon = "🏃‍♂️",
                    title = "Exercise",
                    schedule = "Mon–Fri",
                    time = "6:00 PM",
                    checked = true,
                    onCheckedChange = { }
                )
                Spacer(modifier = Modifier.height(14.dp))
                ReminderCard(
                    icon = "🌙",
                    title = "Sleep",
                    schedule = "Every day",
                    time = "10:30 PM",
                    checked = true,
                    onCheckedChange = { }
                )
                Spacer(modifier = Modifier.height(14.dp))
                ReminderCard(
                    icon = "💊",
                    title = "Medication",
                    schedule = "Every day",
                    time = "9:00 AM",
                    checked = true,
                    onCheckedChange = { }
                )
            } else {
                reminders.forEachIndexed { index, item ->
                    val icon = getReminderIcon(item.title, item.reminderType)
                    ReminderCard(
                        icon = icon,
                        title = item.title,
                        schedule = item.frequency.ifBlank { "Every day" },
                        time = item.time.ifBlank { "08:00 AM" },
                        checked = item.isEnabled,
                        onCheckedChange = { isChecked ->
                            onToggleReminder(item, isChecked)
                        }
                    )
                    if (index < reminders.size - 1) {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Add Reminder Button
        Button(
            onClick = onAddReminderClick,
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
                text = "+ Add Reminder",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun getReminderIcon(title: String, type: String?): String {
    val lower = (title + " " + (type ?: "")).lowercase()
    return when {
        lower.contains("water") || lower.contains("hydrate") -> "💧"
        lower.contains("exercise") || lower.contains("workout") || lower.contains("gym") || lower.contains("run") -> "🏃‍♂️"
        lower.contains("sleep") || lower.contains("bed") -> "🌙"
        lower.contains("med") || lower.contains("pill") -> "💊"
        lower.contains("heart") || lower.contains("vital") || lower.contains("bp") -> "❤️"
        else -> "⏰"
    }
}

@Composable
private fun ReminderCard(
    icon: String,
    title: String,
    schedule: String,
    time: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = icon, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = schedule,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { }
            ) {
                Text(
                    text = time,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(8.dp))
                PencilEditIcon(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PencilEditIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF6B7280)
) {
    Canvas(modifier = modifier) {
        val w = size.width

        val path = Path().apply {
            moveTo(w * 0.7f, w * 0.1f)
            lineTo(w * 0.9f, w * 0.3f)
            lineTo(w * 0.3f, w * 0.9f)
            lineTo(w * 0.1f, w * 0.9f)
            lineTo(w * 0.1f, w * 0.7f)
            close()
        }

        drawPath(path = path, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
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
fun RemindersScreenPreview() {
    VitalsyncTheme {
        RemindersScreen()
    }
}
