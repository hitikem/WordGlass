package com.wordglass.app.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Data sync service — backs up and restores learning data to/from the local PC server.
 * Also checks for app updates.
 *
 * All data is stored on YOUR server (your PC), fully under your control.
 */
object SyncApi {

    var serverUrl: String = "https://producers-reaction-divided-shield.trycloudflare.com"

    // ---------- Data Sync ----------

    /**
     * Upload learning progress to server.
     * @param words JSON array of word states (id, mastery, reviewCount, wrongCount, favorite, nextReviewAt, lastReviewedAt)
     * @return true if upload succeeded
     */
    suspend fun uploadProgress(words: JSONArray): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$serverUrl/api/sync/upload")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            val body = JSONObject().apply {
                put("device", android.os.Build.MODEL)
                put("timestamp", System.currentTimeMillis())
                put("words", words)
            }

            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            val code = conn.responseCode
            conn.disconnect()
            code == 200
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Download learning progress from server.
     * @return JSONArray of word states, or null on failure
     */
    suspend fun downloadProgress(): JSONArray? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$serverUrl/api/sync/download")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            if (conn.responseCode != 200) return@withContext null
            val body = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val json = JSONObject(body)
            json.optJSONArray("words")
        } catch (_: Exception) {
            null
        }
    }

    // ---------- Auto Update ----------

    data class UpdateInfo(
        val hasUpdate: Boolean,
        val version: String = "",
        val downloadUrl: String = "",
        val changelog: String = "",
    )

    /**
     * Check for app update.
     * @param currentVersion Current app version (e.g. "1.0.0-final-debug")
     */
    suspend fun checkUpdate(currentVersion: String): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            val url = URL("$serverUrl/api/update/check?version=$currentVersion")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            if (conn.responseCode != 200) {
                return@withContext UpdateInfo(hasUpdate = false)
            }
            val body = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val json = JSONObject(body)
            UpdateInfo(
                hasUpdate = json.optBoolean("hasUpdate", false),
                version = json.optString("version", ""),
                downloadUrl = json.optString("downloadUrl", ""),
                changelog = json.optString("changelog", ""),
            )
        } catch (_: Exception) {
            UpdateInfo(hasUpdate = false)
        }
    }
}
