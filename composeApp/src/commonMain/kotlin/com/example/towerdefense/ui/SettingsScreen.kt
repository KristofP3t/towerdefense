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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(onBack: () -> Unit) {
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
                "EINSTELLUNGEN",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF95a5a6),
                letterSpacing = 4.sp,
            )

            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    "Noch keine Einstellungen verfügbar.",
                    color = Color(0xFF888899),
                    fontSize = 16.sp,
                )
            }

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
