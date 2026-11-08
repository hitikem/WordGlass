package com.wordglass.app.ui.study

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

/**
 * Word game: fill in the blank / choose the correct word.
 * Shows a sentence with a missing word, user picks from 4 options.
 */
@Composable
fun WordGameScreen(
    words: List<Word>,
    currentIndex: Int,
    onRate: (StudyRating) -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    if (words.isEmpty()) return

    val word = words[currentIndex % words.size]
    var selectedIndex by remember(word.id) { mutableIntStateOf(-1) }
    var showResult by remember(word.id) { mutableStateOf(false) }

    // Generate 4 options: 1 correct + 3 wrong
    val options = remember(word.id) {
        val wrong = words.filter { it.id != word.id }.shuffled().take(3)
        (listOf(word) + wrong).shuffled()
    }
    val correctIndex = options.indexOfFirst { it.id == word.id }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Header
        Text("单词游戏", style = WgType.eyebrow, color = colors.inkTertiary)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "第 ${currentIndex + 1} 题",
            style = MaterialTheme.typography.bodySmall,
            color = colors.inkTertiary,
        )
        Spacer(Modifier.height(16.dp))

        // Question: show phonetic, pick the word
        Text("根据发音和释义，选出正确单词", style = MaterialTheme.typography.bodyMedium, color = colors.inkSecondary)
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.blue.copy(alpha = 0.08f))
                .border(1.dp, colors.blue.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (word.phonetic.isNotBlank()) {
                    Text(
                        text = "/${word.phonetic}/",
                        style = WgType.phonetic,
                        color = colors.inkTertiary,
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    text = word.meaning,
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.ink,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Options
        options.forEachIndexed { index, option ->
            val isCorrect = index == correctIndex
            val isSelected = selectedIndex == index
            val showCorrect = showResult && isCorrect
            val showWrong = showResult && isSelected && !isCorrect

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        when {
                            showCorrect -> colors.success.copy(alpha = 0.15f)
                            showWrong -> colors.danger.copy(alpha = 0.15f)
                            isSelected -> colors.blue.copy(alpha = 0.10f)
                            else -> colors.surface
                        }
                    )
                    .border(
                        2.dp,
                        when {
                            showCorrect -> colors.success
                            showWrong -> colors.danger
                            isSelected -> colors.blue
                            else -> colors.divider
                        },
                        RoundedCornerShape(14.dp),
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        if (!showResult) {
                            selectedIndex = index
                            showResult = true
                        }
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option.word,
                    style = MaterialTheme.typography.titleMedium,
                    color = when {
                        showCorrect -> colors.success
                        showWrong -> colors.danger
                        else -> colors.ink
                    },
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Result
        if (showResult) {
            val isCorrect = selectedIndex == correctIndex
            Text(
                text = if (isCorrect) "🎉 回答正确！" else "❌ 回答错误，正确答案是 \"${word.word}\"",
                style = MaterialTheme.typography.titleMedium,
                color = if (isCorrect) colors.success else colors.danger,
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GameButton("下一题", colors.blue) {
                    onRate(if (isCorrect) StudyRating.GOOD else StudyRating.AGAIN)
                }
                GameButton("听发音", colors.warning) {
                    onSpeak(word.word)
                }
            }
        }
    }
}

@Composable
private fun GameButton(label: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.SemiBold)
    }
}
