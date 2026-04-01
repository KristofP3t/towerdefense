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
    BLUE  (damage = 12f, rangePx = 160f, fireInterval = 2.0f, cost = 60, slowDuration = 1.0f),
}

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
)

data class Tower(
    val gridPos: GridPos,
    val type: TowerType,
    var cooldown: Float = 0f,
)

data class Projectile(
    var position: Vec2,
    val targetId: Int,
    val damage: Float,
    val speed: Float = 300f,
    var alive: Boolean = true,
    val slowDuration: Float = 0f,
)
