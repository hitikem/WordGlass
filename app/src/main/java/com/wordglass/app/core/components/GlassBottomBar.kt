package com.wordglass.app.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import kotlin.math.roundToInt

data class WgTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val WgTabs = listOf(
    WgTab("home", "首页", Icons.Rounded.Home),
    WgTab("study", "背单词", Icons.Rounded.Style),
    WgTab("library", "复习", Icons.Rounded.AutoStories),
    WgTab("profile", "我的", Icons.Rounded.Person),
)

/**
 * Native bottom navigation.
 *
 * No floating glass, no liquid lens: a flat bar separated from content by one hairline, with a
 * pill that slides behind the selected label. Movement is a short spring on a single offset, so
 * it costs one layout pass and stays smooth on low-end hardware.
 */
@Composable
fun GlassBottomBar(
    selectedRoute: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    val selectedIndex = WgTabs.indexOfFirst { it.route == selectedRoute }.coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .navigationBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.divider),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 12.dp),
        ) {
            // Sliding selection pill: one animated offset, no per-frame relayout.
            val pillWidth = 1f / WgTabs.size
            val pillOffset by animateDpAsState(
                targetValue = (selectedIndex * 100).dp,
                animationSpec = spring(dampingRatio = 0.86f, stiffness = 900f),
                label = "navPill",
            )
            androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth()) {
                val itemWidth = maxWidth / WgTabs.size
                Box(
                    modifier = Modifier
                        .offset { IntOffset((pillWidth * itemWidth.toPx() * 0f).roundToInt(), 0) }
                        .offset {
                            IntOffset(
                                x = (itemWidth.toPx() * selectedIndex).roundToInt(),
                                y = 0,
                            )
                        }
                        .size(width = itemWidth, height = 34.dp)
                        .align(Alignment.CenterStart)
                        .background(
                            colors.ink.copy(alpha = 0.055f),
                            RoundedCornerShape(50),
                        ),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    WgTabs.forEachIndexed { index, tab ->
                        val selected = index == selectedIndex
                        val tint by animateColorAsState(
                            targetValue = if (selected) colors.ink else colors.inkTertiary,
                            animationSpec = tween(160),
                            label = "navTint",
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .testTag("tab_${tab.route}")
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    role = Role.Tab,
                                    onClick = {
                                        if (index != selectedIndex) onSelect(tab.route)
                                    },
                                )
                                .semantics {
                                    this.selected = selected
                                    role = Role.Tab
                                    contentDescription = tab.label
                                    onClick(label = tab.label) {
                                        if (index != selectedIndex) onSelect(tab.route)
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
                            Spacer(Modifier.size(3.dp))
                            Text(
                                text = tab.label,
                                color = tint,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}
