package com.example.towerdefense.game

import kotlin.math.sqrt

data class Vec2(val x: Float, val y: Float) {
    operator fun plus(other: Vec2) = Vec2(x + other.x, y + other.y)
    operator fun minus(other: Vec2) = Vec2(x - other.x, y - other.y)
    operator fun times(f: Float) = Vec2(x * f, y * f)
    fun length() = sqrt(x * x + y * y)
    fun distanceTo(other: Vec2) = (this - other).length()
    fun normalized(): Vec2 = length().let { l -> if (l == 0f) this else Vec2(x / l, y / l) }
}

data class GridPos(val col: Int, val row: Int)

enum class TowerType(
    val damage: Float,
    val rangePx: Float,
    val fireInterval: Float,
    val cost: Int,
    val slowDuration: Float,
) {
    RED   (damage = 55f, rangePx = 144f, fireInterval = 1.2f, cost = 75, slowDuration = 0f),
    YELLOW(damage = 20f, rangePx = 240f, fireInterval = 1.6f, cost = 50, slowDuration = 0f),
    BLUE  (damage = 12f, rangePx = 160f, fireInterval = 2.0f, cost = 60, slowDuration = 1.0f);

    /** Goldkosten für das Upgrade von `currentLevel` auf `currentLevel + 1`. */
    fun upgradeCost(currentLevel: Int): Int = when (currentLevel) {
        1 -> cost
        2 -> cost * 2
        else -> Int.MAX_VALUE
    }

    fun damageAt(level: Int): Float      = damage      * (1f + (level - 1) * 0.5f)
    fun rangeAt(level: Int): Float       = rangePx     * (1f + (level - 1) * 0.15f)
    fun intervalAt(level: Int): Float    = fireInterval * (1f - (level - 1) * 0.10f)
}

// ── Enemy variants ────────────────────────────────────────────────────────────

enum class EnemyVariant {
    NORMAL,   // Standard
    FAST,     // 2× Geschwindigkeit, 0.4× HP  – ab Welle 3
    ARMORED,  // 0.6× Geschwindigkeit, 2.5× HP, 10 Schadensreduktion – ab Welle 6
}

// ── Data classes ──────────────────────────────────────────────────────────────

data class Enemy(
    val id: Int,
    var position: Vec2,
    var waypointIndex: Int = 0,
    var health: Float,
    val maxHealth: Float,
    val baseSpeed: Float,
    var alive: Boolean = true,
    var reachedEnd: Boolean = false,
    var slowedUntil: Float = 0f,
    val isBoss: Boolean = false,
    val variant: EnemyVariant = EnemyVariant.NORMAL,
    val armor: Float = 0f,
)

data class Tower(
    val gridPos: GridPos,
    val type: TowerType,
    var cooldown: Float = 0f,
    var level: Int = 1,         // 1 = Basis, 2 = Aufgewertet, 3 = Maximum
)

data class Projectile(
    var position: Vec2,
    val targetId: Int,
    val damage: Float,
    val speed: Float = 300f,
    var alive: Boolean = true,
    val slowDuration: Float = 0f,
)
