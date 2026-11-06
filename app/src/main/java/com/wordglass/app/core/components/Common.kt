package com.wordglass.app.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordglass.app.core.design.WgMotion
import com.wordglass.app.core.design.WgRadius
import com.wordglass.app.core.design.WgTheme

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    height: Dp = 54.dp,
    enabled: Boolean = true,
) {
    val colors = WgTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.975f else 1f,
        animationSpec = WgMotion.snappy(),
        label = "primaryPress",
    )
    Box(
        modifier = modifier
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.42f
            }
            .shadow(
                elevation = if (pressed) 5.dp else 14.dp,
                shape = RoundedCornerShape(WgRadius.small),
                clip = false,
                ambientColor = colors.blue.copy(alpha = 0.30f),
                spotColor = colors.blue.copy(alpha = 0.24f),
            )
            .clip(RoundedCornerShape(WgRadius.small))
            .background(
                Brush.linearGradient(
                    listOf(
                        if (colors.isDark) colors.blue else colors.blue,
                        colors.blueDeep,
                    ),
                ),
            )
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(Modifier.width(9.dp))
            }
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val colors = WgTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.blue,
                modifier = Modifier.size(15.dp),
            )
            Spacer(Modifier.width(5.dp))
        }
        Text(
            text = text,
            color = colors.blue,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = WgTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (eyebrow != null) {
                Text(
                    text = eyebrow,
                    style = com.wordglass.app.core.design.WgType.eyebrow,
                    color = colors.blue,
                )
                Spacer(Modifier.height(5.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.ink,
            )
        }
        if (action != null && onAction != null) {
            GhostButton(text = action, onClick = onAction)
        }
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    stroke: Dp = 9.dp,
    color: Color = WgTheme.colors.blue,
    track: Color = WgTheme.colors.blue.copy(alpha = 0.12f),
    content: @Composable () -> Unit = {},
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val strokePx = stroke.toPx()
            val inset = strokePx / 2f
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(this.size.width - strokePx, this.size.height - strokePx),
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
            drawArc(
                color = color.copy(alpha = 0.18f),
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(this.size.width - strokePx, this.size.height - strokePx),
                style = Stroke(width = strokePx * 1.9f, cap = StrokeCap.Round),
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(this.size.width - strokePx, this.size.height - strokePx),
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
        }
        content()
    }
}

@Composable
fun ThinProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    color: Color = WgTheme.colors.blue,
) {
    val c = WgTheme.colors
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(c.ink.copy(alpha = 0.07f)),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0.001f, 1f))
                .clip(RoundedCornerShape(50))
                .background(Brush.horizontalGradient(listOf(color, c.cyan))),
        )
    }
}

@Composable
fun WgSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    val trackColor by animateColorAsState(
        targetValue = if (checked) colors.blue else colors.ink.copy(alpha = 0.12f),
        animationSpec = tween(220),
        label = "switchTrack",
    )
    val thumbX by animateDpAsState(
        targetValue = if (checked) 24.dp else 3.dp,
        animationSpec = WgMotion.springy(),
        label = "switchThumb",
    )
    Box(
        modifier = modifier
            .width(50.dp)
            .height(29.dp)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
            .toggleable(
                value = checked,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .padding(horizontal = 3.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset { IntOffset(thumbX.roundToPx(), 0) }
                .size(23.dp)
                .shadow(4.dp, CircleShape)
                .background(Color.White, CircleShape),
        )
    }
}

@Composable
fun SegmentedGlass(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 42.dp,
) {
    val colors = WgTheme.colors
    BoxWithConstraints(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.ink.copy(alpha = if (colors.isDark) 0.20f else 0.045f))
            .border(0.8.dp, colors.glassStroke.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
    ) {
        val itemWidth = maxWidth / options.size
        val indicatorX by animateDpAsState(
            targetValue = itemWidth * selectedIndex,
            animationSpec = WgMotion.springy(),
            label = "segment",
        )
        Box(
            Modifier
                .offset { IntOffset(indicatorX.roundToPx(), 0) }
                .width(itemWidth)
                .fillMaxHeight()
                .padding(3.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.blue.copy(alpha = 0.96f),
                            colors.blueDeep.copy(alpha = 0.96f),
                        ),
                    ),
                ),
        )
        Row(Modifier.fillMaxWidth().fillMaxHeight()) {
            options.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Tab,
                            onClick = { onSelect(index) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        color = if (index == selectedIndex) Color.White else colors.inkSecondary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
fun BigNumber(
    value: String,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    color: Color = WgTheme.colors.ink,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        Text(
            text = value,
            style = com.wordglass.app.core.design.WgType.numeric,
            color = color,
        )
        if (suffix != null) {
            Text(
                text = suffix,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.58f),
                modifier = Modifier.padding(start = 3.dp, bottom = 4.dp),
            )
        }
    }
}

@Composable
fun IconCircle(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    background: Color = tint.copy(alpha = 0.12f),
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.48f),
        )
    }
}

@Composable
fun VerticalGap(height: Dp) = Spacer(Modifier.height(height))

@Composable
fun HPad(width: Dp) = Spacer(Modifier.width(width))
