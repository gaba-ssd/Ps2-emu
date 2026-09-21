package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.emulator.PS2EmulatorEngine
import com.example.ui.theme.*
import kotlin.math.sqrt

@Composable
fun DualShockController(
    engine: PS2EmulatorEngine,
    opacity: Float = 0.7f,
    modifier: Modifier = Modifier
) {
    val analogOn by engine.analogModeEnabled.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(opacity)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("dualshock_controller_container"),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Shoulder Buttons (L1, L2, R1, R2)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShoulderButton(label = "L2", tag = "btn_l2") { isDown -> engine.handleButtonPress("L2", isDown) }
                ShoulderButton(label = "L1", tag = "btn_l1") { isDown -> engine.handleButtonPress("L1", isDown) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShoulderButton(label = "R1", tag = "btn_r1") { isDown -> engine.handleButtonPress("R1", isDown) }
                ShoulderButton(label = "R2", tag = "btn_r2") { isDown -> engine.handleButtonPress("R2", isDown) }
            }
        }

        // Main Controls Row (DPad - Sticks/Select/Start - Action Buttons)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: D-Pad
            DPadCross(engine = engine)

            // Center: Select, Analog LED, Start, and Dual Analog Sticks
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Select & Start & Analog
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PillButton(label = "SELECT", tag = "btn_select") { isDown ->
                        engine.handleButtonPress("SELECT", isDown)
                    }

                    // Analog mode button with red LED
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PS2SurfaceElevated)
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { engine.handleButtonPress("ANALOG", true) })
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("btn_analog_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (analogOn) Color(0xFFFF2D55) else Color(0xFF3A101A))
                            )
                            Text("ANALOG", color = PS2TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    PillButton(label = "START", tag = "btn_start") { isDown ->
                        engine.handleButtonPress("START", isDown)
                    }
                }

                // Dual Analog Sticks
                if (analogOn) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnalogStick(tag = "stick_left") { x, y -> engine.setLeftStick(x, y) }
                        AnalogStick(tag = "stick_right") { x, y -> engine.setRightStick(x, y) }
                    }
                }
            }

            // Right: Action Buttons (Triangle, Circle, Cross, Square)
            ActionButtonsCluster(engine = engine)
        }
    }
}

@Composable
fun ShoulderButton(
    label: String,
    tag: String,
    onStateChange: (Boolean) -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(58.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (pressed) PS2ElectricBlue else PS2SurfaceElevated)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        onStateChange(true)
                        tryAwaitRelease()
                        pressed = false
                        onStateChange(false)
                    }
                )
            }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (pressed) Color.White else PS2TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PillButton(
    label: String,
    tag: String,
    onStateChange: (Boolean) -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(52.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (pressed) PS2ElectricBlue else PS2SurfaceElevated)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        onStateChange(true)
                        tryAwaitRelease()
                        pressed = false
                        onStateChange(false)
                    }
                )
            }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (pressed) Color.White else PS2TextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DPadCross(
    engine: PS2EmulatorEngine,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(136.dp)
            .testTag("dpad_cluster"),
        contentAlignment = Alignment.Center
    ) {
        // UP
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            DPadButton("▲", "dpad_up") { engine.handleButtonPress("DPAD_UP", it) }
        }
        // DOWN
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            DPadButton("▼", "dpad_down") { engine.handleButtonPress("DPAD_DOWN", it) }
        }
        // LEFT
        Box(modifier = Modifier.align(Alignment.CenterStart)) {
            DPadButton("◀", "dpad_left") { engine.handleButtonPress("DPAD_LEFT", it) }
        }
        // RIGHT
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            DPadButton("▶", "dpad_right") { engine.handleButtonPress("DPAD_RIGHT", it) }
        }

        // Center hub
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PS2SurfaceDark)
        )
    }
}

@Composable
fun DPadButton(
    glyph: String,
    tag: String,
    onStateChange: (Boolean) -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (pressed) PS2ElectricBlue else PS2SurfaceElevated)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        onStateChange(true)
                        tryAwaitRelease()
                        pressed = false
                        onStateChange(false)
                    }
                )
            }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = glyph,
            color = if (pressed) Color.White else PS2TextSecondary,
            fontSize = 16.sp
        )
    }
}

@Composable
fun ActionButtonsCluster(
    engine: PS2EmulatorEngine,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(136.dp)
            .testTag("action_buttons_cluster"),
        contentAlignment = Alignment.Center
    ) {
        // Triangle (Top)
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            SymbolActionButton(
                symbol = "▲",
                symbolColor = PS2TriangleGreen,
                tag = "btn_triangle"
            ) { engine.handleButtonPress("TRIANGLE", it) }
        }
        // Circle (Right)
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            SymbolActionButton(
                symbol = "●",
                symbolColor = PS2CircleRed,
                tag = "btn_circle"
            ) { engine.handleButtonPress("CIRCLE", it) }
        }
        // Cross (Bottom)
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            SymbolActionButton(
                symbol = "✖",
                symbolColor = PS2CrossBlue,
                tag = "btn_cross"
            ) { engine.handleButtonPress("CROSS", it) }
        }
        // Square (Left)
        Box(modifier = Modifier.align(Alignment.CenterStart)) {
            SymbolActionButton(
                symbol = "■",
                symbolColor = PS2SquarePink,
                tag = "btn_square"
            ) { engine.handleButtonPress("SQUARE", it) }
        }
    }
}

@Composable
fun SymbolActionButton(
    symbol: String,
    symbolColor: Color,
    tag: String,
    onStateChange: (Boolean) -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(if (pressed) symbolColor.copy(alpha = 0.35f) else PS2SurfaceElevated)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        onStateChange(true)
                        tryAwaitRelease()
                        pressed = false
                        onStateChange(false)
                    }
                )
            }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = symbolColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AnalogStick(
    tag: String,
    onStickMove: (x: Float, y: Float) -> Unit
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }
    val maxRadiusPx = 36f

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(PS2SurfaceElevated)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        thumbOffset = Offset.Zero
                        onStickMove(0f, 0f)
                    },
                    onDragCancel = {
                        thumbOffset = Offset.Zero
                        onStickMove(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = thumbOffset + dragAmount
                        val distance = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
                        thumbOffset = if (distance > maxRadiusPx) {
                            Offset(
                                (newOffset.x / distance) * maxRadiusPx,
                                (newOffset.y / distance) * maxRadiusPx
                            )
                        } else {
                            newOffset
                        }
                        onStickMove(thumbOffset.x / maxRadiusPx, thumbOffset.y / maxRadiusPx)
                    }
                )
            }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        // Ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = PS2SurfaceBorder,
                radius = size.minDimension / 2f - 2f,
                style = Stroke(width = 2f)
            )
            // Center thumb
            drawCircle(
                color = PS2ElectricBlue,
                radius = 18f,
                center = center + thumbOffset
            )
        }
    }
}
