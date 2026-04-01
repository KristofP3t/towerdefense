package com.example.towerdefense.game

import androidx.compose.runtime.mutableStateListOf

data class HighscoreEntry(
    val score: Int,
    val wave: Int,
    val totalWaves: Int,
    val victory: Boolean,
)

object HighscoreStore {
    private val _entries = mutableStateListOf<HighscoreEntry>()

    val entries: List<HighscoreEntry>
        get() = _entries.sortedByDescending { it.score }

    fun add(score: Int, wave: Int, victory: Boolean) {
        _entries.add(HighscoreEntry(score, wave, GameMap.TOTAL_WAVES, victory))
    }
}
