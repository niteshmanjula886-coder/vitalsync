package com.simats.vitalsync.ui.vitals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.data.model.vitals.VitalRecentLogDto
import com.simats.vitalsync.ui.theme.VitalTealDark

/**
 * Focused single-vital bottom sheet for logging Heart Rate (30–220 BPM).
 */
@Composable
fun LogHeartRateSheet(
    initialBpm: Int? = null,
    isSaving: Boolean = false,
    serverError: String? = null,
    onDismiss: () -> Unit,
    onSave: (heartRate: Int, notes: String?) -> Unit
) {
    var bpmText by remember(initialBpm) {
        mutableStateOf(initialBpm?.takeIf { it > 0 }?.toString() ?: "")
    }
    var notesText by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    SingleVitalModalSheetFrame(
        title = "Log Heart Rate",
        subtitle = "Enter your pulse reading in Beats Per Minute (30–220 BPM)",
        buttonLabel = "Save Heart Rate",
        notesText = notesText,
        onNotesChange = { notesText = it },
        errorMessage = localError ?: serverError,
        isSaving = isSaving,
        onDismiss = onDismiss,
        onSaveClick = {
            val bpm = bpmText.trim().toIntOrNull()
            when {
                bpm == null -> localError = "Please enter your Heart Rate in BPM."
                bpm !in 30..220 -> localError = "Heart Rate must be between 30 and 220 BPM."
                else -> onSave(bpm, notesText.trim().ifBlank { null })
            }
        }
    ) {
        OutlinedTextField(
            value = bpmText,
            onValueChange = {
                bpmText = it.filter { ch -> ch.isDigit() }
                localError = null
            },
            label = { Text("Heart Rate (BPM)") },
            placeholder = { Text("e.g. 74") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Focused single-vital bottom sheet for logging Blood Oxygen / SpO2 (70–100%).
 */
@Composable
fun LogSpo2Sheet(
    initialSpo2: Int? = null,
    isSaving: Boolean = false,
    serverError: String? = null,
    onDismiss: () -> Unit,
    onSave: (spo2: Int, notes: String?) -> Unit
) {
    var spo2Text by remember(initialSpo2) {
        mutableStateOf(initialSpo2?.takeIf { it > 0 }?.toString() ?: "")
    }
    var notesText by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    SingleVitalModalSheetFrame(
        title = "Log Blood Oxygen (SpO₂)",
        subtitle = "Enter oxygen saturation percentage measured at rest (70–100%)",
        buttonLabel = "Save Blood Oxygen",
        notesText = notesText,
        onNotesChange = { notesText = it },
        errorMessage = localError ?: serverError,
        isSaving = isSaving,
        onDismiss = onDismiss,
        onSaveClick = {
            val spo2 = spo2Text.trim().toIntOrNull()
            when {
                spo2 == null -> localError = "Please enter your SpO₂ percentage."
                spo2 !in 70..100 -> localError = "SpO₂ must be between 70% and 100%."
                else -> onSave(spo2, notesText.trim().ifBlank { null })
            }
        }
    ) {
        OutlinedTextField(
            value = spo2Text,
            onValueChange = {
                spo2Text = it.filter { ch -> ch.isDigit() }
                localError = null
            },
            label = { Text("SpO₂ (%)") },
            placeholder = { Text("e.g. 98") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Focused dual-field bottom sheet for logging Blood Pressure (Systolic 70–220 & Diastolic 40–140 mmHg).
 */
@Composable
fun LogBloodPressureSheet(
    initialSystolic: Int? = null,
    initialDiastolic: Int? = null,
    isSaving: Boolean = false,
    serverError: String? = null,
    onDismiss: () -> Unit,
    onSave: (systolic: Int, diastolic: Int, notes: String?) -> Unit
) {
    var sysText by remember(initialSystolic) {
        mutableStateOf(initialSystolic?.takeIf { it > 0 }?.toString() ?: "")
    }
    var diaText by remember(initialDiastolic) {
        mutableStateOf(initialDiastolic?.takeIf { it > 0 }?.toString() ?: "")
    }
    var notesText by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    SingleVitalModalSheetFrame(
        title = "Log Blood Pressure",
        subtitle = "Enter Systolic (70–220) and Diastolic (40–140) cuff pressure in mmHg",
        buttonLabel = "Save Blood Pressure",
        notesText = notesText,
        onNotesChange = { notesText = it },
        errorMessage = localError ?: serverError,
        isSaving = isSaving,
        onDismiss = onDismiss,
        onSaveClick = {
            val sys = sysText.trim().toIntOrNull()
            val dia = diaText.trim().toIntOrNull()
            when {
                sys == null || dia == null ->
                    localError = "Enter both Systolic and Diastolic pressures."
                sys !in 70..220 ->
                    localError = "Systolic pressure must be between 70 and 220 mmHg."
                dia !in 40..140 ->
                    localError = "Diastolic pressure must be between 40 and 140 mmHg."
                sys <= dia ->
                    localError = "Systolic pressure must be higher than Diastolic pressure."
                else -> onSave(sys, dia, notesText.trim().ifBlank { null })
            }
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = sysText,
                onValueChange = {
                    sysText = it.filter { ch -> ch.isDigit() }
                    localError = null
                },
                label = { Text("Systolic (mmHg)") },
                placeholder = { Text("120") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = diaText,
                onValueChange = {
                    diaText = it.filter { ch -> ch.isDigit() }
                    localError = null
                },
                label = { Text("Diastolic (mmHg)") },
                placeholder = { Text("80") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleVitalModalSheetFrame(
    title: String,
    subtitle: String,
    buttonLabel: String,
    notesText: String,
    onNotesChange: (String) -> Unit,
    errorMessage: String?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSaveClick: () -> Unit,
    metricInputContent: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
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
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            metricInputContent()

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = notesText,
                onValueChange = onNotesChange,
                label = { Text("Notes (Optional)") },
                placeholder = { Text("e.g. Resting before breakfast") },
                maxLines = 2,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFDC2626)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onSaveClick,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VitalTealDark),
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(buttonLabel, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Shared top navigation bar for dedicated Vital Detail Screens (Back arrow, title, primary log pill).
 */
@Composable
internal fun VitalDetailTopBar(
    title: String,
    actionLabel: String,
    onBackClick: () -> Unit,
    onActionClick: () -> Unit
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
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(36.dp)
            ) {
                Canvas(modifier = Modifier.size(20.dp)) {
                    val w = size.width
                    val h = size.height
                    val path = Path().apply {
                        moveTo(w * 0.75f, h * 0.5f)
                        lineTo(w * 0.25f, h * 0.5f)
                        moveTo(w * 0.45f, h * 0.25f)
                        lineTo(w * 0.25f, h * 0.5f)
                        lineTo(w * 0.45f, h * 0.75f)
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFF111827),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(VitalTealDark)
                .clickable { onActionClick() }
                .padding(horizontal = 13.dp, vertical = 8.dp)
        ) {
            Text(
                text = actionLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

/**
 * Shared conditional Urgent Medical Advisory Banner rendered when a reading hits critical thresholds.
 */
@Composable
internal fun VitalMedicalAlertBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        border = BorderStroke(1.5.dp, Color(0xFFFCA5A5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = "⚠️", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF991B1B),
                lineHeight = 19.sp
            )
        }
    }
}

/**
 * Shared Ollama AI Evaluation & Activity Guidance card rendered on Vital Detail Screens.
 */
@Composable
internal fun VitalOllamaGuidanceCard(
    isLoading: Boolean,
    modelUsed: String?,
    explanation: String,
    recommendedActivity: String,
    lifestyleTip: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE9D5FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✦ VitalSync AI Analysis",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6D28D9)
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3E8FF))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Analyzing...",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED)
                        )
                    }
                } else {
                    Text(
                        text = modelUsed ?: "qwen2.5:1.5b",
                        fontSize = 10.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = explanation,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = Color(0xFF374151)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFECFDF5))
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Column {
                    Text(
                        text = "🏃 Recommended Activity",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recommendedActivity,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = Color(0xFF064E3B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFFFBEB))
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Column {
                    Text(
                        text = "💡 Recovery Tip",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lifestyleTip,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = Color(0xFF78350F)
                    )
                }
            }
        }
    }
}

/**
 * Shared chronological history card listing recent single-vital logs with status badges.
 */
@Composable
internal fun VitalRecentLogsCard(
    title: String,
    logs: List<VitalRecentLogDto>,
    emptyMessage: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (logs.isEmpty()) {
                Text(
                    text = emptyMessage,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                logs.forEachIndexed { index, entry ->
                    val (badgeTextColor, badgeBg) = when {
                        entry.urgency.equals("MEDICAL_ATTENTION", ignoreCase = true) ||
                            entry.status.contains("Hypotension", ignoreCase = true) ->
                            Pair(Color(0xFFE53935), Color(0xFFFEE2E2))
                        entry.urgency.equals("NEEDS_IMPROVEMENT", ignoreCase = true) ||
                            entry.status.contains("Elevated", ignoreCase = true) ->
                            Pair(Color(0xFFD97706), Color(0xFFFEF3C7))
                        else -> Pair(Color(0xFF43A047), Color(0xFFECFDF5))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.value,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (entry.date.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = entry.date,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeBg)
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = entry.status,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeTextColor
                            )
                        }
                    }

                    if (index < logs.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

