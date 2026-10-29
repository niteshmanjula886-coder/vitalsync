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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.simats.vitalsync.data.model.auth.User
import com.simats.vitalsync.ui.auth.viewmodel.AuthUiState
import com.simats.vitalsync.ui.auth.viewmodel.AuthViewModel
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalTealPrimary
import com.simats.vitalsync.ui.theme.VitalsyncTheme

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel? = null,
    onLoginSuccess: () -> Unit = {},
    onLoginSuccessWithUser: (User?) -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    val loginState by viewModel?.loginState?.collectAsState() ?: remember { mutableStateOf(AuthUiState()) }
    val sessionExpiredNotice by viewModel?.sessionExpiredNotice?.collectAsState() ?: remember { mutableStateOf<String?>(null) }

    var email by remember { mutableStateOf("alex.morgan@email.com") }
    var password by remember { mutableStateOf("1234567890") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Trigger success callback when login succeeds
    LaunchedEffect(loginState.isSuccess) {
        if (loginState.isSuccess) {
            onLoginSuccessWithUser(loginState.user)
            onLoginSuccess()
            viewModel?.resetLoginState()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFCFB))
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
            Spacer(modifier = Modifier.height(28.dp))

            // Top App Logo Icon Card
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFE6F4EA)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HeartPulseIcon(modifier = Modifier.size(32.dp))
                    SparkleIconSmall(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Title
            Text(
                text = "Welcome Back",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Your healthier day starts here.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Session Expired Banner (Auto-Logout Notice)
            if (!sessionExpiredNotice.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEF3C7))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⏱️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = sessionExpiredNotice ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Email Label
            Text(
                text = "Email",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email Outlined TextField
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    viewModel?.clearLoginError()
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = VitalTealPrimary,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedTextColor = Color(0xFF111827),
                    unfocusedTextColor = Color(0xFF111827)
                ),
                trailingIcon = {
                    EnvelopeIcon(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF9CA3AF)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Password Label
            Text(
                text = "Password",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Password Outlined TextField
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    viewModel?.clearLoginError()
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = VitalTealPrimary,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedTextColor = Color(0xFF111827),
                    unfocusedTextColor = Color(0xFF111827)
                ),
                trailingIcon = {
                    IconButton(
                        onClick = { isPasswordVisible = !isPasswordVisible }
                    ) {
                        EyeVisibilityIcon(
                            isVisible = isPasswordVisible,
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF9CA3AF)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Forgot Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Forgot Password?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VitalTealDark,
                    modifier = Modifier.clickable { onForgotPasswordClick() }
                )
            }

            // Error Message Banner (from ViewModel)
            if (!loginState.errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEE2E2))
                        .padding(12.dp)
                ) {
                    Text(
                        text = loginState.errorMessage ?: "",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFDC2626)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Login Button (with Loading Spinner support)
            Button(
                onClick = {
                    if (viewModel != null) {
                        viewModel.login(email = email, password = password)
                    } else {
                        onLoginSuccess()
                    }
                },
                enabled = !loginState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VitalTealDark,
                    contentColor = Color.White,
                    disabledContainerColor = VitalTealDark.copy(alpha = 0.65f),
                    disabledContentColor = Color.White.copy(alpha = 0.8f)
                )
            ) {
                if (loginState.isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Logging in...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Login",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            // Sign Up Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.clickable { onNavigateToSignUp() }
            ) {
                Text(
                    text = "Don't have an account? ",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = "Sign Up",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VitalTealDark
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Security Footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                ShieldIcon(
                    modifier = Modifier.size(16.dp),
                    color = VitalTealPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Your health data stays private and secure.",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}

@Composable
private fun HeartPulseIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val heartPath = Path().apply {
            moveTo(w * 0.5f, h * 0.82f)
            cubicTo(
                w * 0.15f, h * 0.58f,
                w * 0.02f, h * 0.30f,
                w * 0.25f, h * 0.14f
            )
            cubicTo(
                w * 0.38f, h * 0.06f,
                w * 0.48f, h * 0.16f,
                w * 0.50f, h * 0.24f
            )
            cubicTo(
                w * 0.52f, h * 0.16f,
                w * 0.62f, h * 0.06f,
                w * 0.75f, h * 0.14f
            )
            cubicTo(
                w * 0.98f, h * 0.30f,
                w * 0.85f, h * 0.58f,
                w * 0.5f, h * 0.82f
            )
            close()
        }

        val pulseLine = Path().apply {
            moveTo(w * 0.20f, h * 0.48f)
            lineTo(w * 0.38f, h * 0.48f)
            lineTo(w * 0.45f, h * 0.34f)
            lineTo(w * 0.53f, h * 0.62f)
            lineTo(w * 0.62f, h * 0.48f)
            lineTo(w * 0.80f, h * 0.48f)
        }

        drawPath(
            path = heartPath,
            color = Color(0xFF0F9F7A),
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = pulseLine,
            color = Color(0xFF0F9F7A),
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun SparkleIconSmall(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.5f, 0f)
            quadraticTo(w * 0.5f, h * 0.5f, w, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.5f, w, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.5f, 0f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.5f, w * 0.5f, 0f)
            close()
        }

        drawPath(
            path = path,
            color = Color(0xFF0F9F7A)
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

@Composable
private fun EyeVisibilityIcon(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val eyePath = Path().apply {
            moveTo(0f, h * 0.5f)
            quadraticTo(w * 0.5f, 0f, w, h * 0.5f)
            quadraticTo(w * 0.5f, h, 0f, h * 0.5f)
            close()
        }

        drawPath(
            path = eyePath,
            color = color,
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        drawCircle(
            color = color,
            radius = w * 0.20f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        if (!isVisible) {
            drawLine(
                color = color,
                start = Offset(w * 0.15f, h * 0.85f),
                end = Offset(w * 0.85f, h * 0.15f),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun ShieldIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val shieldPath = Path().apply {
            moveTo(w * 0.5f, 0f)
            lineTo(w, h * 0.22f)
            lineTo(w, h * 0.55f)
            cubicTo(w, h * 0.82f, w * 0.5f, h, w * 0.5f, h)
            cubicTo(w * 0.5f, h, 0f, h * 0.82f, 0f, h * 0.55f)
            lineTo(0f, h * 0.22f)
            close()
        }

        val checkPath = Path().apply {
            moveTo(w * 0.32f, h * 0.50f)
            lineTo(w * 0.46f, h * 0.65f)
            lineTo(w * 0.70f, h * 0.38f)
        }

        drawPath(
            path = shieldPath,
            color = color,
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = checkPath,
            color = color,
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun LoginScreenPreview() {
    VitalsyncTheme {
        LoginScreen()
    }
}
