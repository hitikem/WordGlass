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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordglass.app.core.design.WgRadius
import com.wordglass.app.core.design.WgTheme
import com.wordglass.app.core.design.WgType
import com.wordglass.app.model.StudyRating
import com.wordglass.app.model.Word

/**
 * Spelling practice: show the Chinese meaning, user must type the English word.
 * Similar to 百词斩's spelling exercise.
 */
@Composable
fun SpellingStudyMode(
    word: Word?,
    onRate: (StudyRating) -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    val haptics = LocalHapticFeedback.current
    var input by remember(word?.id) { mutableStateOf("") }
    var revealed by remember(word?.id) { mutableStateOf(false) }
    var correct by remember(word?.id) { mutableStateOf(false) }

    if (word == null) return

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Chinese meaning as prompt
        Text(
            text = "请拼出这个单词",
            style = WgType.eyebrow,
            color = colors.inkTertiary,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = word.meaning,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.ink,
            fontWeight = FontWeight.SemiBold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))

        // Speak button to hear the word (helps with spelling)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .border(1.dp, colors.divider, RoundedCornerShape(50))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onSpeak(word.word) }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.VolumeUp,
                contentDescription = null,
                tint = colors.ink,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.size(6.dp))
            Text(
                text = "听发音",
                style = MaterialTheme.typography.labelMedium,
                color = colors.ink,
            )
        }

        Spacer(Modifier.height(32.dp))

        // Input field
        if (!revealed) {
            BasicTextField(
                value = input,
                onValueChange = { input = it.take(word.word.length + 2) },
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    color = colors.ink,
                    letterSpacing = 4.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                ),
                cursorBrush = SolidColor(colors.blue),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(colors.surface, RoundedCornerShape(12.dp))
                    .border(1.dp, colors.divider, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SpellButton(
                    text = "确认",
                    filled = true,
                    onClick = {
                        revealed = true
                        correct = input.trim().equals(word.word, ignoreCase = true)
                        haptics.performHapticFeedback(
                            if (correct) HapticFeedbackType.LongPress
                            else HapticFeedbackType.TextHandleMove,
                        )
                    },
                )
                SpellButton(
                    text = "跳过",
                    filled = false,
                    onClick = {
                        revealed = true
                        correct = false
                    },
                )
            }
        } else {
            // Reveal answer
            Text(
                text = word.word,
                style = MaterialTheme.typography.displaySmall,
                color = if (correct) colors.success else colors.danger,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
            )
            if (word.phonetic.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "/${word.phonetic}/",
                    style = WgType.phonetic,
                    color = colors.inkTertiary,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (correct) "✓ 正确！" else "✗ 再试一次",
                style = MaterialTheme.typography.titleMedium,
                color = if (correct) colors.success else colors.danger,
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SpellButton(
                    text = if (correct) "认识了" else "不认识",
                    filled = true,
                    onClick = {
                        onRate(if (correct) StudyRating.GOOD else StudyRating.AGAIN)
                        input = ""
                        revealed = false
                    },
                )
                SpellButton(
                    text = "再试一次",
                    filled = false,
                    onClick = {
                        input = ""
                        revealed = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SpellButton(
    text: String,
    filled: Boolean,
    onClick: () -> Unit,
) {
    val colors = WgTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (filled) colors.blue else Color.Transparent)
            .border(
                1.dp,
                if (filled) Color.Transparent else colors.divider,
                RoundedCornerShape(50),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClick() }
            .padding(horizontal = 22.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (filled) Color.White else colors.ink,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
