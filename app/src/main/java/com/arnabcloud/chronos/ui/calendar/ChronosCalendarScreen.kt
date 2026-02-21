package com.arnabcloud.chronos.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arnabcloud.chronos.ui.home.TimelineItem
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun ChronosCalendarScreen() {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    // Ensure currentMonth updates if selectedDate moves to a different month
    val currentMonth = remember(selectedDate) { YearMonth.from(selectedDate) }
    
    val busyDays = remember { 
        mapOf(
            LocalDate.now().plusDays(1) to 3,
            LocalDate.now().plusDays(3) to 1,
            LocalDate.now().minusDays(2) to 5
        )
    }

    val agendaItems = remember(selectedDate) {
        listOf(
            TimelineItem(title = "Morning Standup", startTime = LocalTime.of(9, 30), isTask = false),
            TimelineItem(title = "Review Project Specs", startTime = LocalTime.of(11, 0)),
            TimelineItem(title = "Lunch with Team", startTime = LocalTime.of(13, 0), isTask = false),
            TimelineItem(title = "Send Invoices", startTime = LocalTime.of(16, 0))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        MonthGrid(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it },
            busyDays = busyDays
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Agenda",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        AgendaList(items = agendaItems)
    }
}

@Composable
fun MonthGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    busyDays: Map<LocalDate, Int>
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstOfMonth = currentMonth.atDay(1)
    
    // ISO-8601: Monday is 1, Sunday is 7.
    // For a grid starting on Monday, we need (dayOfWeek - 1) empty slots.
    val dayOfWeekOffset = firstOfMonth.dayOfWeek.value 
    val leadingEmptyDays = List(dayOfWeekOffset - 1) { null }
    
    val days = (1..daysInMonth).map { currentMonth.atDay(it) }
    val gridItems = leadingEmptyDays + days

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                Text(
                    text = it,
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(260.dp),
            userScrollEnabled = false
        ) {
            items(gridItems) { date ->
                if (date != null) {
                    val isSelected = date == selectedDate
                    val isToday = date == LocalDate.now()
                    val busyLevel = busyDays[date] ?: 0

                    Column(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    else -> Color.Transparent
                                }
                            )
                            .clickable { onDateSelected(date) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        
                        Row(
                            modifier = Modifier.height(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            repeat(minOf(busyLevel, 3)) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                            else MaterialTheme.colorScheme.tertiary,
                                            CircleShape
                                        )
                                )
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.aspectRatio(1f))
                }
            }
        }
    }
}

@Composable
fun AgendaList(items: List<TimelineItem>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items) { item ->
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.startTime.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    if (item.isTask) {
                        Checkbox(checked = item.isCompleted, onCheckedChange = {})
                    } else {
                         Badge(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Text("Event", modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                }
            }
        }
    }
}
