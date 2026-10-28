package com.simats.vitalsync.ui.auth

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalTealPrimary
import com.simats.vitalsync.ui.theme.VitalsyncTheme

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onResetLinkSent: () -> Unit = {}
) {
    var email by remember { mutableStateOf("alex.morgan@email.com") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }
    var isSuccessBannerVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Bar
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
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Reset Password",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Key Lock Icon Header
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFE6F4EA)),
                contentAlignment = Alignment.Center
            ) {
                KeyLockIcon(modifier = Modifier.size(36.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title
            Text(
                text = "Set New Password",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Enter your email along with your new password to update your account.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 1. Email Address Label
            Text(
                text = "Email Address",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Email Input Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = ""
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = VitalTealPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                trailingIcon = {
                    EnvelopeIcon(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. New Password Label
            Text(
                text = "New Password",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            // New Password Input Field
            OutlinedTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    errorMessage = ""
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = VitalTealPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        if (isPasswordVisible) {
                            EyeOffIcon(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            EyeIcon(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Confirm Password Label
            Text(
                text = "Confirm Password",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Confirm Password Input Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = ""
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = VitalTealPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                trailingIcon = {
                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                        if (isConfirmPasswordVisible) {
                            EyeOffIcon(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            EyeIcon(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error Message Display
            if (errorMessage.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEE2E2))
                        .padding(12.dp)
                ) {
                    Text(
                        text = errorMessage,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Success Confirmation Banner
            if (isSuccessBannerVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFDCFCE7))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Password reset successfully! You can now back to login.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF166534)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Reset Password Button
            Button(
                onClick = {
                    when {
                        email.isBlank() || !email.contains("@") -> {
                            errorMessage = "Please enter a valid email address"
                        }
                        newPassword.length < 6 -> {
                            errorMessage = "Password must be at least 6 characters"
                        }
                        newPassword != confirmPassword -> {
                            errorMessage = "Passwords do not match"
                        }
                        else -> {
                            errorMessage = ""
                            isSuccessBannerVisible = true
                            onResetLinkSent()
                        }
                    }
                },
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
                    text = "Reset Password",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Back to Login link
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.clickable { onBackClick() }
            ) {
                Text(
                    text = "Remember password? ",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Back to Login",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VitalTealDark
                )
            }
        }
    }
}

@Composable
fun EyeIcon(modifier: Modifier = Modifier, color: Color = Color.Gray) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val eyePath = Path().apply {
            moveTo(0f, h * 0.5f)
            quadraticTo(w * 0.5f, 0f, w, h * 0.5f)
            quadraticTo(w * 0.5f, h, 0f, h * 0.5f)
            close()
        }

        drawPath(path = eyePath, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(color = color, radius = w * 0.18f, center = Offset(w * 0.5f, h * 0.5f))
    }
}

@Composable
fun EyeOffIcon(modifier: Modifier = Modifier, color: Color = Color.Gray) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val eyePath = Path().apply {
            moveTo(0f, h * 0.5f)
            quadraticTo(w * 0.5f, 0f, w, h * 0.5f)
            quadraticTo(w * 0.5f, h, 0f, h * 0.5f)
            close()
        }

        val slashLine = Path().apply {
            moveTo(w * 0.15f, h * 0.85f)
            lineTo(w * 0.85f, h * 0.15f)
        }

        drawPath(path = eyePath, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(color = color, radius = w * 0.18f, center = Offset(w * 0.5f, h * 0.5f))
        drawPath(path = slashLine, color = color, style = Stroke(width = 2.0.dp.toPx(), cap = StrokeCap.Round))
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

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun KeyLockIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Key Ring Circle
        drawCircle(
            color = Color(0xFF0F9F7A),
            radius = w * 0.22f,
            center = Offset(w * 0.35f, h * 0.38f),
            style = Stroke(width = 2.2.dp.toPx())
        )

        // Key shaft and teeth
        val keyShaft = Path().apply {
            moveTo(w * 0.52f, h * 0.52f)
            lineTo(w * 0.82f, h * 0.82f)
            lineTo(w * 0.72f, h * 0.92f)
            moveTo(w * 0.70f, h * 0.70f)
            lineTo(w * 0.80f, h * 0.80f)
        }

        drawPath(
            path = keyShaft,
            color = Color(0xFF0F9F7A),
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun EnvelopeIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val envPath = Path().apply {
            moveTo(0f, h * 0.22f)
            lineTo(w, h * 0.22f)
            lineTo(w, h * 0.78f)
            lineTo(0f, h * 0.78f)
            close()
        }

        val flapPath = Path().apply {
            moveTo(0f, h * 0.22f)
            lineTo(w * 0.5f, h * 0.55f)
            lineTo(w, h * 0.22f)
        }

        drawPath(
            path = envPath,
            color = color,
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = flapPath,
            color = color,
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun ForgotPasswordScreenPreview() {
    VitalsyncTheme {
        ForgotPasswordScreen()
    }
}
