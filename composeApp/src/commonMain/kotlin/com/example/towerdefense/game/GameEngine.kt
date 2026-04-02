package com.example.towerdefense.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.min
import kotlin.random.Random

class GameEngine(private val difficulty: Difficulty = Difficulty.NORMAL) {

    // ── State ──────────────────────────────────────────────────────────────
    val enemies     = mutableListOf<Enemy>()
    val towers      = mutableListOf<Tower>()
    val projectiles = mutableListOf<Projectile>()

    var lives      by mutableStateOf(difficulty.startLives)
    var gold       by mutableStateOf(difficulty.startGold)
    var wave       by mutableStateOf(0)
    var score      by mutableStateOf(0)
    var gameOver   by mutableStateOf(false)
    var victory    by mutableStateOf(false)
    var waveActive by mutableStateOf(false)

    var gameTime = 0f

    private var toSpawn          = 0
    private var spawnTimer       = 0f
    private var nextEnemyId      = 0
    private var bossSpawnPending = false

    // ── Public API ─────────────────────────────────────────────────────────

    fun startWave() {
        if (waveActive || wave >= GameMap.TOTAL_WAVES || gameOver || victory) return
        wave++
        toSpawn          = 4 + wave * 2
        bossSpawnPending = (wave % 5 == 0)
        waveActive       = true
        spawnTimer       = 0f
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

    /** Upgrades den Turm an Position (col, row) um eine Stufe. Gibt false zurück wenn nicht möglich. */
    fun tryUpgradeTower(col: Int, row: Int): Boolean {
        val tower = towers.find { it.gridPos == GridPos(col, row) } ?: return false
        if (tower.level >= 3) return false
        val cost = tower.type.upgradeCost(tower.level)
        if (gold < cost) return false
        gold -= cost
        tower.level++
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

        val start  = GameMap.cellCenter(GameMap.waypoints.first())
        val isBoss = bossSpawnPending
        if (isBoss) bossSpawnPending = false

        enemies += if (isBoss) {
            Enemy(
                id        = nextEnemyId++,
                position  = start,
                health    = wave * 120f * difficulty.enemyHealthMult,
                maxHealth = wave * 120f * difficulty.enemyHealthMult,
                baseSpeed = 22f,
                isBoss    = true,
            )
        } else {
            val variant = pickVariant()
            val hpBase  = (25f + wave * 20f) * difficulty.enemyHealthMult
            Enemy(
                id        = nextEnemyId++,
                position  = start,
                health    = hpBase  * variant.healthMult,
                maxHealth = hpBase  * variant.healthMult,
                baseSpeed = (35f + wave * 5f) * variant.speedMult,
                variant   = variant.type,
                armor     = variant.armor,
            )
        }

        toSpawn--
        spawnTimer = if (isBoss) 0.3f else 1.0f
    }

    private data class VariantProps(
        val type: EnemyVariant,
        val healthMult: Float,
        val speedMult: Float,
        val armor: Float,
    )

    private fun pickVariant(): VariantProps {
        val roll = Random.nextFloat()
        return when {
            wave >= 6 && roll < 0.25f -> VariantProps(EnemyVariant.ARMORED, 2.5f, 0.6f, 10f)
            wave >= 3 && roll < 0.45f -> VariantProps(EnemyVariant.FAST,    0.4f, 2.0f, 0f)
            else                      -> VariantProps(EnemyVariant.NORMAL,   1.0f, 1.0f, 0f)
        }
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
            val range  = t.type.rangeAt(t.level)
            val target = enemies
                .filter { it.alive && !it.reachedEnd && center.distanceTo(it.position) <= range }
                .maxByOrNull { it.waypointIndex }
                ?: continue
            projectiles += Projectile(
                position     = center,
                targetId     = target.id,
                damage       = t.type.damageAt(t.level),
                slowDuration = t.type.slowDuration,
            )
            t.cooldown = t.type.intervalAt(t.level)
        }
    }

    private fun moveProjectiles(dt: Float) {
        for (p in projectiles) {
            if (!p.alive) continue
            val target = enemies.find { it.id == p.targetId && it.alive } ?: run { p.alive = false; continue }
            val diff = target.position - p.position
            val dist = diff.length()
            val move = p.speed * dt
            if (move >= dist) {
                val effectiveDamage = (p.damage - target.armor).coerceAtLeast(1f)
                target.health -= effectiveDamage
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
