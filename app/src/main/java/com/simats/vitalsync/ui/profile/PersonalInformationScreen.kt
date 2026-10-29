package com.simats.vitalsync.ui.profile

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalTealPrimary
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import java.util.Locale

fun calculateAgeFromDob(dobStr: String): Int {
    if (dobStr.isBlank()) return 28
    val currentYear = 2025
    val yearRegex = Regex("""\b(19\d\d|20\d\d)\b""")
    val match = yearRegex.find(dobStr)
    if (match != null) {
        val birthYear = match.value.toIntOrNull() ?: 1996
        return (currentYear - birthYear).coerceIn(1, 120)
    }
    val numbersOnly = dobStr.filter { it.isDigit() }
    if (numbersOnly.length >= 4) {
        val last4 = numbersOnly.takeLast(4).toIntOrNull()
        if (last4 != null && last4 in 1900..2024) {
            return (currentYear - last4).coerceIn(1, 120)
        }
    }
    return 28
}

@Composable
fun PersonalInformationScreen(
    modifier: Modifier = Modifier,
    currentName: String = "Alex Morgan",
    currentInitials: String = "AM",
    currentHeight: String = "175",
    currentWeight: String = "68.5",
    currentPhotoBitmap: ImageBitmap? = null,
    onBackClick: () -> Unit = {},
    onSaveChangesClick: (newName: String, newInitials: String, newAge: String, newHeight: String, newWeight: String, newPhotoBitmap: ImageBitmap?) -> Unit = { _, _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(currentName) }
    var initials by remember { mutableStateOf(currentInitials) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var profileImageBitmap by remember { mutableStateOf<ImageBitmap?>(currentPhotoBitmap) }

    var dob by remember { mutableStateOf("14 May 1996") }
    var gender by remember { mutableStateOf("Male") }
    var email by remember { mutableStateOf("alex.morgan@example.com") }
    var phone by remember { mutableStateOf("+1 (202) 555-0147") }
    var height by remember { mutableStateOf(currentHeight) }
    var weight by remember { mutableStateOf(currentWeight) }

    val calculatedAge = remember(dob) { calculateAgeFromDob(dob) }
    val hCm = height.toFloatOrNull() ?: 175f
    val wKg = weight.toFloatOrNull() ?: 68.5f
    val calculatedBmi = if (hCm > 0) wKg / ((hCm / 100f) * (hCm / 100f)) else 22.4f

    // Android Gallery Photo Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                profileImageBitmap = bitmap?.asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
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
                        text = "Personal Information",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                IconButton(onClick = { }, modifier = Modifier.size(36.dp)) {
                    PencilEditIcon(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onBackground)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Profile Photo & Gallery Chooser Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Photo Circle
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary)
                            .clickable { galleryLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImageBitmap != null) {
                            Image(
                                bitmap = profileImageBitmap!!,
                                contentDescription = "User Profile Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = initials,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Choose & Remove Photo Action Buttons Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondary)
                                .clickable { galleryLauncher.launch("image/*") }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Choose Photo 📷",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (profileImageBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFEE2E2))
                                    .clickable {
                                        profileImageBitmap = null
                                        selectedImageUri = null
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Remove Photo 🗑️",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = "Or pick preset avatar:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Choosable Avatar Preset Badges
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("AM", "JS", "JD", "SK", "AR").forEach { presetInitials ->
                            val isSelected = initials == presetInitials && profileImageBitmap == null
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) VitalTealDark else MaterialTheme.colorScheme.outlineVariant)
                                    .clickable {
                                        initials = presetInitials
                                        profileImageBitmap = null
                                        selectedImageUri = null
                                    }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = presetInitials,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Editable Input Fields Form
            Column(modifier = Modifier.fillMaxWidth()) {
                // Full Name
                Text(text = "Full name", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { newName ->
                        name = newName
                        val parts = newName.trim().split(" ")
                        if (parts.size >= 2) {
                            initials = (parts[0].take(1) + parts[1].take(1)).uppercase()
                        } else if (parts.isNotEmpty() && parts[0].isNotEmpty()) {
                            initials = parts[0].take(2).uppercase()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = VitalTealPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    trailingIcon = { PencilEditIcon(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date of Birth
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Date of birth", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondary)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⚡ Calculated Age: $calculatedAge yrs",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g. 14 May 1996 or 1996", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = VitalTealPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    trailingIcon = { CalendarIcon(modifier = Modifier.size(16.dp)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Gender
                Text(text = "Gender", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = VitalTealPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email Address
                Text(text = "Email address", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = VitalTealPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    trailingIcon = { PencilEditIcon(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Phone Number
                Text(text = "Phone number", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = VitalTealPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    trailingIcon = { PencilEditIcon(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Height & Weight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Body Dimensions", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondary)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⚡ Calculated BMI: ${String.format(Locale.US, "%.1f", calculatedBmi)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Height (cm)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                focusedBorderColor = VitalTealPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Weight (kg)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                focusedBorderColor = VitalTealPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Save Changes Button
        Button(
            onClick = { onSaveChangesClick(name, initials, "$calculatedAge", height, weight, profileImageBitmap) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VitalTealDark,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Save Changes",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
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
private fun CalendarIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val calPath = Path().apply {
            moveTo(0f, h * 0.25f)
            lineTo(w, h * 0.25f)
            lineTo(w, h * 0.85f)
            lineTo(0f, h * 0.85f)
            close()
        }

        drawPath(path = calPath, color = Color(0xFF6B7280), style = Stroke(width = 1.6.dp.toPx()))
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
fun PersonalInformationScreenPreview() {
    VitalsyncTheme {
        PersonalInformationScreen()
    }
}
