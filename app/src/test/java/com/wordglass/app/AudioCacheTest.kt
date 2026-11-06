package com.wordglass.app

import com.wordglass.app.data.speech.AudioCache
import java.nio.file.Files
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioCacheTest {

    @Test
    fun cacheIsContentAddressedAndBounded() {
        val dir = Files.createTempDirectory("wordglass-audio").toFile()
        val cache = AudioCache(dir, maxBytes = 24)
        val first = ByteArray(10) { 1 }
        val second = ByteArray(10) { 2 }
        cache.write("azure|aria|0.9|change", first)
        cache.write("azure|aria|0.9|interest", second)

        assertTrue(cache.exists("azure|aria|0.9|change"))
        assertArrayEquals(first, cache.read("azure|aria|0.9|change"))
        assertNotNull(cache.fileFor("azure|aria|0.9|change"))
        assertFalse(cache.exists("missing"))

        cache.write("azure|aria|0.9|last", ByteArray(10) { 3 })
        assertTrue(dir.listFiles()!!.sumOf { it.length() } <= 24)
        cache.clear()
        assertFalse(cache.exists("azure|aria|0.9|change"))
    }
}
