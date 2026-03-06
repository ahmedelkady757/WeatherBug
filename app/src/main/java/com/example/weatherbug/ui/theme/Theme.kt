package com.example.weatherbug.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary            = WeatherBlue,
    onPrimary          = NeutralWhite,
    primaryContainer   = WeatherBlueLight,
    onPrimaryContainer = WeatherBlue,

    secondary          = WeatherTeal,
    onSecondary        = NeutralWhite,

    background         = NeutralOffWhite,
    onBackground       = NeutralBlack,

    surface            = NeutralWhite,
    onSurface          = NeutralBlack,
    surfaceVariant     = NeutralLightGray,
    onSurfaceVariant   = NeutralDarkGray,

    error              = ErrorRed,
    onError            = NeutralWhite,

    outline            = NeutralMidGray
)

private val DarkColorScheme = darkColorScheme(
    primary            = WeatherBlueDark,
    onPrimary          = NeutralBlack,
    primaryContainer   = WeatherBlue,
    onPrimaryContainer = WeatherBlueLight,

    secondary          = WeatherTealDark,
    onSecondary        = NeutralBlack,

    background         = DarkBackground,
    onBackground       = NeutralOffWhite,

    surface            = DarkSurface,
    onSurface          = NeutralOffWhite,
    surfaceVariant     = DarkSurfaceVariant,
    onSurfaceVariant   = NeutralLightGray,

    error              = ErrorRedDark,
    onError            = NeutralBlack,

    outline            = NeutralMidGray
)


@Composable
fun WeatherBugTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = WeatherBugTypography,
        content     = content
    )
}