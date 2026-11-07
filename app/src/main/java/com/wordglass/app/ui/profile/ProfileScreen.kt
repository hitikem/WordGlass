package com.wordglass.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.styropyr0.prismal.PrismalBackdrop
import com.wordglass.app.core.components.GlassCard
import com.wordglass.app.core.components.GlassChip
import com.wordglass.app.core.components.IconCircle
import com.wordglass.app.core.components.SectionHeader
import com.wordglass.app.core.components.SegmentedGlass
import com.wordglass.app.core.components.WgSwitch
import com.wordglass.app.core.design.WgRadius
import com.wordglass.app.core.design.WgSpace
import com.wordglass.app.core.design.WgTheme
import com.wordglass.app.core.design.WgType
import com.wordglass.app.model.AppSettings
import com.wordglass.app.model.DashboardStats
import com.wordglass.app.model.ThemeMode
import com.wordglass.app.model.VoiceGender

@Composable
fun ProfileScreen(
    backdrop: PrismalBackdrop,
    settings: AppSettings,
    stats: DashboardStats,
    onThemeChange: (ThemeMode) -> Unit,
    onVoiceChange: (VoiceGender) -> Unit,
    onSpeechRateChange: (Float) -> Unit,
    onAutoPlayChange: (Boolean) -> Unit,
    onDailyGoalChange: (Int) -> Unit,
    onSpeakSample: () -> Unit,
    onImport: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    var goalDialog by remember { mutableStateOf(false) }
    var infoDialog by remember { mutableStateOf<String?>(null) }

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
            Column {
                Text(
                    text = "PROFILE",
                    style = WgType.eyebrow,
                    color = colors.blue,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "我的",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.ink,
                )
            }
        }

        item {
            ProfileHero(
                backdrop = backdrop,
                streakDays = stats.streakDays,
            )
        }

        item {
            GlassCard(
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth().height(92.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                elevation = 10.dp,
            ) {
                Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    ProfileMetric("${stats.totalLearned}", "累计学习", colors.blue, Modifier.weight(1f))
                    MetricDivider()
                    ProfileMetric("${stats.mastered}", "已掌握", colors.success, Modifier.weight(1f))
                    MetricDivider()
                    ProfileMetric(formatStudyMinutes(stats.minutesToday), "今日时长", colors.cyan, Modifier.weight(1f))
                }
            }
        }

        item {
            SectionHeader(
                title = "学习",
                eyebrow = "STUDY",
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        item {
            SettingsGroup(backdrop = backdrop) {
                SettingsRow(
                    icon = Icons.Rounded.Settings,
                    title = "每日目标",
                    subtitle = "${settings.dailyGoal} 个词 / 天",
                    onClick = { goalDialog = true },
                    trailing = {
                        Text(
                            text = "调整",
                            style = WgType.label,
                            color = colors.blue,
                        )
                    },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.VolumeUp,
                    title = "自动播放发音",
                    subtitle = "进入新词时自动朗读",
                    trailing = {
                        WgSwitch(
                            checked = settings.autoPlay,
                            onCheckedChange = onAutoPlayChange,
                        )
                    },
                )
            }
        }

        item {
            SectionHeader(
                title = "语音",
                eyebrow = "VOICE",
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        item {
            SettingsGroup(backdrop = backdrop) {
                SettingBlock(
                    icon = Icons.Rounded.Language,
                    title = "发音人",
                    subtitle = "美式英语 · 原型使用系统 TTS",
                ) {
                    SegmentedGlass(
                        options = VoiceGender.entries.map { it.label },
                        selectedIndex = VoiceGender.entries.indexOf(settings.voice),
                        onSelect = { onVoiceChange(VoiceGender.entries[it]) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                SettingsDivider()
                SettingBlock(
                    icon = Icons.Rounded.GraphicEq,
                    title = "语速",
                    subtitle = "只改变播放速度，不重复消耗合成额度",
                ) {
                    SegmentedGlass(
                        options = listOf("0.8×", "0.9×", "1.0×"),
                        selectedIndex = when (settings.speechRate) {
                            0.8f -> 0
                            1.0f -> 2
                            else -> 1
                        },
                        onSelect = {
                            onSpeechRateChange(listOf(0.8f, 0.9f, 1.0f)[it])
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.VolumeUp,
                    title = "试听当前音色",
                    subtitle = "播放一句美式英语示例",
                    onClick = onSpeakSample,
                    trailing = {
                        GlassChip(text = "播放", onClick = onSpeakSample)
                    },
                )
            }
        }

        item {
            SectionHeader(
                title = "外观",
                eyebrow = "APPEARANCE",
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        item {
            SettingsGroup(backdrop = backdrop) {
                SettingBlock(
                    icon = if (settings.themeMode == ThemeMode.DARK) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                    title = "主题",
                    subtitle = "跟随系统，或手动锁定浅色 / 深色",
                ) {
                    SegmentedGlass(
                        options = ThemeMode.entries.map { it.label },
                        selectedIndex = ThemeMode.entries.indexOf(settings.themeMode),
                        onSelect = { onThemeChange(ThemeMode.entries[it]) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = "数据与词库",
                eyebrow = "DATA",
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        item {
            SettingsGroup(backdrop = backdrop) {
                SettingsRow(
                    icon = Icons.Rounded.CloudUpload,
                    title = "导入词库",
                    subtitle = "WordGlass JSON · 不覆盖学习记录",
                    onClick = onImport,
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.CloudDownload,
                    title = "备份到文件",
                    subtitle = "词条、学习状态、复习记录与设置",
                    onClick = onBackup,
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.CloudDownload,
                    title = "从备份恢复",
                    subtitle = "合并写入，不删除现有词条",
                    onClick = onRestore,
                )
            }
        }

        item {
            SectionHeader(
                title = "关于",
                eyebrow = "ABOUT",
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        item {
            SettingsGroup(backdrop = backdrop) {
                SettingsRow(
                    icon = Icons.Rounded.Info,
                    title = "WordGlass",
                    subtitle = "0.2.0-beta · 内测",
                    onClick = { infoDialog = "WordGlass 0.2.0-beta\n\nJetpack Compose · Room · DataStore · PrismalAGSL (MIT) · Manrope (OFL)\n\n已接入 882 条可溯源词库、学习状态、SM-2 调度、设置持久化与 JSON 备份。云 TTS 仍需配置密钥。" },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Palette,
                    title = "设计语言",
                    subtitle = "Liquid Glass · Premium Minimalism",
                    onClick = { infoDialog = "蓝白液态玻璃设计系统：固定品牌色、连续圆角、克制模糊、清晰文字层级，以及支持降级的玻璃渲染。" },
                )
            }
        }

        item {
            Text(
                text = "Made with care for focused learning.",
                style = WgType.eyebrow,
                color = colors.inkTertiary,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 8.dp),
                textAlign = TextAlign.Center,
            )
        }
    }

    if (goalDialog) {
        DailyGoalDialog(
            current = settings.dailyGoal,
            onDismiss = { goalDialog = false },
            onSelect = {
                onDailyGoalChange(it)
                goalDialog = false
            },
        )
    }

    infoDialog?.let { message ->
        AlertDialog(
            onDismissRequest = { infoDialog = null },
            containerColor = colors.surfaceElevated,
            titleContentColor = colors.ink,
            textContentColor = colors.inkSecondary,
            title = {
                Text(
                    text = "UI 原型说明",
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = { infoDialog = null }) {
                    Text("知道了", color = colors.blue)
                }
            },
        )
    }
}

private fun formatStudyMinutes(minutes: Int): String =
    if (minutes >= 60) "${minutes / 60}h" else "${minutes}m"

@Composable
private fun ProfileHero(
    backdrop: PrismalBackdrop,
    streakDays: Int,
) {
    val colors = WgTheme.colors
    GlassCard(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth().height(126.dp),
        cornerRadius = WgRadius.card,
        contentPadding = PaddingValues(18.dp),
        elevation = 12.dp,
    ) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(colors.blue, colors.blueDeep),
                        ),
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.50f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "A",
                    style = WgType.numericHero,
                    color = Color.White,
                    fontSize = 28.sp,
                )
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Alex Chen",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.ink,
                )
                Text(
                    text = "WordGlass Learner",
                    style = WgType.label,
                    color = colors.inkSecondary,
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.warning.copy(alpha = 0.10f))
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = colors.warning,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(
                        text = "连续学习 $streakDays 天",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.warning,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMetric(
    value: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = WgType.numeric,
            color = tint,
            fontSize = 23.sp,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = WgTheme.colors.inkSecondary,
        )
    }
}

@Composable
private fun MetricDivider() {
    Box(
        Modifier
            .width(1.dp)
            .height(30.dp)
            .background(WgTheme.colors.divider),
    )
}

@Composable
private fun SettingsGroup(
    backdrop: PrismalBackdrop,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassCard(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp),
        elevation = 10.dp,
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val colors = WgTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = onClick != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onClick?.invoke() },
            )
            .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconCircle(icon = icon, tint = colors.blue, size = 34.dp)
        Spacer(Modifier.size(11.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.ink,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkSecondary,
            )
        }
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.inkTertiary,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
private fun SettingBlock(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    val colors = WgTheme.colors
    Column(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 13.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconCircle(icon = icon, tint = colors.blue, size = 34.dp)
            Spacer(Modifier.size(11.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.ink,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkSecondary,
                )
            }
        }
        Spacer(Modifier.height(11.dp))
        content()
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 60.dp, end = 15.dp)
            .height(1.dp)
            .background(WgTheme.colors.divider),
    )
}

@Composable
private fun DailyGoalDialog(
    current: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    val colors = WgTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceElevated,
        titleContentColor = colors.ink,
        textContentColor = colors.inkSecondary,
        title = {
            Text(
                text = "每日目标",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    text = "选择一个可持续的节奏。目标会在正式版本中持久化。",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(3.dp))
                listOf(15, 20, 25, 30).forEach { value ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(13.dp))
                            .background(
                                if (current == value) colors.blue.copy(alpha = 0.10f)
                                else Color.Transparent,
                            )
                            .clickable { onSelect(value) }
                            .padding(horizontal = 12.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "$value 个词 / 天",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (current == value) colors.blue else colors.ink,
                            modifier = Modifier.weight(1f),
                        )
                        if (current == value) {
                            Text(
                                text = "当前",
                                style = WgType.label,
                                color = colors.blue,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = colors.inkSecondary)
            }
        },
    )
}
