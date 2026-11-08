package com.wordglass.app.data.import

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * PDF word list extractor.
 * Extracts text from PDF and parses word entries (word, phonetic, meaning).
 *
 * PDF text extraction approach:
 * 1. Read raw PDF bytes
 * 2. Extract text streams (Tj, TJ operators)
 * 3. Parse word entries from extracted text
 */
object PdfWordExtractor {

    data class PdfWord(
        val word: String,
        val phonetic: String,
        val meaning: String,
    )

    data class ExtractResult(
        val words: List<PdfWord>,
        val totalLines: Int,
        val parsedCount: Int,
        val errors: List<String>,
    )

    /**
     * Extract words from a PDF file.
     * Uses raw PDF text stream parsing (no external library needed).
     */
    suspend fun extractFromFile(
        context: Context,
        uri: Uri,
    ): ExtractResult = withContext(Dispatchers.IO) {
        val errors = mutableListOf<String>()

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return@withContext ExtractResult(
                emptyList(), 0, 0, listOf("无法读取文件")
            )
            inputStream.close()

            // Extract text from PDF
            val text = extractTextFromPdf(bytes)
            val lines = text.split('\n').filter { it.isNotBlank() }

            // Parse word entries
            val words = parseWordEntries(lines, errors)

            ExtractResult(
                words = words,
                totalLines = lines.size,
                parsedCount = words.size,
                errors = errors,
            )
        } catch (e: Exception) {
            ExtractResult(emptyList(), 0, 0, listOf("PDF 解析失败: ${e.message}"))
        }
    }

    /**
     * Extract text content from PDF bytes.
     * Looks for text between BT/ET markers and Tj/TJ operators.
     */
    private fun extractTextFromPdf(bytes: ByteArray): String {
        val text = StringBuilder()
        val content = String(bytes, Charsets.ISO_8859_1)

        // Find text show operators: (text) Tj or [(text)] TJ
        val tjPattern = Regex("\\(([^)]+)\\)\\s*Tj")
        val tjArrayPattern = Regex("\\[([^\\]]+)\\]\\s*TJ")

        for (match in tjPattern.findAll(content)) {
            text.append(match.groupValues[1]).append('\n')
        }

        for (match in tjArrayPattern.findAll(content)) {
            val inner = match.groupValues[1]
            val strPattern = Regex("\\(([^)]+)\\)")
            for (s in strPattern.findAll(inner)) {
                text.append(s.groupValues[1])
            }
            text.append('\n')
        }

        // Also try to find text between BT and ET
        val btPattern = Regex("BT(.*?)ET", RegexOption.DOT_MATCHES_ALL)
        for (match in btPattern.findAll(content)) {
            val block = match.groupValues[1]
            val strPattern = Regex("\\(([^)]+)\\)")
            for (s in strPattern.findAll(block)) {
                val t = s.groupValues[1]
                if (t.isNotBlank() && t.length > 1) {
                    text.append(t).append(' ')
                }
            }
        }

        return text.toString()
    }

    /**
     * Parse word entries from text lines.
     * Common formats:
     * - word /phonetic/ meaning
     * - word  meaning
     * - word\tphonetic\tmeaning
     */
    private fun parseWordEntries(lines: List<String>, errors: MutableList<String>): List<PdfWord> {
        val words = mutableListOf<PdfWord>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.length < 2) continue

            try {
                // Try tab-separated
                if (trimmed.contains('\t')) {
                    val parts = trimmed.split('\t').map { it.trim() }
                    if (parts.size >= 3) {
                        words.add(PdfWord(parts[0], parts[1], parts[2]))
                        continue
                    }
                }

                // Try "word /phonetic/ meaning" format
                val phoneticMatch = Regex("^(\\S+)\\s+/([^/]+)/\\s+(.+)$").find(trimmed)
                if (phoneticMatch != null) {
                    words.add(PdfWord(
                        phoneticMatch.groupValues[1],
                        phoneticMatch.groupValues[2],
                        phoneticMatch.groupValues[3],
                    ))
                    continue
                }

                // Try "word  meaning" (two spaces or more)
                val doubleSpace = Regex("^(\\S+)\\s{2,}(.+)$").find(trimmed)
                if (doubleSpace != null) {
                    words.add(PdfWord(
                        doubleSpace.groupValues[1],
                        "",
                        doubleSpace.groupValues[2],
                    ))
                    continue
                }

                // Try "word,meaning" format
                if (trimmed.contains(',')) {
                    val parts = trimmed.split(',', limit = 2)
                    if (parts.size == 2 && parts[0].isNotBlank()) {
                        words.add(PdfWord(parts[0].trim(), "", parts[1].trim()))
                    }
                }
            } catch (e: Exception) {
                errors.add("解析行失败: $trimmed")
            }
        }

        return words
    }

    /**
     * Save extracted words to JSON format for import.
     */
    fun toJson(words: List<PdfWord>): String {
        val sb = StringBuilder("[")
        words.forEachIndexed { i, w ->
            if (i > 0) sb.append(",")
            sb.append("{\"word\":\"${w.word}\",\"phonetic\":\"${w.phonetic}\",\"meaning\":\"${w.meaning}\"}")
        }
        sb.append("]")
        return sb.toString()
    }
}
