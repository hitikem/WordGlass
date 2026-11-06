package com.wordglass.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wordglass.app.model.AppSettings
import com.wordglass.app.model.ThemeMode
import com.wordglass.app.model.VoiceGender
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "wordglass_settings")

class SettingsRepository(private val context: Context) {

    val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { error ->
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw error
        }
        .map { prefs ->
            AppSettings(
                themeMode = prefs[THEME]?.let { name ->
                    ThemeMode.entries.firstOrNull { it.name == name }
                } ?: ThemeMode.SYSTEM,
                voice = prefs[VOICE]?.let { name ->
                    VoiceGender.entries.firstOrNull { it.name == name }
                } ?: VoiceGender.FEMALE,
                speechRate = (prefs[RATE] ?: 0.9f).coerceIn(0.5f, 1.2f),
                autoPlay = prefs[AUTO_PLAY] ?: false,
                dailyGoal = (prefs[DAILY_GOAL] ?: 25).coerceIn(5, 100),
            )
        }

    suspend fun setTheme(value: ThemeMode) {
        context.settingsDataStore.edit { it[THEME] = value.name }
    }

    suspend fun setVoice(value: VoiceGender) {
        context.settingsDataStore.edit { it[VOICE] = value.name }
    }

    suspend fun setSpeechRate(value: Float) {
        context.settingsDataStore.edit { it[RATE] = value.coerceIn(0.5f, 1.2f) }
    }

    suspend fun setAutoPlay(value: Boolean) {
        context.settingsDataStore.edit { it[AUTO_PLAY] = value }
    }

    suspend fun setDailyGoal(value: Int) {
        context.settingsDataStore.edit { it[DAILY_GOAL] = value.coerceIn(5, 100) }
    }

    private companion object {
        val THEME = stringPreferencesKey("theme")
        val VOICE = stringPreferencesKey("voice")
        val RATE = floatPreferencesKey("speech_rate")
        val AUTO_PLAY = booleanPreferencesKey("auto_play")
        val DAILY_GOAL = intPreferencesKey("daily_goal")
    }
}
