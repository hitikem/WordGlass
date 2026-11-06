package com.wordglass.app

import android.app.Application
import com.wordglass.app.data.local.WordDatabase
import com.wordglass.app.data.repository.BackupRepository
import com.wordglass.app.data.repository.SettingsRepository
import com.wordglass.app.data.repository.WordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WordGlassApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.scope.launch {
            runCatching { container.repository.seedIfNeeded() }
                .onFailure { android.util.Log.e("WordGlass", "Seed failed", it) }
        }
    }
}

class AppContainer(application: Application) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database = WordDatabase.create(application)
    val repository = WordRepository(database.wordDao(), application)
    val settingsRepository = SettingsRepository(application)
    val backupRepository = BackupRepository(database.wordDao(), settingsRepository)
}
