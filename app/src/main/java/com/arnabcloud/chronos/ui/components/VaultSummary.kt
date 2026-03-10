package com.arnabcloud.chronos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arnabcloud.chronos.model.TimelineItem

object VaultSummaryDefaults {
    val BORDER_WIDTH = 1.dp
    val OUTER_PADDING_BOTTOM = 24.dp
    val INNER_PADDING = 20.dp
    
    const val CONTAINER_ALPHA = 0.4f
    const val BORDER_ALPHA = 0.2f
}

@Composable
fun VaultSummary(
    items: List<TimelineItem>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = VaultSummaryDefaults.OUTER_PADDING_BOTTOM),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = VaultSummaryDefaults.CONTAINER_ALPHA),
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(
            width = VaultSummaryDefaults.BORDER_WIDTH,
            color = MaterialTheme.colorScheme.primary.copy(alpha = VaultSummaryDefaults.BORDER_ALPHA)
        )
    ) {
        Row(
            modifier = Modifier.padding(all = VaultSummaryDefaults.INNER_PADDING),
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
}
