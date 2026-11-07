package com.wordglass.app.ui.search

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import com.wordglass.app.core.design.WgRadius
import com.wordglass.app.core.design.WgTheme
import com.wordglass.app.core.design.WgType
import com.wordglass.app.data.network.DictionaryApi
import kotlinx.coroutines.delay

/**
 * Online word search — like 百词斩's dictionary lookup.
 * Searches the local PC server or free public dictionary APIs.
 */
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    var query by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<DictionaryApi.LookupResult?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var searchHistory by remember { mutableStateOf(listOf<String>()) }

    // Auto-search after 500ms of typing
    LaunchedEffect(query) {
        if (query.length >= 2) {
            delay(500)
            isSearching = true
            result = DictionaryApi.lookup(query.trim())
            isSearching = false
            if (query.isNotBlank() && query.trim() !in searchHistory) {
                searchHistory = (listOf(query.trim()) + searchHistory).take(10)
            }
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
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "返回",
                    tint = colors.ink,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "查词",
                style = MaterialTheme.typography.titleLarge,
                color = colors.ink,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.divider, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = colors.inkTertiary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.ink),
                    cursorBrush = SolidColor(colors.blue),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text(
                                text = "输入英文单词或中文释义…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.inkTertiary,
                            )
                        }
                        inner()
                    },
                )
                if (query.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { query = "" },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "清除",
                            tint = colors.inkTertiary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Results
        if (isSearching) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = colors.blue, strokeWidth = 2.dp)
            }
        } else if (result != null && query.length >= 2) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                result?.let { res ->
                    // Word header
                    item {
                        WordHeader(result = res, onSpeak = onSpeak)
                    }

                    // English meanings
                    if (res.englishMeanings.isNotEmpty()) {
                        item {
                            SectionLabel("英文释义")
                        }
                        items(res.englishMeanings) { meaning ->
                            MeaningCard(meaning = meaning)
                        }
                    }

                    // Chinese meanings
                    if (res.chineseMeanings.isNotEmpty()) {
                        item {
                            SectionLabel("中文释义")
                        }
                        items(res.chineseMeanings) { cn ->
                            ChineseMeaningCard(cn)
                        }
                    }

                    // Phrases
                    if (res.phrases.isNotEmpty()) {
                        item {
                            SectionLabel("常用短语")
                        }
                        items(res.phrases) { phrase ->
                            PhraseCard(phrase = phrase)
                        }
                    }

                    // Root analysis
                    if (res.rootAnalysis.isNotBlank()) {
                        item {
                            SectionLabel("词根词缀分析")
                            RootCard(analysis = res.rootAnalysis)
                        }
                    }
                }
            }
        } else if (searchHistory.isNotEmpty()) {
            // Search history
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    SectionLabel("搜索历史")
                }
                items(searchHistory) { word ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surface)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { query = word }
                            .padding(14.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = colors.inkTertiary,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = word,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.ink,
                            )
                        }
                    }
                }
            }
        } else {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "输入单词开始查询",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.inkTertiary,
                )
            }
        }
    }
}

@Composable
private fun WordHeader(
    result: DictionaryApi.LookupResult,
    onSpeak: (String) -> Unit,
) {
    val colors = WgTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(
                text = result.word,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.ink,
                fontWeight = FontWeight.Bold,
            )
            if (result.phonetic.isNotBlank()) {
                Text(
                    text = result.phonetic,
                    style = WgType.phonetic,
                    color = colors.inkTertiary,
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(50))
                .background(colors.blue.copy(alpha = 0.12f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onSpeak(result.word) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.VolumeUp,
                contentDescription = "发音",
                tint = colors.blue,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val colors = WgTheme.colors
    Text(
        text = text,
        style = WgType.eyebrow,
        color = colors.inkTertiary,
    )
}

@Composable
private fun MeaningCard(meaning: DictionaryApi.LookupResult.Meaning) {
    val colors = WgTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .padding(14.dp),
    ) {
        if (meaning.partOfSpeech.isNotBlank()) {
            Text(
                text = meaning.partOfSpeech,
                style = MaterialTheme.typography.labelSmall,
                color = colors.blue,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
        }
        Text(
            text = meaning.definition,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink,
        )
        if (meaning.example.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "例：${meaning.example}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkSecondary,
            )
        }
    }
}

@Composable
private fun ChineseMeaningCard(cn: String) {
    val colors = WgTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = cn,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink,
        )
    }
}

@Composable
private fun PhraseCard(phrase: DictionaryApi.LookupResult.Phrase) {
    val colors = WgTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .padding(14.dp),
    ) {
        Column {
            Text(
                text = phrase.phrase,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.ink,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = phrase.meaning,
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkSecondary,
            )
        }
    }
}

@Composable
private fun RootCard(analysis: String) {
    val colors = WgTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.blue.copy(alpha = 0.06f))
            .border(1.dp, colors.blue.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) {
        Text(
            text = analysis,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink,
        )
    }
}
