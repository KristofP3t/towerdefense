package com.example.towerdefense.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import kotlin.math.sin
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.towerdefense.game.*
import kotlinx.coroutines.isActive

// ── Palette ────────────────────────────────────────────────────────────────
private val C_BG_UI    = Color(0xFF1a1a2e)
private val C_BG_GAME  = Color(0xFF2d5a27)
private val C_GRID     = Color(0xFF214020)
private val C_PATH     = Color(0xFFc4a44a)
private val C_PATH_BD  = Color(0xFFa08030)
private val C_PROJ     = Color(0xFFf1c40f)
private val C_HP_BG    = Color(0xFF444444)
private val C_HP_FG    = Color(0xFF2ecc71)
private val C_HUD_BG   = Color(0xFF16213e)

private val towerColor = mapOf(
    TowerType.RED    to Color(0xFFe74c3c),
    TowerType.YELLOW to Color(0xFFf1c40f),
    TowerType.BLUE   to Color(0xFF3498db),
)
private val towerDark = mapOf(
    TowerType.RED    to Color(0xFF922b21),
    TowerType.YELLOW to Color(0xFF9a7d0a),
    TowerType.BLUE   to Color(0xFF1a5276),
)

// ── Main composable ────────────────────────────────────────────────────────

@Composable
fun GameScreen(
    difficulty: Difficulty,
    onGameEnd: (com.example.towerdefense.game.GameStats) -> Unit,
) {
    val engine = remember { GameEngine(difficulty) }
    var frameCount    by remember { mutableStateOf(0) }
    var selectedTower by remember { mutableStateOf(TowerType.RED) }
    var selectedCell  by remember { mutableStateOf<GridPos?>(null) }
    var gameSpeed     by remember { mutableStateOf(1f) }
    var paused        by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        var lastMs = withFrameMillis { it }
        while (isActive) {
            val nowMs = withFrameMillis { it }
            if (!paused) engine.update((nowMs - lastMs) / 1000f * gameSpeed)
            lastMs = nowMs
            frameCount++
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(C_BG_UI),
    ) {
        Hud(engine, gameSpeed, paused,
            onToggleSpeed = { gameSpeed = if (gameSpeed == 1f) 2f else 1f },
            onTogglePause = { paused = !paused },
            onExit        = { onGameEnd(engine.buildStats()) },
        )

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val density = LocalDensity.current
            val areaW = with(density) { maxWidth.toPx() }
            val areaH = with(density) { maxHeight.toPx() }
            val scale = minOf(areaW / GameMap.GAME_WIDTH, areaH / GameMap.GAME_HEIGHT)
            val offX  = (areaW - GameMap.GAME_WIDTH  * scale) / 2f
            val offY  = (areaH - GameMap.GAME_HEIGHT * scale) / 2f

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(scale, offX, offY) {
                        detectTapGestures(
                            onTap = { tap ->
                                val gx   = (tap.x - offX) / scale
                                val gy   = (tap.y - offY) / scale
                                val cell = GameMap.pixelToGrid(gx, gy)
                                val towerAtCell = engine.towers.find { it.gridPos == cell }
                                if (towerAtCell != null) {
                                    selectedCell = if (selectedCell == cell) null else cell
                                } else {
                                    selectedCell = null
                                    engine.tryPlaceTower(cell.col, cell.row, selectedTower)
                                }
                            },
                            onLongPress = { tap ->
                                // Touch-Optimierung: langer Druck platziert Turm (Mobile)
                                val gx   = (tap.x - offX) / scale
                                val gy   = (tap.y - offY) / scale
                                val cell = GameMap.pixelToGrid(gx, gy)
                                if (engine.towers.none { it.gridPos == cell }) {
                                    selectedCell = null
                                    engine.tryPlaceTower(cell.col, cell.row, selectedTower)
                                }
                            },
                        )
                    },
            ) {
                @Suppress("UNUSED_EXPRESSION") frameCount
                withTransform(
                    transformBlock = {
                        translate(left = offX, top = offY)
                        scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
                    },
                ) {
                    drawGame(engine, selectedCell)
                }
            }
        }

        BottomBar(engine, selectedTower, selectedCell, frameCount, onGameEnd,
            onSelectTower = { selectedTower = it; selectedCell = null },
            onUpgrade = {
                val cell = selectedCell ?: return@BottomBar
                engine.tryUpgradeTower(cell.col, cell.row)
                selectedCell = null
            },
            onSell = {
                val cell = selectedCell ?: return@BottomBar
                engine.sellTower(cell.col, cell.row)
                selectedCell = null
            },
            onDeselectCell = { selectedCell = null },
        )
    }
}

// ── HUD ────────────────────────────────────────────────────────────────────

@Composable
private fun Hud(engine: GameEngine, gameSpeed: Float, paused: Boolean, onToggleSpeed: () -> Unit, onTogglePause: () -> Unit, onExit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(C_HUD_BG)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HudStat("Leben", "${engine.lives}",                        Color(0xFFe74c3c))
        HudStat("Gold",  "${engine.gold} ¢",                      Color(0xFFf1c40f))
        HudStat("Welle", "${engine.wave}/${GameMap.TOTAL_WAVES}",  Color(0xFF2ecc71))
        HudStat("Score", "${engine.score}",                        Color.White)

        // Pause-Toggle
        Button(
            onClick = onTogglePause,
            modifier = Modifier.height(34.dp),
            contentPadding = PaddingValues(horizontal = 10.dp),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (paused) Color(0xFF2ecc71).copy(alpha = 0.25f) else Color(0xFF2c3e50),
            ),
        ) {
            Text(
                if (paused) "▶" else "⏸",
                fontSize = 14.sp, fontWeight = FontWeight.Bold,
                color = if (paused) Color(0xFF2ecc71) else Color.White,
            )
        }

        // Geschwindigkeits-Toggle
        Button(
            onClick = onToggleSpeed,
            modifier = Modifier.height(34.dp),
            contentPadding = PaddingValues(horizontal = 10.dp),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (gameSpeed == 2f) Color(0xFFf1c40f) else Color(0xFF2c3e50),
            ),
        ) {
            Text(
                if (gameSpeed == 2f) "2×" else "1×",
                fontSize = 13.sp, fontWeight = FontWeight.Bold,
                color = if (gameSpeed == 2f) Color.Black else Color.White,
            )
        }

        // Beenden-Button
        Button(
            onClick = onExit,
            modifier = Modifier.height(34.dp),
            contentPadding = PaddingValues(horizontal = 10.dp),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7f8c8d).copy(alpha = 0.4f)),
        ) {
            Text("✕ Menü", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFbdc3c7))
        }
    }
}

@Composable
private fun HudStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Color.Gray)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

// ── Bottom bar ─────────────────────────────────────────────────────────────

@Composable
private fun BottomBar(
    engine: GameEngine,
    selected: TowerType,
    selectedCell: GridPos?,
    @Suppress("UNUSED_PARAMETER") frameCount: Int,
    onGameEnd: (com.example.towerdefense.game.GameStats) -> Unit,
    onSelectTower: (TowerType) -> Unit,
    onUpgrade: () -> Unit,
    onSell: () -> Unit,
    onDeselectCell: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(C_HUD_BG)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val upgradeTower = selectedCell?.let { cell -> engine.towers.find { it.gridPos == cell } }

        if (!engine.gameOver && !engine.victory) {
            if (upgradeTower != null) {
                // ── Upgrade-Panel ──────────────────────────────────────────
                UpgradePanel(engine, upgradeTower, onUpgrade, onSell, onDeselectCell)
            } else {
                // ── Turm-Auswahl ───────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    TowerType.entries.forEach { type ->
                        val color      = towerColor[type]!!
                        val canAfford  = engine.gold >= type.cost
                        val isSelected = selected == type
                        val label = when (type) {
                            TowerType.RED    -> "ROT"
                            TowerType.YELLOW -> "GELB"
                            TowerType.BLUE   -> "BLAU"
                        }
                        val desc = when (type) {
                            TowerType.RED    -> "Hoher Schaden"
                            TowerType.YELLOW -> "Große Reichweite"
                            TowerType.BLUE   -> "Verlangsamt"
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 52.dp)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp),
                                )
                                .background(
                                    color = if (isSelected) color.copy(alpha = 0.3f) else color.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp),
                                )
                                .pointerInput(type) { detectTapGestures { onSelectTower(type) } }
                                .padding(horizontal = 4.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                    color = if (canAfford) color else color.copy(alpha = 0.4f))
                                Text(desc, fontSize = 9.sp, color = Color.Gray, maxLines = 1)
                                Text("${type.cost} ¢", fontSize = 11.sp,
                                    color = if (canAfford) Color.White else Color(0xFFe74c3c))
                            }
                        }
                    }
                }
            }
        }

        // ── Wellen-Status / Spielende ──────────────────────────────────────
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            when {
                engine.gameOver -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("GAME OVER  —  Score: ${engine.score}",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFe74c3c))
                    Button(
                        onClick = { onGameEnd(engine.buildStats()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFe74c3c).copy(alpha = 0.2f)),
                    ) { Text("Statistiken →", color = Color(0xFFe74c3c), fontWeight = FontWeight.SemiBold) }
                }
                engine.victory -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("SIEG!  —  Score: ${engine.score}",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2ecc71))
                    Button(
                        onClick = { onGameEnd(engine.buildStats()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ecc71).copy(alpha = 0.2f)),
                    ) { Text("Statistiken →", color = Color(0xFF2ecc71), fontWeight = FontWeight.SemiBold) }
                }
                engine.waveActive -> {
                    val isBossWave = engine.wave % 5 == 0
                    Text(
                        if (isBossWave) "⚠ BOSS-Welle ${engine.wave} läuft …"
                        else "Welle ${engine.wave} läuft …",
                        fontSize = 13.sp,
                        color = if (isBossWave) Color(0xFFe74c3c) else Color.Gray,
                    )
                }
                else -> Button(
                    onClick = { engine.startWave() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ecc71)),
                ) {
                    val nextIsBoss = (engine.wave + 1) % 5 == 0
                    Text(
                        when {
                            engine.wave == 0 -> "Spiel starten"
                            nextIsBoss       -> "Nächste Welle ⚠ BOSS"
                            else             -> "Nächste Welle"
                        },
                        color = Color.Black, fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

// ── Upgrade-Panel ──────────────────────────────────────────────────────────

@Composable
private fun UpgradePanel(
    engine: GameEngine,
    tower: com.example.towerdefense.game.Tower,
    onUpgrade: () -> Unit,
    onSell: () -> Unit,
    onClose: () -> Unit,
) {
    val color      = towerColor[tower.type]!!
    val isMaxLevel = tower.level >= 3
    val upgradeCost = if (!isMaxLevel) tower.type.upgradeCost(tower.level) else 0
    val canAfford   = engine.gold >= upgradeCost

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Turminfo
        Column(modifier = Modifier.weight(1f)) {
            val typeName = when (tower.type) {
                TowerType.RED    -> "Roter Turm"
                TowerType.YELLOW -> "Gelber Turm"
                TowerType.BLUE   -> "Blauer Turm"
            }
            Text(typeName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
            Text(
                "Stufe ${tower.level}/3  •  " +
                "Schaden: ${tower.type.damageAt(tower.level).toInt()}  •  " +
                "Reichweite: ${tower.type.rangeAt(tower.level).toInt()} px",
                fontSize = 10.sp, color = Color.Gray,
            )
        }

        // Upgrade-Button
        if (!isMaxLevel) {
            Button(
                onClick = onUpgrade,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) Color(0xFF2ecc71) else Color(0xFF444444),
                    disabledContainerColor = Color(0xFF444444),
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    "Upgrade $upgradeCost ¢",
                    fontSize = 11.sp,
                    color = if (canAfford) Color.Black else Color.Gray,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        } else {
            Text("MAX", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFf1c40f))
        }

        // Verkaufen-Button
        Button(
            onClick = onSell,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFe67e22).copy(alpha = 0.25f)),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                "Verk. ${tower.type.sellValue(tower.level)} ¢",
                fontSize = 11.sp,
                color = Color(0xFFe67e22),
                fontWeight = FontWeight.SemiBold,
            )
        }

        // Schließen
        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(4.dp),
        ) {
            Text("✕", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

// ── Canvas drawing ─────────────────────────────────────────────────────────

private fun DrawScope.drawGame(engine: GameEngine, selectedCell: GridPos?) {
    val cs = GameMap.CELL_SIZE

    // 1. Hintergrund
    drawRect(C_BG_GAME, size = Size(GameMap.GAME_WIDTH, GameMap.GAME_HEIGHT))

    // 2. Gitterlinien
    for (col in 0..GameMap.COLS) {
        drawLine(C_GRID, Offset(col * cs, 0f), Offset(col * cs, GameMap.GAME_HEIGHT), strokeWidth = 0.8f)
    }
    for (row in 0..GameMap.ROWS) {
        drawLine(C_GRID, Offset(0f, row * cs), Offset(GameMap.GAME_WIDTH, row * cs), strokeWidth = 0.8f)
    }

    // 3. Pfad-Zellen
    for (cell in GameMap.pathCells) {
        val x = cell.col * cs; val y = cell.row * cs
        drawRect(C_PATH,    topLeft = Offset(x + 1f, y + 1f), size = Size(cs - 2f, cs - 2f))
        drawRect(C_PATH_BD, topLeft = Offset(x + 1f, y + 1f), size = Size(cs - 2f, cs - 2f), style = Stroke(1f))
    }

    // 4. Ausgewählte Zelle hervorheben
    if (selectedCell != null) {
        val x = selectedCell.col * cs; val y = selectedCell.row * cs
        drawRect(Color.White.copy(alpha = 0.25f), topLeft = Offset(x, y), size = Size(cs, cs))
        drawRect(Color.White.copy(alpha = 0.8f),  topLeft = Offset(x, y), size = Size(cs, cs), style = Stroke(2f))
    }

    // 5. Turm-Reichweiten-Ringe
    for (tower in engine.towers) {
        val cx    = tower.gridPos.col * cs + cs / 2f
        val cy    = tower.gridPos.row * cs + cs / 2f
        val color = (towerColor[tower.type] ?: Color.White).copy(alpha = 0.12f)
        drawCircle(color, radius = tower.type.rangeAt(tower.level), center = Offset(cx, cy))
    }

    // 6. Türme
    for (tower in engine.towers) {
        val cx   = tower.gridPos.col * cs + cs / 2f
        val cy   = tower.gridPos.row * cs + cs / 2f
        val pad  = 5f
        val base = towerColor[tower.type]  ?: Color.White
        val dark = towerDark[tower.type]   ?: Color.Gray
        drawRect(base, topLeft = Offset(cx - cs / 2f + pad, cy - cs / 2f + pad), size = Size(cs - pad * 2, cs - pad * 2))
        drawCircle(dark,        radius = cs / 3.5f, center = Offset(cx, cy))
        drawCircle(Color.Black, radius = cs / 3.5f, center = Offset(cx, cy), style = Stroke(1.5f))

        // Mündungsblitz (4.5): leuchtet kurz nach dem Schuss
        val justFired = tower.cooldown > tower.type.intervalAt(tower.level) * 0.85f
        if (justFired) {
            drawCircle(Color.White.copy(alpha = 0.6f), radius = cs / 5f, center = Offset(cx, cy))
        }

        // Upgrade-Stufen-Punkte
        if (tower.level > 1) {
            val dotSpacing = 6f
            val startX = cx - (tower.level - 1) * dotSpacing / 2f
            for (i in 0 until tower.level) {
                drawCircle(Color(0xFFf1c40f), radius = 2.5f, center = Offset(startX + i * dotSpacing, cy - cs / 2f + 4f))
            }
        }
    }

    // 7. Feinde (mit Bobbing-Animation 4.5)
    for (enemy in engine.enemies) {
        val ex = enemy.position.x
        val bobOffset = sin(engine.gameTime * 4f + enemy.id * 0.7f) * 2f
        val ey = enemy.position.y + bobOffset
        val (r, color, border) = when {
            enemy.isBoss -> Triple(20f, Color(0xFF8e44ad), Color(0xFF6c3483))
            enemy.variant == EnemyVariant.FAST    -> Triple(10f, Color(0xFFe67e22), Color(0xFFd35400))
            enemy.variant == EnemyVariant.ARMORED -> Triple(16f, Color(0xFF7f8c8d), Color(0xFF566573))
            else -> Triple(13f, Color(0xFFe74c3c), Color(0xFFa93226))
        }
        drawCircle(color,  radius = r, center = Offset(ex, ey))
        drawCircle(border, radius = r, center = Offset(ex, ey), style = Stroke(if (enemy.isBoss) 2.5f else 1.5f))

        // Verlangsamungs-Ring
        if (engine.gameTime < enemy.slowedUntil) {
            drawCircle(Color(0x883498db), radius = r + 4f, center = Offset(ex, ey), style = Stroke(2.5f))
        }

        // Panzerungs-Ring für gepanzerte Feinde
        if (enemy.variant == EnemyVariant.ARMORED) {
            drawCircle(Color(0xFFbdc3c7), radius = r + 2f, center = Offset(ex, ey), style = Stroke(1.5f))
        }

        // Lebensbalken
        val barW = if (enemy.isBoss) 36f else 26f
        val barH = if (enemy.isBoss) 5f  else 4f
        val barX = ex - barW / 2f
        val barY = ey - r - 8f
        drawRect(C_HP_BG, topLeft = Offset(barX, barY), size = Size(barW, barH))
        val hpFrac  = (enemy.health / enemy.maxHealth).coerceIn(0f, 1f)
        val hpColor = when {
            enemy.isBoss -> Color(0xFFe74c3c)
            enemy.variant == EnemyVariant.ARMORED -> Color(0xFF95a5a6)
            else -> C_HP_FG
        }
        drawRect(hpColor, topLeft = Offset(barX, barY), size = Size(barW * hpFrac, barH))
    }

    // 8. Projektile
    for (proj in engine.projectiles) {
        val projColor = if (proj.slowDuration > 0f) Color(0xFF3498db) else C_PROJ
        drawCircle(projColor, radius = 5f, center = Offset(proj.position.x, proj.position.y))
    }

    // 9. Partikel (4.2)
    for (p in engine.particles) {
        val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
        val baseColor = when (p.type) {
            ParticleType.NORMAL  -> Color(0xFFf1c40f)
            ParticleType.BOSS    -> Color(0xFF8e44ad)
            ParticleType.ARMORED -> Color(0xFFbdc3c7)
        }
        drawCircle(baseColor.copy(alpha = alpha), radius = p.radius * alpha,
            center = Offset(p.position.x, p.position.y))
    }
}
