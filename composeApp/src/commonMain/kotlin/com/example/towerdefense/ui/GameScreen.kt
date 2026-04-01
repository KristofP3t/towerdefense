package com.example.towerdefense.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
private val C_BG_UI   = Color(0xFF1a1a2e)
private val C_BG_GAME = Color(0xFF2d5a27)
private val C_GRID    = Color(0xFF214020)
private val C_PATH    = Color(0xFFc4a44a)
private val C_PATH_BD = Color(0xFFa08030)
private val C_TOWER   = Color(0xFF4a90e2)
private val C_TOWER_D = Color(0xFF2c5f8a)
private val C_RANGE   = Color(0x184a90e2)
private val C_ENEMY   = Color(0xFFe74c3c)
private val C_ENEMY_BD= Color(0xFFa93226)
private val C_PROJ    = Color(0xFFf1c40f)
private val C_HP_BG   = Color(0xFF444444)
private val C_HP_FG   = Color(0xFF2ecc71)
private val C_HUD_BG  = Color(0xFF16213e)

// ── Main composable ────────────────────────────────────────────────────────

@Composable
fun GameScreen() {
    val engine = remember { GameEngine() }
    // frameCount drives Canvas redraws; state reads inside Canvas establish the dependency.
    var frameCount by remember { mutableStateOf(0) }

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
        Hud(engine, frameCount)

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val density = LocalDensity.current
            val areaW = with(density) { maxWidth.toPx() }
            val areaH = with(density) { maxHeight.toPx() }
            val scale = minOf(areaW / GameMap.GAME_WIDTH, areaH / GameMap.GAME_HEIGHT)
            val offX = (areaW - GameMap.GAME_WIDTH * scale) / 2f
            val offY = (areaH - GameMap.GAME_HEIGHT * scale) / 2f

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(scale, offX, offY) {
                        detectTapGestures { tap ->
                            // Convert screen tap → game pixel → grid cell
                            val gx = (tap.x - offX) / scale
                            val gy = (tap.y - offY) / scale
                            val cell = GameMap.pixelToGrid(gx, gy)
                            engine.tryPlaceTower(cell.col, cell.row)
                        }
                    },
            ) {
                // Reading frameCount here makes this block re-execute every frame.
                @Suppress("UNUSED_EXPRESSION")
                frameCount

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

        BottomBar(engine, frameCount)
    }
}

// ── HUD ────────────────────────────────────────────────────────────────────

@Composable
private fun Hud(engine: GameEngine, @Suppress("UNUSED_PARAMETER") frameCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(C_HUD_BG)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HudStat("Leben",  "${engine.lives}",              Color(0xFFe74c3c))
        HudStat("Gold",   "${engine.gold} ¢",             Color(0xFFf1c40f))
        HudStat("Welle",  "${engine.wave}/${GameMap.TOTAL_WAVES}", Color(0xFF2ecc71))
        HudStat("Score",  "${engine.score}",              Color.White)
        HudStat("Turm",   "50 ¢",                         Color(0xFF4a90e2))
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
private fun BottomBar(engine: GameEngine, @Suppress("UNUSED_PARAMETER") frameCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(C_HUD_BG)
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            engine.gameOver -> Text(
                "GAME OVER  —  Score: ${engine.score}",
                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFe74c3c),
            )
            engine.victory -> Text(
                "SIEG!  —  Score: ${engine.score}",
                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2ecc71),
            )
            engine.waveActive -> Text(
                "Welle ${engine.wave} läuft …",
                fontSize = 14.sp, color = Color.Gray,
            )
            else -> Button(
                onClick = { engine.startWave() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ecc71)),
            ) {
                Text(
                    if (engine.wave == 0) "Spiel starten" else "Nächste Welle",
                    color = Color.Black, fontWeight = FontWeight.Bold,
                )
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
        drawRect(C_PATH, topLeft = Offset(x + 1f, y + 1f), size = Size(cs - 2f, cs - 2f))
        drawRect(C_PATH_BD, topLeft = Offset(x + 1f, y + 1f), size = Size(cs - 2f, cs - 2f), style = Stroke(1f))
    }

    // 4. Tower range rings (drawn before towers so they appear below)
    for (tower in engine.towers) {
        val cx = tower.gridPos.col * cs + cs / 2f
        val cy = tower.gridPos.row * cs + cs / 2f
        drawCircle(C_RANGE, radius = tower.rangePx, center = Offset(cx, cy))
    }

    // 5. Towers
    for (tower in engine.towers) {
        val cx = tower.gridPos.col * cs + cs / 2f
        val cy = tower.gridPos.row * cs + cs / 2f
        val pad = 5f
        // Base
        drawRect(C_TOWER, topLeft = Offset(cx - cs / 2f + pad, cy - cs / 2f + pad), size = Size(cs - pad * 2, cs - pad * 2))
        // Turret
        drawCircle(C_TOWER_D, radius = cs / 3.5f, center = Offset(cx, cy))
        drawCircle(Color.Black, radius = cs / 3.5f, center = Offset(cx, cy), style = Stroke(1.5f))
    }

    // 6. Enemies
    for (enemy in engine.enemies) {
        val ex = enemy.position.x
        val ey = enemy.position.y
        val r = 13f
        drawCircle(C_ENEMY, radius = r, center = Offset(ex, ey))
        drawCircle(C_ENEMY_BD, radius = r, center = Offset(ex, ey), style = Stroke(1.5f))
        // Health bar
        val barW = 26f
        val barH = 4f
        val barX = ex - barW / 2f
        val barY = ey - r - 7f
        drawRect(C_HP_BG, topLeft = Offset(barX, barY), size = Size(barW, barH))
        val hpFrac = (enemy.health / enemy.maxHealth).coerceIn(0f, 1f)
        drawRect(C_HP_FG, topLeft = Offset(barX, barY), size = Size(barW * hpFrac, barH))
    }

    // 7. Projectiles
    for (proj in engine.projectiles) {
        drawCircle(C_PROJ, radius = 5f, center = Offset(proj.position.x, proj.position.y))
    }
}
