package com.wordglass.app.data.speech

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Alibaba Cloud TTS (DashScope WebSocket API).
 *
 * Uses the `sk-ws-` API key for WebSocket TTS synthesis.
 * Supports multiple voices. Audio is returned as PCM and converted to playable audio.
 *
 * Security: the API key is stored in DataStore (app settings), never hardcoded in source.
 */
class AlibabaTtsService(
    private val apiKey: String,
    private val onAudioData: (ByteArray) -> Unit,
    private val onComplete: () -> Unit,
    private val onError: (String) -> Unit,
) {
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    companion object {
        const val URL = "wss://dashscope.aliyuncs.com/api-ws/v1/inference"
        const val TASK_ID = "tts"

        /** Free/cheap Alibaba TTS voices. */
        val VOICES = listOf(
            AlibabaVoice("longxiaochun_v2", "龙小淳（女）", "female"),
            AlibabaVoice("longwan_v2", "龙婉（女）", "female"),
            AlibabaVoice("longcheng_v2", "龙城（男）", "male"),
            AlibabaVoice("longhua_v2", "龙华（男）", "male"),
            AlibabaVoice("longxiaoxia_v2", "龙小夏（女）", "female"),
            AlibabaVoice("longlaotie_v2", "龙老铁（男）", "male"),
            AlibabaVoice("loongstella_v2", "Stella（女）", "female"),
            AlibabaVoice("loongbella_v2", "Bella（女）", "female"),
        )
    }

    /**
     * Synthesize speech via Alibaba Cloud TTS WebSocket.
     * @param text Text to speak
     * @param voice Voice ID (e.g. "longxiaochun_v2")
     */
    suspend fun speak(text: String, voice: String) {
        if (apiKey.isBlank()) {
            onError("阿里云 API Key 未设置")
            return
        }

        val taskUuid = UUID.randomUUID().toString()
        val url = URL

        suspendCancellableCoroutine<Unit> { cont ->
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "bearer $apiKey")
                .addHeader("X-DashScope-DataInspection", "enable")
                .build()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(ws: WebSocket, response: Response) {
                    // Send run-task
                    val runTask = JSONObject().apply {
                        put("header", JSONObject().apply {
                            put("action", "run-task")
                            put("task_id", taskUuid)
                            put("streaming", "duplex")
                        })
                        put("payload", JSONObject().apply {
                            put("task_group", "audio")
                            put("task", "tts")
                            put("function", "SpeechSynthesizer")
                            put("model", voice)
                            put("parameters", JSONObject().apply {
                                put("text_type", "PlainText")
                                put("voice", voice)
                                put("format", "mp3")
                                put("sample_rate", 22050)
                                put("volume", 50)
                                put("rate", 1.0)
                                put("pitch", 1.0)
                            })
                            put("input", JSONObject().apply {
                                put("text", text)
                            })
                        })
                    }
                    ws.send(runTask.toString())

                    // Send continue-task with text
                    val continueTask = JSONObject().apply {
                        put("header", JSONObject().apply {
                            put("action", "continue-task")
                            put("task_id", taskUuid)
                            put("streaming", "duplex")
                        })
                        put("payload", JSONObject().apply {
                            put("input", JSONObject().apply {
                                put("text", text)
                            })
                        })
                    }
                    ws.send(continueTask.toString())

                    // Send finish-task
                    val finishTask = JSONObject().apply {
                        put("header", JSONObject().apply {
                            put("action", "finish-task")
                            put("task_id", taskUuid)
                            put("streaming", "duplex")
                        })
                        put("payload", JSONObject().apply {
                            put("input", JSONObject().apply {
                                put("text", "")
                            })
                        })
                    }
                    ws.send(finishTask.toString())
                }

                override fun onMessage(ws: WebSocket, text: String) {
                    try {
                        val json = JSONObject(text)
                        val header = json.optJSONObject("header")
                        val event = header?.optString("event")
                        when (event) {
                            "task-finished" -> {
                                onComplete()
                                if (cont.isActive) cont.resume(Unit)
                                ws.close(1000, null)
                            }
                            "task-failed" -> {
                                val msg = json.optJSONObject("header")?.optString("error_message") ?: "TTS 合成失败"
                                onError(msg)
                                if (cont.isActive) cont.resumeWithException(Exception(msg))
                                ws.close(1000, null)
                            }
                        }
                        // Check for binary audio data in payload
                        val payload = json.optJSONObject("payload")
                        val audioBase64 = payload?.optString("audio")
                        if (!audioBase64.isNullOrEmpty()) {
                            val audioBytes = Base64.decode(audioBase64, Base64.DEFAULT)
                            onAudioData(audioBytes)
                        }
                    } catch (_: Exception) { }
                }

                override fun onMessage(ws: WebSocket, bytes: ByteString) {
                    // Binary audio frame
                    onAudioData(bytes.toByteArray())
                }

                override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                    onError(t.message ?: "连接失败")
                    if (cont.isActive) cont.resumeWithException(t)
                }

                override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                    if (cont.isActive) cont.resume(Unit)
                }
            })
        }
    }

    fun cancel() {
        webSocket?.close(1000, null)
        webSocket = null
    }
}

data class AlibabaVoice(
    val id: String,
    val displayName: String,
    val gender: String,
)
