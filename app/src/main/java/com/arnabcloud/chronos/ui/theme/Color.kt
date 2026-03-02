package com.arnabcloud.chronos.ui.theme

import androidx.compose.ui.graphics.Color
import com.arnabcloud.chronos.model.Priority

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Vibrant Status Colors
val PriorityHigh = Color(0xFFFF5252)    // Vibrant Red
val PriorityMedium = Color(0xFFFFAB40)  // Vibrant Orange/Amber
val PriorityLow = Color(0xFF69F0AE)     // Vibrant Green
val EventColor = Color(0xFF448AFF)      // Vibrant Blue
val MissedColor = Color(0xFFB00020)     // Deep Red for missed
val CompletedColor = Color(0xFFBDBDBD)  // Muted Grey for completed

// Light Theme Surface Colors
val PriorityHighLight = Color(0xFFFFEBEE)
val PriorityMediumLight = Color(0xFFFFF3E0)
val PriorityLowLight = Color(0xFFE8F5E9)
val EventColorLight = Color(0xFFE3F2FD)

// Dark Theme Surface Colors
val PriorityHighDark = Color(0xFF311B1B)
val PriorityMediumDark = Color(0xFF332414)
val PriorityLowDark = Color(0xFF142918)
val EventColorDark = Color(0xFF102031)

fun getPriorityColor(priority: Priority): Color = when (priority) {
    Priority.HIGH -> PriorityHigh
    Priority.MEDIUM -> PriorityMedium
    Priority.LOW -> PriorityLow
}

fun getPriorityContainerColor(priority: Priority, isDark: Boolean): Color = when (priority) {
    Priority.HIGH -> if (isDark) PriorityHighDark else PriorityHighLight
    Priority.MEDIUM -> if (isDark) PriorityMediumDark else PriorityMediumLight
    Priority.LOW -> if (isDark) PriorityLowDark else PriorityLowLight
}
