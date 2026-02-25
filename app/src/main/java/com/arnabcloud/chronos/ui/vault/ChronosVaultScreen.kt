package com.arnabcloud.chronos.ui.vault

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arnabcloud.chronos.model.TimelineItem
import com.arnabcloud.chronos.viewmodel.ChronosViewModel
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
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Quick Summary
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.large
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val remaining = items.count { it is TimelineItem.Task && !it.isCompleted }
                    Text(
                        text = "$remaining tasks remaining",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your productivity hub",
                        style = MaterialTheme.typography.bodySmall
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

    val border = when {
        isMissed -> BorderStroke(2.dp, MaterialTheme.colorScheme.error)
        !isTask -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        else -> null
    }

    val containerColor = when {
        isCompleted -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        !isTask -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        isMissed -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f)
        else -> MaterialTheme.colorScheme.surface
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        border = border ?: CardDefaults.outlinedCardBorder(),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isTask) {
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = "Toggle Status",
                        tint = if (isCompleted) MaterialTheme.colorScheme.primary else if (isMissed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                        color = if (isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (!isTask) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                            Text("Event", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                if (item.details.isNotBlank()) {
                    Text(
                        text = item.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Time/Date Info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val timeStr = when (item) {
                            is TimelineItem.Event -> if (item.isAllDay) "All Day" else item.startTime.format(
                                DateTimeFormatter.ofPattern("hh:mm a")
                            )

                            is TimelineItem.Task -> "Task"
                        }
                        Text(
                            text = "${item.date.format(DateTimeFormatter.ofPattern("MMM dd"))} • $timeStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    // Deadline Info for Tasks
                    if (item is TimelineItem.Task && item.deadlineDate != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Flag,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isMissed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error.copy(
                                    alpha = 0.6f
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Due ${item.deadlineDate.format(DateTimeFormatter.ofPattern("MMM dd"))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isMissed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error.copy(
                                    alpha = 0.8f
                                ),
                                fontWeight = if (isMissed) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun ItemDetailDialog(
    item: TimelineItem,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onToggleTask: () -> Unit
) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Badge(
                            containerColor = if (item is TimelineItem.Task) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (item is TimelineItem.Task) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text(
                                text = if (item is TimelineItem.Task) "TASK" else "EVENT",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Date and Time Info
                DetailRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Date",
                    value = item.date.format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy"))
                )

                if (item is TimelineItem.Event) {
                    val timeRange = if (item.isAllDay) "All Day" else {
                        "${item.startTime.format(DateTimeFormatter.ofPattern("hh:mm a"))} - ${
                            item.endTime.format(
                                DateTimeFormatter.ofPattern("hh:mm a")
                            )
                        }"
                    }
                    DetailRow(icon = Icons.Default.AccessTime, label = "Time", value = timeRange)

                    item.location?.let {
                        if (it.isNotBlank()) {
                            DetailRow(
                                icon = Icons.Default.LocationOn,
                                label = "Location",
                                value = it
                            )
                        }
                    }
                }

                if (item is TimelineItem.Task) {
                    item.deadlineDate?.let {
                        DetailRow(
                            icon = Icons.Default.Flag,
                            label = "Deadline",
                            value = it.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                            color = if (item.isMissed()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    DetailRow(
                        icon = Icons.Default.PriorityHigh,
                        label = "Priority",
                        value = item.priority.name
                    )
                }

                if (item.details.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
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
                    if (item is TimelineItem.Task) {
                        Button(
                            onClick = onToggleTask,
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(if (item.isCompleted) "Mark as Active" else "Mark as Done")
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

@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
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
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = color
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeadlinePicker by remember { mutableStateOf(false) }

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

    if (showTimePicker) {
        val timePickerState =
            rememberTimePickerState(initialHour = startTime.hour, initialMinute = startTime.minute)
        Dialog(
            onDismissRequest = { showTimePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.padding(24.dp)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            startTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                            endTime = startTime.plusHours(1)
                            showTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEvent) "New Event" else "New Task") },
        text = {
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

                TextButton(onClick = { showDatePicker = true }) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Date: ${date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}")
                }

                if (isEvent) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isAllDay, onCheckedChange = { isAllDay = it })
                        Text("All day", style = MaterialTheme.typography.bodyMedium)
                    }

                    if (!isAllDay) {
                        TextButton(onClick = { showTimePicker = true }) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Time: ${startTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}")
                        }
                    }
                } else {
                    TextButton(onClick = { showDeadlinePicker = true }) {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (deadlineDate == null) "Add Deadline" else "Deadline: ${
                                deadlineDate?.format(
                                    DateTimeFormatter.ofPattern("MMM dd, yyyy")
                                )
                            }"
                        )
                    }
                }
            }
        },
        confirmButton = {
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
                                deadlineDate = deadlineDate
                            )
                        }
                        onConfirm(item)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
