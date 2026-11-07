package com.wordglass.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wordglass.app.core.design.WgTheme
import kotlin.math.abs

/**
 * Alarm-clock style scroll wheel number picker.
 * Drag vertically to change the value, like setting time in the Android alarm app.
 */
@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int = 5,
    maxValue: Int = 100,
    modifier: Modifier = Modifier,
    label: String = "",
    unit: String = "",
) {
    val colors = WgTheme.colors
    var currentValue by remember(value) { mutableIntStateOf(value) }
    val itemHeight = 48.dp
    val visibleItems = 5
    val halfVisible = visibleItems / 2

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.inkTertiary,
            )
            Spacer(Modifier.height(8.dp))
        }

        // Picker wheel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * visibleItems)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.blue.copy(alpha = 0.06f))
                .pointerInput(Unit) {
                    var accumulatedDrag = 0f
                    detectVerticalDragGestures(
                        onDragStart = { accumulatedDrag = 0f },
                        onVerticalDrag = { _, dragAmount ->
                            accumulatedDrag += dragAmount
                            val step = itemHeight.toPx() * 0.4f
                            if (abs(accumulatedDrag) > step) {
                                val direction = if (accumulatedDrag > 0) -1 else 1
                                val newValue = (currentValue + direction).coerceIn(minValue, maxValue)
                                if (newValue != currentValue) {
                                    currentValue = newValue
                                    onValueChange(newValue)
                                }
                                accumulatedDrag = 0f
                            }
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            // Center highlight band
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.blue.copy(alpha = 0.12f)),
            )

            // Numbers
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                for (offset in -halfVisible..halfVisible) {
                    val itemValue = currentValue + offset
                    val isCenter = offset == 0
                    val isInRange = itemValue in minValue..maxValue
                    Box(
                        modifier = Modifier
                            .height(itemHeight)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isInRange) {
                            Text(
                                text = "$itemValue $unit",
                                style = if (isCenter)
                                    MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = colors.ink,
                                    )
                                else
                                    MaterialTheme.typography.titleMedium.copy(
                                        color = colors.inkTertiary.copy(alpha = 0.5f),
                                    ),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Quick +/- buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PickerButton(
                text = "−",
                onClick = {
                    val newValue = (currentValue - 1).coerceIn(minValue, maxValue)
                    currentValue = newValue
                    onValueChange(newValue)
                },
            )
            Text(
                text = "$currentValue $unit",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.ink,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.Center,
            )
            PickerButton(
                text = "+",
                onClick = {
                    val newValue = (currentValue + 1).coerceIn(minValue, maxValue)
                    currentValue = newValue
                    onValueChange(newValue)
                },
            )
        }
    }
}

@Composable
private fun PickerButton(
    text: String,
    onClick: () -> Unit,
) {
    val colors = WgTheme.colors
    Box(
        modifier = Modifier
            .height(36.dp)
            .width(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.blue.copy(alpha = 0.10f))
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, _ -> }
            }
            .then(
                Modifier.pointerInput(text) {
                    detectVerticalDragGestures { _, _ -> onClick() }
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = colors.blue,
            fontWeight = FontWeight.Bold,
        )
    }
}
