package com.arnabcloud.chronos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arnabcloud.chronos.model.Priority
import com.arnabcloud.chronos.model.RecurrenceType
import com.arnabcloud.chronos.model.TimelineItem
import com.arnabcloud.chronos.ui.theme.EventColor
import com.arnabcloud.chronos.ui.theme.getPriorityColor
import com.arnabcloud.chronos.util.formatDuration
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Preview
@Composable
fun PreviewDialog() {

    AddTaskDialog(
        isEvent = false,
        onDismiss = {},
        onConfirm = {}
    )
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
    var priority by remember { mutableStateOf(Priority.LOW) }
    var taskTime by remember { mutableStateOf<LocalTime?>(null) }
    var deadlineTime by remember { mutableStateOf<LocalTime?>(null) }

    var isPeriodic by remember { mutableStateOf(false) }
    var recurrence by remember { mutableStateOf(RecurrenceType.DAILY) }

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
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 12.dp)
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
                        Text(
                            text = if (isEvent) "Create Event" else "Create Task",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Dialog")
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp) // Constrain max height but let it wrap content
                        .verticalScroll(state = rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text(text = "Details") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 100.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                        TextButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(weight = 1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(size = 18.dp)
                            )
                            Spacer(modifier = Modifier.width(width = 4.dp))
                            Text(
                                text = "Date: ${date.format(DateTimeFormatter.ofPattern("MMM dd"))}",
                                style = MaterialTheme.typography.bodySmall
                            )
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
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Time: ${taskTime?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "None"}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    if (isEvent) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isAllDay,
                                onCheckedChange = { isAllDay = it },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("All day", style = MaterialTheme.typography.bodySmall)
                        }

                        if (!isAllDay) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                TextButton(onClick = { showStartTimePicker = true }) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Start: ${startTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = !isDurationMode,
                                        onClick = { isDurationMode = false },
                                        label = {
                                            Text(
                                                "End Time",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        modifier = Modifier.height(28.dp)
                                    )
                                    FilterChip(
                                        selected = isDurationMode,
                                        onClick = { isDurationMode = true },
                                        label = {
                                            Text(
                                                "Duration",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        modifier = Modifier.height(28.dp)
                                    )
                                }

                                if (isDurationMode) {
                                    TextButton(onClick = { showDurationPicker = true }) {
                                        Icon(
                                            Icons.Default.AccessTime,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Duration: ${
                                                formatDuration(
                                                    Duration.between(
                                                        startTime,
                                                        endTime
                                                    )
                                                )
                                            }",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                } else {
                                    TextButton(onClick = { showEndTimePicker = true }) {
                                        Icon(
                                            Icons.Default.AccessTime,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "End: ${endTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Periodic Task Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Repeat,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Repeat Task", style = MaterialTheme.typography.bodyMedium)
                            }
                            Switch(
                                checked = isPeriodic,
                                onCheckedChange = { isPeriodic = it }
                            )
                        }

                        AnimatedVisibility(visible = isPeriodic) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                RecurrenceType.entries.forEach { type ->
                                    FilterChip(
                                        selected = recurrence == type,
                                        onClick = { recurrence = type },
                                        label = {
                                            Text(
                                                type.name.lowercase()
                                                    .replaceFirstChar { it.uppercase() })
                                        },
                                        modifier = Modifier.height(28.dp)
                                    )
                                }
                            }
                        }

                        if (!isPeriodic) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = { showDeadlinePicker = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.Flag,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        if (deadlineDate == null) "Deadline" else "Due: ${
                                            deadlineDate?.format(
                                                DateTimeFormatter.ofPattern("MMM dd")
                                            )
                                        }",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                if (deadlineDate != null) {
                                    TextButton(
                                        onClick = { showDeadlineTimePicker = true },
                                        modifier = Modifier.weight(weight = 1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccessTime,
                                            contentDescription = null,
                                            modifier = Modifier.size(size = 18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(width = 4.dp))
                                        Text(
                                            text = deadlineTime?.format(
                                                DateTimeFormatter.ofPattern(
                                                    "hh:mm a"
                                                )
                                            )
                                                ?: "None",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Priority",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(
                                space = 24.dp,
                                alignment = Alignment.CenterHorizontally
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Priority.entries.forEach { p ->
                                val pColor = getPriorityColor(priority = p)
                                FilterChip(
                                    selected = priority == p,
                                    onClick = { priority = p },
                                    label = {
                                        Text(
                                            text = p.name,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    modifier = Modifier.height(28.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = pColor.copy(alpha = 0.2f),
                                        selectedLabelColor = pColor
                                    ),
                                    leadingIcon = if (priority == p) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(size = 14.dp)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(weight = 0.4f)
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
                                        deadlineDate = if (isPeriodic) null else deadlineDate,
                                        deadlineTime = if (isPeriodic) null else deadlineTime,
                                        priority = priority,
                                        isPeriodic = isPeriodic,
                                        recurrence = if (isPeriodic) recurrence else null
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
