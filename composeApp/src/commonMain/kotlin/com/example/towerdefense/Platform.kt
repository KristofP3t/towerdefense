package com.example.towerdefense

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun storageWrite(key: String, value: String)
expect fun storageRead(key: String): String?

/**
 * Spielt einen kurzen Ton. ids: "shoot", "kill", "boss_kill", "game_over", "victory"
 * Implementierung ist plattformspezifisch; scheitert lautlos wenn kein Audio verfügbar.
 */
expect fun playSound(id: String)