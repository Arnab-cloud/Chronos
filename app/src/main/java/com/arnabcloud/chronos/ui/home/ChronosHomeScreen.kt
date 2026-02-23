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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoveUp
import androidx.compose.material.icons.outlined.CheckCircle
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
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChronosHomeScreen(viewModel: ChronosViewModel) {
    var multiSelectMode by rememberSaveable { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<UUID>() }
    var showMoveDialog by remember { mutableStateOf(false) }

    val onClearSelection = {
        multiSelectMode = false
        selectedItems.clear()
    }

    if (showMoveDialog) {
        MoveItemsDialog(
            onDismiss = { },
            onConfirm = { newDate ->
                viewModel.moveItems(selectedItems.toList(), newDate)
                onClearSelection()
            }
        )
    }

    Scaffold(
        topBar = {
            if (multiSelectMode) {
                ContextualTopAppBar(
                    selectionCount = selectedItems.size,
                    onClose = onClearSelection,
                    onDelete = {
                        selectedItems.forEach { id ->
                            viewModel.items.find { it.id == id }?.let { item ->
                                viewModel.removeItem(item)
                            }
                        }
                        onClearSelection()
                    },
                    onMove = { }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            DayPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            Timeline(
                modifier = Modifier.weight(1f),
                items = viewModel.getItemsForDate(LocalDate.now()),
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
fun DayPicker(modifier: Modifier = Modifier) {
    val days = (0..14).map { LocalDate.now().plusDays(it.toLong()) }
    Column(modifier = modifier) {
        Text(
            text = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")),
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
                val isToday = day == LocalDate.now()
                Column(
                    modifier = Modifier
                        .width(55.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(
                            if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.3f
                            )
                        )
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day.dayOfWeek.name.take(3),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = day.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
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
    var editingHour by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        val currentHour = LocalTime.now().hour
        scrollState.scrollTo((currentHour * 100 * 2.5).toInt())
    }

    Box(modifier = modifier.verticalScroll(scrollState)) {
        Column(modifier = Modifier.padding(16.dp)) {
            hours.forEach { hour ->
                HourSlot(
                    hour = hour,
                    itemsInHour = items.filter { it.startTime?.hour == hour },
                    isEditing = editingHour == hour,
                    onStartEditing = { editingHour = hour },
                    onStopEditing = { editingHour = null },
                    selectedItems = selectedItems,
                    onItemClick = onItemClick,
                    onItemLongClick = onItemLongClick,
                    onAddItem = onAddItem,
                    onRemoveItem = onRemoveItem,
                    onSetCompleted = onSetCompleted
                )
            }
        }
        NowLine()
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HourSlot(
    hour: Int,
    itemsInHour: List<TimelineItem>,
    isEditing: Boolean,
    onStartEditing: () -> Unit,
    onStopEditing: () -> Unit,
    selectedItems: List<UUID>,
    onItemClick: (TimelineItem) -> Unit,
    onItemLongClick: (TimelineItem) -> Unit,
    onAddItem: (TimelineItem) -> Unit,
    onRemoveItem: (TimelineItem) -> Unit,
    onSetCompleted: (TimelineItem, Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .heightIn(min = 100.dp)
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

                if (isEditing) {
                    QuickAddTextField(
                        onAdd = {
                            onAddItem(TimelineItem(title = it, startTime = LocalTime.of(hour, 0)))
                            onStopEditing()
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clickable(onClick = onStartEditing),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Quick Add",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickAddTextField(onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    BasicTextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onKeyEvent {
                if (it.key == Key.Enter) {
                    onAdd(text)
                    true
                } else {
                    false
                }
            },
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            onAdd(text)
            keyboardController?.hide()
        }),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.padding(vertical = 8.dp)) {
                if (text.isEmpty()) {
                    Text(
                        "Quick Type a task...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                innerTextField()
            }
        }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
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
            onSetCompleted(!item.isCompleted)
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromEndToStart = true,
        enableDismissFromStartToEnd = true,
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
            if (item.isTask) {
                TaskCard(item, isSelected) { onSetCompleted(!item.isCompleted) }
            } else {
                EventCard(item, isSelected)
            }
        }
    )
}

@Composable
fun EventCard(item: TimelineItem, isSelected: Boolean, modifier: Modifier = Modifier) {
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
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (item.details.isNotBlank()) {
                Text(
                    text = item.details,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            val timeText = if (item.isAllDay) "All Day" else item.startTime?.format(
                DateTimeFormatter.ofPattern("hh:mm a")
            ) ?: ""
            Text(
                text = timeText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun TaskCard(
    item: TimelineItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onToggleComplete: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
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
                    tint = if (item.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
                if (item.details.isNotBlank()) {
                    Text(
                        text = item.details,
                        style = MaterialTheme.typography.bodySmall,
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
    val yOffset = (minutesFromMidnight * (100.0 / 60.0)) + 16.0

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
