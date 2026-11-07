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
import com.styropyr0.prismal.shapes.PrismalCapsule
import com.styropyr0.prismal.shapes.PrismalRoundedRectangle
import com.styropyr0.prismal.sources.prismalGlassLayer
import com.styropyr0.prismal.sources.rememberPrismalGlassLayer
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

// FlClash constants — reverse-engineered from libapp.so + prism_ui/navigation_dock.dart
private const val BAR_HEIGHT = 62f
private const val BAR_PADDING = 4f
private const val EDGE_MARGIN = 21f
private const val SHADOW_ROOM = 8f
private const val LENS_GROWTH = 8f
private const val JELLY_ASPECT = 1.4f
private const val JELLY_STRETCH = 0.2f
private const val PLATTER_ALPHA = 0.85f

/**
 * FlClash NavigationDock — 1:1 port from prism_ui/navigation_dock.dart.
 *
 * _lensRect:
 *   growth = _lensGrowth * 2 * lift
 *   lensWidth  = (width + growth) * (1 + stretch)
 *   lensHeight = (height + growth) * (1 - stretch * _jellyAspect)
 *   rect = Rect.fromLTWH(
 *     (position + 0.5) * pitch - lensWidth / 2,
 *     (height - lensHeight) / 2,
 *     lensWidth, lensHeight
 *   )
 *
 * _dockLayout:
 *   pitch = (span - _barPadding * 2) / count
 *   lens = pitch + _barPadding * 2
 *   inset = _barPadding
 *
 * _Lens platter:
 *   tone = dark ? 4.0 : 90.0
 *   platter = Color(hct.toInt()).withValues(alpha: 0.85)
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
    val barShape = remember { PrismalRoundedRectangle(22.dp) }
    val dropletShape = remember { PrismalCapsule() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = EDGE_MARGIN.dp, vertical = SHADOW_ROOM.dp),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
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
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
                label = "jelly",
            )

            LaunchedEffect(selectedIndex) {
                if (!dragging) {
                    animate(
                        initialValue = position.floatValue,
                        targetValue = selectedIndex.toFloat(),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow,
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

            // Bar content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(BAR_HEIGHT.dp)
                    .clip(barShape)
                    .prismalGlassLayer(navLayer)
                    .background(Color.Transparent)
                    .border(0.8.dp, colors.glassStroke.copy(alpha = 0.25f), barShape),
            ) {
                Row(Modifier.fillMaxSize()) {
                    WgTabs.forEachIndexed { index, tab ->
                        val selected = index == activeIndex
                        val tint by animateColorAsState(
                            targetValue = if (selected) colors.blue else colors.inkTertiary,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow,
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

            // Droplet — FlClash _lensRect formula, no squash during drag
            val growthPx = LENS_GROWTH * 2 * jelly
            val lensW = lensWidthPx + growthPx
            val lensH = lensHeightPx + growthPx

            // Platter color — FlClash _Lens (verified 0xD9 = 0.85*255)
            val platterColor = remember(colors) {
                val base = if (colors.isDark) colors.blue else colors.blueDeep
                val tone = if (colors.isDark) 4f / 100f else 90f / 100f
                Color(
                    red = base.red * (1f - tone) + tone,
                    green = base.green * (1f - tone) + tone,
                    blue = base.blue * (1f - tone) + tone,
                    alpha = PLATTER_ALPHA,
                )
            }

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
                // Water-like liquid glass: transparent, pure colour tint, refracts the nav layer
                com.styropyr0.prismal.PrismalGlassSurface(
                    backdrop = navLayer,
                    modifier = Modifier.fillMaxSize(),
                    shape = { dropletShape },
                    blurRadius = 0.dp,
                    surfaceColor = Color.White.copy(alpha = 0.06f),
                    tint = colors.cyan.copy(alpha = 0.18f),
                    tintAlpha = 0.22f,
                    saturation = 1.6f,
                    refractionHeightPx = with(density) { 18.dp.toPx() },
                    refractionAmountPx = with(density) { 24.dp.toPx() },
                    chromaticAberration = 0.05f,
                ) {
                    Box(Modifier.fillMaxSize())
                }
                // Specular ring
                Box(
                    Modifier
                        .matchParentSize()
                        .border(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.85f),
                                    colors.cyan.copy(alpha = 0.30f),
                                    Color.White.copy(alpha = 0.10f),
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
                                listOf(Color.White.copy(alpha = 0.48f), Color.Transparent),
                            ),
                            RoundedCornerShape(50),
                        ),
                )
            }

            // Full-bar drag gesture
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                                    stiffness = Spring.StiffnessLow,
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
