package com.wordglass.app.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.wordglass.app.core.design.WgTheme

/**
 * Flat paper/ink backdrop.
 *
 * Deliberately a single flat fill: the previous radial-gradient version cost several seconds of
 * first-frame GPU time on software-GL devices (emulators), which triggered ANRs. Depth now comes
 * from type, hairlines and spacing instead of a full-screen gradient.
 */
@Composable
fun AmbientBackground(modifier: Modifier = Modifier) {
    val c = WgTheme.colors
    androidx.compose.foundation.layout.Box(
        modifier.fillMaxSize().background(c.background),
    )
}

/** Vertical wash used sparingly behind hero areas. */
@Composable
fun softWash(alpha: Float = 0.06f): Color {
    val c = WgTheme.colors
    return if (c.isDark) Color.White.copy(alpha = alpha) else c.ink.copy(alpha = alpha)
}
