package com.arnabcloud.chronos.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoveUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.arnabcloud.chronos.model.TimelineItem
import com.arnabcloud.chronos.viewmodel.ChronosViewModel
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

const val HourSlotHeight = 100.0

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChronosTimelineScreen(viewModel: ChronosViewModel) {
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var multiSelectMode by rememberSaveable { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<UUID>() }
    var showMoveDialog by remember { mutableStateOf(false) }

    val onClearSelection = {
        multiSelectMode = false
        selectedItems.clear()
    }

    if (showMoveDialog) {
        MoveItemsDialog(
            onDismiss = { showMoveDialog = false },
            onConfirm = { newDate ->
                viewModel.moveItems(selectedItems.toList(), newDate)
                showMoveDialog = false
                onClearSelection()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DayPicker(
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it },
            modifier = Modifier.fillMaxWidth()
        )
        Timeline(
            modifier = Modifier.weight(1f),
            selectedDate = selectedDate,
            items = viewModel.getItemsForDate(selectedDate),
            selectedItems = selectedItems,
            onItemClick = { item ->
                if (multiSelectMode) {
                    if (item.id in selectedItems) selectedItems.remove(item.id) else selectedItems.add(
                        item.id
                    )
                }
            },
            onItemLongClick = { item ->
                if (!multiSelectMode) {
                    multiSelectMode = true
                    selectedItems.add(item.id)
                }
            },
            onAddItem = { viewModel.addItem(it) },
            onRemoveItem = { viewModel.removeItem(it) },
            onSetCompleted = { item, _ -> viewModel.toggleComplete(item) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextualTopAppBar(
    selectionCount: Int,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit
) {
    TopAppBar(
        title = { Text("$selectionCount selected") },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close selection")
            }
        },
        actions = {
            IconButton(onClick = onMove) {
                Icon(Icons.Default.MoveUp, contentDescription = "Move items")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete items")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveItemsDialog(onDismiss: () -> Unit, onConfirm: (LocalDate) -> Unit) {
    val datePickerState =
        rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    onConfirm(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate())
                }
            }) { Text("Move") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun DayPicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = (0..14).map { LocalDate.now().plusDays(it.toLong()) }
    Column(modifier = modifier) {
        Text(
            text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(days) { day ->
                val isSelected = day == selectedDate
                Column(
                    modifier = Modifier
                        .width(55.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.3f
                            )
                        )
                        .clickable { onDateSelected(day) }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day.dayOfWeek.name.take(3),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = day.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Timeline(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    items: List<TimelineItem>,
    selectedItems: List<UUID>,
    onItemClick: (TimelineItem) -> Unit,
    onItemLongClick: (TimelineItem) -> Unit,
    onAddItem: (TimelineItem) -> Unit,
    onRemoveItem: (TimelineItem) -> Unit,
    onSetCompleted: (TimelineItem, Boolean) -> Unit
) {
    val hours = (0..23).toList()
    val scrollState = rememberScrollState()

    // Scroll to current hour only if today is selected
    LaunchedEffect(selectedDate) {
        if (selectedDate == LocalDate.now()) {
            val currentHour = LocalTime.now().hour
            scrollState.scrollTo((currentHour * HourSlotHeight * 2.5).toInt())
        } else {
            scrollState.scrollTo(0)
        }
    }

    Box(modifier = modifier.verticalScroll(scrollState)) {
        Column(modifier = Modifier.padding(16.dp)) {
            hours.forEach { hour ->
                HourSlot(
                    hour = hour,
                    itemsInHour = items.filter {
                        when (it) {
                            is TimelineItem.Event -> it.startTime.hour == hour
                            is TimelineItem.Task -> it.date.atStartOfDay().hour == hour
                        }
                    },
                    selectedItems = selectedItems,
                    onItemClick = onItemClick,
                    onItemLongClick = onItemLongClick,
                    onRemoveItem = onRemoveItem,
                    onSetCompleted = onSetCompleted
                )
            }
        }
        if (selectedDate == LocalDate.now()) {
            NowLine()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HourSlot(
    hour: Int,
    itemsInHour: List<TimelineItem>,
    selectedItems: List<UUID>,
    onItemClick: (TimelineItem) -> Unit,
    onItemLongClick: (TimelineItem) -> Unit,
    onRemoveItem: (TimelineItem) -> Unit,
    onSetCompleted: (TimelineItem, Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .heightIn(min = HourSlotHeight.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = String.format(Locale.getDefault(), "%02d:00", hour),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .width(48.dp)
                .padding(top = 8.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )

            Column(modifier = Modifier.padding(top = 24.dp, start = 8.dp, end = 8.dp)) {
                itemsInHour.forEach { item ->
                    val isSelected = item.id in selectedItems
                    SwipeableTaskCard(
                        item = item,
                        isSelected = isSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .combinedClickable(
                                onClick = { onItemClick(item) },
                                onLongClick = { onItemLongClick(item) }
                            ),
                        onRemove = { onRemoveItem(item) },
                        onSetCompleted = { completed -> onSetCompleted(item, completed) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTaskCard(
    item: TimelineItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onRemove: () -> Unit,
    onSetCompleted: (Boolean) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(positionalThreshold = { it * .25f })

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onRemove()
        } else if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
            if (item is TimelineItem.Task) {
                onSetCompleted(!item.isCompleted)
            }
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromEndToStart = true,
        enableDismissFromStartToEnd = item is TimelineItem.Task,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                else -> Color.Transparent
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.CenterStart
            }
            val icon = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                else -> Icons.Default.Check
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .background(color, shape = MaterialTheme.shapes.large)
                    .padding(horizontal = 16.dp),
                contentAlignment = alignment
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        content = {
            when (item) {
                is TimelineItem.Task -> TaskCard(
                    item,
                    isSelected
                ) { onSetCompleted(!item.isCompleted) }

                is TimelineItem.Event -> EventCard(item, isSelected)
            }
        }
    )
}

@Composable
fun EventCard(item: TimelineItem.Event, isSelected: Boolean, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.border(
            border = if (isSelected) BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            ) else BorderStroke(width = 0.dp, color = Color.Black)
        ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Badge(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) {
                    Text("Event", style = MaterialTheme.typography.labelSmall)
                }
            }
            if (item.details.isNotBlank()) {
                Text(
                    text = item.details,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            val timeText = if (item.isAllDay) "All Day" else "${
                item.startTime.format(
                    DateTimeFormatter.ofPattern("hh:mm a")
                )
            } - ${item.endTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}"
            Text(
                text = timeText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                    alpha = 0.8f
                )
            )
        }
    }
}

@Composable
fun TaskCard(
    item: TimelineItem.Task,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onToggleComplete: () -> Unit
) {
    val isMissed = item.isMissed()
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else if (isMissed) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.error)
        } else {
            null
        },
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.3f
            ) else if (isMissed) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (item.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleComplete) {
                Icon(
                    imageVector = if (item.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = "Complete",
                    tint = if (item.isCompleted) MaterialTheme.colorScheme.primary else if (isMissed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isMissed && !item.isCompleted) FontWeight.Bold else FontWeight.Normal,
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null,
                    modifier = Modifier.padding(start = 8.dp)
                )
                if (item.details.isNotBlank()) {
                    Text(
                        text = item.details,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                if (isMissed && !item.isCompleted) {
                    Text(
                        text = "Missed Deadline",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NowLine() {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1000)
        }
    }

    val minutesFromMidnight = currentTime.hour * 60 + currentTime.minute
    val yOffset = minutesFromMidnight * (HourSlotHeight / 60.0) + HourSlotHeight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = yOffset.dp)
            .padding(start = 40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(MaterialTheme.colorScheme.error, CircleShape)
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.error, thickness = 2.dp)
    }
}
