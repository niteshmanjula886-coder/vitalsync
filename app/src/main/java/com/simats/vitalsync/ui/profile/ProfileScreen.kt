package com.simats.vitalsync.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.theme.VitalsyncTheme

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    userName: String = "Alex Morgan",
    userInitials: String = "AM",
    userAge: String = "28",
    userHeight: String = "175",
    userWeight: String = "68.5",
    userPhotoBitmap: ImageBitmap? = null,
    onEditProfileClick: () -> Unit = {},
    onPersonalInformationClick: () -> Unit = {},
    onHealthGoalsClick: () -> Unit = {},
    onConnectedDevicesClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onPrivacySecurityClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onHelpSupportClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
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

        Spacer(modifier = Modifier.height(20.dp))

        // Choosable/Editable Avatar Circle
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary)
                .clickable { onPersonalInformationClick() },
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
                    color = MaterialTheme.colorScheme.primary
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
                text = userName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(6.dp))
            PencilEditIcon(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Goal Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.secondary)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Goal: Improve Fitness",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
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
                    Text(text = "$userAge years", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Height", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$userHeight cm", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Weight", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$userWeight kg", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
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

                ProfileMenuItem(title = "Connected Devices", iconType = ProfileMenuIcon.DEVICES, onClick = onConnectedDevicesClick)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))

                ProfileMenuItem(title = "Notifications", iconType = ProfileMenuIcon.BELL, onClick = onNotificationsClick)
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
                .clickable { onLogoutClick() },
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
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun ProfileScreenPreview() {
    VitalsyncTheme {
        ProfileScreen()
    }
}
