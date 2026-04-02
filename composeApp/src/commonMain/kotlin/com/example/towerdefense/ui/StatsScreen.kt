package com.example.towerdefense.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.towerdefense.game.GameStats

@Composable
fun StatsScreen(
    stats: GameStats,
    onContinue: (playerName: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }

    val resultColor = if (stats.victory) Color(0xFF2ecc71) else Color(0xFFe74c3c)
    val resultLabel = if (stats.victory) "SIEG!" else "NIEDERLAGE"
    val accuracy    = if (stats.shotsFired > 0) stats.shotsHit * 100 / stats.shotsFired else 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0f0c29), Color(0xFF302b63)))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Ergebnis-Header
            Text(resultLabel, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = resultColor)
            Text(
                "Welle ${stats.wave}/${stats.totalWaves}  •  Score: ${stats.score}",
                fontSize = 16.sp, color = Color(0xFFaaaacc),
            )

            Spacer(Modifier.height(8.dp))

            // Statistiken
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatRow("Türme gebaut",      "${stats.towersBuilt}",   Color(0xFF3498db))
                StatRow("Gegner getötet",    "${stats.enemiesKilled}", Color(0xFF2ecc71))
                StatRow("Bosse getötet",     "${stats.bossesKilled}",  Color(0xFF8e44ad))
                StatRow("Schüsse abgefeuert","${stats.shotsFired}",    Color(0xFFf1c40f))
                StatRow("Trefferquote",      "$accuracy %",            Color(0xFFe67e22))
                StatRow("Gold verdient",     "${stats.goldEarned} ¢",  Color(0xFFf1c40f))
            }

            Spacer(Modifier.height(4.dp))

            // Name eingeben
            Text("Dein Name für die Highscore-Liste:", fontSize = 13.sp, color = Color(0xFF888899))

            OutlinedTextField(
                value         = name,
                onValueChange = { if (it.length <= 20) name = it },
                placeholder   = { Text("Spielername …", color = Color(0xFF555577)) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Color(0xFF2ecc71),
                    unfocusedBorderColor = Color(0xFF444466),
                    focusedTextColor     = Color.White,
                    unfocusedTextColor   = Color.White,
                    cursorColor          = Color(0xFF2ecc71),
                ),
                shape = RoundedCornerShape(8.dp),
            )

            Button(
                onClick   = { onContinue(name) },
                modifier  = Modifier.fillMaxWidth().height(50.dp),
                shape     = RoundedCornerShape(10.dp),
                colors    = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ecc71)),
            ) {
                Text(
                    "Eintragen & zum Hauptmenü",
                    color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 14.sp, color = Color(0xFF888899))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.End)
    }
}
