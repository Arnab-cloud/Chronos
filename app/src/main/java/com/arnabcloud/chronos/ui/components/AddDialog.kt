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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arnabcloud.chronos.model.Priority
import com.arnabcloud.chronos.model.TimelineItem
import com.arnabcloud.chronos.ui.theme.EventColor
import com.arnabcloud.chronos.ui.theme.getPriorityColor
//import com.arnabcloud.chronos.ui.screen.vault.DurationPickerDialog
import com.arnabcloud.chronos.util.formatDuration
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
                                    colors = FilterChipDefaults.filterChipColors(
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

