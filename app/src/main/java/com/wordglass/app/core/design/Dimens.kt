package com.wordglass.app.core.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object WgSpace {
    val xxs = 4.dp
    val xs = 6.dp
    val sm = 10.dp
    val md = 14.dp
    val lg = 18.dp
    val xl = 22.dp
    val xxl = 28.dp
    val huge = 36.dp
    val screen = 20.dp
    val navReserve = 138.dp
}

object WgRadius {
    val chip = 14.dp
    val small = 16.dp
    val card = 24.dp
    val large = 30.dp
    val hero = 34.dp
    val nav = 30.dp
}

object WgSize {
    val touch = 44.dp
    val navHeight = 74.dp
    val cardMin = 330.dp
    val hero = 246.dp
    val stat = 104.dp
}

val WgShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(WgRadius.small),
    medium = RoundedCornerShape(WgRadius.card),
    large = RoundedCornerShape(WgRadius.large),
    extraLarge = RoundedCornerShape(WgRadius.hero),
)
