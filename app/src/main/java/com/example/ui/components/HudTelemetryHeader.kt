package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.system.SystemStats
import com.example.ui.theme.AmberTactical
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.HudCardBorder
import com.example.ui.theme.HudCardSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.SipraStatus
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun HudTelemetryHeader(
    stats: SystemStats,
    status: SipraStatus,
    isMuted: Boolean,
    isTorchOn: Boolean,
    onToggleMute: () -> Unit,
    onToggleTorch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWizard: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Animatable 1: Holographic header border & corner bracket luminescence
    val headerGlowAlpha = remember { Animatable(0.35f) }

    // Animatable 2: Status beacon scale pulse (living heartbeat)
    val statusDotPulse = remember { Animatable(1f) }

    // Animatable 3: Status beacon glow opacity
    val statusDotGlow = remember { Animatable(0.5f) }

    // Animatable 4: Telemetry readout tick breathing
    val telemetryPulse = remember { Animatable(0.85f) }

    LaunchedEffect(status) {
        val (cycleMs, beaconMinScale, beaconMaxScale) = when (status) {
            SipraStatus.IDLE -> Triple(2800, 0.90f, 1.25f)
            SipraStatus.LISTENING -> Triple(1200, 0.85f, 1.45f)
            SipraStatus.PROCESSING -> Triple(800, 0.82f, 1.55f)
            SipraStatus.SPEAKING -> Triple(1300, 0.88f, 1.38f)
            SipraStatus.ERROR -> Triple(500, 0.80f, 1.40f)
        }
        val halfCycle = cycleMs / 2

        // Living border luminescence pulse
        launch {
            while (isActive) {
                headerGlowAlpha.animateTo(
                    targetValue = 0.75f,
                    animationSpec = tween(durationMillis = halfCycle, easing = FastOutSlowInEasing)
                )
                headerGlowAlpha.animateTo(
                    targetValue = 0.28f,
                    animationSpec = tween(durationMillis = halfCycle, easing = FastOutSlowInEasing)
                )
            }
        }

        // Heartbeat pulse for status indicator beacon
        launch {
            while (isActive) {
                statusDotPulse.animateTo(
                    targetValue = beaconMaxScale,
                    animationSpec = tween(durationMillis = halfCycle, easing = FastOutSlowInEasing)
                )
                statusDotPulse.animateTo(
                    targetValue = beaconMinScale,
                    animationSpec = tween(durationMillis = halfCycle, easing = FastOutSlowInEasing)
                )
            }
        }

        launch {
            while (isActive) {
                statusDotGlow.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = halfCycle, easing = FastOutSlowInEasing)
                )
                statusDotGlow.animateTo(
                    targetValue = 0.40f,
                    animationSpec = tween(durationMillis = halfCycle, easing = FastOutSlowInEasing)
                )
            }
        }

        // Telemetry readout pulse
        launch {
            while (isActive) {
                telemetryPulse.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = halfCycle, easing = FastOutSlowInEasing)
                )
                telemetryPulse.animateTo(
                    targetValue = 0.75f,
                    animationSpec = tween(durationMillis = halfCycle, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    val statusColor = when (status) {
        SipraStatus.LISTENING -> AmberTactical
        SipraStatus.SPEAKING -> CyanNeon
        SipraStatus.PROCESSING -> CyanBright
        SipraStatus.ERROR -> Color(0xFFEF4444)
        SipraStatus.IDLE -> EmeraldOnline
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(HudCardSurface)
            .border(
                width = 1.dp,
                color = CyanNeon.copy(alpha = headerGlowAlpha.value.coerceIn(0.2f, 0.8f)),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
            .drawBehind {
                // Subtle holographic corner brackets with breathing glow
                drawHolographicCornerBrackets(
                    color = CyanNeon.copy(alpha = (headerGlowAlpha.value * 0.7f).coerceIn(0.15f, 0.7f)),
                    bracketLength = 10.dp.toPx(),
                    strokeWidth = 1.dp.toPx(),
                    inset = 4.dp.toPx()
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top row: Brand & Status + Action Icons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Glowing status indicator beacon with breathing aura via Animatable
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp * statusDotPulse.value)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = (statusDotGlow.value * 0.35f).coerceIn(0f, 0.5f)))
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = statusDotGlow.value))
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LYRA",
                    style = MaterialTheme.typography.titleLarge,
                    color = CyanNeon
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "// MK-IV",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            // Quick HUD Action Icons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Torch toggle
                IconButton(
                    onClick = onToggleTorch,
                    modifier = Modifier.size(36.dp).testTag("hud_torch_toggle")
                ) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = "Tactical Torch",
                        tint = if (isTorchOn) AmberTactical else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Mute toggle
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier.size(36.dp).testTag("hud_mute_toggle")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Mute Voice",
                        tint = if (isMuted) Color(0xFFEF4444) else CyanBright,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 6-Step Setup Wizard
                IconButton(
                    onClick = onOpenWizard,
                    modifier = Modifier.size(36.dp).testTag("hud_setup_wizard_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Setup Wizard (6 Steps)",
                        tint = CyanNeon,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Settings
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(36.dp).testTag("hud_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Diagnostic Telemetry Readouts Row with subtle breathing luminescence
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Power / Battery
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (stats.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                    contentDescription = null,
                    tint = if (stats.batteryPct <= 20) AmberTactical else EmeraldOnline,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "PWR ${stats.batteryPct}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary
                )
            }

            // Memory RAM
            Text(
                text = "RAM ${stats.ramUsedMb}/${stats.ramTotalMb}MB",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = (0.7f + telemetryPulse.value * 0.3f).coerceIn(0f, 1f))
            )

            // Chronometer time
            Text(
                text = stats.currentTime.ifBlank { "00:00:00" },
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                color = CyanBright
            )

            // Protocol Status with living status label pulse
            val statusLabel = when (status) {
                SipraStatus.LISTENING -> "LISTENING"
                SipraStatus.PROCESSING -> "COMPUTING"
                SipraStatus.SPEAKING -> "VOCALIZING"
                SipraStatus.ERROR -> "FAULT"
                SipraStatus.IDLE -> "STANDBY"
            }
            Text(
                text = "SYS: $statusLabel",
                style = MaterialTheme.typography.labelSmall,
                color = if (status == SipraStatus.IDLE) EmeraldOnline else AmberTactical
            )
        }
    }
}

