package com.simats.vitalsync.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalTealPrimary
import com.simats.vitalsync.ui.theme.VitalsyncTheme
import java.util.Locale

fun calculateAgeFromDob(dobStr: String): Int {
    if (dobStr.isBlank()) return 0
    val currentYear = 2025
    val yearRegex = Regex("""\b(19\d\d|20\d\d)\b""")
    val match = yearRegex.find(dobStr)
    if (match != null) {
        val birthYear = match.value.toIntOrNull() ?: return 0
        return (currentYear - birthYear).coerceIn(1, 120)
    }
    val numbersOnly = dobStr.filter { it.isDigit() }
    if (numbersOnly.length >= 4) {
        val last4 = numbersOnly.takeLast(4).toIntOrNull()
        if (last4 != null && last4 in 1900..2024) {
            return (currentYear - last4).coerceIn(1, 120)
        }
    }
    return 0
}

@Composable
fun PersonalInformationScreen(
    modifier: Modifier = Modifier,
    currentName: String = "",
    currentInitials: String = "--",
    currentHeight: String = "",
    currentWeight: String = "",
    currentDob: String = "",
    currentGender: String = "",
    currentEmail: String = "",
    currentPhone: String = "",
    currentPhotoBitmap: ImageBitmap? = null,
    onBackClick: () -> Unit = {},
    onSaveAvatarFile: (avatarFile: File?, cleared: Boolean) -> Unit = { _, _ -> },
    onSaveChangesClick: (newName: String, newInitials: String, newAge: String, newHeight: String, newWeight: String, newPhotoBitmap: ImageBitmap?, newDob: String, newGender: String, newPhone: String) -> Unit = { _, _, _, _, _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(currentName) }
    var initials by remember { mutableStateOf(currentInitials) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var photoCleared by remember { mutableStateOf(false) }
    var profileImageBitmap by remember { mutableStateOf<ImageBitmap?>(currentPhotoBitmap) }

    LaunchedEffect(currentPhotoBitmap) {
        if (!photoCleared && selectedImageUri == null && currentPhotoBitmap != null) {
            profileImageBitmap = currentPhotoBitmap
        }
    }

    var dob by remember { mutableStateOf(currentDob) }
    var gender by remember { mutableStateOf(currentGender) }
    var email by remember { mutableStateOf(currentEmail) }
    var phone by remember { mutableStateOf(currentPhone) }
    var height by remember { mutableStateOf(currentHeight) }
    var weight by remember { mutableStateOf(currentWeight) }

    val calculatedAge = remember(dob) { calculateAgeFromDob(dob) }
    val hCm = height.toFloatOrNull() ?: 0f
    val wKg = weight.toFloatOrNull() ?: 0f
    val calculatedBmi = if (hCm > 50f && wKg > 20f) wKg / ((hCm / 100f) * (hCm / 100f)) else 0f

    var rawBitmapToCrop by remember { mutableStateOf<Bitmap?>(null) }
    var showCropDialog by remember { mutableStateOf(false) }
    var activeCroppedAvatarFile by remember { mutableStateOf<File?>(null) }

    // Android Gallery Photo Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        rawBitmapToCrop = bitmap
                        showCropDialog = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (showCropDialog && rawBitmapToCrop != null) {
        ImageCropDialog(
            sourceBitmap = rawBitmapToCrop!!,
            onDismiss = {
                showCropDialog = false
                rawBitmapToCrop = null
            },
            onCropComplete = { cropped ->
                try {
                    val maxDim = 1024
                    val scaled = if (cropped.width > maxDim || cropped.height > maxDim) {
                        val ratio = minOf(maxDim.toFloat() / cropped.width, maxDim.toFloat() / cropped.height)
                        Bitmap.createScaledBitmap(
                            cropped,
                            (cropped.width * ratio).toInt().coerceAtLeast(1),
                            (cropped.height * ratio).toInt().coerceAtLeast(1),
                            true
                        )
                    } else {
                        cropped
                    }
                    val outFile = File(context.cacheDir, "avatar_upload_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(outFile).use { out ->
                        scaled.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    activeCroppedAvatarFile = outFile
                    profileImageBitmap = scaled.asImageBitmap()
                    selectedImageUri = Uri.fromFile(outFile)
                    photoCleared = false
                } catch (e: Exception) {
                    e.printStackTrace()
                    profileImageBitmap = cropped.asImageBitmap()
                }
                showCropDialog = false
                rawBitmapToCrop = null
            }
        )
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
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
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
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
                                    .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                    .clickable {
                                        profileImageBitmap = null
                                        selectedImageUri = null
                                        activeCroppedAvatarFile = null
                                        photoCleared = true
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Remove Photo 🗑️",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
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
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        initials = presetInitials
                                        profileImageBitmap = null
                                        selectedImageUri = null
                                        activeCroppedAvatarFile = null
                                        photoCleared = true
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
                    placeholder = { Text("e.g. John Doe", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        cursorColor = MaterialTheme.colorScheme.primary
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (calculatedAge > 0) "⚡ Calculated Age: $calculatedAge yrs" else "⚡ Age: Not set",
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
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        cursorColor = MaterialTheme.colorScheme.primary
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
                    placeholder = { Text("e.g. Male / Female", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        cursorColor = MaterialTheme.colorScheme.primary
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
                    placeholder = { Text("e.g. alex@example.com", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        cursorColor = MaterialTheme.colorScheme.primary
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
                    placeholder = { Text("e.g. +1 234 567 8900", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        cursorColor = MaterialTheme.colorScheme.primary
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (calculatedBmi > 0f) "⚡ Calculated BMI: ${String.format(Locale.US, "%.1f", calculatedBmi)}" else "⚡ BMI: Not set",
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
                            placeholder = { Text("e.g. 175", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                cursorColor = MaterialTheme.colorScheme.primary
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
                            placeholder = { Text("e.g. 68.5", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Save Changes Button
        Button(
            onClick = {
                val avatarFile = activeCroppedAvatarFile ?: selectedImageUri?.let { uri ->
                    try {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            val decoded = BitmapFactory.decodeStream(stream)
                            if (decoded != null) {
                                val maxDim = 1024
                                val scaled = if (decoded.width > maxDim || decoded.height > maxDim) {
                                    val ratio = minOf(maxDim.toFloat() / decoded.width, maxDim.toFloat() / decoded.height)
                                    Bitmap.createScaledBitmap(
                                        decoded,
                                        (decoded.width * ratio).toInt().coerceAtLeast(1),
                                        (decoded.height * ratio).toInt().coerceAtLeast(1),
                                        true
                                    )
                                } else {
                                    decoded
                                }
                                val outFile = File(context.cacheDir, "avatar_upload_${System.currentTimeMillis()}.jpg")
                                FileOutputStream(outFile).use { out ->
                                    scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
                                }
                                outFile
                            } else null
                        }
                    } catch (_: Exception) {
                        null
                    }
                }
                onSaveAvatarFile(avatarFile, photoCleared && avatarFile == null)
                onSaveChangesClick(name, initials, "$calculatedAge", height, weight, profileImageBitmap, dob, gender, phone)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
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
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
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
    val iconColor = MaterialTheme.colorScheme.onSurfaceVariant
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

        drawPath(path = calPath, color = iconColor, style = Stroke(width = 1.6.dp.toPx()))
    }
}

@Composable
private fun BackArrowIcon(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground
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

@Composable
fun ImageCropDialog(
    sourceBitmap: Bitmap,
    onDismiss: () -> Unit,
    onCropComplete: (Bitmap) -> Unit
) {
    var rotationDegrees by remember { mutableFloatStateOf(0f) }
    var flipHorizontal by remember { mutableStateOf(false) }
    var flipVertical by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0F0F)),
            color = Color(0xFF0F0F0F)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Title & Hint
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Crop & Adjust Photo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF1F1F1)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rotate, flip, and align inside the square viewfinder",
                        fontSize = 12.sp,
                        color = Color(0xFFAAAAAA)
                    )
                }

                // Square Viewfinder Box with Grid Overlay
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1E1E)),
                    contentAlignment = Alignment.Center
                ) {
                    // Preview Bitmap with Live Transformations
                    Image(
                        bitmap = sourceBitmap.asImageBitmap(),
                        contentDescription = "Crop Preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationZ = rotationDegrees
                                scaleX = if (flipHorizontal) -1f else 1f
                                scaleY = if (flipVertical) -1f else 1f
                            },
                        contentScale = ContentScale.Crop
                    )

                    // Crop Grid Overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Outer Frame Border
                        drawRect(
                            color = Color(0xFF10A37F),
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // 3x3 Grid Lines (Rule of Thirds)
                        val gridColor = Color.White.copy(alpha = 0.35f)
                        val strokeW = 1.dp.toPx()

                        // Vertical lines
                        drawLine(color = gridColor, start = Offset(w / 3f, 0f), end = Offset(w / 3f, h), strokeWidth = strokeW)
                        drawLine(color = gridColor, start = Offset(w * 2f / 3f, 0f), end = Offset(w * 2f / 3f, h), strokeWidth = strokeW)

                        // Horizontal lines
                        drawLine(color = gridColor, start = Offset(0f, h / 3f), end = Offset(w, h / 3f), strokeWidth = strokeW)
                        drawLine(color = gridColor, start = Offset(0f, h * 2f / 3f), end = Offset(w, h * 2f / 3f), strokeWidth = strokeW)

                        // Corner Viewfinder Brackets
                        val cornerLen = 20.dp.toPx()
                        val cornerStroke = 3.dp.toPx()
                        val cornerColor = Color(0xFF10A37F)

                        // Top-Left
                        drawLine(cornerColor, Offset(0f, 0f), Offset(cornerLen, 0f), cornerStroke)
                        drawLine(cornerColor, Offset(0f, 0f), Offset(0f, cornerLen), cornerStroke)

                        // Top-Right
                        drawLine(cornerColor, Offset(w, 0f), Offset(w - cornerLen, 0f), cornerStroke)
                        drawLine(cornerColor, Offset(w, 0f), Offset(w, cornerLen), cornerStroke)

                        // Bottom-Left
                        drawLine(cornerColor, Offset(0f, h), Offset(cornerLen, h), cornerStroke)
                        drawLine(cornerColor, Offset(0f, h), Offset(0f, h - cornerLen), cornerStroke)

                        // Bottom-Right
                        drawLine(cornerColor, Offset(w, h), Offset(w - cornerLen, h), cornerStroke)
                        drawLine(cornerColor, Offset(w, h), Offset(w, cornerLen), cornerStroke)
                    }
                }

                // Adjustment Controls (Rotate, Flip, Reset)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CropToolButton(icon = "↺", label = "-90°") {
                            rotationDegrees = (rotationDegrees - 90f) % 360f
                        }
                        CropToolButton(icon = "↻", label = "+90°") {
                            rotationDegrees = (rotationDegrees + 90f) % 360f
                        }
                        CropToolButton(icon = "⇄", label = "Flip H") {
                            flipHorizontal = !flipHorizontal
                        }
                        CropToolButton(icon = "⇅", label = "Flip V") {
                            flipVertical = !flipVertical
                        }
                        CropToolButton(icon = "⟲", label = "Reset") {
                            rotationDegrees = 0f
                            flipHorizontal = false
                            flipVertical = false
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Cancel & Done Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFF333333)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFF1F1F1)
                            )
                        ) {
                            Text(text = "Cancel", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }

                        Button(
                            onClick = {
                                try {
                                    val matrix = Matrix().apply {
                                        postRotate(rotationDegrees)
                                        postScale(
                                            if (flipHorizontal) -1f else 1f,
                                            if (flipVertical) -1f else 1f
                                        )
                                    }
                                    val transformed = Bitmap.createBitmap(
                                        sourceBitmap,
                                        0,
                                        0,
                                        sourceBitmap.width,
                                        sourceBitmap.height,
                                        matrix,
                                        true
                                    )
                                    val minDim = minOf(transformed.width, transformed.height)
                                    val x = (transformed.width - minDim) / 2
                                    val y = (transformed.height - minDim) / 2
                                    val cropped = Bitmap.createBitmap(transformed, x, y, minDim, minDim)
                                    onCropComplete(cropped)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    onCropComplete(sourceBitmap)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10A37F),
                                contentColor = Color.White
                            )
                        ) {
                            Text(text = "Done", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CropToolButton(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Color(0xFF2A2A2A), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 20.sp, color = Color(0xFFF1F1F1))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, color = Color(0xFFAAAAAA))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun PersonalInformationScreenPreview() {
    VitalsyncTheme {
        PersonalInformationScreen()
    }
}
