package com.wordglass.app.ui.study

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Refresh
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path as ComposePath
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wordglass.app.core.design.WgRadius
import com.wordglass.app.core.design.WgTheme
import com.wordglass.app.core.design.WgType
import com.wordglass.app.model.StudyRating
import com.wordglass.app.model.Word

/**
 * Handwriting practice: write the word on screen, then reveal and compare.
 * Like writing practice in Chinese calligraphy apps.
 */
@Composable
fun HandwritingPracticeScreen(
    word: Word?,
    onRate: (StudyRating) -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WgTheme.colors
    if (word == null) return

    var paths by remember { mutableStateOf(listOf<ComposePath>()) }
    var currentPath by remember { mutableStateOf<ComposePath?>(null) }
    var showReference by remember { mutableStateOf(false) }
    var isDrawing by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header
        Text("手写练习", style = WgType.eyebrow, color = colors.inkTertiary)
        Spacer(Modifier.height(8.dp))

        // Show meaning (prompt)
        Text(
            text = word.meaning,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.ink,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "在下方手写英文单词",
            style = MaterialTheme.typography.bodySmall,
            color = colors.inkTertiary,
        )
        Spacer(Modifier.height(16.dp))

        // Drawing canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(2.dp, colors.divider, RoundedCornerShape(16.dp)),
        ) {
            // Guide lines (like ruled paper)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val lineHeight = size.height / 3f
                for (i in 1..2) {
                    drawLine(
                        color = Color(0xFFE0E0E0),
                        start = Offset(0f, lineHeight * i),
                        end = Offset(size.width, lineHeight * i),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            }

            // User's drawing
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPath = ComposePath().apply { moveTo(offset.x, offset.y) }
                                isDrawing = true
                            },
                            onDrag = { change, _ ->
                                currentPath?.lineTo(change.position.x, change.position.y)
                                paths = paths.toList()
                            },
                            onDragEnd = {
                                currentPath?.let { paths = paths + it }
                                currentPath = null
                                isDrawing = false
                            },
                        )
                    },
            ) {
                // Completed paths
                paths.forEach { path ->
                    drawPath(
                        path = path,
                        color = Color(0xFF1565C0),
                        style = Stroke(
                            width = 5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )
                }
                // Current drawing path
                currentPath?.let { path ->
                    drawPath(
                        path = path,
                        color = Color(0xFF1565C0),
                        style = Stroke(
                            width = 5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )
                }
            }

            // Reference word overlay (faded)
            if (showReference) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.displayMedium,
                        color = Color(0xFF1565C0).copy(alpha = 0.15f),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Control buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Clear button
            ActionSmall(
                icon = Icons.Rounded.Delete,
                label = "清除",
                color = colors.danger,
                onClick = {
                    paths = emptyList()
                    currentPath = null
                },
            )

            // Show reference button
            ActionSmall(
                icon = Icons.Rounded.Refresh,
                label = if (showReference) "隐藏" else "提示",
                color = colors.warning,
                onClick = { showReference = !showReference },
            )

            // Listen button
            ActionSmall(
                icon = Icons.Rounded.Refresh,
                label = "听发音",
                color = colors.blue,
                onClick = { onSpeak(word.word) },
            )
        }

        Spacer(Modifier.height(16.dp))

        // Check button
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ActionSmall(
                icon = Icons.Rounded.Check,
                label = "写对了",
                color = colors.success,
                onClick = { onRate(StudyRating.GOOD) },
            )
            ActionSmall(
                icon = Icons.Rounded.Refresh,
                label = "写错了",
                color = colors.danger,
                onClick = { onRate(StudyRating.AGAIN) },
            )
        }

        Spacer(Modifier.height(12.dp))

        // Show correct answer
        if (showReference) {
            Text(
                text = "正确写法: ${word.word}",
                style = MaterialTheme.typography.titleMedium,
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
        }
    }
}

@Composable
private fun ActionSmall(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    val colors = WgTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
    }
}
