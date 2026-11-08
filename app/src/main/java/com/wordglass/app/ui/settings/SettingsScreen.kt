package com.wordglass.app.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wordglass.app.core.design.WgTheme
import com.wordglass.app.core.design.WgType
import com.wordglass.app.model.AppSettings
import com.wordglass.app.model.ThemeMode
import com.wordglass.app.model.VoiceGender
import java.io.File

/**
 * 独立设置界面：所有设置项都可以实际操作。
 */
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
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
    var dialogState by remember { mutableStateOf<String?>(null) }

    // Name editor
    var nameInput by remember { mutableStateOf(settings.profileName) }
    var bioInput by remember { mutableStateOf(settings.profileBio) }

    // Voice picker
    var showVoicePicker by remember { mutableStateOf(false) }

    // Daily goal
    var goalValue by remember { mutableIntStateOf(settings.dailyGoal) }

    // Speech rate
    var rateValue by remember { mutableFloatStateOf(settings.speechRate) }

    // Avatar
    val context = androidx.compose.ui.platform.LocalContext.current
    val avatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val dir = File(context.filesDir, "avatars").apply { mkdirs() }
            val file = File(dir, "avatar_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            onAvatarUriChange(file.absolutePath)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onBack() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.ArrowBack, "返回", tint = colors.ink)
            }
            Spacer(Modifier.width(12.dp))
            Text("设置", style = MaterialTheme.typography.titleLarge, color = colors.ink, fontWeight = FontWeight.SemiBold)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // ========== 个人资料 ==========
            item { SectionTitle("个人资料") }
            item {
                Card {
                    Row(
                        Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { dialogState = "name" }
                            .padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Edit, null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("昵称", style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                            Text(settings.profileName, style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
                        }
                    }
                    Div()
                    Row(
                        Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { dialogState = "bio" }
                            .padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Edit, null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("个性签名", style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                            Text(settings.profileBio, style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
                        }
                    }
                    Div()
                    Row(
                        Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                            avatarLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Palette, null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("头像", style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                            Text(if (settings.profileAvatarUri != null) "点击更换照片" else "点击选择照片", style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
                        }
                    }
                }
            }

            // ========== 发音 ==========
            item { SectionTitle("发音") }
            item {
                Card {
                    Row(
                        Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { showVoicePicker = true }
                            .padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.VolumeUp, null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("发音人", style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                            Text("点击选择音色，可试听", style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
                        }
                    }
                    Div()
                    Row(
                        Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSpeakSample() }
                            .padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.VolumeUp, null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("试听当前音色", style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                    }
                    Div()
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AccessTime, null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("语速: ${String.format("%.1f", settings.speechRate)}×", style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                            Slider(
                                value = settings.speechRate,
                                onValueChange = { rateValue = it },
                                onValueChangeFinished = { onSpeechRateChange(rateValue) },
                                valueRange = 0.5f..1.5f,
                                steps = 9,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    Div()
                    Row(
                        Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onAutoPlayChange(!settings.autoPlay) }
                            .padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Refresh, null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("自动播放发音", style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                            Text(if (settings.autoPlay) "已开启" else "已关闭", style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
                        }
                    }
                }
            }

            // ========== 学习 ==========
            item { SectionTitle("学习") }
            item {
                Card {
                    Row(
                        Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { dialogState = "goal" }
                            .padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.School, null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("每日目标", style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                            Text("${settings.dailyGoal} 个词 / 天", style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
                        }
                    }
                }
            }

            // ========== 外观 ==========
            item { SectionTitle("外观") }
            item {
                Card {
                    Row(
                        Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { dialogState = "theme" }
                            .padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Palette, null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("主题模式", style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                            Text(settings.themeMode.label, style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
                        }
                    }
                }
            }

            // ========== 数据管理 ==========
            item { SectionTitle("数据管理") }
            item {
                Card {
                    Row(Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onImport() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CloudUpload, null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("导入词库", style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                            Text("从文件导入单词", style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
                        }
                    }
                    Div()
                    Row(Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onBackup() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Backup, null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("备份到文件", style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                            Text("保存学习数据到本地文件", style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
                        }
                    }
                    Div()
                    Row(Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onRestore() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Restore, null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("从备份恢复", style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                            Text("从本地文件恢复数据", style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
                        }
                    }
                }
            }

            // ========== 云同步 ==========
            item { SectionTitle("云同步") }
            item {
                Card {
                    Row(Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSyncUpload() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CloudUpload, null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("上传学习记录", style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                            Text("备份到电脑服务器 · $syncStatus", style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
                        }
                    }
                    Div()
                    Row(Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSyncDownload() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Storage, null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("从云端恢复", style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                            Text("从电脑服务器下载学习记录", style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
                        }
                    }
                    Div()
                    Row(Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onCheckUpdate() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Refresh, null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("检查更新", style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                            Text("检查是否有新版本", style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
                        }
                    }
                }
            }

            // ========== 关于 ==========
            item { SectionTitle("关于") }
            item {
                Card {
                    Row(Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { dialogState = "about" }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Info, null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("版本信息", style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                            Text("WordGlass 2.3.1", style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
                        }
                    }
                }
            }
        }
    }

    // ========== DIALOGS ==========

    if (dialogState == "name") {
        AlertDialog(
            onDismissRequest = { dialogState = null },
            containerColor = colors.surfaceElevated,
            title = { Text("修改昵称") },
            text = {
                BasicTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it.take(32) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.ink),
                    cursorBrush = SolidColor(colors.blue),
                    modifier = Modifier.fillMaxWidth().background(colors.background, RoundedCornerShape(10.dp)).border(1.dp, colors.divider, RoundedCornerShape(10.dp)).padding(12.dp),
                )
            },
            confirmButton = {
                TextButton(onClick = { onNameChange(nameInput); dialogState = null }) { Text("保存", color = colors.blue) }
            },
            dismissButton = {
                TextButton(onClick = { dialogState = null }) { Text("取消", color = colors.inkSecondary) }
            },
        )
    }

    if (dialogState == "bio") {
        AlertDialog(
            onDismissRequest = { dialogState = null },
            containerColor = colors.surfaceElevated,
            title = { Text("修改签名") },
            text = {
                BasicTextField(
                    value = bioInput,
                    onValueChange = { bioInput = it.take(60) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.ink),
                    cursorBrush = SolidColor(colors.blue),
                    modifier = Modifier.fillMaxWidth().background(colors.background, RoundedCornerShape(10.dp)).border(1.dp, colors.divider, RoundedCornerShape(10.dp)).padding(12.dp),
                )
            },
            confirmButton = {
                TextButton(onClick = { onBioChange(bioInput); dialogState = null }) { Text("保存", color = colors.blue) }
            },
            dismissButton = {
                TextButton(onClick = { dialogState = null }) { Text("取消", color = colors.inkSecondary) }
            },
        )
    }

    if (dialogState == "goal") {
        AlertDialog(
            onDismissRequest = { dialogState = null },
            containerColor = colors.surfaceElevated,
            title = { Text("每日目标") },
            text = {
                Column {
                    Text("每天背多少个词？")
                    Spacer(Modifier.height(12.dp))
                    Slider(
                        value = goalValue.toFloat(),
                        onValueChange = { goalValue = it.toInt() },
                        valueRange = 5f..100f,
                        steps = 18,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("${goalValue} 个词 / 天", style = MaterialTheme.typography.titleMedium, color = colors.ink)
                    Text("约 ${(882f / goalValue).toInt()} 天背完全部", style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
                }
            },
            confirmButton = {
                TextButton(onClick = { onDailyGoalChange(goalValue); dialogState = null }) { Text("确定", color = colors.blue) }
            },
            dismissButton = {
                TextButton(onClick = { dialogState = null }) { Text("取消", color = colors.inkSecondary) }
            },
        )
    }

    if (dialogState == "theme") {
        AlertDialog(
            onDismissRequest = { dialogState = null },
            containerColor = colors.surfaceElevated,
            title = { Text("主题模式") },
            text = {
                Column {
                    listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK).forEach { mode ->
                        Row(
                            Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                onThemeChange(mode); dialogState = null
                            }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (settings.themeMode == mode) {
                                Box(Modifier.size(10.dp).background(colors.blue, CircleShape))
                            } else {
                                Box(Modifier.size(10.dp).border(1.dp, colors.inkTertiary, CircleShape))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(mode.label, style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogState = null }) { Text("取消", color = colors.inkSecondary) }
            },
        )
    }

    if (showVoicePicker) {
        AlertDialog(
            onDismissRequest = { showVoicePicker = false },
            containerColor = colors.surfaceElevated,
            title = { Text("选择音色") },
            text = {
                LazyColumn(modifier = Modifier.height(400.dp)) {
                    // ========== 有道词典 ==========
                    item {
                        VoiceSectionLabel("有道词典（真人发音）")
                    }
                    item {
                        VoiceRow("有道·美式真人", "男声 · en-US", "youdao_real", settings.voiceProfileId) {
                            onVoiceProfileChange(it); showVoicePicker = false
                        }
                    }
                    item {
                        VoiceRow("有道·英式真人", "男声 · en-GB", "youdao_real_uk", settings.voiceProfileId) {
                            onVoiceProfileChange(it); showVoicePicker = false
                        }
                    }

                    // ========== 微软 ==========
                    item {
                        VoiceSectionLabel("微软（自然语音）")
                    }
                    item { VoiceRow("Aria·女声", "自然 · en-US", "ms_aria", settings.voiceProfileId) { onVoiceProfileChange(it); showVoicePicker = false } }
                    item { VoiceRow("Guy·男声", "沉稳 · en-US", "ms_guy", settings.voiceProfileId) { onVoiceProfileChange(it); showVoicePicker = false } }
                    item { VoiceRow("Jenny·女声", "亲切 · en-US", "ms_jenny", settings.voiceProfileId) { onVoiceProfileChange(it); showVoicePicker = false } }
                    item { VoiceRow("Michelle·女声", "温柔 · en-US", "ms_michelle", settings.voiceProfileId) { onVoiceProfileChange(it); showVoicePicker = false } }
                    item { VoiceRow("Ryan·男声", "英式 · en-GB", "ms_ryan", settings.voiceProfileId) { onVoiceProfileChange(it); showVoicePicker = false } }
                    item { VoiceRow("Sonia·女声", "英式 · en-GB", "ms_sonia", settings.voiceProfileId) { onVoiceProfileChange(it); showVoicePicker = false } }

                    // ========== 谷歌 ==========
                    item {
                        VoiceSectionLabel("谷歌（多口音）")
                    }
                    item { VoiceRow("美式女声", "en-US", "google_us", settings.voiceProfileId) { onVoiceProfileChange(it); showVoicePicker = false } }
                    item { VoiceRow("英式女声", "en-GB", "google_uk", settings.voiceProfileId) { onVoiceProfileChange(it); showVoicePicker = false } }
                    item { VoiceRow("澳式女声", "en-AU", "google_au", settings.voiceProfileId) { onVoiceProfileChange(it); showVoicePicker = false } }
                    item { VoiceRow("印度女声", "en-IN", "google_in", settings.voiceProfileId) { onVoiceProfileChange(it); showVoicePicker = false } }

                    // ========== 系统 ==========
                    item {
                        VoiceSectionLabel("系统（手机自带）")
                    }
                    item { VoiceRow("系统默认语音", "跟随手机设置", "system_default", settings.voiceProfileId) { onVoiceProfileChange(it); showVoicePicker = false } }
                    availableVoices.filter { it.id.startsWith("sys_") }.forEach { voice ->
                        item {
                            VoiceRow(voice.displayName, voice.locale, voice.id, settings.voiceProfileId) {
                                onVoiceProfileChange(it); showVoicePicker = false
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVoicePicker = false }) { Text("完成", color = colors.blue) }
            },
        )
    }

    if (dialogState == "about") {
        AlertDialog(
            onDismissRequest = { dialogState = null },
            containerColor = colors.surfaceElevated,
            title = { Text("WordGlass 2.3.1") },
            text = { Text("一款专注背单词的 Android 应用。\n\nKotlin · Jetpack Compose · Room\nPrismalAGSL · Coil · Manrope") },
            confirmButton = {
                TextButton(onClick = { dialogState = null }) { Text("确定", color = colors.blue) }
            },
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    val colors = WgTheme.colors
    Text(text = title, style = WgType.eyebrow, color = colors.inkTertiary, modifier = Modifier.padding(top = 8.dp, bottom = 2.dp))
}

@Composable
private fun VoiceSectionLabel(label: String) {
    val colors = WgTheme.colors
    Text(
        text = label,
        style = WgType.eyebrow,
        color = colors.inkTertiary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun VoiceRow(
    name: String,
    subtitle: String,
    id: String,
    currentId: String,
    onSelect: (com.wordglass.app.model.VoiceProfile) -> Unit,
) {
    val colors = WgTheme.colors
    val selected = id == currentId
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) colors.blue.copy(alpha = 0.10f) else Color.Transparent)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                onSelect(com.wordglass.app.model.VoiceProfile(
                    id = id,
                    gender = com.wordglass.app.model.VoiceGender.FEMALE,
                    displayName = name,
                    locale = subtitle,
                ))
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selected) {
            Box(Modifier.size(10.dp).background(colors.blue, CircleShape))
        } else {
            Box(Modifier.size(10.dp).border(1.dp, colors.inkTertiary, CircleShape))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyMedium, color = colors.ink, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
        }
        if (selected) {
            Icon(Icons.Rounded.Check, null, tint = colors.blue, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    val colors = WgTheme.colors
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.surface)) { content() }
}

@Composable
private fun Div() {
    val colors = WgTheme.colors
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(0.5.dp).background(colors.divider))
}
