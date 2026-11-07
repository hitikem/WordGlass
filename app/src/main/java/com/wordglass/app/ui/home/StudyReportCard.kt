package com.wordglass.app.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wordglass.app.core.components.GlassCard
import com.wordglass.app.core.design.WgRadius
import com.wordglass.app.core.design.WgTheme
import com.wordglass.app.core.design.WgType
import com.wordglass.app.model.DashboardStats

/**
 * Study report: weekly rhythm + monthly summary + streak.
 */
@Composable
fun StudyReportCard(
    stats: DashboardStats,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    GlassCard(
        backdrop = com.wordglass.app.ui.LocalGlassBackdrop.current,
        modifier = modifier.fillMaxWidth(),
        cornerRadius = WgRadius.card,
        contentPadding = PaddingValues(18.dp),
        elevation = 10.dp,
    ) {
        Column {
            Text(
                text = "学习报告",
                style = WgType.eyebrow,
                color = colors.inkTertiary,
            )
            Spacer(Modifier.height(10.dp))

            // Weekly bars
            val maxMin = (stats.weeklyMinutes.maxOrNull() ?: 1).coerceAtLeast(1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                stats.weeklyMinutes.forEachIndexed { index, minute ->
                    val frac = minute.toFloat() / maxMin
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height((8 + 46 * frac).dp)
                                .background(
                                    if (index == stats.weeklyMinutes.lastIndex) colors.blue
                                    else colors.ink.copy(alpha = 0.16f),
                                    RoundedCornerShape(3.dp),
                                ),
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            // Dynamic day labels: compute Mon-Sun labels for the last 7 days
            val todayDow = java.time.LocalDate.now().dayOfWeek.value // 1=Mon..7=Sun
            // week[0]=6 days ago, week[6]=today. Label for week[i] = weekday of (today - (6-i) days)
            val dayLabels = (6 downTo 0).map { offset ->
                val day = java.time.LocalDate.now().minusDays(offset.toLong())
                when (day.dayOfWeek.value) {
                    1 -> "一"; 2 -> "二"; 3 -> "三"; 4 -> "四"
                    5 -> "五"; 6 -> "六"; 7 -> "日"; else -> "?"
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                dayLabels.forEachIndexed { index, label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (index == 6) colors.blue else colors.inkTertiary,
                        fontWeight = if (index == 6) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MetricCell("本周学习", "${stats.weeklyMinutes.sum()}", "分钟")
                MetricCell("连续打卡", "${stats.streakDays}", "天")
                MetricCell("已学单词", "${stats.totalLearned}", "词")
                MetricCell("已掌握", "${stats.mastered}", "词")
            }
        }
    }
}

@Composable
private fun MetricCell(label: String, value: String, unit: String) {
    val colors = WgTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = WgType.numeric,
                color = colors.ink,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = " $unit",
                style = MaterialTheme.typography.labelSmall,
                color = colors.inkTertiary,
                modifier = Modifier.padding(bottom = 3.dp),
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.inkSecondary,
        )
    }
}
