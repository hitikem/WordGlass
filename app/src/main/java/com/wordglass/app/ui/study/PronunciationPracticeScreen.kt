package com.wordglass.app.ui.study

import android.Manifest
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
import androidx.compose.material3.LinearProgressIndicator
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
import com.wordglass.app.model.StudyRating
import com.wordglass.app.model.Word
import com.wordglass.app.voice.PronunciationScorer

/**
 * Pronunciation follow-along with automatic scoring:
 * 1. Listen to standard pronunciation
 * 2. Record yourself speaking
 * 3. Auto-score using speech recognition
 * 4. Move to next word
 */
@Composable
fun PronunciationPracticeScreen(
    word: Word?,
    onSpeak: (String) -> Unit,
    onRate: (StudyRating) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    if (word == null) return

    var isListening by remember { mutableStateOf(false) }
    var isPlayingReference by remember { mutableStateOf(false) }
    var scoreResult by remember { mutableStateOf<PronunciationScorer.ScoreResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isListening = true
            errorMessage = null
            scoreResult = null
            PronunciationScorer.recognize(
                context = context,
                language = "en-US",
                onResult = { transcript ->
                    isListening = false
                    scoreResult = PronunciationScorer.detailedScore(word.word, transcript)
                },
                onError = { msg ->
                    isListening = false
                    errorMessage = msg
                },
            )
        } else {
            errorMessage = "需要录音权限才能评分"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Header
        Text("跟读练习", style = WgType.eyebrow, color = colors.inkTertiary)
        Spacer(Modifier.height(12.dp))

        // Word
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
        Text(word.meaning, style = MaterialTheme.typography.bodyMedium, color = colors.inkSecondary, textAlign = TextAlign.Center)

        Spacer(Modifier.height(32.dp))

        // Listen to standard pronunciation
        ActionButton(
            icon = Icons.Rounded.VolumeUp,
            label = "听标准发音",
            color = colors.blue,
            onClick = { onSpeak(word.word) },
        )
        Spacer(Modifier.height(16.dp))

        // Record & Score
        ActionButton(
            icon = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
            label = if (isListening) "正在听..." else "点我开始跟读",
            color = if (isListening) colors.danger else colors.success,
            onClick = {
                if (isListening) {
                    isListening = false
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
        )

        // Listening indicator
        if (isListening) {
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = colors.danger,
            )
            Spacer(Modifier.height(8.dp))
            Text("请读出单词...", style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
        }

        // Error
        errorMessage?.let { msg ->
            Spacer(Modifier.height(16.dp))
            Text(msg, style = MaterialTheme.typography.bodyMedium, color = colors.danger)
        }

        // Score result
        scoreResult?.let { result ->
            Spacer(Modifier.height(24.dp))

            // Score circle
            val scoreColor = when {
                result.score >= 85 -> colors.success
                result.score >= 60 -> colors.warning
                else -> colors.danger
            }
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(scoreColor.copy(alpha = 0.12f))
                    .border(3.dp, scoreColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${result.score}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = scoreColor,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "得分",
                style = MaterialTheme.typography.labelMedium,
                color = colors.inkTertiary,
            )

            Spacer(Modifier.height(12.dp))
            Text(
                text = result.feedback,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.ink,
                textAlign = TextAlign.Center,
            )

            // Transcript
            Spacer(Modifier.height(8.dp))
            Text(
                text = "识别结果: \"${result.transcript}\"",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkTertiary,
            )

            Spacer(Modifier.height(24.dp))

            // Next word button
            ActionButton(
                icon = Icons.Rounded.PlayArrow,
                label = "下一个词",
                color = colors.blue,
                onClick = {
                    val rating = when {
                        result.score >= 85 -> StudyRating.EASY
                        result.score >= 60 -> StudyRating.GOOD
                        result.score >= 30 -> StudyRating.HARD
                        else -> StudyRating.AGAIN
                    }
                    onRate(rating)
                },
            )

            // Retry button
            Spacer(Modifier.height(8.dp))
            ActionButton(
                icon = Icons.Rounded.Mic,
                label = "再试一次",
                color = colors.warning,
                onClick = {
                    scoreResult = null
                    errorMessage = null
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
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
