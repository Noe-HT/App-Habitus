package com.app.habitus.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightHabitusColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = White,
    secondary = PrimaryGreen,
    onSecondary = White,
    tertiary = PrimaryPurple,
    onTertiary = White,

    background = AppBackground,
    onBackground = TextPrimary,

    surface = CardBackground,
    onSurface = TextPrimary,

    surfaceVariant = CardBackgroundSoft,
    onSurfaceVariant = TextSecondary,

    outline = BorderColor,
    error = ErrorSoft,
    onError = White
)

private val DarkHabitusColorScheme = darkColorScheme(
    primary = PrimaryBlueDark,
    onPrimary = White,
    secondary = PrimaryGreenDark,
    onSecondary = White,
    tertiary = PrimaryPurple,
    onTertiary = White,

    background = AppBackgroundDark,
    onBackground = TextPrimaryDark,

    surface = CardBackgroundDark,
    onSurface = TextPrimaryDark,

    surfaceVariant = CardBackgroundSoftDark,
    onSurfaceVariant = TextSecondaryDark,

    outline = BorderColorDark,
    error = ErrorSoft,
    onError = White
)

@Composable
fun HabitusTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkHabitusColorScheme else LightHabitusColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Iconos oscuros de la barra de estado solo tienen sentido sobre
            // fondo claro; en modo oscuro deben ser claros.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                !useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}