package com.example.data

import com.example.core.model.DemoType
import com.example.core.model.GameItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object EmulatorRepository {

    private val bundledGames = listOf(
        GameItem(
            id = "demo_cube_vu1",
            title = "VU1 Vector Matrix & 3D Lit Polyhedra",
            subtitle = "PS2 Vector Unit 1 microcode & GS 3D rasterization test",
            discFormat = "ELF",
            fileSizeFormatted = "4.2 MB",
            region = "NTSC-U",
            isBundledDemo = true,
            demoType = DemoType.CUBE_VU1_3D,
            coverAccentColor = 0xFF0066FF,
            playtimeMinutes = 14
        ),
        GameItem(
            id = "demo_retro_racer",
            title = "Retro Sprint 3D PS2 Prototype",
            subtitle = "High-speed 60 FPS polygon racer with frame-skip & audio engine",
            discFormat = "ISO",
            fileSizeFormatted = "14.8 MB",
            region = "NTSC-U",
            isBundledDemo = true,
            demoType = DemoType.RETRO_RACER_3D,
            coverAccentColor = 0xFF00D4FF,
            playtimeMinutes = 28
        ),
        GameItem(
            id = "demo_dualshock_lab",
            title = "DualShock 2 Diagnostics & Haptic Lab",
            subtitle = "Interactive pad calibration, analog stick vectors & haptic motors",
            discFormat = "ELF",
            fileSizeFormatted = "1.8 MB",
            region = "NTSC-U",
            isBundledDemo = true,
            demoType = DemoType.DUALSHOCK_CALIBRATION,
            coverAccentColor = 0xFF00E676,
            playtimeMinutes = 8
        ),
        GameItem(
            id = "demo_ee_benchmark",
            title = "Emotion Engine 32-Bit Benchmark Suite",
            subtitle = "ARMv7 NEON SIMD vs Scalar throughput & memory bandwidth",
            discFormat = "ELF",
            fileSizeFormatted = "3.1 MB",
            region = "NTSC-U",
            isBundledDemo = true,
            demoType = DemoType.EE_32BIT_BENCHMARK,
            coverAccentColor = 0xFFFF9500,
            playtimeMinutes = 6
        )
    )

    private val _gameLibrary = MutableStateFlow<List<GameItem>>(bundledGames)
    val gameLibrary: StateFlow<List<GameItem>> = _gameLibrary.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanMessage = MutableStateFlow<String?>(null)
    val scanMessage: StateFlow<String?> = _scanMessage.asStateFlow()

    fun setScanning(scanning: Boolean, message: String? = null) {
        _isScanning.value = scanning
        _scanMessage.value = message
    }

    fun addGameItem(game: GameItem): GameItem {
        // Prevent duplicate games by title/path
        val current = _gameLibrary.value
        val exists = current.any {
            (it.filePath != null && it.filePath == game.filePath) ||
            (it.fileUriString != null && it.fileUriString == game.fileUriString) ||
            (it.title.equals(game.title, ignoreCase = true) && it.discFormat == game.discFormat)
        }
        if (!exists) {
            _gameLibrary.value = listOf(game) + current
        }
        return game
    }

    fun addScannedGames(games: List<GameItem>): Int {
        val current = _gameLibrary.value
        val newGames = games.filter { scanned ->
            !current.any { existing ->
                (existing.filePath != null && existing.filePath == scanned.filePath) ||
                (existing.fileUriString != null && existing.fileUriString == scanned.fileUriString) ||
                (existing.title.equals(scanned.title, ignoreCase = true) && existing.discFormat == scanned.discFormat)
            }
        }
        if (newGames.isNotEmpty()) {
            _gameLibrary.value = newGames + current
        }
        return newGames.size
    }

    fun addCustomRom(fileName: String, discFormat: String, sizeMb: Float): GameItem {
        val cleanTitle = fileName
            .replace(".iso", "", ignoreCase = true)
            .replace(".cso", "", ignoreCase = true)
            .replace(".chd", "", ignoreCase = true)
            .replace(".bin", "", ignoreCase = true)
            .replace(".elf", "", ignoreCase = true)
            .replace("_", " ")

        val newItem = GameItem(
            id = "rom_${System.currentTimeMillis()}",
            title = cleanTitle,
            subtitle = "User Imported PS2 Disc Image (HLE Bios Ready)",
            discFormat = discFormat.uppercase(),
            fileSizeFormatted = String.format("%.1f MB", sizeMb),
            region = "NTSC-U",
            isBundledDemo = false,
            demoType = DemoType.CUBE_VU1_3D, // Can run universal 3D test pipeline
            coverAccentColor = 0xFF5AC8FA
        )

        _gameLibrary.value = listOf(newItem) + _gameLibrary.value
        return newItem
    }

    fun removeGame(id: String) {
        _gameLibrary.value = _gameLibrary.value.filter { it.id != id }
    }
}
