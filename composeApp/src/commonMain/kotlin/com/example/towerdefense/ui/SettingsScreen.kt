package com.example.towerdefense.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.towerdefense.game.Difficulty

@Composable
fun SettingsScreen(
    currentDifficulty: Difficulty,
    onDifficultyChange: (Difficulty) -> Unit,
    onBack: () -> Unit,
) {
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                "EINSTELLUNGEN",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF95a5a6),
                letterSpacing = 4.sp,
            )

            Spacer(Modifier.height(8.dp))

            // ── Schwierigkeitsgrad ─────────────────────────────────────────
            Text(
                "SCHWIERIGKEITSGRAD",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7f8c8d),
                letterSpacing = 2.sp,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Difficulty.entries.forEach { diff ->
                    val selected = diff == currentDifficulty
                    val accentColor = when (diff) {
                        Difficulty.EASY   -> Color(0xFF2ecc71)
                        Difficulty.NORMAL -> Color(0xFF3498db)
                        Difficulty.HARD   -> Color(0xFFe74c3c)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) accentColor else Color(0xFF444466),
                                shape = RoundedCornerShape(10.dp),
                            )
                            .background(
                                color = if (selected) accentColor.copy(alpha = 0.2f) else Color(0xFF1e1e3a),
                                shape = RoundedCornerShape(10.dp),
                            )
                            .pointerInput(diff) { detectTapGestures { onDifficultyChange(diff) } }
                            .padding(vertical = 14.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                diff.label,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) accentColor else Color(0xFF888899),
                            )
                            Spacer(Modifier.height(6.dp))
                            Text("Gold: ${diff.startGold}",   fontSize = 11.sp, color = Color(0xFFf1c40f))
                            Text("Leben: ${diff.startLives}", fontSize = 11.sp, color = Color(0xFFe74c3c))
                            Text(
                                "Feind-HP: ×${diff.enemyHealthMult}",
                                fontSize = 11.sp,
                                color = Color(0xFF95a5a6),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onBack,
                modifier = Modifier.width(200.dp).height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ecc71).copy(alpha = 0.15f)),
            ) {
                Text("← Zurück", color = Color(0xFF2ecc71), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
