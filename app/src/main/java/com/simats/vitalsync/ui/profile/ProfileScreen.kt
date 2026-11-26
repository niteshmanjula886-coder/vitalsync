package com.simats.vitalsync.ui.profile

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.simats.vitalsync.ui.theme.CarbonDarkAccent
import com.simats.vitalsync.ui.theme.CarbonDarkBackground
import com.simats.vitalsync.ui.theme.CarbonDarkSurfaceVariant
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    userName: String = "",
    userInitials: String = "--",
    userAge: String = "--",
    userHeight: String = "--",
    userWeight: String = "--",
    userPrimaryGoal: String? = null,
    userPhotoBitmap: ImageBitmap? = null,
    onEditProfileClick: () -> Unit = {},
    onPersonalInformationClick: () -> Unit = {},
    onHealthGoalsClick: () -> Unit = {},
    onLogVitalsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onPrivacySecurityClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onHelpSupportClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val isDark = MaterialTheme.colorScheme.background == CarbonDarkBackground
    val haptic = LocalHapticFeedback.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPhotoViewer by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Log Out",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of VitalSync?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    }
                ) {
                    Text(
                        text = "Log Out",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(18.dp)
        )
    }

    if (showPhotoViewer) {
        FullScreenProfileImageViewer(
            userPhotoBitmap = userPhotoBitmap,
            userName = userName,
            userInitials = userInitials,
            isDark = isDark,
            onDismiss = { showPhotoViewer = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pinned Top Header (Persists cleanly below status bar/cutout without scrolling away)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profile",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            IconButton(
                onClick = onEditProfileClick,
                modifier = Modifier.size(36.dp)
            ) {
                PencilEditIcon(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onBackground)
            }
        }

        // Scrollable Profile Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Profile Avatar with Camera Edit Badge
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center
            ) {
                // Avatar Circle (Tap or Long Press opens full-screen photo preview)
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(if (isDark) CarbonDarkSurfaceVariant else MaterialTheme.colorScheme.secondary)
                        .combinedClickable(
                            onClick = { showPhotoViewer = true },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showPhotoViewer = true
                            },
                            onClickLabel = "View profile photo",
                            onLongClickLabel = "View profile photo"
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (userPhotoBitmap != null) {
                        Image(
                            bitmap = userPhotoBitmap,
                            contentDescription = "User Profile Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = userInitials,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) CarbonDarkAccent else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Edit Photo Camera Badge (Independent button to edit photo)
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(
                            onClick = onPersonalInformationClick,
                            onClickLabel = "Change profile photo"
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoCamera,
                        contentDescription = "Change profile photo",
                        tint = if (isDark) CarbonDarkBackground else Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

        Spacer(modifier = Modifier.height(12.dp))

        // Name (Clickable to Edit)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onPersonalInformationClick() }
        ) {
            Text(
                text = userName.ifBlank { "Set up profile" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(6.dp))
            PencilEditIcon(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Goal Badge
        val goalText = if (!userPrimaryGoal.isNullOrBlank()) "Goal: $userPrimaryGoal" else "Goal: No Goal Set"
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isDark) CarbonDarkSurfaceVariant else MaterialTheme.colorScheme.secondary)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = goalText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) CarbonDarkAccent else MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // User Stats Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Age", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    val ageDisplay = if (userAge.isNotBlank() && userAge != "--" && userAge != "0") "$userAge years" else "--"
                    Text(text = ageDisplay, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Height", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    val heightDisplay = if (userHeight.isNotBlank() && userHeight != "--" && userHeight != "0") "$userHeight cm" else "--"
                    Text(text = heightDisplay, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Weight", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    val weightDisplay = if (userWeight.isNotBlank() && userWeight != "--" && userWeight != "0") "$userWeight kg" else "--"
                    Text(text = weightDisplay, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Settings Menu Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                ProfileMenuItem(title = "Personal Information", iconType = ProfileMenuIcon.PERSON, onClick = onPersonalInformationClick)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))

                ProfileMenuItem(title = "Health Goals", iconType = ProfileMenuIcon.GOALS, onClick = onHealthGoalsClick)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))

                ProfileMenuItem(title = "Privacy & Security", iconType = ProfileMenuIcon.SECURITY, onClick = onPrivacySecurityClick)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))

                ProfileMenuItem(title = "Settings", iconType = ProfileMenuIcon.SETTINGS, onClick = onSettingsClick)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))

                ProfileMenuItem(title = "Help & Support", iconType = ProfileMenuIcon.HELP, onClick = onHelpSupportClick)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Logout Card Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showLogoutDialog = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Logout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

enum class ProfileMenuIcon {
    PERSON,
    GOALS,
    DEVICES,
    BELL,
    SECURITY,
    SETTINGS,
    HELP
}

@Composable
private fun ProfileMenuItem(
    title: String,
    iconType: ProfileMenuIcon,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                when (iconType) {
                    ProfileMenuIcon.PERSON -> PersonIcon(modifier = Modifier.size(18.dp))
                    ProfileMenuIcon.GOALS -> GoalsIcon(modifier = Modifier.size(18.dp))
                    ProfileMenuIcon.DEVICES -> WatchIcon(modifier = Modifier.size(18.dp))
                    ProfileMenuIcon.BELL -> BellIcon(modifier = Modifier.size(18.dp))
                    ProfileMenuIcon.SECURITY -> ShieldIcon(modifier = Modifier.size(18.dp))
                    ProfileMenuIcon.SETTINGS -> SettingsIcon(modifier = Modifier.size(18.dp))
                    ProfileMenuIcon.HELP -> HelpIcon(modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        ChevronRightIcon(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PencilEditIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF374151)
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
private fun ChevronRightIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF9CA3AF)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.3f, h * 0.2f)
            lineTo(w * 0.7f, h * 0.5f)
            lineTo(w * 0.3f, h * 0.8f)
        }

        drawPath(path = path, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun PersonIcon(modifier: Modifier = Modifier) {
    val mint = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(color = mint, radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.3f), style = Stroke(width = 1.6.dp.toPx()))
        val body = Path().apply {
            moveTo(w * 0.15f, h * 0.85f)
            cubicTo(w * 0.15f, h * 0.60f, w * 0.85f, h * 0.60f, w * 0.85f, h * 0.85f)
        }
        drawPath(path = body, color = mint, style = Stroke(width = 1.6.dp.toPx()))
    }
}

@Composable
private fun GoalsIcon(modifier: Modifier = Modifier) {
    val mint = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(color = mint, radius = w * 0.40f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = 1.6.dp.toPx()))
        drawCircle(color = mint, radius = w * 0.20f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = 1.6.dp.toPx()))
    }
}

@Composable
private fun WatchIcon(modifier: Modifier = Modifier) {
    val mint = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(color = mint, radius = w * 0.30f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = 1.6.dp.toPx()))
        drawLine(color = mint, start = Offset(w * 0.5f, 0f), end = Offset(w * 0.5f, h * 0.20f), strokeWidth = 1.6.dp.toPx())
        drawLine(color = mint, start = Offset(w * 0.5f, h * 0.80f), end = Offset(w * 0.5f, h), strokeWidth = 1.6.dp.toPx())
    }
}

@Composable
private fun BellIcon(modifier: Modifier = Modifier) {
    val mint = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val bell = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            cubicTo(w * 0.25f, h * 0.25f, w * 0.2f, h * 0.65f, w * 0.15f, h * 0.75f)
            lineTo(w * 0.85f, h * 0.75f)
            cubicTo(w * 0.8f, h * 0.65f, w * 0.75f, h * 0.25f, w * 0.5f, h * 0.15f)
            close()
        }
        drawPath(path = bell, color = mint, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun ShieldIcon(modifier: Modifier = Modifier) {
    val mint = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val shield = Path().apply {
            moveTo(w * 0.5f, 0f)
            lineTo(w, h * 0.22f)
            lineTo(w, h * 0.55f)
            cubicTo(w, h * 0.82f, w * 0.5f, h, w * 0.5f, h)
            cubicTo(w * 0.5f, h, 0f, h * 0.82f, 0f, h * 0.55f)
            lineTo(0f, h * 0.22f)
            close()
        }
        drawPath(path = shield, color = mint, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun SettingsIcon(modifier: Modifier = Modifier) {
    val mint = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(color = mint, radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = 1.6.dp.toPx()))
        drawCircle(color = mint, radius = w * 0.42f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = 1.4.dp.toPx()))
    }
}

@Composable
private fun HelpIcon(modifier: Modifier = Modifier) {
    val mint = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(color = mint, radius = w * 0.40f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = 1.6.dp.toPx()))
    }
}

@Composable
private fun FullScreenProfileImageViewer(
    userPhotoBitmap: ImageBitmap?,
    userName: String,
    userInitials: String,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF50A0D10))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (scale <= 1.05f) {
                        onDismiss()
                    }
                }
        ) {
            // Interactive Image / Fallback Avatar Container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 72.dp)
                    .onGloballyPositioned { coordinates ->
                        containerSize = coordinates.size
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                coroutineScope.launch {
                                    val targetScale = if (scale > 1.05f) 1f else 2.5f
                                    val startScale = scale
                                    val startOffset = offset
                                    animate(
                                        initialValue = 0f,
                                        targetValue = 1f,
                                        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
                                    ) { progress, _ ->
                                        scale = startScale + (targetScale - startScale) * progress
                                        offset = startOffset * (1f - progress)
                                    }
                                }
                            },
                            onTap = {
                                if (scale <= 1.05f) {
                                    onDismiss()
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(1f, 4f)
                            scale = newScale
                            if (newScale > 1f && containerSize.width > 0 && containerSize.height > 0) {
                                val maxX = (containerSize.width.toFloat() * (newScale - 1f)) / 2f
                                val maxY = (containerSize.height.toFloat() * (newScale - 1f)) / 2f
                                offset = Offset(
                                    x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                                    y = (offset.y + pan.y).coerceIn(-maxY, maxY)
                                )
                            } else {
                                offset = Offset.Zero
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (userPhotoBitmap != null) {
                        Image(
                            bitmap = userPhotoBitmap,
                            contentDescription = "Full Profile Photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        // Default Avatar fallback (Large circular badge)
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .clip(CircleShape)
                                .background(if (isDark) CarbonDarkSurfaceVariant else MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userInitials.ifBlank { "--" },
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) CarbonDarkAccent else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Top Bar Overlay (Translucent header bar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xCC111827))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close photo viewer",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userName.ifBlank { "Profile Photo" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (userPhotoBitmap != null) "Double-tap or pinch to zoom" else "Default avatar preview",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun ProfileScreenPreview() {
    VitalsyncTheme {
        ProfileScreen()
    }
}
