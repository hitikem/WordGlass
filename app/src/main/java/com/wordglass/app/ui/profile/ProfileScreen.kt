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
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.Color as ComposeColor

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
    onNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onAvatarSeedChange: (String) -> Unit,
    onAvatarUriChange: (String?) -> Unit,
    onVoiceProfileChange: (com.wordglass.app.model.VoiceProfile) -> Unit,
    availableVoices: List<com.wordglass.app.model.VoiceProfile>,
    isLoadingVoices: Boolean,
    onPreviewVoice: (com.wordglass.app.model.VoiceProfile) -> Unit,
    onSyncUpload: () -> Unit,
    onSyncDownload: () -> Unit,
    onCheckUpdate: () -> Unit,
    syncStatus: String,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    var goalDialog by remember { mutableStateOf(false) }
    var infoDialog by remember { mutableStateOf<String?>(null) }
    var editProfile by remember { mutableStateOf(false) }
    var voicePickerDialog by remember { mutableStateOf(false) }
    var avatarPickerDialog by remember { mutableStateOf(false) }
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
                settings = settings,
                onEditClick = { editProfile = true },
                onAvatarClick = { avatarPickerDialog = true },
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
                    subtitle = "点击选择音色，可试听",
                ) {
                    GlassChip(
                        text = "选择音色",
                        onClick = { voicePickerDialog = true },
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
                title = "云同步",
                eyebrow = "CLOUD SYNC",
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        item {
            SettingsGroup(backdrop = backdrop) {
                SettingsRow(
                    icon = Icons.Rounded.CloudUpload,
                    title = "上传学习记录",
                    subtitle = "备份到你的电脑服务器 · $syncStatus",
                    onClick = onSyncUpload,
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.CloudDownload,
                    title = "从云端恢复",
                    subtitle = "从电脑服务器下载学习记录",
                    onClick = onSyncDownload,
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Refresh,
                    title = "检查更新",
                    subtitle = "检查是否有新版本",
                    onClick = onCheckUpdate,
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
                    title = "WordGlass 2.0",
                    subtitle = "专注背单词 · 真人发音 · 联网查词",
                    onClick = { infoDialog = "WordGlass 2.0.0\n\n一款专注背单词的 Android 应用。\n\n【核心功能】\n· 真人发音（有道免费词典语音）\n· 五种学习模式：卡片 / 滑动 / 选择 / 听写 / 拼写\n· SM-2 记忆算法，科学安排复习\n· 联网查词：英文释义 + 中文释义 + 词根词缀 + 短语\n· 学习计划：设定目标，每天学一点\n· 学习报告：周/月统计，可视化进度\n· 桌面小组件：不用打开 App 也能看进度\n· 每日提醒：坚持打卡不忘记\n· 云同步：学习记录备份到你的电脑\n· 自定义头像、昵称\n\n【词库】\n882 条高中英语核心词汇，来源可溯，不编造数据。\n\n【技术】\nKotlin · Jetpack Compose · Room · DataStore\nPrismalAGSL (MIT) · Coil (Apache-2.0) · Manrope (OFL)\n\n【隐私】\n所有数据保存在你的手机和你的电脑上，不经过任何第三方服务器。" },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Palette,
                    title = "设计语言",
                    subtitle = "Liquid Glass × Premium Minimalism",
                    onClick = { infoDialog = "设计语言\n\n· Apple Liquid Glass 液态玻璃视觉\n· 极简主义：少即是多\n· 蓝白配色：清爽、专注\n· 连续圆角：统一视觉节奏\n· 克制动效：不抢注意力\n· 真实数据：不编造任何内容\n\n开源组件：\nPrismalAGSL 1.0.6 (MIT)\nCoil 2.7.0 (Apache-2.0)\nManrope Font (OFL)" },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Public,
                    title = "联网服务",
                    subtitle = "词典 API · 云同步 · 自动更新",
                    onClick = { infoDialog = "联网服务\n\n【联网查词】\n使用免费词典 API（dictionaryapi.dev + 有道词典），查询英文释义、中文翻译、词根词缀、常用短语。\n\n【云同步】\n学习记录备份到你自己的电脑服务器（Cloudflare Tunnel 免费公网访问），不经过任何第三方。\n\n【自动更新】\n检测新版本并提示更新。\n\n【隐私】\n联网查词只发送单词文本，不发送你的学习数据。" },
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

    if (editProfile) {
        EditProfileDialog(
            name = settings.profileName,
            bio = settings.profileBio,
            onDismiss = { editProfile = false },
            onSave = { name, bio, seed ->
                onNameChange(name)
                onBioChange(bio)
                onAvatarSeedChange(seed)
                editProfile = false
            },
        )
    }

    if (voicePickerDialog) {
        VoicePickerDialog(
            currentVoiceId = settings.voiceProfileId,
            availableVoices = availableVoices,
            isLoadingVoices = isLoadingVoices,
            onPreview = { onPreviewVoice(it) },
            onSelect = { onVoiceProfileChange(it) },
            onDismiss = { voicePickerDialog = false },
        )
    }

    if (avatarPickerDialog) {
        AvatarPickerDialog(
            currentAvatarUri = settings.profileAvatarUri,
            onAvatarSelected = { uri -> onAvatarUriChange(uri) },
            onDismiss = { avatarPickerDialog = false },
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
                    text = "WordGlass",
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
private fun EditProfileDialog(
    name: String,
    bio: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
) {
    val colors = WgTheme.colors
    var nameInput by remember { mutableStateOf(name) }
    var bioInput by remember { mutableStateOf(bio) }
    var avatarSeed by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceElevated,
        titleContentColor = colors.ink,
        textContentColor = colors.inkSecondary,
        title = {
            Text(text = "编辑个人资料", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "昵称", style = MaterialTheme.typography.labelMedium, color = colors.inkSecondary)
                BasicTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it.take(32) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.ink),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.background, RoundedCornerShape(10.dp))
                        .border(1.dp, colors.divider, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                )
                Text(text = "个性签名", style = MaterialTheme.typography.labelMedium, color = colors.inkSecondary)
                BasicTextField(
                    value = bioInput,
                    onValueChange = { bioInput = it.take(60) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.ink),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.background, RoundedCornerShape(10.dp))
                        .border(1.dp, colors.divider, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                )
                Text(text = "头像颜色", style = MaterialTheme.typography.labelMedium, color = colors.inkSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("0", "1", "2", "3", "4", "5").forEach { seed ->
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(avatarColorForSeed(seed))
                                .border(
                                    if (avatarSeed == seed) 2.dp else 0.dp,
                                    if (avatarSeed == seed) colors.ink else Color.Transparent,
                                    CircleShape,
                                )
                                .clickable { avatarSeed = seed },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(nameInput, bioInput, avatarSeed) }) {
                Text("保存", color = colors.blue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = colors.inkSecondary)
            }
        },
    )
}

@Composable
private fun avatarColorForSeed(seed: String): Color {
    val colors = WgTheme.colors
    return when (seed) {
        "1" -> colors.blueDeep
        "2" -> colors.mint
        "3" -> colors.warning
        "4" -> colors.cyan
        "5" -> colors.danger
        else -> colors.blue
    }
}

@Composable
private fun ProfileHero(
    backdrop: PrismalBackdrop,
    streakDays: Int,
    settings: AppSettings,
    onEditClick: () -> Unit,
    onAvatarClick: () -> Unit,
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
                    .border(2.dp, Color.White.copy(alpha = 0.50f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onAvatarClick() },
                contentAlignment = Alignment.Center,
            ) {
                // Show photo avatar if set, otherwise show initial
                val avatarFile = settings.profileAvatarUri?.let { java.io.File(it) }
                if (avatarFile != null && avatarFile.exists()) {
                    coil.compose.AsyncImage(
                        model = avatarFile,
                        contentDescription = "头像",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.50f), CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text(
                        text = settings.profileName.firstOrNull()?.uppercase() ?: "A",
                        style = WgType.numericHero,
                        color = Color.White,
                        fontSize = 28.sp,
                    )
                }
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = settings.profileName,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.ink,
                    )
                    Spacer(Modifier.size(6.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .border(1.dp, colors.divider, RoundedCornerShape(50))
                            .clickable { onEditClick() }
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "编辑",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.inkSecondary,
                        )
                    }
                }
                Text(
                    text = settings.profileBio,
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
    var selectedValue by remember { mutableIntStateOf(current) }
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "每天背多少个单词？",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(16.dp))
                com.wordglass.app.ui.components.NumberPicker(
                    value = selectedValue,
                    onValueChange = { selectedValue = it },
                    minValue = 5,
                    maxValue = 100,
                    unit = "词",
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "约 ${(882f / selectedValue).toInt()} 天背完全部单词",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkTertiary,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSelect(selectedValue)
                },
            ) {
                Text("确定", color = colors.blue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = colors.inkSecondary)
            }
        },
    )
}

