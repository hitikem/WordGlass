package com.wordglass.app.ui.settings

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wordglass.app.core.design.WgTheme
import com.wordglass.app.core.design.WgType
import com.wordglass.app.model.FreeVoices

/**
 * Free voice list: shows ALL free voices from all platforms.
 */
@Composable
fun FreeVoiceListScreen(
    selectedVoiceId: String,
    onSelect: (FreeVoices.VoiceOption) -> Unit,
    onPreview: (FreeVoices.VoiceOption) -> Unit,
    onBack: () -> Unit,
) {
    val colors = WgTheme.colors
    val platforms = FreeVoices.byPlatform()

    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onBack() },
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.ArrowBack, "返回", tint = colors.ink) }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("全部免费音色", style = MaterialTheme.typography.titleLarge, color = colors.ink, fontWeight = FontWeight.SemiBold)
                Text("${FreeVoices.ALL_FREE_VOICES.size} 个免费音色可选", style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            platforms.forEach { (platform, voices) ->
                item {
                    Text(
                        text = platform,
                        style = WgType.eyebrow,
                        color = colors.inkTertiary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    )
                }
                items(voices) { voice ->
                    val selected = voice.id == selectedVoiceId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) colors.blue.copy(alpha = 0.10f) else colors.surface)
                            .border(
                                if (selected) 1.5.dp else 0.5.dp,
                                if (selected) colors.blue else colors.divider,
                                RoundedCornerShape(12.dp),
                            )
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                onSelect(voice)
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Selection indicator
                        if (selected) {
                            Box(Modifier.size(20.dp).background(colors.blue, CircleShape), Alignment.Center) {
                                Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        } else {
                            Box(Modifier.size(20.dp).border(1.5.dp, colors.inkTertiary, CircleShape))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(voice.displayName, style = MaterialTheme.typography.bodyMedium, color = colors.ink, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                            Text("${voice.platform} · ${voice.gender}声 · ${voice.locale}", style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
                        }
                        // Preview button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.blue.copy(alpha = 0.10f))
                                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                    onPreview(voice)
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.VolumeUp, "试听", tint = colors.blue, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
