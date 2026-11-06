package com.wordglass.app.data.speech

/**
 * Provider-neutral speech boundary.
 *
 * The current implementation is [SystemSpeechService]. A cloud provider (Azure/Aliyun) can
 * implement the same interface without touching any Compose screen. Long-lived API keys must
 * never be embedded in this client; cloud implementations should receive short-lived tokens
 * from a server-side gateway.
 */
interface SpeechService {
    fun speak(text: String)
    fun stop()
    fun shutdown()
}
