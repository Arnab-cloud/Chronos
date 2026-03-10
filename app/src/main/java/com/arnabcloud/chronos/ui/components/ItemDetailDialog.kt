package com.arnabcloud.chronos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arnabcloud.chronos.model.Priority
import com.arnabcloud.chronos.model.TimelineItem
import com.arnabcloud.chronos.ui.theme.EventColor
import com.arnabcloud.chronos.ui.theme.MissedColor
import com.arnabcloud.chronos.ui.theme.getPriorityColor
import com.arnabcloud.chronos.util.formatDuration
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private object DetailDialogDefaults {
    val PADDING_SMALL = 4.dp
    val PADDING_MEDIUM = 8.dp
    val PADDING_LARGE = 16.dp
    val PADDING_XLARGE = 24.dp
    
    val SPACING_SMALL = 8.dp
    val SPACING_MEDIUM = 12.dp
    val SPACING_LARGE = 16.dp
    val SPACING_XLARGE = 32.dp
    
    val ICON_SIZE_SMALL = 16.dp
    val ICON_SIZE_MEDIUM = 18.dp
    val ICON_SIZE_LARGE = 20.dp
    
    const val DIALOG_WIDTH_FRACTION = 0.9f
    const val DURATION_DIALOG_WIDTH_FRACTION = 0.85f
    const val TONAL_ELEVATION = 6
    const val CONTAINER_ALPHA_MUTED = 0.2f
    const val LABEL_ALPHA_MUTED = 0.7f
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
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
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
        TimePickerDialog(
            initialTime = editedStartTime,
            onDismiss = { showStartTimePicker = false },
            onConfirm = { newStart ->
                if (isDurationMode) {
                    val duration = Duration.between(editedStartTime, editedEndTime)
                    editedStartTime = newStart
                    editedEndTime = editedStartTime.plus(duration)
                } else {
                    editedStartTime = newStart
                }
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            initialTime = editedEndTime,
            onDismiss = { showEndTimePicker = false },
            onConfirm = { newEnd ->
                editedEndTime = newEnd
                showEndTimePicker = false
            }
        )
    }

    if (showTaskTimePicker) {
        TimePickerDialog(
            initialTime = editedTaskTime ?: LocalTime.now(),
            onDismiss = { showTaskTimePicker = false },
            onConfirm = { editedTaskTime = it; showTaskTimePicker = false },
            onClear = { editedTaskTime = null; showTaskTimePicker = false }
        )
    }

    if (showDeadlineTimePicker) {
        TimePickerDialog(
            initialTime = editedDeadlineTime ?: LocalTime.now(),
            onDismiss = { showDeadlineTimePicker = false },
            onConfirm = { editedDeadlineTime = it; showDeadlineTimePicker = false },
            onClear = { editedDeadlineTime = null; showDeadlineTimePicker = false }
        )
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
                .fillMaxWidth(DetailDialogDefaults.DIALOG_WIDTH_FRACTION)
                .padding(DetailDialogDefaults.PADDING_LARGE),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = DetailDialogDefaults.TONAL_ELEVATION.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(DetailDialogDefaults.PADDING_XLARGE)
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
                                alpha = DetailDialogDefaults.CONTAINER_ALPHA_MUTED
                            ),
                            contentColor = if (item is TimelineItem.Task) MaterialTheme.colorScheme.onSecondaryContainer else EventColor
                        ) {
                            Text(
                                text = if (item is TimelineItem.Task) "TASK" else "EVENT",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                modifier = Modifier.padding(horizontal = DetailDialogDefaults.PADDING_MEDIUM, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(DetailDialogDefaults.PADDING_SMALL))
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
                                    // Discard changes
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

                Spacer(modifier = Modifier.height(DetailDialogDefaults.SPACING_LARGE))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(DetailDialogDefaults.SPACING_LARGE))

                // Editable Fields / View Fields
                if (isEditing) {
                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = DetailDialogDefaults.SPACING_MEDIUM)
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
                                modifier = Modifier.size(DetailDialogDefaults.ICON_SIZE_MEDIUM)
                            )
                            Spacer(modifier = Modifier.width(DetailDialogDefaults.SPACING_SMALL))
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
                                    modifier = Modifier.size(DetailDialogDefaults.ICON_SIZE_MEDIUM)
                                )
                                Spacer(modifier = Modifier.width(DetailDialogDefaults.SPACING_SMALL))
                                Text(
                                    "Time: ${
                                        editedTaskTime?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "None"
                                    }"
                                )
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
                            Checkbox(checked = editedIsAllDay, onCheckedChange = { editedIsAllDay = it })
                            Text("All day", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (!editedIsAllDay) {
                            Column(modifier = Modifier.padding(start = DetailDialogDefaults.SPACING_SMALL)) {
                                TextButton(onClick = { showStartTimePicker = true }) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(DetailDialogDefaults.ICON_SIZE_MEDIUM)
                                    )
                                    Spacer(modifier = Modifier.width(DetailDialogDefaults.SPACING_SMALL))
                                    Text("Start: ${editedStartTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}")
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(DetailDialogDefaults.SPACING_SMALL)) {
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
                                            modifier = Modifier.size(DetailDialogDefaults.ICON_SIZE_MEDIUM)
                                        )
                                        Spacer(modifier = Modifier.width(DetailDialogDefaults.SPACING_SMALL))
                                        Text("Duration: ${formatDuration(Duration.between(editedStartTime, editedEndTime))}")
                                    }
                                } else {
                                    TextButton(onClick = { showEndTimePicker = true }) {
                                        Icon(
                                            Icons.Default.AccessTime,
                                            contentDescription = null,
                                            modifier = Modifier.size(DetailDialogDefaults.ICON_SIZE_MEDIUM)
                                        )
                                        Spacer(modifier = Modifier.width(DetailDialogDefaults.SPACING_SMALL))
                                        Text("End: ${editedEndTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}")
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
                                .padding(top = DetailDialogDefaults.SPACING_SMALL)
                        )
                    } else {
                        val timeRange = if (item.isAllDay) "All Day" else {
                            "${item.startTime.format(DateTimeFormatter.ofPattern("hh:mm a"))} - ${
                                item.endTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
                            }"
                        }
                        DetailRow(icon = Icons.Default.AccessTime, label = "Time", value = timeRange)
                        item.location?.let {
                            if (it.isNotBlank()) DetailRow(icon = Icons.Default.LocationOn, label = "Location", value = it)
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
                                    modifier = Modifier.size(DetailDialogDefaults.ICON_SIZE_MEDIUM)
                                )
                                Spacer(modifier = Modifier.width(DetailDialogDefaults.SPACING_SMALL))
                                Text(
                                    if (editedDeadlineDate == null) "Add Deadline" else "Due: ${
                                        editedDeadlineDate?.format(DateTimeFormatter.ofPattern("MMM dd"))
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
                                        modifier = Modifier.size(DetailDialogDefaults.ICON_SIZE_MEDIUM)
                                    )
                                    Spacer(modifier = Modifier.width(DetailDialogDefaults.SPACING_SMALL))
                                    Text("at ${editedDeadlineTime?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "None"}")
                                }
                            }
                        }

                        Text(
                            "Priority",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = DetailDialogDefaults.SPACING_SMALL)
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(DetailDialogDefaults.SPACING_SMALL)) {
                            Priority.entries.forEach { priority ->
                                val pColor = getPriorityColor(priority)
                                FilterChip(
                                    selected = editedPriority == priority,
                                    onClick = { editedPriority = priority },
                                    label = { Text(priority.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = pColor.copy(alpha = DetailDialogDefaults.CONTAINER_ALPHA_MUTED),
                                        selectedLabelColor = pColor
                                    ),
                                    leadingIcon = if (editedPriority == priority) {
                                        {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(DetailDialogDefaults.ICON_SIZE_SMALL)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }
                    } else {
                        item.deadlineDate?.let { it ->
                            val timeStr = item.deadlineTime?.let { " at ${it.format(DateTimeFormatter.ofPattern("hh:mm a"))}" } ?: ""
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

                Spacer(modifier = Modifier.height(DetailDialogDefaults.SPACING_LARGE))
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
                            modifier = Modifier.size(DetailDialogDefaults.ICON_SIZE_LARGE)
                        )
                        Spacer(modifier = Modifier.width(DetailDialogDefaults.SPACING_MEDIUM))
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

                Spacer(modifier = Modifier.height(DetailDialogDefaults.SPACING_XLARGE))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DetailDialogDefaults.SPACING_MEDIUM)
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
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(DetailDialogDefaults.ICON_SIZE_MEDIUM))
                            Spacer(modifier = Modifier.width(DetailDialogDefaults.SPACING_SMALL))
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
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(DetailDialogDefaults.ICON_SIZE_MEDIUM))
                            Spacer(modifier = Modifier.width(DetailDialogDefaults.SPACING_SMALL))
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
    onClear: (() -> Unit)? = null
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute
    )
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.extraLarge) {
            Column(
                modifier = Modifier.padding(DetailDialogDefaults.PADDING_XLARGE),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = timePickerState)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (onClear != null) {
                        TextButton(onClick = onClear) { Text("Clear") }
                    }
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = {
                        onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    }) { Text("OK") }
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
                .fillMaxWidth(DetailDialogDefaults.DURATION_DIALOG_WIDTH_FRACTION)
                .padding(DetailDialogDefaults.PADDING_LARGE),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = DetailDialogDefaults.TONAL_ELEVATION.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(DetailDialogDefaults.PADDING_XLARGE)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Set Duration",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(DetailDialogDefaults.PADDING_XLARGE))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DetailDialogDefaults.SPACING_SMALL),
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

                Spacer(modifier = Modifier.height(DetailDialogDefaults.PADDING_XLARGE))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(DetailDialogDefaults.SPACING_SMALL))
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
            .padding(vertical = DetailDialogDefaults.SPACING_SMALL),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(DetailDialogDefaults.ICON_SIZE_LARGE)
        )
        Spacer(modifier = Modifier.width(DetailDialogDefaults.SPACING_MEDIUM))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary.copy(alpha = DetailDialogDefaults.LABEL_ALPHA_MUTED)
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
