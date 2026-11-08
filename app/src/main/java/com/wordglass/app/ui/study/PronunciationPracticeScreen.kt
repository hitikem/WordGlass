package com.wordglass.app.ui.study

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wordglass.app.core.design.WgRadius
import com.wordglass.app.core.design.WgTheme
import com.wordglass.app.core.design.WgType
import com.wordglass.app.model.Word
import java.io.File

/**
 * Pronunciation follow-along (发音跟读):
 * 1. Listen to the correct pronunciation
 * 2. Record yourself speaking
 * 3. Play back and compare
 * Simple self-assessment scoring.
 */
@Composable
fun PronunciationPracticeScreen(
    word: Word?,
    onSpeak: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    if (word == null) return

    var isRecording by remember { mutableStateOf(false) }
    var hasRecording by remember { mutableStateOf(false) }
    var isPlayingRecording by remember { mutableStateOf(false) }
    var selfScore by remember { mutableIntStateOf(-1) }

    val context = androidx.compose.ui.platform.LocalContext.current
    var recorder: MediaRecorder? by remember { mutableStateOf(null) }
    var player: MediaPlayer? by remember { mutableStateOf(null) }
    val audioFile = remember { File(context.cacheDir, "pronunciation_${word.id}.m4a") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Start recording
            isRecording = true
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try { recorder?.stop(); recorder?.release() } catch (_: Exception) {}
            try { player?.stop(); player?.release() } catch (_: Exception) {}
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Word to pronounce
        Text("跟读练习", style = WgType.eyebrow, color = colors.inkTertiary)
        Spacer(Modifier.height(12.dp))
        Text(
            text = word.word,
            style = MaterialTheme.typography.displaySmall,
            color = colors.ink,
            fontWeight = FontWeight.Bold,
        )
        if (word.phonetic.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text("/${word.phonetic}/", style = WgType.phonetic, color = colors.inkTertiary)
        }
        Spacer(Modifier.height(8.dp))
        Text(word.meaning, style = MaterialTheme.typography.bodyMedium, color = colors.inkSecondary)
        Spacer(Modifier.height(32.dp))

        // Listen button
        ActionButton(
            icon = Icons.Rounded.VolumeUp,
            label = "听标准发音",
            color = colors.blue,
            onClick = { onSpeak(word.word) },
        )
        Spacer(Modifier.height(16.dp))

        // Record button
        ActionButton(
            icon = if (isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic,
            label = if (isRecording) "停止录音" else "开始跟读",
            color = if (isRecording) colors.danger else colors.success,
            onClick = {
                if (isRecording) {
                    // Stop recording
                    try {
                        recorder?.stop()
                        recorder?.release()
                    } catch (_: Exception) {}
                    recorder = null
                    isRecording = false
                    hasRecording = true
                } else {
                    // Request permission and start
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
        )

        if (hasRecording) {
            Spacer(Modifier.height(16.dp))
            // Play back
            ActionButton(
                icon = if (isPlayingRecording) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                label = if (isPlayingRecording) "停止播放" else "播放我的录音",
                color = colors.warning,
                onClick = {
                    if (isPlayingRecording) {
                        player?.stop()
                        player?.release()
                        player = null
                        isPlayingRecording = false
                    } else {
                        player = MediaPlayer().apply {
                            setDataSource(audioFile.absolutePath)
                            prepare()
                            start()
                            setOnCompletionListener {
                                isPlayingRecording = false
                                it.release()
                            }
                        }
                        isPlayingRecording = true
                    }
                },
            )
            Spacer(Modifier.height(24.dp))

            // Self-assessment
            Text("你觉得读得怎么样？", style = MaterialTheme.typography.bodyMedium, color = colors.inkSecondary)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ScoreButton("😊 很好", 3, selfScore) { selfScore = it }
                ScoreButton("🙂 还行", 2, selfScore) { selfScore = it }
                ScoreButton("😐 需练习", 1, selfScore) { selfScore = it }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Done button
        ActionButton(
            icon = Icons.Rounded.PlayArrow,
            label = "完成练习",
            color = colors.blue,
            onClick = onDone,
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    val colors = WgTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ScoreButton(label: String, score: Int, current: Int, onSelect: (Int) -> Unit) {
    val colors = WgTheme.colors
    val isSelected = current == score
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) colors.blue.copy(alpha = 0.15f) else colors.surface)
            .border(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) colors.blue else colors.divider,
                RoundedCornerShape(12.dp),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onSelect(score) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = if (isSelected) colors.blue else colors.ink)
    }
}
