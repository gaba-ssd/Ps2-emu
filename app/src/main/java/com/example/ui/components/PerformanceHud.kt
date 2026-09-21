package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.EmulationStats
import com.example.ui.theme.*

@Composable
fun PerformanceHud(
    stats: EmulationStats,
    resolutionScale: Float,
    modifier: Modifier = Modifier
) {
    val fpsColor = when {
        stats.fps >= 55f -> PS2SuccessGreen
        stats.fps >= 42f -> PS2WarningOrange
        else -> PS2CircleRed
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(PS2DeepNavy.copy(alpha = 0.85f))
            .border(1.dp, PS2SurfaceBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("performance_hud")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // FPS Counter
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format("%.1f", stats.fps),
                    color = fpsColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = " FPS",
                    color = PS2TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // CPU Emotion Engine load
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "EE:", color = PS2TextTertiary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${stats.eeLoadPercent}%",
                    color = PS2NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Vector Unit load
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "VU:", color = PS2TextTertiary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${stats.vuLoadPercent}%",
                    color = PS2BrightGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // GS Internal Res
            val resLabel = when (resolutionScale) {
                0.5f -> "0.5x"
                0.75f -> "0.75x"
                else -> "1.0x"
            }
            Text(
                text = "GS:$resLabel",
                color = PS2TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            // 32-bit Memory usage
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "RAM:", color = PS2TextTertiary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = String.format("%.0fMB", stats.memoryUsedMb),
                    color = PS2TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // 32-Bit Architecture Tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(PS2BadgeBg)
                    .border(0.5.dp, PS2BadgeBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "32-BIT NEON",
                    color = PS2ElectricBlue,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
