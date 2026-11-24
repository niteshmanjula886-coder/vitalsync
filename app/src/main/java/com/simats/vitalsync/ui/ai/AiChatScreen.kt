package com.simats.vitalsync.ui.ai

import androidx.activity.compose.BackHandler

import android.app.Application
import android.widget.Toast
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.vitalsync.data.local.SessionManager
import com.simats.vitalsync.data.model.ai.AiChatSessionDto
import com.simats.vitalsync.ui.ai.viewmodel.AiViewModel
import com.simats.vitalsync.ui.ai.viewmodel.AiViewModelFactory
import com.simats.vitalsync.ui.theme.CarbonDarkAccent
import com.simats.vitalsync.ui.theme.CarbonDarkBackground
import com.simats.vitalsync.ui.theme.CarbonDarkBorder
import com.simats.vitalsync.ui.theme.CarbonDarkSurface
import com.simats.vitalsync.ui.theme.CarbonDarkSurfaceVariant
import com.simats.vitalsync.ui.theme.CarbonDarkTextPrimary
import com.simats.vitalsync.ui.theme.CarbonDarkTextSecondary
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalTealPrimary
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import kotlinx.coroutines.launch
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
    userId: Int? = null,
    userEmail: String = "",
    viewModel: AiViewModel = viewModel(
        factory = AiViewModelFactory(LocalContext.current.applicationContext as Application)
    ),
    onBackClick: () -> Unit = {},
    onChatHistoryClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val focusRequester = remember { FocusRequester() }
    val sessionManager = remember { SessionManager.getInstance(context) }
    val resolvedUserId = remember(userId) {
        userId?.takeIf { it > 0 } ?: sessionManager.getUser()?.id?.takeIf { it > 0 }
    }
    val resolvedEmail = remember(userEmail) {
        userEmail.ifBlank { sessionManager.getUserEmail() ?: "" }
    }

    val uiState by viewModel.uiState.collectAsState()
    val isDark = MaterialTheme.colorScheme.background == CarbonDarkBackground
    var messageInput by remember { mutableStateOf("") }
    var editingMessageIndex by remember { mutableStateOf<Int?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Load saved chat sessions and initial conversation for the active user
    LaunchedEffect(resolvedUserId, resolvedEmail) {
        viewModel.loadSessionsAndInitialChat(
            userId = resolvedUserId,
            email = resolvedEmail.ifBlank { null }
        )
    }

    BackHandler(enabled = true) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            onBackClick()
        }
    }

    // Smooth auto-scroll to bottom whenever message count changes or AI begins typing
    LaunchedEffect(uiState.messages.size, uiState.isAiThinking, uiState.isSending) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    fun submitMessage(textToSend: String) {
        val trimmed = textToSend.trim()
        if (trimmed.isEmpty()) return
        messageInput = ""
        val targetEditIndex = editingMessageIndex
        editingMessageIndex = null
        if (targetEditIndex != null) {
            viewModel.editMessageAndResend(
                targetMessageIndex = targetEditIndex,
                newText = trimmed,
                explicitUserId = resolvedUserId,
                explicitEmail = resolvedEmail.ifBlank { null }
            )
        } else {
            viewModel.sendMessage(
                messageText = trimmed,
                explicitUserId = resolvedUserId,
                explicitEmail = resolvedEmail.ifBlank { null }
            )
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(310.dp)
                    .fillMaxHeight(),
                drawerContainerColor = if (isDark) CarbonDarkSurface else Color(0xFFFAFCFB),
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                ChatSessionsDrawerContent(
                    sessions = uiState.sessions,
                    currentSessionId = uiState.currentSessionId,
                    isLoading = uiState.isLoadingSessions,
                    isDark = isDark,
                    onNewChatClick = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.startNewChat()
                    },
                    onSelectSession = { session ->
                        coroutineScope.launch { drawerState.close() }
                        viewModel.selectSession(
                            session = session,
                            userId = resolvedUserId,
                            email = resolvedEmail.ifBlank { null }
                        )
                    },
                    onDeleteSession = { sessionId ->
                        viewModel.deleteSession(
                            sessionId = sessionId,
                            userId = resolvedUserId,
                            email = resolvedEmail.ifBlank { null }
                        )
                    },
                    onCloseDrawer = {
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(if (isDark) CarbonDarkBackground else Color(0xFFFAFCFB))
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Main Chat Conversation Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
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

                    // Header Bar with Session History Drawer Toggle, Jr. Doctor Branding & Quick New Chat
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
                                BackArrowIcon(
                                    modifier = Modifier.size(20.dp),
                                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewModel.loadSessions(
                                        userId = resolvedUserId,
                                        email = resolvedEmail.ifBlank { null }
                                    )
                                    coroutineScope.launch { drawerState.open() }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Menu,
                                    contentDescription = "Menu",
                                    modifier = Modifier.size(22.dp),
                                    tint = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) CarbonDarkSurfaceVariant else Color(0xFFEDE9FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.HealthAndSafety,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isDark) Color(0xFFC084FC) else Color(0xFF7C3AED)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "VitalSync AI",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Quick New Chat Action Button
                            IconButton(
                                onClick = { viewModel.startNewChat() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "New Chat",
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                                )
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
                                        color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("+ New Chat") },
                                        onClick = {
                                            showMenu = false
                                            viewModel.startNewChat()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Chat History") },
                                        onClick = {
                                            showMenu = false
                                            viewModel.loadSessions(
                                                userId = resolvedUserId,
                                                email = resolvedEmail.ifBlank { null }
                                            )
                                            coroutineScope.launch { drawerState.open() }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Clear Current Chat") },
                                        onClick = {
                                            showMenu = false
                                            viewModel.clearChatHistory(
                                                userId = resolvedUserId,
                                                email = resolvedEmail.ifBlank { null }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.messages.isEmpty() && !uiState.isLoadingMessages && !uiState.isAiThinking) {
                    Spacer(modifier = Modifier.height(36.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) CarbonDarkSurface else Color.White),
                        border = if (isDark) BorderStroke(1.dp, CarbonDarkBorder) else null,
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.HealthAndSafety,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = if (isDark) Color(0xFFC084FC) else Color(0xFF7C3AED)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "How can I help today?",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ask about your heart rate, sleep recovery, hydration goals, workout readiness, or any wellness question.",
                                fontSize = 13.sp,
                                color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280),
                                lineHeight = 19.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Dynamic Chat Messages List
                uiState.messages.forEachIndexed { index, msg ->
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
                                    .background(if (isDark) Color(0xFF2F2F2F) else VitalTealDark)
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
                            // Bottom Action Row: Timestamp, Copy & Edit
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (msg.timeText.isNotBlank()) {
                                    Text(
                                        text = msg.timeText,
                                        fontSize = 10.sp,
                                        color = if (isDark) CarbonDarkTextSecondary else Color(0xFF9CA3AF)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                // Copy Button
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(msg.text))
                                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 5.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy message",
                                        tint = if (isDark) CarbonDarkTextSecondary else Color(0xFF9CA3AF),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Copy",
                                        fontSize = 11.sp,
                                        color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280),
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Edit Button
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            editingMessageIndex = index
                                            messageInput = msg.text
                                            focusRequester.requestFocus()
                                        }
                                        .padding(horizontal = 5.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit message",
                                        tint = if (isDark) CarbonDarkTextSecondary else Color(0xFF9CA3AF),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Edit",
                                        fontSize = 11.sp,
                                        color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
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
                                colors = CardDefaults.cardColors(containerColor = if (isDark) CarbonDarkSurface else Color(0xFFF3E8FF)),
                                border = if (isDark) BorderStroke(1.dp, Color(0xFF2F2F2F)) else null,
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.AutoAwesome,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = if (isDark) Color(0xFFC084FC) else Color(0xFF7C3AED)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "VitalSync AI",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) Color(0xFFC084FC) else Color(0xFF7C3AED)
                                            )
                                        }

                                        if (msg.timeText.isNotBlank()) {
                                            Text(
                                                text = msg.timeText,
                                                fontSize = 10.sp,
                                                color = if (isDark) Color(0xFF8E8E93) else Color(0xFF9CA3AF)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    val formattedText = remember(msg.text) {
                                        formatAiMessageAnnotatedString(msg.text)
                                    }
                                    Text(
                                        text = formattedText,
                                        fontSize = 14.sp,
                                        color = if (isDark) Color(0xFFECECEC) else Color(0xFF111827),
                                        lineHeight = 21.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Dedicated AI Thinking / Typing Bubble
                if (uiState.isAiThinking || uiState.isSending) {
                    TypingIndicatorBubble()
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Input Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                if (editingMessageIndex != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = if (isDark) CarbonDarkAccent else VitalTealDark,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Editing previous message",
                                fontSize = 12.sp,
                                color = if (isDark) CarbonDarkAccent else VitalTealDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "Cancel",
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    editingMessageIndex = null
                                    messageInput = ""
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            text = if (editingMessageIndex != null) "Edit your message..." else "Ask VitalSync AI anything...",
                            fontSize = 14.sp,
                            color = if (isDark) Color(0xFF8E8E93) else Color(0xFF9CA3AF)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submitMessage(messageInput) }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) CarbonDarkSurfaceVariant else Color.White,
                        unfocusedContainerColor = if (isDark) CarbonDarkSurfaceVariant else Color.White,
                        focusedTextColor = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827),
                        unfocusedTextColor = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827),
                        focusedBorderColor = if (isDark) CarbonDarkAccent else VitalTealPrimary,
                        unfocusedBorderColor = if (isDark) Color(0xFF3D3D3D) else Color(0xFFE5E7EB)
                    ),
                    trailingIcon = {
                        Row(
                            modifier = Modifier.padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isCurrentlyGenerating = uiState.isGenerating || uiState.isSending || uiState.isAiThinking

                            if (isCurrentlyGenerating) {
                                // Stop Generation Button
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                        .clickable {
                                            viewModel.stopGeneration()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Stop generation",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                // Send Message Button
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (messageInput.isNotBlank()) (if (isDark) CarbonDarkAccent else VitalTealPrimary) else (if (isDark) Color(0xFF3D3D3D) else Color(0xFFD1D5DB)))
                                        .clickable(enabled = messageInput.isNotBlank()) {
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
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "General wellness guidance only · Not a medical diagnosis.",
                    fontSize = 10.sp,
                    color = if (isDark) CarbonDarkTextSecondary else Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
}

@Composable
private fun ChatSessionsDrawerContent(
    sessions: List<AiChatSessionDto>,
    currentSessionId: Int?,
    isLoading: Boolean,
    isDark: Boolean = false,
    onNewChatClick: () -> Unit,
    onSelectSession: (AiChatSessionDto) -> Unit,
    onDeleteSession: (Int) -> Unit,
    onCloseDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // Drawer Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.ChatBubbleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = VitalTealDark
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VitalSync AI History",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) CarbonDarkTextPrimary else Color(0xFF111827)
                )
            }
            IconButton(
                onClick = onCloseDrawer,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    modifier = Modifier.size(18.dp),
                    tint = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prominent "+ New Chat" Button
        Button(
            onClick = onNewChatClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) CarbonDarkAccent else VitalTealDark)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "+ New Chat",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Past Conversations",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) CarbonDarkTextSecondary else Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading && sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = if (isDark) CarbonDarkAccent else VitalTealPrimary,
                    strokeWidth = 2.dp
                )
            }
        } else if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = Color(0xFF9CA3AF)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No saved conversations yet.\nTap '+ New Chat' to begin!",
                        fontSize = 13.sp,
                        color = if (isDark) CarbonDarkTextSecondary else Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions, key = { it.id }) { sess ->
                    val isSelected = (sess.id == currentSessionId)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectSession(sess) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) (if (isDark) Color(0xFF2A2640) else Color(0xFFEDE9FE)) else (if (isDark) CarbonDarkSurfaceVariant else Color(0xFFF3F4F6))
                        ),
                        border = if (isDark) BorderStroke(1.dp, CarbonDarkBorder) else null,
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sess.title,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) (if (isDark) Color(0xFFC084FC) else Color(0xFF7C3AED)) else (if (isDark) CarbonDarkTextPrimary else Color(0xFF1F2937)),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val timeDisplay = sess.updatedAt.takeIf { it.isNotBlank() }?.take(16) ?: ""
                                val msgCountStr = if (sess.messageCount > 0) "${sess.messageCount} msgs" else ""
                                val metaText = listOf(timeDisplay, msgCountStr).filter { it.isNotBlank() }.joinToString(" • ")
                                if (metaText.isNotBlank()) {
                                    Text(
                                        text = metaText,
                                        fontSize = 10.sp,
                                        color = if (isDark) CarbonDarkTextSecondary else Color(0xFF9CA3AF),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onDeleteSession(sess.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicatorBubble(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "TypingIndicatorTransition")

    // Vertical bounce offset per dot spaced sequentially (0ms, 150ms, 300ms)
    val dot1Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, delayMillis = 0, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot1Offset"
    )
    val dot2Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, delayMillis = 150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot2Offset"
    )
    val dot3Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, delayMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot3Offset"
    )

    // Pulsing alpha per dot spaced sequentially (0ms, 150ms, 300ms)
    val dot1Alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, delayMillis = 0, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot1Alpha"
    )
    val dot2Alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, delayMillis = 150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot2Alpha"
    )
    val dot3Alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, delayMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot3Alpha"
    )

    val isDark = MaterialTheme.colorScheme.background == CarbonDarkBackground
    val dotColor = if (isDark) Color(0xFFC084FC) else Color(0xFF7C3AED)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 28.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) CarbonDarkSurface else Color(0xFFF3E8FF)),
            border = if (isDark) BorderStroke(1.dp, Color(0xFF2F2F2F)) else null,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isDark) Color(0xFFC084FC) else Color(0xFF7C3AED)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "VitalSync AI",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFC084FC) else Color(0xFF7C3AED)
                        )
                    }

                    Text(
                        text = "Thinking...",
                        fontSize = 10.sp,
                        color = if (isDark) Color(0xFF8E8E93) else Color(0xFF9CA3AF)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .offset(y = dot1Offset.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(dotColor.copy(alpha = dot1Alpha))
                    )
                    Box(
                        modifier = Modifier
                            .offset(y = dot2Offset.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(dotColor.copy(alpha = dot2Alpha))
                    )
                    Box(
                        modifier = Modifier
                            .offset(y = dot3Offset.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(dotColor.copy(alpha = dot3Alpha))
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "VitalSync AI is thinking...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = dotColor
                    )
                }
            }
        }
    }
}

// Ultra-Vivid Futuristic Glowing Violet/Magenta/Cyan AI Particle Orb Animation
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

/**
 * Formats markdown-style AI output (**bold**, *italic*, headings, and bullets) cleanly for Compose Text.
 */
private fun formatAiMessageAnnotatedString(rawText: String): AnnotatedString {
    val normalized = rawText
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()

    val lines = normalized.split("\n").map { line ->
        val trimmedEnd = line.trimEnd()
        when {
            trimmedEnd.matches(Regex("^#{1,6}\\s+.*")) ->
                "**${trimmedEnd.replaceFirst(Regex("^#{1,6}\\s+"), "")}**"
            trimmedEnd.matches(Regex("^\\s*[-*•]\\s+.*")) ->
                "• ${trimmedEnd.replaceFirst(Regex("^\\s*[-*•]\\s+"), "")}"
            else -> trimmedEnd
        }
    }
    val cleaned = lines.joinToString("\n")

    return buildAnnotatedString {
        var idx = 0
        while (idx < cleaned.length) {
            if (cleaned.startsWith("**", idx)) {
                val endIdx = cleaned.indexOf("**", idx + 2)
                if (endIdx > idx + 2) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(cleaned.substring(idx + 2, endIdx))
                    }
                    idx = endIdx + 2
                    continue
                }
            } else if (cleaned[idx] == '*') {
                val endIdx = cleaned.indexOf('*', idx + 1)
                if (endIdx > idx + 1 && !cleaned.substring(idx + 1, endIdx).contains('\n')) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(cleaned.substring(idx + 1, endIdx))
                    }
                    idx = endIdx + 1
                    continue
                }
            }
            append(cleaned[idx])
            idx++
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

