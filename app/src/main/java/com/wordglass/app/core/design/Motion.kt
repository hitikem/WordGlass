package com.wordglass.app.core.design

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

object WgMotion {
    const val screenIn = 420
    const val screenOut = 220
    const val flip = 520
    const val reveal = 260

    val standardEase = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val exitEase = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)

    fun <T> springy() = spring<T>(
        dampingRatio = 0.78f,
        stiffness = Spring.StiffnessMediumLow,
    )

    fun <T> gentle() = spring<T>(
        dampingRatio = 0.9f,
        stiffness = Spring.StiffnessLow,
    )

    fun <T> snappy() = spring<T>(
        dampingRatio = 0.72f,
        stiffness = 620f,
    )
}
