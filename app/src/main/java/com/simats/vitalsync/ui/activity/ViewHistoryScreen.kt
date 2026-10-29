package com.simats.vitalsync.ui.activity

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.vitalsync.ui.activity.viewmodel.ActivityViewModel
import com.simats.vitalsync.ui.activity.viewmodel.HistoryUiState
import com.simats.vitalsync.ui.theme.VitalTealDark
import com.simats.vitalsync.ui.theme.VitalTealPrimary
import com.simats.vitalsync.ui.theme.VitalsyncTheme

data class HistoryItem(
    val id: String,
    val category: String,
    val dateGroup: String, // "Today · Oct 15", "Yesterday · Oct 14"
    val dayTag: String, // "Oct 15", "Oct 14"
    val icon: String,
    val title: String,
    val value: String,
    val unit: String,
    val status: String,
    val time: String
)

private val sampleHistoryData = listOf(
    // Oct 15
    HistoryItem("1", "Heart Rate", "Today · Oct 15", "Oct 15", "💗", "Heart Rate", "78", "bpm", "Normal", "9:30 AM · Health Connect"),
    HistoryItem("2", "Heart Rate", "Today · Oct 15", "Oct 15", "💗", "Heart Rate", "82", "bpm", "Normal", "7:15 AM · VitalSync Watch"),
    HistoryItem("3", "Blood Pressure", "Today · Oct 15", "Oct 15", "🩺", "Blood Pressure", "120/80", "mmHg", "Normal", "9:30 AM · Saved"),
    HistoryItem("4", "SpO₂", "Today · Oct 15", "Oct 15", "🫀", "SpO₂", "98", "%", "Optimal", "9:30 AM · Saved"),
    // Oct 14
    HistoryItem("5", "Heart Rate", "Yesterday · Oct 14", "Oct 14", "💗", "Heart Rate", "74", "bpm", "Normal", "10:00 PM · VitalSync Watch"),
    HistoryItem("6", "Blood Pressure", "Yesterday · Oct 14", "Oct 14", "🩺", "Blood Pressure", "118/78", "mmHg", "Normal", "8:30 AM · Saved"),
    HistoryItem("7", "SpO₂", "Yesterday · Oct 14", "Oct 14", "🫀", "SpO₂", "99", "%", "Optimal", "8:30 AM · Saved"),
    HistoryItem("8", "Temperature", "Yesterday · Oct 14", "Oct 14", "🌡️", "Temperature", "36.7", "°C", "Normal", "8:15 PM · Saved"),
    HistoryItem("9", "Weight", "Yesterday · Oct 14", "Oct 14", "⚖️", "Weight", "68.5", "kg", "Normal", "8:00 AM · Saved"),
    // Oct 13
    HistoryItem("10", "Heart Rate", "Oct 13, 2025", "Oct 13", "💗", "Heart Rate", "76", "bpm", "Normal", "6:00 PM · VitalSync Watch")
)

@Composable
fun ViewHistoryScreen(
    modifier: Modifier = Modifier,
    initialFilter: String = "All",
    viewModel: ActivityViewModel? = null,
    userEmail: String = "alex.morgan@email.com",
    onBackClick: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf(initialFilter) }
    var selectedDateRange by remember { mutableStateOf("Oct 14–15, 2025") }
    var isDatePickerExpanded by remember { mutableStateOf(false) }
    var selectedDayTag by remember { mutableStateOf("All Days") }

    // Load History from ViewModel
    LaunchedEffect(userEmail) {
        viewModel?.loadHistory(userEmail)
    }

    val historyState by (viewModel?.historyState?.collectAsState() ?: remember { mutableStateOf(HistoryUiState()) })

    val activeHistoryList = remember(historyState.history) {
        if (historyState.history.isNotEmpty()) {
            historyState.history.map { item ->
                HistoryItem(
                    id = item.id,
                    category = item.category,
                    dateGroup = item.dateGroup,
                    dayTag = item.dayTag,
                    icon = item.icon,
                    title = item.title,
                    value = item.value,
                    unit = item.unit,
                    status = item.status,
                    time = item.time
                )
            }
        } else {
            sampleHistoryData
        }
    }

    val filteredItems = remember(selectedFilter, selectedDayTag, activeHistoryList) {
        activeHistoryList.filter { item ->
            val matchesFilter = selectedFilter == "All" || item.category.equals(selectedFilter, ignoreCase = true)
            val matchesDay = selectedDayTag == "All Days" || item.dayTag == selectedDayTag
            matchesFilter && matchesDay
        }
    }

    val groupedItems = remember(filteredItems) {
        filteredItems.groupBy { it.dateGroup }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFCFB))
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
                        color = Color(0xFF111827)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "View History",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9))
                    .clickable { isDatePickerExpanded = !isDatePickerExpanded },
                contentAlignment = Alignment.Center
            ) {
                if (historyState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = VitalTealDark,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "📅", fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Interactive Date Banner Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isDatePickerExpanded = !isDatePickerExpanded },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🗓️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = selectedDateRange, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Text(text = if (selectedDayTag == "All Days") "All dates in range" else "Filtered: $selectedDayTag", fontSize = 11.sp, color = Color(0xFF0F9F7A))
                    }
                }

                ChevronDownIcon(modifier = Modifier.size(16.dp))
            }
        }

        // Expandable Date & Timeframe Chooser Modal Card
        if (isDatePickerExpanded) {
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F6F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "SELECT TIMEFRAME & DAY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F9F7A), letterSpacing = 0.8.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    // Date Range Preset Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Oct 14–15, 2025", "Past 7 Days", "October 2025").forEach { range ->
                            val isRangeSelected = selectedDateRange == range
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isRangeSelected) VitalTealPrimary else Color.White)
                                    .clickable {
                                        selectedDateRange = range
                                        selectedDayTag = "All Days"
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = range,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRangeSelected) Color.White else Color(0xFF374151)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = "Filter by specific day:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))

                    Spacer(modifier = Modifier.height(8.dp))

                    // Specific Day Selector Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All Days", "Oct 15", "Oct 14", "Oct 13").forEach { day ->
                            val isDaySelected = selectedDayTag == day
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isDaySelected) VitalTealDark else Color.White)
                                    .border(1.dp, if (isDaySelected) Color.Transparent else Color(0xFFD1D5DB), RoundedCornerShape(20.dp))
                                    .clickable { selectedDayTag = day }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = day,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDaySelected) Color.White else Color(0xFF4B5563)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Health Category Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Heart Rate", "Blood Pressure", "SpO₂").forEach { filter ->
                val isSelected = selectedFilter.equals(filter, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) VitalTealDark else Color(0xFFF1F5F9))
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = filter,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color(0xFF475569)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Summary Badge Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F6F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${filteredItems.size} readings shown",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "${selectedDateRange} · ${if (selectedFilter == "All") "All Categories" else selectedFilter}",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = selectedDayTag,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F9F7A)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Render Dynamic Grouped History Cards
        if (groupedItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No readings found for $selectedFilter on $selectedDayTag",
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        } else {
            groupedItems.forEach { (dateHeader, items) ->
                Text(
                    text = dateHeader,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(10.dp))

                items.forEach { item ->
                    HistoryCard(
                        icon = item.icon,
                        title = item.title,
                        value = item.value,
                        unit = item.unit,
                        status = item.status,
                        time = item.time
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun HistoryCard(
    icon: String,
    title: String,
    value: String,
    unit: String,
    status: String,
    time: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 20.sp)

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = time,
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                val badgeColor = when (status) {
                    "Optimal" -> Color(0xFF0F9F7A)
                    "Normal" -> Color(0xFF10B981)
                    else -> Color(0xFF6B7280)
                }

                Text(
                    text = status,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = badgeColor
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

        drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun ChevronDownIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF6B7280)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.25f, h * 0.35f)
            lineTo(w * 0.5f, h * 0.65f)
            lineTo(w * 0.75f, h * 0.35f)
        }

        drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun ViewHistoryScreenPreview() {
    VitalsyncTheme {
        ViewHistoryScreen()
    }
}
