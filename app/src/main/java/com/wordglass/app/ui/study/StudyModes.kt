package com.wordglass.app.ui.study

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.styropyr0.prismal.PrismalBackdrop
import com.wordglass.app.core.components.GhostButton
import com.wordglass.app.core.components.GlassCard
import com.wordglass.app.core.components.PrimaryButton
import com.wordglass.app.core.components.SegmentedGlass
import com.wordglass.app.core.design.WgMotion
import com.wordglass.app.core.design.WgRadius
import com.wordglass.app.core.design.WgTheme
import com.wordglass.app.core.design.WgType
import com.wordglass.app.model.StudyRating
import com.wordglass.app.model.Word
import com.wordglass.app.ui.StudyUiState
import kotlin.math.abs
import kotlin.math.sign
import kotlin.random.Random
import kotlinx.coroutines.launch

@Composable
fun CardStudyMode(
    word: Word?,
    state: StudyUiState,
    onFlip: () -> Unit,
    onRate: (StudyRating) -> Unit,
    onSpeak: (String) -> Unit,
    onFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (word == null) {
        MissingWord(modifier)
        return
    }
    Column(modifier) {
        FlippableWordCard(
            word = word,
            flipped = state.flipped,
            position = state.index + 1,
            onFlip = onFlip,
            onSpeak = onSpeak,
            onFavorite = onFavorite,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        AnimatedContent(
            targetState = state.flipped,
            transitionSpec = {
                (fadeIn(tween(180)) + scaleIn(initialScale = 0.98f)) togetherWith fadeOut(tween(120))
            },
            label = "cardActions",
        ) { flipped ->
            if (!flipped) {
                Column {
                    PrimaryButton(
                        text = "翻转查看释义",
                        onClick = onFlip,
                        icon = Icons.Rounded.TouchApp,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "轻触卡片也可翻转 · 先回忆，再看答案",
                        style = MaterialTheme.typography.bodySmall,
                        color = WgTheme.colors.inkTertiary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                Column {
                    Text(
                        text = "这个词，你记得多牢？",
                        style = MaterialTheme.typography.labelMedium,
                        color = WgTheme.colors.inkSecondary,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        textAlign = TextAlign.Center,
                    )
                    RatingRow(onRate = onRate)
                }
            }
        }
    }
}

@Composable
private fun FlippableWordCard(
    word: Word,
    flipped: Boolean,
    position: Int,
    onFlip: () -> Unit,
    onSpeak: (String) -> Unit,
    onFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(WgMotion.flip, easing = FastOutSlowInEasing),
        label = "cardFlip",
    )
    val density = LocalDensity.current.density

    Box(
        modifier = modifier.graphicsLayer {
            rotationY = rotation
            cameraDistance = 18f * density
            transformOrigin = TransformOrigin.Center
        },
    ) {
        if (rotation <= 90f) {
            CardFront(
                word = word,
                position = position,
                onFlip = onFlip,
                onSpeak = onSpeak,
                onFavorite = onFavorite,
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f },
            ) {
                CardBack(
                    word = word,
                    onFlip = onFlip,
                    onFavorite = onFavorite,
                )
            }
        }
    }
}

@Composable
private fun CardFront(
    word: Word,
    position: Int,
    onFlip: () -> Unit,
    onSpeak: (String) -> Unit,
    onFavorite: () -> Unit,
) {
    val colors = WgTheme.colors
    GlassCard(
        backdrop = com.wordglass.app.ui.LocalGlassBackdrop.current,
        modifier = Modifier.fillMaxSize(),
        cornerRadius = WgRadius.large,
        contentPadding = PaddingValues(0.dp),
        elevation = 18.dp,
        overlay = Brush.linearGradient(
            listOf(
                colors.blue.copy(alpha = if (colors.isDark) 0.13f else 0.055f),
                Color.Transparent,
                colors.cyan.copy(alpha = 0.05f),
            ),
        ),
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onFlip)
                    .padding(22.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colors.blue.copy(alpha = 0.10f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(colors.blue),
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(
                            text = "CORE ${position.toString().padStart(2, '0')}",
                            style = WgType.eyebrow,
                            color = colors.blue,
                            fontSize = 8.sp,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    BookmarkButton(word.favorite, onFavorite)
                }
                Spacer(Modifier.weight(1f))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = word.word,
                        style = WgType.wordHero,
                        color = colors.ink,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = word.phonetic,
                        style = WgType.phonetic,
                        color = colors.inkSecondary,
                    )
                    Spacer(Modifier.height(20.dp))
                    AudioOrb(
                        onSpeak = { onSpeak(word.word) },
                    )
                }
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "轻触卡片查看释义",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.inkTertiary,
                    )
                    Spacer(Modifier.size(5.dp))
                    Icon(
                        Icons.Rounded.TouchApp,
                        contentDescription = null,
                        tint = colors.inkTertiary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CardBack(
    word: Word,
    onFlip: () -> Unit,
    onFavorite: () -> Unit,
) {
    val colors = WgTheme.colors
    GlassCard(
        backdrop = com.wordglass.app.ui.LocalGlassBackdrop.current,
        modifier = Modifier.fillMaxSize(),
        cornerRadius = WgRadius.large,
        contentPadding = PaddingValues(0.dp),
        elevation = 18.dp,
        overlay = Brush.linearGradient(
            listOf(
                colors.glassStrongFill.copy(alpha = 0.18f),
                Color.Transparent,
                colors.blue.copy(alpha = 0.04f),
            ),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = word.word,
                        style = WgType.wordCard,
                        color = colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = word.phonetic,
                        style = WgType.phonetic,
                        color = colors.inkSecondary,
                    )
                }
                BookmarkButton(word.favorite, onFavorite)
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = "MEANING",
                style = WgType.eyebrow,
                color = colors.blue,
            )
            Spacer(Modifier.height(7.dp))
            Text(
                text = word.meaning,
                style = MaterialTheme.typography.titleLarge,
                color = colors.ink,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 29.sp,
            )

            if (word.example != null) {
                Spacer(Modifier.height(18.dp))
                Row {
                    Box(
                        Modifier
                            .padding(top = 3.dp)
                            .size(width = 2.dp, height = 42.dp)
                            .clip(RoundedCornerShape(50))
                            .background(colors.blue.copy(alpha = 0.30f)),
                    )
                    Spacer(Modifier.size(11.dp))
                    Column {
                        Text(
                            text = word.example,
                            style = WgType.englishBody,
                            color = colors.ink,
                            fontStyle = FontStyle.Italic,
                        )
                        if (word.translation != null) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = word.translation,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkSecondary,
                            )
                        }
                    }
                }
            }

            if (word.usage != null) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.warning.copy(alpha = 0.09f))
                        .border(
                            0.8.dp,
                            colors.warning.copy(alpha = 0.16f),
                            RoundedCornerShape(14.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = colors.warning,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = word.usage,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkSecondary,
                        lineHeight = 19.sp,
                    )
                }
            }

            if (word.root != null || word.affix != null) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = listOfNotNull(word.root, word.affix).joinToString("  ·  "),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkTertiary,
                )
            }

            Spacer(Modifier.height(18.dp))
            GhostButton(
                text = "返回正面",
                onClick = onFlip,
            )
        }
    }
}

@Composable
private fun BookmarkButton(
    favorite: Boolean,
    onClick: () -> Unit,
) {
    val colors = WgTheme.colors
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(colors.glassFill)
            .border(0.8.dp, colors.glassStroke.copy(alpha = 0.55f), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (favorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
            contentDescription = if (favorite) "取消收藏" else "收藏单词",
            tint = if (favorite) colors.blue else colors.inkSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun AudioOrb(
    onSpeak: () -> Unit,
    size: Int = 58,
) {
    val colors = WgTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = WgMotion.snappy(),
        label = "audioScale",
    )
    val haptics = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .size(size.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        colors.blue.copy(alpha = 0.20f),
                        colors.blue.copy(alpha = 0.08f),
                    ),
                ),
            )
            .border(0.8.dp, colors.blue.copy(alpha = 0.20f), CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSpeak()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Rounded.VolumeUp,
                contentDescription = "播放发音",
                tint = colors.blue,
                modifier = Modifier.size(21.dp),
            )
            Text(
                text = "US",
                style = WgType.eyebrow,
                color = colors.blue.copy(alpha = 0.80f),
                fontSize = 7.sp,
            )
        }
    }
}

@Composable
private fun RatingRow(onRate: (StudyRating) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        StudyRating.entries.forEach { rating ->
            val tint = ratingColor(rating)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(tint.copy(alpha = 0.10f))
                    .border(0.8.dp, tint.copy(alpha = 0.18f), RoundedCornerShape(15.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = { onRate(rating) },
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(tint),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = rating.shortLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = tint,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ratingColor(rating: StudyRating): Color = when (rating) {
    StudyRating.AGAIN -> WgTheme.colors.danger
    StudyRating.HARD -> WgTheme.colors.warning
    StudyRating.GOOD -> WgTheme.colors.blue
    StudyRating.EASY -> WgTheme.colors.success
}

@Composable
fun SwipeStudyMode(
    word: Word?,
    state: StudyUiState,
    onRate: (StudyRating) -> Unit,
    onUndo: () -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (word == null) {
        MissingWord(modifier)
        return
    }
    val colors = WgTheme.colors
    Column(modifier) {
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val widthPx = constraints.maxWidth.toFloat()
            var dragX by remember(word.id) { mutableFloatStateOf(0f) }
            val scope = rememberCoroutineScope()
            LaunchedEffect(word.id) { dragX = 0f }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(word.id, widthPx) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                val threshold = widthPx * 0.25f
                                if (abs(dragX) > threshold) {
                                    val direction = sign(dragX)
                                    scope.launch {
                                        animate(
                                            initialValue = dragX,
                                            targetValue = direction * widthPx * 1.5f,
                                            animationSpec = tween(200),
                                        ) { value, _ -> dragX = value }
                                        onRate(if (direction > 0) StudyRating.AGAIN else StudyRating.GOOD)
                                        dragX = 0f
                                    }
                                } else {
                                    scope.launch {
                                        animate(
                                            initialValue = dragX,
                                            targetValue = 0f,
                                            animationSpec = WgMotion.springy(),
                                        ) { value, _ -> dragX = value }
                                    }
                                }
                            },
                            onHorizontalDrag = { change, amount ->
                                change.consume()
                                dragX = (dragX + amount).coerceIn(
                                    -widthPx * 1.2f,
                                    widthPx * 1.2f,
                                )
                            },
                        )
                    }
                    .graphicsLayer {
                        translationX = dragX
                        rotationZ = (dragX / widthPx).coerceIn(-1f, 1f) * 7f
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    },
            ) {
                SwipeWordCard(word = word, onSpeak = onSpeak)
                SwipeStamp(
                    text = "认识",
                    tint = colors.success,
                    modifier = Modifier.align(Alignment.TopStart).padding(24.dp),
                    alpha = { (dragX / widthPx * 2f).coerceIn(0f, 1f) },
                    rotation = -13f,
                )
                SwipeStamp(
                    text = "不认识",
                    tint = colors.danger,
                    modifier = Modifier.align(Alignment.TopEnd).padding(24.dp),
                    alpha = { (-dragX / widthPx * 2f).coerceIn(0f, 1f) },
                    rotation = 13f,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SwipeActionButton(
                label = "不认识",
                icon = Icons.Rounded.Close,
                tint = colors.danger,
                modifier = Modifier.weight(1f),
                onClick = { onRate(StudyRating.AGAIN) },
            )
            SwipeActionButton(
                label = "认识",
                icon = Icons.Rounded.Check,
                tint = colors.success,
                modifier = Modifier.weight(1f),
                onClick = { onRate(StudyRating.GOOD) },
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.index > 0) {
                GhostButton(
                    text = "撤销",
                    icon = Icons.Rounded.Undo,
                    onClick = onUndo,
                )
            } else {
                Spacer(Modifier.size(1.dp))
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "左滑不认识  ·  右滑认识",
                style = MaterialTheme.typography.labelSmall,
                color = colors.inkTertiary,
            )
        }
    }
}

@Composable
private fun SwipeWordCard(
    word: Word,
    onSpeak: (String) -> Unit,
) {
    val colors = WgTheme.colors
    GlassCard(
        backdrop = com.wordglass.app.ui.LocalGlassBackdrop.current,
        modifier = Modifier.fillMaxSize(),
        cornerRadius = WgRadius.large,
        contentPadding = PaddingValues(0.dp),
        elevation = 18.dp,
        overlay = Brush.linearGradient(
            listOf(
                colors.blue.copy(alpha = 0.08f),
                Color.Transparent,
                colors.mint.copy(alpha = 0.04f),
            ),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SWIPE",
                    style = WgType.eyebrow,
                    color = colors.blue,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "凭第一反应",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkTertiary,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = word.word,
                style = WgType.wordHero,
                color = colors.ink,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = word.phonetic,
                style = WgType.phonetic,
                color = colors.inkSecondary,
            )
            Spacer(Modifier.height(18.dp))
            AudioOrb(onSpeak = { onSpeak(word.word) })
            Spacer(Modifier.weight(1f))
            Text(
                text = "你知道它的意思吗？",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkSecondary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "向左或向右拖动卡片",
                style = MaterialTheme.typography.labelSmall,
                color = colors.inkTertiary,
            )
        }
    }
}

@Composable
private fun SwipeStamp(
    text: String,
    tint: Color,
    modifier: Modifier = Modifier,
    alpha: () -> Float,
    rotation: Float,
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha()
                rotationZ = rotation
            }
            .border(2.dp, tint.copy(alpha = 0.80f), RoundedCornerShape(10.dp))
            .padding(horizontal = 13.dp, vertical = 7.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = tint,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SwipeActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(tint.copy(alpha = 0.09f))
            .border(1.dp, tint.copy(alpha = 0.17f), RoundedCornerShape(15.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(17.dp))
        Spacer(Modifier.size(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = tint,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun ChoiceStudyMode(
    word: Word?,
    allWords: List<Word>,
    state: StudyUiState,
    onChoose: (Int, Boolean) -> Unit,
    onNext: () -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (word == null) {
        MissingWord(modifier)
        return
    }
    val colors = WgTheme.colors
    val options = remember(word.id, allWords.size) {
        val others = allWords
            .filter { it.id != word.id && it.meaning.isNotBlank() }
            .shuffled(Random(word.id))
            .take(3)
        (listOf(word) + others).shuffled(Random(word.id + 17))
    }
    val selected = state.selectedChoice
    val correctIndex = options.indexOfFirst { it.id == word.id }
    val isAnswered = selected != null

    Column(modifier) {
        GlassCard(
            backdrop = com.wordglass.app.ui.LocalGlassBackdrop.current,
            modifier = Modifier.fillMaxWidth().height(136.dp),
            cornerRadius = WgRadius.card,
            contentPadding = PaddingValues(18.dp),
            elevation = 12.dp,
        ) {
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "选择正确释义",
                        style = WgType.eyebrow,
                        color = colors.blue,
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = word.word,
                        style = WgType.wordCard,
                        color = colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = word.phonetic,
                        style = WgType.phonetic,
                        color = colors.inkSecondary,
                    )
                }
                AudioOrb(onSpeak = { onSpeak(word.word) }, size = 50)
            }
        }
        Spacer(Modifier.height(13.dp))
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            options.forEachIndexed { index, option ->
                val isCorrect = index == correctIndex
                val isSelected = selected == index
                val revealed = isAnswered
                val tint = when {
                    revealed && isCorrect -> colors.success
                    revealed && isSelected -> colors.danger
                    else -> colors.inkSecondary
                }
                val fill = when {
                    revealed && isCorrect -> colors.success.copy(alpha = 0.11f)
                    revealed && isSelected -> colors.danger.copy(alpha = 0.09f)
                    else -> colors.glassFill
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 58.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(fill)
                        .border(
                            0.8.dp,
                            if (revealed && (isCorrect || isSelected)) tint.copy(alpha = 0.24f)
                            else colors.glassStroke.copy(alpha = 0.45f),
                            RoundedCornerShape(16.dp),
                        )
                        .clickable(
                            enabled = !isAnswered,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = { onChoose(index, option.id == word.id) },
                        )
                        .padding(horizontal = 13.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (revealed && isCorrect) tint.copy(alpha = 0.18f)
                                else colors.ink.copy(alpha = 0.055f),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = ('A' + index).toString(),
                            style = WgType.label,
                            color = if (revealed && isCorrect) tint else colors.inkSecondary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.size(11.dp))
                    Text(
                        text = option.meaning,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (revealed && isCorrect) tint else colors.ink,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (revealed && isCorrect) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = isAnswered,
            enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.98f),
            exit = fadeOut(tween(120)),
        ) {
            Column {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (selected == correctIndex) "答对了，记忆又稳了一点。" else "正确答案：${options[correctIndex].meaning}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected == correctIndex) colors.success else colors.danger,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                PrimaryButton(
                    text = "下一题",
                    onClick = onNext,
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun DictationStudyMode(
    word: Word?,
    state: StudyUiState,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (word == null) {
        MissingWord(modifier)
        return
    }
    val colors = WgTheme.colors
    var listenMode by rememberSaveable { mutableStateOf(true) }
    val focusManager = LocalFocusManager.current
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = modifier.imePadding(),
    ) {
        SegmentedGlass(
            options = listOf("听音拼写", "释义拼写"),
            selectedIndex = if (listenMode) 0 else 1,
            onSelect = { listenMode = it == 0 },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(13.dp))
        GlassCard(
            backdrop = com.wordglass.app.ui.LocalGlassBackdrop.current,
            modifier = Modifier.fillMaxWidth().height(198.dp),
            cornerRadius = WgRadius.card,
            contentPadding = PaddingValues(20.dp),
            elevation = 12.dp,
        ) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = if (listenMode) "LISTEN & TYPE" else "MEANING → ENGLISH",
                    style = WgType.eyebrow,
                    color = colors.blue,
                )
                Spacer(Modifier.height(12.dp))
                if (listenMode) {
                    AudioOrb(onSpeak = { onSpeak(word.word) }, size = 66)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "点击播放，听清发音",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkSecondary,
                    )
                } else {
                    Text(
                        text = word.meaning,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.ink,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "根据释义拼出英文单词",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkSecondary,
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        val borderColor = when {
            !state.dictationChecked -> colors.glassStroke.copy(alpha = 0.55f)
            state.dictationCorrect == true -> colors.success.copy(alpha = 0.50f)
            else -> colors.danger.copy(alpha = 0.50f)
        }
        BasicTextField(
            value = state.dictation,
            onValueChange = onTextChange,
            enabled = !state.dictationChecked,
            singleLine = true,
            textStyle = WgType.wordCard.copy(
                color = colors.ink,
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
            ),
            cursorBrush = SolidColor(colors.blue),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (state.dictation.isNotBlank() && !state.dictationChecked) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        focusManager.clearFocus()
                        onSubmit()
                    }
                },
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(colors.glassFill)
                .border(1.dp, borderColor, RoundedCornerShape(17.dp))
                .padding(horizontal = 16.dp, vertical = 16.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.dictation.isEmpty()) {
                        Text(
                            text = "输入英文单词",
                            style = WgType.englishBody,
                            color = colors.inkTertiary,
                        )
                    }
                    innerTextField()
                }
            },
        )
        Spacer(Modifier.height(12.dp))
        if (state.dictationChecked) {
            Text(
                text = if (state.dictationCorrect == true) {
                    "拼写正确 · ${word.word}"
                } else {
                    "正确拼写：${word.word}  ·  ${word.meaning}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (state.dictationCorrect == true) colors.success else colors.danger,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            PrimaryButton(
                text = "下一个词",
                onClick = {
                    focusManager.clearFocus()
                    onNext()
                },
                icon = Icons.AutoMirrored.Rounded.ArrowForward,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            PrimaryButton(
                text = "检查拼写",
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    focusManager.clearFocus()
                    onSubmit()
                },
                enabled = state.dictation.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (listenMode) "也可以切换到“释义拼写”做反向回忆" else "提示：音标会在检查后显示",
            style = MaterialTheme.typography.labelSmall,
            color = colors.inkTertiary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun StudyCompletion(
    known: Int,
    unknown: Int,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    GlassCard(
        backdrop = com.wordglass.app.ui.LocalGlassBackdrop.current,
        modifier = modifier,
        cornerRadius = WgRadius.large,
        contentPadding = PaddingValues(24.dp),
        elevation = 18.dp,
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(colors.success.copy(alpha = 0.12f))
                    .border(1.dp, colors.success.copy(alpha = 0.20f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = colors.success,
                    modifier = Modifier.size(36.dp),
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "本组完成",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.ink,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "记忆不是一次完成，而是每一次回来。",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CompletionMetric(
                    value = "$known",
                    label = "认识",
                    tint = colors.success,
                    modifier = Modifier.weight(1f),
                )
                CompletionMetric(
                    value = "$unknown",
                    label = "需巩固",
                    tint = colors.warning,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(22.dp))
            PrimaryButton(
                text = "再练一组",
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CompletionMetric(
    value: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(tint.copy(alpha = 0.08f))
            .border(0.8.dp, tint.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = WgType.numeric,
            color = tint,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.inkSecondary,
        )
    }
}

@Composable
private fun MissingWord(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Text(
            text = "没有可学习的词条",
            style = MaterialTheme.typography.bodyLarge,
            color = WgTheme.colors.inkSecondary,
        )
    }
}
