package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.emulator.PS2EmulatorEngine
import com.example.core.model.GameItem
import com.example.ui.components.DualShockController
import com.example.ui.components.GamePauseDialog
import com.example.ui.components.PS2ScreenCanvas
import com.example.ui.components.PerformanceHud
import com.example.ui.theme.*

@Composable
fun EmulatorScreen(
    engine: PS2EmulatorEngine,
    game: GameItem,
    onBackToLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by engine.stats.collectAsState()
    val config by engine.config.collectAsState()
    val isPaused by engine.isPaused.collectAsState()

    var showPauseMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("emulator_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PS2SurfaceDark)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = {
                            engine.pauseEmulation()
                            showPauseMenu = true
                        },
                        modifier = Modifier.testTag("btn_emulator_pause")
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause", tint = PS2NeonCyan)
                    }

                    Column {
                        Text(
                            text = game.title,
                            color = PS2TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${game.discFormat} | ${game.region} | Play! HLE Bios",
                            color = PS2TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                // Fast Forward & Stats HUD
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = {
                            val nextSpeed = if (config.fastForwardSpeed == 1) 2 else if (config.fastForwardSpeed == 2) 4 else 1
                            engine.updateConfig(config.copy(fastForwardSpeed = nextSpeed))
                        },
                        modifier = Modifier.testTag("btn_fast_forward")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.FastForward,
                                contentDescription = "Fast Forward",
                                tint = if (config.fastForwardSpeed > 1) PS2WarningOrange else PS2TextSecondary
                            )
                            if (config.fastForwardSpeed > 1) {
                                Text(
                                    text = "${config.fastForwardSpeed}x",
                                    color = PS2WarningOrange,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            engine.stopEmulation()
                            onBackToLibrary()
                        },
                        modifier = Modifier.testTag("btn_emulator_exit")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit", tint = PS2TextSecondary)
                    }
                }
            }

            // Optional HUD
            if (config.showPerformanceHud) {
                PerformanceHud(
                    stats = stats,
                    resolutionScale = config.resolutionScale,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // PS2 Display Canvas (4:3 aspect ratio or 16:9 widescreen)
            val aspect = if (config.enableWidescreenPatch) (16f / 9f) else (4f / 3f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspect)
                        .background(Color.Black)
                ) {
                    PS2ScreenCanvas(
                        engine = engine,
                        game = game,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // On-screen DualShock 2 Touch Controller
            DualShockController(
                engine = engine,
                opacity = config.controllerOpacity,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Pause Menu Dialog
        if (showPauseMenu) {
            GamePauseDialog(
                engine = engine,
                onResume = {
                    showPauseMenu = false
                    engine.resumeEmulation()
                },
                onExit = {
                    showPauseMenu = false
                    engine.stopEmulation()
                    onBackToLibrary()
                }
            )
        }
    }
}
