package com.example.comp1786_su25.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary80,
    onPrimary = TextOnColor,
    primaryContainer = Primary20,
    onPrimaryContainer = Primary80,
    secondary = Secondary80,
    onSecondary = TextOnColor,
    secondaryContainer = Secondary20,
    onSecondaryContainer = Secondary80,
    tertiary = Warning80,
    onTertiary = TextOnColor,
    tertiaryContainer = Warning20,
    onTertiaryContainer = Warning80,
    error = Error80,
    onError = TextOnColor,
    errorContainer = Error20,
    onErrorContainer = Error80,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = CardBorder,
    outlineVariant = SurfaceVariant,
    scrim = CardShadow
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary40,
    onPrimary = TextPrimary,
    primaryContainer = Primary80,
    onPrimaryContainer = Primary20,
    secondary = Secondary40,
    onSecondary = TextPrimary,
    secondaryContainer = Secondary80,
    onSecondaryContainer = Secondary20,
    tertiary = Warning40,
    onTertiary = TextPrimary,
    tertiaryContainer = Warning80,
    onTertiaryContainer = Warning20,
    error = Error40,
    onError = TextPrimary,
    errorContainer = Error80,
    onErrorContainer = Error20,
    background = TextPrimary,
    onBackground = TextOnColor,
    surface = OnSurfaceVariant,
    onSurface = TextOnColor,
    surfaceVariant = OnSurface,
    onSurfaceVariant = SurfaceVariant,
    outline = OnSurfaceVariant,
    outlineVariant = OnSurface,
    scrim = CardShadow
)

@Composable
fun COMP1786_SU25Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}