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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalTealPrimary
import com.simats.vitalsync.ui.theme.VitalsyncTheme

@Composable
fun AddReminderScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSaveReminderSuccess: () -> Unit = {}
) {
    var reminderName by remember { mutableStateOf("Drink Water") }
    var selectedTime by remember { mutableStateOf("08:00 AM") }
    var selectedFrequency by remember { mutableStateOf("Every day") }
    var soundVibrationEnabled by remember { mutableStateOf(true) }
    var selectedDays by remember { mutableStateOf(setOf(0, 1, 2, 3, 4, 5, 6)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFCFB))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
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
                        color = Color(0xFF111827)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Add Custom Reminder",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Reminder Label
            Text(text = "Reminder Label", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = reminderName,
                onValueChange = { reminderName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color(0xFF111827),
                    unfocusedTextColor = Color(0xFF111827),
                    focusedBorderColor = VitalTealPrimary,
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Target Time Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Trigger Time", fontSize = 12.sp, color = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = selectedTime, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE8F6F0))
                            .clickable { }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "Change Time", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A))
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Repeat Day Selector
            Text(text = "Repeat Days", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEachIndexed { index, dayLabel ->
                    val isSelected = selectedDays.contains(index)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) VitalTealDark else Color(0xFFF1F5F9))
                            .clickable {
                                selectedDays = if (isSelected) {
                                    selectedDays - index
                                } else {
                                    selectedDays + index
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFF4B5563)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Alert Sound & Vibration Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Sound & Vibration Alert", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Text(text = "Notify with system alarm tone", fontSize = 11.sp, color = Color(0xFF6B7280))
                    }

                    Switch(
                        checked = soundVibrationEnabled,
                        onCheckedChange = { soundVibrationEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = VitalTealPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Save Reminder Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Button(
                onClick = onSaveReminderSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VitalTealDark,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Save Reminder",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
fun AddReminderScreenPreview() {
    VitalsyncTheme {
        AddReminderScreen()
    }
}
