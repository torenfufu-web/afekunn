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
    primary = CafeEspressoDark,
    secondary = CafeLatteDark,
    tertiary = CafeCreamDark,
    background = CafeBackgroundDark,
    surface = CafeSurfaceDark,
    onPrimary = CafeOnPrimaryDark,
    onBackground = CafeOnBackgroundDark,
    onSurface = CafeOnSurfaceDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CafeEspresso,
    secondary = CafeLatte,
    tertiary = CafeCream,
    background = CafeBackgroundLight,
    surface = CafeSurfaceLight,
    onPrimary = CafeOnPrimaryLight,
    onBackground = CafeOnBackgroundLight,
    onSurface = CafeOnSurfaceLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Curator theme defaults to our custom Cafe colors by default!
  dynamicColor: Boolean = false,
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
