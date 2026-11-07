package com.wordglass.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wordglass.app.model.AppSettings
import com.wordglass.app.model.ThemeMode
import com.wordglass.app.model.VoiceGender
import com.wordglass.app.model.VoicePresets
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
                voiceProfileId = prefs[VOICE_PROFILE] ?: "youdao_real",
                speechRate = (prefs[RATE] ?: 0.9f).coerceIn(0.5f, 1.2f),
                autoPlay = prefs[AUTO_PLAY] ?: false,
                dailyGoal = (prefs[DAILY_GOAL] ?: 25).coerceIn(5, 100),
                profileName = prefs[PROFILE_NAME] ?: "Alex Chen",
                profileBio = prefs[PROFILE_BIO] ?: "WordGlass Learner",
                profileAvatarUri = prefs[PROFILE_AVATAR],
                profileAvatarSeed = prefs[PROFILE_AVATAR_SEED] ?: "0",
                planStartDate = prefs[PLAN_START] ?: 0L,
                planTotalDays = (prefs[PLAN_DAYS] ?: 0).coerceIn(0, 365),
                alibabaTtsApiKey = prefs[ALIBABA_TTS_KEY] ?: "",
            )
        }

    suspend fun setTheme(value: ThemeMode) {
        context.settingsDataStore.edit { it[THEME] = value.name }
    }

    suspend fun setVoice(value: VoiceGender) {
        context.settingsDataStore.edit { it[VOICE] = value.name }
    }

    suspend fun setVoiceProfile(id: String) {
        val profile = VoicePresets.byId(id)
        context.settingsDataStore.edit {
            it[VOICE_PROFILE] = id
            it[VOICE] = profile.gender.name
        }
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

    suspend fun setProfileName(value: String) {
        context.settingsDataStore.edit { it[PROFILE_NAME] = value.take(32) }
    }

    suspend fun setProfileBio(value: String) {
        context.settingsDataStore.edit { it[PROFILE_BIO] = value.take(60) }
    }

    suspend fun setProfileAvatarUri(uri: String?) {
        context.settingsDataStore.edit {
            if (uri == null) it.remove(PROFILE_AVATAR) else it[PROFILE_AVATAR] = uri
        }
    }

    /** Set the learning plan. Also auto-derives dailyGoal from the plan. */
    suspend fun setPlan(startDate: Long, totalDays: Int) {
        context.settingsDataStore.edit {
            it[PLAN_START] = startDate
            it[PLAN_DAYS] = totalDays.coerceIn(1, 365)
            // Auto-derive daily goal from plan: totalWords / totalDays
            // We use 882 as the default word count
            val derivedGoal = (882f / totalDays.coerceIn(1, 365)).toInt().coerceIn(5, 100)
            it[DAILY_GOAL] = derivedGoal
        }
    }

    suspend fun setAlibabaTtsApiKey(key: String) {
        context.settingsDataStore.edit {
            if (key.isBlank()) it.remove(ALIBABA_TTS_KEY) else it[ALIBABA_TTS_KEY] = key.trim()
        }
    }

    suspend fun setProfileAvatarSeed(seed: String) {
        context.settingsDataStore.edit { it[PROFILE_AVATAR_SEED] = seed }
    }

    private companion object {
        val THEME = stringPreferencesKey("theme")
        val VOICE = stringPreferencesKey("voice")
        val VOICE_PROFILE = stringPreferencesKey("voice_profile")
        val RATE = floatPreferencesKey("speech_rate")
        val AUTO_PLAY = booleanPreferencesKey("auto_play")
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val PROFILE_NAME = stringPreferencesKey("profile_name")
        val PROFILE_BIO = stringPreferencesKey("profile_bio")
        val PROFILE_AVATAR = stringPreferencesKey("profile_avatar")
        val PROFILE_AVATAR_SEED = stringPreferencesKey("profile_avatar_seed")
        val PLAN_START = longPreferencesKey("plan_start")
        val PLAN_DAYS = intPreferencesKey("plan_days")
        val ALIBABA_TTS_KEY = stringPreferencesKey("alibaba_tts_key")
    }
}

