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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.IconButton
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
fun SignUpScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    var name by remember { mutableStateOf("Alex Morgan") }
    var email by remember { mutableStateOf("alex.morgan@email.com") }
    var password by remember { mutableStateOf("1234567890") }
    var confirmPassword by remember { mutableStateOf("1234567890") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var isTermsAccepted by remember { mutableStateOf(true) }

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
            Spacer(modifier = Modifier.height(16.dp))

            // Top Bar with Back Arrow and Title
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
                        color = Color(0xFF111827)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Create your account",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Subtitle Header with Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE6F4EA)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        HeartPulseIconSmall(modifier = Modifier.size(22.dp))
                        SparkleIconTiny(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "A healthier you, one day at a time.",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Name Field
            Text(
                text = "Name",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151)
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
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
                    UserPersonIcon(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF9CA3AF)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Field
            Text(
                text = "Email",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151)
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
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

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            Text(
                text = "Password",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151)
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
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
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field
            Text(
                text = "Confirm Password",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151)
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                        onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }
                    ) {
                        EyeVisibilityIcon(
                            isVisible = isConfirmPasswordVisible,
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

            Spacer(modifier = Modifier.height(14.dp))

            // Checkbox & Terms
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isTermsAccepted,
                    onCheckedChange = { isTermsAccepted = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = VitalTealPrimary,
                        uncheckedColor = Color(0xFF9CA3AF)
                    )
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "I agree to the Terms of Service and Privacy Policy.",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Create Account Button
            Button(
                onClick = onSignUpSuccess,
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
                    text = "Create Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Already have an account? Login
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.clickable { onNavigateToLogin() }
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = "Login",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VitalTealDark
                )
            }
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

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun HeartPulseIconSmall(modifier: Modifier = Modifier) {
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
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = pulseLine,
            color = Color(0xFF0F9F7A),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun SparkleIconTiny(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.5f, 0f)
            quadraticTo(w * 0.5f, h * 0.5f, w, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.5f, w * 0.5f, h)
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
private fun UserPersonIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Head
        drawCircle(
            color = color,
            radius = w * 0.22f,
            center = Offset(w * 0.5f, h * 0.30f),
            style = Stroke(width = 1.6.dp.toPx())
        )

        // Shoulder curve
        val bodyPath = Path().apply {
            moveTo(w * 0.15f, h * 0.85f)
            cubicTo(
                w * 0.15f, h * 0.60f,
                w * 0.85f, h * 0.60f,
                w * 0.85f, h * 0.85f
            )
        }

        drawPath(
            path = bodyPath,
            color = color,
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun SignUpScreenPreview() {
    VitalsyncTheme {
        SignUpScreen()
    }
}
