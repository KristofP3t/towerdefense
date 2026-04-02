package com.example.towerdefense

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.towerdefense.game.Difficulty
import com.example.towerdefense.game.GameMap
import com.example.towerdefense.game.HighscoreStore
import com.example.towerdefense.game.MapDefinition
import com.example.towerdefense.ui.*

private sealed interface Screen {
    data object MainMenu   : Screen
    data object MapSelect  : Screen
    data object Game       : Screen
    data object Highscores : Screen
    data object Settings   : Screen
}

@Composable
fun App() {
    var screen     by remember { mutableStateOf<Screen>(Screen.MainMenu) }
    var difficulty by remember { mutableStateOf(Difficulty.NORMAL) }

    MaterialTheme {
        when (screen) {
            Screen.MainMenu -> MainMenuScreen(
                onNewGame    = { screen = Screen.MapSelect },
                onHighscores = { screen = Screen.Highscores },
                onSettings   = { screen = Screen.Settings },
            )
            Screen.MapSelect -> MapSelectScreen(
                onMapSelected = { map ->
                    GameMap.current = map
                    screen = Screen.Game
                },
                onBack = { screen = Screen.MainMenu },
            )
            Screen.Game -> GameScreen(
                difficulty   = difficulty,
                onBackToMenu = { score, wave, victory ->
                    HighscoreStore.add(score, wave, victory)
                    screen = Screen.MainMenu
                },
            )
            Screen.Highscores -> HighscoreScreen(
                entries = HighscoreStore.entries,
                onBack  = { screen = Screen.MainMenu },
            )
            Screen.Settings -> SettingsScreen(
                currentDifficulty  = difficulty,
                onDifficultyChange = { difficulty = it },
                onBack             = { screen = Screen.MainMenu },
            )
        }
    }
}
