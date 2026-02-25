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
import com.arnabcloud.chronos.model.TimelineItem
import com.arnabcloud.chronos.viewmodel.ChronosViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun ChronosCalendarScreen(viewModel: ChronosViewModel) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val currentMonth = remember(selectedDate) { YearMonth.from(selectedDate) }
    
    // Calculate busy days from ViewModel
    val busyDays = remember(viewModel.items) {
        viewModel.items.groupBy { it.date }.mapValues { it.value.size }
    }

    val agendaItems = viewModel.getItemsForDate(selectedDate)

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
            text = "Agenda for ${selectedDate.dayOfMonth} ${selectedDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        AgendaList(items = agendaItems, onToggle = { viewModel.toggleComplete(it) })
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
            modifier = Modifier.height(300.dp),
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
//                            .padding(vertical = 1.dp)
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
fun AgendaList(items: List<TimelineItem>, onToggle: (TimelineItem) -> Unit) {
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
                        if (item.details.isNotBlank()) {
                            Text(text = item.details, style = MaterialTheme.typography.bodySmall)
                        }
                        val timeStr = when (item) {
                            is TimelineItem.Event -> if (item.isAllDay) "All Day" else item.startTime.toString()
                            is TimelineItem.Task -> "Due: ${item.deadlineDate ?: "No deadline"}"
                        }
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    if (item is TimelineItem.Task) {
                        Checkbox(checked = item.isCompleted, onCheckedChange = { onToggle(item) })
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
