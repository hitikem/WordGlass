package com.wordglass.app

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WordGlassApplicationTest {

    @Test
    fun applicationSeedsRoomOnFirstLaunch() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<WordGlassApplication>()
        val words = withTimeout(20_000) {
            app.container.repository.words.first { it.size == 882 }
        }
        assertEquals(882, words.size)
    }
}
