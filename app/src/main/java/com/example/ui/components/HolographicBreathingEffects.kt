package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.example.viewmodel.SipraStatus
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * State holder managing coordinated Animatable instances that produce
 * subtle, organic respiration (breathing scale, glow pulse, outward resonance, and scanline)
 * for futuristic holographic HUD components.
 */
class HolographicPulseState(
    val scaleAnimatable: Animatable<Float, AnimationVector1D>,
    val glowAnimatable: Animatable<Float, AnimationVector1D>,
    val ringExpansionAnimatable: Animatable<Float, AnimationVector1D>,
    val scanAnimatable: Animatable<Float, AnimationVector1D>
) {
    val scale: Float get() = scaleAnimatable.value
    val glow: Float get() = glowAnimatable.value
    val ringExpansion: Float get() = ringExpansionAnimatable.value
    val scanY: Float get() = scanAnimatable.value
}

/**
 * Remember a living, breathing holographic state driven by Compose Animatable.
 * The breathing rate and intensity smoothly adjust according to the Lyra assistant's status.
 */
@Composable
fun rememberHolographicPulseState(
    status: SipraStatus,
    minScale: Float = 0.97f,
    maxScale: Float = 1.03f,
    minGlow: Float = 0.35f,
    maxGlow: Float = 0.85f,
    baseDurationMillis: Int = 2600
): HolographicPulseState {
    val scaleAnim = remember { Animatable(1f) }
    val glowAnim = remember { Animatable((minGlow + maxGlow) / 2f) }
    val ringAnim = remember { Animatable(0.85f) }
    val scanAnim = remember { Animatable(0f) }

    LaunchedEffect(status, minScale, maxScale, minGlow, maxGlow, baseDurationMillis) {
        // Organic respiratory rate based on assistant operational status
        val speedFactor = when (status) {
            SipraStatus.IDLE -> 1.0f
            SipraStatus.LISTENING -> 0.58f
            SipraStatus.PROCESSING -> 0.42f
            SipraStatus.SPEAKING -> 0.68f
            SipraStatus.ERROR -> 0.35f
        }
        val cycleTime = (baseDurationMillis * speedFactor).toInt().coerceAtLeast(600)
        val inhaleTime = (cycleTime * 0.46f).toInt()
        val exhaleTime = cycleTime - inhaleTime

        // 1. Organic Respiration (Scale Inhale / Exhale)
        launch {
            while (isActive) {
                scaleAnim.animateTo(
                    targetValue = maxScale,
                    animationSpec = tween(durationMillis = inhaleTime, easing = FastOutSlowInEasing)
                )
                scaleAnim.animateTo(
                    targetValue = minScale,
                    animationSpec = tween(durationMillis = exhaleTime, easing = FastOutSlowInEasing)
                )
            }
        }

        // 2. Luminescence / Radiant Glow Pulse
        launch {
            while (isActive) {
                glowAnim.animateTo(
                    targetValue = maxGlow,
                    animationSpec = tween(durationMillis = inhaleTime, easing = FastOutSlowInEasing)
                )
                glowAnim.animateTo(
                    targetValue = minGlow,
                    animationSpec = tween(durationMillis = exhaleTime, easing = FastOutSlowInEasing)
                )
            }
        }

        // 3. Outward Holographic Wave / Resonance Ring
        launch {
            while (isActive) {
                ringAnim.animateTo(
                    targetValue = 1.16f,
                    animationSpec = tween(durationMillis = cycleTime, easing = LinearEasing)
                )
                ringAnim.snapTo(0.85f)
            }
        }

        // 4. Subtle Scanline / Vertical Holographic Sweep
        launch {
            while (isActive) {
                scanAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = cycleTime * 2, easing = LinearEasing)
                )
                scanAnim.snapTo(0f)
            }
        }
    }

    return remember(scaleAnim, glowAnim, ringAnim, scanAnim) {
        HolographicPulseState(
            scaleAnimatable = scaleAnim,
            glowAnimatable = glowAnim,
            ringExpansionAnimatable = ringAnim,
            scanAnimatable = scanAnim
        )
    }
}

/**
 * Tactical sci-fi holographic corner brackets for HUD elements.
 * Accents the living holographic frame appearance.
 */
fun DrawScope.drawHolographicCornerBrackets(
    color: Color,
    bracketLength: Float = 14.dp.toPx(),
    strokeWidth: Float = 1.5.dp.toPx(),
    inset: Float = 2.dp.toPx()
) {
    val left = inset
    val top = inset
    val right = size.width - inset
    val bottom = size.height - inset

    // Top-Left corner bracket
    drawLine(color, Offset(left, top), Offset(left + bracketLength, top), strokeWidth, StrokeCap.Square)
    drawLine(color, Offset(left, top), Offset(left, top + bracketLength), strokeWidth, StrokeCap.Square)

    // Top-Right corner bracket
    drawLine(color, Offset(right, top), Offset(right - bracketLength, top), strokeWidth, StrokeCap.Square)
    drawLine(color, Offset(right, top), Offset(right, top + bracketLength), strokeWidth, StrokeCap.Square)

    // Bottom-Left corner bracket
    drawLine(color, Offset(left, bottom), Offset(left + bracketLength, bottom), strokeWidth, StrokeCap.Square)
    drawLine(color, Offset(left, bottom), Offset(left, bottom - bracketLength), strokeWidth, StrokeCap.Square)

    // Bottom-Right corner bracket
    drawLine(color, Offset(right, bottom), Offset(right - bracketLength, bottom), strokeWidth, StrokeCap.Square)
    drawLine(color, Offset(right, bottom), Offset(right, bottom - bracketLength), strokeWidth, StrokeCap.Square)
}
