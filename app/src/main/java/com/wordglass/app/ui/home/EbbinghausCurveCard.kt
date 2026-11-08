package com.wordglass.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wordglass.app.core.components.GlassCard
import com.wordglass.app.core.design.WgRadius
import com.wordglass.app.core.design.WgTheme
import com.wordglass.app.core.design.WgType

/**
 * Ebbinghaus forgetting curve visualization.
 * Shows memory retention over time with review points.
 */
@Composable
fun EbbinghausCurveCard(
    retention: Float = 1.0f,
    reviewCount: Int = 0,
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
            Text("记忆曲线", style = WgType.eyebrow, color = colors.inkTertiary)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "艾宾浩斯遗忘曲线",
                style = MaterialTheme.typography.titleMedium,
                color = colors.ink,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "遗忘先快后慢，及时复习能大幅提高记忆保持率",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkTertiary,
            )

            Spacer(Modifier.height(16.dp))

            // Curve chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
            ) {
                val w = size.width
                val h = size.height
                val leftPad = 30.dp.toPx()
                val rightPad = 10.dp.toPx()
                val topPad = 10.dp.toPx()
                val bottomPad = 20.dp.toPx()
                val chartW = w - leftPad - rightPad
                val chartH = h - topPad - bottomPad

                // Draw axes
                drawLine(
                    color = Color(0xFFE0E0E0),
                    start = Offset(leftPad, h - bottomPad),
                    end = Offset(w - rightPad, h - bottomPad),
                    strokeWidth = 1.dp.toPx(),
                )
                drawLine(
                    color = Color(0xFFE0E0E0),
                    start = Offset(leftPad, topPad),
                    end = Offset(leftPad, h - bottomPad),
                    strokeWidth = 1.dp.toPx(),
                )

                // Forgetting curve (without review): exponential decay
                val noReviewPath = Path().apply {
                    moveTo(leftPad, topPad)
                    for (i in 0..20) {
                        val t = i / 20f
                        val x = leftPad + chartW * t
                        val retention = Math.exp(-3.0 * t).toFloat()
                        val y = topPad + chartH * (1f - retention)
                        lineTo(x, y)
                    }
                }
                drawPath(
                    path = noReviewPath,
                    color = Color(0xFFE57373),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )

                // With review curve (spaced repetition): higher retention
                val reviewPath = Path().apply {
                    moveTo(leftPad, topPad)
                    val reviewPoints = listOf(0.15f, 0.35f, 0.6f, 0.85f)
                    for (i in 0..20) {
                        val t = i / 20f
                        val x = leftPad + chartW * t
                        // Base decay
                        var retention = Math.exp(-1.5 * t).toFloat()
                        // Boost at review points
                        reviewPoints.forEach { rp ->
                            if (t >= rp) {
                                val sinceReview = (t - rp) * 5
                                retention = maxOf(retention, (Math.exp(-1.5 * sinceReview)).toFloat() * 0.95f + 0.05f)
                            }
                        }
                        val y = topPad + chartH * (1f - retention)
                        lineTo(x, y)
                    }
                }
                drawPath(
                    path = reviewPath,
                    color = Color(0xFF4CAF50),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )

                // Review point markers
                listOf(0.15f, 0.35f, 0.6f, 0.85f).forEach { rp ->
                    val x = leftPad + chartW * rp
                    drawCircle(
                        color = Color(0xFF4CAF50),
                        radius = 4.dp.toPx(),
                        center = Offset(x, h - bottomPad),
                    )
                }

                // Labels
                // Y axis labels
                drawLine(
                    color = Color(0xFFE0E0E0),
                    start = Offset(leftPad, topPad),
                    end = Offset(leftPad + chartW, topPad),
                    strokeWidth = 0.5.dp.toPx(),
                )
            }

            Spacer(Modifier.height(8.dp))

            // Legend
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(Color(0xFFE57373), "不复习")
                LegendDot(Color(0xFF4CAF50), "科学复习")
                LegendDot(colors.blue, "复习点")
            }

            Spacer(Modifier.height(12.dp))

            // Review schedule
            Text("推荐复习时间点", style = MaterialTheme.typography.labelMedium, color = colors.inkSecondary)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ReviewPoint("第1次", "5分钟后")
                ReviewPoint("第2次", "30分钟后")
                ReviewPoint("第3次", "12小时后")
                ReviewPoint("第4次", "1天后")
                ReviewPoint("第5次", "3天后")
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    val colors = WgTheme.colors
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = color)
        }
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.inkTertiary)
    }
}

@Composable
private fun ReviewPoint(label: String, time: String) {
    val colors = WgTheme.colors
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.ink, fontWeight = FontWeight.SemiBold)
        Text(time, style = MaterialTheme.typography.labelSmall, color = colors.inkTertiary)
    }
}
