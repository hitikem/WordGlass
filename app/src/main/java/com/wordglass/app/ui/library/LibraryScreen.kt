package com.wordglass.app.ui.library

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.styropyr0.prismal.PrismalBackdrop
import com.wordglass.app.core.components.GlassCard
import com.wordglass.app.core.components.GlassChip
import com.wordglass.app.core.components.IconCircle
import com.wordglass.app.core.components.PrimaryButton
import com.wordglass.app.core.components.ThinProgressBar
import com.wordglass.app.core.design.WgRadius
import com.wordglass.app.core.design.WgSpace
import com.wordglass.app.core.design.WgTheme
import com.wordglass.app.core.design.WgType
import com.wordglass.app.model.LibraryFilter
import com.wordglass.app.model.LibrarySort
import com.wordglass.app.model.Word
import com.wordglass.app.model.WordMastery
import com.wordglass.app.ui.WordGlassViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    backdrop: PrismalBackdrop,
    vm: WordGlassViewModel,
    onSpeak: (String) -> Unit,
    onStartStudy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    val allWords by vm.words.collectAsStateWithLifecycle()
    val words by vm.visibleWords.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val filter by vm.filter.collectAsStateWithLifecycle()
    val sort by vm.sort.collectAsStateWithLifecycle()
    var selectedWord by remember { mutableStateOf<Word?>(null) }
    var sortMenu by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "LIBRARY",
                        style = WgType.eyebrow,
                        color = colors.blue,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "复习词库",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.ink,
                    )
                    Text(
                        text = "所有词，都在一个清晰的视图里。",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkSecondary,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.blue.copy(alpha = 0.09f))
                        .padding(horizontal = 11.dp, vertical = 7.dp),
                ) {
                    Text(
                        text = "${allWords.size} 个本地词条",
                        style = WgType.label,
                        color = colors.blue,
                        fontSize = 10.sp,
                    )
                }
            }
        }

        item {
            SearchField(
                value = query,
                onValueChange = vm::setQuery,
                onClear = { vm.setQuery("") },
                focusManager = focusManager,
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                contentPadding = PaddingValues(horizontal = 1.dp),
            ) {
                items(LibraryFilter.entries) { item ->
                    GlassChip(
                        text = item.label,
                        selected = filter == item,
                        onClick = { vm.setFilter(item) },
                    )
                }
            }
        }

        item {
            LibrarySummary(
                backdrop = backdrop,
                words = allWords,
                visibleCount = words.size,
            )
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = if (query.isBlank()) "词条" else "搜索结果",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.ink,
                    )
                    Text(
                        text = "${words.size} 个匹配",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkTertiary,
                    )
                }
                Box {
                    GlassChip(
                        text = sort.label,
                        onClick = { sortMenu = true },
                        leading = {
                            Icon(
                                Icons.Rounded.Sort,
                                contentDescription = null,
                                tint = colors.inkSecondary,
                                modifier = Modifier.size(13.dp),
                            )
                        },
                    )
                    DropdownMenu(
                        expanded = sortMenu,
                        onDismissRequest = { sortMenu = false },
                        containerColor = colors.surfaceElevated,
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        LibrarySort.entries.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (option == sort) colors.blue else colors.ink,
                                    )
                                },
                                onClick = {
                                    vm.setSort(option)
                                    sortMenu = false
                                },
                            )
                        }
                    }
                }
            }
        }

        if (words.isEmpty()) {
            item {
                EmptyLibrary()
            }
        } else {
            items(words, key = { it.id }) { word ->
                WordListRow(
                    word = word,
                    onClick = { selectedWord = word },
                    onToggleFavorite = { vm.toggleFavorite(word.id) },
                )
            }
        }

        item {
            Text(
                text = "LOCAL ROOM · 来自 887 词 PDF 的 882 条可溯源词条",
                style = WgType.eyebrow,
                color = colors.inkTertiary,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 6.dp),
            )
        }
    }

    selectedWord?.let { word ->
        ModalBottomSheet(
            onDismissRequest = { selectedWord = null },
            sheetState = sheetState,
            containerColor = colors.surface,
            contentColor = colors.ink,
            scrimColor = Color.Black.copy(alpha = 0.38f),
            tonalElevation = 0.dp,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            dragHandle = {
                Box(
                    Modifier
                        .padding(top = 12.dp, bottom = 4.dp)
                        .width(38.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(colors.inkTertiary.copy(alpha = 0.32f)),
                )
            },
        ) {
            WordDetailSheet(
                word = word,
                onSpeak = onSpeak,
                onToggleFavorite = { vm.toggleFavorite(word.id) },
                onSetMastery = { level -> vm.setMastery(word.id, level) },
                onStartStudy = {
                    selectedWord = null
                    onStartStudy()
                },
            )
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager,
) {
    val colors = WgTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(colors.glassFill)
            .border(0.8.dp, colors.glassStroke.copy(alpha = 0.55f), RoundedCornerShape(17.dp))
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = colors.inkTertiary,
            modifier = Modifier.size(19.dp),
        )
        Spacer(Modifier.size(10.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.ink),
            cursorBrush = SolidColor(colors.blue),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search,
            ),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = "搜索单词或中文释义",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.inkTertiary,
                        )
                    }
                    inner()
                }
            },
        )
        if (value.isNotEmpty()) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onClear),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "清空搜索",
                    tint = colors.inkSecondary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun LibrarySummary(
    backdrop: PrismalBackdrop,
    words: List<Word>,
    visibleCount: Int,
) {
    val colors = WgTheme.colors
    val mastered = words.count { it.mastery >= 4 }
    val favorites = words.count { it.favorite }
    GlassCard(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth().height(132.dp),
        cornerRadius = WgRadius.card,
        contentPadding = PaddingValues(17.dp),
        elevation = 10.dp,
    ) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "当前本地词库",
                    style = WgType.eyebrow,
                    color = colors.blue,
                )
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${words.size}",
                        style = WgType.numericHero,
                        color = colors.ink,
                        fontSize = 36.sp,
                    )
                    Text(
                        text = " 条 · 目标 882 条",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkSecondary,
                        modifier = Modifier.padding(bottom = 5.dp),
                    )
                }
                Spacer(Modifier.height(7.dp))
                ThinProgressBar(
                    progress = if (words.isEmpty()) 0f else mastered.toFloat() / words.size,
                    modifier = Modifier.width(136.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "已掌握 $mastered · 收藏 $favorites · 当前筛选 $visibleCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkTertiary,
                )
            }
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(colors.blue.copy(alpha = 0.16f), colors.cyan.copy(alpha = 0.10f)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Aa",
                    style = WgType.wordCard,
                    color = colors.blue,
                    fontSize = 24.sp,
                )
            }
        }
    }
}

@Composable
private fun WordListRow(
    word: Word,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val colors = WgTheme.colors
    val mastery = WordMastery.from(word.mastery)
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                )
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = word.word,
                        style = WgType.wordRow,
                        color = colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (word.phonetic.isNotBlank()) {
                        Spacer(Modifier.size(7.dp))
                        Text(
                            text = "/${word.phonetic}/",
                            style = WgType.phonetic,
                            color = colors.inkTertiary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = word.meaning,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.size(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = mastery.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkTertiary,
                    fontSize = 10.sp,
                )
                Spacer(Modifier.height(2.dp))
                Box(
                    Modifier
                        .size(26.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = onToggleFavorite,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (word.favorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = if (word.favorite) "取消收藏" else "收藏",
                        tint = if (word.favorite) colors.blue else colors.inkTertiary,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.divider),
        )
    }
}

@Composable
private fun masteryTint(level: Int): Color = when {
    level >= 5 -> WgTheme.colors.mint
    level >= 4 -> WgTheme.colors.success
    level >= 2 -> WgTheme.colors.blue
    level >= 1 -> WgTheme.colors.warning
    else -> WgTheme.colors.inkTertiary
}

@Composable
private fun EmptyLibrary() {
    val colors = WgTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 42.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconCircle(
            icon = Icons.Rounded.Search,
            tint = colors.blue,
            size = 50.dp,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "没有匹配的词",
            style = MaterialTheme.typography.titleMedium,
            color = colors.ink,
        )
        Text(
            text = "试试更短的关键词，或切换筛选条件。",
            style = MaterialTheme.typography.bodySmall,
            color = colors.inkSecondary,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WordDetailSheet(
    word: Word,
    onSpeak: (String) -> Unit,
    onToggleFavorite: () -> Unit,
    onSetMastery: (Int) -> Unit,
    onStartStudy: () -> Unit,
) {
    val colors = WgTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(bottom = 26.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = word.word,
                    style = WgType.wordHero,
                    color = colors.ink,
                    fontSize = 38.sp,
                )
                Text(
                    text = word.phonetic,
                    style = WgType.phonetic,
                    color = colors.inkSecondary,
                )
            }
            IconCircle(
                icon = Icons.Rounded.VolumeUp,
                tint = colors.blue,
                size = 44.dp,
                modifier = Modifier.clickable { onSpeak(word.word) },
            )
            Spacer(Modifier.size(8.dp))
            IconCircle(
                icon = if (word.favorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                tint = if (word.favorite) colors.blue else colors.inkSecondary,
                size = 44.dp,
                modifier = Modifier.clickable(onClick = onToggleFavorite),
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = "释义",
            style = WgType.eyebrow,
            color = colors.blue,
        )
        Text(
            text = word.meaning,
            style = MaterialTheme.typography.titleLarge,
            color = colors.ink,
            modifier = Modifier.padding(top = 5.dp),
        )
        if (word.example != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = word.example,
                style = WgType.englishBody,
                color = colors.ink,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            )
            if (word.translation != null) {
                Text(
                    text = word.translation,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkSecondary,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
        if (word.usage != null) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = word.usage,
                style = MaterialTheme.typography.bodySmall,
                color = colors.warning,
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "掌握程度",
            style = MaterialTheme.typography.titleMedium,
            color = colors.ink,
        )
        Spacer(Modifier.height(9.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            (0..5).forEach { level ->
                val selected = word.mastery == level
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (selected) colors.blue else colors.ink.copy(alpha = 0.05f))
                        .border(
                            0.8.dp,
                            if (selected) Color.Transparent else colors.glassStroke.copy(alpha = 0.35f),
                            RoundedCornerShape(11.dp),
                        )
                        .clickable { onSetMastery(level) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$level",
                        style = WgType.label,
                        color = if (selected) Color.White else colors.inkSecondary,
                    )
                }
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text = WordMastery.from(word.mastery).label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.inkTertiary,
        )
        Spacer(Modifier.height(20.dp))
        PrimaryButton(
            text = "开始复习这个词",
            onClick = onStartStudy,
            icon = Icons.AutoMirrored.Rounded.ArrowForward,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

