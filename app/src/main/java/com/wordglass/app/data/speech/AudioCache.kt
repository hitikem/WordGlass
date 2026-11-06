package com.wordglass.app.data.speech

import java.io.File
import java.security.MessageDigest

/**
 * Content-addressed audio cache for a future cloud SpeechService.
 *
 * Files are keyed by provider + voice + rate + text, so the same word is never synthesized
 * twice. The cache is bounded and trims least-recently-modified files first. It is intentionally
 * independent of Android so it can be unit-tested without a device.
 */
class AudioCache(
    private val directory: File,
    private val maxBytes: Long = DEFAULT_MAX_BYTES,
) {
    init {
        directory.mkdirs()
    }

    fun fileFor(key: String): File = File(directory, "${sha256(key)}.mp3")

    fun exists(key: String): Boolean = fileFor(key).let { it.isFile && it.length() > 0 }

    fun read(key: String): ByteArray? {
        val file = fileFor(key)
        if (!file.isFile || file.length() == 0L) return null
        return runCatching { file.readBytes() }.getOrNull()
    }

    fun write(key: String, bytes: ByteArray): File {
        require(bytes.isNotEmpty()) { "Refusing to cache empty audio" }
        val file = fileFor(key)
        val temp = File(directory, "${file.name}.tmp")
        temp.writeBytes(bytes)
        if (!temp.renameTo(file)) {
            file.writeBytes(bytes)
            temp.delete()
        }
        trim()
        return file
    }

    fun trim() {
        val files = directory.listFiles()
            ?.filter { it.isFile && it.extension == "mp3" }
            ?.sortedBy { it.lastModified() }
            .orEmpty()
        var total = files.sumOf { it.length() }
        for (file in files) {
            if (total <= maxBytes) break
            val length = file.length()
            if (file.delete()) total -= length
        }
    }

    fun clear() {
        directory.listFiles()?.forEach { it.delete() }
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    companion object {
        const val DEFAULT_MAX_BYTES: Long = 32L * 1024L * 1024L
    }
}
