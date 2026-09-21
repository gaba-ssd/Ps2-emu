package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.emulator.PS2EmulatorEngine
import com.example.core.model.FrameSkipMode
import com.example.core.model.JitMode
import com.example.core.model.PerformancePreset
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    engine: PS2EmulatorEngine,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val config by engine.config.collectAsState()

    Scaffold(
        modifier = modifier.testTag("settings_screen"),
        containerColor = PS2DeepNavy,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("32-Bit Performance Settings", color = PS2TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Hardware Optimization & Architecture Tuning", color = PS2NeonCyan, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_settings_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PS2TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PS2SurfaceDark)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Presets
            item {
                SettingsSectionHeader("PERFORMANCE PRESETS", Icons.Default.Speed)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PerformancePreset.values().forEach { preset ->
                        OutlinedButton(
                            onClick = { engine.applyPreset(preset) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(PS2SurfaceBorder)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = PS2SurfaceDark)
                        ) {
                            Text(
                                text = when (preset) {
                                    PerformancePreset.LOW_END_32BIT -> "Low 32-Bit"
                                    PerformancePreset.BALANCED_32BIT -> "Balanced"
                                    PerformancePreset.MAX_PERFORMANCE -> "Native"
                                },
                                color = PS2NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Graphics Synthesizer (GS)
            item {
                SettingsSectionHeader("GRAPHICS SYNTHESIZER (GS)", Icons.Default.Tv)
                Card(
                    colors = CardDefaults.cardColors(containerColor = PS2SurfaceDark),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(PS2SurfaceBorder)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Internal Resolution
                        Text("Internal Resolution Scaling:", color = PS2TextSecondary, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0.5f to "0.5x (320x224)", 0.75f to "0.75x (480x336)", 1.0f to "1.0x (640x448)").forEach { (scale, label) ->
                                val isSel = config.resolutionScale == scale
                                FilterChip(
                                    selected = isSel,
                                    onClick = { engine.updateConfig(config.copy(resolutionScale = scale)) },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PS2ElectricBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        HorizontalDivider(color = PS2SurfaceBorder)

                        // Frame Skip
                        Text("Frame Skipping (Crucial for Low-End Devices):", color = PS2TextSecondary, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FrameSkipMode.values().forEach { mode ->
                                val isSel = config.frameSkipMode == mode
                                FilterChip(
                                    selected = isSel,
                                    onClick = { engine.updateConfig(config.copy(frameSkipMode = mode)) },
                                    label = { Text(mode.label, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PS2ElectricBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        HorizontalDivider(color = PS2SurfaceBorder)

                        // 16-bit texture switch
                        SettingToggleRow(
                            title = "16-Bit Texture Format (RGB5A1)",
                            subtitle = "Reduces VRAM memory bus bandwidth by 50% on older GPUs (Mali-400 / Adreno)",
                            checked = config.use16BitTextures,
                            onCheckedChange = { engine.updateConfig(config.copy(use16BitTextures = it)) }
                        )

                        // Widescreen patch
                        SettingToggleRow(
                            title = "Widescreen 16:9 Aspect Ratio",
                            subtitle = "Adapts PS2 4:3 framebuffer to modern full displays",
                            checked = config.enableWidescreenPatch,
                            onCheckedChange = { engine.updateConfig(config.copy(enableWidescreenPatch = it)) }
                        )
                    }
                }
            }

            // Emotion Engine & VU
            item {
                SettingsSectionHeader("EMOTION ENGINE & VECTOR UNITS", Icons.Default.Memory)
                Card(
                    colors = CardDefaults.cardColors(containerColor = PS2SurfaceDark),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(PS2SurfaceBorder)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // JIT Mode
                        Text("CPU Recompiler Engine:", color = PS2TextSecondary, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            JitMode.values().forEach { mode ->
                                val isSel = config.jitCompilerMode == mode
                                FilterChip(
                                    selected = isSel,
                                    onClick = { engine.updateConfig(config.copy(jitCompilerMode = mode)) },
                                    label = { Text(mode.label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PS2ElectricBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        HorizontalDivider(color = PS2SurfaceBorder)

                        SettingToggleRow(
                            title = "Multi-Threaded VU1 Microcode",
                            subtitle = "Offload 3D geometry matrix transforms to secondary CPU core",
                            checked = config.enableMultithreadedVU1,
                            onCheckedChange = { engine.updateConfig(config.copy(enableMultithreadedVU1 = it)) }
                        )

                        SettingToggleRow(
                            title = "Fast Memory Access (Unsafe Pointer Mode)",
                            subtitle = "Bypasses virtual page translation overhead for older 32-bit Android kernels",
                            checked = config.fastMemoryAccess,
                            onCheckedChange = { engine.updateConfig(config.copy(fastMemoryAccess = it)) }
                        )
                    }
                }
            }

            // Audio & Controller
            item {
                SettingsSectionHeader("AUDIO & ON-SCREEN CONTROLS", Icons.Default.Gamepad)
                Card(
                    colors = CardDefaults.cardColors(containerColor = PS2SurfaceDark),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(PS2SurfaceBorder)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SettingToggleRow(
                            title = "Low Latency Audio Buffer (1024 Samples)",
                            subtitle = "Prevents SPU2 sound crackling and buffer underruns on budget SoCs",
                            checked = config.lowLatencyAudio,
                            onCheckedChange = { engine.updateConfig(config.copy(lowLatencyAudio = it)) }
                        )

                        SettingToggleRow(
                            title = "Vibration Haptic Feedback",
                            subtitle = "DualShock 2 rumble emulation on button presses",
                            checked = config.hapticFeedbackEnabled,
                            onCheckedChange = { engine.updateConfig(config.copy(hapticFeedbackEnabled = it)) }
                        )

                        SettingToggleRow(
                            title = "Real-Time Performance HUD",
                            subtitle = "Display live FPS, EE%, VU%, RAM, and 32-bit NEON badge during play",
                            checked = config.showPerformanceHud,
                            onCheckedChange = { engine.updateConfig(config.copy(showPerformanceHud = it)) }
                        )

                        // Controller Opacity Slider
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Touch Controls Opacity:", color = PS2TextSecondary, fontSize = 12.sp)
                                Text("${(config.controllerOpacity * 100).toInt()}%", color = PS2NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = config.controllerOpacity,
                                onValueChange = { engine.updateConfig(config.copy(controllerOpacity = it)) },
                                valueRange = 0.2f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = PS2ElectricBlue,
                                    activeTrackColor = PS2ElectricBlue
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = PS2NeonCyan, modifier = Modifier.size(16.dp))
        Text(title, color = PS2TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = PS2TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = PS2TextTertiary, fontSize = 10.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PS2ElectricBlue
            )
        )
    }
}
