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
 * Static blue-white atmosphere. Deliberately not animated: it is the sampled backdrop for every
 * glass surface, so keeping it stable avoids re-recording the backdrop on every frame.
 */
@Composable
fun AmbientBackground(modifier: Modifier = Modifier) {
    val c = WgTheme.colors
    val glowBlue = c.blue.copy(alpha = if (c.isDark) 0.34f else 0.20f)
    val glowCyan = c.cyan.copy(alpha = if (c.isDark) 0.18f else 0.22f)
    val glowMint = c.mint.copy(alpha = if (c.isDark) 0.10f else 0.13f)
    val grid = c.ink.copy(alpha = if (c.isDark) 0.055f else 0.032f)

    Canvas(modifier) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(c.background, c.backgroundAlt, c.background),
                startY = 0f,
                endY = size.height,
            ),
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glowBlue, Color.Transparent),
                center = Offset(size.width * 0.06f, size.height * 0.04f),
                radius = size.width * 0.95f,
            ),
            radius = size.width * 0.95f,
            center = Offset(size.width * 0.06f, size.height * 0.04f),
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glowCyan, Color.Transparent),
                center = Offset(size.width * 0.98f, size.height * 0.28f),
                radius = size.width * 0.72f,
            ),
            radius = size.width * 0.72f,
            center = Offset(size.width * 0.98f, size.height * 0.28f),
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glowMint, Color.Transparent),
                center = Offset(size.width * 0.22f, size.height * 0.92f),
                radius = size.width * 0.85f,
            ),
            radius = size.width * 0.85f,
            center = Offset(size.width * 0.22f, size.height * 0.92f),
        )

        // Very faint dot grid adds depth without an image asset or per-frame cost.
        val step = 42.dp.toPx()
        var x = step / 2f
        while (x < size.width) {
            var y = step / 2f
            while (y < size.height) {
                drawCircle(color = grid, radius = 0.9.dp.toPx(), center = Offset(x, y))
                y += step
            }
            x += step
        }

        // A restrained horizon line keeps the large background from feeling empty.
        drawLine(
            color = c.ink.copy(alpha = if (c.isDark) 0.035f else 0.022f),
            start = Offset(0f, size.height * 0.72f),
            end = Offset(size.width, size.height * 0.60f),
            strokeWidth = 1.dp.toPx(),
        )
    }
}
