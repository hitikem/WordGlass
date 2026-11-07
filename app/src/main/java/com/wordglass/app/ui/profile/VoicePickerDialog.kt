package com.wordglass.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wordglass.app.core.design.WgTheme
import com.wordglass.app.model.VoiceProfile
import com.wordglass.app.model.VoicePresets

/**
 * Voice selection dialog with built-in preview.
 *
 * Shows all free voices: Youdao real-human pronunciation + all installed system TTS voices.
 * Each row has a play button for preview and a radio to select.
 */
@Composable
fun VoicePickerDialog(
    currentVoiceId: String,
    availableVoices: List<VoiceProfile>,
    isLoadingVoices: Boolean,
    onPreview: (VoiceProfile) -> Unit,
    onSelect: (VoiceProfile) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = WgTheme.colors
    var selectedId by remember { mutableStateOf(currentVoiceId) }
    var previewingId by remember { mutableStateOf<String?>(null) }

    // Auto-clear previewing indicator after 3s
    LaunchedEffect(previewingId) {
        if (previewingId != null) {
            kotlinx.coroutines.delay(3000)
            previewingId = null
        }
    }

    val allVoices = remember(availableVoices) {
        buildList {
            add(VoicePresets.YOUDAO_REAL)
            addAll(availableVoices)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceElevated,
        titleContentColor = colors.ink,
        textContentColor = colors.inkSecondary,
        title = {
            Text(text = "选择音色", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column {
                Text(
                    text = "点击 🔊 试听，点击选中音色。",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkTertiary,
                )
                Spacer(Modifier.height(8.dp))

                if (isLoadingVoices) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colors.blue,
                            strokeWidth = 2.dp,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.height(320.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(allVoices, key = { it.id }) { voice ->
                            VoiceRow(
                                voice = voice,
                                isSelected = voice.id == selectedId,
                                isPreviewing = voice.id == previewingId,
                                onClick = {
                                    selectedId = voice.id
                                    onSelect(voice)
                                },
                                onPreview = {
                                    previewingId = voice.id
                                    onPreview(voice)
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成", color = colors.blue)
            }
        },
    )
}

@Composable
private fun VoiceRow(
    voice: VoiceProfile,
    isSelected: Boolean,
    isPreviewing: Boolean,
    onClick: () -> Unit,
    onPreview: () -> Unit,
) {
    val colors = WgTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) colors.blue.copy(alpha = 0.10f)
                else Color.Transparent,
            )
            .border(
                1.dp,
                if (isSelected) colors.blue else colors.divider,
                RoundedCornerShape(10.dp),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Radio indicator
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .border(
                    2.dp,
                    if (isSelected) colors.blue else colors.inkTertiary,
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(colors.blue, CircleShape),
                )
            }
        }
        Spacer(Modifier.width(10.dp))

        // Voice info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = voice.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.ink,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
                if (voice.isRealHuman) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.success.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "真人",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.success,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85f,
                        )
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${voice.genderLabel} · ${voice.locale}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.inkTertiary,
            )
        }

        // Preview button
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isPreviewing) colors.blue.copy(alpha = 0.15f)
                    else colors.ink.copy(alpha = 0.05f),
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onPreview() },
            contentAlignment = Alignment.Center,
        ) {
            if (isPreviewing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = colors.blue,
                    strokeWidth = 1.5.dp,
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.VolumeUp,
                    contentDescription = "试听",
                    tint = colors.inkSecondary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
