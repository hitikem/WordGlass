package com.wordglass.app.core.design

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.wordglass.app.model.ThemeMode

val LocalWgColors = staticCompositionLocalOf { LightWgColors }

object WgTheme {
    val colors: WgColors
        @Composable
        @ReadOnlyComposable
        get() = LocalWgColors.current
}

private fun colorScheme(colors: WgColors) = if (colors.isDark) {
    darkColorScheme(
        primary = colors.blue,
        onPrimary = Color(0xFF06152D),
        secondary = colors.cyan,
        onSecondary = Color(0xFF06152D),
        tertiary = colors.mint,
        background = colors.background,
        onBackground = colors.ink,
        surface = colors.surface,
        onSurface = colors.ink,
        surfaceVariant = colors.surfaceElevated,
        onSurfaceVariant = colors.inkSecondary,
        outline = colors.inkTertiary,
        outlineVariant = colors.divider,
        error = colors.danger,
        onError = Color.White,
    )
} else {
    lightColorScheme(
        primary = colors.blue,
        onPrimary = Color.White,
        secondary = colors.blueDeep,
        onSecondary = Color.White,
        tertiary = colors.mint,
        background = colors.background,
        onBackground = colors.ink,
        surface = colors.surface,
        onSurface = colors.ink,
        surfaceVariant = colors.surfaceElevated,
        onSurfaceVariant = colors.inkSecondary,
        outline = colors.inkTertiary,
        outlineVariant = colors.divider,
        error = colors.danger,
        onError = Color.White,
    )
}

@Composable
fun WordGlassTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colors = if (dark) DarkWgColors else LightWgColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }

    CompositionLocalProvider(LocalWgColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme(colors),
            typography = WgMaterialTypography,
            shapes = WgShapes,
            content = content,
        )
    }
}
