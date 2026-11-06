package com.wordglass.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.styropyr0.prismal.sources.prismalGlassLayer
import com.styropyr0.prismal.sources.rememberPrismalGlassLayer
import com.wordglass.app.core.components.AmbientBackground
import com.wordglass.app.core.components.GlassBottomBar
import com.wordglass.app.core.design.WgTheme
import com.wordglass.app.core.design.WordGlassTheme
import com.wordglass.app.data.rememberSystemSpeechService
import com.wordglass.app.model.ThemeMode
import com.wordglass.app.ui.LocalGlassBackdrop
import com.wordglass.app.ui.WordGlassViewModel
import com.wordglass.app.ui.home.HomeScreen
import com.wordglass.app.ui.library.LibraryScreen
import com.wordglass.app.ui.profile.ProfileScreen
import com.wordglass.app.ui.study.StudyScreen
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val debugStart = if (BuildConfig.DEBUG) {
            intent.getStringExtra("wordglass_start")
        } else {
            null
        }
        setContent { WordGlassApp(startDestination = debugStart ?: Routes.HOME) }
    }
}

private object Routes {
    const val HOME = "home"
    const val STUDY = "study"
    const val LIBRARY = "library"
    const val PROFILE = "profile"
}

@Composable
fun WordGlassApp(
    startDestination: String = Routes.HOME,
    vm: WordGlassViewModel = viewModel(),
) {
    val safeStart = startDestination.takeIf {
        it == Routes.HOME || it == Routes.STUDY || it == Routes.LIBRARY || it == Routes.PROFILE
    } ?: Routes.HOME
    val settings by vm.settings.collectAsStateWithLifecycle()
    val speaker = rememberSystemSpeechService(settings.voice, settings.speechRate)
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.HOME
    val backdrop = rememberPrismalGlassLayer()
    backdrop.readSamplingState()
    val words by vm.words.collectAsStateWithLifecycle()
    val dashboard by vm.dashboard.collectAsStateWithLifecycle()
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let(vm::exportBackup) }
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let(vm::restoreBackup) }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let(vm::importWords) }

    WordGlassTheme(themeMode = settings.themeMode) {
        val dark = androidx.compose.foundation.isSystemInDarkTheme() &&
            settings.themeMode == ThemeMode.SYSTEM ||
            settings.themeMode == ThemeMode.DARK
        CompositionLocalProvider(LocalGlassBackdrop provides backdrop) {
            Box(Modifier.fillMaxSize()) {
                AmbientBackground(
                    modifier = Modifier
                        .fillMaxSize()
                        .prismalGlassLayer(backdrop),
                )
                NavHost(
                    navController = navController,
                    startDestination = safeStart,
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = {
                        fadeIn(tween(360)) + slideInHorizontally(tween(430)) { it / 12 }
                    },
                    exitTransition = { fadeOut(tween(180)) },
                    popEnterTransition = {
                        fadeIn(tween(300)) + slideInHorizontally(tween(360)) { -it / 12 }
                    },
                    popExitTransition = {
                        fadeOut(tween(170)) + slideOutHorizontally(tween(300)) { it / 14 }
                    },
                ) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            backdrop = backdrop,
                            stats = dashboard.copy(dailyGoal = settings.dailyGoal),
                            words = words,
                            dark = dark,
                            onToggleTheme = {
                                vm.setTheme(if (dark) ThemeMode.LIGHT else ThemeMode.DARK)
                            },
                            onStartStudy = {
                                navController.navigateTab(Routes.STUDY)
                            },
                            onOpenLibrary = {
                                navController.navigateTab(Routes.LIBRARY)
                            },
                        )
                    }
                    composable(Routes.STUDY) {
                        StudyScreen(
                            backdrop = backdrop,
                            vm = vm,
                            speaker = speaker,
                            autoPlay = settings.autoPlay,
                            onSpeak = { speaker.speak(it) },
                        )
                    }
                    composable(Routes.LIBRARY) {
                        LibraryScreen(
                            backdrop = backdrop,
                            vm = vm,
                            onSpeak = { speaker.speak(it) },
                            onStartStudy = {
                                navController.navigateTab(Routes.STUDY)
                            },
                        )
                    }
                    composable(Routes.PROFILE) {
                        ProfileScreen(
                            backdrop = backdrop,
                            settings = settings,
                            stats = dashboard.copy(dailyGoal = settings.dailyGoal),
                            onThemeChange = vm::setTheme,
                            onVoiceChange = vm::setVoice,
                            onSpeechRateChange = vm::setSpeechRate,
                            onAutoPlayChange = vm::setAutoPlay,
                            onDailyGoalChange = vm::setDailyGoal,
                            onSpeakSample = {
                                speaker.speak("Small steps, every day. Welcome to WordGlass.")
                            },
                            onImport = {
                                importLauncher.launch(arrayOf("application/json", "text/plain"))
                            },
                            onBackup = {
                                backupLauncher.launch("wordglass-backup-${LocalDate.now()}.json")
                            },
                            onRestore = {
                                restoreLauncher.launch(arrayOf("application/json", "text/plain"))
                            },
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    WgTheme.colors.background.copy(alpha = 0.90f),
                                    WgTheme.colors.background,
                                ),
                            ),
                        ),
                )
                GlassBottomBar(
                    selectedRoute = currentRoute,
                    onSelect = { navController.navigateTab(it) },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

private fun NavHostController.navigateTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
