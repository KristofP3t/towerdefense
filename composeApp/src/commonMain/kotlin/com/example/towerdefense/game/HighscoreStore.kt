package com.example.towerdefense.game

import androidx.compose.runtime.mutableStateListOf
import com.example.towerdefense.storageRead
import com.example.towerdefense.storageWrite

data class HighscoreEntry(
    val playerName: String,
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

    fun add(playerName: String, score: Int, wave: Int, victory: Boolean) {
        _entries.add(HighscoreEntry(playerName.ifBlank { "Unbekannt" }, score, wave, GameMap.TOTAL_WAVES, victory))
        storageWrite(STORAGE_KEY, serialize())
    }

    private fun serialize(): String =
        _entries.joinToString("\n") { "${it.playerName}|${it.score},${it.wave},${it.totalWaves},${it.victory}" }

    private fun load(data: String) {
        _entries.clear()
        data.lines().filter { it.isNotBlank() }.forEach { line ->
            // Format: "Name|score,wave,totalWaves,victory"  (or legacy without name)
            val (name, rest) = if ('|' in line) {
                val idx = line.indexOf('|')
                line.substring(0, idx) to line.substring(idx + 1)
            } else {
                "Unbekannt" to line
            }
            val p = rest.split(",")
            if (p.size == 4) {
                _entries.add(
                    HighscoreEntry(
                        playerName = name,
                        score      = p[0].toIntOrNull()           ?: return@forEach,
                        wave       = p[1].toIntOrNull()           ?: return@forEach,
                        totalWaves = p[2].toIntOrNull()           ?: return@forEach,
                        victory    = p[3].toBooleanStrictOrNull() ?: return@forEach,
                    )
                )
            }
        }
    }
}
