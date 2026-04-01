package com.example.towerdefense.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
fun GameScreen(onBackToMenu: (score: Int, wave: Int, victory: Boolean) -> Unit) {
    val engine = remember { GameEngine() }
    var frameCount by remember { mutableStateOf(0) }
    var selectedTower by remember { mutableStateOf(TowerType.RED) }

    LaunchedEffect(Unit) {
        var lastMs = withFrameMillis { it }
        while (isActive) {
            val nowMs = withFrameMillis { it }
            engine.update((nowMs - lastMs) / 1000f)
            lastMs = nowMs
            frameCount++
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(C_BG_UI),
    ) {
        Hud(engine)

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
                        detectTapGestures { tap ->
                            val gx   = (tap.x - offX) / scale
                            val gy   = (tap.y - offY) / scale
                            val cell = GameMap.pixelToGrid(gx, gy)
                            engine.tryPlaceTower(cell.col, cell.row, selectedTower)
                        }
                    },
            ) {
                @Suppress("UNUSED_EXPRESSION") frameCount
                withTransform(
                    transformBlock = {
                        translate(left = offX, top = offY)
                        scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
                    },
                ) {
                    drawGame(engine)
                }
            }
        }

        BottomBar(engine, selectedTower, frameCount, onBackToMenu) { selectedTower = it }
    }
}

// ── HUD ────────────────────────────────────────────────────────────────────

@Composable
private fun Hud(engine: GameEngine) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(C_HUD_BG)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HudStat("Leben", "${engine.lives}",                          Color(0xFFe74c3c))
        HudStat("Gold",  "${engine.gold} ¢",                        Color(0xFFf1c40f))
        HudStat("Welle", "${engine.wave}/${GameMap.TOTAL_WAVES}",    Color(0xFF2ecc71))
        HudStat("Score", "${engine.score}",                          Color.White)
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
    @Suppress("UNUSED_PARAMETER") frameCount: Int,
    onBackToMenu: (score: Int, wave: Int, victory: Boolean) -> Unit,
    onSelect: (TowerType) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(C_HUD_BG)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Tower selection row
        if (!engine.gameOver && !engine.victory) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                TowerType.entries.forEach { type ->
                    val color       = towerColor[type]!!
                    val canAfford   = engine.gold >= type.cost
                    val isSelected  = selected == type
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
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(6.dp),
                            )
                            .background(
                                color = if (isSelected) color.copy(alpha = 0.3f) else color.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp),
                            )
                            .then(
                                if (!engine.gameOver && !engine.victory)
                                    Modifier.pointerInput(type) {
                                        detectTapGestures { onSelect(type) }
                                    }
                                else Modifier
                            )
                            .padding(horizontal = 6.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (canAfford) color else color.copy(alpha = 0.4f),
                            )
                            Text(
                                desc,
                                fontSize = 9.sp,
                                color = Color.Gray,
                            )
                            Text(
                                "${type.cost} ¢",
                                fontSize = 11.sp,
                                color = if (canAfford) Color.White else Color(0xFFe74c3c),
                            )
                        }
                    }
                }
            }
        }

        // Wave / status row
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            when {
                engine.gameOver -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        "GAME OVER  —  Score: ${engine.score}",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFe74c3c),
                    )
                    Button(
                        onClick = { onBackToMenu(engine.score, engine.wave, false) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFe74c3c).copy(alpha = 0.2f)),
                    ) {
                        Text("Hauptmenü", color = Color(0xFFe74c3c), fontWeight = FontWeight.SemiBold)
                    }
                }
                engine.victory -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        "SIEG!  —  Score: ${engine.score}",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2ecc71),
                    )
                    Button(
                        onClick = { onBackToMenu(engine.score, engine.wave, true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ecc71).copy(alpha = 0.2f)),
                    ) {
                        Text("Hauptmenü", color = Color(0xFF2ecc71), fontWeight = FontWeight.SemiBold)
                    }
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

// ── Canvas drawing ─────────────────────────────────────────────────────────

private fun DrawScope.drawGame(engine: GameEngine) {
    val cs = GameMap.CELL_SIZE

    // 1. Background
    drawRect(C_BG_GAME, size = Size(GameMap.GAME_WIDTH, GameMap.GAME_HEIGHT))

    // 2. Grid lines
    for (col in 0..GameMap.COLS) {
        drawLine(C_GRID, Offset(col * cs, 0f), Offset(col * cs, GameMap.GAME_HEIGHT), strokeWidth = 0.8f)
    }
    for (row in 0..GameMap.ROWS) {
        drawLine(C_GRID, Offset(0f, row * cs), Offset(GameMap.GAME_WIDTH, row * cs), strokeWidth = 0.8f)
    }

    // 3. Path cells
    for (cell in GameMap.pathCells) {
        val x = cell.col * cs
        val y = cell.row * cs
        drawRect(C_PATH,    topLeft = Offset(x + 1f, y + 1f), size = Size(cs - 2f, cs - 2f))
        drawRect(C_PATH_BD, topLeft = Offset(x + 1f, y + 1f), size = Size(cs - 2f, cs - 2f), style = Stroke(1f))
    }

    // 4. Tower range rings
    for (tower in engine.towers) {
        val cx    = tower.gridPos.col * cs + cs / 2f
        val cy    = tower.gridPos.row * cs + cs / 2f
        val color = (towerColor[tower.type] ?: Color.White).copy(alpha = 0.12f)
        drawCircle(color, radius = tower.type.rangePx, center = Offset(cx, cy))
    }

    // 5. Towers
    for (tower in engine.towers) {
        val cx    = tower.gridPos.col * cs + cs / 2f
        val cy    = tower.gridPos.row * cs + cs / 2f
        val pad   = 5f
        val base  = towerColor[tower.type]  ?: Color.White
        val dark  = towerDark[tower.type]   ?: Color.Gray
        drawRect(base, topLeft = Offset(cx - cs / 2f + pad, cy - cs / 2f + pad), size = Size(cs - pad * 2, cs - pad * 2))
        drawCircle(dark,       radius = cs / 3.5f, center = Offset(cx, cy))
        drawCircle(Color.Black, radius = cs / 3.5f, center = Offset(cx, cy), style = Stroke(1.5f))
    }

    // 6. Enemies
    for (enemy in engine.enemies) {
        val ex     = enemy.position.x
        val ey     = enemy.position.y
        val r      = if (enemy.isBoss) 20f else 13f
        val color  = if (enemy.isBoss) Color(0xFF8e44ad) else Color(0xFFe74c3c)
        val border = if (enemy.isBoss) Color(0xFF6c3483) else Color(0xFFa93226)
        drawCircle(color,  radius = r, center = Offset(ex, ey))
        drawCircle(border, radius = r, center = Offset(ex, ey), style = Stroke(if (enemy.isBoss) 2.5f else 1.5f))

        // Slow indicator (blue ring)
        if (engine.gameTime < enemy.slowedUntil) {
            drawCircle(Color(0x883498db), radius = r + 4f, center = Offset(ex, ey), style = Stroke(2.5f))
        }

        // Health bar
        val barW = if (enemy.isBoss) 36f else 26f
        val barH = if (enemy.isBoss) 5f  else 4f
        val barX = ex - barW / 2f
        val barY = ey - r - 8f
        drawRect(C_HP_BG, topLeft = Offset(barX, barY), size = Size(barW, barH))
        val hpFrac = (enemy.health / enemy.maxHealth).coerceIn(0f, 1f)
        val hpColor = if (enemy.isBoss) Color(0xFFe74c3c) else C_HP_FG
        drawRect(hpColor, topLeft = Offset(barX, barY), size = Size(barW * hpFrac, barH))
    }

    // 7. Projectiles
    for (proj in engine.projectiles) {
        val projColor = if (proj.slowDuration > 0f) Color(0xFF3498db) else C_PROJ
        drawCircle(projColor, radius = 5f, center = Offset(proj.position.x, proj.position.y))
    }
}
