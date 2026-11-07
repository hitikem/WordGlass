package com.wordglass.app.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Dictionary API service — connects to the local WordGlass dictionary server
 * (running on the user's PC) or falls back to free public APIs.
 */
object DictionaryApi {

    /** Cloudflare Tunnel URL — global access, free. */
    var serverUrl: String = "https://producers-reaction-divided-shield.trycloudflare.com"

    data class LookupResult(
        val word: String,
        val phonetic: String = "",
        val englishMeanings: List<Meaning> = emptyList(),
        val chineseMeanings: List<String> = emptyList(),
        val phrases: List<Phrase> = emptyList(),
        val rootAnalysis: String = "",
        val audioUrl: String = "",
    ) {
        data class Meaning(val partOfSpeech: String, val definition: String, val example: String)
        data class Phrase(val phrase: String, val meaning: String)
    }

    /**
     * Search a word using the local server first, then fall back to direct APIs.
     */
    suspend fun lookup(word: String): LookupResult = withContext(Dispatchers.IO) {
        // Try local server first
        val local = tryLocalServer(word)
        if (local != null) return@withContext local

        // Fallback: direct free dictionary API
        return@withContext tryFreeDictApi(word)
    }

    private fun tryLocalServer(word: String): LookupResult? {
        return try {
            val url = URL("$serverUrl/api/lookup?word=$word")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 5000
            if (conn.responseCode != 200) return null

            val body = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val json = JSONObject(body)
            val freeDict = json.optJSONObject("free_dict") ?: JSONObject()
            val youdao = json.optJSONObject("youdao") ?: JSONObject()
            val roots = json.optJSONObject("roots") ?: JSONObject()

            val phonetic = freeDict.optString("phonetic", "")
            val audioUrl = freeDict.optString("audio", "")

            val meanings = freeDict.optJSONArray("meanings")?.let { arr ->
                (0 until arr.length()).map { i ->
                    val m = arr.getJSONObject(i)
                    LookupResult.Meaning(
                        partOfSpeech = m.optString("partOfSpeech", ""),
                        definition = m.optString("definition", ""),
                        example = m.optString("example", ""),
                    )
                }
            } ?: emptyList()

            val chinese = youdao.optJSONArray("chinese_meanings")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            } ?: emptyList()

            val phrases = youdao.optJSONArray("phrases")?.let { arr ->
                (0 until arr.length()).map { i ->
                    val p = arr.getJSONObject(i)
                    LookupResult.Phrase(
                        phrase = p.optString("phrase", ""),
                        meaning = p.optString("meaning", ""),
                    )
                }
            } ?: emptyList()

            val rootAnalysis = roots.optString("analysis", "")

            LookupResult(
                word = word,
                phonetic = phonetic,
                englishMeanings = meanings,
                chineseMeanings = chinese,
                phrases = phrases,
                rootAnalysis = rootAnalysis,
                audioUrl = audioUrl,
            )
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun tryFreeDictApi(word: String): LookupResult {
        return try {
            val url = URL("https://api.dictionaryapi.dev/api/v2/entries/en/$word")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            if (conn.responseCode != 200) {
                return LookupResult(word = word)
            }
            val body = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val arr = org.json.JSONArray("[$body]")
            val data = arr.getJSONObject(0)

            var phonetic = ""
            var audioUrl = ""
            for (i in 0 until (data.optJSONArray("phonetics")?.length() ?: 0)) {
                val ph = data.getJSONArray("phonetics").getJSONObject(i)
                if (ph.optString("text").isNotBlank()) phonetic = ph.optString("text")
                if (ph.optString("audio").isNotBlank()) audioUrl = ph.optString("audio")
            }

            val meanings = data.optJSONArray("meanings")?.let { mArr ->
                (0 until mArr.length()).flatMap { i ->
                    val m = mArr.getJSONObject(i)
                    val pos = m.optString("partOfSpeech", "")
                    m.optJSONArray("definitions")?.let { dArr ->
                        (0 until minOf(dArr.length(), 3)).map { j ->
                            val d = dArr.getJSONObject(j)
                            LookupResult.Meaning(
                                partOfSpeech = pos,
                                definition = d.optString("definition", ""),
                                example = d.optString("example", ""),
                            )
                        }
                    } ?: emptyList()
                }
            } ?: emptyList()

            LookupResult(
                word = word,
                phonetic = phonetic,
                englishMeanings = meanings,
                audioUrl = audioUrl,
            )
        } catch (_: Exception) {
            LookupResult(word = word)
        }
    }

    /** Get audio URL for a word. */
    suspend fun audioUrl(word: String): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("$serverUrl/api/audio/$word")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            if (conn.responseCode == 200) {
                val json = JSONObject(conn.inputStream.bufferedReader().readText())
                conn.disconnect()
                return@withContext json.optString("audio_url", "")
            }
        } catch (_: Exception) {}
        // Fallback
        return@withContext "https://dict.youdao.com/dictvoice?audio=$word&type=2"
    }
}
