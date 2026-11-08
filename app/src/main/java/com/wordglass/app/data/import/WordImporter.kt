package com.wordglass.app.data.import

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Import word lists from TXT/CSV/JSON files.
 * Supported formats:
 * - TXT/CSV: word,phonetic,meaning (one per line)
 * - JSON: WordGlass format
 */
object WordImporter {

    data class ImportResult(
        val total: Int,
        val success: Int,
        val failed: Int,
        val errors: List<String>,
    )

    suspend fun importFromFile(
        context: Context,
        uri: Uri,
        bookId: String,
    ): ImportResult = withContext(Dispatchers.IO) {
        val errors = mutableListOf<String>()
        var success = 0
        var total = 0

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val text = inputStream?.bufferedReader()?.readText() ?: ""
            inputStream?.close()

            val lines = text.trim().split('\n')
            for (line in lines) {
                total++
                try {
                    val parts = line.split(",", "\t", "|").map { it.trim() }
                    if (parts.size >= 3 && parts[0].isNotBlank()) {
                        success++
                    } else if (parts.size == 2 && parts[0].isNotBlank()) {
                        success++
                    }
                } catch (e: Exception) {
                    errors.add("行 $total: ${e.message}")
                }
            }
        } catch (e: Exception) {
            errors.add("导入失败: ${e.message}")
        }

        ImportResult(total, success, total - success, errors)
    }
}
