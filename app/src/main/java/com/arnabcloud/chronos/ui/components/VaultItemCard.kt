package com.arnabcloud.chronos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.arnabcloud.chronos.model.TimelineItem
import com.arnabcloud.chronos.ui.theme.CompletedColor
import com.arnabcloud.chronos.ui.theme.CompletedContainerDark
import com.arnabcloud.chronos.ui.theme.CompletedContainerLight
import com.arnabcloud.chronos.ui.theme.EventColor
import com.arnabcloud.chronos.ui.theme.EventColorDark
import com.arnabcloud.chronos.ui.theme.EventColorLight
import com.arnabcloud.chronos.ui.theme.EventContentDark
import com.arnabcloud.chronos.ui.theme.EventContentLight
import com.arnabcloud.chronos.ui.theme.MissedColor
import com.arnabcloud.chronos.ui.theme.MissedContentDark
import com.arnabcloud.chronos.ui.theme.getPriorityColor
import com.arnabcloud.chronos.ui.theme.getPriorityContainerColor
import java.time.format.DateTimeFormatter

private val DateFormatter = DateTimeFormatter.ofPattern("MMM dd")
private val TimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

private object VaultItemCardDefaults {
    val ACCENT_BAR_WIDTH = 6.dp
    val CARD_PADDING = 16.dp
    val CONTENT_SPACING = 12.dp
    val ICON_SIZE_SMALL = 14.dp
    val ICON_SIZE_MEDIUM = 20.dp
    val ICON_SIZE_LARGE = 28.dp
    val STATUS_ICON_BG_SIZE = 40.dp
    val BADGE_SPACING = 8.dp
    val DETAILS_TOP_PADDING = 2.dp
    val METADATA_TOP_PADDING = 8.dp
    val METADATA_SPACING = 12.dp
    val META_ICON_TEXT_SPACING = 4.dp

    val MISSED_BORDER_WIDTH = 2.dp
    const val MISSED_ALPHA_DARK = 0.15f
    const val MISSED_ALPHA_LIGHT = 0.05f
    const val MISSED_BORDER_ALPHA = 0.5f
    const val SUBTITLE_ALPHA = 0.7f
    const val META_ALPHA = 0.6f
    const val ICON_BG_ALPHA = 0.2f
    const val DISABLED_ICON_ALPHA_DARK = 0.4f
    const val DISABLED_ICON_ALPHA_LIGHT = 0.3f
    const val COMPLETED_CONTENT_ALPHA = 0.5f
}

@Composable
fun VaultItemCard(
    item: TimelineItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val isTask = item is TimelineItem.Task
    val isCompleted = (item as? TimelineItem.Task)?.isCompleted == true
    val isMissed = (item as? TimelineItem.Task)?.isMissed() == true
    val isDark = isSystemInDarkTheme()

    val accentColor = remember(item, isTask, isCompleted, isMissed) {
        getAccentColor(item, isTask, isCompleted, isMissed)
    }
    val containerColor = getContainerColor(item, isTask, isCompleted, isMissed, isDark)
    val contentColor = getContentColor(isTask, isCompleted, isMissed, isDark)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        border = if (isMissed && !isCompleted) BorderStroke(
            width = VaultItemCardDefaults.MISSED_BORDER_WIDTH,
            color = MissedColor.copy(alpha = VaultItemCardDefaults.MISSED_BORDER_ALPHA)
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
                    .width(VaultItemCardDefaults.ACCENT_BAR_WIDTH)
                    .fillMaxHeight()
                    .background(accentColor)
            )

            Row(
                modifier = Modifier.padding(VaultItemCardDefaults.CARD_PADDING),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ItemStatusIcon(
                    isTask = isTask,
                    isCompleted = isCompleted,
                    accentColor = accentColor,
                    onToggle = onToggle
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = VaultItemCardDefaults.CONTENT_SPACING)
                ) {
                    ItemHeader(
                        title = item.title,
                        isTask = isTask,
                        isCompleted = isCompleted,
                        isMissed = isMissed,
                        accentColor = accentColor,
                        contentColor = contentColor
                    )

                    if (item.details.isNotBlank()) {
                        Text(
                            text = item.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = VaultItemCardDefaults.SUBTITLE_ALPHA),
                            maxLines = 1,
                            modifier = Modifier.padding(top = VaultItemCardDefaults.DETAILS_TOP_PADDING)
                        )
                    }

                    ItemMetadata(
                        item = item,
                        contentColor = contentColor,
                        accentColor = accentColor,
                        isMissed = isMissed
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = if (isDark)
                            Color.White.copy(alpha = VaultItemCardDefaults.DISABLED_ICON_ALPHA_DARK)
                        else
                            Color.Black.copy(alpha = VaultItemCardDefaults.DISABLED_ICON_ALPHA_LIGHT),
                        modifier = Modifier.size(VaultItemCardDefaults.ICON_SIZE_MEDIUM)
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemStatusIcon(
    isTask: Boolean,
    isCompleted: Boolean,
    accentColor: Color,
    onToggle: () -> Unit
) {
    if (isTask) {
        IconButton(onClick = onToggle) {
            Icon(
                imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                contentDescription = "Toggle Status",
                tint = accentColor,
                modifier = Modifier.size(VaultItemCardDefaults.ICON_SIZE_LARGE)
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(VaultItemCardDefaults.STATUS_ICON_BG_SIZE)
                .background(
                    color = accentColor.copy(alpha = VaultItemCardDefaults.ICON_BG_ALPHA),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(VaultItemCardDefaults.ICON_SIZE_MEDIUM)
            )
        }
    }
}

@Composable
private fun ItemHeader(
    title: String,
    isTask: Boolean,
    isCompleted: Boolean,
    isMissed: Boolean,
    accentColor: Color,
    contentColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f, fill = false)
        )

        Spacer(modifier = Modifier.width(VaultItemCardDefaults.BADGE_SPACING))

        when {
            !isTask -> StatusBadge(
                text = "EVENT",
                containerColor = accentColor.copy(alpha = VaultItemCardDefaults.ICON_BG_ALPHA),
                contentColor = contentColor
            )

            isMissed && !isCompleted -> StatusBadge(
                text = "MISSED",
                containerColor = MissedColor,
                contentColor = Color.White
            )
        }
    }
}

@Composable
private fun StatusBadge(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Badge(
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
        )
    }
}

@Composable
private fun ItemMetadata(
    item: TimelineItem,
    contentColor: Color,
    accentColor: Color,
    isMissed: Boolean
) {
    Row(
        modifier = Modifier.padding(top = VaultItemCardDefaults.METADATA_TOP_PADDING),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(VaultItemCardDefaults.METADATA_SPACING)
    ) {
        MetadataInfo(
            icon = Icons.Default.AccessTime,
            text = remember(item) { getItemTimeText(item) },
            color = contentColor.copy(alpha = VaultItemCardDefaults.META_ALPHA)
        )

        if (item is TimelineItem.Task && item.deadlineDate != null) {
            MetadataInfo(
                icon = Icons.Default.Flag,
                text = remember(item.deadlineDate) { "Due ${item.deadlineDate.format(DateFormatter)}" },
                color = if (isMissed) MissedColor else accentColor.copy(alpha = VaultItemCardDefaults.META_ALPHA),
                isBold = true
            )
        }
    }
}

@Composable
private fun MetadataInfo(
    icon: ImageVector,
    text: String,
    color: Color,
    isBold: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(VaultItemCardDefaults.ICON_SIZE_SMALL),
            tint = color
        )
        Spacer(modifier = Modifier.width(VaultItemCardDefaults.META_ICON_TEXT_SPACING))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isBold) FontWeight.Black else FontWeight.Bold,
            color = color
        )
    }
}

private fun getItemTimeText(item: TimelineItem): String {
    val dateStr = item.date.format(DateFormatter)
    val timeStr = when (item) {
        is TimelineItem.Event -> if (item.isAllDay) "All Day" else item.startTime.format(
            TimeFormatter
        )

        is TimelineItem.Task -> item.taskTime?.format(TimeFormatter) ?: "Anytime"
    }
    return "$dateStr • $timeStr"
}

private fun getAccentColor(
    item: TimelineItem,
    isTask: Boolean,
    isCompleted: Boolean,
    isMissed: Boolean
): Color = when {
    !isTask -> EventColor
    isCompleted -> CompletedColor
    isMissed -> MissedColor
    item is TimelineItem.Task -> getPriorityColor(item.priority)
    else -> Color.Gray
}

@Composable
private fun getContainerColor(
    item: TimelineItem,
    isTask: Boolean,
    isCompleted: Boolean,
    isMissed: Boolean,
    isDark: Boolean
): Color = when {
    !isTask -> if (isDark) EventColorDark else EventColorLight
    isCompleted -> if (isDark) CompletedContainerDark else CompletedContainerLight
    isMissed -> {
        val alpha =
            if (isDark) VaultItemCardDefaults.MISSED_ALPHA_DARK else VaultItemCardDefaults.MISSED_ALPHA_LIGHT
        MissedColor.copy(alpha = alpha)
    }

    item is TimelineItem.Task -> getPriorityContainerColor(item.priority, isDark)
    else -> MaterialTheme.colorScheme.surface
}

@Composable
private fun getContentColor(
    isTask: Boolean,
    isCompleted: Boolean,
    isMissed: Boolean,
    isDark: Boolean
): Color = when {
    !isTask -> if (isDark) EventContentDark else EventContentLight
    isCompleted -> MaterialTheme.colorScheme.onSurface.copy(alpha = VaultItemCardDefaults.COMPLETED_CONTENT_ALPHA)
    isMissed -> if (isDark) MissedContentDark else MissedColor
    else -> MaterialTheme.colorScheme.onSurface
}
