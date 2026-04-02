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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.towerdefense.game.MapDefinition

@Composable
fun MapSelectScreen(
    onMapSelected: (MapDefinition) -> Unit,
    onBack: () -> Unit,
) {
    var selected by remember { mutableStateOf(MapDefinition.FOREST) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0f0c29), Color(0xFF302b63)))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                "KARTE WÄHLEN",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF95a5a6),
                letterSpacing = 4.sp,
            )

            // ── Kartenvorschauen ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MapDefinition.ALL.forEach { map ->
                    val isSelected = map.id == selected.id
                    val accentColor = when (map.id) {
                        "forest" -> Color(0xFF2ecc71)
                        "desert" -> Color(0xFFe67e22)
                        else     -> Color(0xFF3498db)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) accentColor else Color(0xFF444466),
                                shape = RoundedCornerShape(12.dp),
                            )
                            .background(
                                color = if (isSelected) accentColor.copy(alpha = 0.12f) else Color(0xFF1e1e3a),
                                shape = RoundedCornerShape(12.dp),
                            )
                            .pointerInput(map.id) { detectTapGestures { selected = map } }
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            map.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) accentColor else Color(0xFF888899),
                        )

                        // Mini-Kartenvorschau via Canvas
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(map.cols.toFloat() / map.rows.toFloat()),
                        ) {
                            val scaleX = size.width  / map.cols
                            val scaleY = size.height / map.rows

                            // Hintergrund
                            drawRect(Color(0xFF2d5a27), size = size)

                            // Pfad-Zellen
                            for (cell in map.pathCells) {
                                drawRect(
                                    color   = Color(0xFFc4a44a),
                                    topLeft = Offset(cell.col * scaleX, cell.row * scaleY),
                                    size    = Size(scaleX, scaleY),
                                )
                            }

                            // Pfad-Linie
                            for (i in 0 until map.waypoints.size - 1) {
                                val a = map.waypoints[i]
                                val b = map.waypoints[i + 1]
                                drawLine(
                                    color       = accentColor.copy(alpha = 0.7f),
                                    start       = Offset(a.col * scaleX + scaleX / 2, a.row * scaleY + scaleY / 2),
                                    end         = Offset(b.col * scaleX + scaleX / 2, b.row * scaleY + scaleY / 2),
                                    strokeWidth = 2f,
                                )
                            }

                            // Start / Ziel
                            val start = map.waypoints.first()
                            val end   = map.waypoints.last()
                            drawCircle(Color(0xFF2ecc71), radius = scaleX * 0.4f,
                                center = Offset(start.col * scaleX + scaleX / 2, start.row * scaleY + scaleY / 2))
                            drawCircle(Color(0xFFe74c3c), radius = scaleX * 0.4f,
                                center = Offset(end.col * scaleX + scaleX / 2, end.row * scaleY + scaleY / 2))

                            // Rand
                            drawRect(Color.White.copy(alpha = 0.1f), size = size, style = Stroke(1f))
                        }

                        val pathLen = map.pathCells.size
                        Text("Pfadlänge: $pathLen Zellen", fontSize = 11.sp, color = Color(0xFF888899))
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Buttons ───────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ecc71).copy(alpha = 0.15f)),
                ) {
                    Text("← Zurück", color = Color(0xFF2ecc71), fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { onMapSelected(selected) },
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ecc71)),
                ) {
                    Text("Spielen →", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
