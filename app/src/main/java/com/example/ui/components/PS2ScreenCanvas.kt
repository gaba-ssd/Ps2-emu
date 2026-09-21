package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.emulator.PS2EmulatorEngine
import com.example.core.model.DemoType
import com.example.core.model.GameItem
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun PS2ScreenCanvas(
    engine: PS2EmulatorEngine,
    game: GameItem,
    modifier: Modifier = Modifier
) {
    // Collect active input states to trigger recomposition / frame redraw
    val leftX by engine.leftStickX.collectAsState()
    val leftY by engine.leftStickY.collectAsState()
    val cross by engine.buttonCross.collectAsState()
    val square by engine.buttonSquare.collectAsState()
    val triangle by engine.buttonTriangle.collectAsState()
    val circle by engine.buttonCircle.collectAsState()
    val dpadUp by engine.dpadUp.collectAsState()
    val dpadDown by engine.dpadDown.collectAsState()
    val dpadLeft by engine.dpadLeft.collectAsState()
    val dpadRight by engine.dpadRight.collectAsState()
    val l1 by engine.buttonL1.collectAsState()
    val r1 by engine.buttonR1.collectAsState()
    val analogOn by engine.analogModeEnabled.collectAsState()

    // 60 FPS animation ticker
    var frameTick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                frameTick++
            }
        }
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .testTag("ps2_screen_canvas"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            when (game.demoType) {
                DemoType.CUBE_VU1_3D -> {
                    drawVu1VectorDemo(
                        rotX = engine.demoRotationX,
                        rotY = engine.demoRotationY,
                        shapeMode = engine.demoShapeIndex,
                        frame = frameTick
                    )
                }
                DemoType.RETRO_RACER_3D -> {
                    drawRetroRacer(
                        posX = engine.racerPositionX,
                        speed = engine.racerSpeed,
                        distance = engine.racerDistance,
                        lap = engine.racerLap,
                        frame = frameTick
                    )
                }
                DemoType.DUALSHOCK_CALIBRATION -> {
                    drawDualShockCalibration(
                        cross = cross, circle = circle, square = square, triangle = triangle,
                        dpadUp = dpadUp, dpadDown = dpadDown, dpadLeft = dpadLeft, dpadRight = dpadRight,
                        l1 = l1, r1 = r1, leftX = leftX, leftY = leftY, analogOn = analogOn
                    )
                }
                DemoType.EE_32BIT_BENCHMARK -> {
                    drawEeBenchmark(
                        progress = engine.benchmarkProgress,
                        scores = engine.benchmarkScores
                    )
                }
                null -> {
                    drawGenericPs2Output(game = game, rot = engine.demoRotationX, frame = frameTick)
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 1. VU1 3D LIT POLYHEDRON PIPELINE
// -------------------------------------------------------------------------------------------------
private fun DrawScope.drawVu1VectorDemo(
    rotX: Float,
    rotY: Float,
    shapeMode: Int,
    frame: Long
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val scale = min(size.width, size.height) * 0.32f

    // Draw background 32-bit GS grid
    val gridCount = 8
    for (i in -gridCount..gridCount) {
        val y = cy + (i * 24f)
        drawLine(
            color = PS2DeepNavy,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
    }

    // 3D Cube Vertices
    val radX = Math.toRadians(rotX.toDouble())
    val radY = Math.toRadians(rotY.toDouble())
    val cosX = cos(radX).toFloat()
    val sinX = sin(radX).toFloat()
    val cosY = cos(radY).toFloat()
    val sinY = sin(radY).toFloat()

    val rawVertices = listOf(
        floatArrayOf(-1f, -1f, -1f),
        floatArrayOf(1f, -1f, -1f),
        floatArrayOf(1f, 1f, -1f),
        floatArrayOf(-1f, 1f, -1f),
        floatArrayOf(-1f, -1f, 1f),
        floatArrayOf(1f, -1f, 1f),
        floatArrayOf(1f, 1f, 1f),
        floatArrayOf(-1f, 1f, 1f)
    )

    // Project 3D -> 2D with perspective division
    val projected = rawVertices.map { v ->
        // Rotate Y
        val x1 = v[0] * cosY + v[2] * sinY
        val z1 = -v[0] * sinY + v[2] * cosY
        // Rotate X
        val y2 = v[1] * cosX - z1 * sinX
        val z2 = v[1] * sinX + z1 * cosX

        val fov = 3.2f
        val pz = z2 + fov
        val px = cx + (x1 / pz) * scale * 2.2f
        val py = cy + (y2 / pz) * scale * 2.2f
        Offset(px, py)
    }

    val edges = listOf(
        0 to 1, 1 to 2, 2 to 3, 3 to 0, // front
        4 to 5, 5 to 6, 6 to 7, 7 to 4, // back
        0 to 4, 1 to 5, 2 to 6, 3 to 7  // sides
    )

    // Draw solid inner core glow
    val coreRadius = 24f + (sin(frame * 0.05f) * 6f)
    drawCircle(
        color = PS2ElectricBlue.copy(alpha = 0.35f),
        radius = coreRadius,
        center = Offset(cx, cy)
    )

    // Draw Vector Unit transformed edges
    edges.forEachIndexed { index, (from, to) ->
        val edgeColor = when ((index + shapeMode) % 3) {
            0 -> PS2NeonCyan
            1 -> PS2ElectricBlue
            else -> PS2BrightGlow
        }
        drawLine(
            color = edgeColor,
            start = projected[from],
            end = projected[to],
            strokeWidth = 3.5f
        )
    }

    // Draw vertex joints
    projected.forEach { pt ->
        drawCircle(
            color = Color.White,
            radius = 4.5f,
            center = pt
        )
    }

    // VU1 Status overlay
    drawCircle(
        color = PS2SuccessGreen,
        radius = 5f,
        center = Offset(24f, 24f)
    )
}

// -------------------------------------------------------------------------------------------------
// 2. RETRO SPRINT 3D PSEUDO-RACER
// -------------------------------------------------------------------------------------------------
private fun DrawScope.drawRetroRacer(
    posX: Float,
    speed: Float,
    distance: Float,
    lap: Int,
    frame: Long
) {
    val horizonY = size.height * 0.44f
    val cx = size.width / 2f

    // Sky gradient
    drawRect(
        color = Color(0xFF040817),
        topLeft = Offset(0f, 0f),
        size = Size(size.width, horizonY)
    )

    // Distant cyber mountain line
    val mountainPath = Path().apply {
        moveTo(0f, horizonY)
        val mCount = 8
        val step = size.width / mCount
        for (i in 0..mCount) {
            val peakH = if (i % 2 == 0) 38f else 18f
            lineTo(i * step, horizonY - peakH)
        }
        lineTo(size.width, horizonY)
        close()
    }
    drawPath(mountainPath, color = Color(0xFF14203D))

    // Ground & Road
    drawRect(
        color = Color(0xFF0D1527),
        topLeft = Offset(0f, horizonY),
        size = Size(size.width, size.height - horizonY)
    )

    // Road segments projecting from horizon
    val segments = 24
    for (i in segments downTo 1) {
        val y1 = horizonY + ((i.toFloat() / segments).pow(2.2f) * (size.height - horizonY))
        val y2 = horizonY + (((i + 1).toFloat() / segments).pow(2.2f) * (size.height - horizonY))

        val w1 = ((y1 - horizonY) / (size.height - horizonY)) * size.width * 0.75f
        val w2 = ((y2 - horizonY) / (size.height - horizonY)) * size.width * 0.75f

        val roadXOffset = sin((distance * 0.003f) + (i * 0.12f)) * (1.0f - (i.toFloat() / segments)) * 60f

        val roadLeft1 = (cx - w1 / 2f) + roadXOffset
        val roadRight1 = (cx + w1 / 2f) + roadXOffset
        val roadLeft2 = (cx - w2 / 2f) + roadXOffset
        val roadRight2 = (cx + w2 / 2f) + roadXOffset

        val isLight = ((distance * 0.05f + i).toInt() % 2 == 0)
        val roadColor = if (isLight) Color(0xFF1B2335) else Color(0xFF151C2C)
        val curbColor = if (isLight) PS2CircleRed else Color.White

        // Road Surface
        val roadQuad = Path().apply {
            moveTo(roadLeft1, y1)
            lineTo(roadRight1, y1)
            lineTo(roadRight2, y2)
            lineTo(roadLeft2, y2)
            close()
        }
        drawPath(roadQuad, color = roadColor)

        // Left & Right Curbs
        val curbW1 = w1 * 0.06f
        val curbW2 = w2 * 0.06f
        drawLine(curbColor, Offset(roadLeft1, y1), Offset(roadLeft2, y2), strokeWidth = max(2f, curbW2))
        drawLine(curbColor, Offset(roadRight1, y1), Offset(roadRight2, y2), strokeWidth = max(2f, curbW2))
    }

    // Player Racecar
    val carY = size.height * 0.82f
    val carX = cx + (posX * size.width * 0.28f)
    val carW = 100f
    val carH = 46f

    // Car shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.6f),
        topLeft = Offset(carX - carW * 0.6f, carY + carH * 0.7f),
        size = Size(carW * 1.2f, 18f)
    )

    // Car Body (PS2 sleek cyan sports chassis)
    val carBody = Path().apply {
        moveTo(carX - carW * 0.5f, carY + carH * 0.5f)
        lineTo(carX - carW * 0.4f, carY - carH * 0.2f)
        lineTo(carX + carW * 0.4f, carY - carH * 0.2f)
        lineTo(carX + carW * 0.5f, carY + carH * 0.5f)
        close()
    }
    drawPath(carBody, color = PS2ElectricBlue)

    // Cockpit windshield
    drawRect(
        color = PS2NeonCyan.copy(alpha = 0.8f),
        topLeft = Offset(carX - carW * 0.25f, carY - carH * 0.15f),
        size = Size(carW * 0.5f, carH * 0.35f)
    )

    // Rear Spoiler
    drawRect(
        color = Color(0xFF0F1524),
        topLeft = Offset(carX - carW * 0.45f, carY - carH * 0.35f),
        size = Size(carW * 0.9f, 6f)
    )

    // Tires
    drawRect(Color.Black, topLeft = Offset(carX - carW * 0.52f, carY + carH * 0.1f), size = Size(14f, 24f))
    drawRect(Color.Black, topLeft = Offset(carX + carW * 0.40f, carY + carH * 0.1f), size = Size(14f, 24f))

    // Taillights
    val brakeLight = if (speed < 30f) Color(0xFFFF3B30) else Color(0xFFFF6B6B)
    drawRect(brakeLight, topLeft = Offset(carX - carW * 0.38f, carY + carH * 0.35f), size = Size(16f, 6f))
    drawRect(brakeLight, topLeft = Offset(carX + carW * 0.24f, carY + carH * 0.35f), size = Size(16f, 6f))
}

// -------------------------------------------------------------------------------------------------
// 3. DUALSHOCK 2 CONTROLLER DIAGNOSTICS LAB
// -------------------------------------------------------------------------------------------------
private fun DrawScope.drawDualShockCalibration(
    cross: Boolean, circle: Boolean, square: Boolean, triangle: Boolean,
    dpadUp: Boolean, dpadDown: Boolean, dpadLeft: Boolean, dpadRight: Boolean,
    l1: Boolean, r1: Boolean, leftX: Float, leftY: Float, analogOn: Boolean
) {
    val cx = size.width / 2f
    val cy = size.height / 2f

    // Draw Controller Silhouette
    drawRoundRect(
        color = PS2SurfaceElevated,
        topLeft = Offset(cx - 190f, cy - 110f),
        size = Size(380f, 220f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
    )

    // Left analog scope
    drawCircle(color = PS2SurfaceDark, radius = 48f, center = Offset(cx - 85f, cy + 32f))
    drawCircle(
        color = if (analogOn) PS2NeonCyan else PS2TextTertiary,
        radius = 16f,
        center = Offset(cx - 85f + (leftX * 32f), cy + 32f + (leftY * 32f))
    )

    // Right analog scope
    drawCircle(color = PS2SurfaceDark, radius = 48f, center = Offset(cx + 85f, cy + 32f))
    drawCircle(
        color = if (analogOn) PS2ElectricBlue else PS2TextTertiary,
        radius = 16f,
        center = Offset(cx + 85f, cy + 32f)
    )

    // D-Pad feedback
    val dpadCenter = Offset(cx - 130f, cy - 36f)
    drawRect(if (dpadUp) PS2ElectricBlue else PS2SurfaceDark, topLeft = Offset(dpadCenter.x - 10f, dpadCenter.y - 28f), size = Size(20f, 18f))
    drawRect(if (dpadDown) PS2ElectricBlue else PS2SurfaceDark, topLeft = Offset(dpadCenter.x - 10f, dpadCenter.y + 10f), size = Size(20f, 18f))
    drawRect(if (dpadLeft) PS2ElectricBlue else PS2SurfaceDark, topLeft = Offset(dpadCenter.x - 28f, dpadCenter.y - 10f), size = Size(18f, 20f))
    drawRect(if (dpadRight) PS2ElectricBlue else PS2SurfaceDark, topLeft = Offset(dpadCenter.x + 10f, dpadCenter.y - 10f), size = Size(18f, 20f))

    // Action buttons feedback
    val actionCenter = Offset(cx + 130f, cy - 36f)
    drawCircle(if (triangle) PS2TriangleGreen else PS2SurfaceDark, radius = 10f, center = Offset(actionCenter.x, actionCenter.y - 20f))
    drawCircle(if (circle) PS2CircleRed else PS2SurfaceDark, radius = 10f, center = Offset(actionCenter.x + 20f, actionCenter.y))
    drawCircle(if (cross) PS2CrossBlue else PS2SurfaceDark, radius = 10f, center = Offset(actionCenter.x, actionCenter.y + 20f))
    drawCircle(if (square) PS2SquarePink else PS2SurfaceDark, radius = 10f, center = Offset(actionCenter.x - 20f, actionCenter.y))

    // Shoulders
    drawRect(if (l1) PS2ElectricBlue else PS2SurfaceBorder, topLeft = Offset(cx - 170f, cy - 100f), size = Size(46f, 14f))
    drawRect(if (r1) PS2ElectricBlue else PS2SurfaceBorder, topLeft = Offset(cx + 124f, cy - 100f), size = Size(46f, 14f))
}

// -------------------------------------------------------------------------------------------------
// 4. EMOTION ENGINE 32-BIT BENCHMARK
// -------------------------------------------------------------------------------------------------
private fun DrawScope.drawEeBenchmark(
    progress: Float,
    scores: FloatArray
) {
    val margin = 36f
    val barWidth = size.width - (margin * 2f)
    val startY = 48f

    // 4 benchmark bars
    val labels = listOf("ARMv7 NEON Instructions", "VU1 Vector GFLOPS", "GS Fillrate (MPix/s)", "32-Bit Memory Throughput")
    val maxValues = floatArrayOf(800f, 400f, 3800f, 900f)

    labels.forEachIndexed { index, label ->
        val y = startY + (index * 42f)
        val score = scores.getOrElse(index) { 0f }
        val pct = (score / maxValues[index]).coerceIn(0f, 1f)

        // Background Track
        drawRoundRect(
            color = PS2SurfaceElevated,
            topLeft = Offset(margin, y),
            size = Size(barWidth, 16f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )

        // Filled bar
        val barColor = when (index) {
            0 -> PS2NeonCyan
            1 -> PS2ElectricBlue
            2 -> PS2SuccessGreen
            else -> PS2BrightGlow
        }
        drawRoundRect(
            color = barColor,
            topLeft = Offset(margin, y),
            size = Size(barWidth * pct, 16f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
    }

    // Overall Progress
    val progY = startY + (4 * 44f) + 12f
    drawRect(
        color = PS2SurfaceDark,
        topLeft = Offset(margin, progY),
        size = Size(barWidth, 8f)
    )
    drawRect(
        color = PS2WarningOrange,
        topLeft = Offset(margin, progY),
        size = Size(barWidth * (progress / 100f), 8f)
    )
}

// -------------------------------------------------------------------------------------------------
// 5. GENERIC PS2 ROM FALLBACK OUTPUT
// -------------------------------------------------------------------------------------------------
private fun DrawScope.drawGenericPs2Output(game: GameItem, rot: Float, frame: Long) {
    val cx = size.width / 2f
    val cy = size.height / 2f

    drawRect(Color(0xFF060B18), size = size)

    // Rotating PS2 3D Monolith
    val rad = Math.toRadians(rot.toDouble())
    val w = 90f * cos(rad).toFloat()
    val h = 180f

    drawRect(
        color = PS2ElectricBlue.copy(alpha = 0.85f),
        topLeft = Offset(cx - abs(w) / 2f, cy - h / 2f),
        size = Size(abs(w), h)
    )

    drawRect(
        color = PS2NeonCyan,
        topLeft = Offset(cx - abs(w) / 2f, cy - h / 2f),
        size = Size(abs(w), h),
        style = Stroke(width = 3f)
    )
}
