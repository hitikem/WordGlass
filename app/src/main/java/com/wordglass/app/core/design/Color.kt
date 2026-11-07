package com.wordglass.app.core.design

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * WordGlass palette: Apple minimalism x editorial.
 *
 * Warm paper, ink and a single olive accent. Blue/violet gradients and heavy glassmorphism are
 * intentionally absent: hierarchy comes from type, spacing and hairlines instead of colour.
 *
 * The legacy token names (`blue`, `cyan`, `mint`) are kept so existing call sites keep
 * compiling, but they now resolve to the olive/orange accent family.
 */
@Immutable
data class WgColors(
    val isDark: Boolean,
    val background: Color,
    val backgroundAlt: Color,
    val backgroundDeep: Color,
    val ink: Color,
    val inkSecondary: Color,
    val inkTertiary: Color,
    val blue: Color,
    val blueDeep: Color,
    val cyan: Color,
    val mint: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val glassFill: Color,
    val glassStrongFill: Color,
    val glassStroke: Color,
    val glassTint: Color,
    val navFill: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val divider: Color,
    val shadow: Color,
    val whiteGlass: Color,
)

val LightWgColors = WgColors(
    isDark = false,
    background = Color(0xFFF7F6F2),
    backgroundAlt = Color(0xFFF1EFE9),
    backgroundDeep = Color(0xFFEAE8E1),
    ink = Color(0xFF20211F),
    inkSecondary = Color(0xFF85847E),
    inkTertiary = Color(0xFFA8A69F),
    blue = Color(0xFF687653),
    blueDeep = Color(0xFF4F5B3E),
    cyan = Color(0xFF8A9776),
    mint = Color(0xFF687653),
    success = Color(0xFF5C7A4A),
    warning = Color(0xFFD98B59),
    danger = Color(0xFFB4553F),
    glassFill = Color(0xFFFFFFFF),
    glassStrongFill = Color(0xFFFFFFFF),
    glassStroke = Color(0xFFEAE8E1),
    glassTint = Color(0x0D687653),
    navFill = Color(0xFFFCFBF8),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFFFFFFF),
    divider = Color(0xFFEAE8E1),
    shadow = Color(0x1420211F),
    whiteGlass = Color(0xFFFFFFFF),
)

val DarkWgColors = WgColors(
    isDark = true,
    background = Color(0xFF171816),
    backgroundAlt = Color(0xFF1C1D1B),
    backgroundDeep = Color(0xFF202220),
    ink = Color(0xFFF2F0EA),
    inkSecondary = Color(0xFF9C9A92),
    inkTertiary = Color(0xFF6E6C66),
    blue = Color(0xFFA3B187),
    blueDeep = Color(0xFF8A9776),
    cyan = Color(0xFF9AA886),
    mint = Color(0xFFA3B187),
    success = Color(0xFF93B07C),
    warning = Color(0xFFE0A272),
    danger = Color(0xFFD37962),
    glassFill = Color(0xFF242622),
    glassStrongFill = Color(0xFF242622),
    glassStroke = Color(0xFF32342F),
    glassTint = Color(0x14A3B187),
    navFill = Color(0xFF1E201D),
    surface = Color(0xFF242622),
    surfaceElevated = Color(0xFF2A2C28),
    divider = Color(0xFF32342F),
    shadow = Color(0x66000000),
    whiteGlass = Color(0xFF242622),
)
