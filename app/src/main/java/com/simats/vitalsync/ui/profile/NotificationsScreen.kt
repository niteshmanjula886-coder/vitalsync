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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.simats.vitalsync.data.model.profile.NotificationItem
import com.simats.vitalsync.ui.theme.VitalsyncTheme

@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    notifications: List<NotificationItem> = emptyList(),
    unreadCount: Int = 0,
    isLoading: Boolean = false,
    onMarkAllReadClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
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
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Notifications",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onMarkAllReadClick() },
                contentAlignment = Alignment.Center
            ) {
                DoubleCheckIcon(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Stay in sync with your day.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (unreadCount > 0) {
                Text(
                    text = "Mark all read",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onMarkAllReadClick() }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Alerts",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (unreadCount > 0) {
                Text(
                    text = "$unreadCount unread",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (notifications.isEmpty() && !isLoading) {
            // Default sample notifications if none fetched yet
            NotificationCard(
                icon = "💧",
                title = "Time to drink water",
                description = "Your hydration goal is 62% complete.",
                time = "5 min ago",
                isUnread = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            NotificationCard(
                icon = "🏃‍♂️",
                title = "Activity Reminder",
                description = "You have 1,755 steps left to hit your goal.",
                time = "28 min ago",
                isUnread = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            NotificationCard(
                icon = "🌙",
                title = "Sleep Reminder",
                description = "Your scheduled bedtime is in 30 minutes.",
                time = "Yesterday • Read",
                isUnread = false
            )
            Spacer(modifier = Modifier.height(12.dp))
            NotificationCard(
                icon = "✨",
                title = "AI Wellness Update",
                description = "Your weekly wellness report is ready to view.",
                time = "Yesterday • Read",
                isUnread = false
            )
        } else {
            notifications.forEachIndexed { index, notif ->
                val icon = resolveNotificationIcon(notif.icon, notif.title)
                NotificationCard(
                    icon = icon,
                    title = notif.title,
                    description = notif.description,
                    time = notif.timeAgo ?: "Recent",
                    isUnread = !notif.isRead
                )
                if (index < notifications.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

private fun resolveNotificationIcon(iconStr: String?, title: String): String {
    if (!iconStr.isNullOrBlank() && iconStr.length <= 4) return iconStr
    val lower = title.lowercase()
    return when {
        lower.contains("water") || lower.contains("drink") || lower.contains("hydrat") -> "💧"
        lower.contains("step") || lower.contains("activity") || lower.contains("walk") -> "🏃‍♂️"
        lower.contains("sleep") || lower.contains("bed") -> "🌙"
        lower.contains("heart") || lower.contains("pulse") || lower.contains("bp") -> "❤️"
        else -> "✨"
    }
}

@Composable
private fun NotificationCard(
    icon: String,
    title: String,
    description: String,
    time: String,
    isUnread: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (isUnread) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (isUnread) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = time,
                    fontSize = 11.sp,
                    color = if (isUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DoubleCheckIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF0F9F7A)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path1 = Path().apply {
            moveTo(w * 0.1f, h * 0.55f)
            lineTo(w * 0.35f, h * 0.8f)
            lineTo(w * 0.75f, h * 0.25f)
        }

        val path2 = Path().apply {
            moveTo(w * 0.35f, h * 0.55f)
            lineTo(w * 0.55f, h * 0.8f)
            lineTo(w * 0.95f, h * 0.25f)
        }

        drawPath(path = path1, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(path = path2, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
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
fun NotificationsScreenPreview() {
    VitalsyncTheme {
        NotificationsScreen()
    }
}
