package com.simats.vitalsync.ui.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

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

    // Smooth Animated Blur Dp
    val chatBlurDp by animateDpAsState(
        targetValue = if (isListeningForVoice) 18.dp else 0.dp,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "ChatBlurAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFCFB))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Main Chat Conversation Content (Blurred when AI Voice Animation is Active)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .blur(chatBlurDp)
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
                            text = "VitalSync AI Assistant",
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

                // Suggested Prompts
                SuggestedPromptChip("How was my activity this week?") { sendMessageText("How was my activity this week?") }
                Spacer(modifier = Modifier.height(8.dp))
                SuggestedPromptChip("How can I improve my sleep?") { sendMessageText("How can I improve my sleep?") }
                Spacer(modifier = Modifier.height(8.dp))
                SuggestedPromptChip("Give me my wellness summary.") { sendMessageText("Give me my wellness summary.") }

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

            // Bottom Message Input Bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
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
                            // Interactive Animated AI Voice Orb Icon (Replaces plain mic emoji)
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isListeningForVoice) Color(0xFFF3E8FF) else Color.Transparent)
                                    .clickable {
                                        isListeningForVoice = !isListeningForVoice
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                AiVoiceSphereAnimation(
                                    modifier = Modifier.size(28.dp),
                                    isListening = isListeningForVoice
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Send Message Button
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
                                SendArrowIcon(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White
                                )
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

        // Frosted Glass Soft Dark Scrim Overlay when AI Voice is Active
        AnimatedVisibility(
            visible = isListeningForVoice,
            enter = fadeIn(animationSpec = tween(350)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x600F0B1E))
                    .clickable { isListeningForVoice = false }
            )
        }

        // Seamless Floating Transparent AI Voice Orb Overlay + "Speak now..." Text
        AnimatedVisibility(
            visible = isListeningForVoice,
            enter = fadeIn(animationSpec = tween(350)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    sendMessageText("How was my activity and sleep quality this week?")
                }
            ) {
                // 🔮 Pure Ultra-Vivid Glowing AI Particle Orb
                Box(
                    modifier = Modifier.size(210.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AiVoiceSphereAnimation(
                        modifier = Modifier.fillMaxSize(),
                        isListening = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // "Speak now..." Glowing Label
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF281C3D))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Speak now...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE9D5FF)
                        )
                    }
                }
            }
        }
    }
}

// 🔮 Ultra-Vivid Futuristic Glowing Violet/Magenta/Cyan AI Particle Orb Animation
@Composable
fun AiVoiceSphereAnimation(
    modifier: Modifier = Modifier,
    isListening: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AiVoiceSphereTransition")

    // Continuous Primary Rotation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SphereRotation"
    )

    // Counter Rotation for Gyroscope Effect
    val counterRotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CounterRotation"
    )

    // Pulsating Aura Scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SphereAuraPulse"
    )

    // Fluid Wave Oscillations
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f
        val centerY = h / 2f
        val baseRadius = (minOf(w, h) / 2.2f) * (if (isListening) pulseScale else 1f)

        // 1. Hyper-Glow Outer Background Radial Gradient (Vivid Neon Violet + Electric Cyan + Core Glow)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE086D3).copy(alpha = 0.75f), // Glowing Pink-Violet Nucleus
                    Color(0xFFC084FC).copy(alpha = 0.50f), // Neon Purple
                    Color(0xFF8B5CF6).copy(alpha = 0.30f), // Deep Violet
                    Color(0xFF38BDF8).copy(alpha = 0.15f), // Cyan Edge Accent
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = baseRadius * 1.65f
            ),
            radius = baseRadius * 1.65f,
            center = Offset(centerX, centerY)
        )

        // 2. Inner Glowing Core Nucleus Sphere
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White,
                    Color(0xFFF472B6),
                    Color(0xFFA855F7),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = baseRadius * 0.45f
            ),
            radius = baseRadius * 0.45f,
            center = Offset(centerX, centerY)
        )

        // 3. Multi-Axis 3D Gyroscope Energy Swirl Rings (45°, -45°, 90°)
        val ribbonColorsList = listOf(
            listOf(Color(0xFFFFFFFF), Color(0xFFF472B6), Color(0xFFA855F7), Color(0xFF38BDF8)),
            listOf(Color(0xFF38BDF8), Color(0xFFC084FC), Color(0xFFEC4899), Color(0xFFFFFFFF)),
            listOf(Color(0xFFE9D5FF), Color(0xFF8B5CF6), Color(0xFF0284C7), Color(0xFFF472B6))
        )

        repeat(3) { ribbonIndex ->
            val angleOffset = ribbonIndex * (Math.PI.toFloat() * 2f / 3f) + (if (ribbonIndex % 2 == 0) rotationAngle else counterRotationAngle) * (Math.PI.toFloat() / 180f)
            val ribbonPath = Path().apply {
                val steps = 40
                for (step in 0..steps) {
                    val t = step.toFloat() / steps
                    val angle = t * 2f * Math.PI.toFloat()
                    val waveDistort = sin(angle * 3f + wavePhase + ribbonIndex) * 0.18f
                    val r = baseRadius * (1f + waveDistort)

                    // 3D Rotated Ellipse coordinates
                    val tiltScale = if (ribbonIndex == 0) 0.65f else if (ribbonIndex == 1) 0.45f else 0.85f
                    val x = centerX + r * cos(angle + angleOffset)
                    val y = centerY + r * sin(angle + angleOffset) * tiltScale

                    if (step == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }

            drawPath(
                path = ribbonPath,
                brush = Brush.sweepGradient(
                    colors = ribbonColorsList[ribbonIndex],
                    center = Offset(centerX, centerY)
                ),
                style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // 4. Spherical Particle Lattice Mesh Nodes (Latitude Rings)
        val latitudeRings = 13
        for (i in 0 until latitudeRings) {
            val normI = (i.toFloat() / (latitudeRings - 1)) * 2f - 1f // -1 to 1
            val ringRadiusY = sqrt((1f - normI * normI).coerceAtLeast(0f)) * baseRadius
            val ringCenterY = centerY + normI * baseRadius * 0.85f

            val dotsCount = 30
            for (j in 0 until dotsCount) {
                val dotAngle = (j.toFloat() / dotsCount) * 2f * Math.PI.toFloat() + (rotationAngle * Math.PI.toFloat() / 180f) * (if (i % 2 == 0) 1.2f else -1.2f)
                val dotX = centerX + ringRadiusY * cos(dotAngle)
                val dotY = ringCenterY + ringRadiusY * sin(dotAngle) * 0.35f // 3D Perspective Tilt

                val waveDistort = sin(wavePhase * 2.2f + dotAngle * 2.5f + i) * 4.0.dp.toPx()
                val dotAlpha = ((sin(dotAngle + wavePhase) + 1f) / 2f * 0.75f + 0.30f).coerceIn(0.15f, 1f)

                val dotColor = if (j % 3 == 0) Color(0xFF38BDF8) else if (j % 2 == 0) Color(0xFFF472B6) else Color(0xFFFFFFFF)

                drawCircle(
                    color = dotColor.copy(alpha = dotAlpha),
                    radius = (2.2.dp.toPx() + waveDistort * 0.12f).coerceAtLeast(1.dp.toPx()),
                    center = Offset(dotX, dotY + waveDistort)
                )
            }
        }

        // 5. Outer Bright Glowing Glass Rim
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(Color(0xFFFFFFFF), Color(0xFFF472B6), Color(0xFF38BDF8), Color(0xFFA855F7), Color(0xFFFFFFFF)),
                center = Offset(centerX, centerY)
            ),
            radius = baseRadius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 3.2.dp.toPx())
        )

        // 6. Floating Orbiting Sparkle Star Particles around Sphere Edge
        val sparkleCount = 12
        for (s in 0 until sparkleCount) {
            val sparkleAngle = (s.toFloat() / sparkleCount) * 2f * Math.PI.toFloat() + (counterRotationAngle * Math.PI.toFloat() / 180f) * 1.5f
            val orbitR = baseRadius * (1.15f + 0.1f * sin(wavePhase + s))
            val sx = centerX + orbitR * cos(sparkleAngle)
            val sy = centerY + orbitR * sin(sparkleAngle) * 0.6f

            val sparkleAlpha = ((sin(wavePhase * 3f + s) + 1f) / 2f).coerceIn(0.2f, 1f)
            drawCircle(
                color = Color.White.copy(alpha = sparkleAlpha),
                radius = 2.4.dp.toPx(),
                center = Offset(sx, sy)
            )
        }
    }
}

@Composable
private fun SendArrowIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.15f, h * 0.15f)
            lineTo(w * 0.88f, h * 0.50f)
            lineTo(w * 0.15f, h * 0.85f)
            lineTo(w * 0.35f, h * 0.50f)
            close()
        }

        drawPath(path = path, color = color)
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
