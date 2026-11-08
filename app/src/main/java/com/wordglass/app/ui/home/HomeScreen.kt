package com.wordglass.app.ui.home

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.styropyr0.prismal.PrismalBackdrop
import com.wordglass.app.core.components.AccentGlassCard
import com.wordglass.app.core.components.GhostButton
import com.wordglass.app.core.components.GlassCard
import com.wordglass.app.core.components.GlassIconButton
import com.wordglass.app.core.components.IconCircle
import com.wordglass.app.core.components.ProgressRing
import com.wordglass.app.core.components.SectionHeader
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

@Composable
fun HomeScreen(
    backdrop: PrismalBackdrop,
    stats: DashboardStats,
    words: List<Word>,
    dark: Boolean,
    userName: String,
    settings: com.wordglass.app.model.AppSettings,
    onToggleTheme: () -> Unit,
    onStartStudy: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenSettings: () -> Unit,
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
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE · dd MMM", Locale.ENGLISH))
    }
    val dueWords = remember(words) { words.filter { it.due }.take(2) }
    val remaining = (stats.dailyGoal - stats.completedToday).coerceAtLeast(0)
    val progress = stats.completedToday.toFloat() / stats.dailyGoal

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = WgSpace.screen,
            end = WgSpace.screen,
            top = 10.dp,
            bottom = WgSpace.navReserve,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HomeHeader(
                dateLine = dateLine,
                greeting = greeting,
                userName = userName,
                settings = settings,
                dark = dark,
                backdrop = backdrop,
                onToggleTheme = onToggleTheme,
                onAvatarClick = onOpenSettings,
            )
        }

        item {
            AccentGlassCard(
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth().height(250.dp),
            ) {
                Box(Modifier.fillMaxSize()) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.10f),
                            radius = size.width * 0.42f,
                            center = Offset(size.width * 0.92f, size.height * 0.05f),
                        )
                        drawCircle(
                            color = colors.cyan.copy(alpha = 0.12f),
                            radius = size.width * 0.28f,
                            center = Offset(size.width * 0.72f, size.height * 0.82f),
                        )
                    }
                    Column(
                        modifier = Modifier.fillMaxSize().padding(22.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "今日学习",
                                color = Color.White.copy(alpha = 0.72f),
                                style = WgType.label,
                            )
                            Spacer(Modifier.weight(1f))
                            StreakBadge(stats.streakDays)
                        }
                        Spacer(Modifier.height(17.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ProgressRing(
                                progress = progress,
                                size = 94.dp,
                                stroke = 9.dp,
                                color = Color.White,
                                track = Color.White.copy(alpha = 0.20f),
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${(progress * 100).toInt()}%",
                                        style = WgType.numeric,
                                        color = Color.White,
                                        fontSize = 22.sp,
                                    )
                                    Text(
                                        text = "TODAY",
                                        style = WgType.eyebrow,
                                        color = Color.White.copy(alpha = 0.62f),
                                        fontSize = 8.sp,
                                    )
                                }
                            }
                            Spacer(Modifier.size(20.dp))
                            Column {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${stats.completedToday}",
                                        style = WgType.numericHero,
                                        color = Color.White,
                                    )
                                    Text(
                                        text = " / ${stats.dailyGoal} 词",
                                        style = WgType.label,
                                        color = Color.White.copy(alpha = 0.65f),
                                        modifier = Modifier.padding(bottom = 7.dp),
                                    )
                                }
                                Text(
                                    text = if (stats.estimatedDaysToFinish > 0) "预计还需 ${stats.estimatedDaysToFinish} 天学完" else "已学完全部单词",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.78f),
                                )
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        HeroAction(
                            text = "继续学习",
                            onClick = onStartStudy,
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatTile(
                    backdrop = backdrop,
                    icon = Icons.Rounded.Schedule,
                    label = "待复习",
                    value = "${stats.dueCount}",
                    suffix = "词",
                    tint = colors.blue,
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    backdrop = backdrop,
                    icon = Icons.Rounded.TaskAlt,
                    label = "已掌握",
                    value = "${stats.mastered}",
                    suffix = "词",
                    tint = colors.mint,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatTile(
                    backdrop = backdrop,
                    icon = Icons.Rounded.LocalFireDepartment,
                    label = "连续打卡",
                    value = "${stats.streakDays}",
                    suffix = "天",
                    tint = colors.warning,
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    backdrop = backdrop,
                    icon = Icons.Rounded.Timer,
                    label = "今日学习",
                    value = "${stats.minutesToday}",
                    suffix = "分钟",
                    tint = colors.cyan,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            SectionHeader(
                title = "今日待复习",
                eyebrow = "REVIEW QUEUE",
                action = "查看全部",
                onAction = onOpenLibrary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        item {
            GlassCard(
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 17.dp, vertical = 8.dp),
            ) {
                Column {
                    dueWords.forEachIndexed { index, word ->
                        DueWordRow(
                            word = word,
                            onClick = onOpenLibrary,
                        )
                        if (index != dueWords.lastIndex) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(colors.divider),
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "本周节奏",
                eyebrow = "WEEKLY RHYTHM",
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        item {
            GlassCard(
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(18.dp),
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "比上周多学了 18%",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.ink,
                            )
                            Text(
                                text = "稳定的小步，比偶尔的冲刺更接近掌握。",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkSecondary,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "3h 42m",
                                style = WgType.label,
                                color = colors.ink,
                            )
                            Text(
                                text = "+18%",
                                style = WgType.label,
                                color = colors.success,
                            )
                        }
                    }
                    Spacer(Modifier.height(17.dp))
                    WeeklyBars(
                        values = stats.weeklyMinutes,
                        labels = (6 downTo 0).map { offset ->
                            when (java.time.LocalDate.now().minusDays(offset.toLong()).dayOfWeek.value) {
                                1 -> "一"; 2 -> "二"; 3 -> "三"; 4 -> "四"
                                5 -> "五"; 6 -> "六"; 7 -> "日"; else -> "?"
                            }
                        },
                        highlightIndex = 6,
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MiniActionCard(
                    backdrop = backdrop,
                    icon = Icons.Rounded.School,
                    title = "错词本",
                    detail = "${stats.wrongCount} 个待巩固",
                    onClick = onOpenLibrary,
                    modifier = Modifier.weight(1f),
                )
                MiniActionCard(
                    backdrop = backdrop,
                    icon = Icons.Rounded.Bookmark,
                    title = "收藏夹",
                    detail = "${stats.favoriteCount} 个收藏词",
                    onClick = onOpenLibrary,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            StudyCalendarCard(studiedDays = stats.studiedDaysInMonth)
        }

        item {
            Text(
                text = "LOCAL ROOM · ${words.size} 个本地词条",
                style = WgType.eyebrow,
                color = colors.inkTertiary,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 6.dp),
            )
        }
    }
}

@Composable
private fun HomeHeader(
    dateLine: String,
    greeting: String,
    userName: String,
    settings: com.wordglass.app.model.AppSettings,
    dark: Boolean,
    backdrop: PrismalBackdrop,
    onToggleTheme: () -> Unit,
    onAvatarClick: () -> Unit,
) {
    val colors = WgTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(colors.ink, colors.blueDeep),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    // Avatar with white circle border (always visible)
                    val avatarFile = settings.profileAvatarUri?.let { java.io.File(it) }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { onAvatarClick() },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (avatarFile != null && avatarFile.exists()) {
                            coil.compose.AsyncImage(
                                model = avatarFile,
                                contentDescription = "头像",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Text(
                                text = settings.profileName.firstOrNull()?.uppercase() ?: "W",
                                style = WgType.label,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                    }
                }
                Spacer(Modifier.size(9.dp))
                Column {
                    Text(
                        text = "WORDGLASS",
                        style = WgType.eyebrow,
                        color = colors.blue,
                    )
                    Text(
                        text = dateLine.uppercase(Locale.ENGLISH),
                        style = WgType.eyebrow,
                        color = colors.inkTertiary,
                        fontSize = 8.sp,
                    )
                }
            }
            Spacer(Modifier.height(15.dp))
            Text(
                text = "$greeting，$userName",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.ink,
            )
            Text(
                text = "坚持每天学习，积少成多。",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkSecondary,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}

@Composable
private fun StreakBadge(days: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.LocalFireDepartment,
            contentDescription = null,
            tint = Color(0xFFFFD27A),
            modifier = Modifier.size(13.dp),
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = "$days DAYS",
            style = WgType.eyebrow,
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 8.sp,
        )
    }
}

@Composable
private fun HeroAction(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(WgRadius.small))
            .background(
                Brush.verticalGradient(
                    listOf(Color.White, Color(0xFFF3F7FF)),
                ),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = WgTheme.colors.blueDeep,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.weight(1f))
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null,
            tint = WgTheme.colors.blueDeep,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun StatTile(
    backdrop: PrismalBackdrop,
    icon: ImageVector,
    label: String,
    value: String,
    suffix: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    GlassCard(
        backdrop = backdrop,
        modifier = modifier.height(108.dp),
        contentPadding = PaddingValues(14.dp),
        elevation = 10.dp,
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconCircle(icon = icon, tint = tint, size = 32.dp)
                Spacer(Modifier.weight(1f))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkSecondary,
                )
            }
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = WgType.numeric,
                    color = colors.ink,
                )
                Text(
                    text = suffix,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkTertiary,
                    modifier = Modifier.padding(start = 3.dp, bottom = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun DueWordRow(word: Word, onClick: () -> Unit) {
    val colors = WgTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.blue.copy(alpha = 0.09f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = word.word.take(1).uppercase(Locale.US),
                style = WgType.wordRow,
                color = colors.blue,
            )
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = word.word,
                    style = WgType.wordRow,
                    color = colors.ink,
                )
                Spacer(Modifier.size(7.dp))
                Text(
                    text = word.phonetic,
                    style = WgType.phonetic,
                    color = colors.inkTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = word.meaning,
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = "10 min",
            style = WgType.label,
            color = colors.blue,
        )
    }
}

@Composable
private fun WeeklyBars(
    values: List<Int>,
    labels: List<String>,
    highlightIndex: Int,
) {
    val colors = WgTheme.colors
    val max = (values.maxOrNull() ?: 1).coerceAtLeast(1)
    Column {
        Canvas(Modifier.fillMaxWidth().height(72.dp)) {
            val slot = size.width / values.size
            val barWidth = slot * 0.34f
            values.forEachIndexed { index, value ->
                val fraction = value.toFloat() / max
                val barHeight = size.height * (0.15f + fraction * 0.85f)
                val x = slot * index + (slot - barWidth) / 2f
                drawRoundRect(
                    brush = if (index == highlightIndex) {
                        Brush.verticalGradient(listOf(colors.blue, colors.cyan))
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                colors.blue.copy(alpha = 0.30f),
                                colors.blue.copy(alpha = 0.12f),
                            ),
                        )
                    },
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            labels.forEachIndexed { index, label ->
                Text(
                    text = label,
                    style = WgType.label,
                    color = if (index == highlightIndex) colors.blue else colors.inkTertiary,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun MiniActionCard(
    backdrop: PrismalBackdrop,
    icon: ImageVector,
    title: String,
    detail: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    GlassCard(
        backdrop = backdrop,
        modifier = modifier.height(84.dp),
        contentPadding = PaddingValues(13.dp),
        elevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconCircle(icon = icon, tint = colors.blue, size = 34.dp)
            Spacer(Modifier.size(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.ink,
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkSecondary,
                )
            }
        }
    }
}


