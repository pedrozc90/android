package com.pedrozc90.prototype.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB9D6FF),
    onPrimary = Color(0xFF07203A),
    primaryContainer = Color(0xFF2D4F73),
    onPrimaryContainer = Color(0xFFFFFFFF),

    secondary = Color(0xFFABC8E6),
    onSecondary = Color(0xFF06202D),
    secondaryContainer = Color(0xFF274A5F),
    onSecondaryContainer = Color(0xFFFFFFFF),

    tertiary = Color(0xFFFFB4AB),
    onTertiary = Color(0xFF3B0E0B),
    tertiaryContainer = Color(0xFF7A2F2A),
    onTertiaryContainer = Color(0xFFFFFFFF),

    background = Color(0xFF0E1113),
    onBackground = Color(0xFFECEFF1),

    surface = Color(0xFF111417),
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = Color(0xFF1B2228),
    onSurfaceVariant = Color(0xFFBFC7CE),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),

    outline = Color(0xFF8F9AA5)
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,

    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,

    background = Background,
    onBackground = OnBackground,

    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,

    error = Error,
    onError = OnError,

    outline = Outline
)

@Composable
fun PrototypeTheme(
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
