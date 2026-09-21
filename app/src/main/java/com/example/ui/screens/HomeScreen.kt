package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.emulator.PS2EmulatorEngine
import com.example.core.model.ArchitectureInfo
import com.example.core.model.GameItem
import com.example.core.model.PerformancePreset
import com.example.data.EmulatorRepository
import com.example.data.StorageScannerService
import com.example.ui.components.RomSearchBar
import com.example.ui.components.StorageScannerBanner
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    engine: PS2EmulatorEngine,
    archInfo: ArchitectureInfo,
    onLaunchGame: (GameItem) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val games by EmulatorRepository.gameLibrary.collectAsState()
    val isScanning by EmulatorRepository.isScanning.collectAsState()
    val scanMessage by EmulatorRepository.scanMessage.collectAsState()
    val config by engine.config.collectAsState()
    
    var showAddRomDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    // SAF File Picker Launcher to manually pick ROM from local storage
    val pickRomLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val pickedGame = StorageScannerService.parseGameFromUri(context, uri)
                EmulatorRepository.addGameItem(pickedGame)
                Toast.makeText(context, "Loaded disc: ${pickedGame.title}", Toast.LENGTH_SHORT).show()
                onLaunchGame(pickedGame)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not mount selected file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Filter and search game items
    val filteredGames = remember(games, searchQuery, selectedFilter) {
        games.filter { game ->
            val matchesFilter = when (selectedFilter.uppercase()) {
                "ALL" -> true
                "BUNDLED" -> game.isBundledDemo
                else -> game.discFormat.equals(selectedFilter, ignoreCase = true)
            }
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                game.title.contains(searchQuery, ignoreCase = true) ||
                game.subtitle.contains(searchQuery, ignoreCase = true) ||
                game.discFormat.contains(searchQuery, ignoreCase = true) ||
                game.region.contains(searchQuery, ignoreCase = true)
            }
            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        modifier = modifier.testTag("home_screen"),
        containerColor = PS2DeepNavy,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(PS2ElectricBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("PS2", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("PS2 Emulator", color = PS2TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("ARM 32-Bit Edition", color = PS2NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenDiagnostics, modifier = Modifier.testTag("btn_diagnostics")) {
                        Icon(Icons.Default.Speed, contentDescription = "Diagnostics", tint = PS2NeonCyan)
                    }
                    IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("btn_settings")) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PS2TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PS2SurfaceDark)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    pickRomLauncher.launch(
                        arrayOf(
                            "application/octet-stream",
                            "application/x-iso9660-image",
                            "*/*"
                        )
                    )
                },
                containerColor = PS2ElectricBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_rom")
            ) {
                Icon(Icons.Default.FileOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pick PS2 Disc", fontWeight = FontWeight.SemiBold)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .testTag("hero_banner_card"),
                    colors = CardDefaults.cardColors(containerColor = PS2SurfaceDark)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_ps2_banner),
                            contentDescription = "PS2 Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f))
                                .padding(16.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Column {
                                Text(
                                    text = "32-Bit Low-Spec Optimized Engine",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Powered by Play! Core Architecture with ARMv7 NEON JIT & HLE BIOS",
                                    color = PS2NeonCyan,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Storage Scanner & Picker Service Banner
            item {
                StorageScannerBanner(
                    isScanning = isScanning,
                    scanMessage = scanMessage,
                    onStartScan = {
                        coroutineScope.launch {
                            EmulatorRepository.setScanning(true, "Scanning device storage for PS2 ROMs...")
                            val result = StorageScannerService.scanDeviceStorage(context)
                            val addedCount = EmulatorRepository.addScannedGames(result.foundGames)
                            val summary = if (addedCount > 0) {
                                "Found $addedCount new PS2 disc images in storage!"
                            } else if (result.foundGames.isNotEmpty()) {
                                "Storage scanned. ${result.foundGames.size} game(s) already in library."
                            } else {
                                "Scanned ${result.scannedDirectoriesCount} folders (${result.totalFilesInspected} files). No new ROMs found. Use 'Pick File' to select manually."
                            }
                            EmulatorRepository.setScanning(false, summary)
                            Toast.makeText(context, summary, Toast.LENGTH_LONG).show()
                        }
                    },
                    onPickRomFile = {
                        pickRomLauncher.launch(
                            arrayOf(
                                "application/octet-stream",
                                "application/x-iso9660-image",
                                "*/*"
                            )
                        )
                    },
                    onManualAddDialog = {
                        showAddRomDialog = true
                    }
                )
            }

            // Architecture Status & Profile Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PS2SurfaceBorder, RoundedCornerShape(12.dp))
                        .testTag("arch_status_card"),
                    colors = CardDefaults.cardColors(containerColor = PS2SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Memory, contentDescription = null, tint = PS2NeonCyan, modifier = Modifier.size(18.dp))
                                Text("Architecture Profile", color = PS2TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PS2BadgeBg)
                                    .border(1.dp, PS2BadgeBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (archInfo.hasNeon) "ARMv7 NEON 32-BIT" else "32-BIT SCALAR",
                                    color = PS2NeonCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total RAM", color = PS2TextTertiary, fontSize = 10.sp)
                                Text("${archInfo.totalMemoryMb} MB", color = PS2TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text("Heap Limit", color = PS2TextTertiary, fontSize = 10.sp)
                                Text("${archInfo.maxHeapMemoryMb} MB", color = PS2TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text("CPU Cores", color = PS2TextTertiary, fontSize = 10.sp)
                                Text("${archInfo.cpuCores} Cores", color = PS2TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text("HLE BIOS", color = PS2TextTertiary, fontSize = 10.sp)
                                Text("Built-in Active", color = PS2SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        HorizontalDivider(color = PS2SurfaceBorder)

                        // One-tap Performance Presets
                        Text("Active Optimization Preset:", color = PS2TextTertiary, fontSize = 11.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PerformancePreset.values().forEach { preset ->
                                val isSelected = when (preset) {
                                    PerformancePreset.LOW_END_32BIT -> config.resolutionScale == 0.5f
                                    PerformancePreset.BALANCED_32BIT -> config.resolutionScale == 0.75f
                                    PerformancePreset.MAX_PERFORMANCE -> config.resolutionScale == 1.0f
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) PS2ElectricBlue else PS2SurfaceElevated)
                                        .clickable { engine.applyPreset(preset) }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (preset) {
                                             PerformancePreset.LOW_END_32BIT -> "Low-End 32B"
                                            PerformancePreset.BALANCED_32BIT -> "Balanced"
                                            PerformancePreset.MAX_PERFORMANCE -> "Native 1.0x"
                                        },
                                        color = if (isSelected) Color.White else PS2TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search and Filter Bar
            item {
                RomSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    selectedFilter = selectedFilter,
                    onFilterSelect = { selectedFilter = it }
                )
            }

            // Game Library Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GAMES & 32-BIT HOMEBREW",
                        color = PS2TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${filteredGames.size} of ${games.size} Titles",
                        color = PS2TextTertiary,
                        fontSize = 11.sp
                    )
                }
            }

            // Empty search state
            if (filteredGames.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("empty_games_state"),
                        colors = CardDefaults.cardColors(containerColor = PS2SurfaceDark)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = PS2TextTertiary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "No matching PS2 games found",
                                color = PS2TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Try adjusting your search query, or use 'Pick File' to locate a ROM on your device.",
                                color = PS2TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Game Items List
            items(filteredGames, key = { it.id }) { game ->
                GameCard(
                    game = game,
                    onPlay = { onLaunchGame(game) }
                )
            }
        }
    }

    if (showAddRomDialog) {
        AddRomModalDialog(
            onDismiss = { showAddRomDialog = false },
            onAdd = { name, format, size ->
                val newGame = EmulatorRepository.addCustomRom(name, format, size)
                showAddRomDialog = false
                onLaunchGame(newGame)
            }
        )
    }
}

@Composable
fun GameCard(
    game: GameItem,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, PS2SurfaceBorder, RoundedCornerShape(12.dp))
            .clickable { onPlay() }
            .testTag("game_card_${game.id}"),
        colors = CardDefaults.cardColors(containerColor = PS2SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Disc / Cover Placeholder
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(game.coverAccentColor).copy(alpha = 0.25f))
                    .border(1.dp, Color(game.coverAccentColor).copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (game.isBundledDemo) Icons.Default.SportsEsports else Icons.Default.Album,
                    contentDescription = null,
                    tint = Color(game.coverAccentColor),
                    modifier = Modifier.size(28.dp)
                )
            }

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.title,
                    color = PS2TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = game.subtitle,
                    color = PS2TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PS2SurfaceElevated)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(game.discFormat, color = PS2NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(game.region, color = PS2TextTertiary, fontSize = 10.sp)
                    Text(game.fileSizeFormatted, color = PS2TextTertiary, fontSize = 10.sp)
                }
            }

            // Play Button
            Button(
                onClick = onPlay,
                colors = ButtonDefaults.buttonColors(containerColor = PS2ElectricBlue),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("btn_play_${game.id}")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("BOOT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AddRomModalDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, format: String, sizeMb: Float) -> Unit
) {
    var romName by remember { mutableStateOf("Gran_Turismo_4.iso") }
    var selectedFormat by remember { mutableStateOf("ISO") }
    var sizeMb by remember { mutableFloatStateOf(1840f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Import PS2 Disc Image", color = PS2TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Select or enter disc filename to mount to the 32-bit HLE PS2 filesystem:",
                    color = PS2TextSecondary,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = romName,
                    onValueChange = { romName = it },
                    label = { Text("ROM / ISO File Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PS2TextPrimary,
                        unfocusedTextColor = PS2TextPrimary,
                        focusedBorderColor = PS2ElectricBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Format Type:", color = PS2TextSecondary, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ISO", "CSO", "CHD", "ELF").forEach { fmt ->
                        FilterChip(
                            selected = selectedFormat == fmt,
                            onClick = { selectedFormat = fmt },
                            label = { Text(fmt, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PS2ElectricBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Text(
                    "Simulated disc size: ${sizeMb.toInt()} MB. Compatible with ARMv7 NEON memory mapping.",
                    color = PS2TextTertiary,
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(romName, selectedFormat, sizeMb) },
                colors = ButtonDefaults.buttonColors(containerColor = PS2ElectricBlue)
            ) {
                Text("Mount & Boot", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = PS2TextSecondary)
            }
        },
        containerColor = PS2SurfaceDark
    )
}
