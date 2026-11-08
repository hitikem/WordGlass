package com.wordglass.app.ui.study

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.styropyr0.prismal.PrismalBackdrop
import com.wordglass.app.core.components.GlassIconButton
import com.wordglass.app.core.components.ThinProgressBar
import com.wordglass.app.core.design.WgMotion
import com.wordglass.app.core.design.WgSpace
import com.wordglass.app.core.design.WgTheme
import com.wordglass.app.core.design.WgType
import com.wordglass.app.data.speech.SpeechService
import com.wordglass.app.model.StudyMode
import com.wordglass.app.model.Word
import com.wordglass.app.ui.StudyUiState
import com.wordglass.app.ui.WordGlassViewModel

@Composable
fun StudyScreen(
    backdrop: PrismalBackdrop,
    vm: WordGlassViewModel,
    speaker: SpeechService,
    autoPlay: Boolean,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by vm.study.collectAsStateWithLifecycle()
    val words by vm.words.collectAsStateWithLifecycle()
    val word = words.getOrNull(state.index)
    val total = words.size
    val colors = WgTheme.colors

    DisposableEffect(Unit) {
        vm.onStudyVisible()
        onDispose { vm.onStudyHidden() }
    }

    LaunchedEffect(word?.id, autoPlay, state.mode) {
        if (autoPlay && word != null && !state.finished &&
            (state.mode == StudyMode.CARD || state.mode == StudyMode.DICTATION)
        ) {
            onSpeak(word.word)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = WgSpace.navReserve),
    ) {
        StudyHeader(
            state = state,
            total = total,
            backdrop = backdrop,
            onRestart = vm::restart,
        )
        Spacer(Modifier.height(14.dp))
        StudyModeSelector(
            selected = state.mode,
            onSelect = vm::setMode,
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ThinProgressBar(
                progress = if (state.finished) 1f else (state.index.toFloat() / total).coerceIn(0f, 1f),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.size(10.dp))
            Text(
                text = if (state.finished) "DONE" else "${(state.index + 1).toString().padStart(2, '0')} / ${total.toString().padStart(2, '0')}",
                style = WgType.eyebrow,
                color = colors.inkTertiary,
                fontSize = 9.sp,
            )
        }
        Spacer(Modifier.height(16.dp))
        Box(Modifier.weight(1f)) {
            if (state.finished) {
                StudyCompletion(
                    known = state.knownCount,
                    unknown = state.unknownCount,
                    onRestart = vm::restart,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                AnimatedContent(
                    targetState = state.mode,
                    transitionSpec = {
                        (fadeIn(tween(WgMotion.reveal)) +
                            slideInHorizontally(tween(WgMotion.reveal)) { it / 12 }) togetherWith
                            fadeOut(tween(160))
                    },
                    label = "studyMode",
                    modifier = Modifier.fillMaxSize(),
                ) { mode ->
                    when (mode) {
                        StudyMode.CARD -> CardStudyMode(
                            word = word,
                            state = state,
                            onFlip = vm::flip,
                            onRate = vm::rate,
                            onSpeak = onSpeak,
                            onFavorite = { word?.let { vm.toggleFavorite(it.id) } },
                            modifier = Modifier.fillMaxSize(),
                        )
                        StudyMode.SWIPE -> SwipeStudyMode(
                            word = word,
                            state = state,
                            onRate = vm::rate,
                            onUndo = vm::undo,
                            onSpeak = onSpeak,
                            modifier = Modifier.fillMaxSize(),
                        )
                        StudyMode.CHOICE -> ChoiceStudyMode(
                            word = word,
                            allWords = words,
                            state = state,
                            onChoose = { index, correct -> vm.choose(index, correct) },
                            onNext = vm::nextAfterAnswer,
                            onSpeak = onSpeak,
                            modifier = Modifier.fillMaxSize(),
                        )
                        StudyMode.SPELLING -> SpellingStudyMode(
                            word = word,
                            onRate = vm::rate,
                            onSpeak = onSpeak,
                            modifier = Modifier.fillMaxSize(),
                        )
                        StudyMode.PRONUNCIATION -> PronunciationPracticeScreen(
                            word = word,
                            onSpeak = onSpeak,
                            onRate = vm::rate,
                            modifier = Modifier.fillMaxSize(),
                        )
                        StudyMode.HANDWRITING -> HandwritingPracticeScreen(
                            word = word,
                            onRate = vm::rate,
                            onSpeak = onSpeak,
                            modifier = Modifier.fillMaxSize(),
                        )
                        StudyMode.GAME -> WordGameScreen(
                            words = listOfNotNull(word),
                            currentIndex = 0,
                            onRate = vm::rate,
                            onSpeak = onSpeak,
                            modifier = Modifier.fillMaxSize(),
                        )
                        StudyMode.DICTATION -> DictationStudyMode(
                            word = word,
                            state = state,
                            onTextChange = vm::updateDictation,
                            onSubmit = vm::submitDictation,
                            onNext = vm::nextAfterAnswer,
                            onSpeak = onSpeak,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudyHeader(
    state: StudyUiState,
    total: Int,
    backdrop: PrismalBackdrop,
    onRestart: () -> Unit,
) {
    val colors = WgTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SESSION 01",
                    style = WgType.eyebrow,
                    color = colors.blue,
                )
                Box(
                    Modifier
                        .padding(horizontal = 7.dp)
                        .size(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.inkTertiary),
                )
                Text(
                    text = "CORE SET",
                    style = WgType.eyebrow,
                    color = colors.inkTertiary,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "背单词",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.ink,
            )
            Text(
                text = "把注意力放在眼前这一个词上。",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkSecondary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (state.finished) "$total / $total" else "${(state.index + 1).coerceAtMost(total)} / $total",
                style = WgType.label,
                color = colors.ink,
            )
            Spacer(Modifier.height(5.dp))
            GlassIconButton(
                backdrop = backdrop,
                icon = Icons.Rounded.RestartAlt,
                contentDescription = "重新开始本组",
                onClick = onRestart,
                size = 38.dp,
            )
        }
    }
}

@Composable
private fun StudyModeSelector(
    selected: StudyMode,
    onSelect: (StudyMode) -> Unit,
) {
    val colors = WgTheme.colors
    val haptics = LocalHapticFeedback.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        StudyMode.entries.forEach { mode ->
            val active = mode == selected
            val icon = modeIcon(mode)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (active) {
                            Brush.verticalGradient(listOf(colors.blue, colors.blueDeep))
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    colors.glassFill,
                                    colors.glassFill.copy(alpha = 0.45f),
                                ),
                            )
                        },
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Tab,
                        onClick = {
                            if (!active) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelect(mode)
                            }
                        },
                    )
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (active) Color.White else colors.inkSecondary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.size(5.dp))
                Text(
                    text = mode.label,
                    color = if (active) Color.White else colors.inkSecondary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}

private fun modeIcon(mode: StudyMode): ImageVector = when (mode) {
    StudyMode.CARD -> Icons.Rounded.Style
    StudyMode.SWIPE -> Icons.Rounded.Swipe
    StudyMode.CHOICE -> Icons.Rounded.Quiz
    StudyMode.DICTATION -> Icons.Rounded.Keyboard
    StudyMode.SPELLING -> Icons.Rounded.Edit
    StudyMode.PRONUNCIATION -> Icons.Rounded.Mic
    StudyMode.HANDWRITING -> Icons.Rounded.Brush
    StudyMode.GAME -> Icons.Rounded.SportsEsports
}


