package com.example.towerdefense.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.min

class GameEngine {

    // ── State ──────────────────────────────────────────────────────────────
    val enemies     = mutableListOf<Enemy>()
    val towers      = mutableListOf<Tower>()
    val projectiles = mutableListOf<Projectile>()

    var lives      by mutableStateOf(20)
    var gold       by mutableStateOf(150)
    var wave       by mutableStateOf(0)
    var score      by mutableStateOf(0)
    var gameOver   by mutableStateOf(false)
    var victory    by mutableStateOf(false)
    var waveActive by mutableStateOf(false)

    var gameTime = 0f   // total elapsed time in seconds (plain var, canvas reads it each frame)

    private var toSpawn           = 0
    private var spawnTimer        = 0f
    private var nextEnemyId       = 0
    private var bossSpawnPending  = false

    // ── Public API ─────────────────────────────────────────────────────────

    fun startWave() {
        if (waveActive || wave >= GameMap.TOTAL_WAVES || gameOver || victory) return
        wave++
        toSpawn           = 4 + wave * 2
        bossSpawnPending  = (wave % 5 == 0)
        waveActive        = true
        spawnTimer        = 0f
    }

    fun tryPlaceTower(col: Int, row: Int, type: TowerType): Boolean {
        val pos = GridPos(col, row)
        if (pos in GameMap.pathCells) return false
        if (towers.any { it.gridPos == pos }) return false
        if (gold < type.cost) return false
        towers += Tower(gridPos = pos, type = type)
        gold -= type.cost
        return true
    }

    fun update(delta: Float) {
        if (gameOver || victory) return
        val dt = min(delta, 0.05f)
        gameTime += dt
        spawnEnemies(dt)
        moveEnemies(dt)
        towerShoot(dt)
        moveProjectiles(dt)
        resolveCollisions()
        checkWaveEnd()
    }

    // ── Private logic ──────────────────────────────────────────────────────

    private fun spawnEnemies(dt: Float) {
        if (!waveActive || toSpawn <= 0) return
        spawnTimer -= dt
        if (spawnTimer > 0f) return

        val start    = GameMap.cellCenter(GameMap.waypoints.first())
        val isBoss   = bossSpawnPending
        if (isBoss) bossSpawnPending = false

        enemies += if (isBoss) {
            Enemy(
                id        = nextEnemyId++,
                position  = start,
                health    = wave * 120f,
                maxHealth = wave * 120f,
                baseSpeed = 22f,
                isBoss    = true,
            )
        } else {
            Enemy(
                id        = nextEnemyId++,
                position  = start,
                health    = 25f + wave * 20f,
                maxHealth = 25f + wave * 20f,
                baseSpeed = 35f + wave * 5f,
            )
        }

        toSpawn--
        spawnTimer = if (isBoss) 0.3f else 1.0f
    }

    private fun moveEnemies(dt: Float) {
        for (e in enemies) {
            if (!e.alive || e.reachedEnd) continue
            val effectiveSpeed = if (gameTime < e.slowedUntil) e.baseSpeed * 0.5f else e.baseSpeed
            var distLeft = effectiveSpeed * dt
            while (distLeft > 0f) {
                val nextIdx = e.waypointIndex + 1
                if (nextIdx >= GameMap.waypoints.size) { e.reachedEnd = true; break }
                val target = GameMap.cellCenter(GameMap.waypoints[nextIdx])
                val diff   = target - e.position
                val dist   = diff.length()
                if (distLeft >= dist) {
                    e.position      = target
                    e.waypointIndex = nextIdx
                    distLeft       -= dist
                } else {
                    e.position = e.position + diff.normalized() * distLeft
                    distLeft   = 0f
                }
            }
        }
    }

    private fun towerShoot(dt: Float) {
        for (t in towers) {
            t.cooldown -= dt
            if (t.cooldown > 0f) continue
            val center = GameMap.cellCenter(t.gridPos)
            val target = enemies
                .filter { it.alive && !it.reachedEnd && center.distanceTo(it.position) <= t.type.rangePx }
                .maxByOrNull { it.waypointIndex }
                ?: continue
            projectiles += Projectile(
                position     = center,
                targetId     = target.id,
                damage       = t.type.damage,
                slowDuration = t.type.slowDuration,
            )
            t.cooldown = t.type.fireInterval
        }
    }

    private fun moveProjectiles(dt: Float) {
        for (p in projectiles) {
            if (!p.alive) continue
            val target = enemies.find { it.id == p.targetId && it.alive }
            if (target == null) { p.alive = false; continue }
            val diff = target.position - p.position
            val dist = diff.length()
            val move = p.speed * dt
            if (move >= dist) {
                target.health -= p.damage
                if (p.slowDuration > 0f) {
                    target.slowedUntil = maxOf(target.slowedUntil, gameTime + p.slowDuration)
                }
                if (target.health <= 0f) {
                    target.alive = false
                    gold  += if (target.isBoss) GOLD_BOSS_KILL  else GOLD_PER_KILL
                    score += if (target.isBoss) SCORE_BOSS_KILL else SCORE_PER_KILL
                }
                p.alive = false
            } else {
                p.position = p.position + diff.normalized() * move
            }
        }
    }

    private fun resolveCollisions() {
        val reached = enemies.count { it.reachedEnd }
        if (reached > 0) {
            lives = (lives - reached).coerceAtLeast(0)
            if (lives == 0) gameOver = true
        }
        enemies.removeAll { !it.alive || it.reachedEnd }
        projectiles.removeAll { !it.alive }
    }

    private fun checkWaveEnd() {
        if (!waveActive || toSpawn > 0 || enemies.isNotEmpty()) return
        waveActive = false
        if (wave >= GameMap.TOTAL_WAVES) {
            victory = true
        } else {
            gold += GOLD_WAVE_BONUS
        }
    }

    // ── Constants ──────────────────────────────────────────────────────────
    companion object {
        const val GOLD_PER_KILL   = 10
        const val GOLD_BOSS_KILL  = 60
        const val SCORE_PER_KILL  = 25
        const val SCORE_BOSS_KILL = 200
        const val GOLD_WAVE_BONUS = 30
    }
}
