package com.example.core.profiler

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.example.core.model.ArchitectureInfo
import com.example.core.model.PerformancePreset
import java.io.File

object ArchitectureProfiler {

    fun detect(context: Context): ArchitectureInfo {
        val supportedAbis = Build.SUPPORTED_ABIS.toList()
        val primaryAbi = if (supportedAbis.isNotEmpty()) supportedAbis[0] else "armeabi-v7a"

        val is32Bit = primaryAbi.contains("v7a") || primaryAbi.contains("armeabi") || primaryAbi == "x86" ||
                !supportedAbis.any { it.contains("64") }

        // Detect NEON SIMD support on ARM
        val hasNeon = checkNeonSupport(primaryAbi)

        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memoryInfo)

        val totalMemoryMb = memoryInfo.totalMem / (1024 * 1024)
        val maxHeapMemoryMb = Runtime.getRuntime().maxMemory() / (1024 * 1024)
        val cpuCores = Runtime.getRuntime().availableProcessors()

        val processorName = Build.HARDWARE.ifBlank { Build.BOARD }

        // Decide recommended preset based on hardware
        val recommendedPreset = when {
            is32Bit || totalMemoryMb <= 2048 || cpuCores <= 4 -> PerformancePreset.LOW_END_32BIT
            totalMemoryMb <= 4096 -> PerformancePreset.BALANCED_32BIT
            else -> PerformancePreset.MAX_PERFORMANCE
        }

        return ArchitectureInfo(
            primaryAbi = primaryAbi,
            supportedAbis = supportedAbis,
            is32Bit = is32Bit,
            hasNeon = hasNeon,
            totalMemoryMb = totalMemoryMb,
            maxHeapMemoryMb = maxHeapMemoryMb,
            cpuCores = cpuCores,
            processorName = processorName,
            recommendedPreset = recommendedPreset
        )
    }

    private fun checkNeonSupport(abi: String): Boolean {
        if (abi.contains("arm64")) return true
        if (abi.contains("v7a")) {
            // Check /proc/cpuinfo for neon / asimd feature flag
            try {
                val cpuInfo = File("/proc/cpuinfo")
                if (cpuInfo.exists()) {
                    val content = cpuInfo.readText()
                    return content.contains("neon", ignoreCase = true) ||
                            content.contains("asimd", ignoreCase = true)
                }
            } catch (_: Exception) {
                // Default to true for armv7a on modern Android
                return true
            }
        }
        return false
    }
}
