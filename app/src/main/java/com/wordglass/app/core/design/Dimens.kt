package com.wordglass.app.core.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Spacing scale: an 4pt-based rhythm with generous editorial gaps.
 */
object WgSpace {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 28.dp
    val xxl = 36.dp
    val huge = 48.dp
    val screen = 24.dp
    val navReserve = 132.dp
}

/**
 * Radius system: restrained. Editorial layouts lean on hairlines and whitespace, not pills.
 */
object WgRadius {
    val chip = 999.dp
    val small = 8.dp
    val card = 12.dp
    val large = 16.dp
    val hero = 20.dp
    val nav = 0.dp
}

object WgSize {
    val touch = 44.dp
    val navHeight = 64.dp
    val cardMin = 320.dp
    val hero = 220.dp
    val stat = 96.dp
    /** Hairline width used for every divider and card edge. */
    val hairline = 1.dp
}

val WgShapes = Shapes(
    extraSmall = RoundedCornerShape(WgRadius.small),
    small = RoundedCornerShape(WgRadius.small),
    medium = RoundedCornerShape(WgRadius.card),
    large = RoundedCornerShape(WgRadius.large),
    extraLarge = RoundedCornerShape(WgRadius.hero),
)
