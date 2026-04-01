package com.example.towerdefense

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.towerdefense.game.HighscoreStore
import com.example.towerdefense.ui.GameScreen
import com.example.towerdefense.ui.HighscoreScreen
import com.example.towerdefense.ui.MainMenuScreen
import com.example.towerdefense.ui.SettingsScreen

private sealed interface Screen {
    data object MainMenu   : Screen
    data object Game       : Screen
    data object Highscores : Screen
    data object Settings   : Screen
}

@Composable
fun App() {
    var screen by remember { mutableStateOf<Screen>(Screen.MainMenu) }

    MaterialTheme {
        when (screen) {
            Screen.MainMenu -> MainMenuScreen(
                onNewGame    = { screen = Screen.Game },
                onHighscores = { screen = Screen.Highscores },
                onSettings   = { screen = Screen.Settings },
            )
            Screen.Game -> GameScreen(
                onBackToMenu = { score, wave, victory ->
                    HighscoreStore.add(score, wave, victory)
                    screen = Screen.MainMenu
                },
            )
            Screen.Highscores -> HighscoreScreen(
                entries  = HighscoreStore.entries,
                onBack   = { screen = Screen.MainMenu },
            )
            Screen.Settings -> SettingsScreen(
                onBack = { screen = Screen.MainMenu },
            )
        }
    }
}
