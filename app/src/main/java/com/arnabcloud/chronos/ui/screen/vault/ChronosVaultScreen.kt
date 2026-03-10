package com.arnabcloud.chronos.ui.screen.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arnabcloud.chronos.model.TimelineItem
import com.arnabcloud.chronos.ui.components.ItemDetailDialog
import com.arnabcloud.chronos.ui.components.VaultItemCard
import com.arnabcloud.chronos.ui.components.VaultSummary
import com.arnabcloud.chronos.viewmodel.ChronosViewModel

private object VaultScreenDefaults {
    val SCREEN_PADDING = 16.dp
    val HEADLINE_BOTTOM_PADDING = 24.dp
    val ITEM_SPACING = 12.dp
}

@Composable
fun ChronosVaultScreen(viewModel: ChronosViewModel) {
    val items by viewModel.items.collectAsState()
    var selectedItem by remember { mutableStateOf<TimelineItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(all = VaultScreenDefaults.SCREEN_PADDING)
    ) {
        Text(
            text = "My Tasks & Events",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = VaultScreenDefaults.HEADLINE_BOTTOM_PADDING)
        )

        VaultSummary(items = items)

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(space = VaultScreenDefaults.ITEM_SPACING),
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
