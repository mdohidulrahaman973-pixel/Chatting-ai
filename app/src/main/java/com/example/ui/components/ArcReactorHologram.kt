package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberTactical
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.GoldCore
import com.example.ui.theme.CrimsonAlert
import com.example.viewmodel.SipraStatus
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorHologram(
    status: SipraStatus,
    amplitude: Float,
    onCoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorTransitions")

    // Slow continuous rotations for outer gyroscopic mechanics
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (status == SipraStatus.PROCESSING) 3000 else 18000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRotation"
    )

    val middleRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (status == SipraStatus.PROCESSING) 2000 else 12000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "middleRotation"
    )

    // Animatable 1: Organic breathing scale (respiration inhale & exhale)
    val coreBreathingScale = remember { Animatable(1f) }

    // Animatable 2: Holographic luminescence glow alpha
    val coreGlowAlpha = remember { Animatable(0.45f) }

    // Animatable 3: Concentric holographic resonance wave expansion
    val resonanceWaveRadius = remember { Animatable(0.82f) }

    // Animatable 4: Tactical HUD reticle bracket expansion
    val reticleOffsetAnim = remember { Animatable(0f) }

    // Animatable 5: Tactile click burst ripple
    val tapBurstScale = remember { Animatable(1f) }

    // Coordinated respiration loops driven by Compose Animatable
    LaunchedEffect(status) {
        val (inhaleMs, exhaleMs, minScale, maxScale, minGlow, maxGlow) = when (status) {
            SipraStatus.IDLE -> HexCadence(2800, 3200, 0.965f, 1.035f, 0.35f, 0.75f)
            SipraStatus.LISTENING -> HexCadence(1100, 1300, 0.940f, 1.065f, 0.55f, 0.95f)
            SipraStatus.PROCESSING -> HexCadence(700, 700, 0.920f, 1.085f, 0.60f, 1.00f)
            SipraStatus.SPEAKING -> HexCadence(1300, 1300, 0.950f, 1.060f, 0.50f, 0.90f)
            SipraStatus.ERROR -> HexCadence(500, 500, 0.930f, 1.050f, 0.40f, 0.85f)
        }

        // 1. Organic breathing scale loop
        launch {
            while (isActive) {
                coreBreathingScale.animateTo(
                    targetValue = maxScale,
                    animationSpec = tween(durationMillis = inhaleMs, easing = FastOutSlowInEasing)
                )
                coreBreathingScale.animateTo(
                    targetValue = minScale,
                    animationSpec = tween(durationMillis = exhaleMs, easing = FastOutSlowInEasing)
                )
            }
        }

        // 2. Holographic luminescence pulse loop
        launch {
            while (isActive) {
                coreGlowAlpha.animateTo(
                    targetValue = maxGlow,
                    animationSpec = tween(durationMillis = inhaleMs, easing = FastOutSlowInEasing)
                )
                coreGlowAlpha.animateTo(
                    targetValue = minGlow,
                    animationSpec = tween(durationMillis = exhaleMs, easing = FastOutSlowInEasing)
                )
            }
        }

        // 3. Outward holographic resonance wave loop
        launch {
            val waveDuration = inhaleMs + exhaleMs
            while (isActive) {
                resonanceWaveRadius.animateTo(
                    targetValue = 1.18f,
                    animationSpec = tween(durationMillis = waveDuration, easing = LinearEasing)
                )
                resonanceWaveRadius.snapTo(0.80f)
            }
        }

        // 4. Reticle brackets subtle breathing offset loop
        launch {
            while (isActive) {
                reticleOffsetAnim.animateTo(
                    targetValue = 5f,
                    animationSpec = tween(durationMillis = inhaleMs, easing = FastOutSlowInEasing)
                )
                reticleOffsetAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = exhaleMs, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    // State colors
    val corePrimaryColor = when (status) {
        SipraStatus.LISTENING -> AmberTactical
        SipraStatus.PROCESSING -> CyanNeon
        SipraStatus.SPEAKING -> CyanBright
        SipraStatus.ERROR -> CrimsonAlert
        SipraStatus.IDLE -> CyanNeon
    }

    val coreAccentColor = when (status) {
        SipraStatus.LISTENING -> GoldCore
        SipraStatus.PROCESSING -> Color.White
        SipraStatus.SPEAKING -> GoldCore
        SipraStatus.ERROR -> AmberTactical
        SipraStatus.IDLE -> CyanBright
    }

    // Dynamic scale driven by Compose Animatable + live audio amplitude
    val livingScale = (coreBreathingScale.value * tapBurstScale.value).coerceIn(0.88f, 1.25f)
    val dynamicPulse = (livingScale + (amplitude * 0.40f)).coerceIn(0.85f, 1.65f)

    Box(
        modifier = modifier
            .size(240.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    coroutineScope.launch {
                        tapBurstScale.animateTo(1.10f, tween(90, easing = FastOutSlowInEasing))
                        tapBurstScale.animateTo(1f, tween(240, easing = FastOutSlowInEasing))
                    }
                    onCoreClick()
                }
            )
            .testTag("arc_reactor_core"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f

            // 1. Ambient outer glow gradient modulated by Animatable coreGlowAlpha & dynamicPulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        corePrimaryColor.copy(alpha = (coreGlowAlpha.value * 0.35f * dynamicPulse).coerceIn(0f, 0.65f)),
                        corePrimaryColor.copy(alpha = (coreGlowAlpha.value * 0.12f).coerceIn(0f, 0.25f)),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius * 0.98f
                ),
                radius = maxRadius * 0.98f,
                center = center
            )

            // 2. Holographic resonance wave expanding outward via Animatable
            val currentWaveRadiusNorm = resonanceWaveRadius.value
            val waveAlpha = ((1.18f - currentWaveRadiusNorm) / (1.18f - 0.80f)).coerceIn(0f, 1f) * 0.42f
            drawCircle(
                color = corePrimaryColor.copy(alpha = waveAlpha),
                radius = maxRadius * currentWaveRadiusNorm,
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )

            // 3. Holographic Reticle Brackets [ ] breathing via Animatable reticleOffsetAnim
            val bracketOffsetPx = reticleOffsetAnim.value.dp.toPx()
            val bracketRadius = (maxRadius * 0.94f) + bracketOffsetPx
            val bracketColor = coreAccentColor.copy(alpha = (coreGlowAlpha.value * 0.6f).coerceIn(0.2f, 0.85f))
            val bracketAngles = listOf(45f, 135f, 225f, 315f)
            val bracketSpan = 18f
            for (ang in bracketAngles) {
                drawArc(
                    color = bracketColor,
                    startAngle = ang - bracketSpan / 2f,
                    sweepAngle = bracketSpan,
                    useCenter = false,
                    topLeft = Offset(center.x - bracketRadius, center.y - bracketRadius),
                    size = Size(bracketRadius * 2, bracketRadius * 2),
                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 4. Outer tactical compass ring with tick marks
            rotate(outerRotation, pivot = center) {
                val outerRingRadius = maxRadius * 0.88f * (0.98f + (coreBreathingScale.value - 1f) * 0.3f)
                drawCircle(
                    color = corePrimaryColor.copy(alpha = 0.38f),
                    radius = outerRingRadius,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Draw 24 telemetry ticks around outer ring
                val tickCount = 24
                for (i in 0 until tickCount) {
                    val angle = (i * 360f / tickCount) * (PI / 180f)
                    val isMajor = i % 6 == 0
                    val tickLen = if (isMajor) 9.dp.toPx() else 4.dp.toPx()
                    val p1 = Offset(
                        center.x + (outerRingRadius - tickLen) * cos(angle).toFloat(),
                        center.y + (outerRingRadius - tickLen) * sin(angle).toFloat()
                    )
                    val p2 = Offset(
                        center.x + outerRingRadius * cos(angle).toFloat(),
                        center.y + outerRingRadius * sin(angle).toFloat()
                    )
                    drawLine(
                        color = if (isMajor) coreAccentColor.copy(alpha = 0.85f) else corePrimaryColor.copy(alpha = 0.35f),
                        start = p1,
                        end = p2,
                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 5. Middle segmented ring with glowing arcs
            rotate(middleRotation, pivot = center) {
                val middleRadius = maxRadius * 0.72f * livingScale
                val arcSegments = 4
                val sweepAngle = 65f
                val gap = (360f - (arcSegments * sweepAngle)) / arcSegments

                for (i in 0 until arcSegments) {
                    val startAngle = i * (sweepAngle + gap)
                    drawArc(
                        color = corePrimaryColor.copy(alpha = (coreGlowAlpha.value + 0.1f).coerceIn(0.4f, 0.95f)),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - middleRadius, center.y - middleRadius),
                        size = Size(middleRadius * 2, middleRadius * 2),
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Node dot at each segment center
                    val midAngle = (startAngle + sweepAngle / 2f) * (PI / 180f)
                    val nodePos = Offset(
                        center.x + middleRadius * cos(midAngle).toFloat(),
                        center.y + middleRadius * sin(midAngle).toFloat()
                    )
                    drawCircle(
                        color = coreAccentColor,
                        radius = 3.dp.toPx(),
                        center = nodePos
                    )
                }
            }

            // 6. Acoustic waveform spikes during speech or listening
            if (status == SipraStatus.SPEAKING || status == SipraStatus.LISTENING) {
                val waveRadius = maxRadius * 0.58f * livingScale
                val waveCount = 16
                for (i in 0 until waveCount) {
                    val angle = (i * 360f / waveCount) * (PI / 180f)
                    val ampOffset = ((sin(i * 1.4 + outerRotation * 0.05).toFloat() + 1f) / 2f) * amplitude * 18.dp.toPx()
                    val startP = Offset(
                        center.x + waveRadius * cos(angle).toFloat(),
                        center.y + waveRadius * sin(angle).toFloat()
                    )
                    val endP = Offset(
                        center.x + (waveRadius + ampOffset + 3.dp.toPx()) * cos(angle).toFloat(),
                        center.y + (waveRadius + ampOffset + 3.dp.toPx()) * sin(angle).toFloat()
                    )
                    drawLine(
                        color = coreAccentColor.copy(alpha = (0.7f + amplitude * 0.3f).coerceIn(0f, 1f)),
                        start = startP,
                        end = endP,
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 7. Inner reactor ring
            val innerRingRadius = maxRadius * 0.44f * dynamicPulse
            drawCircle(
                color = corePrimaryColor.copy(alpha = (coreGlowAlpha.value + 0.15f).coerceIn(0.5f, 1f)),
                radius = innerRingRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 8. Central Arc Reactor Core
            val coreRadius = maxRadius * 0.28f * dynamicPulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        coreAccentColor,
                        corePrimaryColor,
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius * 1.25f
                ),
                radius = coreRadius,
                center = center
            )
        }

        // Center Action Indicator Icon
        when (status) {
            SipraStatus.LISTENING -> {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Listening",
                    tint = SpaceBlack,
                    modifier = Modifier.size(28.dp)
                )
            }
            SipraStatus.SPEAKING, SipraStatus.PROCESSING -> {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Interrupt",
                    tint = SpaceBlack,
                    modifier = Modifier.size(24.dp)
                )
            }
            else -> {
                // Subtle central node core
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                )
            }
        }
    }
}

private data class HexCadence(
    val inhaleMs: Int,
    val exhaleMs: Int,
    val minScale: Float,
    val maxScale: Float,
    val minGlow: Float,
    val maxGlow: Float
)

private val SpaceBlack = Color(0xFF030712)

