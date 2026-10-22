package com.simats.vitalsync.ui.ai

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalTealPrimary
import com.simats.vitalsync.ui.theme.VitalsyncTheme

data class ChatMessage(
    val id: String,
    val sender: String, // "USER" or "AI"
    val text: String,
    val timeText: String
)

private val initialChatMessages = listOf(
    ChatMessage("1", "USER", "How was my activity this week?", "9:00 AM"),
    ChatMessage("2", "AI", "You're making great progress! Your activity is up 12% from your weekly average. A 15-minute walk today can help you stay on track.", "9:01 AM")
)

private fun generateAiResponse(query: String): String {
    val q = query.lowercase()
    return when {
        q.contains("activity") || q.contains("step") || q.contains("walk") ->
            "Your step count today is 6,245 steps (78% of target). You've covered 4.2 km and burned 320 kcal. Keep moving to hit your 8,000 steps goal!"
        q.contains("sleep") || q.contains("rest") || q.contains("bed") ->
            "Your sleep last night was 7h 32m with an optimal Sleep Score of 88. Deep and REM sleep cycles were well balanced for recovery."
        q.contains("water") || q.contains("drink") || q.contains("hydrat") ->
            "You have logged 5 / 8 glasses of water today. Drink 3 more glasses before 8:00 PM to complete your daily hydration goal!"
        q.contains("protein") || q.contains("food") || q.contains("diet") || q.contains("calori") ->
            "Today you've logged 1,820 kcal and 115g protein. For your muscle gain target, aim for another 15g protein in your evening meal."
        q.contains("workout") || q.contains("gym") || q.contains("lift") || q.contains("volume") ->
            "Your workout volume reached 18,500 kg across 4 sessions this week! Excellent training consistency. Make sure to recover well."
        q.contains("heart") || q.contains("bpm") || q.contains("pulse") ->
            "Your resting heart rate is 76 BPM (Normal range: 60-90 BPM). Your cardiovascular trends are healthy and stable."
        else ->
            "I've analyzed your health profile! Your overall Wellness Score is 88/100. Ask me anything about workouts, diet, sleep, or hydration!"
    }
}

@Composable
fun AiChatScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onChatHistoryClick: () -> Unit = {}
) {
    var messageInput by remember { mutableStateOf("") }
    var isListeningForVoice by remember { mutableStateOf(false) }

    val messages = remember { mutableStateListOf<ChatMessage>().apply { addAll(initialChatMessages) } }
    val scrollState = rememberScrollState()

    LaunchedEffect(messages.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    fun sendMessageText(textToSend: String) {
        val userText = textToSend.trim()
        if (userText.isEmpty()) return

        messages.add(ChatMessage(System.currentTimeMillis().toString(), "USER", userText, "Just now"))
        messageInput = ""
        isListeningForVoice = false

        val aiText = generateAiResponse(userText)
        messages.add(ChatMessage((System.currentTimeMillis() + 1).toString(), "AI", aiText, "Just now"))
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
                .verticalScroll(scrollState)
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
                        text = "VitalSync AI",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                IconButton(
                    onClick = onChatHistoryClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Text(
                        text = "•••",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Top Greeting Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✨ Your personal wellness companion",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED)
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Online",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "💬 Hi Alex 👋 How can I help with your wellness today?",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Suggested Prompts (Clicking directly sends query)
            SuggestedPromptChip("How was my activity this week?") { sendMessageText("How was my activity this week?") }
            Spacer(modifier = Modifier.height(8.dp))
            SuggestedPromptChip("How can I improve my sleep?") { sendMessageText("How can I improve my sleep?") }
            Spacer(modifier = Modifier.height(8.dp))
            SuggestedPromptChip("Give me my wellness summary.") { sendMessageText("Give me my wellness summary.") }
            Spacer(modifier = Modifier.height(8.dp))
            SuggestedPromptChip("How can I stay hydrated?") { sendMessageText("How can I stay hydrated?") }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Chat History",
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Chat Messages List
            messages.forEach { msg ->
                if (msg.sender == "USER") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 40.dp, bottom = 12.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(VitalTealDark)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = msg.text,
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 20.dp, bottom = 12.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "✨ VitalSync AI",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C3AED)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = msg.text,
                                fontSize = 14.sp,
                                color = Color(0xFF111827),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom Message Bar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            // Live Voice Speech Banner when Active
            AnimatedVisibility(
                visible = isListeningForVoice,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE8F6F0))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🎙️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Listening... Speak now",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F9F7A)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFDCFCE7))
                                .clickable { isListeningForVoice = false }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "Stop", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                        }
                    }
                }
            }

            OutlinedTextField(
                value = messageInput,
                onValueChange = { messageInput = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = if (isListeningForVoice) "Listening to your voice..." else "Message VitalSync AI...",
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { sendMessageText(messageInput) }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color(0xFF111827),
                    unfocusedTextColor = Color(0xFF111827),
                    focusedBorderColor = VitalTealPrimary,
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                trailingIcon = {
                    Row(
                        modifier = Modifier.padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Interactive Microphone Voice Speech Button
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isListeningForVoice) Color(0xFFDCFCE7) else Color.Transparent)
                                .clickable {
                                    isListeningForVoice = !isListeningForVoice
                                    if (isListeningForVoice) {
                                        messageInput = "How was my activity and sleep quality this week?"
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎙️",
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Send Message Button (Triggers Dynamic Chat Send)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(VitalTealPrimary)
                                .clickable {
                                    sendMessageText(messageInput)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✈️", fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "General wellness information only. Not a medical diagnosis.",
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SuggestedPromptChip(
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
            Text(text = "↗", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF))
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
fun AiChatScreenPreview() {
    VitalsyncTheme {
        AiChatScreen()
    }
}
