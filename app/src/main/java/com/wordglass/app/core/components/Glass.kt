package com.wordglass.app.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.styropyr0.prismal.PrismalBackdrop
import com.wordglass.app.core.design.WgRadius
import com.wordglass.app.core.design.WgTheme

/**
 * Editorial surface.
 *
 * The old glass card is replaced by a flat paper panel with a single hairline edge. It keeps
 * the same signature (`backdrop` is accepted for source compatibility but no longer sampled)
 * so every existing screen inherits the new look without being rewritten.
 */
@Composable
fun GlassCard(
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = WgRadius.card,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    fill: Color = WgTheme.colors.surface,
    tint: Color = Color.Transparent,
    elevation: Dp = 0.dp,
    overlay: Brush? = null,
    liquid: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = WgTheme.colors
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(fill)
            .then(
                if (overlay != null) Modifier.background(overlay) else Modifier,
            )
            .border(width = 1.dp, color = colors.divider, shape = shape),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content,
        )
    }
}

/**
 * Emphasis panel: solid ink block used for the one primary block on a page (hero / CTA).
 * Deliberately monochrome so the accent colour stays rare.
 */
@Composable
fun AccentGlassCard(
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = WgRadius.hero,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val c = WgTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(c.ink),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content,
        )
    }
}

@Composable
fun GlassIconButton(
    backdrop: PrismalBackdrop,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    tint: Color = WgTheme.colors.ink,
) {
    val colors = WgTheme.colors
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.surface)
            .border(1.dp, colors.divider, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size * 0.46f),
        )
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
            .background(if (selected) colors.ink else Color.Transparent)
            .border(
                1.dp,
                if (selected) Color.Transparent else colors.divider,
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
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) {
                leading()
                Spacer(Modifier.size(6.dp))
            }
            Text(
                text = text,
                color = if (selected) colors.background else colors.inkSecondary,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
fun glassBorderBrush(): Brush {
    val c = WgTheme.colors
    return Brush.linearGradient(colors = listOf(c.divider, c.divider))
}
