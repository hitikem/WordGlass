package com.wordglass.app.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.PrismalGlassSurface
import com.styropyr0.prismal.shapes.PrismalRoundedRectangle
import com.wordglass.app.core.design.WgRadius
import com.wordglass.app.core.design.WgTheme

@Composable
fun GlassCard(
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = WgRadius.card,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    fill: Color = WgTheme.colors.glassFill,
    tint: Color = WgTheme.colors.glassTint,
    elevation: Dp = 14.dp,
    overlay: Brush? = null,
    liquid: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = WgTheme.colors
    val shape = remember(cornerRadius) { PrismalRoundedRectangle(cornerRadius) }
    val density = LocalDensity.current
    val refractionHeight = with(density) { 18.dp.toPx() }
    val refractionAmount = with(density) { 22.dp.toPx() }

    Box(
        modifier = modifier.shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = colors.shadow,
            spotColor = colors.shadow,
        ),
    ) {
        // The glass layer fills the measured card, but does not participate in sizing.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape),
        ) {
            if (liquid) {
                PrismalGlassSurface(
                    backdrop = backdrop,
                    modifier = Modifier.matchParentSize(),
                    shape = { shape },
                    blurRadius = 10.dp,
                    surfaceColor = fill,
                    tint = tint,
                    refractionHeightPx = refractionHeight,
                    refractionAmountPx = refractionAmount,
                    adaptiveLuminance = false,
                )
            } else {
                // Static frosted layer: visually close on a smooth backdrop, but without a
                // RenderEffect per card. This is the main scrolling/dragging performance guard.
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(fill)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (colors.isDark) 0.05f else 0.18f),
                                    Color.Transparent,
                                    colors.blue.copy(alpha = if (colors.isDark) 0.04f else 0.025f),
                                ),
                            ),
                        ),
                )
            }

            if (overlay != null) {
                Box(Modifier.matchParentSize().background(overlay))
            }

            Box(
                Modifier
                    .matchParentSize()
                    .border(
                        width = 0.8.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                colors.glassStroke,
                                colors.glassStroke.copy(alpha = 0.12f),
                                colors.glassStroke.copy(alpha = 0.35f),
                            ),
                        ),
                        shape = shape,
                    ),
            )
        }

        // This layer determines the card size for variable-height content.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content,
        )
    }
}

@Composable
fun AccentGlassCard(
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = WgRadius.hero,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val c = WgTheme.colors
    GlassCard(
        backdrop = backdrop,
        modifier = modifier,
        cornerRadius = cornerRadius,
        contentPadding = contentPadding,
        fill = if (c.isDark) Color(0xED174FB8) else Color(0xF02F6BFF),
        tint = Color.White.copy(alpha = 0.16f),
        elevation = 20.dp,
        liquid = true,
        overlay = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.18f),
                Color.Transparent,
                c.blueDeep.copy(alpha = 0.30f),
            ),
        ),
        content = content,
    )
}

@Composable
fun GlassIconButton(
    backdrop: PrismalBackdrop,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tint: Color = WgTheme.colors.ink,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        GlassCard(
            backdrop = backdrop,
            modifier = Modifier.matchParentSize(),
            cornerRadius = size / 2,
            contentPadding = PaddingValues(0.dp),
            fill = WgTheme.colors.glassStrongFill,
            elevation = 8.dp,
        ) {
            Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = Modifier.size(size * 0.44f),
                )
            }
        }
    }
}

@Composable
fun GlassChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    val colors = WgTheme.colors
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (selected) colors.blue else colors.glassFill)
            .border(
                0.8.dp,
                if (selected) Color.Transparent else colors.glassStroke.copy(alpha = 0.55f),
                shape,
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 13.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                leading()
                androidx.compose.foundation.layout.Spacer(Modifier.size(6.dp))
            }
            androidx.compose.material3.Text(
                text = text,
                color = if (selected) Color.White else colors.inkSecondary,
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
fun glassBorderBrush(): Brush {
    val c = WgTheme.colors
    return Brush.linearGradient(
        colors = listOf(
            c.glassStroke,
            c.glassStroke.copy(alpha = 0.08f),
            c.glassStroke.copy(alpha = 0.45f),
        ),
    )
}

@Composable
fun roundedGlassShape(radius: Dp): Shape = RoundedCornerShape(radius)
