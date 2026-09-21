package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun StorageScannerBanner(
    isScanning: Boolean,
    scanMessage: String?,
    onStartScan: () -> Unit,
    onPickRomFile: () -> Unit,
    onManualAddDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, PS2SurfaceBorder, RoundedCornerShape(12.dp))
            .testTag("storage_scanner_card"),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PS2ElectricBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = PS2NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Storage Disc Scanner & Picker",
                            color = PS2TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Locate .ISO, .CSO, .CHD, .ELF on device storage",
                            color = PS2TextTertiary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isScanning,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PS2SurfaceElevated)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = PS2NeonCyan,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = scanMessage ?: "Scanning device directories...",
                            color = PS2NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(3.dp),
                        color = PS2ElectricBlue,
                        trackColor = PS2DeepNavy
                    )
                }
            }

            if (!isScanning && scanMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(PS2SuccessGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PS2SuccessGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = scanMessage,
                        color = PS2SuccessGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Scan Device Storage Button
                Button(
                    onClick = onStartScan,
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(containerColor = PS2ElectricBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("btn_scan_storage"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scan Storage", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Pick ROM via SAF File Picker Button
                FilledTonalButton(
                    onClick = onPickRomFile,
                    enabled = !isScanning,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = PS2SurfaceElevated,
                        contentColor = PS2NeonCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_pick_rom_file"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileOpen,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pick File", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Manual mount dialog
                OutlinedButton(
                    onClick = onManualAddDialog,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PS2TextSecondary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(0.9f)
                        .testTag("btn_manual_mount"),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Manual", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
