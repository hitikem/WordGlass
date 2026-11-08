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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wordglass.app.core.components.GlassCard
import com.wordglass.app.core.design.WgRadius
import com.wordglass.app.core.design.WgTheme
import com.wordglass.app.core.design.WgType
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Study calendar: shows which days the user studied this month.
 * Green = studied, gray = not studied, today has a blue ring.
 */
@Composable
fun StudyCalendarCard(
    studiedDays: Set<Int>,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    val today = LocalDate.now()
    val currentMonth = YearMonth.now()
    val firstDay = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val startDayOfWeek = firstDay.dayOfWeek.value % 7 // 0=Sun, 1=Mon, ...

    GlassCard(
        backdrop = com.wordglass.app.ui.LocalGlassBackdrop.current,
        modifier = modifier.fillMaxWidth(),
        cornerRadius = WgRadius.card,
        contentPadding = PaddingValues(18.dp),
        elevation = 10.dp,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("学习日历", style = WgType.eyebrow, color = colors.inkTertiary)
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.CHINESE)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.inkSecondary,
                )
            }
            Spacer(Modifier.height(12.dp))

            // Weekday headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                listOf("日", "一", "二", "三", "四", "五", "六").forEach {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.inkTertiary,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(Modifier.height(6.dp))

            // Calendar grid
            var dayCounter = 1
            val totalRows = ((daysInMonth + startDayOfWeek + 6) / 7)
            for (row in 0 until totalRows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        if ((row == 0 && col < startDayOfWeek) || dayCounter > daysInMonth) {
                            Box(Modifier.weight(1f).height(32.dp))
                        } else {
                            val day = dayCounter
                            val isToday = day == today.dayOfMonth
                            val isStudied = day in studiedDays
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isStudied) colors.success.copy(alpha = 0.25f)
                                            else Color.Transparent
                                        )
                                        .border(
                                            if (isToday) 2.dp else 0.dp,
                                            if (isToday) colors.blue else Color.Transparent,
                                            CircleShape,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "$day",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isStudied) colors.success
                                        else if (isToday) colors.blue
                                        else colors.inkTertiary,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    )
                                }
                            }
                            dayCounter++
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "本月学习 ${studiedDays.size} 天",
                style = MaterialTheme.typography.labelSmall,
                color = colors.inkTertiary,
            )
        }
    }
}
