package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = AuraIndigo,
    secondary = AuraCoral,
    tertiary = AuraViolet,
    background = AuraSurfaceDark,
    surface = AuraSurfaceDark,
    surfaceContainer = AuraSurfaceContainerDark,
    onBackground = AuraTextOnDark,
    onSurface = AuraTextOnDark,
    onSurfaceVariant = AuraTextMutedOnDark,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AuraIndigo,
    secondary = AuraCoral,
    tertiary = AuraViolet,
    background = AuraSurfaceLight,
    surface = AuraSurfaceLight,
    surfaceContainer = AuraSurfaceContainerLight,
    onBackground = AuraTextOnLight,
    onSurface = AuraTextOnLight,
    onSurfaceVariant = AuraTextMutedOnLight,
  )

@Composable
fun AuraHomeTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
