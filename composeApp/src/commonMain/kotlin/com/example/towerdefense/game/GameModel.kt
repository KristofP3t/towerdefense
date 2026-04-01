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

data class Enemy(
    val id: Int,
    var position: Vec2,
    var waypointIndex: Int = 0,
    var health: Float,
    val maxHealth: Float,
    val speed: Float,
    var alive: Boolean = true,
    var reachedEnd: Boolean = false,
)

data class Tower(
    val gridPos: GridPos,
    val damage: Float = 25f,
    val rangePx: Float = 144f,
    val fireInterval: Float = 1.5f,
    var cooldown: Float = 0f,
)

data class Projectile(
    var position: Vec2,
    val targetId: Int,
    val damage: Float,
    val speed: Float = 300f,
    var alive: Boolean = true,
)
