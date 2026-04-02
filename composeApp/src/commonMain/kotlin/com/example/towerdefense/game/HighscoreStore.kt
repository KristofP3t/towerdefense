package com.example.towerdefense.game

import androidx.compose.runtime.mutableStateListOf
import com.example.towerdefense.storageRead
import com.example.towerdefense.storageWrite

data class HighscoreEntry(
    val score: Int,
    val wave: Int,
    val totalWaves: Int,
    val victory: Boolean,
)

object HighscoreStore {
    private const val STORAGE_KEY = "highscores"
    private val _entries = mutableStateListOf<HighscoreEntry>()

    val entries: List<HighscoreEntry>
        get() = _entries.sortedByDescending { it.score }

    init {
        storageRead(STORAGE_KEY)?.let { load(it) }
    }

    fun add(score: Int, wave: Int, victory: Boolean) {
        _entries.add(HighscoreEntry(score, wave, GameMap.TOTAL_WAVES, victory))
        storageWrite(STORAGE_KEY, serialize())
    }

    private fun serialize(): String =
        _entries.joinToString("\n") { "${it.score},${it.wave},${it.totalWaves},${it.victory}" }

    private fun load(data: String) {
        _entries.clear()
        data.lines().filter { it.isNotBlank() }.forEach { line ->
            val p = line.split(",")
            if (p.size == 4) {
                _entries.add(
                    HighscoreEntry(
                        score      = p[0].toIntOrNull()          ?: return@forEach,
                        wave       = p[1].toIntOrNull()          ?: return@forEach,
                        totalWaves = p[2].toIntOrNull()          ?: return@forEach,
                        victory    = p[3].toBooleanStrictOrNull() ?: return@forEach,
                    )
                )
            }
        }
    }
}
