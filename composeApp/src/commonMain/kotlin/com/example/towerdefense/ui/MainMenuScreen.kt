package com.example.towerdefense.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val C_BG_TOP    = Color(0xFF0f0c29)
private val C_BG_BOT    = Color(0xFF302b63)
private val C_ACCENT    = Color(0xFF2ecc71)
private val C_ACCENT2   = Color(0xFF3498db)
private val C_SUBTITLE  = Color(0xFFaaaacc)

@Composable
fun MainMenuScreen(
    onNewGame: () -> Unit,
    onHighscores: () -> Unit,
    onSettings: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(C_BG_TOP, C_BG_BOT))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "TOWER",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = C_ACCENT,
                    letterSpacing = 8.sp,
                )
                Text(
                    "DEFENSE",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                    color = C_ACCENT2,
                    letterSpacing = 12.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Verteidige deine Basis!",
                    fontSize = 14.sp,
                    color = C_SUBTITLE,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(16.dp))

            // Buttons
            MenuButton("Neues Spiel",  C_ACCENT,           onClick = onNewGame)
            MenuButton("Highscores",   Color(0xFFf1c40f),  onClick = onHighscores)
            MenuButton("Einstellungen",Color(0xFF95a5a6),  onClick = onSettings)
        }
    }
}

@Composable
private fun MenuButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(240.dp)
            .height(52.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.15f)),
    ) {
        Text(
            label,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}
