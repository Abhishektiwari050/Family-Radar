package com.example.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = DeepInkBlack,
    secondary = CyberGreen,
    tertiary = PinkGlow,
    background = StarkOffWhite,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = DeepInkBlack,
    onTertiary = Color.White,
    onBackground = DeepInkBlack,
    onSurface = DeepInkBlack
  )

private val LightColorScheme =
  lightColorScheme(
    primary = DeepInkBlack,
    secondary = CyberGreen,
    tertiary = PinkGlow,
    background = StarkOffWhite,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = DeepInkBlack,
    onTertiary = Color.White,
    onBackground = DeepInkBlack,
    onSurface = DeepInkBlack
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is bypassed for strict Neo-Brutalist / Chaos Branding guidelines
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
