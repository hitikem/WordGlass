package com.wordglass.app.ui

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wordglass.app.WordGlassApplication
import com.wordglass.app.data.PreviewData
import com.wordglass.app.model.AppSettings
import com.wordglass.app.model.DashboardStats
import com.wordglass.app.model.LibraryFilter
import com.wordglass.app.model.LibrarySort
import com.wordglass.app.model.StudyMode
import com.wordglass.app.model.StudyRating
import com.wordglass.app.model.ThemeMode
import com.wordglass.app.model.VoiceGender
import com.wordglass.app.model.Word
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudyUiState(
    val mode: StudyMode = StudyMode.CARD,
    val index: Int = 0,
    val total: Int = 0,
    val flipped: Boolean = false,
    val selectedChoice: Int? = null,
    val choiceCorrect: Boolean? = null,
    val dictation: String = "",
    val dictationChecked: Boolean = false,
    val dictationCorrect: Boolean? = null,
    val finished: Boolean = false,
    val knownCount: Int = 0,
    val unknownCount: Int = 0,
    val lastRating: StudyRating? = null,
) {
    val progress: Float
        get() = when {
            finished -> 1f
            total <= 0 -> 0f
            else -> (index.toFloat() / total).coerceIn(0f, 1f)
        }
}

class WordGlassViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as WordGlassApplication).container
    private val repository = container.repository
    private val settingsRepository = container.settingsRepository
    private val backupRepository = container.backupRepository

    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings(),
    )

    private val _words = MutableStateFlow<List<Word>>(emptyList())
    val words: StateFlow<List<Word>> = _words.asStateFlow()

    private val _study = MutableStateFlow(StudyUiState())
    val study: StateFlow<StudyUiState> = _study.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filter = MutableStateFlow(LibraryFilter.ALL)
    val filter: StateFlow<LibraryFilter> = _filter.asStateFlow()

    private val _sort = MutableStateFlow(LibrarySort.ALPHABETICAL)
    val sort: StateFlow<LibrarySort> = _sort.asStateFlow()

    val visibleWords: StateFlow<List<Word>> = combine(
        _words,
        _query,
        _filter,
        _sort,
    ) { words, query, filter, sort ->
        val normalized = query.trim().lowercase()
        val filtered = words.filter { word ->
            val matchesQuery = normalized.isEmpty() ||
                word.word.lowercase().contains(normalized) ||
                word.meaning.lowercase().contains(normalized)
            val matchesFilter = when (filter) {
                LibraryFilter.ALL -> true
                LibraryFilter.DUE -> word.due
                LibraryFilter.LEARNING -> word.reviewCount > 0 && word.mastery < 4
                LibraryFilter.MASTERED -> word.mastery >= 4
                LibraryFilter.WRONG -> word.wrongCount > 0
                LibraryFilter.FAVORITE -> word.favorite
            }
            matchesQuery && matchesFilter
        }
        when (sort) {
            LibrarySort.ALPHABETICAL -> filtered.sortedBy { it.word.lowercase() }
            LibrarySort.MASTERY -> filtered.sortedByDescending { it.mastery }
            LibrarySort.RECENT -> filtered.sortedByDescending { it.lastReviewedAt ?: 0L }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val dashboard: StateFlow<DashboardStats> = _words.map { words ->
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val dayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val days = words.mapNotNull { it.lastReviewedAt }
            .map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
            .toSet()
        var streakCursor = if (today in days) today else today.minusDays(1)
        var streak = 0
        while (streakCursor in days) {
            streak += 1
            streakCursor = streakCursor.minusDays(1)
        }
        val week = (6 downTo 0).map { offset ->
            val day = today.minusDays(offset.toLong())
            words.count {
                it.lastReviewedAt?.let { ts ->
                    Instant.ofEpochMilli(ts).atZone(zone).toLocalDate() == day
                } == true
            }
        }
        DashboardStats(
            dailyGoal = 25,
            completedToday = words.count { (it.lastReviewedAt ?: 0L) >= dayStart },
            dueCount = words.count { it.due },
            totalLearned = words.count { it.reviewCount > 0 },
            mastered = words.count { it.mastery >= 4 },
            streakDays = streak,
            minutesToday = 0,
            weeklyMinutes = week,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PreviewData.dashboard.copy(completedToday = 0, dueCount = 0, totalLearned = 0, mastered = 0, streakDays = 0, weeklyMinutes = List(7) { 0 }),
    )

    init {
        viewModelScope.launch {
            repository.words.collect { list ->
                _words.value = list
                _study.update { state ->
                    val total = list.size
                    val index = if (total == 0) 0 else state.index.coerceIn(0, total - 1)
                    state.copy(
                        total = total,
                        index = index,
                        finished = if (total == 0) false else state.finished,
                    )
                }
            }
        }
    }

    fun wordOf(id: Int): Word? = _words.value.firstOrNull { it.id == id }

    fun studyWord(): Word? = _words.value.getOrNull(_study.value.index)

    fun setMode(mode: StudyMode) {
        _study.update {
            it.copy(
                mode = mode,
                flipped = false,
                selectedChoice = null,
                choiceCorrect = null,
                dictation = "",
                dictationChecked = false,
                dictationCorrect = null,
            )
        }
    }

    fun flip() {
        _study.update { if (it.finished) it else it.copy(flipped = !it.flipped) }
    }

    fun rate(rating: StudyRating) {
        val word = studyWord() ?: return
        val positive = rating == StudyRating.GOOD || rating == StudyRating.EASY
        viewModelScope.launch {
            repository.submitRating(word.id, rating, _study.value.mode)
        }
        _study.update {
            val nextIndex = it.index + 1
            val finished = nextIndex >= it.total
            it.copy(
                index = if (finished) it.index else nextIndex,
                finished = finished,
                flipped = false,
                selectedChoice = null,
                choiceCorrect = null,
                dictation = "",
                dictationChecked = false,
                dictationCorrect = null,
                knownCount = it.knownCount + if (positive) 1 else 0,
                unknownCount = it.unknownCount + if (positive) 0 else 1,
                lastRating = rating,
            )
        }
    }

    fun choose(index: Int, correct: Boolean) {
        val state = _study.value
        if (state.selectedChoice != null || state.finished) return
        _study.update { it.copy(selectedChoice = index, choiceCorrect = correct) }
    }

    fun updateDictation(text: String) {
        _study.update {
            if (it.dictationChecked) it
            else it.copy(dictation = text.take(40))
        }
    }

    fun submitDictation() {
        val state = _study.value
        if (state.dictationChecked || state.finished) return
        val word = studyWord() ?: return
        val correct = state.dictation.trim().equals(word.word, ignoreCase = true)
        viewModelScope.launch {
            repository.submitRating(
                id = word.id,
                rating = if (correct) StudyRating.GOOD else StudyRating.AGAIN,
                mode = StudyMode.DICTATION,
            )
        }
        _study.update {
            it.copy(
                dictationChecked = true,
                dictationCorrect = correct,
                knownCount = it.knownCount + if (correct) 1 else 0,
                unknownCount = it.unknownCount + if (correct) 0 else 1,
            )
        }
    }

    fun nextAfterAnswer() {
        val state = _study.value
        if (state.mode == StudyMode.CHOICE && state.choiceCorrect != null) {
            _words.value.getOrNull(state.index)?.let { word ->
                viewModelScope.launch {
                    repository.submitRating(
                        id = word.id,
                        rating = if (state.choiceCorrect) StudyRating.GOOD else StudyRating.AGAIN,
                        mode = StudyMode.CHOICE,
                    )
                }
            }
        }
        _study.update {
            val nextIndex = it.index + 1
            val finished = nextIndex >= it.total
            it.copy(
                index = if (finished) it.index else nextIndex,
                finished = finished,
                selectedChoice = null,
                choiceCorrect = null,
                dictation = "",
                dictationChecked = false,
                dictationCorrect = null,
                flipped = false,
            )
        }
    }

    fun undo() {
        _study.update {
            if (it.index == 0) return@update it
            it.copy(
                index = it.index - 1,
                finished = false,
                flipped = false,
                selectedChoice = null,
                choiceCorrect = null,
                dictation = "",
                dictationChecked = false,
                dictationCorrect = null,
                lastRating = null,
            )
        }
    }

    fun restart() {
        _study.value = StudyUiState(total = _words.value.size)
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun setFilter(value: LibraryFilter) {
        _filter.value = value
    }

    fun setSort(value: LibrarySort) {
        _sort.value = value
    }

    fun setTheme(value: ThemeMode) {
        viewModelScope.launch { settingsRepository.setTheme(value) }
    }

    fun setVoice(value: VoiceGender) {
        viewModelScope.launch { settingsRepository.setVoice(value) }
    }

    fun setSpeechRate(value: Float) {
        viewModelScope.launch { settingsRepository.setSpeechRate(value) }
    }

    fun setAutoPlay(value: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoPlay(value) }
    }

    fun setDailyGoal(value: Int) {
        viewModelScope.launch { settingsRepository.setDailyGoal(value) }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                backupRepository.export(getApplication<Application>().contentResolver, uri)
            }.onSuccess {
                Toast.makeText(getApplication(), "已导出 $it 个词条", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(getApplication(), "导出失败：${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun restoreBackup(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                backupRepository.importBackup(
                    getApplication<Application>().contentResolver,
                    uri,
                    includeLearning = true,
                )
            }.onSuccess {
                Toast.makeText(getApplication(), "已恢复 $it 个词条", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(getApplication(), "恢复失败：${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun importWords(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                backupRepository.importBackup(
                    getApplication<Application>().contentResolver,
                    uri,
                    includeLearning = false,
                )
            }.onSuccess {
                Toast.makeText(getApplication(), "已导入 $it 个词条（学习记录未覆盖）", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(getApplication(), "导入失败：${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun toggleFavorite(id: Int) {
        val word = wordOf(id) ?: return
        viewModelScope.launch {
            repository.toggleFavorite(id, !word.favorite)
        }
    }

    fun setMastery(id: Int, level: Int) {
        viewModelScope.launch {
            repository.setMastery(id, level)
        }
    }
}
