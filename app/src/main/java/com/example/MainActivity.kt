package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.core.emulator.PS2EmulatorEngine
import com.example.core.model.EmulatorConfig
import com.example.core.model.GameItem
import com.example.core.profiler.ArchitectureProfiler
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.EmulatorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PS2DeepNavy

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val archInfo = ArchitectureProfiler.detect(this)
    val initialConfig = EmulatorConfig(
      resolutionScale = if (archInfo.is32Bit) 0.5f else 0.75f,
      use16BitTextures = true,
      enableMultithreadedVU1 = true
    )
    val engine = PS2EmulatorEngine(this, initialConfig)

    setContent {
      MyApplicationTheme {
        val navController = rememberNavController()
        var activeGame by remember { mutableStateOf<GameItem?>(null) }

        NavHost(
          navController = navController,
          startDestination = "home",
          modifier = Modifier
            .fillMaxSize()
            .background(PS2DeepNavy)
        ) {
          composable("home") {
            HomeScreen(
              engine = engine,
              archInfo = archInfo,
              onLaunchGame = { game ->
                activeGame = game
                engine.startEmulation(game)
                navController.navigate("emulator")
              },
              onOpenSettings = { navController.navigate("settings") },
              onOpenDiagnostics = { navController.navigate("diagnostics") }
            )
          }

          composable("emulator") {
            activeGame?.let { game ->
              EmulatorScreen(
                engine = engine,
                game = game,
                onBackToLibrary = {
                  navController.popBackStack()
                }
              )
            }
          }

          composable("settings") {
            SettingsScreen(
              engine = engine,
              onBack = { navController.popBackStack() }
            )
          }

          composable("diagnostics") {
            DiagnosticsScreen(
              archInfo = archInfo,
              onBack = { navController.popBackStack() }
            )
          }
        }
      }
    }
  }
}

