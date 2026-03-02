package com.arnabcloud.chronos.ui.vault

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arnabcloud.chronos.model.Priority
import com.arnabcloud.chronos.model.TimelineItem
import com.arnabcloud.chronos.ui.theme.CompletedColor
import com.arnabcloud.chronos.ui.theme.EventColor
import com.arnabcloud.chronos.ui.theme.EventColorDark
import com.arnabcloud.chronos.ui.theme.EventColorLight
import com.arnabcloud.chronos.ui.theme.MissedColor
import com.arnabcloud.chronos.ui.theme.getPriorityColor
import com.arnabcloud.chronos.ui.theme.getPriorityContainerColor
import com.arnabcloud.chronos.viewmodel.ChronosViewModel
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ChronosVaultScreen(viewModel: ChronosViewModel) {
    val items = viewModel.items
    var selectedItem by remember { mutableStateOf<TimelineItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "My Tasks & Events",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Quick Summary
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            shape = MaterialTheme.shapes.extraLarge,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val remaining = items.count { it is TimelineItem.Task && !it.isCompleted }
                    Text(
                        text = if (remaining > 0) "$remaining items need attention" else "All caught up!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Plan your day, conquer your goals",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { item ->
                VaultItemCard(
                    item = item,
                    onToggle = { viewModel.toggleComplete(item) },
                    onDelete = { viewModel.removeItem(item) },
                    onClick = { selectedItem = item }
                )
            }
        }
    }

    selectedItem?.let { item ->
        ItemDetailDialog(
            item = item,
            onDismiss = { selectedItem = null },
            onDelete = {
                viewModel.removeItem(item)
                selectedItem = null
            },
            onToggleTask = {
                viewModel.toggleComplete(item)
                selectedItem = null
            },
            onSave = { updatedItem ->
                viewModel.updateItem(updatedItem)
                selectedItem = null
            }
        )
    }
}

@Composable
fun VaultItemCard(
    item: TimelineItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val isTask = item is TimelineItem.Task
    val isCompleted = if (item is TimelineItem.Task) item.isCompleted else false
    val isMissed = if (item is TimelineItem.Task) item.isMissed() else false
    val isDark = isSystemInDarkTheme()

    val accentColor = when {
        !isTask -> EventColor
        isCompleted -> CompletedColor
        isMissed -> MissedColor
        else -> getPriorityColor(item.priority)
    }

    val containerColor = when {
        !isTask -> if (isDark) EventColorDark else EventColorLight
        isCompleted -> if (isDark) Color(0xFF2C2C2C) else Color(0xFFF5F5F5)
        isMissed -> if (isDark) MissedColor.copy(alpha = 0.15f) else MissedColor.copy(alpha = 0.05f)
        else -> getPriorityContainerColor(item.priority, isDark)
    }

    val contentColor = when {
        !isTask -> if (isDark) Color.White else Color(0xFF0D47A1)
        isCompleted -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        isMissed -> if (isDark) Color(0xFFFFCDD2) else MissedColor
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        border = if (isMissed && !isCompleted) BorderStroke(
            2.dp,
            MissedColor.copy(alpha = 0.5f)
        ) else null,
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
                    .background(accentColor)
            )

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isTask) {
                    IconButton(onClick = onToggle) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = "Toggle Status",
                            tint = accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(accentColor.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (!isTask) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(
                                containerColor = accentColor.copy(alpha = 0.2f),
                                contentColor = contentColor
                            ) {
                                Text(
                                    "EVENT",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                                )
                            }
                        } else if (isMissed && !isCompleted) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(containerColor = MissedColor, contentColor = Color.White) {
                                Text(
                                    "MISSED",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                                )
                            }
                        }
                    }

                    if (item.details.isNotBlank()) {
                        Text(
                            text = item.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.7f),
                            maxLines = 1,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Time/Date Info
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = contentColor.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val timeStr = when (item) {
                                is TimelineItem.Event -> if (item.isAllDay) "All Day" else item.startTime.format(
                                    DateTimeFormatter.ofPattern("hh:mm a")
                                )

                                is TimelineItem.Task -> {
                                    item.taskTime?.format(DateTimeFormatter.ofPattern("hh:mm a"))
                                        ?: "Anytime"
                                }
                            }
                            Text(
                                text = "${item.date.format(DateTimeFormatter.ofPattern("MMM dd"))} • $timeStr",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = contentColor.copy(alpha = 0.6f)
                            )
                        }

                        // Deadline Info for Tasks
                        if (item is TimelineItem.Task && item.deadlineDate != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Flag,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isMissed) MissedColor else accentColor.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Due ${
                                        item.deadlineDate.format(
                                            DateTimeFormatter.ofPattern(
                                                "MMM dd"
                                            )
                                        )
                                    }",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isMissed) MissedColor else contentColor.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = if (isDark) Color.White.copy(alpha = 0.4f) else Color.Black.copy(
                            alpha = 0.3f
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemDetailDialog(
    item: TimelineItem,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onToggleTask: () -> Unit,
    onSave: (TimelineItem) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }

    // Editable state
    var editedTitle by remember { mutableStateOf(item.title) }
    var editedDetails by remember { mutableStateOf(item.details) }
    var editedDate by remember { mutableStateOf(item.date) }
    var editedStartTime by remember { mutableStateOf(if (item is TimelineItem.Event) item.startTime else LocalTime.now()) }
    var editedEndTime by remember {
        mutableStateOf(
            if (item is TimelineItem.Event) item.endTime else LocalTime.now().plusHours(1)
        )
    }
    var editedIsAllDay by remember { mutableStateOf(if (item is TimelineItem.Event) item.isAllDay else false) }
    var editedLocation by remember {
        mutableStateOf(
            if (item is TimelineItem.Event) item.location ?: "" else ""
        )
    }
    var editedDeadlineDate by remember { mutableStateOf(if (item is TimelineItem.Task) item.deadlineDate else null) }
    var editedPriority by remember { mutableStateOf(if (item is TimelineItem.Task) item.priority else Priority.MEDIUM) }
    var editedTaskTime by remember { mutableStateOf(if (item is TimelineItem.Task) item.taskTime else null) }
    var editedDeadlineTime by remember { mutableStateOf(if (item is TimelineItem.Task) item.deadlineTime else null) }

    var isDurationMode by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showDurationPicker by remember { mutableStateOf(false) }
    var showDeadlinePicker by remember { mutableStateOf(false) }
    var showTaskTimePicker by remember { mutableStateOf(false) }
    var showDeadlineTimePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = editedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        editedDate =
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeadlinePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (editedDeadlineDate
                ?: LocalDate.now()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDeadlinePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        editedDeadlineDate =
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDeadlinePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    editedDeadlineDate = null
                    showDeadlinePicker = false
                }) { Text("Clear") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showStartTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = editedStartTime.hour,
            initialMinute = editedStartTime.minute
        )
        Dialog(onDismissRequest = { showStartTimePicker = false }) {
            Surface(shape = MaterialTheme.shapes.extraLarge) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            val newStart =
                                LocalTime.of(timePickerState.hour, timePickerState.minute)
                            if (isDurationMode) {
                                val duration = Duration.between(editedStartTime, editedEndTime)
                                editedStartTime = newStart
                                editedEndTime = editedStartTime.plus(duration)
                            } else {
                                editedStartTime = newStart
                            }
                            showStartTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }

    if (showEndTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = editedEndTime.hour,
            initialMinute = editedEndTime.minute
        )
        Dialog(onDismissRequest = { showEndTimePicker = false }) {
            Surface(shape = MaterialTheme.shapes.extraLarge) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            editedEndTime =
                                LocalTime.of(timePickerState.hour, timePickerState.minute)
                            showEndTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }

    if (showTaskTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = editedTaskTime?.hour ?: LocalTime.now().hour,
            initialMinute = editedTaskTime?.minute ?: LocalTime.now().minute
        )
        Dialog(onDismissRequest = { showTaskTimePicker = false }) {
            Surface(shape = MaterialTheme.shapes.extraLarge) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            editedTaskTime = null
                            showTaskTimePicker = false
                        }) { Text("Clear") }
                        TextButton(onClick = { showTaskTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            editedTaskTime =
                                LocalTime.of(timePickerState.hour, timePickerState.minute)
                            showTaskTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }

    if (showDeadlineTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = editedDeadlineTime?.hour ?: LocalTime.now().hour,
            initialMinute = editedDeadlineTime?.minute ?: LocalTime.now().minute
        )
        Dialog(onDismissRequest = { showDeadlineTimePicker = false }) {
            Surface(shape = MaterialTheme.shapes.extraLarge) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            editedDeadlineTime = null
                            showDeadlineTimePicker = false
                        }) { Text("Clear") }
                        TextButton(onClick = { showDeadlineTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            editedDeadlineTime =
                                LocalTime.of(timePickerState.hour, timePickerState.minute)
                            showDeadlineTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }

    if (showDurationPicker) {
        DurationPickerDialog(
            initialDuration = Duration.between(editedStartTime, editedEndTime),
            onDismiss = { showDurationPicker = false },
            onConfirm = { duration ->
                editedEndTime = editedStartTime.plus(duration)
                showDurationPicker = false
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Badge(
                            containerColor = if (item is TimelineItem.Task) MaterialTheme.colorScheme.secondaryContainer else EventColor.copy(
                                alpha = 0.2f
                            ),
                            contentColor = if (item is TimelineItem.Task) MaterialTheme.colorScheme.onSecondaryContainer else EventColor
                        ) {
                            Text(
                                text = if (item is TimelineItem.Task) "TASK" else "EVENT",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        if (isEditing) {
                            Text(
                                text = "Editing Details",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isEditing) {
                            TextButton(
                                onClick = {
                                    // Discard changes and revert state
                                    editedTitle = item.title
                                    editedDetails = item.details
                                    editedDate = item.date
                                    if (item is TimelineItem.Event) {
                                        editedStartTime = item.startTime
                                        editedEndTime = item.endTime
                                        editedIsAllDay = item.isAllDay
                                        editedLocation = item.location ?: ""
                                    }
                                    if (item is TimelineItem.Task) {
                                        editedDeadlineDate = item.deadlineDate
                                        editedPriority = item.priority
                                        editedTaskTime = item.taskTime
                                        editedDeadlineTime = item.deadlineTime
                                    }
                                    isEditing = false
                                }
                            ) {
                                Text("Discard", color = MaterialTheme.colorScheme.error)
                            }
                        } else {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Item",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Dialog"
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Editable Fields / View Fields
                if (isEditing) {
                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                // Date and Time Info
                if (isEditing) {
                    Row {
                        TextButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Date: ${editedDate.format(DateTimeFormatter.ofPattern("MMM dd"))}")
                        }
                        if (item is TimelineItem.Task) {
                            TextButton(
                                onClick = { showTaskTimePicker = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Time: ${editedTaskTime?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "None"}")
                            }
                        }
                    }
                } else {
                    DetailRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Date",
                        value = item.date.format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy"))
                    )
                    if (item is TimelineItem.Task && item.taskTime != null) {
                        DetailRow(
                            icon = Icons.Default.AccessTime,
                            label = "Time",
                            value = item.taskTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
                        )
                    }
                }

                if (item is TimelineItem.Event) {
                    if (isEditing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = editedIsAllDay,
                                onCheckedChange = { editedIsAllDay = it })
                            Text("All day", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (!editedIsAllDay) {
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                TextButton(onClick = { showStartTimePicker = true }) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Start: ${
                                            editedStartTime.format(
                                                DateTimeFormatter.ofPattern(
                                                    "hh:mm a"
                                                )
                                            )
                                        }"
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = !isDurationMode,
                                        onClick = { isDurationMode = false },
                                        label = { Text("End Time") }
                                    )
                                    FilterChip(
                                        selected = isDurationMode,
                                        onClick = { isDurationMode = true },
                                        label = { Text("Duration") }
                                    )
                                }

                                if (isDurationMode) {
                                    TextButton(onClick = { showDurationPicker = true }) {
                                        Icon(
                                            Icons.Default.AccessTime,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Duration: ${
                                                formatDuration(
                                                    Duration.between(
                                                        editedStartTime,
                                                        editedEndTime
                                                    )
                                                )
                                            }"
                                        )
                                    }
                                } else {
                                    TextButton(onClick = { showEndTimePicker = true }) {
                                        Icon(
                                            Icons.Default.AccessTime,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "End: ${
                                                editedEndTime.format(
                                                    DateTimeFormatter.ofPattern(
                                                        "hh:mm a"
                                                    )
                                                )
                                            }"
                                        )
                                    }
                                }
                            }
                        }
                        OutlinedTextField(
                            value = editedLocation,
                            onValueChange = { editedLocation = it },
                            label = { Text("Location") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    } else {
                        val timeRange = if (item.isAllDay) "All Day" else {
                            "${item.startTime.format(DateTimeFormatter.ofPattern("hh:mm a"))} - ${
                                item.endTime.format(
                                    DateTimeFormatter.ofPattern("hh:mm a")
                                )
                            }"
                        }
                        DetailRow(
                            icon = Icons.Default.AccessTime,
                            label = "Time",
                            value = timeRange
                        )
                        item.location?.let {
                            if (it.isNotBlank()) DetailRow(
                                icon = Icons.Default.LocationOn,
                                label = "Location",
                                value = it
                            )
                        }
                    }
                }

                if (item is TimelineItem.Task) {
                    if (isEditing) {
                        Row {
                            TextButton(
                                onClick = { showDeadlinePicker = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Flag,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (editedDeadlineDate == null) "Add Deadline" else "Due: ${
                                        editedDeadlineDate?.format(
                                            DateTimeFormatter.ofPattern("MMM dd")
                                        )
                                    }"
                                )
                            }
                            if (editedDeadlineDate != null) {
                                TextButton(
                                    onClick = { showDeadlineTimePicker = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "at ${
                                            editedDeadlineTime?.format(
                                                DateTimeFormatter.ofPattern(
                                                    "hh:mm a"
                                                )
                                            ) ?: "None"
                                        }"
                                    )
                                }
                            }
                        }

                        Text(
                            "Priority",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Priority.entries.forEach { priority ->
                                val pColor = getPriorityColor(priority)
                                FilterChip(
                                    selected = editedPriority == priority,
                                    onClick = { editedPriority = priority },
                                    label = { Text(priority.name) },
                                    colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = pColor.copy(alpha = 0.2f),
                                        selectedLabelColor = pColor
                                    ),
                                    leadingIcon = if (editedPriority == priority) {
                                        {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }
                    } else {
                        item.deadlineDate?.let { it ->
                            val timeStr = item.deadlineTime?.let {
                                " at ${
                                    it.format(
                                        DateTimeFormatter.ofPattern("hh:mm a")
                                    )
                                }"
                            } ?: ""
                            val isMissed = item.isMissed()
                            DetailRow(
                                icon = Icons.Default.Flag,
                                label = "Deadline",
                                value = "${it.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}$timeStr",
                                color = if (isMissed) MissedColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        DetailRow(
                            icon = Icons.Default.PriorityHigh,
                            label = "Priority",
                            value = item.priority.name,
                            color = getPriorityColor(item.priority)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                if (isEditing) {
                    OutlinedTextField(
                        value = editedDetails,
                        onValueChange = { editedDetails = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                } else if (item.details.isNotBlank()) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.AutoMirrored.Filled.Notes,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = item.details,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isEditing) {
                        Button(
                            onClick = {
                                val updatedItem = when (item) {
                                    is TimelineItem.Event -> item.copy(
                                        title = editedTitle,
                                        details = editedDetails,
                                        date = editedDate,
                                        startTime = if (editedIsAllDay) LocalTime.MIDNIGHT else editedStartTime,
                                        endTime = if (editedIsAllDay) LocalTime.MAX else editedEndTime,
                                        isAllDay = editedIsAllDay,
                                        location = editedLocation.ifBlank { null }
                                    )

                                    is TimelineItem.Task -> item.copy(
                                        title = editedTitle,
                                        details = editedDetails,
                                        date = editedDate,
                                        taskTime = editedTaskTime,
                                        deadlineDate = editedDeadlineDate,
                                        deadlineTime = editedDeadlineTime,
                                        priority = editedPriority
                                    )
                                }
                                onSave(updatedItem)
                            },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Changes")
                        }
                    } else {
                        if (item is TimelineItem.Task) {
                            Button(
                                onClick = onToggleTask,
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(if (item.isCompleted) "Re-activate Task" else "Complete Task")
                            }
                        }

                        TextButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(if (item is TimelineItem.Task) 0.5f else 1f),
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DurationPickerDialog(
    initialDuration: Duration,
    onDismiss: () -> Unit,
    onConfirm: (Duration) -> Unit
) {
    var hours by remember { mutableStateOf(initialDuration.toHours().toString()) }
    var minutes by remember { mutableStateOf((initialDuration.toMinutes() % 60).toString()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Set Duration",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { if (it.all { c -> c.isDigit() }) hours = it },
                        label = { Text("Hours") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Text(":", style = MaterialTheme.typography.headlineMedium)
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { if (it.all { c -> c.isDigit() }) minutes = it },
                        label = { Text("Minutes") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val h = hours.toLongOrNull() ?: 0L
                            val m = minutes.toLongOrNull() ?: 0L
                            onConfirm(Duration.ofHours(h).plusMinutes(m))
                        },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

private fun formatDuration(duration: Duration): String {
    val h = duration.toHours()
    val m = duration.toMinutes() % 60
    return buildString {
        if (h > 0) append("${h}h ")
        if (m > 0 || h == 0L) append("${m}m")
    }.trim()
}

@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTaskDialog(
    isEvent: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (TimelineItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var isAllDay by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(LocalTime.now()) }
    var endTime by remember { mutableStateOf(LocalTime.now().plusHours(1)) }
    var deadlineDate by remember { mutableStateOf<LocalDate?>(null) }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var taskTime by remember { mutableStateOf<LocalTime?>(null) }
    var deadlineTime by remember { mutableStateOf<LocalTime?>(null) }

    var isDurationMode by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showDurationPicker by remember { mutableStateOf(false) }
    var showDeadlinePicker by remember { mutableStateOf(false) }
    var showTaskTimePicker by remember { mutableStateOf(false) }
    var showDeadlineTimePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeadlinePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (deadlineDate
                ?: LocalDate.now()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDeadlinePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        deadlineDate =
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDeadlinePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    deadlineDate = null
                    showDeadlinePicker = false
                }) { Text("Clear") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showStartTimePicker) {
        val timePickerState =
            rememberTimePickerState(initialHour = startTime.hour, initialMinute = startTime.minute)
        Dialog(onDismissRequest = { showStartTimePicker = false }) {
            Surface(shape = MaterialTheme.shapes.extraLarge) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState, modifier = Modifier.fillMaxWidth())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            val newStart =
                                LocalTime.of(timePickerState.hour, timePickerState.minute)
                            if (isDurationMode) {
                                val duration = Duration.between(startTime, endTime)
                                startTime = newStart
                                endTime = startTime.plus(duration)
                            } else {
                                startTime = newStart
                            }
                            showStartTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }

    if (showEndTimePicker) {
        val timePickerState =
            rememberTimePickerState(initialHour = endTime.hour, initialMinute = endTime.minute)
        Dialog(onDismissRequest = { showEndTimePicker = false }) {
            Surface(shape = MaterialTheme.shapes.extraLarge) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            endTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                            showEndTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }

    if (showTaskTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = taskTime?.hour ?: LocalTime.now().hour,
            initialMinute = taskTime?.minute ?: LocalTime.now().minute
        )
        Dialog(onDismissRequest = { showTaskTimePicker = false }) {
            Surface(shape = MaterialTheme.shapes.extraLarge) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            taskTime = null
                            showTaskTimePicker = false
                        }) { Text("Clear") }
                        TextButton(onClick = { showTaskTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            taskTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                            showTaskTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }

    if (showDeadlineTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = deadlineTime?.hour ?: LocalTime.now().hour,
            initialMinute = deadlineTime?.minute ?: LocalTime.now().minute
        )
        Dialog(onDismissRequest = { showDeadlineTimePicker = false }) {
            Surface(shape = MaterialTheme.shapes.extraLarge) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            deadlineTime = null
                            showDeadlineTimePicker = false
                        }) { Text("Clear") }
                        TextButton(onClick = { showDeadlineTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            deadlineTime =
                                LocalTime.of(timePickerState.hour, timePickerState.minute)
                            showDeadlineTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }

    if (showDurationPicker) {
        DurationPickerDialog(
            initialDuration = Duration.between(startTime, endTime),
            onDismiss = { showDurationPicker = false },
            onConfirm = { duration ->
                endTime = startTime.plus(duration)
                showDurationPicker = false
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Badge(
                            containerColor = if (!isEvent) MaterialTheme.colorScheme.secondaryContainer else EventColor.copy(
                                alpha = 0.2f
                            ),
                            contentColor = if (!isEvent) MaterialTheme.colorScheme.onSecondaryContainer else EventColor
                        ) {
                            Text(
                                text = if (isEvent) "NEW EVENT" else "NEW TASK",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isEvent) "Create Event" else "Create Task",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Dialog")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("Details") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row {
                        TextButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Date: ${date.format(DateTimeFormatter.ofPattern("MMM dd"))}")
                        }
                        if (!isEvent) {
                            TextButton(
                                onClick = { showTaskTimePicker = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Time: ${taskTime?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "None"}")
                            }
                        }
                    }

                    if (isEvent) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isAllDay, onCheckedChange = { isAllDay = it })
                            Text("All day", style = MaterialTheme.typography.bodyMedium)
                        }

                        if (!isAllDay) {
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                TextButton(onClick = { showStartTimePicker = true }) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Start: ${startTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}")
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = !isDurationMode,
                                        onClick = { isDurationMode = false },
                                        label = { Text("End Time") }
                                    )
                                    FilterChip(
                                        selected = isDurationMode,
                                        onClick = { isDurationMode = true },
                                        label = { Text("Duration") }
                                    )
                                }

                                if (isDurationMode) {
                                    TextButton(onClick = { showDurationPicker = true }) {
                                        Icon(
                                            Icons.Default.AccessTime,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Duration: ${
                                                formatDuration(
                                                    Duration.between(
                                                        startTime,
                                                        endTime
                                                    )
                                                )
                                            }"
                                        )
                                    }
                                } else {
                                    TextButton(onClick = { showEndTimePicker = true }) {
                                        Icon(
                                            Icons.Default.AccessTime,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("End: ${endTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}")
                                    }
                                }
                            }
                        }
                    } else {
                        Row {
                            TextButton(
                                onClick = { showDeadlinePicker = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Flag,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (deadlineDate == null) "Add Deadline" else "Due: ${
                                        deadlineDate?.format(
                                            DateTimeFormatter.ofPattern("MMM dd")
                                        )
                                    }"
                                )
                            }
                            if (deadlineDate != null) {
                                TextButton(
                                    onClick = { showDeadlineTimePicker = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("at ${deadlineTime?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "None"}")
                                }
                            }
                        }

                        Text(
                            "Priority",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Priority.entries.forEach { p ->
                                val pColor = getPriorityColor(p)
                                FilterChip(
                                    selected = priority == p,
                                    onClick = { priority = p },
                                    label = { Text(p.name) },
                                    colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = pColor.copy(alpha = 0.2f),
                                        selectedLabelColor = pColor
                                    ),
                                    leadingIcon = if (priority == p) {
                                        {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.5f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val item = if (isEvent) {
                                    TimelineItem.Event(
                                        title = title,
                                        details = details,
                                        date = date,
                                        startTime = if (isAllDay) LocalTime.MIDNIGHT else startTime,
                                        endTime = if (isAllDay) LocalTime.MAX else endTime,
                                        isAllDay = isAllDay
                                    )
                                } else {
                                    TimelineItem.Task(
                                        title = title,
                                        details = details,
                                        date = date,
                                        taskTime = taskTime,
                                        deadlineDate = deadlineDate,
                                        deadlineTime = deadlineTime,
                                        priority = priority
                                    )
                                }
                                onConfirm(item)
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create")
                    }
                }
            }
        }
    }
}
