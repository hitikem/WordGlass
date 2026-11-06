package com.wordglass.app.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.styropyr0.prismal.PrismalGlassSurface
import com.styropyr0.prismal.shapes.PrismalCapsule
import com.styropyr0.prismal.shapes.PrismalRoundedRectangle
import com.styropyr0.prismal.sources.prismalGlassLayer
import com.styropyr0.prismal.sources.rememberPrismalGlassLayer
import com.wordglass.app.core.design.WgMotion
import com.wordglass.app.core.design.WgRadius
import com.wordglass.app.core.design.WgTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

data class WgTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val WgTabs = listOf(
    WgTab("home", "首页", Icons.Rounded.Home),
    WgTab("study", "背单词", Icons.Rounded.Style),
    WgTab("library", "复习词库", Icons.Rounded.AutoStories),
    WgTab("profile", "我的", Icons.Rounded.Person),
)

/**
 * FlClash-inspired mobile navigation dock.
 *
 * The bar is a thin frosted slab; selection is a circular liquid lens that samples a layer
 * containing the bar's icons and labels. The lens is draggable across all four items and
 * springs to the nearest tab. This is an independent Compose implementation using the
 * MIT-licensed Prismal primitives; FlClash's GPL source is not copied.
 */
@Composable
fun GlassBottomBar(
    selectedRoute: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    val selectedIndex = WgTabs.indexOfFirst { it.route == selectedRoute }.coerceAtLeast(0)
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val navLayer = rememberPrismalGlassLayer()
    navLayer.readSamplingState()
    val barShape = remember { PrismalRoundedRectangle(WgRadius.nav) }
    val lensSize = 92.dp
    val lensPx = with(density) { lensSize.toPx() }
    val barHeight = 74.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .height(96.dp),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val itemWidth = maxWidth / WgTabs.size
            val itemWidthPx = with(density) { itemWidth.toPx() }
            val position = remember { mutableFloatStateOf(selectedIndex.toFloat()) }
            var dragging by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            val activeIndex by remember {
                derivedStateOf {
                    position.floatValue.roundToInt().coerceIn(0, WgTabs.lastIndex)
                }
            }
            val stretch by animateFloatAsState(
                targetValue = if (dragging) 1.06f else 1f,
                animationSpec = WgMotion.snappy(),
                label = "lensStretch",
            )

            LaunchedEffect(selectedIndex) {
                if (!dragging) {
                    animate(
                        initialValue = position.floatValue,
                        targetValue = selectedIndex.toFloat(),
                        animationSpec = WgMotion.springy(),
                    ) { value, _ -> position.floatValue = value }
                }
            }

            fun select(index: Int) {
                val safe = index.coerceIn(0, WgTabs.lastIndex)
                if (WgTabs[safe].route != selectedRoute) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSelect(WgTabs[safe].route)
                }
            }

            // The layer the lens samples: bar fill + icons + labels. It is not a glass surface
            // itself, so the dock costs one AGSL pass (the lens) instead of a full-screen blur.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(barHeight)
                    .shadow(
                        elevation = 22.dp,
                        shape = barShape,
                        clip = false,
                        ambientColor = colors.shadow,
                        spotColor = colors.shadow,
                    )
                    .clip(barShape)
                    .prismalGlassLayer(navLayer)
                    .background(colors.navFill)
                    .border(
                        width = 0.8.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                colors.glassStroke,
                                colors.glassStroke.copy(alpha = 0.16f),
                                colors.glassStroke.copy(alpha = 0.42f),
                            ),
                        ),
                        shape = barShape,
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = if (colors.isDark) 0.025f else 0.12f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )

                Row(Modifier.fillMaxSize()) {
                    WgTabs.forEachIndexed { index, tab ->
                        val selected = index == activeIndex
                        val tint by animateColorAsState(
                            targetValue = if (selected) colors.blue else colors.inkTertiary,
                            animationSpec = WgMotion.gentle(),
                            label = "dockTint",
                        )
                        val iconScale by animateFloatAsState(
                            targetValue = if (selected) 1.06f else 1f,
                            animationSpec = WgMotion.snappy(),
                            label = "dockScale",
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .testTag("tab_${tab.route}")
                                .semantics {
                                    this.selected = selected
                                    role = Role.Tab
                                    contentDescription = tab.label
                                    onClick(label = tab.label) {
                                        select(index)
                                        true
                                    }
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier
                                    .size(22.dp)
                                    .graphicsLayer {
                                        scaleX = iconScale
                                        scaleY = iconScale
                                    },
                            )
                            androidx.compose.foundation.layout.Spacer(Modifier.size(3.dp))
                            Text(
                                text = tab.label,
                                color = tint,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                maxLines = 1,
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(0.72f)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    colors.whiteGlass.copy(alpha = 0.50f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )
            }

            // The liquid lens is deliberately outside the bar clip so it can swell above/below.
            PrismalGlassSurface(
                backdrop = navLayer,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset {
                        IntOffset(
                            x = (itemWidthPx * position.floatValue + (itemWidthPx - lensPx) / 2f)
                                .roundToInt(),
                            y = 0,
                        )
                    }
                    .size(lensSize)
                    .graphicsLayer {
                        scaleX = stretch
                        scaleY = if (dragging) 0.96f else 1f
                    },
                shape = { PrismalCapsule() },
                blurRadius = 0.5.dp,
                surfaceColor = Color.White.copy(alpha = if (colors.isDark) 0.06f else 0.10f),
                tint = colors.blue.copy(alpha = 0.16f),
                tintAlpha = 0.18f,
                saturation = 1.35f,
                refractionHeightPx = with(density) { 22.dp.toPx() },
                refractionAmountPx = with(density) { 30.dp.toPx() },
                chromaticAberration = 0.035f,
            ) {
                Box(
                    Modifier
                        .matchParentSize()
                        .border(
                            1.dp,
                            Brush.sweepGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.88f),
                                    colors.cyan.copy(alpha = 0.35f),
                                    Color.White.copy(alpha = 0.12f),
                                    colors.blue.copy(alpha = 0.38f),
                                    Color.White.copy(alpha = 0.88f),
                                ),
                            ),
                            CircleShape,
                        ),
                )
                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 19.dp, top = 14.dp)
                        .size(18.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(Color.White.copy(alpha = 0.48f), Color.Transparent),
                            ),
                            CircleShape,
                        ),
                )
            }

            // Gesture surface above the visuals: tap selects, horizontal drag moves the lens.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(itemWidthPx, selectedIndex) {
                        val touchSlop = viewConfiguration.touchSlop
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var totalX = 0f
                            var isDrag = false
                            var lastHapticIndex = selectedIndex
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                totalX += change.positionChange().x
                                if (!isDrag && abs(totalX) > touchSlop) {
                                    isDrag = true
                                    dragging = true
                                }
                                if (isDrag) {
                                    change.consume()
                                    val raw = selectedIndex + totalX / itemWidthPx
                                    position.floatValue = raw.coerceIn(0f, WgTabs.lastIndex.toFloat())
                                    val hapticIndex = position.floatValue.roundToInt()
                                    if (hapticIndex != lastHapticIndex) {
                                        lastHapticIndex = hapticIndex
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                                if (!change.pressed) {
                                    if (isDrag) {
                                        val target = position.floatValue.roundToInt()
                                        dragging = false
                                        select(target)
                                        scope.launch {
                                            animate(
                                                initialValue = position.floatValue,
                                                targetValue = target.toFloat(),
                                                animationSpec = WgMotion.springy(),
                                            ) { value, _ -> position.floatValue = value }
                                        }
                                    } else {
                                        select((down.position.x / itemWidthPx).toInt())
                                    }
                                    break
                                }
                            }
                        }
                    },
            )
        }
    }
}
