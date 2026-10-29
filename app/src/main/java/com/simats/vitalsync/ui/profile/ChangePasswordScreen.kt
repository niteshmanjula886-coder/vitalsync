package com.simats.vitalsync.ui.profile

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
fun ChangePasswordScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onUpdatePasswordSuccess: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isCurrentVisible by remember { mutableStateOf(false) }
    var isNewVisible by remember { mutableStateOf(false) }
    var isConfirmVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFCFB))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Bar
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

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Change Password",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Ensure your account is using a strong and unique password.",
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Current Password
            Text(text = "Current Password", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                visualTransformation = if (isCurrentVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color(0xFF111827),
                    unfocusedTextColor = Color(0xFF111827),
                    focusedBorderColor = VitalTealPrimary,
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                trailingIcon = {
                    IconButton(onClick = { isCurrentVisible = !isCurrentVisible }) {
                        EyeVisibilityIcon(isVisible = isCurrentVisible, modifier = Modifier.size(20.dp))
                    }
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Forgot password?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = VitalTealPrimary,
                    modifier = Modifier.clickable { onForgotPasswordClick() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // New Password
            Text(text = "New Password", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                visualTransformation = if (isNewVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color(0xFF111827),
                    unfocusedTextColor = Color(0xFF111827),
                    focusedBorderColor = VitalTealPrimary,
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                trailingIcon = {
                    IconButton(onClick = { isNewVisible = !isNewVisible }) {
                        EyeVisibilityIcon(isVisible = isNewVisible, modifier = Modifier.size(20.dp))
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password
            Text(text = "Confirm New Password", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                visualTransformation = if (isConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color(0xFF111827),
                    unfocusedTextColor = Color(0xFF111827),
                    focusedBorderColor = VitalTealPrimary,
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                trailingIcon = {
                    IconButton(onClick = { isConfirmVisible = !isConfirmVisible }) {
                        EyeVisibilityIcon(isVisible = isConfirmVisible, modifier = Modifier.size(20.dp))
                    }
                }
            )

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Save Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Button(
                onClick = { onUpdatePasswordSuccess() },
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
                    text = "Update Password",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
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
fun ChangePasswordScreenPreview() {
    VitalsyncTheme {
        ChangePasswordScreen()
    }
}
