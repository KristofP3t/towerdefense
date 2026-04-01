package com.example.towerdefense.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.towerdefense.game.HighscoreEntry

private val RANK_COLORS = listOf(
    Color(0xFFf1c40f),  // 1st – gold
    Color(0xFFbdc3c7),  // 2nd – silver
    Color(0xFFe67e22),  // 3rd – bronze
)

@Composable
fun HighscoreScreen(
    entries: List<HighscoreEntry>,
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
        ) {
            Text(
                "HIGHSCORES",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFf1c40f),
                letterSpacing = 4.sp,
            )

            Spacer(Modifier.height(24.dp))

            if (entries.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        "Noch keine Einträge.\nSpiel zuerst eine Runde!",
                        color = Color(0xFF888899),
                        fontSize = 16.sp,
                        lineHeight = 26.sp,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(entries) { index, entry ->
                        HighscoreRow(rank = index + 1, entry = entry)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

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

@Composable
private fun HighscoreRow(rank: Int, entry: HighscoreEntry) {
    val rankColor = RANK_COLORS.getOrElse(rank - 1) { Color(0xFFaaaacc) }
    val resultColor = if (entry.victory) Color(0xFF2ecc71) else Color(0xFFe74c3c)
    val resultLabel = if (entry.victory) "SIEG" else "Welle ${entry.wave}/${entry.totalWaves}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "#$rank",
            modifier = Modifier.width(36.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = rankColor,
        )
        Text(
            "${entry.score}",
            modifier = Modifier.weight(1f),
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
        )
        Text(
            resultLabel,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = resultColor,
        )
    }
}
