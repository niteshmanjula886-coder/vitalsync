package com.simats.vitalsync.ui.ai

import android.app.Application
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.vitalsync.ui.ai.viewmodel.AiViewModel
import com.simats.vitalsync.ui.ai.viewmodel.AiViewModelFactory
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

@Composable
fun AiChatScreen(
    modifier: Modifier = Modifier,
    viewModel: AiViewModel = viewModel(
        factory = AiViewModelFactory(LocalContext.current.applicationContext as Application)
    ),
    onBackClick: () -> Unit = {},
    onChatHistoryClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var messageInput by remember { mutableStateOf("") }
    var isListeningForVoice by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Smooth auto-scroll to bottom whenever message count changes or AI begins typing
    LaunchedEffect(uiState.messages.size, uiState.isSending) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    fun submitMessage(textToSend: String) {
        val trimmed = textToSend.trim()
        if (trimmed.isEmpty()) return
        messageInput = ""
        isListeningForVoice = false
        viewModel.sendMessage(trimmed)
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
            .imePadding()
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

                // Header Bar with Jr. Doctor Branding & Live Engine Status
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

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEDE9FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🩺", fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "VitalSync AI ✨",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = uiState.activeModelLabel,
                                    fontSize = 11.sp,
                                    color = Color(0xFF059669),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text(
                                text = "•••",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Clear Conversation") },
                                onClick = {
                                    showMenu = false
                                    viewModel.clearChatHistory()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("View History") },
                                onClick = {
                                    showMenu = false
                                    onChatHistoryClick()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Jr. Doctor Introduction & Telemetry Sync Banner
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
                                text = "✨ General & RAG-Enhanced AI",
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
                                    text = "Ollama Active",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Hello! I am your versatile AI assistant powered by Qwen3 (4B) and verified RAG medical knowledge. Ask me anything: from science, movies, and code to personalized vitals, workout recovery, and nutrition.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E1B4B),
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Clinical Suggested Prompts
                Text(
                    text = "Quick Inquiries",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6B7280)
                )

                Spacer(modifier = Modifier.height(8.dp))

                SuggestedPromptChip("🩺 Review my resting heart rate today") {
                    submitMessage("Can you review my resting heart rate and cardiovascular trends today?")
                }
                Spacer(modifier = Modifier.height(6.dp))
                SuggestedPromptChip("💧 Check today's hydration target") {
                    submitMessage("How much water have I consumed today, and what is my remaining hydration goal?")
                }
                Spacer(modifier = Modifier.height(6.dp))
                SuggestedPromptChip("🌙 How was my sleep recovery?") {
                    submitMessage("Analyze my sleep score and duration from last night.")
                }
                Spacer(modifier = Modifier.height(6.dp))
                SuggestedPromptChip("🥗 Best high protein sources for muscle gain") {
                    submitMessage("What are the best high protein sources for building muscle?")
                }
                Spacer(modifier = Modifier.height(6.dp))
                SuggestedPromptChip("🎬 Who directed Inception and what is it about?") {
                    submitMessage("Who directed Inception and what is it about?")
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Conversational Memory",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Dynamic Chat Messages List
                uiState.messages.forEach { msg ->
                    if (msg.sender == "USER") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 48.dp, bottom = 12.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(VitalTealDark)
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 20.sp
                                )
                            }
                            if (msg.timeText.isNotBlank()) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = msg.timeText,
                                    fontSize = 10.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 28.dp, bottom = 12.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "✨ Jr. Doctor",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF7C3AED)
                                        )

                                        if (msg.timeText.isNotBlank()) {
                                            Text(
                                                text = msg.timeText,
                                                fontSize = 10.sp,
                                                color = Color(0xFF9CA3AF)
                                            )
                                        }
                                    }

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
                }

                // Pulsating "Jr. Doctor is thinking..." Bubble
                if (uiState.isSending) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 40.dp, bottom = 12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PulsingThinkingDots()
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Jr. Doctor is analyzing your vitals...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF7C3AED)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Input Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = if (isListeningForVoice) "Listening to your voice..." else "Ask VitalSync AI anything...",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submitMessage(messageInput) }),
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
                            // Interactive Animated AI Voice Orb Icon
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
                                    .background(if (messageInput.isNotBlank() && !uiState.isSending) VitalTealPrimary else Color(0xFFD1D5DB))
                                    .clickable(enabled = messageInput.isNotBlank() && !uiState.isSending) {
                                        submitMessage(messageInput)
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
                    text = "VitalSync AI provides general answers and wellness insights. Not a substitute for clinical emergency diagnosis.",
                    fontSize = 10.sp,
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
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
                    submitMessage("How was my activity and sleep quality this week?")
                }
            ) {
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

@Composable
private fun PulsingThinkingDots() {
    val transition = rememberInfiniteTransition(label = "ThinkingDots")
    val alpha1 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot1"
    )
    val alpha2 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot2"
    )
    val alpha3 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF7C3AED).copy(alpha = alpha1))
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF7C3AED).copy(alpha = alpha2))
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF7C3AED).copy(alpha = alpha3))
        )
    }
}

// 🔮 Ultra-Vivid Futuristic Glowing Violet/Magenta/Cyan AI Particle Orb Animation
@Composable
fun AiVoiceSphereAnimation(
    modifier: Modifier = Modifier,
    isListening: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AiVoiceSphereTransition")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SphereRotation"
    )

    val counterRotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CounterRotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SphereAuraPulse"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SphereWavePhase"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val baseRadius = (size.minDimension / 2f) * 0.72f * pulseScale

        // Ambient Soft Core Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x99A855F7),
                    Color(0x55EC4899),
                    Color(0x2238BDF8),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = baseRadius * 1.35f
            ),
            radius = baseRadius * 1.35f,
            center = Offset(centerX, centerY)
        )

        // Fluid Outer Shell
        val numPoints = 28
        val path1 = Path()
        val path2 = Path()

        for (i in 0 until numPoints) {
            val angle = (i.toFloat() / numPoints) * 2f * Math.PI.toFloat()
            val totalAngle1 = angle + Math.toRadians(rotationAngle.toDouble()).toFloat()
            val totalAngle2 = angle - Math.toRadians(counterRotationAngle.toDouble()).toFloat()

            val r1 = baseRadius + (sin((angle * 3f + wavePhase).toDouble()).toFloat() * 10f)
            val r2 = baseRadius * 0.85f + (cos((angle * 4f - wavePhase).toDouble()).toFloat() * 8f)

            val x1 = centerX + r1 * cos(totalAngle1.toDouble()).toFloat()
            val y1 = centerY + r1 * sin(totalAngle1.toDouble()).toFloat()

            val x2 = centerX + r2 * cos(totalAngle2.toDouble()).toFloat()
            val y2 = centerY + r2 * sin(totalAngle2.toDouble()).toFloat()

            if (i == 0) {
                path1.moveTo(x1, y1)
                path2.moveTo(x2, y2)
            } else {
                path1.lineTo(x1, y1)
                path2.lineTo(x2, y2)
            }
        }
        path1.close()
        path2.close()

        drawPath(
            path = path1,
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFF8B5CF6),
                    Color(0xFFEC4899),
                    Color(0xFF06B6D4),
                    Color(0xFF8B5CF6)
                ),
                center = Offset(centerX, centerY)
            ),
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
        )

        drawPath(
            path = path2,
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFF38BDF8),
                    Color(0xFFC084FC),
                    Color(0xFFF472B6),
                    Color(0xFF38BDF8)
                ),
                center = Offset(centerX, centerY)
            ),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Floating Micro Particles
        val particleCount = 14
        for (j in 0 until particleCount) {
            val pAngle = (j.toFloat() / particleCount) * 2f * Math.PI.toFloat() + (wavePhase * 0.7f)
            val pDist = baseRadius * 0.55f + (sin((j * 1.5f + wavePhase).toDouble()).toFloat() * 20f)
            val px = centerX + pDist * cos(pAngle.toDouble()).toFloat()
            val py = centerY + pDist * sin(pAngle.toDouble()).toFloat()

            drawCircle(
                color = if (j % 2 == 0) Color(0xFFE9D5FF) else Color(0xFFBAE6FD),
                radius = (2.2.dp.toPx()),
                center = Offset(px, py)
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
