package com.example.towerdefense.game

enum class Difficulty(
    val label: String,
    val startGold: Int,
    val startLives: Int,
    val enemyHealthMult: Float,
) {
    EASY  (label = "Einfach", startGold = 200, startLives = 30, enemyHealthMult = 0.75f),
    NORMAL(label = "Normal",  startGold = 150, startLives = 20, enemyHealthMult = 1.00f),
    HARD  (label = "Schwer",  startGold = 100, startLives = 10, enemyHealthMult = 1.50f),
}
