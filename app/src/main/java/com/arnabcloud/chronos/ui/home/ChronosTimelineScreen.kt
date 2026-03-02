package com.arnabcloud.chronos.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
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
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
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
        if (multiSelectMode) {
            ContextualTopAppBar(
                selectionCount = selectedItems.size,
                onClose = onClearSelection,
                onDelete = {
                    selectedItems.toList().forEach { id ->
                        viewModel.items.find { it.id == id }?.let { viewModel.removeItem(it) }
                    }
                    onClearSelection()
                },
                onMove = { showMoveDialog = true }
            )
        }

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
    onRemoveItem: (TimelineItem) -> Unit,
    onSetCompleted: (TimelineItem, Boolean) -> Unit
) {
    val hours = (0..23).toList()
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    var expandedUntimedTasks by rememberSaveable { mutableStateOf(setOf<UUID>()) }

    // A task is unscheduled if it has no taskTime AND (no deadlineTime OR deadline is not today)
    val untimedTasks = items.filterIsInstance<TimelineItem.Task>().filter {
        it.taskTime == null && (it.deadlineDate != selectedDate || it.deadlineTime == null)
    }

    // Timed items include events and tasks that have either a scheduled time OR a deadline time today
    val timedItems = items.filter {
        it is TimelineItem.Event || (it is TimelineItem.Task && (it.taskTime != null || (it.deadlineDate == selectedDate && it.deadlineTime != null)))
    }

    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(60000)
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val viewportHeightPx = with(density) { maxHeight.toPx() }
//        val hourSlotHeightPx = with(density) { HourSlotHeight.dp.toPx() }
        // Scroll to current hour only if today is selected
        LaunchedEffect(selectedDate) {
            if (selectedDate == LocalDate.now()) {
                val now = LocalTime.now()
                val currentHour = now.hour
                val fraction = now.minute / 60f
                val untimedHeaderCount = if (untimedTasks.isNotEmpty()) 1 else 0
                val index = currentHour + untimedHeaderCount

                // Position of red line within the slot (matching HourSlot logic)
                val redLineOffsetInSlotPx =
                    with(density) { (16.dp + (HourSlotHeight.dp - 16.dp) * fraction).toPx() }
                // Calculate scroll offset to put red line at viewport center
                val centerOffsetPx = (viewportHeightPx / 2) - redLineOffsetInSlotPx
                listState.scrollToItem(index, -centerOffsetPx.toInt())
            } else {
                listState.scrollToItem(0)
            }
        }

        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize()
        ) {
            if (untimedTasks.isNotEmpty()) {
                stickyHeader {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.background,
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                            untimedTasks.forEach { task ->
                                UntimedTaskBookmark(
                                    task = task,
                                    isExpanded = task.id in expandedUntimedTasks,
                                    onToggleExpand = {
                                        expandedUntimedTasks =
                                            if (task.id in expandedUntimedTasks) {
                                                expandedUntimedTasks - task.id
                                            } else {
                                                expandedUntimedTasks + task.id
                                            }
                                    },
                                    onSetCompleted = { completed ->
                                        onSetCompleted(
                                            task,
                                            completed
                                        )
                                    },
                                    isSelected = task.id in selectedItems,
                                    onLongClick = { onItemLongClick(task) }
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }

            items(hours) { hour ->
                val itemsInHour = timedItems.filter {
                    when (it) {
                        is TimelineItem.Event -> it.startTime.hour == hour
                        is TimelineItem.Task -> {
                            val taskHour = it.taskTime?.hour
                            val deadlineHour =
                                if (it.deadlineDate == selectedDate) it.deadlineTime?.hour else null
                            // Task appears in both scheduled hour and deadline hour
                            taskHour == hour || deadlineHour == hour
                        }
                    }
                }
                HourSlot(
                    hour = hour,
                    itemsInHour = itemsInHour,
                    selectedItems = selectedItems,
                    onItemClick = onItemClick,
                    onItemLongClick = onItemLongClick,
                    onRemoveItem = onRemoveItem,
                    onSetCompleted = onSetCompleted,
                    currentTime = if (selectedDate == LocalDate.now() && currentTime.hour == hour) currentTime else null,
                    selectedDate = selectedDate
                )
            }
        }
    }

}

@Composable
fun UntimedTaskBookmark(
    task: TimelineItem.Task,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSetCompleted: (Boolean) -> Unit,
    isSelected: Boolean,
    onLongClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val priorityColor = getPriorityColor(task.priority)
    val containerColor = getPriorityContainerColor(task.priority, isDark)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .combinedClickable(
                onClick = onToggleExpand,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(
            topStart = 4.dp,
            bottomStart = 4.dp,
            topEnd = 8.dp,
            bottomEnd = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.5f
            ) else containerColor
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column {
            Row(
                modifier = Modifier
                    .height(32.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // The "Bookmark" tab on the left
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(if (task.isCompleted) CompletedColor else priorityColor)
                )

                IconButton(
                    onClick = { onSetCompleted(!task.isCompleted) },
                    modifier = Modifier
                        .size(32.dp)
                        .padding(start = 2.dp)
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = if (task.isCompleted) CompletedColor else priorityColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(16.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(start = 34.dp, end = 12.dp, bottom = 6.dp)) {
                    if (task.details.isNotBlank()) {
                        Text(
                            text = task.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (task.deadlineDate != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.Flag,
                                null,
                                modifier = Modifier.size(10.dp),
                                tint = if (task.isMissed()) MissedColor else priorityColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Due: ${
                                    task.deadlineDate.format(
                                        DateTimeFormatter.ofPattern(
                                            "MMM dd"
                                        )
                                    )
                                }",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (task.isMissed()) MissedColor else priorityColor
                            )
                        }
                    }
                }
            }
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
    onSetCompleted: (TimelineItem, Boolean) -> Unit,
    currentTime: LocalTime? = null,
    selectedDate: LocalDate
) {
    var slotHeight by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    Row(
        modifier = Modifier
            .heightIn(min = HourSlotHeight.dp)
            .fillMaxWidth()
            .onGloballyPositioned { slotHeight = it.size.height }
    ) {
        Text(
            text = String.format(Locale.getDefault(), "%02d:00", hour),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .width(48.dp)
                .padding(top = 8.dp, start = 8.dp)
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

            Column(modifier = Modifier.padding(top = 24.dp, start = 8.dp, end = 16.dp)) {
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
                        onSetCompleted = { completed -> onSetCompleted(item, completed) },
                        selectedDate = selectedDate
                    )
                }
            }

            if (currentTime != null) {
                val fraction = currentTime.minute / 60f
                val slotHeightDp = with(density) { slotHeight.toDp() }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 16.dp + (slotHeightDp - 16.dp) * fraction),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.error, CircleShape)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.error, thickness = 2.dp)
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
    onSetCompleted: (Boolean) -> Unit,
    selectedDate: LocalDate
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
                    tint = if (direction == SwipeToDismissBoxValue.EndToStart) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        content = {
            when (item) {
                is TimelineItem.Task -> TaskCard(
                    item,
                    isSelected,
                    selectedDate = selectedDate
                ) { onSetCompleted(!item.isCompleted) }

                is TimelineItem.Event -> EventCard(item, isSelected)
            }
        }
    )
}

@Composable
fun EventCard(item: TimelineItem.Event, isSelected: Boolean, modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    val containerColor = if (isDark) EventColorDark else EventColorLight
    val contentColor = if (isDark) Color.White else Color(0xFF0D47A1)

    ElevatedCard(
        modifier = modifier.border(
            border = if (isSelected) BorderStroke(
                3.dp,
                MaterialTheme.colorScheme.primary
            ) else BorderStroke(width = 0.dp, color = Color.Transparent)
        ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(EventColor)
            )
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
                if (item.details.isNotBlank()) {
                    Text(
                        text = item.details,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                }
                val timeText = if (item.isAllDay) "All Day" else "${
                    item.startTime.format(
                        DateTimeFormatter.ofPattern("hh:mm a")
                    )
                } - ${item.endTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}"
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun TaskCard(
    item: TimelineItem.Task,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    onToggleComplete: () -> Unit
) {
    val isMissed = item.isMissed()
    val isDark = isSystemInDarkTheme()
    val priorityColor = getPriorityColor(item.priority)
    val priorityContainer = getPriorityContainerColor(item.priority, isDark)

    val containerColor = when {
        item.isCompleted -> if (isDark) Color(0xFF2C2C2C) else Color(0xFFF5F5F5)
        isMissed -> if (isDark) MissedColor.copy(alpha = 0.15f) else MissedColor.copy(alpha = 0.05f)
        else -> priorityContainer
    }

    val contentColor = when {
        item.isCompleted -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        isMissed -> if (isDark) Color(0xFFFFCDD2) else MissedColor
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        border = if (isSelected) {
            BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
        } else if (isMissed) {
            BorderStroke(2.dp, MissedColor.copy(alpha = 0.5f))
        } else {
            null
        },
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(if (item.isCompleted) CompletedColor else if (isMissed) MissedColor else priorityColor)
            )
            IconButton(onClick = onToggleComplete, modifier = Modifier.padding(start = 4.dp)) {
                Icon(
                    imageVector = if (item.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = "Complete",
                    tint = if (item.isCompleted) CompletedColor else if (isMissed) MissedColor else priorityColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 8.dp)
                .weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isMissed && !item.isCompleted) FontWeight.Black else FontWeight.Bold,
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
                    } else if (!item.isCompleted) {
                        Badge(
                            containerColor = priorityColor.copy(alpha = 0.2f),
                            contentColor = priorityColor
                        ) {
                            Text(
                                item.priority.name,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                if (item.details.isNotBlank()) {
                    Text(
                        text = item.details,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Show scheduled time
                    item.taskTime?.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = priorityColor.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = it.format(DateTimeFormatter.ofPattern("hh:mm a")),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Show deadline if it's today
                    if (item.deadlineDate == selectedDate) {
                        item.deadlineTime?.let {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Flag,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = if (isMissed) MissedColor else priorityColor.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Due: ${it.format(DateTimeFormatter.ofPattern("hh:mm a"))}",
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
    }
}
