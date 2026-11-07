package com.wordglass.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wordglass.app.core.components.GlassCard
import com.wordglass.app.core.design.WgRadius
import com.wordglass.app.core.design.WgTheme
import com.wordglass.app.core.design.WgType
import com.wordglass.app.model.DashboardStats

/**
 * Learning plan: shows how many days to finish all words and lets the user set a target.
 */
@Composable
fun PlanCard(
    stats: DashboardStats,
    onSetPlan: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    var planDialog by remember { mutableStateOf(false) }

    GlassCard(
        backdrop = com.wordglass.app.ui.LocalGlassBackdrop.current,
        modifier = modifier.fillMaxWidth(),
        cornerRadius = WgRadius.card,
        contentPadding = PaddingValues(18.dp),
        elevation = 10.dp,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "学习计划",
                    style = WgType.eyebrow,
                    color = colors.inkTertiary,
                )
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .border(1.dp, colors.divider, RoundedCornerShape(50))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { planDialog = true }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = if (stats.planDays > 0) "修改计划" else "制定计划",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.inkSecondary,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            if (stats.planDays > 0) {
                // Active plan
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${stats.estimatedDaysToFinish}",
                        style = WgType.numericHero,
                        color = colors.ink,
                    )
                    Text(
                        text = " 天完成",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.inkSecondary,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "目标 ${stats.planDays} 天 · 每天约 ${stats.planDailyWords} 词",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkSecondary,
                )
            } else {
                Text(
                    text = "还没制定计划",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.ink,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "制定一个目标，每天学一点，慢慢变强。",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkSecondary,
                )
            }

            Spacer(Modifier.height(12.dp))
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(colors.divider, RoundedCornerShape(50)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(stats.planProgress)
                        .height(6.dp)
                        .background(colors.blue, RoundedCornerShape(50)),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "已学 ${stats.totalLearned} / ${stats.totalWords} 词",
                style = MaterialTheme.typography.labelSmall,
                color = colors.inkTertiary,
            )
        }
    }

    if (planDialog) {
        PlanDialog(
            totalWords = stats.totalWords,
            currentDays = stats.planDays,
            onDismiss = { planDialog = false },
            onSelect = {
                onSetPlan(it)
                planDialog = false
            },
        )
    }
}

@Composable
private fun PlanDialog(
    totalWords: Int,
    currentDays: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    val colors = WgTheme.colors
    val options = listOf(7, 14, 30, 60, 90, 120)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceElevated,
        titleContentColor = colors.ink,
        textContentColor = colors.inkSecondary,
        title = {
            Text(text = "制定学习计划", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "共 $totalWords 词，选择每天的学词量：",
                    style = MaterialTheme.typography.bodyMedium,
                )
                options.forEach { days ->
                    val daily = totalWords / days
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (currentDays == days) colors.blue.copy(alpha = 0.10f)
                                else Color.Transparent,
                            )
                            .border(
                                1.dp,
                                if (currentDays == days) colors.blue else colors.divider,
                                RoundedCornerShape(10.dp),
                            )
                            .clickable { onSelect(days) }
                            .padding(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$days 天",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.ink,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "每天约 $daily 词",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkSecondary,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭", color = colors.inkSecondary)
            }
        },
    )
}
