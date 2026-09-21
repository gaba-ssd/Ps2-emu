package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PS2ElectricBlue
import com.example.ui.theme.PS2SurfaceElevated
import com.example.ui.theme.PS2TextPrimary
import com.example.ui.theme.PS2TextSecondary
import com.example.ui.theme.PS2TextTertiary

@Composable
fun RomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rom_search_bar_container"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    "Search games by title, format (ISO, ELF), or region...",
                    color = PS2TextTertiary,
                    fontSize = 12.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = PS2ElectricBlue
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = PS2TextSecondary
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PS2SurfaceElevated,
                unfocusedContainerColor = PS2SurfaceElevated,
                focusedBorderColor = PS2ElectricBlue,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = PS2TextPrimary,
                unfocusedTextColor = PS2TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_search_roms")
        )

        // Filter chips: ALL, ISO, CSO, CHD, ELF, BUNDLED
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val filters = listOf("ALL", "ISO", "CSO", "CHD", "ELF", "BUNDLED")
            filters.forEach { filterTag ->
                val isSelected = selectedFilter.equals(filterTag, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = { onFilterSelect(filterTag) },
                    label = {
                        Text(
                            text = filterTag,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PS2ElectricBlue,
                        selectedLabelColor = Color.White,
                        containerColor = PS2SurfaceElevated,
                        labelColor = PS2TextSecondary
                    ),
                    modifier = Modifier.testTag("chip_filter_$filterTag")
                )
            }
        }
    }
}
