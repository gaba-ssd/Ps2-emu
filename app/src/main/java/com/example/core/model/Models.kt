package com.example.core.model

/**
 * Hardware architecture profile detected from the Android device.
 */
data class ArchitectureInfo(
    val primaryAbi: String,
    val supportedAbis: List<String>,
    val is32Bit: Boolean,
    val hasNeon: Boolean,
    val totalMemoryMb: Long,
    val maxHeapMemoryMb: Long,
    val cpuCores: Int,
    val processorName: String,
    val recommendedPreset: PerformancePreset
)

/**
 * Pre-tuned configuration presets for different hardware tiers.
 */
enum class PerformancePreset(val title: String, val description: String) {
    LOW_END_32BIT(
        "Low-End 32-Bit (Older Devices)",
        "0.5x Native Res, Frame Skip 1, 16-bit textures, Threaded VU1. Max stability for 1-2GB RAM SoCs."
    ),
    BALANCED_32BIT(
        "Balanced 32-Bit",
        "0.75x Res, Auto Frame Skip, 16-bit depth, Threaded VU1. Good visuals and steady 55-60 FPS."
    ),
    MAX_PERFORMANCE(
        "Native 1.0x (High-Spec)",
        "1.0x Native 640x448, No Frame Skip, 32-bit textures. Requires fast multi-core ARM SoC."
    )
}

/**
 * Granular emulator configuration for 32-bit execution and performance tuning.
 */
data class EmulatorConfig(
    // Graphics Synthesizer (GS)
    val resolutionScale: Float = 0.5f, // 0.5f (320x224), 0.75f (480x336), 1.0f (640x448)
    val frameSkipMode: FrameSkipMode = FrameSkipMode.FIXED_1,
    val use16BitTextures: Boolean = true,
    val enableWidescreenPatch: Boolean = true,
    val bilinearFiltering: Boolean = false, // disabled for 32-bit speed

    // Emotion Engine (EE) & Vector Units (VU)
    val enableMultithreadedVU1: Boolean = true,
    val jitCompilerMode: JitMode = JitMode.ARMV7_NEON_JIT,
    val fastMemoryAccess: Boolean = true, // unsafe direct pointer addressing
    val eeCycleRatePercent: Int = 100, // 50% to 130%

    // Audio (SPU2)
    val lowLatencyAudio: Boolean = true,
    val audioBufferSizeSamples: Int = 1024,
    val audioEnabled: Boolean = true,

    // System & BIOS
    val useHleBios: Boolean = true,
    val customBiosName: String = "Built-in Play! HLE BIOS v0.60",
    val consoleRegion: String = "NTSC-U",

    // Touch Controller & UI
    val controllerOpacity: Float = 0.65f,
    val controllerScale: Float = 1.0f,
    val hapticFeedbackEnabled: Boolean = true,
    val showPerformanceHud: Boolean = true,
    val fastForwardSpeed: Int = 2 // 2x or 4x
)

enum class FrameSkipMode(val label: String, val skipCount: Int) {
    OFF("Off (0 frames)", 0),
    AUTO("Auto (Adaptive)", -1),
    FIXED_1("Fixed 1 Frame (Every 2nd)", 1),
    FIXED_2("Fixed 2 Frames (Every 3rd)", 2),
    FIXED_3("Fixed 3 Frames (Older SoCs)", 3)
}

enum class JitMode(val label: String, val description: String) {
    ARMV7_NEON_JIT("32-bit ARMv7 NEON JIT", "Hardware-accelerated SIMD with dynamic recompiler"),
    INTERPRETER("Safe Interpreter (Slow)", "Pure instruction-by-instruction execution, zero JIT cache overhead")
}

/**
 * Represents a playable game or interactive homebrew demonstration.
 */
data class GameItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val discFormat: String, // ISO, CSO, CHD, ELF, BIN
    val fileSizeFormatted: String,
    val region: String,
    val isBundledDemo: Boolean = false,
    val demoType: DemoType? = null,
    val coverAccentColor: Long = 0xFF0066FF,
    val lastPlayedTimestamp: Long = 0L,
    val playtimeMinutes: Int = 0,
    val filePath: String? = null,
    val fileUriString: String? = null
)

enum class DemoType {
    CUBE_VU1_3D,
    DUALSHOCK_CALIBRATION,
    EE_32BIT_BENCHMARK,
    RETRO_RACER_3D
}

/**
 * Save state slot record.
 */
data class SaveState(
    val slot: Int,
    val gameId: String,
    val timestamp: Long,
    val stateSizeKb: Int,
    val screenshotNote: String
)

/**
 * Real-time performance telemetry.
 */
data class EmulationStats(
    val fps: Float = 60.0f,
    val targetFps: Float = 60.0f,
    val eeLoadPercent: Int = 42,
    val vuLoadPercent: Int = 36,
    val gsLoadPercent: Int = 28,
    val memoryUsedMb: Float = 38.4f,
    val maxMemoryMb: Long = 192,
    val framesSkippedCount: Long = 0,
    val frameTimeMs: Float = 16.6f,
    val isThrottling: Boolean = false,
    val activeAbi: String = "armeabi-v7a"
)
