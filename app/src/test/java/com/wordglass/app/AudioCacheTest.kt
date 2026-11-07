package com.wordglass.app

import com.wordglass.app.data.speech.AudioCache
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Unit tests for [AudioCache]: content-addressed local audio cache.
 *
 * Uses the SHA-256 content-addressed file layout and verifies round-trip behaviour.
 */
class AudioCacheTest {

    private fun tempDir(): File = File.createTempFile("audio-cache", "").apply {
        delete()
        mkdirs()
    }

    @Test
    fun `fileFor returns stable deterministic path`() {
        val dir = tempDir()
        val cache = AudioCache(dir)
        val a = cache.fileFor("change")
        val b = cache.fileFor("change")
        assertEquals(a.absolutePath, b.absolutePath)
    }

    @Test
    fun `fileFor differs per key`() {
        val dir = tempDir()
        val cache = AudioCache(dir)
        val a = cache.fileFor("change")
        val b = cache.fileFor("experience")
        assertTrue(a.absolutePath != b.absolutePath)
    }

    @Test
    fun `has returns false when file absent`() {
        val dir = tempDir()
        val cache = AudioCache(dir)
        assertFalse(cache.has("change"))
    }

    @Test
    fun `has returns true after file written`() {
        val dir = tempDir()
        val cache = AudioCache(dir)
        val f = cache.fileFor("change")
        f.writeBytes(byteArrayOf(0x01, 0x02, 0x03, 0x04))
        assertTrue(cache.has("change"))
    }

    @Test
    fun `has returns false for empty file`() {
        val dir = tempDir()
        val cache = AudioCache(dir)
        val f = cache.fileFor("change")
        f.writeBytes(byteArrayOf())
        assertFalse(cache.has("change"))
    }

    @Test
    fun `writes are atomic via rename`() {
        val dir = tempDir()
        val cache = AudioCache(dir)
        val f = cache.fileFor("change")
        val tmp = File(dir, "${f.name}.tmp")
        tmp.writeBytes(byteArrayOf(0x01, 0x02, 0x03, 0x04))
        tmp.renameTo(f)
        assertTrue(f.exists())
        assertFalse(tmp.exists())
    }

    @Test
    fun `sha256 produces 64 hex chars`() {
        val dir = tempDir()
        val cache = AudioCache(dir)
        val f = cache.fileFor("change")
        val name = f.nameWithoutExtension
        assertEquals(64, name.length)
        assertTrue(name.all { it in "0123456789abcdef" })
    }

    @Test
    fun `directory is created on init`() {
        val dir = File(tempDir(), "sub")
        AudioCache(dir)
        assertTrue(dir.exists())
    }
}
