package com.example.towerdefense.game

import kotlin.math.min

class GameEngine {

    // ── State ──────────────────────────────────────────────────────────────
    val enemies = mutableListOf<Enemy>()
    val towers = mutableListOf<Tower>()
    val projectiles = mutableListOf<Projectile>()

    var lives = 20
    var gold = 150
    var wave = 0
    var score = 0
    var gameOver = false
    var victory = false

    var waveActive = false
    private var toSpawn = 0
    private var spawnTimer = 0f
    private var nextEnemyId = 0

    // ── Public API ─────────────────────────────────────────────────────────

    fun startWave() {
        if (waveActive || wave >= GameMap.TOTAL_WAVES || gameOver || victory) return
        wave++
        toSpawn = 5 + wave * 3
        waveActive = true
        spawnTimer = 0f
    }

    /** Returns true if a tower was placed successfully. */
    fun tryPlaceTower(col: Int, row: Int): Boolean {
        val pos = GridPos(col, row)
        if (pos in GameMap.pathCells) return false
        if (towers.any { it.gridPos == pos }) return false
        if (gold < TOWER_COST) return false
        towers += Tower(gridPos = pos)
        gold -= TOWER_COST
        return true
    }

    fun update(delta: Float) {
        if (gameOver || victory) return
        val dt = min(delta, 0.05f) // cap at 50 ms to avoid tunneling on lag spikes
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
        if (spawnTimer <= 0f) {
            val start = GameMap.cellCenter(GameMap.waypoints.first())
            enemies += Enemy(
                id = nextEnemyId++,
                position = start,
                health = 80f + wave * 25f,
                maxHealth = 80f + wave * 25f,
                speed = 55f + wave * 5f,
            )
            toSpawn--
            spawnTimer = 1.2f
        }
    }

    private fun moveEnemies(dt: Float) {
        for (e in enemies) {
            if (!e.alive || e.reachedEnd) continue
            var distLeft = e.speed * dt
            // Walk along waypoint segments
            while (distLeft > 0f) {
                val nextIdx = e.waypointIndex + 1
                if (nextIdx >= GameMap.waypoints.size) {
                    e.reachedEnd = true
                    break
                }
                val target = GameMap.cellCenter(GameMap.waypoints[nextIdx])
                val diff = target - e.position
                val dist = diff.length()
                if (distLeft >= dist) {
                    e.position = target
                    e.waypointIndex = nextIdx
                    distLeft -= dist
                } else {
                    e.position = e.position + diff.normalized() * distLeft
                    distLeft = 0f
                }
            }
        }
    }

    private fun towerShoot(dt: Float) {
        for (t in towers) {
            t.cooldown -= dt
            if (t.cooldown > 0f) continue
            val center = GameMap.cellCenter(t.gridPos)
            // Target: furthest-along enemy within range
            val target = enemies
                .filter { it.alive && !it.reachedEnd && center.distanceTo(it.position) <= t.rangePx }
                .maxByOrNull { it.waypointIndex }
                ?: continue
            projectiles += Projectile(
                position = center,
                targetId = target.id,
                damage = t.damage,
            )
            t.cooldown = t.fireInterval
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
                // Hit!
                target.health -= p.damage
                if (target.health <= 0f) {
                    target.alive = false
                    gold += GOLD_PER_KILL
                    score += SCORE_PER_KILL
                }
                p.alive = false
            } else {
                p.position = p.position + diff.normalized() * move
            }
        }
    }

    private fun resolveCollisions() {
        // Enemies that reached the end deduct lives
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
        const val TOWER_COST = 50
        const val GOLD_PER_KILL = 10
        const val SCORE_PER_KILL = 25
        const val GOLD_WAVE_BONUS = 30
    }
}
