package com.wordglass.app.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wordglass.app.core.design.WgTheme

/**
 * Warm paper backdrop.
 *
 * No colour glows and no dot grid: the new design gets its depth from typography and hairline
 * rules, so the background stays a quiet, almost flat paper field. It is still static (never
 * animated) because any glass surface on top samples it.
 */
@Composable
fun AmbientBackground(modifier: Modifier = Modifier) {
    val c = WgTheme.colors
    Canvas(modifier) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(c.background, c.backgroundAlt, c.background),
                startY = 0f,
                endY = size.height,
            ),
        )

        // An extremely soft warm vignette keeps the top of the page from reading as pure grey.
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    c.backgroundDeep.copy(alpha = if (c.isDark) 0.30f else 0.55f),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.5f, -size.height * 0.10f),
                radius = size.width * 1.15f,
            ),
        )
    }
}
