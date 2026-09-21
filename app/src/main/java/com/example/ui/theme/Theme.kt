package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PS2ColorScheme = darkColorScheme(
  primary = PS2ElectricBlue,
  onPrimary = PS2TextPrimary,
  primaryContainer = PS2BadgeBg,
  onPrimaryContainer = PS2NeonCyan,
  secondary = PS2NeonCyan,
  onSecondary = PS2DeepNavy,
  secondaryContainer = PS2SurfaceElevated,
  onSecondaryContainer = PS2TextPrimary,
  tertiary = PS2BrightGlow,
  background = PS2DeepNavy,
  onBackground = PS2TextPrimary,
  surface = PS2SurfaceDark,
  onSurface = PS2TextPrimary,
  surfaceVariant = PS2SurfaceElevated,
  onSurfaceVariant = PS2TextSecondary,
  outline = PS2SurfaceBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = PS2ColorScheme,
    typography = Typography,
    content = content
  )
}

