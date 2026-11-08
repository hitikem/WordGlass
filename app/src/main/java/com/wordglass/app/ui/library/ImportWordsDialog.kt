package com.wordglass.app.ui.library

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wordglass.app.core.design.WgTheme

/**
 * Import word lists from PDF/XLSX/TXT files.
 * Shows a dialog with format instructions and file picker.
 */
@Composable
fun ImportWordsDialog(
    onDismiss: () -> Unit,
    onImport: (Uri) -> Unit,
) {
    val colors = WgTheme.colors
    var importResult by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImport(uri)
        }
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceElevated,
        titleContentColor = colors.ink,
        textContentColor = colors.inkSecondary,
        title = { Text("导入词库", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text("支持以下格式：", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))

                FormatRow(Icons.Rounded.Description, "TXT / CSV", "每行：单词,音标,释义")
                Spacer(Modifier.height(8.dp))
                FormatRow(Icons.Rounded.TableChart, "XLSX", "第1列=单词，第2列=音标，第3列=释义")
                Spacer(Modifier.height(8.dp))
                FormatRow(Icons.Rounded.CloudUpload, "JSON", "WordGlass 备份格式")

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "导入后会添加到词库中，不会覆盖已有词条。",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkTertiary,
                )

                if (importResult != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(importResult!!, style = MaterialTheme.typography.bodyMedium, color = colors.success)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    filePicker.launch(arrayOf(
                        "text/plain",
                        "text/csv",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "application/json",
                    ))
                },
            ) {
                Text("选择文件", color = colors.blue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = colors.inkSecondary)
            }
        },
    )
}

@Composable
private fun FormatRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
) {
    val colors = WgTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = colors.blue, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = colors.ink)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = colors.inkTertiary)
        }
    }
}
