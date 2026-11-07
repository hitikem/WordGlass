package com.wordglass.app.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.styropyr0.prismal.PrismalBackdrop
import com.wordglass.app.core.components.GlassIconButton
import com.wordglass.app.core.design.WgRadius
import com.wordglass.app.core.design.WgSpace
import com.wordglass.app.core.design.WgTheme
import com.wordglass.app.core.design.WgType
import com.wordglass.app.model.DashboardStats
import com.wordglass.app.model.Word
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Dashboard as an editorial cover page.
 *
 * One ink block carries the single most important number; everything else is separated by
 * hairline rules and whitespace instead of cards. Nothing here samples the backdrop.
 */
@Composable
fun HomeScreen(
    backdrop: PrismalBackdrop,
    stats: DashboardStats,
    words: List<Word>,
    dark: Boolean,
    onToggleTheme: () -> Unit,
    onStartStudy: () -> Unit,
    onOpenLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    val greeting = remember {
        when (LocalTime.now().hour) {
            in 5..11 -> "早上好"
            in 12..17 -> "下午好"
            else -> "晚上好"
        }
    }
    val dateLine = remember {
        LocalDate.now().format(
            DateTimeFormatter.ofPattern("yyyy 年 M 月 d 日 · EEEE", Locale.CHINA),
        )
    }
    val dueWords = remember(words) { words.filter { it.due }.take(3) }
    val remaining = (stats.dailyGoal - stats.completedToday).coerceAtLeast(0)
    val progress = if (stats.dailyGoal <= 0) 0f else
        (stats.completedToday.toFloat() / stats.dailyGoal).coerceIn(0f, 1f)

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(
            start = WgSpace.screen,
            end = WgSpace.screen,
            top = 8.dp,
            bottom = WgSpace.navReserve,
        ),
    ) {
        item { HomeMasthead(dateLine = dateLine, dark = dark, onToggleTheme = onToggleTheme) }

        item { Spacer(Modifier.height(WgSpace.xl)) }

        item {
            Text(
                text = "$greeting，Alex",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.ink,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (remaining > 0) {
                    "今天还有 $remaining 个词。慢慢来，一次一个。"
                } else {
                    "今日目标已完成。保持这个节奏。"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkSecondary,
            )
        }

        item { Spacer(Modifier.height(WgSpace.xl)) }

        item {
            TodayBlock(
                completed = stats.completedToday,
                goal = stats.dailyGoal,
                progress = progress,
                streakDays = stats.streakDays,
                minutes = stats.minutesToday,
                onStartStudy = onStartStudy,
            )
        }

        item { Spacer(Modifier.height(WgSpace.xxl)) }

        item { Rule() }
        item { StatPair("待复习", "${stats.dueCount}", "词", "已掌握", "${stats.mastered}", "词") }
        item { Rule() }
        item { StatPair("连续打卡", "${stats.streakDays}", "天", "今日学习", "${stats.minutesToday}", "分钟") }
        item { Rule() }

        item { Spacer(Modifier.height(WgSpace.xxl)) }

        item {
            SectionTitle("今日待复习", "REVIEW")
        }
        if (dueWords.isEmpty()) {
            item {
                Text(
                    text = "暂时没有到期的词。去词库翻一翻，或开始新的一课。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkSecondary,
                    modifier = Modifier.padding(vertical = WgSpace.md),
                )
            }
        } else {
            dueWords.forEach { word ->
                item(key = "due-${word.id}") {
                    DueRow(word = word, onClick = onOpenLibrary)
                }
            }
        }

        item { Spacer(Modifier.height(WgSpace.lg)) }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = onOpenLibrary,
                    )
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "我的词库",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.ink,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${words.size} 个词 · 按原文顺序",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkSecondary,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.inkTertiary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        item { Spacer(Modifier.height(WgSpace.xxl)) }

        item { SectionTitle("本周节奏", "WEEK") }
        item {
            Column(Modifier.padding(top = WgSpace.md)) {
                WeekBars(values = stats.weeklyMinutes)
                Spacer(Modifier.height(WgSpace.sm))
                WeekLabels()
            }
        }
    }
}

@Composable
private fun HomeMasthead(
    dateLine: String,
    dark: Boolean,
    onToggleTheme: () -> Unit,
) {
    val colors = WgTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "WORDGLASS",
                style = WgType.eyebrow,
                color = colors.ink,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = dateLine,
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkTertiary,
            )
        }
        GlassIconButton(
            backdrop = com.wordglass.app.ui.LocalGlassBackdrop.current,
            icon = if (dark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
            contentDescription = if (dark) "切换到浅色模式" else "切换到深色模式",
            onClick = onToggleTheme,
            size = 36.dp,
        )
    }
}

@Composable
private fun TodayBlock(
    completed: Int,
    goal: Int,
    progress: Float,
    streakDays: Int,
    minutes: Int,
    onStartStudy: () -> Unit,
) {
    val colors = WgTheme.colors
    val animated by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "todayProgress",
    )
    val density = LocalDensity.current
    val barHeight = with(density) { 6.dp.toPx() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.ink, RoundedCornerShape(WgRadius.hero))
            .padding(horizontal = 22.dp, vertical = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "今日进度",
                style = WgType.eyebrow,
                color = colors.background.copy(alpha = 0.62f),
            )
            Spacer(Modifier.weight(1f))
            if (streakDays > 0) {
                Text(
                    text = "连续 $streakDays 天",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.background.copy(alpha = 0.70f),
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "$completed",
                style = WgType.numericHero,
                color = colors.background,
                fontSize = 52.sp,
            )
            Text(
                text = " / $goal",
                style = MaterialTheme.typography.titleLarge,
                color = colors.background.copy(alpha = 0.55f),
                modifier = Modifier.padding(bottom = 9.dp, start = 2.dp),
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${minutes} 分钟",
                style = MaterialTheme.typography.bodySmall,
                color = colors.background.copy(alpha = 0.70f),
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    colors.background.copy(alpha = 0.16f),
                    RoundedCornerShape(50),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animated)
                    .height(6.dp)
                    .background(colors.background, RoundedCornerShape(50)),
            )
        }
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.background, RoundedCornerShape(50))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = onStartStudy,
                )
                .padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (completed == 0) "开始学习" else "继续学习",
                style = MaterialTheme.typography.labelLarge,
                color = colors.ink,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.ink,
                modifier = Modifier.size(18.dp).padding(start = 4.dp),
            )
        }
        if (barHeight > 0) Spacer(Modifier.height(0.dp))
    }
}

@Composable
private fun Rule() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(WgTheme.colors.divider),
    )
}

@Composable
private fun StatPair(
    leftLabel: String,
    leftValue: String,
    leftUnit: String,
    rightLabel: String,
    rightValue: String,
    rightUnit: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatCell(leftLabel, leftValue, leftUnit, Modifier.weight(1f))
        Box(
            Modifier
                .padding(horizontal = 10.dp)
                .height(34.dp)
                .width(1.dp)
                .background(WgTheme.colors.divider),
        )
        StatCell(rightLabel, rightValue, rightUnit, Modifier.weight(1f))
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    Column(modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.inkSecondary,
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = WgType.numeric,
                color = colors.ink,
            )
            Text(
                text = " $unit",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkTertiary,
                modifier = Modifier.padding(bottom = 3.dp),
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String, eyebrow: String) {
    val colors = WgTheme.colors
    Column {
        Text(
            text = eyebrow,
            style = WgType.eyebrow,
            color = colors.inkTertiary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.ink,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DueRow(word: Word, onClick: () -> Unit) {
    val colors = WgTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = word.word,
                    style = WgType.wordRow,
                    color = colors.ink,
                )
                if (word.phonetic.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "/${word.phonetic}/",
                        style = WgType.phonetic,
                        color = colors.inkTertiary,
                    )
                }
            }
            Text(
                text = word.meaning.take(18),
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkSecondary,
            )
        }
        Spacer(Modifier.height(1.dp))
    }
    Rule()
}

@Composable
private fun WeekBars(values: List<Int>) {
    val colors = WgTheme.colors
    val max = (values.maxOrNull() ?: 0).coerceAtLeast(1)
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        values.forEachIndexed { index, value ->
            val frac = value.toFloat() / max
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
                            if (index == values.lastIndex) colors.blue
                            else colors.ink.copy(alpha = 0.16f),
                            RoundedCornerShape(3.dp),
                        ),
                )
            }
        }
    }
}

@Composable
private fun WeekLabels() {
    val colors = WgTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf("一", "二", "三", "四", "五", "六", "日").forEachIndexed { index, label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (index == 6) colors.ink else colors.inkTertiary,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}
