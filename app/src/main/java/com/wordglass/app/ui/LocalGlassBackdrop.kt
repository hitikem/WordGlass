package com.wordglass.app.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.styropyr0.prismal.PrismalBackdrop

/**
 * The single recorded backdrop shared by every glass surface in the prototype.
 * Provided once at the app root so deep study cards stay free of plumbing.
 */
val LocalGlassBackdrop = staticCompositionLocalOf<PrismalBackdrop> {
    error("LocalGlassBackdrop was not provided")
}
