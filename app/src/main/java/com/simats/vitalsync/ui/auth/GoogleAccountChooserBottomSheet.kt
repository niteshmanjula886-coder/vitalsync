package com.simats.vitalsync.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.theme.VitalsyncTheme

data class GoogleAccountItem(
    val name: String,
    val email: String,
    val avatarBgColor: Color,
    val initial: String
)

val sampleGoogleAccounts = listOf(
    GoogleAccountItem(
        name = "Alex Morgan",
        email = "alex.morgan@gmail.com",
        avatarBgColor = Color(0xFF1A73E8),
        initial = "A"
    ),
    GoogleAccountItem(
        name = "Alex Sync Work",
        email = "alex.sync@vitalsync.io",
        avatarBgColor = Color(0xFF0F9F7A),
        initial = "A"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleAccountChooserBottomSheet(
    onDismissRequest: () -> Unit = {},
    onAccountSelected: (GoogleAccountItem) -> Unit = {},
    onAddAnotherAccount: () -> Unit = {},
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Google G Logo Header
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF8FAFC)),
                contentAlignment = Alignment.Center
            ) {
                GoogleGLogoLarge(modifier = Modifier.size(26.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Sign in with Google",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Choose an account to continue to VitalSync",
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(color = Color(0xFFF1F5F9))

            Spacer(modifier = Modifier.height(12.dp))

            // Account List
            sampleGoogleAccounts.forEach { account ->
                AccountRow(
                    account = account,
                    onClick = { onAccountSelected(account) }
                )
                HorizontalDivider(color = Color(0xFFF8FAFC), modifier = Modifier.padding(start = 56.dp))
            }

            // Add another account option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onAddAnotherAccount() }
                    .padding(vertical = 14.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Add another account",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF334155)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Privacy terms notice
            Text(
                text = "To continue, Google will share your name, email address, and profile picture with VitalSync. See VitalSync's Privacy Policy and Terms of Service.",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun AccountRow(
    account: GoogleAccountItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(account.avatarBgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = account.initial,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = account.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = account.email,
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun GoogleGLogoLarge(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -30f,
            sweepAngle = 100f,
            useCenter = false,
            style = Stroke(width = w * 0.22f, cap = StrokeCap.Round)
        )
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 70f,
            sweepAngle = 100f,
            useCenter = false,
            style = Stroke(width = w * 0.22f, cap = StrokeCap.Round)
        )
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 170f,
            sweepAngle = 80f,
            useCenter = false,
            style = Stroke(width = w * 0.22f, cap = StrokeCap.Round)
        )
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 250f,
            sweepAngle = 80f,
            useCenter = false,
            style = Stroke(width = w * 0.22f, cap = StrokeCap.Round)
        )
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(w * 0.5f, h * 0.5f),
            end = Offset(w * 0.85f, h * 0.5f),
            strokeWidth = w * 0.22f,
            cap = StrokeCap.Round
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun GoogleAccountChooserPreview() {
    VitalsyncTheme {
        GoogleAccountChooserBottomSheet()
    }
}
