package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = WhiteBackground,
    primaryContainer = IndigoLightBg,
    onPrimaryContainer = IndigoDark,
    background = WhiteBackground,
    onBackground = TextDark,
    surface = WhiteBackground,
    onSurface = TextDark,
    surfaceVariant = WhiteBackground,
    onSurfaceVariant = TextLight,
    outline = BorderLight,
    outlineVariant = BorderLight,
    error = AccentRed,
    onError = WhiteBackground
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkIndigoPrimary,
    onPrimary = DarkBackground,
    primaryContainer = DarkSurface,
    onPrimaryContainer = DarkIndigoPrimary,
    background = DarkBackground,
    onBackground = DarkTextDark,
    surface = DarkSurface,
    onSurface = DarkTextDark,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkTextLight,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = AccentRed,
    onError = DarkBackground
)

val MinimalShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp)
)

@Composable
fun ExpenseSplitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = MinimalShapes,
        content = content
    )
}
