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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberTactical
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanNeon
import com.example.viewmodel.SipraStatus
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun AudioWaveformVisualizer(
    status: SipraStatus,
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VisualizerTransition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Animatable 1: Baseline electric current breathing amplitude
    val baselineBreathingAmp = remember { Animatable(0.06f) }

    // Animatable 2: Holographic visualizer brackets offset
    val bracketBreathingOffset = remember { Animatable(0f) }

    // Animatable 3: Visualizer brackets glow alpha
    val bracketBreathingAlpha = remember { Animatable(0.35f) }

    LaunchedEffect(status) {
        val (cycleDuration, minAmp, maxAmp) = when (status) {
            SipraStatus.IDLE -> Triple(2600, 0.04f, 0.12f)
            SipraStatus.LISTENING -> Triple(1100, 0.12f, 0.28f)
            SipraStatus.PROCESSING -> Triple(800, 0.15f, 0.32f)
            SipraStatus.SPEAKING -> Triple(1200, 0.14f, 0.30f)
            SipraStatus.ERROR -> Triple(600, 0.05f, 0.15f)
        }
        val half = cycleDuration / 2

        launch {
            while (isActive) {
                baselineBreathingAmp.animateTo(
                    targetValue = maxAmp,
                    animationSpec = tween(durationMillis = half, easing = FastOutSlowInEasing)
                )
                baselineBreathingAmp.animateTo(
                    targetValue = minAmp,
                    animationSpec = tween(durationMillis = half, easing = FastOutSlowInEasing)
                )
            }
        }

        launch {
            while (isActive) {
                bracketBreathingOffset.animateTo(
                    targetValue = 4f,
                    animationSpec = tween(durationMillis = half, easing = FastOutSlowInEasing)
                )
                bracketBreathingOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = half, easing = FastOutSlowInEasing)
                )
            }
        }

        launch {
            while (isActive) {
                bracketBreathingAlpha.animateTo(
                    targetValue = 0.85f,
                    animationSpec = tween(durationMillis = half, easing = FastOutSlowInEasing)
                )
                bracketBreathingAlpha.animateTo(
                    targetValue = 0.30f,
                    animationSpec = tween(durationMillis = half, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    val activeColor = if (status == SipraStatus.LISTENING) AmberTactical else CyanNeon
    val accentColor = if (status == SipraStatus.LISTENING) Color(0xFFFFD166) else CyanBright

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
    ) {
        val barCount = 28
        val spacing = 3.dp.toPx()
        val sidePadding = 12.dp.toPx()
        val availableWidth = size.width - (sidePadding * 2)
        val totalSpacing = spacing * (barCount - 1)
        val barWidth = ((availableWidth - totalSpacing) / barCount).coerceAtLeast(2.dp.toPx())
        val maxHeight = size.height

        val isActive = status == SipraStatus.SPEAKING || status == SipraStatus.LISTENING || status == SipraStatus.PROCESSING
        val effectiveAmp = if (isActive) amplitude.coerceIn(0.12f, 1f) else baselineBreathingAmp.value

        // Draw left and right flanking holographic measurement brackets
        val bracketColor = accentColor.copy(alpha = bracketBreathingAlpha.value)
        val bOffset = bracketBreathingOffset.value.dp.toPx()

        // Left HUD bracket [
        val leftX = sidePadding - 6.dp.toPx() - bOffset
        drawLine(
            color = bracketColor,
            start = Offset(leftX, 8.dp.toPx()),
            end = Offset(leftX, maxHeight - 8.dp.toPx()),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = bracketColor,
            start = Offset(leftX, 8.dp.toPx()),
            end = Offset(leftX + 4.dp.toPx(), 8.dp.toPx()),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = bracketColor,
            start = Offset(leftX, maxHeight - 8.dp.toPx()),
            end = Offset(leftX + 4.dp.toPx(), maxHeight - 8.dp.toPx()),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Right HUD bracket ]
        val rightX = size.width - sidePadding + 6.dp.toPx() + bOffset
        drawLine(
            color = bracketColor,
            start = Offset(rightX, 8.dp.toPx()),
            end = Offset(rightX, maxHeight - 8.dp.toPx()),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = bracketColor,
            start = Offset(rightX, 8.dp.toPx()),
            end = Offset(rightX - 4.dp.toPx(), 8.dp.toPx()),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = bracketColor,
            start = Offset(rightX, maxHeight - 8.dp.toPx()),
            end = Offset(rightX - 4.dp.toPx(), maxHeight - 8.dp.toPx()),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Draw breathing waveform harmonic bars
        for (i in 0 until barCount) {
            val harmonic1 = sin(phase + (i * 0.35f))
            val harmonic2 = sin(phase * 1.5f + (i * 0.2f))
            val combinedWave = ((harmonic1 + harmonic2) / 2f + 1f) / 2f // 0 .. 1

            val barHeight = ((combinedWave * effectiveAmp * maxHeight) + 3.dp.toPx()).coerceIn(4.dp.toPx(), maxHeight)
            val left = sidePadding + i * (barWidth + spacing)
            val top = (maxHeight - barHeight) / 2f

            val barAlpha = if (isActive) {
                0.95f
            } else {
                (bracketBreathingAlpha.value * 0.7f).coerceIn(0.2f, 0.55f)
            }

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = barAlpha),
                        activeColor.copy(alpha = barAlpha * 0.75f)
                    ),
                    startY = top,
                    endY = top + barHeight
                ),
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

