package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.emulator.PS2EmulatorEngine
import com.example.core.model.FrameSkipMode
import com.example.ui.theme.*

@Composable
fun GamePauseDialog(
    engine: PS2EmulatorEngine,
    onResume: () -> Unit,
    onExit: () -> Unit
) {
    val config by engine.config.collectAsState()
    val saveStates by engine.saveStates.collectAsState()
    var selectedSlot by remember { mutableIntStateOf(1) }
    var actionFeedback by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onResume) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, PS2SurfaceBorder, RoundedCornerShape(16.dp))
                .testTag("game_pause_dialog"),
            color = PS2SurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EMULATION PAUSED",
                        color = PS2TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PS2BadgeBg)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "32-BIT MODE",
                            color = PS2NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = PS2SurfaceBorder)

                // Save / Load Slots
                Text(
                    text = "Save State Slots (1 - 5):",
                    color = PS2TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (1..5).forEach { slot ->
                        val hasState = saveStates.containsKey(slot)
                        val isSelected = selectedSlot == slot
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isSelected -> PS2ElectricBlue
                                        hasState -> PS2BadgeBg
                                        else -> PS2SurfaceElevated
                                    }
                                )
                                .clickable { selectedSlot = slot }
                                .testTag("slot_button_$slot"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "S$slot",
                                color = if (isSelected) Color.White else PS2TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Save / Load Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            engine.quickSave(selectedSlot)
                            actionFeedback = "Saved state to Slot $selectedSlot"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_save_state"),
                        colors = ButtonDefaults.buttonColors(containerColor = PS2SurfaceElevated)
                    ) {
                        Text("Save Slot $selectedSlot", fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            if (engine.quickLoad(selectedSlot)) {
                                actionFeedback = "Loaded state from Slot $selectedSlot"
                            } else {
                                actionFeedback = "No save found in Slot $selectedSlot"
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_load_state"),
                        colors = ButtonDefaults.buttonColors(containerColor = PS2SurfaceElevated)
                    ) {
                        Text("Load Slot $selectedSlot", fontSize = 12.sp)
                    }
                }

                actionFeedback?.let { msg ->
                    Text(
                        text = msg,
                        color = PS2NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                HorizontalDivider(color = PS2SurfaceBorder)

                // Quick 32-Bit Performance Tuning
                Text(
                    text = "Quick 32-Bit Resolution:",
                    color = PS2TextSecondary,
                    fontSize = 12.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0.5f to "0.5x (Fastest)", 0.75f to "0.75x (Balanced)", 1.0f to "1.0x (Native)").forEach { (scale, label) ->
                        val isSelected = config.resolutionScale == scale
                        FilterChip(
                            selected = isSelected,
                            onClick = { engine.updateConfig(config.copy(resolutionScale = scale)) },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PS2ElectricBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Fast-Forward Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Fast-Forward Speed:", color = PS2TextSecondary, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1 to "1x", 2 to "2x", 4 to "4x").forEach { (speed, label) ->
                            val isSel = config.fastForwardSpeed == speed
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) PS2ElectricBlue else PS2SurfaceElevated)
                                    .clickable { engine.updateConfig(config.copy(fastForwardSpeed = speed)) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, color = if (isSel) Color.White else PS2TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }

                HorizontalDivider(color = PS2SurfaceBorder)

                // Bottom Buttons: Resume & Exit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onExit,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_pause_exit"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PS2CircleRed)
                    ) {
                        Text("Exit Game", fontSize = 13.sp)
                    }
                    Button(
                        onClick = onResume,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_pause_resume"),
                        colors = ButtonDefaults.buttonColors(containerColor = PS2ElectricBlue)
                    ) {
                        Text("Resume", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
