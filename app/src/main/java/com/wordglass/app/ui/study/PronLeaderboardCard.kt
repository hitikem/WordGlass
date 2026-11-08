package com.wordglass.app.ui.study

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wordglass.app.core.components.GlassCard
import com.wordglass.app.core.design.WgRadius
import com.wordglass.app.core.design.WgTheme
import com.wordglass.app.core.design.WgType

/**
 * Pronunciation leaderboard: shows best scores per word and overall stats.
 */
data class PronScore(
    val word: String,
    val bestScore: Int,
    val attempts: Int,
    val lastPracticed: Long,
)

@Composable
fun PronLeaderboardCard(
    scores: List<PronScore>,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    val sorted = scores.sortedByDescending { it.bestScore }
    val avgScore = if (scores.isNotEmpty()) scores.map { it.bestScore }.average().toInt() else 0
    val totalAttempts = scores.sumOf { it.attempts }
    val perfectCount = scores.count { it.bestScore >= 95 }

    GlassCard(
        backdrop = com.wordglass.app.ui.LocalGlassBackdrop.current,
        modifier = modifier.fillMaxWidth(),
        cornerRadius = WgRadius.card,
        contentPadding = PaddingValues(18.dp),
        elevation = 10.dp,
    ) {
        Column {
            Text("跟读排行", style = WgType.eyebrow, color = colors.inkTertiary)
            Spacer(Modifier.height(10.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatCell("平均分", "$avgScore")
                StatCell("总练习", "${totalAttempts}次")
                StatCell("完美发音", "${perfectCount}个")
            }

            Spacer(Modifier.height(16.dp))

            // Top words
            if (sorted.isNotEmpty()) {
                Text("最佳发音 TOP 5", style = MaterialTheme.typography.labelMedium, color = colors.inkSecondary)
                Spacer(Modifier.height(8.dp))

                sorted.take(5).forEachIndexed { index, score ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Rank badge
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    when (index) {
                                        0 -> colors.warning.copy(alpha = 0.2f)
                                        1 -> colors.inkTertiary.copy(alpha = 0.15f)
                                        2 -> colors.warning.copy(alpha = 0.1f)
                                        else -> colors.inkTertiary.copy(alpha = 0.08f)
                                    }
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = when (index) {
                                    0 -> colors.warning
                                    1 -> colors.inkSecondary
                                    2 -> colors.warning
                                    else -> colors.inkTertiary
                                },
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        // Word
                        Text(
                            text = score.word,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.ink,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                        )

                        // Score bar
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(colors.divider),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(score.bestScore / 100f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        when {
                                            score.bestScore >= 85 -> colors.success
                                            score.bestScore >= 60 -> colors.warning
                                            else -> colors.danger
                                        }
                                    ),
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = "${score.bestScore}",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.ink,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            } else {
                Text(
                    text = "还没有跟读记录，快去练习吧！",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun StatCell(label: String, value: String) {
    val colors = WgTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = WgType.numeric,
            color = colors.ink,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.inkTertiary,
        )
    }
}
