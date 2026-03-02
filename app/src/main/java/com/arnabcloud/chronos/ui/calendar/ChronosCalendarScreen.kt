package com.arnabcloud.chronos.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.arnabcloud.chronos.model.TimelineItem
import com.arnabcloud.chronos.ui.theme.CompletedColor
import com.arnabcloud.chronos.ui.theme.EventColor
import com.arnabcloud.chronos.ui.theme.EventColorDark
import com.arnabcloud.chronos.ui.theme.EventColorLight
import com.arnabcloud.chronos.ui.theme.MissedColor
import com.arnabcloud.chronos.ui.theme.getPriorityColor
import com.arnabcloud.chronos.ui.theme.getPriorityContainerColor
import com.arnabcloud.chronos.viewmodel.ChronosViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ChronosCalendarScreen(viewModel: ChronosViewModel) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val currentMonth = remember(selectedDate) { YearMonth.from(selectedDate) }

    // Calculate busy days from ViewModel
    val busyDays = remember(viewModel.items) {
        viewModel.items.groupBy { it.date }.mapValues { entry ->
            entry.value.any { it is TimelineItem.Task && it.isMissed() } to entry.value.size
        }
    }

    val agendaItems = viewModel.getItemsForDate(selectedDate)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "${
                currentMonth.month.getDisplayName(
                    TextStyle.FULL,
                    Locale.getDefault()
                )
            } ${currentMonth.year}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        MonthGrid(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it },
            busyDays = busyDays
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Agenda • ${selectedDate.dayOfMonth} ${
                selectedDate.month.getDisplayName(
                    TextStyle.SHORT,
                    Locale.getDefault()
                )
            }",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AgendaList(
            items = agendaItems,
            onToggle = { viewModel.toggleComplete(it) },
            selectedDate = selectedDate
        )
    }
}

@Composable
fun MonthGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    busyDays: Map<LocalDate, Pair<Boolean, Int>>
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
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(300.dp),
            userScrollEnabled = false,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(gridItems) { date ->
                if (date != null) {
                    val isSelected = date == selectedDate
                    val isToday = date == LocalDate.now()
                    val (hasMissed, busyLevel) = busyDays[date] ?: (false to 0)

                    Column(
                        modifier = Modifier
                            .aspectRatio(1f)
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
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.height(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (busyLevel > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(if (hasMissed) 6.dp else 4.dp)
                                        .background(
                                            when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                hasMissed -> MissedColor
                                                else -> MaterialTheme.colorScheme.tertiary
                                            },
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
fun AgendaList(
    items: List<TimelineItem>,
    onToggle: (TimelineItem) -> Unit,
    selectedDate: LocalDate
) {
    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Nothing planned for today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { item ->
                when (item) {
                    is TimelineItem.Task -> AgendaTaskCard(item, onToggle, selectedDate)
                    is TimelineItem.Event -> AgendaEventCard(item)
                }
            }
        }
    }
}

@Composable
fun AgendaEventCard(item: TimelineItem.Event) {
    val isDark = isSystemInDarkTheme()
    val containerColor = if (isDark) EventColorDark else EventColorLight
    val contentColor = if (isDark) Color.White else Color(0xFF0D47A1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(EventColor))
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Badge(
                        containerColor = EventColor.copy(alpha = 0.2f),
                        contentColor = contentColor
                    ) {
                        Text(
                            "EVENT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
                val timeStr =
                    if (item.isAllDay) "All Day" else "${item.startTime} - ${item.endTime}"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        null,
                        modifier = Modifier.size(12.dp),
                        tint = contentColor.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AgendaTaskCard(
    item: TimelineItem.Task,
    onToggle: (TimelineItem) -> Unit,
    selectedDate: LocalDate
) {
    val isMissed = item.isMissed()
    val isDark = isSystemInDarkTheme()
    val priorityColor = getPriorityColor(item.priority)

    val containerColor = when {
        item.isCompleted -> if (isDark) Color(0xFF2C2C2C) else Color(0xFFF5F5F5)
        isMissed -> if (isDark) MissedColor.copy(alpha = 0.15f) else MissedColor.copy(alpha = 0.05f)
        else -> getPriorityContainerColor(item.priority, isDark)
    }

    val contentColor = when {
        item.isCompleted -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        isMissed -> if (isDark) Color(0xFFFFCDD2) else MissedColor
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(if (item.isCompleted) CompletedColor else if (isMissed) MissedColor else priorityColor)
            )

            IconButton(onClick = { onToggle(item) }, modifier = Modifier.padding(start = 4.dp)) {
                Icon(
                    imageVector = if (item.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = if (item.isCompleted) CompletedColor else if (isMissed) MissedColor else priorityColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 8.dp)
                .weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null,
                        modifier = Modifier.weight(1f)
                    )
                    if (isMissed && !item.isCompleted) {
                        Badge(containerColor = MissedColor, contentColor = Color.White) {
                            Text(
                                "MISSED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    val timeStr =
                        item.taskTime?.let { it.format(DateTimeFormatter.ofPattern("hh:mm a")) }
                            ?: "Anytime"
                    Icon(
                        Icons.Default.AccessTime,
                        null,
                        modifier = Modifier.size(12.dp),
                        tint = contentColor.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (item.deadlineDate != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            Icons.Default.Flag,
                            null,
                            modifier = Modifier.size(12.dp),
                            tint = if (isMissed) MissedColor else priorityColor.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Due: ${item.deadlineDate.format(DateTimeFormatter.ofPattern("MMM dd"))}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isMissed) MissedColor else contentColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
