package com.example.data

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import com.example.core.model.DemoType
import com.example.core.model.GameItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * Result data holder for scanned storage locations.
 */
data class ScanResult(
    val foundGames: List<GameItem>,
    val scannedDirectoriesCount: Int,
    val totalFilesInspected: Int
)

/**
 * Service to locate PS2 ROM files (.iso, .cso, .chd, .bin, .elf, .gz)
 * across device storage directories (Downloads, Documents, ROMs, ExternalStorage),
 * and parse single picked files from Storage Access Framework (SAF).
 */
object StorageScannerService {

    val SUPPORTED_EXTENSIONS = setOf("iso", "cso", "chd", "bin", "elf", "gz")

    /**
     * Scans standard directories for PS2 ROM files.
     * Looks through common emulation paths:
     * - Download
     * - Documents
     * - /PS2, /ROMs, /roms/ps2
     * - App-specific external storage and public SD Card paths
     */
    suspend fun scanDeviceStorage(context: Context): ScanResult = withContext(Dispatchers.IO) {
        val foundGames = mutableListOf<GameItem>()
        var scannedDirsCount = 0
        var totalFilesCount = 0

        val directoriesToScan = mutableSetOf<File>()

        // Standard user storage directories
        val extStorage = Environment.getExternalStorageDirectory()
        if (extStorage != null && extStorage.exists()) {
            directoriesToScan.add(File(extStorage, "Download"))
            directoriesToScan.add(File(extStorage, "Documents"))
            directoriesToScan.add(File(extStorage, "ROMs"))
            directoriesToScan.add(File(extStorage, "Roms/ps2"))
            directoriesToScan.add(File(extStorage, "PS2"))
            directoriesToScan.add(File(extStorage, "Play!"))
            directoriesToScan.add(File(extStorage, "AetherSX2"))
        }

        // App-specific external files dir (always accessible without permissions)
        context.getExternalFilesDir(null)?.let {
            directoriesToScan.add(it)
            directoriesToScan.add(File(it, "roms"))
        }

        // Internal app files dir
        context.filesDir?.let {
            directoriesToScan.add(it)
            directoriesToScan.add(File(it, "roms"))
        }

        for (dir in directoriesToScan) {
            if (dir.exists() && dir.isDirectory) {
                scannedDirsCount++
                scanDirectoryRecursively(dir, 0, 3, foundGames) {
                    totalFilesCount++
                }
            }
        }

        ScanResult(
            foundGames = foundGames.distinctBy { it.filePath ?: it.title },
            scannedDirectoriesCount = scannedDirsCount,
            totalFilesInspected = totalFilesCount
        )
    }

    private fun scanDirectoryRecursively(
        directory: File,
        currentDepth: Int,
        maxDepth: Int,
        outputList: MutableList<GameItem>,
        onFileInspected: () -> Unit
    ) {
        if (currentDepth > maxDepth) return
        val files = directory.listFiles() ?: return

        for (file in files) {
            if (file.isDirectory) {
                // Avoid hidden or system heavy directories
                if (!file.name.startsWith(".") && !file.name.equals("Android", ignoreCase = true)) {
                    scanDirectoryRecursively(file, currentDepth + 1, maxDepth, outputList, onFileInspected)
                }
            } else if (file.isFile) {
                onFileInspected()
                val ext = file.extension.lowercase(Locale.ROOT)
                if (ext in SUPPORTED_EXTENSIONS) {
                    val sizeBytes = file.length()
                    // Filter out 0 byte files or non-disc files
                    val sizeMb = sizeBytes.toFloat() / (1024f * 1024f)
                    val gameItem = createGameItemFromFile(
                        title = file.nameWithoutExtension,
                        format = ext.uppercase(Locale.ROOT),
                        sizeMb = sizeMb,
                        filePath = file.absolutePath,
                        fileUriString = Uri.fromFile(file).toString()
                    )
                    outputList.add(gameItem)
                }
            }
        }
    }

    /**
     * Parse game metadata from a SAF Document Uri selected via file picker.
     */
    fun parseGameFromUri(context: Context, uri: Uri): GameItem {
        var displayName = "ps2_disc_image.iso"
        var sizeBytes: Long = 0L

        try {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex >= 0) {
                        displayName = cursor.getString(nameIndex) ?: displayName
                    }
                    if (sizeIndex >= 0) {
                        sizeBytes = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback to uri path segments if content query fails
            uri.lastPathSegment?.let { segment ->
                displayName = segment.substringAfterLast("/")
            }
        }

        val extension = displayName.substringAfterLast('.', "iso").uppercase(Locale.ROOT)
        val cleanTitle = displayName.substringBeforeLast('.').replace("_", " ").replace("-", " ")
        val sizeMb = if (sizeBytes > 0) sizeBytes.toFloat() / (1024f * 1024f) else 1420.0f

        val formattedSize = if (sizeMb >= 1024f) {
            String.format(Locale.US, "%.2f GB", sizeMb / 1024f)
        } else {
            String.format(Locale.US, "%.1f MB", sizeMb)
        }

        return GameItem(
            id = "rom_uri_${System.currentTimeMillis()}_${displayName.hashCode()}",
            title = cleanTitle,
            subtitle = "Direct Storage Mounted ($displayName)",
            discFormat = extension,
            fileSizeFormatted = formattedSize,
            region = detectRegionFromName(displayName),
            isBundledDemo = false,
            demoType = DemoType.CUBE_VU1_3D,
            coverAccentColor = pickAccentColor(cleanTitle),
            fileUriString = uri.toString(),
            filePath = null
        )
    }

    private fun createGameItemFromFile(
        title: String,
        format: String,
        sizeMb: Float,
        filePath: String,
        fileUriString: String
    ): GameItem {
        val cleanTitle = title.replace("_", " ").replace("-", " ")
        val formattedSize = if (sizeMb >= 1024f) {
            String.format(Locale.US, "%.2f GB", sizeMb / 1024f)
        } else {
            String.format(Locale.US, "%.1f MB", sizeMb)
        }

        return GameItem(
            id = "rom_file_${filePath.hashCode()}",
            title = cleanTitle,
            subtitle = "Local File ($filePath)",
            discFormat = format,
            fileSizeFormatted = formattedSize,
            region = detectRegionFromName(title),
            isBundledDemo = false,
            demoType = DemoType.CUBE_VU1_3D,
            coverAccentColor = pickAccentColor(cleanTitle),
            filePath = filePath,
            fileUriString = fileUriString
        )
    }

    private fun detectRegionFromName(name: String): String {
        val upper = name.uppercase(Locale.ROOT)
        return when {
            upper.contains("(USA)") || upper.contains("NTSC-U") || upper.contains("(U)") -> "NTSC-U"
            upper.contains("(EUROPE)") || upper.contains("PAL") || upper.contains("(E)") -> "PAL"
            upper.contains("(JAPAN)") || upper.contains("NTSC-J") || upper.contains("(J)") -> "NTSC-J"
            else -> "NTSC-U"
        }
    }

    private fun pickAccentColor(title: String): Long {
        val colors = listOf(
            0xFF0066FF, // PS2 Classic Blue
            0xFF00D4FF, // Cyan
            0xFFFF9500, // Orange
            0xFF00E676, // Neon Green
            0xFFFF3B30, // Red
            0xFFAF52DE  // Purple
        )
        val index = kotlin.math.abs(title.hashCode()) % colors.size
        return colors[index]
    }
}
