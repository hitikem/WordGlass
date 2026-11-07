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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wordglass.app.core.components.GlassCard
import com.wordglass.app.core.design.WgRadius
import com.wordglass.app.core.design.WgTheme
import com.wordglass.app.core.design.WgType
import com.wordglass.app.model.Word

/**
 * Vocabulary size test: show 20 sample words from the user's library,
 * user marks known/unknown, we estimate their vocabulary size.
 */
@Composable
fun VocabTestCard(
    words: List<Word>,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    var testOpen by remember { mutableStateOf(false) }
    var resultScore by remember { mutableIntStateOf(-1) }

    GlassCard(
        backdrop = com.wordglass.app.ui.LocalGlassBackdrop.current,
        modifier = modifier.fillMaxWidth(),
        cornerRadius = WgRadius.card,
        contentPadding = PaddingValues(18.dp),
        elevation = 10.dp,
    ) {
        Column {
            Text(
                text = "词汇量测试",
                style = WgType.eyebrow,
                color = colors.inkTertiary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (resultScore >= 0) "你的词汇量约 $resultScore 词" else "测测你的词汇量",
                style = MaterialTheme.typography.titleMedium,
                color = colors.ink,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "从你的词库随机抽词，快速估算掌握程度。",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkSecondary,
            )
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(colors.blue)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { testOpen = true }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            ) {
                Text(
                    text = "开始测试",
                    style = MaterialTheme.typography.labelLarge,
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }

    if (testOpen) {
        VocabTestDialog(
            words = words,
            onDismiss = { testOpen = false },
            onFinish = { score ->
                resultScore = score
                testOpen = false
            },
        )
    }
}

@Composable
private fun VocabTestDialog(
    words: List<Word>,
    onDismiss: () -> Unit,
    onFinish: (Int) -> Unit,
) {
    val colors = WgTheme.colors
    val sample = remember(words) {
        words.shuffled().take(20)
    }
    var index by remember { mutableIntStateOf(0) }
    var knownCount by remember { mutableIntStateOf(0) }

    if (sample.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = colors.surfaceElevated,
            title = { Text("词汇量测试") },
            text = { Text("词库为空，无法测试。") },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("知道了", color = colors.blue)
                }
            },
        )
        return
    }

    val word = sample[index]
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceElevated,
        titleContentColor = colors.ink,
        textContentColor = colors.inkSecondary,
        title = {
            Text(
                text = "词汇量测试 ${index + 1}/${sample.size}",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.ink,
                    fontWeight = FontWeight.Bold,
                )
                if (word.phonetic.isNotBlank()) {
                    Text(
                        text = "/${word.phonetic}/",
                        style = WgType.phonetic,
                        color = colors.inkTertiary,
                    )
                }
                Text(
                    text = "你认识这个单词吗？",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkSecondary,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    knownCount++
                    if (index + 1 >= sample.size) {
                        // Estimate: 20 sampled from 882 total
                        val score = (knownCount * 882 / sample.size)
                        onFinish(score)
                    } else {
                        index++
                    }
                },
            ) {
                Text("认识", color = colors.success)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (index + 1 >= sample.size) {
                        val score = (knownCount * 882 / sample.size)
                        onFinish(score)
                    } else {
                        index++
                    }
                },
            ) {
                Text("不认识", color = colors.danger)
            }
        },
    )
}
