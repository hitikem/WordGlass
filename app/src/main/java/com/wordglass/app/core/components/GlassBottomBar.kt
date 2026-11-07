package com.wordglass.app.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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

// FlClash constants (prism_ui/navigation_dock.dart)
private const val BAR_HEIGHT = 62f
private const val BAR_PADDING = 4f
private const val EDGE_MARGIN = 21f
private const val SHADOW_ROOM = 8f
private const val LENS_GROWTH = 8f

/**
 * FlClash-style liquid dock.
 *
 * Pure Compose rendering (no AGSL/RenderEffect) so it runs on software-GL devices.
 * The droplet is a water-tinted capsule: gradient fill + specular ring + highlight.
 * Geometry follows FlClash's _lensRect / _dockLayout formulas.
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
    val barShape = remember { RoundedCornerShape(22.dp) }
    val dropletShape = remember { RoundedCornerShape(31.dp) }

    // The bar is exactly BAR_HEIGHT tall. Do NOT use fillMaxSize on the inner container: inside a
    // wrap-content parent that expands to the full screen and the drag layer would then swallow
    // every touch on the page behind it.
    val barHeight = (BAR_HEIGHT + SHADOW_ROOM * 2).dp
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = EDGE_MARGIN.dp)
            .height(barHeight),
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth().height(barHeight)) {
            val span = maxWidth
            val itemWidthPx = with(density) { (span / WgTabs.size).toPx() }

            val pitch = (span - BAR_PADDING.dp * 2) / WgTabs.size
            val pitchPx = with(density) { pitch.toPx() }
            val lensWidth = pitch + BAR_PADDING.dp * 2
            val lensWidthPx = with(density) { lensWidth.toPx() }
            val lensHeightPx = with(density) { BAR_HEIGHT.dp.toPx() }

            val position = remember { mutableFloatStateOf(selectedIndex.toFloat()) }
            var dragging by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            val activeIndex by remember {
                derivedStateOf {
                    position.floatValue.roundToInt().coerceIn(0, WgTabs.lastIndex)
                }
            }

            val jelly by animateFloatAsState(
                targetValue = if (dragging) 1f else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "jelly",
            )

            LaunchedEffect(selectedIndex) {
                if (!dragging) {
                    animate(
                        initialValue = position.floatValue,
                        targetValue = selectedIndex.toFloat(),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
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

            // Bar: transparent slab with a hairline border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(BAR_HEIGHT.dp)
                    .clip(barShape)
                    .background(colors.navFill.copy(alpha = 0.55f))
                    .border(0.8.dp, colors.glassStroke.copy(alpha = 0.35f), barShape),
            ) {
                Row(Modifier.fillMaxSize()) {
                    WgTabs.forEachIndexed { index, tab ->
                        val selected = index == activeIndex
                        val tint by animateColorAsState(
                            targetValue = if (selected) colors.blue else colors.inkTertiary,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium,
                            ),
                            label = "dockTint",
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
                                modifier = Modifier.size(21.dp),
                            )
                            Spacer(Modifier.size(2.dp))
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
            }

            // Droplet — water-tinted capsule, pure Compose (no AGSL)
            val growthPx = LENS_GROWTH * 2 * jelly
            val lensW = lensWidthPx + growthPx
            val lensH = lensHeightPx + growthPx

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset {
                        IntOffset(
                            x = ((position.floatValue + 0.5f) * pitchPx - lensW / 2f).roundToInt(),
                            y = 0,
                        )
                    }
                    .width(with(density) { lensW.toDp() })
                    .height(with(density) { lensH.toDp() }),
            ) {
                // Water tint: soft cyan-blue gradient fill
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(dropletShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    colors.cyan.copy(alpha = 0.32f),
                                    colors.cyan.copy(alpha = 0.18f),
                                ),
                            ),
                        ),
                )
                // Specular ring
                Box(
                    Modifier
                        .matchParentSize()
                        .border(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.85f),
                                    colors.cyan.copy(alpha = 0.35f),
                                    Color.White.copy(alpha = 0.12f),
                                ),
                            ),
                            dropletShape,
                        ),
                )
                // Highlight
                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 18.dp, top = 12.dp)
                        .size(18.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(Color.White.copy(alpha = 0.50f), Color.Transparent),
                            ),
                            RoundedCornerShape(50),
                        ),
                )
            }

            // Drag layer: confined to the bar itself so page content stays tappable.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(BAR_HEIGHT.dp)
                    .pointerInput(pitchPx, selectedIndex) {
                        val touchSlop = viewConfiguration.touchSlop
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var totalX = 0f
                            var isDrag = false
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
                                    val raw = selectedIndex + totalX / pitchPx
                                    position.floatValue = raw.coerceIn(0f, WgTabs.lastIndex.toFloat())
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
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMedium,
                                                ),
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
