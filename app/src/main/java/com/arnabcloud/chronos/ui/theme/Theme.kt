package com.arnabcloud.chronos.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// Accent Colors
val BlueAccent = Color(0xFF2196F3)
val GreenAccent = Color(0xFF4CAF50)
val RedAccent = Color(0xFFF44336)
val OrangeAccent = Color(0xFFFF9800)
val PurpleAccent = Color(0xFF9C27B0)

@Composable
fun ChronosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    compactMode: Boolean = false,
    accentColor: String = "Default",
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    val baseColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && accentColor == "Default" -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val colorScheme = if (accentColor != "Default") {
        val customPrimary = when (accentColor) {
            "Blue" -> BlueAccent
            "Green" -> GreenAccent
            "Red" -> RedAccent
            "Orange" -> OrangeAccent
            "Purple" -> PurpleAccent
            else -> baseColorScheme.primary
        }
        baseColorScheme.copy(primary = customPrimary)
    } else {
        baseColorScheme
    }

    val typography = if (compactMode) {
        Typography(
            bodyLarge = AppTypography.bodyLarge.copy(fontSize = 14.sp, lineHeight = 20.sp),
            bodyMedium = AppTypography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 16.sp),
            titleLarge = AppTypography.titleLarge.copy(fontSize = 18.sp, lineHeight = 24.sp),
            labelLarge = AppTypography.labelLarge.copy(fontSize = 12.sp)
        )
    } else {
        AppTypography
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = Shapes,
        content = content
    )
}
